import os
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from langchain_openai import ChatOpenAI
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
from dotenv import load_dotenv

load_dotenv()

app = FastAPI()

vectorstore = None
retriever = None
rag_chain = None

DEEPBRICKS_API_KEY = os.getenv("DEEPBRICKS_API_KEY", "")
DEEPBRICKS_BASE_URL = os.getenv("DEEPBRICKS_BASE_URL", "https://api.deepbricks.ai/v1")

def init_rag():
    global vectorstore, retriever, rag_chain
    # index_path = os.path.join(os.path.dirname(__file__), 'faiss_index')
    index_path = r"C:\Users\MI\Desktop\faiss_index"
    if not os.path.exists(index_path):
        print("警告: 找不到 FAISS 索引。请先运行 build_knowledge_base.py。")
        return

    print("正在加载本地 Embedding 模型...")
    embeddings = HuggingFaceEmbeddings(
        model_name="BAAI/bge-small-zh-v1.5",
        model_kwargs={"device": "cpu"},
        encode_kwargs={"normalize_embeddings": True},
    )

    print("正在加载 FAISS 索引...")
    vectorstore = FAISS.load_local(index_path, embeddings, allow_dangerous_deserialization=True)
    retriever = vectorstore.as_retriever(search_kwargs={"k": 5})

    if not DEEPBRICKS_API_KEY:
        print("错误: 未配置 DEEPBRICKS_API_KEY，请在 .env 文件中设置")
        return

    try:
        llm = ChatOpenAI(
            api_key=DEEPBRICKS_API_KEY,
            base_url=DEEPBRICKS_BASE_URL,
            model="gpt-4o-mini",
            temperature=0,
        )
    except Exception as e:
        print(f"LLM 初始化失败，请检查 DEEPBRICKS_API_KEY: {e}")
        return

    system_prompt = """
你是一个专业的英语学习助教，叫 AI 学习助手。

以下是本平台学习资料中与用户问题最匹配的内容（包含单词释义、语法讲解、作文模板）：
{context}

请按以下方式回答：
1. 先用自然的口吻回答用户的问题，解释单词释义、用法、例句等
2. 回答末尾，根据参考内容中标注的"收录等级"，主动推荐平台功能，格式示例：
   - 收录等级为"四级词汇" → "这个单词属于四级词汇范围，你可以在「单词打卡」模块选择四级词书开始每日打卡练习"
   - 收录等级为"六级词汇" → "这个单词是六级词汇，建议在「单词打卡」模块选六级词书进行系统学习"
   - 收录等级为"牛津3000" → "这是牛津3000核心词汇，推荐在「单词打卡」模块从牛津3000词书入手"
   - 收录等级为"托福雅思" → "这个单词属于托福雅思词汇，可以在「单词打卡」模块选择托福雅思词书备考"
3. 如果参考内容里有例句，鼓励用户用例句中的句式仿写造句
4. 如果参考内容没有匹配（比如用户问语法问题），直接用你的英语教学知识回答，不要编造等级推荐
5. 禁止使用"知识库"、"向量数据库"、"检索"、"RAG"、"参考内容"、"根据资料"等字眼
"""

    prompt = ChatPromptTemplate.from_messages([
        ("system", system_prompt),
        ("human", "{input}"),
    ])

    def format_docs(docs):
        parts = []
        for doc in docs:
            content = doc.page_content
            meta = doc.metadata
            # 把等级信息显式附加到上下文，方便模型识别
            level = meta.get("level", "")
            if level:
                content += f"\n[收录等级: {level}]"
            parts.append(content)
        return "\n\n".join(parts)

    rag_chain = (
        {"context": retriever | format_docs, "input": RunnablePassthrough()}
        | prompt
        | llm
        | StrOutputParser()
    )
    print("RAG 系统初始化完成！")

@app.on_event("startup")
async def startup_event():
    init_rag()

class ChatRequest(BaseModel):
    query: str

class ChatResponse(BaseModel):
    answer: str

@app.post("/api/rag_chat", response_model=ChatResponse)
async def rag_chat(request: ChatRequest):
    if rag_chain is None:
        raise HTTPException(status_code=500, detail="RAG 系统未初始化（可能是索引丢失或 LLM Key 未配置）")

    try:
        response = rag_chain.invoke(request.query)
        return ChatResponse(answer=response)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
