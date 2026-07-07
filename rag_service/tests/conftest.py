# rag_service/tests/conftest.py
import pytest
from unittest.mock import MagicMock


@pytest.fixture
def mock_llm():
    llm = MagicMock()

    def mock_invoke(messages):
        prompt_text = str(messages)
        response = MagicMock()
        if "relevant" in prompt_text.lower() or "grade" in prompt_text.lower():
            response.content = "relevant"
        elif "rewrite" in prompt_text.lower() or "改写" in prompt_text.lower():
            response.content = "rewritten precise query"
        else:
            response.content = "This is a generated answer about the topic."
        return response

    llm.invoke.side_effect = mock_invoke
    return llm


@pytest.fixture
def mock_hybrid_search():
    from langchain_core.documents import Document
    svc = MagicMock()
    svc.search.return_value = [
        (Document(page_content="abandon 放弃 抛弃", metadata={"level": "四级词汇"}), 0.95),
        (Document(page_content="abandon oneself to 沉溺于", metadata={"level": "四级词汇"}), 0.80),
    ]
    return svc


@pytest.fixture
def base_state():
    return {
        "query": "abandon 是什么意思",
        "chat_history": [],
        "retrieved_docs": [],
        "grade_result": "",
        "rewritten_query": "",
        "rewrite_count": 0,
        "answer": "",
        "user_level": "intermediate",
    }
