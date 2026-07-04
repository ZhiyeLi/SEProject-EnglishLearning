from typing import TypedDict, List, Annotated, Optional, Any, Dict
from langgraph.graph.message import add_messages


class AgentState(TypedDict):
    """LangGraph Agent 的全局状态。

    每个节点读取需要的字段，写入更新的字段。
    checkpoint 按 thread_id 持久化整个状态。
    """
    query: str
    chat_history: Annotated[List[Dict[str, str]], add_messages]
    retrieved_docs: List[Any]
    grade_result: str
    rewritten_query: str
    rewrite_count: int
    answer: str
    user_level: str
