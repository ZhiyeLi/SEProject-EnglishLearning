import pytest
from graph.state import AgentState


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

    def test_chat_history_uses_add_messages(self):
        """chat_history 字段使用 add_messages reducer。"""
        assert "add_messages" in str(AgentState.__annotations__["chat_history"])
