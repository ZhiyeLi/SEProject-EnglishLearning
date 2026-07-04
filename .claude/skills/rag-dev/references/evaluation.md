# RAG Evaluation Framework

## Test Set Construction

For this English learning platform, create test sets for each content type:

```json
[
  {
    "query": "What does 'ubiquitous' mean?",
    "expected_chunks": ["word_ubiquitous_0"],
    "expected_answer": "Present everywhere at the same time",
    "difficulty": "intermediate"
  },
  {
    "query": "Explain the grammar rule for 'if only' in the article about conditionals",
    "expected_chunks": ["article_conditional_grammar_2", "article_conditional_grammar_3"],
    "difficulty": "advanced"
  }
]
```

## Retrieval Metrics

### Recall@K
Fraction of relevant chunks retrieved in top-K results.

```
Recall@K = |retrieved_chunks ∩ relevant_chunks| / |relevant_chunks|
```

Target: Recall@5 > 0.85

### MRR (Mean Reciprocal Rank)
Where the first relevant result appears on average.

```
MRR = (1/N) * Σ (1 / rank_of_first_relevant)
```

Target: MRR > 0.6

## Generation Metrics

### Faithfulness
Does the answer only contain information from the retrieved context?

**Measure:** LLM-as-judge — ask GPT-4 to compare answer against context and flag unsupported claims.

Target: > 0.9 faithfulness score.

### Answer Relevance
Does the answer address the user's query?

**Measure:** Generate reverse questions from the answer, compute cosine similarity with original query.

Target: > 0.8 relevance score.

## Simple Evaluation Script (Python)

```python
# eval_rag.py — run against your deployed endpoints
import requests
import json
from sklearn.metrics import ndcg_score

def evaluate_retrieval(test_set, endpoint="http://localhost:8080/api/search"):
    recalls = []
    mrrs = []
    for item in test_set:
        resp = requests.post(endpoint, json={"query": item["query"], "topK": 5})
        retrieved_ids = [r["id"] for r in resp.json()["results"]]

        # Recall@5
        hit = len(set(retrieved_ids) & set(item["expected_chunks"])) / len(item["expected_chunks"])
        recalls.append(hit)

        # MRR
        for i, rid in enumerate(retrieved_ids, 1):
            if rid in item["expected_chunks"]:
                mrrs.append(1.0 / i)
                break
        else:
            mrrs.append(0.0)

    print(f"Recall@5: {sum(recalls)/len(recalls):.3f}")
    print(f"MRR: {sum(mrrs)/len(mrrs):.3f}")
```

## Feedback Loop

Use the `UserAnswerDetail` / thumbs-up-down UX in `AiChat.vue` to collect implicit labels:

- 👍 (thumbs up) → mark retrieved chunks as positive examples
- 👎 (thumbs down) → mark retrieved chunks as negative examples

Periodically re-train/swap the embedding model or adjust retrieval (boost certain source types, adjust chunk sizes) based on this feedback.
