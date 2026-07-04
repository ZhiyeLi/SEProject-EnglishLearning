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


class TestHybridSearchService:
    """HybridSearchService 单元测试（使用小规模 mock 数据）。"""

    @pytest.fixture
    def mock_docs(self):
        from langchain_core.documents import Document
        return [
            Document(page_content="abandon 放弃 抛弃", metadata={"word": "abandon", "level": "四级词汇"}),
            Document(page_content="beautiful 美丽的 漂亮的", metadata={"word": "beautiful", "level": "四级词汇"}),
            Document(page_content="computer 计算机 电脑", metadata={"word": "computer", "level": "四级词汇"}),
            Document(page_content="现在完成时 have/has + 过去分词", metadata={"topic": "present perfect", "level": "四级词汇"}),
            Document(page_content="作文模板：议论文三段式结构", metadata={"title": "argumentative essay", "exam_level": "四级词汇"}),
        ]

    @pytest.fixture
    def service(self, mock_docs):
        import faiss
        import numpy as np
        from retrieval.embeddings import EmbeddingService

        emb = EmbeddingService()
        texts = [d.page_content for d in mock_docs]
        vecs = emb.embed_documents(texts)

        dense_index = faiss.IndexFlatIP(1024)
        faiss.normalize_L2(vecs["dense"])
        dense_index.add(vecs["dense"])

        from retrieval.hybrid_search import HybridSearchService
        return HybridSearchService(emb, dense_index, vecs["sparse"], mock_docs)

    def test_search_returns_documents(self, service):
        results = service.search("abandon", top_k=3)
        assert len(results) > 0
        assert len(results) <= 3

    def test_search_relevant_word_in_top_results(self, service):
        results = service.search("abandon", top_k=5)
        # First result should contain "abandon"
        assert "abandon" in results[0][0].page_content.lower()
        # Top results should all be relevant
        top_contents = [r[0].page_content for r in results[:2]]
        assert any("abandon" in c.lower() for c in top_contents)

    def test_search_chinese_query(self, service):
        results = service.search("计算机", top_k=3)
        assert len(results) > 0
        top_contents = [r[0].page_content for r in results]
        assert any("计算机" in c for c in top_contents)

    def test_search_grammar_topic(self, service):
        results = service.search("现在完成时", top_k=3)
        found_grammar = any("完成时" in r[0].page_content for r in results)
        assert found_grammar

    def test_empty_query_handled(self, service):
        results = service.search("", top_k=3)
        assert isinstance(results, list)
        assert len(results) == 0

    def test_whitespace_query_handled(self, service):
        results = service.search("   ", top_k=3)
        assert isinstance(results, list)
        assert len(results) == 0

    def test_search_returns_doc_with_score(self, service):
        results = service.search("beautiful", top_k=3)
        if results:
            doc, score = results[0]
            assert hasattr(doc, 'page_content')
            assert isinstance(score, float)
