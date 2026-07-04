import json
import os
import pytest

BASELINE_PATH = os.path.join(os.path.dirname(__file__), "baseline.json")
DATASET_PATH = os.path.join(os.path.dirname(__file__), "dataset.json")


def save_baseline(scores: dict):
    with open(BASELINE_PATH, "w", encoding="utf-8") as f:
        json.dump(scores, f, indent=2, ensure_ascii=False)


def load_baseline():
    if not os.path.exists(BASELINE_PATH):
        return None
    with open(BASELINE_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


def load_dataset():
    with open(DATASET_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


class TestRegression:
    @pytest.mark.slow
    def test_retrieval_recall_regression(self):
        """检索 Recall@5 不应相比 baseline 下降超过 5%."""
        from retrieval.embeddings import EmbeddingService
        from retrieval.hybrid_search import HybridSearchService
        from retrieval.reranker import RerankerService

        index_dir = os.path.join(os.path.dirname(__file__), "..", "..", "faiss_index")
        if not os.path.exists(index_dir):
            pytest.skip("FAISS index not found.")

        embeddings = EmbeddingService()
        hybrid_search = HybridSearchService.load(index_dir, embeddings)
        reranker = RerankerService()

        dataset = [d for d in load_dataset() if d["relevant_type"] != "不相关" and d.get("expected_keywords")]
        hits = 0
        for item in dataset:
            candidates = hybrid_search.search(item["query"], top_k=20)
            reranked = reranker.rerank(item["query"], candidates, top_k=5)
            all_text = " ".join(doc.page_content for doc, _ in reranked).lower()
            if any(kw.lower() in all_text for kw in item["expected_keywords"]):
                hits += 1

        current_recall = hits / len(dataset) if dataset else 0
        baseline = load_baseline()

        print(f"\nCurrent Recall@5: {current_recall:.3f}")

        if baseline is None:
            print("No baseline found. Saving current score.")
            save_baseline({"recall_at_5": current_recall})
            pytest.skip("Baseline created. Run again to compare.")
        else:
            baseline_recall = baseline["recall_at_5"]
            drop = baseline_recall - current_recall
            pct = drop / baseline_recall * 100
            print(f"Baseline Recall@5: {baseline_recall:.3f}, Drop: {pct:.1f}%")
            if pct > 5:
                pytest.fail(f"Recall@5 dropped by {pct:.1f}% (>5%). Investigate.")
