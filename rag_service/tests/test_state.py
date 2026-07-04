import pytest
from graph.state import AgentState
from langchain_core.documents import Document


class TestAgentState:
    """验证 AgentState TypedDict 的字段和默认行为。"""

    def test_state_has_required_fields(self):
        """状态字典应包含所有必需字段。"""
        required_fields = [
            "query", "chat_history", "retrieved_docs",
            "grade_result", "rewritten_query", "rewrite_count",
            "answer", "user_level"
        ]
        for field in required_fields:
            assert field in AgentState.__annotations__, f"Missing field: {field}"

    def test_state_initialization_minimal(self):
        """用最小字段初始化状态。"""
        state: AgentState = {
            "query": "hello",
            "chat_history": [],
            "retrieved_docs": [],
            "grade_result": "",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        assert state["query"] == "hello"
        assert state["user_level"] == "intermediate"
        assert state["rewrite_count"] == 0

    def test_chat_history_uses_operator_add(self):
        """chat_history 应使用 operator.add reducer。"""
        import operator
        from typing import get_type_hints
        hints = get_type_hints(AgentState, include_extras=True)
        assert hints["chat_history"].__metadata__[0] is operator.add

    def test_retrieved_docs_accepts_documents(self):
        """retrieved_docs 应接受 Document 列表。"""
        doc = Document(page_content="test", metadata={})
        state: AgentState = {
            "query": "test",
            "chat_history": [],
            "retrieved_docs": [doc],
            "grade_result": "",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        assert len(state["retrieved_docs"]) == 1
        assert state["retrieved_docs"][0].page_content == "test"
