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
