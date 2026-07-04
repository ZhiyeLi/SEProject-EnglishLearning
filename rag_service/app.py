import os
import json
import uuid
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, Request
from pydantic import BaseModel
from langchain_openai import ChatOpenAI
from dotenv import load_dotenv
from starlette.responses import StreamingResponse

from retrieval.embeddings import EmbeddingService
from retrieval.hybrid_search import HybridSearchService


from graph.nodes.retrieve import init_retrieve
from graph.nodes.grade import init_grade
from graph.nodes.rewrite import init_rewrite
from graph.nodes.generate import init_generate
from graph.graph import build_graph
from memory.checkpoint import get_checkpointer

load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

graph = None
checkpointer = None

DEEPBRICKS_API_KEY = os.getenv("DEEPBRICKS_API_KEY", "")
DEEPBRICKS_BASE_URL = os.getenv("DEEPBRICKS_BASE_URL", "https://api.deepbricks.ai/v1")


@asynccontextmanager
async def lifespan(app: FastAPI):
    global graph, checkpointer
    init_rag()
    yield


app = FastAPI(lifespan=lifespan)


def init_rag():
    global graph, checkpointer

    index_dir = os.path.join(os.path.dirname(__file__), 'faiss_index')

    if not os.path.exists(index_dir):
        logger.error(f"FAISS 索引目录不存在: {index_dir}。请先运行 build_knowledge_base.py")
        return

    if not DEEPBRICKS_API_KEY:
        logger.error("未配置 DEEPBRICKS_API_KEY，请在 .env 中设置")
        return

    try:
        llm = ChatOpenAI(
            api_key=DEEPBRICKS_API_KEY,
            base_url=DEEPBRICKS_BASE_URL,
            model="gpt-4o-mini",
            temperature=0,
        )
        logger.info("LLM initialized")
    except Exception as e:
        logger.error(f"LLM 初始化失败: {e}")
        return

    try:
        embeddings = EmbeddingService()
        logger.info("EmbeddingService (BGE-M3) initialized")
    except Exception as e:
        logger.error(f"EmbeddingService 初始化失败: {e}")
        return

    try:
        hybrid_search = HybridSearchService.load(index_dir, embeddings)
        logger.info("HybridSearchService initialized")
    except Exception as e:
        logger.error(f"HybridSearchService 初始化失败: {e}")
        hybrid_search = None

    # Reranker disabled — BGE-M3 CUDA + reranker CPU conflict causes segfault on Windows.
    # Hybrid search (dense+sparse RRF) already provides good ranking quality.
    reranker = None

    init_retrieve(hybrid_search, reranker)
    init_grade(llm)
    init_rewrite(llm)
    init_generate(llm)

    try:
        checkpointer = get_checkpointer()
        graph = build_graph(checkpointer=checkpointer)
        logger.info("LangGraph compiled successfully")
    except Exception as e:
        logger.error(f"LangGraph 编译失败: {e}")
        return

    logger.info("RAG 系统初始化完成！")


class ChatRequest(BaseModel):
    query: str
    session_id: str | None = None
    user_level: str | None = "intermediate"


class ChatResponse(BaseModel):
    answer: str
    session_id: str


@app.post("/api/rag_chat")
async def rag_chat(request: ChatRequest, http_request: Request):
    if graph is None:
        raise HTTPException(status_code=500, detail="RAG 系统未初始化")

    query = request.query.strip()

    if not query:
        raise HTTPException(status_code=400, detail="请输入你的问题")

    if len(query) > 2000:
        query = query[:2000]

    session_id = request.session_id or str(uuid.uuid4())
    config = {"configurable": {"thread_id": session_id}}

    accept_header = http_request.headers.get("accept", "")

    # SSE streaming — token-level output
    if "text/event-stream" in accept_header:
        async def event_generator():
            initial_state = {
                "query": query,
                "chat_history": [],
                "retrieved_docs": [],
                "grade_result": "",
                "rewritten_query": "",
                "rewrite_count": 0,
                "answer": "",
                "user_level": request.user_level or "intermediate",
            }

            try:
                # Step 1: Run retrieval graph
                state = graph.invoke(initial_state, config=config)
                docs = state.get("retrieved_docs", [])

                # Step 2: Stream generation token by token
                gen_state = {**state, "query": query}
                from graph.nodes.generate import generate_node_stream
                async for token in generate_node_stream(gen_state):
                    data = json.dumps({"token": token}, ensure_ascii=False)
                    yield f"event: token\ndata: {data}\n\n"

                done = json.dumps({"done": True, "session_id": session_id}, ensure_ascii=False)
                yield f"event: done\ndata: {done}\n\n"
            except Exception as e:
                logger.error(f"SSE stream error: {e}", exc_info=True)
                err = json.dumps({"error": "stream error, retry"}, ensure_ascii=False)
                yield f"event: error\ndata: {err}\n\n"

        return StreamingResponse(
            event_generator(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "X-Accel-Buffering": "no",
            },
        )

    # JSON full response (backward compat)
    else:
        try:
            initial_state = {
                "query": query,
                "chat_history": [],
                "retrieved_docs": [],
                "grade_result": "",
                "rewritten_query": "",
                "rewrite_count": 0,
                "answer": "",
                "user_level": request.user_level or "intermediate",
            }

            # Step 1: Run retrieval graph
            state = graph.invoke(initial_state, config=config)

            # Step 2: Generate answer
            from graph.nodes.generate import generate_node
            gen_state = {**state, "query": query}
            result = generate_node(gen_state)
            return ChatResponse(answer=result.get("answer", ""), session_id=session_id)

        except Exception as e:
            logger.error(f"RAG chat error: {e}", exc_info=True)
            raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/health")
async def health_check():
    return {
        "status": "ok",
        "graph_initialized": graph is not None,
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
