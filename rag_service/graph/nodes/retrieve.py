import logging
from typing import List, Any

logger = logging.getLogger(__name__)

# 这些变量在 app.py startup 时注入
hybrid_search = None
reranker = None


def init_retrieve(hybrid_search_service, reranker_service):
    """初始化检索服务引用（在 app startup 时调用）。"""
    global hybrid_search, reranker
    hybrid_search = hybrid_search_service
    reranker = reranker_service


def retrieve_node(state: dict) -> dict:
    """检索节点：混合检索 + 重排序。

    从状态中读取 query 或 rewritten_query，
    执行 BGE-M3 dense+sparse 混合检索，
    然后用 Cross-Encoder 重排序取 Top-5。
    """
    query = state.get("rewritten_query", "") or state.get("query", "")

    if not query.strip():
        logger.warning("retrieve_node: empty query, returning empty docs")
        return {"retrieved_docs": []}

    try:
        if hybrid_search is None:
            logger.error("retrieve_node: hybrid_search not initialized")
            return {"retrieved_docs": []}

        # 1. 混合检索（dense + sparse -> RRF），取 Top-20 候选
        candidates = hybrid_search.search(query, top_k=20)
        logger.debug(f"Hybrid search returned {len(candidates)} candidates")

        # 2. Cross-Encoder 重排序，取 Top-5
        if reranker is not None and len(candidates) > 1:
            reranked = reranker.rerank(query, candidates, top_k=5)
            logger.debug(f"Reranker narrowed to {len(reranked)} docs")
        else:
            reranked = candidates[:5]

        docs_only = [doc for doc, _ in reranked]
        return {"retrieved_docs": docs_only}

    except Exception as e:
        logger.error(f"retrieve_node failed: {e}", exc_info=True)
        return {"retrieved_docs": []}
