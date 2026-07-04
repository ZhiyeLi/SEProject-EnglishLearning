import json
import os
import pytest

DATASET_PATH = os.path.join(os.path.dirname(__file__), "dataset.json")
FORBIDDEN_WORDS = ["向量数据库", "RAG", "参考内容", "根据资料", "知识库", "检索"]


def load_dataset():
    with open(DATASET_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


@pytest.mark.slow
class TestRetrievalQuality:
    @pytest.fixture(scope="class")
    def hybrid_search(self):
        from retrieval.embeddings import EmbeddingService
        from retrieval.hybrid_search import HybridSearchService
        index_dir = os.path.join(os.path.dirname(__file__), "..", "..", "faiss_index")
        if not os.path.exists(index_dir):
            pytest.skip("FAISS index not found. Run build_knowledge_base.py first.")
        embeddings = EmbeddingService()
        return HybridSearchService.load(index_dir, embeddings)

    @pytest.fixture(scope="class")
    def reranker(self):
        from retrieval.reranker import RerankerService
        return RerankerService()

    @pytest.mark.parametrize("item", load_dataset())
    def test_retrieval_has_results(self, hybrid_search, reranker, item):
        candidates = hybrid_search.search(item["query"], top_k=20)
        reranked = reranker.rerank(item["query"], candidates, top_k=5)
        assert len(reranked) > 0, f"No results for: {item['query']}"

    @pytest.mark.parametrize("item", [i for i in load_dataset() if i["relevant_type"] != "不相关"])
    def test_relevant_query_finds_keywords(self, hybrid_search, reranker, item):
        expected_keywords = item.get("expected_keywords", [])
        if not expected_keywords:
            return
        candidates = hybrid_search.search(item["query"], top_k=20)
        reranked = reranker.rerank(item["query"], candidates, top_k=5)
        all_text = " ".join(doc.page_content for doc, _ in reranked).lower()
        matched = any(kw.lower() in all_text for kw in expected_keywords)
        assert matched, (
            f"Query '{item['query']}': no expected keyword found.\n"
            f"Expected: {expected_keywords}\n"
            f"Top result: {reranked[0][0].page_content[:200] if reranked else 'N/A'}"
        )

    def test_recall_at_5(self, hybrid_search, reranker):
        dataset = [d for d in load_dataset() if d["relevant_type"] != "不相关" and d.get("expected_keywords")]
        hits = 0
        misses = []
        for item in dataset:
            candidates = hybrid_search.search(item["query"], top_k=20)
            reranked = reranker.rerank(item["query"], candidates, top_k=5)
            all_text = " ".join(doc.page_content for doc, _ in reranked).lower()
            if any(kw.lower() in all_text for kw in item["expected_keywords"]):
                hits += 1
            else:
                misses.append(item["query"])
        recall = hits / len(dataset) if dataset else 0
        print(f"\nRecall@5: {recall:.3f} ({hits}/{len(dataset)})")
        if misses:
            print(f"Missed: {misses}")
        assert recall >= 0.85, f"Recall@5 ({recall:.3f}) below 0.85"


@pytest.mark.slow
class TestGenerationQuality:
    def test_no_forbidden_words_in_prompts(self):
        from prompts.system import SYSTEM_PROMPT, NO_CONTEXT_PROMPT
        from prompts.grader import GRADE_PROMPT, REWRITE_PROMPT
        for word in FORBIDDEN_WORDS:
            assert word not in SYSTEM_PROMPT
            assert word not in NO_CONTEXT_PROMPT
            assert word not in GRADE_PROMPT
            assert word not in REWRITE_PROMPT
