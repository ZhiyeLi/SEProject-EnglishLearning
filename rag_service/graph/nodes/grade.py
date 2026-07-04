import logging
from langchain_openai import ChatOpenAI
from prompts.grader import GRADE_PROMPT

logger = logging.getLogger(__name__)

_llm = None
VALID_GRADES = ["irrelevant", "partial", "relevant"]


def init_grade(llm: ChatOpenAI):
    global _llm
    _llm = llm


def _format_docs_for_grade(docs: list) -> str:
    if not docs:
        return "(无检索结果)"
    parts = []
    for i, doc in enumerate(docs, 1):
        content = doc.page_content[:500]
        parts.append(f"[{i}] {content}")
    return "\n".join(parts)


def grade_node(state: dict) -> dict:
    query = state.get("rewritten_query", "") or state.get("query", "")
    docs = state.get("retrieved_docs", [])

    if not query.strip():
        return {"grade_result": "irrelevant"}

    if not docs:
        logger.info("grade_node: no documents, defaulting to irrelevant")
        return {"grade_result": "irrelevant"}

    prompt_text = GRADE_PROMPT.format(
        query=query,
        documents=_format_docs_for_grade(docs),
    )

    try:
        if _llm is None:
            return {"grade_result": "relevant"}

        response = _llm.invoke(prompt_text)
        result = response.content.strip().lower()

        for grade in VALID_GRADES:
            if grade in result:
                logger.info(f"grade_node: {grade}")
                return {"grade_result": grade}

        logger.warning(f"grade_node: unexpected output '{result[:50]}', defaulting to relevant")
        return {"grade_result": "relevant"}

    except Exception as e:
        logger.error(f"grade_node failed: {e}", exc_info=True)
        return {"grade_result": "relevant"}
