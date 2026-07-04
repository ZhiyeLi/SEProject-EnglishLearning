# rag_service/graph/nodes/rewrite.py
import logging
from prompts.grader import REWRITE_PROMPT

logger = logging.getLogger(__name__)

_llm = None


def init_rewrite(llm):
    global _llm
    _llm = llm


def _format_history(chat_history: list) -> str:
    if not chat_history:
        return "(无历史对话)"
    lines = []
    for msg in chat_history[-6:]:
        role = "用户" if msg.get("role") == "user" else "助手"
        content = str(msg.get("content", ""))[:200]
        lines.append(f"{role}: {content}")
    return "\n".join(lines)


def rewrite_node(state: dict) -> dict:
    query = state.get("query", "")
    chat_history = state.get("chat_history", [])
    current_count = state.get("rewrite_count", 0)

    if not query.strip():
        return {"rewritten_query": query, "rewrite_count": current_count + 1}

    prompt_text = REWRITE_PROMPT.format(
        query=query,
        chat_history=_format_history(chat_history),
    )

    try:
        if _llm is None:
            return {"rewritten_query": query, "rewrite_count": current_count + 1}

        response = _llm.invoke(prompt_text)
        rewritten = response.content.strip()
        logger.info(f"rewrite_node: '{query}' → '{rewritten}'")
        return {"rewritten_query": rewritten, "rewrite_count": current_count + 1}

    except Exception as e:
        logger.error(f"rewrite_node failed: {e}", exc_info=True)
        return {"rewritten_query": query, "rewrite_count": current_count + 1}
