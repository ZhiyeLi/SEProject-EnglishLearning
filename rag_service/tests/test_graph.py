# rag_service/tests/test_graph.py
import pytest
from unittest.mock import MagicMock
from graph.graph import route_after_grade, build_graph


class TestRouteAfterGrade:
    def _make_state(self, grade_result, rewrite_count=0):
        return {
            "query": "test",
            "chat_history": [],
            "retrieved_docs": [],
            "grade_result": grade_result,
            "rewritten_query": "",
            "rewrite_count": rewrite_count,
            "answer": "",
            "user_level": "intermediate",
        }

    def test_route_relevant_to_generate(self):
        assert route_after_grade(self._make_state("relevant")) == "generate"

    def test_route_partial_to_generate(self):
        assert route_after_grade(self._make_state("partial")) == "generate"

    def test_route_irrelevant_to_rewrite(self):
        assert route_after_grade(self._make_state("irrelevant", rewrite_count=0)) == "rewrite"

    def test_route_irrelevant_already_rewritten_to_generate(self):
        assert route_after_grade(self._make_state("irrelevant", rewrite_count=1)) == "generate"

    def test_route_irrelevant_twice_rewritten_to_generate(self):
        assert route_after_grade(self._make_state("irrelevant", rewrite_count=2)) == "generate"


class TestBuildGraph:
    def test_build_graph_without_checkpointer(self):
        graph = build_graph(checkpointer=None)
        assert graph is not None
        nodes = graph.get_graph().nodes
        assert "retrieve" in nodes
        assert "grade" in nodes
        assert "rewrite" in nodes
        assert "generate" in nodes

    def test_graph_edges_exist(self):
        graph = build_graph(checkpointer=None)
        edges = list(graph.get_graph().edges)
        edge_sources = [e[0] for e in edges]
        assert "retrieve" in edge_sources


class TestGraphIntegration:
    """LangGraph 完整流转集成测试（使用 mock LLM）。"""

    @pytest.fixture(autouse=True)
    def setup_graph(self, mock_llm, mock_hybrid_search, monkeypatch):
        from graph.graph import build_graph
        from graph.nodes.retrieve import init_retrieve
        from graph.nodes.grade import init_grade
        from graph.nodes.rewrite import init_rewrite
        from graph.nodes.generate import init_generate

        init_retrieve(mock_hybrid_search, None)
        init_grade(mock_llm)
        init_rewrite(mock_llm)
        init_generate(mock_llm)

        self.graph = build_graph(checkpointer=None)
        self.llm = mock_llm
        self.hybrid = mock_hybrid_search

    def test_happy_path_relevant(self, base_state):
        self.llm.invoke.return_value.content = "relevant"
        result = self.graph.invoke(base_state)
        assert result["grade_result"] == "relevant"
        assert result["rewrite_count"] == 0
        assert len(result["answer"]) > 0

    def test_self_correction_path(self, base_state):
        call_count = [0]

        def grade_then_relevant(messages):
            call_count[0] += 1
            response = MagicMock()
            response.content = "irrelevant" if call_count[0] == 1 else "relevant"
            return response

        self.llm.invoke.side_effect = grade_then_relevant
        result = self.graph.invoke(base_state)
        assert result["grade_result"] == "relevant"
        assert result["rewrite_count"] == 1
        assert len(result["answer"]) > 0

    def test_partial_path(self, base_state):
        self.llm.invoke.side_effect = None
        self.llm.invoke.return_value.content = "partial"
        result = self.graph.invoke(base_state)
        assert result["grade_result"] == "partial"
        assert result["rewrite_count"] == 0
        assert len(result["answer"]) > 0

    def test_retrieve_failure_graceful(self, base_state):
        failing_hybrid = MagicMock()
        failing_hybrid.search.side_effect = RuntimeError("FAISS corrupted")
        from graph.nodes.retrieve import init_retrieve
        init_retrieve(failing_hybrid, None)
        self.llm.invoke.return_value.content = "irrelevant"
        result = self.graph.invoke(base_state)
        assert "answer" in result
        assert len(result["answer"]) > 0

    def test_no_infinite_loop(self, base_state):
        state = {**base_state, "rewrite_count": 1}
        self.llm.invoke.return_value.content = "irrelevant"
        result = self.graph.invoke(state)
        assert result["rewrite_count"] >= 1
        assert len(result["answer"]) > 0

    def test_answer_no_forbidden_words(self, base_state):
        self.llm.invoke.return_value.content = (
            "abandon 的意思是放弃。你可以在单词打卡模块学习更多四级词汇。"
        )
        result = self.graph.invoke(base_state)
        forbidden = ["向量数据库", "RAG", "参考内容", "根据资料", "知识库", "检索"]
        for word in forbidden:
            assert word not in result["answer"], f"Answer contains: '{word}'"
