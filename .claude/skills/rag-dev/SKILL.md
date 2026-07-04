---
name: rag-dev
description: This skill should be used when the user asks about "RAG", "检索增强生成", "vector search", "embedding", "knowledge base", "知识库", "semantic search", "语义搜索", "document retrieval", "chunking", "vector database", "向量数据库", or wants to build/improve a retrieval-augmented generation pipeline. Also triggers on mentions of integrating AI chat with project data, building a Q&A system over English learning content, or adding document intelligence features.
version: 1.0.0
---

# RAG Development Skill

Guidance for designing, building, and debugging Retrieval-Augmented Generation (RAG) pipelines within this English Learning Platform project.

## Project Context

This project has three layers relevant to RAG work:

| Layer | Stack | RAG Relevance |
|---|---|---|
| Frontend | Vue 3 + Element Plus | AI Chat UI (`AiChat.vue`, `Chat.vue`), streaming display |
| Node.js Server | Express + SQLite | Real-time messaging, user sessions, word/plan data |
| Spring Boot | Java 17 + JPA + MySQL | AI Chat API (`AiChatController`, `AiChatService`), structured learning content (words, questions, articles) |

Existing AI Chat infrastructure is in `AiChatService.java` and `AiChatController.java` — this is the natural integration point for RAG.

## When to Use This Skill

Activate when tasks involve:
- Adding vector search or semantic retrieval to any feature
- Building a knowledge base from English learning content (word definitions, articles, exam questions)
- Integrating embeddings (OpenAI, local model, etc.) into the chat pipeline
- Chunking documents for retrieval
- Evaluating retrieval quality or tuning chunk/embedding strategies
- Connecting a vector database (ChromaDB, Milvus, Pinecone, pgvector, etc.)
- Adding context-augmented prompts to the existing AI Chat flow

## RAG Architecture for This Project

### Recommended Pipeline

```
User Query (Vue → AiChat.vue)
    │
    ▼
Spring Boot AiChatController
    │
    ├── 1. Embed query ──► Embedding Service (API or local)
    │
    ├── 2. Vector Search ──► Vector DB (ChromaDB / pgvector / Milvus)
    │         │
    │         └── Top-K relevant chunks (words, articles, questions)
    │
    ├── 3. Augment Prompt ──► Combine query + retrieved context + system prompt
    │
    └── 4. Generate ──► LLM API (OpenAI / Claude / local)
              │
              └── Stream response back to Vue frontend
```

### Data Sources to Index

Prioritize these project data sources for knowledge base construction:

1. **Word bank** — `Word` entity / `单词数据集/` directory: definitions, examples, phonetics
2. **Articles** — `ArticleDetail.vue` + `articleData.js`: reading passages, translations, notes
3. **Question bank** — `QuestionBank` / `ExamPaper` entities: exam questions, answers, explanations
4. **Course content** — `Course` entity: structured lesson material
5. **Learning plans** — `Plan` entity: study plans and schedules

## Key Design Decisions

### 1. Vector Database Choice

| Option | When to Use | Integration Pattern |
|---|---|---|
| **ChromaDB** | Quick prototyping, small-to-medium scale (<100K docs), already using Python or Node | Run as separate process, REST API |
| **pgvector** | Already using PostgreSQL, want transactional consistency, medium scale | PostgreSQL extension, migrate MySQL→PG or add PG sidecar |
| **Milvus** | Large scale (>1M docs), need high-performance ANN search | Docker deployment, gRPC client in Spring Boot |
| **Qdrant** | Rust-native performance, good filtering + vector hybrid search | Docker deployment, REST/gRPC |

Since this project uses MySQL via Spring Boot JPA, **ChromaDB** (simple REST API, great for prototyping) or **pgvector** (if willing to add PostgreSQL) are the most natural choices.

### 2. Embedding Model

