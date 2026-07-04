import logging
import threading
from typing import List, Dict
import numpy as np

logger = logging.getLogger(__name__)


class EmbeddingService:
    """BGE-M3 嵌入模型封装。

    提供 dense (1024-dim) 和 sparse (lexical weights) 两种输出。
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
                    cls._instance._model = None
        return cls._instance

    @property
    def model(self):
        if self._model is None:
            with self._lock:
                if self._model is None:
                    if self._load_failed:
                        raise RuntimeError(
                            "BGE-M3 model previously failed to load; "
                            "EmbeddingService is unavailable."
                        )
                    logger.info(
                        "Loading BGE-M3 model (first use, ~2.2GB download if not cached)..."
                    )
                    try:
                        from FlagEmbedding import BGEM3FlagModel
                        self._model = BGEM3FlagModel(
                            'BAAI/bge-m3',
                            use_fp16=False,
                            device="cpu",
                        )
                        logger.info("BGE-M3 model loaded successfully.")
                    except Exception:
                        EmbeddingService._load_failed = True
                        logger.exception("Failed to load BGE-M3 model.")
                        raise
        return self._model

    @property
    def dim(self) -> int:
        return 1024

    def embed_query(self, query: str) -> Dict[str, np.ndarray]:
        """编码单个查询，返回 dense + sparse 向量。"""
        if not isinstance(query, str) or not query.strip():
            raise ValueError("query must be a non-empty string")
        output = self.model.encode(
            [query],
            return_dense=True,
            return_sparse=True,
            return_colbert_vecs=False,
        )
        return {
            "dense": output["dense_vecs"][0],
            "sparse": output["lexical_weights"][0],
        }

    def embed_documents(self, texts: List[str]) -> Dict[str, np.ndarray]:
        """批量编码文档，返回 dense + sparse 向量。"""
        if not texts:
            raise ValueError("texts must not be an empty list")
        output = self.model.encode(
            texts,
            return_dense=True,
            return_sparse=True,
            return_colbert_vecs=False,
        )
        return {
            "dense": output["dense_vecs"],
            "sparse": output["lexical_weights"],
        }
