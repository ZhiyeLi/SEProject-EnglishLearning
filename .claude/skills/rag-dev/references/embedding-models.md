# Embedding Model Comparison (Bilingual EN/ZH)

## Cloud APIs

| Model | Dimensions | Languages | Cost | Latency |
|---|---|---|---|---|
| OpenAI text-embedding-3-small | 1536 | EN>>ZH | $0.02/1M tokens | ~100ms |
| OpenAI text-embedding-3-large | 3072 | EN>>ZH | $0.13/1M tokens | ~200ms |

Call via `spring-ai-openai` or direct HTTP:

```java
// Direct OpenAI call example
RestTemplate rt = new RestTemplate();
HttpHeaders headers = new HttpHeaders();
headers.setBearerAuth(apiKey);
headers.setContentType(MediaType.APPLICATION_JSON);
String body = """
    {"model": "text-embedding-3-small", "input": "%s"}
    """.formatted(text);
ResponseEntity<EmbeddingResponse> resp = rt.postForEntity(
    "https://api.openai.com/v1/embeddings",
    new HttpEntity<>(body, headers),
    EmbeddingResponse.class
);
```

## Self-Hosted Models

| Model | Dimensions | Languages | Hardware | Throughput |
|---|---|---|---|---|
| BGE-M3 | 1024 | EN+ZH (native) | 4GB VRAM | ~50 docs/s on T4 |
| BGE-large-zh-v1.5 | 1024 | ZH>>EN | 2GB VRAM | ~80 docs/s on T4 |
| multilingual-e5-large | 1024 | EN≈ZH | 4GB VRAM | ~40 docs/s on T4 |
| all-MiniLM-L6-v2 | 384 | EN only | 1GB RAM | ~200 docs/s CPU |

### Self-hosting with sentence-transformers (Python sidecar)

```python
# embedding_server.py
from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer

app = FastAPI()
model = SentenceTransformer("BAAI/bge-m3")

class EmbedRequest(BaseModel):
    texts: list[str]

@app.post("/embed")
def embed(req: EmbedRequest):
    vectors = model.encode(req.texts, normalize_embeddings=True)
    return {"embeddings": vectors.tolist()}
```

Call from Spring Boot via REST client.

## Recommendation for This Project

**BGE-M3** is the best fit:
- Native bilingual support (English + Chinese)
- Good quality on both retrieval benchmarks
- 1024-dim vectors are a good balance of quality and storage
- Self-hosted = no per-token cost, good for batch indexing thousands of words/questions
