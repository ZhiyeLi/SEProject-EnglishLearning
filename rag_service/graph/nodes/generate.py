# rag_service/graph/nodes/generate.py
import logging
from typing import AsyncIterator
from langchain_core.messages import HumanMessage, SystemMessage
from prompts.system import SYSTEM_PROMPT, NO_CONTEXT_PROMPT

logger = logging.getLogger(__name__)

_llm = None


def init_generate(llm):
    global _llm
    _llm = llm


def format_docs(docs: list) -> str:
    if not docs:
        return "(无匹配内容)"
    parts = []
    for doc in docs:
        content = doc.page_content
        meta = doc.metadata
        level = meta.get("level", "")
        if level:
            content += f"\n[收录等级: {level}]"
        parts.append(content)
    return "\n\n---\n\n".join(parts)


def _format_history(chat_history: list) -> str:
    if not chat_history:
        return "(新对话)"
    lines = []
    for msg in chat_history[-6:]:
        role = "用户" if msg.get("role") == "user" else "助手"
        content = str(msg.get("content", ""))[:300]
        lines.append(f"{role}: {content}")
    return "\n".join(lines)


def build_generate_messages(state: dict) -> list:
    query = state.get("query", "")
    docs = state.get("retrieved_docs", [])
    chat_history = state.get("chat_history", [])
    user_level = state.get("user_level", "intermediate")
    formatted_history = _format_history(chat_history)

    if docs:
        context = format_docs(docs)
        system_text = SYSTEM_PROMPT.format(
            context=context,
            chat_history=formatted_history,
            user_level=user_level,
        )
    else:
        system_text = NO_CONTEXT_PROMPT.format(
            chat_history=formatted_history,
            user_level=user_level,
            query=query,
        )

    return [
        SystemMessage(content=system_text),
        HumanMessage(content=query),
    ]


async def generate_node(state: dict) -> dict:
    try:
        if _llm is None:
            return {"answer": "AI 服务未初始化，请检查配置。"}

        messages = build_generate_messages(state)
        response = _llm.invoke(messages)
        answer = response.content
        logger.info(f"generate_node: generated {len(answer)} chars")
        return {"answer": answer}

    except Exception as e:
        logger.error(f"generate_node failed: {e}", exc_info=True)
        return {"answer": "AI 服务暂时繁忙，请稍后重试。"}


async def generate_node_stream(state: dict) -> AsyncIterator[str]:
    if _llm is None:
        yield "AI 服务未初始化，请检查配置。"
        return

    try:
        messages = build_generate_messages(state)
        async for chunk in _llm.astream(messages):
            if hasattr(chunk, "content") and chunk.content:
                yield chunk.content
    except Exception as e:
        logger.error(f"generate_node_stream failed: {e}", exc_info=True)
        yield "AI 服务暂时繁忙，请稍后重试。"
