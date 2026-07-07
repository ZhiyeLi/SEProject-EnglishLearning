import operator
from typing import TypedDict, List, Annotated, Dict
from langchain_core.documents import Document


class AgentState(TypedDict):
    """LangGraph Agent 的全局状态。

    每个节点读取需要的字段，写入更新的字段。
    checkpoint 按 thread_id 持久化整个状态。
    """
    query: str
    chat_history: Annotated[List[Dict[str, str]], operator.add]
    retrieved_docs: List[Document]
    grade_result: str
    rewritten_query: str
    rewrite_count: int
    answer: str
    user_level: str
