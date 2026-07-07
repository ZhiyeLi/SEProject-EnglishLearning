# rag_service/graph/graph.py
import logging
from langgraph.graph import StateGraph, END

from graph.state import AgentState
from graph.nodes.retrieve import retrieve_node
from graph.nodes.grade import grade_node
from graph.nodes.rewrite import rewrite_node

logger = logging.getLogger(__name__)


def route_after_grade(state: AgentState) -> str:
    """条件路由：grade 后决定是否重写查询。"""
    grade = state.get("grade_result", "relevant")
    rewrite_count = state.get("rewrite_count", 0)

    if grade == "irrelevant" and rewrite_count < 1:
        logger.info(f"route: '{grade}' (count={rewrite_count}) -> rewrite")
        return "rewrite"

    logger.info(f"route: '{grade}' (count={rewrite_count}) -> end")
    return "end"


def build_graph(checkpointer=None):
    """构建检索决策 Graph（不含生成）。

    流程：retrieve → grade → (rewrite → retrieve) → END
    生成由 app.py 调用 generate_node/generate_node_stream 处理，
    这样 SSE 流式输出可以直接拿到 token 级别的控制权。
    """
    workflow = StateGraph(AgentState)

    workflow.add_node("retrieve", retrieve_node)
    workflow.add_node("grade", grade_node)
    workflow.add_node("rewrite", rewrite_node)

    workflow.set_entry_point("retrieve")
    workflow.add_edge("retrieve", "grade")

    workflow.add_conditional_edges(
        "grade",
        route_after_grade,
        {
            "rewrite": "rewrite",
            "end": END,
        },
    )

    workflow.add_edge("rewrite", "retrieve")

    compiled = workflow.compile(checkpointer=checkpointer)
    logger.info(f"LangGraph compiled with {len(workflow.nodes)} nodes (retrieval only)")
    return compiled