- **OpenAI `text-embedding-3-small`** — best quality, external API, cost per token
- **BGE-M3 / BGE-large-zh** — open-source, good for Chinese+English, self-hosted
- **all-MiniLM-L6-v2** — lightweight, fast, English-only

For this bilingual (EN/ZH) learning platform, prefer a model that handles both languages well.

### 3. Chunking Strategy

For the content types in this project:

| Content Type | Chunk Size | Overlap | Strategy |
|---|---|---|---|
| Word definitions | 1 word = 1 chunk | 0 | Atomic, no splitting needed |
| Articles | 500-800 chars | 100 chars | Split by paragraph, preserve headings |
| Exam questions | 1 question = 1 chunk | 0 | Atomic with metadata (type, difficulty, tags) |
| Course content | 800-1200 chars | 200 chars | Split by section with hierarchical metadata |

Always attach metadata: `source_type`, `source_id`, `title`, `difficulty`, `tags`.

### 4. Prompt Template Pattern

```
System: You are an English learning assistant. Use the following retrieved
context to answer the user's question. If the context doesn't contain the
answer, say so — don't hallucinate.

Retrieved context:
{context}

User: {query}
```

For the learning domain, add a "difficulty adaptation" layer that adjusts response language complexity based on the user's level (stored in `UserSettings`).

## Implementation Checklist

When building a RAG feature, work through these phases:

### Phase 0 — Prerequisites
- [ ] Choose embedding model and get API key / deploy locally
- [ ] Choose vector database and set up (Docker / cloud / embedded)
- [ ] Add vector DB client dependency to `pom.xml`

### Phase 1 — Ingestion Pipeline
- [ ] Create `EmbeddingService` (or add to existing `AiChatService`)
- [ ] Create `IngestionService` that reads from JPA entities, chunks, embeds, and upserts into vector DB
- [ ] Add a CLI tool or REST endpoint to trigger re-indexing
- [ ] Handle incremental updates (entity change → re-embed → update vector)

### Phase 2 — Retrieval
- [ ] Implement `searchSimilar(query, topK, filters)` in a new `RetrievalService`
- [ ] Add hybrid search support (keyword BM25 + vector) if needed
- [ ] Add metadata filtering (e.g., only search articles, or only words of certain type)

### Phase 3 — Generation
- [ ] Modify `AiChatService.java` to inject retrieved context into the prompt
- [ ] Add token budget management (truncate context to fit model context window)
- [ ] Add source citation in the response

### Phase 4 — Evaluation
- [ ] Create a test set of Q&A pairs with expected answers
- [ ] Measure retrieval metrics: recall@K, MRR, NDCG
- [ ] Measure generation metrics: faithfulness, relevance, correctness
- [ ] User feedback loop (thumbs up/down on AI responses → improve retrieval)

## Common Pitfalls

1. **Stale vectors** — when source data changes, vectors must be re-embedded. Build a sync mechanism, not a one-time import.
2. **Over-chunking** — splitting too small loses context. For word definitions, keep them whole.
3. **Ignoring metadata** — vector similarity alone is insufficient. Filter by content type, difficulty, etc.
4. **Prompt stuffing** — jamming too much context degrades generation quality. Keep retrieved context concise and relevant.
5. **No reranking** — raw embedding similarity often puts the best match at rank 3-5, not rank 1. Add a cross-encoder reranker for quality-sensitive Q&A.
6. **Language mismatch** — the embedding model must support both English and Chinese, or you need separate indices.

## Reference Files

- `references/vector-db-setup.md` — Quickstart guides for ChromaDB, pgvector, Milvus
- `references/embedding-models.md` — Comparison of embedding models for bilingual (EN/ZH) use cases
- `references/chunking-patterns.md` — Detailed chunking strategies with code examples
- `references/evaluation.md` — RAG evaluation framework and metrics
- `examples/spring-boot-chromadb.md` — End-to-end example integrating ChromaDB with this project's Spring Boot backend
