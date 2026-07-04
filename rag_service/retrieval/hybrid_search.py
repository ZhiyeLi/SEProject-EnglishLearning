import logging
import pickle
import os
import numpy as np
from typing import List, Tuple, Dict, Any

logger = logging.getLogger(__name__)


class HybridSearchService:
    """BGE-M3 dense + sparse 混合检索。

    使用 Reciprocal Rank Fusion (RRF) 融合两路检索结果。
    """

    def __init__(self, embeddings, dense_index, sparse_vectors, all_documents):
        self.embeddings = embeddings
        self.dense_index = dense_index
        self.sparse_vectors = sparse_vectors
        self.all_documents = all_documents
        self._doc_count = len(all_documents)

    def search(
        self, query: str, top_k: int = 20, rrf_k: int = 60
    ) -> List[Tuple[Any, float]]:
        """混合检索：dense + sparse 双路召回 → RRF 融合。

        Args:
            query: 用户查询
            top_k: 返回文档数量
            rrf_k: RRF 平滑参数

        Returns:
            [(Document, score), ...] 按 RRF 分数降序排列
        """
        if not query or not query.strip():
            return []

        query_vecs = self.embeddings.embed_query(query)
        query_dense = query_vecs["dense"].reshape(1, -1)
        query_sparse = query_vecs["sparse"]

        # 1. Dense search via FAISS
        dense_k = min(top_k * 3, self._doc_count)
        if dense_k > 0:
            dense_scores, dense_indices = self.dense_index.search(query_dense, dense_k)
        else:
            dense_scores, dense_indices = np.array([[]]), np.array([[]])

        # 2. Sparse search via lexical weight similarity
        sparse_k = min(top_k * 3, self._doc_count)
        sparse_scores, sparse_indices = self._sparse_search(query_sparse, sparse_k)

        # 3. RRF fusion
        fused = self._reciprocal_rank_fusion(
            list(zip(dense_indices[0], dense_scores[0])) if len(dense_indices[0]) > 0 else [],
            list(zip(sparse_indices, sparse_scores)) if len(sparse_indices) > 0 else [],
            rrf_k=rrf_k,
        )

        if not fused:
            return []

        # Take top_k by fused score
        fused_sorted = sorted(fused.items(), key=lambda x: x[1], reverse=True)[:top_k]
        return [(self.all_documents[idx], score) for idx, score in fused_sorted]

    def _sparse_search(
        self, query_sparse: Dict[int, float], k: int
    ) -> Tuple[np.ndarray, np.ndarray]:
        """稀疏词权重检索：计算查询与所有文档的稀疏内积。"""
        if k <= 0:
            return np.array([]), np.array([])
        scores = np.zeros(self._doc_count, dtype=np.float32)
        for token_id, q_weight in query_sparse.items():
            for doc_idx, doc_sparse in enumerate(self.sparse_vectors):
                if token_id in doc_sparse:
                    scores[doc_idx] += q_weight * doc_sparse[token_id]
        k = min(k, self._doc_count)
        indices = np.argsort(scores)[::-1][:k]
        return scores[indices], indices

    @staticmethod
    def _reciprocal_rank_fusion(
        dense_results: List[Tuple[int, float]],
        sparse_results: List[Tuple[int, float]],
        rrf_k: int = 60,
    ) -> Dict[int, float]:
        """RRF 融合两路结果。"""
        fused_scores: Dict[int, float] = {}
        for rank, (idx, _) in enumerate(dense_results):
            fused_scores[idx] = fused_scores.get(idx, 0) + 1.0 / (rrf_k + rank + 1)
        for rank, (idx, _) in enumerate(sparse_results):
            fused_scores[idx] = fused_scores.get(idx, 0) + 1.0 / (rrf_k + rank + 1)
        return fused_scores

    @staticmethod
    def load(index_dir: str, embeddings):
        """从磁盘加载 FAISS 索引和稀疏向量。"""
        import faiss

        dense_path = os.path.join(index_dir, "faiss_index")
        sparse_path = os.path.join(index_dir, "sparse_vectors.pkl")
        docs_path = os.path.join(index_dir, "documents.pkl")

        logger.info(f"Loading FAISS dense index from {dense_path}...")
        dense_index = faiss.read_index(dense_path)

        logger.info(f"Loading sparse vectors from {sparse_path}...")
        with open(sparse_path, "rb") as f:
            sparse_vectors = pickle.load(f)

        logger.info(f"Loading documents from {docs_path}...")
        with open(docs_path, "rb") as f:
            all_documents = pickle.load(f)

        logger.info(
            f"HybridSearchService loaded: {len(all_documents)} docs, "
            f"dense dim={dense_index.d}"
        )
        return HybridSearchService(embeddings, dense_index, sparse_vectors, all_documents)
