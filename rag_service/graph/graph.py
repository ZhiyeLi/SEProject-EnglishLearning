# rag_service/graph/graph.py
import logging
from langgraph.graph import StateGraph, END

from graph.state import AgentState
from graph.nodes.retrieve import retrieve_node
from graph.nodes.grade import grade_node
from graph.nodes.rewrite import rewrite_node
from graph.nodes.generate import generate_node

logger = logging.getLogger(__name__)


def route_after_grade(state: AgentState) -> str:
    grade = state.get("grade_result", "relevant")
    rewrite_count = state.get("rewrite_count", 0)

    if grade == "irrelevant" and rewrite_count < 1:
        logger.info(f"route: '{grade}' (count={rewrite_count}) -> rewrite")
        return "rewrite"

    logger.info(f"route: '{grade}' (count={rewrite_count}) -> generate")
    return "generate"


def build_graph(checkpointer=None):
    workflow = StateGraph(AgentState)

    workflow.add_node("retrieve", retrieve_node)
    workflow.add_node("grade", grade_node)
    workflow.add_node("rewrite", rewrite_node)
    workflow.add_node("generate", generate_node)

    workflow.set_entry_point("retrieve")

    workflow.add_edge("retrieve", "grade")

    workflow.add_conditional_edges(
        "grade",
        route_after_grade,
        {
            "rewrite": "rewrite",
            "generate": "generate",
        },
    )

    workflow.add_edge("rewrite", "retrieve")
    workflow.add_edge("generate", END)

    compiled = workflow.compile(checkpointer=checkpointer)
    logger.info(f"LangGraph compiled with {len(workflow.nodes)} nodes")
    return compiled
