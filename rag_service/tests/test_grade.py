import pytest
from unittest.mock import MagicMock
from graph.nodes.grade import grade_node, _format_docs_for_grade


class TestFormatDocsForGrade:
    def test_format_empty_docs(self):
        assert "无检索结果" in _format_docs_for_grade([])

    def test_format_single_doc(self):
        from langchain_core.documents import Document
        doc = Document(page_content="abandon 放弃")
        result = _format_docs_for_grade([doc])
        assert "[1]" in result
        assert "abandon 放弃" in result

    def test_format_truncates_long_content(self):
        from langchain_core.documents import Document
        doc = Document(page_content="x" * 1000)
        result = _format_docs_for_grade([doc])
        assert len(result) < 1000


class TestGradeNode:
    def _make_state(self, **overrides):
        state = {
            "query": "abandon 是什么意思",
            "retrieved_docs": [],
            "chat_history": [],
            "grade_result": "",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        state.update(overrides)
        return state

    def test_grade_empty_docs(self):
        result = grade_node(self._make_state(retrieved_docs=[]))
        assert result["grade_result"] == "irrelevant"

    def test_grade_empty_query(self):
        result = grade_node(self._make_state(query="", rewritten_query=""))
        assert result["grade_result"] == "irrelevant"

    def test_grade_relevant(self, monkeypatch):
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="relevant")
        monkeypatch.setattr("graph.nodes.grade._llm", mock_llm)
        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[Document(page_content="abandon 放弃")])
        assert grade_node(state)["grade_result"] == "relevant"

    def test_grade_irrelevant(self, monkeypatch):
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="irrelevant")
        monkeypatch.setattr("graph.nodes.grade._llm", mock_llm)
        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[Document(page_content="作文模板")])
        assert grade_node(state)["grade_result"] == "irrelevant"

    def test_grade_partial(self, monkeypatch):
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="partial")
        monkeypatch.setattr("graph.nodes.grade._llm", mock_llm)
        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[Document(page_content="abandon 放弃")])
        assert grade_node(state)["grade_result"] == "partial"

    def test_grade_llm_unexpected_output(self, monkeypatch):
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="somewhat related but unclear")
        monkeypatch.setattr("graph.nodes.grade._llm", mock_llm)
        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[Document(page_content="abandon 放弃")])
        assert grade_node(state)["grade_result"] == "relevant"

    def test_grade_llm_exception(self, monkeypatch):
        mock_llm = MagicMock()
        mock_llm.invoke.side_effect = Exception("API timeout")
        monkeypatch.setattr("graph.nodes.grade._llm", mock_llm)
        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[Document(page_content="abandon 放弃")])
        assert grade_node(state)["grade_result"] == "relevant"
