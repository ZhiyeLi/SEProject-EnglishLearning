# rag_service/tests/test_rewrite.py
import pytest
from unittest.mock import MagicMock
from graph.nodes.rewrite import rewrite_node, _format_history


class TestFormatHistory:
    def test_format_empty_history(self):
        assert "无历史对话" in _format_history([])

    def test_format_single_turn(self):
        history = [
            {"role": "user", "content": "hello"},
            {"role": "assistant", "content": "hi there"},
        ]
        result = _format_history(history)
        assert "用户: hello" in result
        assert "助手: hi there" in result

    def test_format_truncates_long_content(self):
        history = [{"role": "user", "content": "x" * 500}]
        result = _format_history(history)
        assert len(result) < 300


class TestRewriteNode:
    def _make_state(self, **overrides):
        state = {
            "query": "abandon",
            "chat_history": [],
            "retrieved_docs": [],
            "grade_result": "irrelevant",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        state.update(overrides)
        return state

    def test_rewrite_expands_query(self, monkeypatch):
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="abandon definition usage examples")
        monkeypatch.setattr("graph.nodes.rewrite._llm", mock_llm)
        result = rewrite_node(self._make_state(query="abandon"))
        assert "abandon" in result["rewritten_query"]
        assert result["rewrite_count"] == 1

    def test_rewrite_increments_count(self, monkeypatch):
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="rewritten query")
        monkeypatch.setattr("graph.nodes.rewrite._llm", mock_llm)
        state = self._make_state(rewrite_count=0)
        assert rewrite_node(state)["rewrite_count"] == 1
        state2 = self._make_state(rewrite_count=1)
        assert rewrite_node(state2)["rewrite_count"] == 2

    def test_rewrite_empty_query(self):
        result = rewrite_node(self._make_state(query=""))
        assert result["rewrite_count"] == 1

    def test_rewrite_llm_exception(self, monkeypatch):
        mock_llm = MagicMock()
        mock_llm.invoke.side_effect = Exception("timeout")
        monkeypatch.setattr("graph.nodes.rewrite._llm", mock_llm)
        state = self._make_state(query="original query")
        assert rewrite_node(state)["rewritten_query"] == "original query"
