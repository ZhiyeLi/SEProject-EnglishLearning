import logging
import threading
from typing import List, Dict
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer

logger = logging.getLogger(__name__)


class EmbeddingService:
    """BGE-M3 嵌入模型封装。

    Dense: FlagEmbedding BGE-M3 (CPU — GPU causes segfault on this Windows env)
    Sparse: sklearn TfidfVectorizer (fast BM25-like lexical weights)

    模型在首次使用时加载，之后缓存在内存中。
    """

    _instance = None
    _lock = threading.Lock()
    _load_failed = False

    def __new__(cls):
        if cls._instance is None:
            with cls._lock:
                if cls._instance is None:
                    cls._instance = super().__new__(cls)
                    cls._instance._dense_model = None
                    cls._instance._tfidf = None
        return cls._instance

    @property
    def dense_model(self):
        if self._dense_model is None:
            with self._lock:
                if self._dense_model is None:
                    if self._load_failed:
                        raise RuntimeError("BGE-M3 model failed to load previously")
                    logger.info("Loading BGE-M3 via FlagEmbedding (CPU)...")
                    try:
                        from FlagEmbedding import BGEM3FlagModel
                        import torch
                        device = "cuda" if torch.cuda.is_available() else "cpu"
                        use_fp16 = (device == "cuda")
                        self._dense_model = BGEM3FlagModel(
                            'BAAI/bge-m3',
                            use_fp16=use_fp16,
                            device=device,
                        )
                        logger.info(f"BGE-M3 loaded on {device}" + (" (fp16)" if use_fp16 else ""))
                    except Exception:
                        EmbeddingService._load_failed = True
                        logger.exception("Failed to load BGE-M3")
                        raise
        return self._dense_model

    @property
    def dim(self) -> int:
        return 1024

    def fit_tfidf(self, texts: List[str]):
        """在文档集上拟合 TF-IDF 向量器（构建索引时调用一次）。"""
        logger.info(f"Fitting TF-IDF on {len(texts)} documents...")
        self._tfidf = TfidfVectorizer(
            max_features=50000,
            ngram_range=(1, 2),
            sublinear_tf=True,
        )
        self._tfidf.fit(texts)
        logger.info(f"TF-IDF vocab: {len(self._tfidf.vocabulary_)}")
        return self._tfidf

    def embed_query(self, query: str) -> Dict[str, np.ndarray]:
        """编码单个查询，返回 dense + sparse 向量。"""
        if not isinstance(query, str) or not query.strip():
            raise ValueError("query must be a non-empty string")

        output = self.dense_model.encode(
            [query],
            return_dense=True,
            return_sparse=False,
            return_colbert_vecs=False,
        )
        dense = output["dense_vecs"][0]

        sparse = {}
        if self._tfidf is not None:
            vec = self._tfidf.transform([query])
            coo = vec.tocoo()
            for row, col, val in zip(coo.row, coo.col, coo.data):
                sparse[int(col)] = float(val)

        return {"dense": dense, "sparse": sparse}

    def embed_documents(self, texts: List[str]) -> Dict[str, np.ndarray]:
        """批量编码文档，返回 dense + sparse 向量。"""
        if not texts:
            raise ValueError("texts must not be an empty list")

        output = self.dense_model.encode(
            texts,
            return_dense=True,
            return_sparse=False,
            return_colbert_vecs=False,
            batch_size=32,
        )
        dense = output["dense_vecs"]

        if self._tfidf is not None:
            mat = self._tfidf.transform(texts)
            sparse_list = []
            for i in range(mat.shape[0]):
                row = mat[i].tocoo()
                d = {int(c): float(v) for c, v in zip(row.col, row.data)}
                sparse_list.append(d)
        else:
            sparse_list = [{} for _ in texts]

        return {"dense": dense, "sparse": sparse_list}
