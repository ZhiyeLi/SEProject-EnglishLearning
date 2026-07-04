import pytest
import numpy as np
from retrieval.embeddings import EmbeddingService


class TestEmbeddingService:
    @pytest.fixture(scope="module")
    def service(self):
        return EmbeddingService()

    def test_singleton(self):
        s1 = EmbeddingService()
        s2 = EmbeddingService()
        assert s1 is s2

    def test_dimension(self, service):
        assert service.dim == 1024

    def test_embed_query_returns_dense_and_sparse(self, service):
        result = service.embed_query("hello world")
        assert "dense" in result
        assert "sparse" in result
        assert isinstance(result["dense"], np.ndarray)
        assert result["dense"].shape == (1024,)
        assert isinstance(result["sparse"], dict)

    def test_embed_documents_returns_batch(self, service):
        texts = ["apple", "banana", "orange"]
        result = service.embed_documents(texts)
        assert result["dense"].shape == (3, 1024)
        assert len(result["sparse"]) == 3

    def test_dense_vectors_are_normalized(self, service):
        result = service.embed_query("test normalization")
        norm = np.linalg.norm(result["dense"])
        assert abs(norm - 1.0) < 0.01

    def test_different_queries_produce_different_vectors(self, service):
        v1 = service.embed_query("abandon")["dense"]
        v2 = service.embed_query("beautiful")["dense"]
        cosine_sim = np.dot(v1, v2)
        assert cosine_sim < 0.95
