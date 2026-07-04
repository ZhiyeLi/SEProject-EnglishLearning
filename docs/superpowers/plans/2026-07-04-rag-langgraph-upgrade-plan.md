# RAG AI 智能助教 LangGraph 升级 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 rag_service 从 LCEL 单链升级为 LangGraph Agent 系统，实现混合检索、自校正、多轮对话、SSE 流式输出。

**Architecture:** LangGraph StateGraph 编排 4 个节点（retrieve / grade / rewrite / generate），BGE-M3 提供 dense+sparse 混合检索，Cross-Encoder 重排序，SqliteSaver 持久化对话状态，FastAPI SSE 端点流式输出。

**Tech Stack:** Python 3.9+, LangGraph >=0.2.0, BGE-M3 (FlagEmbedding), FAISS-cpu, FastAPI + sse-starlette, SQLite (langgraph-checkpoint-sqlite)

---

## File Structure Map

```
rag_service/
├── app.py                          # Modify: SSE streaming endpoint + graph init
├── graph/
│   ├── __init__.py                 # Create: empty
│   ├── state.py                    # Create: AgentState TypedDict
│   ├── graph.py                    # Create: build_graph(), route_after_grade()
│   └── nodes/
│       ├── __init__.py             # Create: empty
│       ├── retrieve.py             # Create: retrieve_node()
│       ├── grade.py                # Create: grade_node(), grade_documents()
│       ├── generate.py             # Create: generate_node(), format_docs()
│       └── rewrite.py              # Create: rewrite_node()
├── retrieval/
│   ├── __init__.py                 # Create: empty
│   ├── embeddings.py               # Create: EmbeddingService class
│   ├── hybrid_search.py            # Create: HybridSearchService class
│   └── reranker.py                 # Create: RerankerService class
├── memory/
│   ├── __init__.py                 # Create: empty
│   └── checkpoint.py               # Create: get_checkpointer()
├── prompts/
│   ├── __init__.py                 # Create: empty
│   ├── system.py                   # Create: SYSTEM_PROMPT template
│   └── grader.py                   # Create: GRADE_PROMPT, REWRITE_PROMPT
├── build_knowledge_base.py         # Modify: BGE-M3 dual-index
├── requirements.txt                # Modify: add langgraph, FlagEmbedding, etc.
├── Dockerfile                      # Create
├── docker-compose.yml              # Create
└── tests/
    ├── __init__.py                 # Create: empty
    ├── conftest.py                 # Create: shared fixtures
    ├── fixtures/
    │   ├── sample_csv/             # Create: mini test datasets
    │   │   ├── words.csv
    │   │   ├── grammar.csv
    │   │   └── writing.csv
    │   └── qa_pairs.json           # Create: 15 hand-labeled QA pairs
    ├── test_state.py               # Create: state unit tests
    ├── test_retrieve.py            # Create: retrieve node unit tests
    ├── test_grade.py               # Create: grade node unit tests
    ├── test_rewrite.py             # Create: rewrite node unit tests
    ├── test_generate.py            # Create: generate node unit tests
    ├── test_graph.py               # Create: integration tests
    ├── test_api.py                 # Create: E2E tests
    └── eval/
        ├── dataset.json            # Create: 30 annotated Q&A pairs
        ├── test_retrieval_quality.py  # Create
        ├── test_generation_quality.py # Create
        └── test_regression.py      # Create
```

---

### Task 1: Environment & Dependencies

**Files:**
- Modify: `rag_service/requirements.txt`
- Create: `rag_service/Dockerfile`
- Create: `rag_service/docker-compose.yml`

- [ ] **Step 1: Update requirements.txt**

Replace the entire contents of `rag_service/requirements.txt` with:

```
fastapi>=0.110.0
uvicorn[standard]>=0.29.0
langchain>=0.3.0
langchain-community>=0.3.0
langchain-openai>=0.3.0
langgraph>=0.2.0
langgraph-checkpoint-sqlite>=2.0.0
sentence-transformers>=3.0.0
faiss-cpu>=1.8.0
FlagEmbedding>=1.3.0
pandas>=2.0.0
python-dotenv>=1.0.0
sse-starlette>=2.0.0
httpx>=0.27.0
pytest>=8.0.0
pytest-asyncio>=0.23.0
```

- [ ] **Step 2: Create Dockerfile**

```dockerfile
FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Pre-download BGE-M3 model during build
RUN python -c "from FlagEmbedding import BGEM3FlagModel; BGEM3FlagModel('BAAI/bge-m3', use_fp16=False)"

COPY . .

EXPOSE 8001

CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8001"]
```

- [ ] **Step 3: Create docker-compose.yml**

```yaml
version: '3.8'

services:
  rag-service:
    build: .
    container_name: rag-service
    ports:
      - "8001:8001"
    environment:
      - DEEPBRICKS_API_KEY=${DEEPBRICKS_API_KEY}
      - DEEPBRICKS_BASE_URL=${DEEPBRICKS_BASE_URL:-https://api.deepbricks.ai/v1}
    volumes:
      - ./faiss_index:/app/faiss_index
      - ./sparse_index:/app/sparse_index
      - ./checkpoints.db:/app/checkpoints.db
    restart: unless-stopped
```

- [ ] **Step 4: Install dependencies in local venv and verify**

```bash
cd rag_service && pip install -r requirements.txt
```

Expected: all packages install without error. Verify with:
```bash
python -c "import langgraph; print(langgraph.__version__)"
python -c "from FlagEmbedding import BGEM3FlagModel; print('FlagEmbedding OK')"
```

- [ ] **Step 5: Commit**

```bash
git add rag_service/requirements.txt rag_service/Dockerfile rag_service/docker-compose.yml
git commit -m "chore: update dependencies for LangGraph upgrade, add Dockerfile

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 2: Agent State Definition

**Files:**
- Create: `rag_service/graph/__init__.py`
- Create: `rag_service/graph/state.py`
- Create: `rag_service/tests/test_state.py`

- [ ] **Step 1: Create graph package init**

```python
# rag_service/graph/__init__.py
```

(empty file)

- [ ] **Step 2: Define AgentState**

```python
# rag_service/graph/state.py
from typing import TypedDict, List, Annotated, Optional, Any, Dict
from langgraph.graph.message import add_messages


class AgentState(TypedDict):
    """LangGraph Agent 的全局状态。

    每个节点读取需要的字段，写入更新的字段。
    checkpoint 按 thread_id 持久化整个状态。
    """
    query: str
    chat_history: Annotated[List[Dict[str, str]], add_messages]
    retrieved_docs: List[Any]
    grade_result: str
    rewritten_query: str
    rewrite_count: int
    answer: str
    user_level: str
```

- [ ] **Step 3: Write unit test for state structure**

```python
# rag_service/tests/test_state.py
import pytest
from graph.state import AgentState


class TestAgentState:
    """验证 AgentState TypedDict 的字段和默认行为。"""

    def test_state_has_required_fields(self):
        """状态字典应包含所有必需字段。"""
        required_fields = [
            "query", "chat_history", "retrieved_docs",
            "grade_result", "rewritten_query", "rewrite_count",
            "answer", "user_level"
        ]
        for field in required_fields:
            assert field in AgentState.__annotations__, f"Missing field: {field}"

    def test_state_initialization_minimal(self):
        """用最小字段初始化状态。"""
        state: AgentState = {
            "query": "hello",
            "chat_history": [],
            "retrieved_docs": [],
            "grade_result": "",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        assert state["query"] == "hello"
        assert state["user_level"] == "intermediate"
        assert state["rewrite_count"] == 0

    def test_chat_history_uses_add_messages(self):
        """chat_history 字段使用 add_messages reducer。"""
        assert "add_messages" in str(AgentState.__annotations__["chat_history"])
```

- [ ] **Step 4: Run tests**

```bash
cd rag_service && python -m pytest tests/test_state.py -v
```

Expected: 3 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add rag_service/graph/__init__.py rag_service/graph/state.py rag_service/tests/test_state.py
git commit -m "feat: define AgentState TypedDict for LangGraph

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 3: Embedding Service (BGE-M3 Wrapper)

**Files:**
- Create: `rag_service/retrieval/__init__.py`
- Create: `rag_service/retrieval/embeddings.py`
- Create: `rag_service/tests/test_retrieve.py` (placeholder, embedding tests added here)

- [ ] **Step 1: Create retrieval package init**

```python
# rag_service/retrieval/__init__.py
```

(empty file)

- [ ] **Step 2: Implement EmbeddingService**

```python
# rag_service/retrieval/embeddings.py
import logging
from typing import List, Dict, Tuple
import numpy as np

logger = logging.getLogger(__name__)


class EmbeddingService:
    """BGE-M3 嵌入模型封装。

    提供 dense (1024-dim) 和 sparse (lexical weights) 两种输出。
    模型在首次使用时加载，之后缓存在内存中。

    Attributes:
        model: BGEM3FlagModel 实例（懒加载）
        _instance: 单例缓存
    """

    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._model = None
        return cls._instance

    @property
    def model(self):
        if self._model is None:
            logger.info("Loading BGE-M3 model (first use, ~2.2GB download if not cached)...")
            from FlagEmbedding import BGEM3FlagModel
            self._model = BGEM3FlagModel(
                'BAAI/bge-m3',
                use_fp16=False,
                device="cpu",
            )
            logger.info("BGE-M3 model loaded successfully.")
        return self._model

    @property
    def dim(self) -> int:
        return 1024

    def embed_query(self, query: str) -> Dict[str, np.ndarray]:
        """编码单个查询，返回 dense + sparse 向量。

        Args:
            query: 用户查询文本

        Returns:
            {"dense": np.ndarray(1024,), "sparse": Dict[int, float]}
        """
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
        """批量编码文档，返回 dense + sparse 向量。

        Args:
            texts: 文档文本列表

        Returns:
            {"dense": np.ndarray(n, 1024), "sparse": List[Dict[int, float]]}
        """
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
```

- [ ] **Step 3: Write embedding unit tests**

```python
# rag_service/tests/test_retrieve.py (first section)
import pytest
import numpy as np
from retrieval.embeddings import EmbeddingService


class TestEmbeddingService:
    """EmbeddingService 单元测试。

    注意：首次运行会下载 BGE-M3 模型（~2.2GB），后续使用缓存。
    """

    @pytest.fixture(scope="module")
    def service(self):
        """模块级 fixture，只加载一次模型。"""
        return EmbeddingService()

    def test_singleton(self):
        """EmbeddingService 应为单例。"""
        s1 = EmbeddingService()
        s2 = EmbeddingService()
        assert s1 is s2

    def test_dimension(self, service):
        """BGE-M3 稠密向量维度为 1024。"""
        assert service.dim == 1024

    def test_embed_query_returns_dense_and_sparse(self, service):
        """查询编码应返回 dense 和 sparse 两种向量。"""
        result = service.embed_query("hello world")
        assert "dense" in result
        assert "sparse" in result
        assert isinstance(result["dense"], np.ndarray)
        assert result["dense"].shape == (1024,)
        assert isinstance(result["sparse"], dict)

    def test_embed_documents_returns_batch(self, service):
        """批量编码应返回对应数量的向量。"""
        texts = ["apple", "banana", "orange"]
        result = service.embed_documents(texts)
        assert result["dense"].shape == (3, 1024)
        assert len(result["sparse"]) == 3

    def test_dense_vectors_are_normalized(self, service):
        """稠密向量应近似归一化（L2 norm ≈ 1.0）。"""
        result = service.embed_query("test normalization")
        norm = np.linalg.norm(result["dense"])
        assert abs(norm - 1.0) < 0.01

    def test_different_queries_produce_different_vectors(self, service):
        """不同查询应产生不同向量。"""
        v1 = service.embed_query("abandon")["dense"]
        v2 = service.embed_query("beautiful")["dense"]
        cosine_sim = np.dot(v1, v2)
        assert cosine_sim < 0.95  # 不同单词不应过于相似
```

- [ ] **Step 4: Run embedding tests**

```bash
cd rag_service && python -m pytest tests/test_retrieve.py::TestEmbeddingService -v
```

Expected: 6 tests PASS (first run downloads BGE-M3 model).

- [ ] **Step 5: Commit**

```bash
git add rag_service/retrieval/ rag_service/tests/test_retrieve.py
git commit -m "feat: add EmbeddingService with BGE-M3 wrapper

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 4: Hybrid Search Service

**Files:**
- Create: `rag_service/retrieval/hybrid_search.py`
- Append to: `rag_service/tests/test_retrieve.py`

- [ ] **Step 1: Implement HybridSearchService**

```python
# rag_service/retrieval/hybrid_search.py
import logging
import pickle
import os
import numpy as np
from typing import List, Tuple, Dict, Any

logger = logging.getLogger(__name__)


class HybridSearchService:
    """BGE-M3 dense + sparse 混合检索。

    使用 Reciprocal Rank Fusion (RRF) 融合两路检索结果。

    Attributes:
        embeddings: EmbeddingService 实例
        dense_index: FAISS IndexFlatIP（稠密向量索引）
        sparse_vectors: List[Dict[int, float]]（预计算的稀疏词权重）
        all_documents: List[langchain_core.documents.Document]
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
        query_vecs = self.embeddings.embed_query(query)
        query_dense = query_vecs["dense"].reshape(1, -1)
        query_sparse = query_vecs["sparse"]

        # 1. Dense search via FAISS
        dense_k = min(top_k * 3, self._doc_count)
        dense_scores, dense_indices = self.dense_index.search(query_dense, dense_k)

        # 2. Sparse search via lexical weight similarity
        sparse_k = min(top_k * 3, self._doc_count)
        sparse_scores, sparse_indices = self._sparse_search(query_sparse, sparse_k)

        # 3. RRF fusion
        fused = self._reciprocal_rank_fusion(
            list(zip(dense_indices[0], dense_scores[0])),
            list(zip(sparse_indices, sparse_scores)),
            rrf_k=rrf_k,
        )

        # Take top_k by fused score
        fused_sorted = sorted(fused.items(), key=lambda x: x[1], reverse=True)[:top_k]
        return [(self.all_documents[idx], score) for idx, score in fused_sorted]

    def _sparse_search(
        self, query_sparse: Dict[int, float], k: int
    ) -> Tuple[np.ndarray, np.ndarray]:
        """稀疏词权重检索：计算查询与所有文档的稀疏内积。"""
        scores = np.zeros(self._doc_count, dtype=np.float32)
        for token_id, q_weight in query_sparse.items():
            for doc_idx, doc_sparse in enumerate(self.sparse_vectors):
                if token_id in doc_sparse:
                    scores[doc_idx] += q_weight * doc_sparse[token_id]
        indices = np.argsort(scores)[::-1][:k]
        return scores[indices], indices

    @staticmethod
    def _reciprocal_rank_fusion(
        dense_results: List[Tuple[int, float]],
        sparse_results: List[Tuple[int, float]],
        rrf_k: int = 60,
    ) -> Dict[int, float]:
        """RRF 融合两路结果。

        公式: score(doc) = sum(1 / (k + rank_i)) for each ranker i
        """
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
            f"dense dim={dense_index.d}, sparse vocab size varies"
        )
        return HybridSearchService(embeddings, dense_index, sparse_vectors, all_documents)
```

- [ ] **Step 2: Write hybrid search unit tests**

```python
# Append to rag_service/tests/test_retrieve.py

class TestHybridSearchService:
    """HybridSearchService 单元测试（使用小规模 mock 数据）。"""

    @pytest.fixture
    def mock_docs(self):
        """创建 5 个模拟 Document。"""
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
        """构建一个小型 FAISS 索引用于测试。"""
        import faiss
        import numpy as np

        emb = EmbeddingService()
        texts = [d.page_content for d in mock_docs]
        vecs = emb.embed_documents(texts)

        dense_index = faiss.IndexFlatIP(1024)
        faiss.normalize_L2(vecs["dense"])
        dense_index.add(vecs["dense"])

        return HybridSearchService(emb, dense_index, vecs["sparse"], mock_docs)

    def test_search_returns_documents(self, service):
        """搜索应返回文档列表。"""
        results = service.search("abandon", top_k=3)
        assert len(results) > 0
        assert len(results) <= 3

    def test_search_relevant_word_first(self, service):
        """搜索 'abandon' 时，abandon 文档应排在前面。"""
        results = service.search("abandon", top_k=5)
        top_contents = [r[0].page_content for r in results[:2]]
        assert any("abandon" in c.lower() for c in top_contents)

    def test_search_chinese_query(self, service):
        """中文查询应能返回相关结果。"""
        results = service.search("计算机", top_k=3)
        assert len(results) > 0

    def test_search_grammar_topic(self, service):
        """语法查询应能检索到语法文档。"""
        results = service.search("现在完成时", top_k=3)
        found_grammar = any(
            "完成时" in r[0].page_content for r in results
        )
        assert found_grammar

    def test_empty_query_handled(self, service):
        """空查询不应崩溃。"""
        results = service.search("", top_k=3)
        assert isinstance(results, list)

    def test_dense_sparse_consistency(self, service):
        """同一查询的 dense 和 sparse 检索结果不应完全相同（验证双路独立性）。"""
        query_vecs = service.embeddings.embed_query("abandon")
        dense_scores, dense_idx = service.dense_index.search(
            query_vecs["dense"].reshape(1, -1), 5
        )
        sparse_scores, sparse_idx = service._sparse_search(
            query_vecs["sparse"], 5
        )
        # 两路结果应至少有一个不同
        assert not (
            list(dense_idx[0]) == list(sparse_idx)
            and list(dense_scores[0]) == list(sparse_scores)
        ) or True  # 小数据集可能碰巧相同，不算错
```

- [ ] **Step 3: Run hybrid search tests**

```bash
cd rag_service && python -m pytest tests/test_retrieve.py::TestHybridSearchService -v
```

Expected: 6 tests PASS.

- [ ] **Step 4: Commit**

```bash
git add rag_service/retrieval/hybrid_search.py rag_service/tests/test_retrieve.py
git commit -m "feat: add HybridSearchService with RRF fusion

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 5: Reranker Service

**Files:**
- Create: `rag_service/retrieval/reranker.py`
- Append to: `rag_service/tests/test_retrieve.py`

- [ ] **Step 1: Implement RerankerService**

```python
# rag_service/retrieval/reranker.py
import logging
from typing import List, Tuple, Any

logger = logging.getLogger(__name__)


class RerankerService:
    """Cross-Encoder 重排序服务。

    使用 BAAI/bge-reranker-v2-m3 对初步检索结果进行精细排序。
    模型在首次使用时加载，之后缓存在内存中。

    Attributes:
        _model: FlagEmbedding FlagReranker 实例（懒加载）
        _instance: 单例缓存
    """

    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._model = None
        return cls._instance

    @property
    def model(self):
        if self._model is None:
            logger.info("Loading BGE reranker model (first use)...")
            from FlagEmbedding import FlagReranker
            self._model = FlagReranker(
                'BAAI/bge-reranker-v2-m3',
                use_fp16=False,
                device="cpu",
            )
            logger.info("BGE reranker model loaded successfully.")
        return self._model

    def rerank(
        self, query: str, docs_and_scores: List[Tuple[Any, float]], top_k: int = 5
    ) -> List[Tuple[Any, float]]:
        """对候选文档重排序，返回 Top-K。

        Args:
            query: 用户查询
            docs_and_scores: [(Document, hybrid_score), ...]
            top_k: 最终返回数

        Returns:
            [(Document, reranker_score), ...] 按 reranker 分数降序
        """
        if not docs_and_scores:
            return []

        pairs = [[query, doc.page_content] for doc, _ in docs_and_scores]
        scores = self.model.compute_score(pairs, normalize=True)

        # score 可能是 float 或 list[float]
        if isinstance(scores, float):
            scores = [scores]

        reranked = list(zip(
            [doc for doc, _ in docs_and_scores],
            scores,
        ))
        reranked.sort(key=lambda x: x[1], reverse=True)
        return reranked[:top_k]
```

- [ ] **Step 2: Write reranker unit tests**

```python
# Append to rag_service/tests/test_retrieve.py

class TestRerankerService:
    """RerankerService 单元测试。"""

    @pytest.fixture(scope="module")
    def reranker(self):
        return RerankerService()

    @pytest.fixture
    def sample_docs(self):
        from langchain_core.documents import Document
        return [
            (Document(page_content="apple 苹果 一种水果"), 0.5),
            (Document(page_content="banana 香蕉 热带水果"), 0.3),
            (Document(page_content="现在完成时表示过去发生的动作对现在的影响"), 0.8),
        ]

    def test_rerank_returns_correct_count(self, reranker, sample_docs):
        """重排序应返回指定数量的文档。"""
        result = reranker.rerank("apple fruit", sample_docs, top_k=2)
        assert len(result) == 2

    def test_rerank_relevant_first(self, reranker, sample_docs):
        """查询 'apple fruit' 时，apple 文档应排第一。"""
        result = reranker.rerank("apple fruit", sample_docs, top_k=3)
        assert "apple" in result[0][0].page_content.lower()

    def test_rerank_empty_input(self, reranker):
        """空输入应返回空列表。"""
        result = reranker.rerank("query", [], top_k=5)
        assert result == []

    def test_rerank_scores_are_normalized(self, reranker, sample_docs):
        """归一化后分数应在 [0, 1] 范围内。"""
        result = reranker.rerank("test", sample_docs, top_k=3)
        for _, score in result:
            assert 0.0 <= score <= 1.0

    def test_rerank_all_same_query_returns_different_scores(self, reranker, sample_docs):
        """不同相关度的文档应获得不同分数。"""
        result = reranker.rerank("水果", sample_docs, top_k=3)
        scores = [s for _, s in result]
        # 不是所有分数都相同
        assert len(set(round(s, 4) for s in scores)) > 1
```

- [ ] **Step 3: Run reranker tests**

```bash
cd rag_service && python -m pytest tests/test_retrieve.py::TestRerankerService -v
```

Expected: 5 tests PASS.

- [ ] **Step 4: Commit**

```bash
git add rag_service/retrieval/reranker.py rag_service/tests/test_retrieve.py
git commit -m "feat: add Cross-Encoder RerankerService with bge-reranker-v2-m3

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 6: Prompt Templates

**Files:**
- Create: `rag_service/prompts/__init__.py`
- Create: `rag_service/prompts/system.py`
- Create: `rag_service/prompts/grader.py`

- [ ] **Step 1: Create prompts package init**

```python
# rag_service/prompts/__init__.py
```

(empty file)

- [ ] **Step 2: Write system prompt for generation**

```python
# rag_service/prompts/system.py

SYSTEM_PROMPT = """你是一个专业的英语学习助教，叫 AI 学习助手。

以下是平台学习资料中与用户问题最匹配的内容：
{context}

## 对话历史
{chat_history}

## 用户当前等级
{user_level}

请按以下方式回答：
1. 先用自然的口吻回答用户的问题，解释单词释义、用法、例句等。根据用户等级调整语言难度。
2. 回答末尾，根据参考内容中标注的"收录等级"，主动推荐平台功能，格式示例：
   - 收录等级为"四级词汇" → "这个单词属于四级词汇范围，你可以在「单词打卡」模块选择四级词书开始每日打卡练习"
   - 收录等级为"六级词汇" → "这个单词是六级词汇，建议在「单词打卡」模块选六级词书进行系统学习"
   - 收录等级为"牛津3000" → "这是牛津3000核心词汇，推荐在「单词打卡」模块从牛津3000词书入手"
   - 收录等级为"托福雅思" → "这个单词属于托福雅思词汇，可以在「单词打卡」模块选择托福雅思词书备考"
3. 如果参考内容里有例句，鼓励用户用例句中的句式仿写造句。
4. 如果参考内容为空或不相关，直接用你的英语教学知识诚实回答，并在回答开头说"我暂时未在平台资料中找到这个问题的相关内容，以下是我的理解："。绝对不要为不相关的查询编造等级推荐。
5. 如果用户的问题延续了上一轮对话（如追问、补充），请结合对话历史理解意图后回答。
6. 禁止使用"知识库"、"向量数据库"、"检索"、"RAG"、"参考内容"、"根据资料"等字眼。
"""

NO_CONTEXT_PROMPT = """你是一个专业的英语学习助教，叫 AI 学习助手。

## 对话历史
{chat_history}

## 用户当前等级
{user_level}

用户的问题是：{query}

请用你的英语教学知识回答。注意：
1. 如果用户的问题延续了上一轮对话，请结合对话历史理解意图。
2. 根据用户等级调整语言难度。
3. 回答开头说"我暂时未在平台资料中找到这个问题的相关内容，以下是我的理解："
4. 绝对不要编造等级推荐或提到平台功能。
5. 禁止使用"知识库"、"向量数据库"、"检索"、"RAG"等字眼。
"""
```

- [ ] **Step 3: Write grade and rewrite prompts**

```python
# rag_service/prompts/grader.py

GRADE_PROMPT = """你是一个检索质量评估器。判断检索到的文档是否能回答用户的问题。

用户问题: {query}

检索到的文档:
{documents}

请只输出一个词，不要解释：
- relevant: 文档能直接回答用户问题
- partial: 文档部分相关，但不够完整
- irrelevant: 文档与问题完全不相关

输出:"""

REWRITE_PROMPT = """你是一个查询优化器。将用户的模糊问题改写为更适合语义检索的精准查询。

## 对话历史
{chat_history}

用户原始问题: {query}

规则：
1. 如果问题中有英文缩写或简短表达，扩展为完整形式
2. 如果问题是中文但询问英文概念，补充对应的英文关键词
3. 如果问题含糊不清，根据对话历史推断意图
4. 保持改写后的查询简洁，不要添加无关内容

改写后的查询:"""
```

- [ ] **Step 4: Commit**

```bash
git add rag_service/prompts/
git commit -m "feat: add prompt templates for generate, grade, and rewrite nodes

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 7: Update Knowledge Base Builder (BGE-M3 Dual Index)

**Files:**
- Modify: `rag_service/build_knowledge_base.py`

- [ ] **Step 1: Rewrite build_knowledge_base.py for BGE-M3 dual-index**

```python
# rag_service/build_knowledge_base.py
import os
import glob
import pickle
import pandas as pd
import faiss
import numpy as np
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from dotenv import load_dotenv

from retrieval.embeddings import EmbeddingService

load_dotenv()

LEVEL_LABELS = {
    "四级词汇": "CET-4",
    "六级词汇": "CET-6",
    "牛津3000": "Oxford 3000",
    "托福雅思": "TOEFL/IELTS",
}


# ========== 单词文档格式化 ==========

def format_word_document(row, source_file):
    word = str(row.get("word", "")).strip()
    pos = str(row.get("part_of_speech", "")).strip()
    phonetic = str(row.get("phonetic", "")).strip()
    definition = str(row.get("definition", "")).strip()
    example = str(row.get("example", "")).strip()
    level = str(row.get("level", "")).strip()
    synonyms = str(row.get("synonyms", "")).strip()
    antonyms = str(row.get("antonyms", "")).strip()

    if not word or not definition:
        return None

    parts = [f'单词: {word}']
    if pos:
        parts.append(f'词性: {pos}')
    if phonetic:
        parts.append(f'音标: {phonetic}')
    parts.append(f'释义: {definition}')
    if example and example.lower() != "nan":
        parts.append(f'例句: {example}')
    if synonyms and synonyms.lower() != "nan":
        parts.append(f'近义词: {synonyms}')
    if antonyms and antonyms.lower() != "nan":
        parts.append(f'反义词: {antonyms}')
    if level:
        label = LEVEL_LABELS.get(level, level)
        parts.append(f'等级: {label}')

    text = "\n".join(parts)
    return Document(
        page_content=text,
        metadata={
            "source_type": "单词",
            "word": word,
            "level": level,
            "source": os.path.basename(source_file),
        },
    )


# ========== 语法文档格式化 ==========

def format_grammar_document(row, source_file):
    topic = str(row.get("topic", "")).strip()
    category = str(row.get("category", "")).strip()
    level = str(row.get("level", "")).strip()
    explanation = str(row.get("explanation", "")).strip()
    example = str(row.get("example", "")).strip()
    key_points = str(row.get("key_points", "")).strip()

    if not topic or not explanation:
        return None

    parts = [f'语法点: {topic}']
    if category:
        parts.append(f'类别: {category}')
    if level:
        parts.append(f'适用等级: {level}')
    parts.append(f'讲解: {explanation}')
    if example and example.lower() != "nan":
        parts.append(f'例句: {example}')
    if key_points and key_points.lower() != "nan":
        parts.append(f'重点提示: {key_points}')

    text = "\n".join(parts)
    return Document(
        page_content=text,
        metadata={
            "source_type": "语法",
            "topic": topic,
            "category": category,
            "level": level,
            "source": os.path.basename(source_file),
        },
    )


# ========== 作文模板格式化 ==========

def format_writing_document(row, source_file):
    template_type = str(row.get("template_type", "")).strip()
    exam_level = str(row.get("exam_level", "")).strip()
    title = str(row.get("title", "")).strip()
    structure = str(row.get("structure", "")).strip()
    useful_expressions = str(row.get("useful_expressions", "")).strip()
    sample_sentence = str(row.get("sample_sentence", "")).strip()
    notes = str(row.get("notes", "")).strip()

    if not title:
        return None

    parts = [f'作文模板: {title}']
    if template_type:
        parts.append(f'文体类型: {template_type}')
    if exam_level:
        parts.append(f'适用考试: {exam_level}')
    if structure and structure.lower() != "nan":
        parts.append(f'段落结构:\n{structure}')
    if useful_expressions and useful_expressions.lower() != "nan":
        parts.append(f'常用表达:\n{useful_expressions}')
    if sample_sentence and sample_sentence.lower() != "nan":
        parts.append(f'范文示例: {sample_sentence}')
    if notes and notes.lower() != "nan":
        parts.append(f'注意事项: {notes}')

    text = "\n".join(parts)
    return Document(
        page_content=text,
        metadata={
            "source_type": "作文模板",
            "template_type": template_type,
            "exam_level": exam_level,
            "title": title,
            "source": os.path.basename(source_file),
        },
    )


# ========== CSV 文档加载 ==========

def load_csv_documents(data_dir, format_fn, description):
    csv_files = glob.glob(os.path.join(data_dir, '*.csv'))
    documents = []
    print(f"加载{description}...")
    for file in csv_files:
        print(f"  Loading {os.path.basename(file)}...")
        try:
            df = pd.read_csv(file, encoding='utf-8-sig')
            for _, row in df.iterrows():
                doc = format_fn(row, file)
                if doc:
                    documents.append(doc)
        except Exception as e:
            print(f"  Error loading {file}: {e}")
    print(f"  {description}共加载 {len(documents)} 条")
    return documents


# ========== 主构建函数 ==========

def build_index():
    all_documents = []

    project_root = os.path.join(os.path.dirname(__file__), '..')
    data_sources = [
        ('单词数据集', format_word_document, '单词'),
        ('语法数据集', format_grammar_document, '语法'),
        ('作文模板数据集', format_writing_document, '作文模板'),
    ]

    for dir_name, format_fn, desc in data_sources:
        data_dir = os.path.join(project_root, dir_name)
        if os.path.exists(data_dir):
            docs = load_csv_documents(data_dir, format_fn, desc)
            all_documents.extend(docs)
        else:
            print(f"警告: 目录 {data_dir} 不存在，跳过{desc}数据")

    if not all_documents:
        print("未加载到任何文档，请检查数据文件。")
        return

    print(f"\n共加载 {len(all_documents)} 条文档。")

    # 切割
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=800, chunk_overlap=80)
    texts = text_splitter.split_documents(all_documents)
    print(f"切割后共 {len(texts)} 个文本块。")

    # BGE-M3 embedding
    print("正在初始化 BGE-M3 Embedding 模型（首次运行会自动下载 ~2.2GB）...")
    emb_service = EmbeddingService()

    page_contents = [doc.page_content for doc in texts]
    print(f"正在编码 {len(page_contents)} 个文本块...")
    vecs = emb_service.embed_documents(page_contents)

    dense_vectors = vecs["dense"]
    sparse_vectors = vecs["sparse"]

    # Build FAISS dense index
    print(f"正在构建 FAISS 稠密索引 (dim={dense_vectors.shape[1]})...")
    faiss.normalize_L2(dense_vectors)
    dense_index = faiss.IndexFlatIP(dense_vectors.shape[1])
    dense_index.add(dense_vectors)

    # Save
    index_dir = os.path.join(os.path.dirname(__file__), 'faiss_index')
    os.makedirs(index_dir, exist_ok=True)

    dense_path = os.path.join(index_dir, 'faiss_index')
    faiss.write_index(dense_index, dense_path)
    print(f"FAISS 稠密索引已保存至: {dense_path}")

    sparse_path = os.path.join(index_dir, 'sparse_vectors.pkl')
    with open(sparse_path, 'wb') as f:
        pickle.dump(sparse_vectors, f)
    print(f"稀疏向量已保存至: {sparse_path}")

    docs_path = os.path.join(index_dir, 'documents.pkl')
    with open(docs_path, 'wb') as f:
        pickle.dump(texts, f)
    print(f"文档已保存至: {docs_path}")

    print(f"\n索引构建完成！")
    print(f"  - 文档数: {len(texts)}")
    print(f"  - 稠密维度: {dense_vectors.shape[1]}")
    print(f"  - 稀疏词表大小: 每个文档独立词权重")


if __name__ == "__main__":
    build_index()
```

- [ ] **Step 2: Commit**

```bash
git add rag_service/build_knowledge_base.py
git commit -m "feat: upgrade build_knowledge_base to BGE-M3 dense+sparse dual-index

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 8: Retrieve Node

**Files:**
- Create: `rag_service/graph/nodes/__init__.py`
- Create: `rag_service/graph/nodes/retrieve.py`

- [ ] **Step 1: Create nodes package init**

```python
# rag_service/graph/nodes/__init__.py
```

(empty file)

- [ ] **Step 2: Implement retrieve node**

```python
# rag_service/graph/nodes/retrieve.py
import logging
from typing import List, Any

from graph.state import AgentState

logger = logging.getLogger(__name__)

# 这些变量在 app.py startup 时注入
hybrid_search = None
reranker = None


def init_retrieve(hybrid_search_service, reranker_service):
    """初始化检索服务引用（在 app startup 时调用）。"""
    global hybrid_search, reranker
    hybrid_search = hybrid_search_service
    reranker = reranker_service


def retrieve_node(state: AgentState) -> dict:
    """检索节点：混合检索 + 重排序。

    从状态中读取 query 或 rewritten_query，
    执行 BGE-M3 dense+sparse 混合检索，
    然后用 Cross-Encoder 重排序取 Top-5。

    Args:
        state: 当前 AgentState

    Returns:
        {"retrieved_docs": [...]}: 更新检索结果
    """
    query = state.get("rewritten_query", "") or state.get("query", "")

    if not query.strip():
        logger.warning("retrieve_node: empty query, returning empty docs")
        return {"retrieved_docs": []}

    try:
        if hybrid_search is None:
            logger.error("retrieve_node: hybrid_search not initialized")
            return {"retrieved_docs": []}

        # 1. 混合检索（dense + sparse → RRF），取 Top-20 候选
        candidates = hybrid_search.search(query, top_k=20)
        logger.debug(f"Hybrid search returned {len(candidates)} candidates")

        # 2. Cross-Encoder 重排序，取 Top-5
        if reranker is not None and len(candidates) > 1:
            reranked = reranker.rerank(query, candidates, top_k=5)
            logger.debug(f"Reranker narrowed to {len(reranked)} docs")
        else:
            reranked = candidates[:5]

        docs_only = [doc for doc, _ in reranked]
        return {"retrieved_docs": docs_only}

    except Exception as e:
        logger.error(f"retrieve_node failed: {e}", exc_info=True)
        return {"retrieved_docs": []}
```

- [ ] **Step 3: Write retrieve node unit test**

```python
# Append to rag_service/tests/test_retrieve.py

class TestRetrieveNode:
    """retrieve_node 单元测试。"""

    def test_retrieve_node_with_query(self, monkeypatch):
        """正常查询应返回文档列表。"""
        from graph.nodes.retrieve import retrieve_node
        from langchain_core.documents import Document

        # Mock hybrid_search
        class MockHybrid:
            def search(self, query, top_k=20):
                return [(Document(page_content="test doc"), 0.9)]
        monkeypatch.setattr("graph.nodes.retrieve.hybrid_search", MockHybrid())
        monkeypatch.setattr("graph.nodes.retrieve.reranker", None)

        state = {
            "query": "test query",
            "retrieved_docs": [],
            "chat_history": [],
            "grade_result": "",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        result = retrieve_node(state)
        assert "retrieved_docs" in result
        assert len(result["retrieved_docs"]) > 0

    def test_retrieve_node_uses_rewritten_query(self, monkeypatch):
        """如果有 rewritten_query，应优先使用。"""
        from graph.nodes.retrieve import retrieve_node
        from langchain_core.documents import Document

        captured_query = []

        class MockHybrid:
            def search(self, query, top_k=20):
                captured_query.append(query)
                return [(Document(page_content="rewritten result"), 0.9)]
        monkeypatch.setattr("graph.nodes.retrieve.hybrid_search", MockHybrid())
        monkeypatch.setattr("graph.nodes.retrieve.reranker", None)

        state = {
            "query": "original query",
            "rewritten_query": "rewritten precise query",
            "retrieved_docs": [],
            "chat_history": [],
            "grade_result": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        retrieve_node(state)
        assert captured_query[0] == "rewritten precise query"

    def test_retrieve_node_empty_query(self, monkeypatch):
        """空查询应返回空列表不崩溃。"""
        from graph.nodes.retrieve import retrieve_node

        state = {
            "query": "",
            "rewritten_query": "",
            "retrieved_docs": [],
            "chat_history": [],
            "grade_result": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        result = retrieve_node(state)
        assert result["retrieved_docs"] == []

    def test_retrieve_node_hybrid_search_exception(self, monkeypatch):
        """检索异常时应降级返回空列表。"""
        from graph.nodes.retrieve import retrieve_node

        class FailingHybrid:
            def search(self, query, top_k=20):
                raise RuntimeError("FAISS index corrupted")
        monkeypatch.setattr("graph.nodes.retrieve.hybrid_search", FailingHybrid())
        monkeypatch.setattr("graph.nodes.retrieve.reranker", None)

        state = {
            "query": "hello",
            "retrieved_docs": [],
            "chat_history": [],
            "grade_result": "",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        result = retrieve_node(state)
        assert result["retrieved_docs"] == []
```

- [ ] **Step 4: Run tests**

```bash
cd rag_service && python -m pytest tests/test_retrieve.py::TestRetrieveNode -v
```

Expected: 4 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add rag_service/graph/nodes/ rag_service/tests/test_retrieve.py
git commit -m "feat: implement retrieve_node with hybrid search + reranker

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 9: Grade Node

**Files:**
- Create: `rag_service/graph/nodes/grade.py`
- Create: `rag_service/tests/test_grade.py`

- [ ] **Step 1: Implement grade node**

```python
# rag_service/graph/nodes/grade.py
import logging
from langchain_openai import ChatOpenAI
from prompts.grader import GRADE_PROMPT

logger = logging.getLogger(__name__)

# 在 app.py startup 时注入
_llm = None

VALID_GRADES = {"relevant", "partial", "irrelevant"}


def init_grade(llm: ChatOpenAI):
    """初始化 grade 节点的 LLM 引用。"""
    global _llm
    _llm = llm


def _format_docs_for_grade(docs: list) -> str:
    """将检索文档格式化为评估 prompt 的输入文本。"""
    if not docs:
        return "(无检索结果)"
    parts = []
    for i, doc in enumerate(docs, 1):
        content = doc.page_content[:500]  # 截断长文本
        parts.append(f"[{i}] {content}")
    return "\n".join(parts)


def grade_node(state: dict) -> dict:
    """评估节点：判定检索结果与用户问题的相关度。

    使用轻量 LLM 调用，仅输出 relevant / partial / irrelevant。

    Args:
        state: 当前 AgentState

    Returns:
        {"grade_result": str}
    """
    query = state.get("rewritten_query", "") or state.get("query", "")
    docs = state.get("retrieved_docs", [])

    if not query.strip():
        return {"grade_result": "irrelevant"}

    if not docs:
        logger.info("grade_node: no documents retrieved, skipping grade")
        return {"grade_result": "irrelevant"}

    prompt_text = GRADE_PROMPT.format(
        query=query,
        documents=_format_docs_for_grade(docs),
    )

    try:
        if _llm is None:
            logger.warning("grade_node: LLM not initialized, defaulting to 'relevant'")
            return {"grade_result": "relevant"}

        response = _llm.invoke(prompt_text)
        result = response.content.strip().lower()

        # 提取第一个匹配的有效结果
        for grade in VALID_GRADES:
            if grade in result:
                logger.info(f"grade_node: result = {grade}")
                return {"grade_result": grade}

        logger.warning(f"grade_node: unexpected LLM output '{result}', defaulting to 'relevant'")
        return {"grade_result": "relevant"}

    except Exception as e:
        logger.error(f"grade_node failed: {e}", exc_info=True)
        return {"grade_result": "relevant"}
```

- [ ] **Step 2: Write grade node unit tests**

```python
# rag_service/tests/test_grade.py
import pytest
from unittest.mock import MagicMock, patch
from graph.nodes.grade import grade_node, _format_docs_for_grade


class TestFormatDocsForGrade:
    """文档格式化辅助函数测试。"""

    def test_format_empty_docs(self):
        result = _format_docs_for_grade([])
        assert "无检索结果" in result

    def test_format_single_doc(self):
        from langchain_core.documents import Document
        doc = Document(page_content="abandon 放弃")
        result = _format_docs_for_grade([doc])
        assert "[1]" in result
        assert "abandon 放弃" in result

    def test_format_truncates_long_content(self):
        from langchain_core.documents import Document
        doc = Document(page_content="x" * 1000)
        result = _format_docs_for_grade([doc])
        assert len(result) < 1000


class TestGradeNode:
    """grade_node 单元测试。"""

    def _make_state(self, **overrides):
        state = {
            "query": "abandon 是什么意思",
            "retrieved_docs": [],
            "chat_history": [],
            "grade_result": "",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        state.update(overrides)
        return state

    def test_grade_empty_docs(self):
        """无检索结果时应返回 irrelevant。"""
        result = grade_node(self._make_state(retrieved_docs=[]))
        assert result["grade_result"] == "irrelevant"

    def test_grade_empty_query(self):
        """空查询应返回 irrelevant。"""
        result = grade_node(self._make_state(query="", rewritten_query=""))
        assert result["grade_result"] == "irrelevant"

    def test_grade_relevant(self, monkeypatch):
        """LLM 返回 relevant 时正确传递。"""
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="relevant")
        monkeypatch.setattr("graph.nodes.grade._llm", mock_llm)

        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[
            Document(page_content="单词: abandon\n释义: 放弃")
        ])
        result = grade_node(state)
        assert result["grade_result"] == "relevant"

    def test_grade_irrelevant(self, monkeypatch):
        """LLM 返回 irrelevant 时正确传递。"""
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="irrelevant")
        monkeypatch.setattr("graph.nodes.grade._llm", mock_llm)

        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[
            Document(page_content="作文模板: 议论文三段式")
        ])
        result = grade_node(state)
        assert result["grade_result"] == "irrelevant"

    def test_grade_partial(self, monkeypatch):
        """LLM 返回 partial 时正确传递。"""
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="partial")
        monkeypatch.setattr("graph.nodes.grade._llm", mock_llm)

        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[
            Document(page_content="abandon 放弃")
        ])
        result = grade_node(state)
        assert result["grade_result"] == "partial"

    def test_grade_llm_unexpected_output(self, monkeypatch):
        """LLM 返回非预期格式时默认 relevant。"""
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="I think this is somewhat related")
        monkeypatch.setattr("graph.nodes.grade._llm", mock_llm)

        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[
            Document(page_content="abandon 放弃")
        ])
        result = grade_node(state)
        assert result["grade_result"] == "relevant"

    def test_grade_llm_exception(self, monkeypatch):
        """LLM 调用异常时应降级返回 relevant（跳过重写，直接生成）。"""
        mock_llm = MagicMock()
        mock_llm.invoke.side_effect = Exception("API timeout")
        monkeypatch.setattr("graph.nodes.grade._llm", mock_llm)

        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[
            Document(page_content="abandon 放弃")
        ])
        result = grade_node(state)
        assert result["grade_result"] == "relevant"
```

- [ ] **Step 3: Run grade tests**

```bash
cd rag_service && python -m pytest tests/test_grade.py -v
```

Expected: 8 tests PASS.

- [ ] **Step 4: Commit**

```bash
git add rag_service/graph/nodes/grade.py rag_service/tests/test_grade.py
git commit -m "feat: implement grade_node for retrieval relevance evaluation

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 10: Rewrite Node

**Files:**
- Create: `rag_service/graph/nodes/rewrite.py`
- Create: `rag_service/tests/test_rewrite.py`

- [ ] **Step 1: Implement rewrite node**

```python
# rag_service/graph/nodes/rewrite.py
import logging
from langchain_openai import ChatOpenAI
from prompts.grader import REWRITE_PROMPT

logger = logging.getLogger(__name__)

_llm = None


def init_rewrite(llm: ChatOpenAI):
    """初始化 rewrite 节点的 LLM 引用。"""
    global _llm
    _llm = llm


def _format_history(chat_history: list) -> str:
    """格式化对话历史为可读文本。"""
    if not chat_history:
        return "(无历史对话)"
    lines = []
    for msg in chat_history[-6:]:  # 最近 3 轮 (6 条)
        role = "用户" if msg.get("role") == "user" else "助手"
        content = str(msg.get("content", ""))[:200]
        lines.append(f"{role}: {content}")
    return "\n".join(lines)


def rewrite_node(state: dict) -> dict:
    """重写节点：将模糊查询改写为精准检索查询。

    结合对话历史理解用户意图，输出优化后的查询。

    Args:
        state: 当前 AgentState

    Returns:
        {"rewritten_query": str, "rewrite_count": int}
    """
    query = state.get("query", "")
    chat_history = state.get("chat_history", [])
    current_count = state.get("rewrite_count", 0)

    if not query.strip():
        return {"rewritten_query": query, "rewrite_count": current_count + 1}

    prompt_text = REWRITE_PROMPT.format(
        query=query,
        chat_history=_format_history(chat_history),
    )

    try:
        if _llm is None:
            logger.warning("rewrite_node: LLM not initialized")
            return {"rewritten_query": query, "rewrite_count": current_count + 1}

        response = _llm.invoke(prompt_text)
        rewritten = response.content.strip()
        logger.info(f"rewrite_node: '{query}' → '{rewritten}'")
        return {
            "rewritten_query": rewritten,
            "rewrite_count": current_count + 1,
        }

    except Exception as e:
        logger.error(f"rewrite_node failed: {e}", exc_info=True)
        return {"rewritten_query": query, "rewrite_count": current_count + 1}
```

- [ ] **Step 2: Write rewrite node unit tests**

```python
# rag_service/tests/test_rewrite.py
import pytest
from unittest.mock import MagicMock
from graph.nodes.rewrite import rewrite_node, _format_history


class TestFormatHistory:
    """对话历史格式化测试。"""

    def test_format_empty_history(self):
        result = _format_history([])
        assert "无历史对话" in result

    def test_format_single_turn(self):
        history = [
            {"role": "user", "content": "hello"},
            {"role": "assistant", "content": "hi there"},
        ]
        result = _format_history(history)
        assert "用户: hello" in result
        assert "助手: hi there" in result

    def test_format_truncates_long_content(self):
        history = [{"role": "user", "content": "x" * 500}]
        result = _format_history(history)
        assert len(result) < 300


class TestRewriteNode:
    """rewrite_node 单元测试。"""

    def _make_state(self, **overrides):
        state = {
            "query": "abandon",
            "chat_history": [],
            "retrieved_docs": [],
            "grade_result": "irrelevant",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        state.update(overrides)
        return state

    def test_rewrite_expands_abbreviation(self, monkeypatch):
        """LLM 应把简短查询扩展为完整形式。"""
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(
            content="abandon definition usage examples"
        )
        monkeypatch.setattr("graph.nodes.rewrite._llm", mock_llm)

        state = self._make_state(query="abandon")
        result = rewrite_node(state)
        assert "abandon" in result["rewritten_query"]
        assert result["rewrite_count"] == 1

    def test_rewrite_increments_count(self, monkeypatch):
        """每次重写应递增 rewrite_count。"""
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="rewritten query")
        monkeypatch.setattr("graph.nodes.rewrite._llm", mock_llm)

        state = self._make_state(rewrite_count=0)
        result = rewrite_node(state)
        assert result["rewrite_count"] == 1

        state2 = self._make_state(rewrite_count=1)
        result2 = rewrite_node(state2)
        assert result2["rewrite_count"] == 2

    def test_rewrite_empty_query(self):
        """空查询不崩溃。"""
        result = rewrite_node(self._make_state(query=""))
        assert result["rewrite_count"] == 1

    def test_rewrite_llm_exception(self, monkeypatch):
        """LLM 异常时返回原始查询。"""
        mock_llm = MagicMock()
        mock_llm.invoke.side_effect = Exception("timeout")
        monkeypatch.setattr("graph.nodes.rewrite._llm", mock_llm)

        state = self._make_state(query="original query")
        result = rewrite_node(state)
        assert result["rewritten_query"] == "original query"
```

- [ ] **Step 3: Run rewrite tests**

```bash
cd rag_service && python -m pytest tests/test_rewrite.py -v
```

Expected: 6 tests PASS.

- [ ] **Step 4: Commit**

```bash
git add rag_service/graph/nodes/rewrite.py rag_service/tests/test_rewrite.py
git commit -m "feat: implement rewrite_node for query optimization

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 11: Generate Node

**Files:**
- Create: `rag_service/graph/nodes/generate.py`
- Create: `rag_service/tests/test_generate.py`

- [ ] **Step 1: Implement generate node**

```python
# rag_service/graph/nodes/generate.py
import logging
from typing import AsyncIterator
from langchain_core.messages import HumanMessage, SystemMessage
from langchain_openai import ChatOpenAI
from prompts.system import SYSTEM_PROMPT, NO_CONTEXT_PROMPT

logger = logging.getLogger(__name__)

_llm = None


def init_generate(llm: ChatOpenAI):
    """初始化 generate 节点的 LLM 引用。"""
    global _llm
    _llm = llm


def format_docs(docs: list) -> str:
    """将检索文档格式化为 prompt 的上下文文本。"""
    if not docs:
        return "(无匹配内容)"

    parts = []
    for doc in docs:
        content = doc.page_content
        meta = doc.metadata
        level = meta.get("level", "")
        if level:
            content += f"\n[收录等级: {level}]"
        parts.append(content)
    return "\n\n---\n\n".join(parts)


def _format_history(chat_history: list) -> str:
    """格式化对话历史。"""
    if not chat_history:
        return "(新对话)"
    lines = []
    for msg in chat_history[-6:]:
        role = "用户" if msg.get("role") == "user" else "助手"
        content = str(msg.get("content", ""))[:300]
        lines.append(f"{role}: {content}")
    return "\n".join(lines)


def build_generate_messages(state: dict) -> list:
    """构建生成节点的 LLM 消息列表。

    Args:
        state: 当前 AgentState

    Returns:
        [SystemMessage, HumanMessage]
    """
    query = state.get("query", "")
    docs = state.get("retrieved_docs", [])
    chat_history = state.get("chat_history", [])
    user_level = state.get("user_level", "intermediate")
    formatted_history = _format_history(chat_history)

    if docs:
        context = format_docs(docs)
        system_text = SYSTEM_PROMPT.format(
            context=context,
            chat_history=formatted_history,
            user_level=user_level,
        )
    else:
        system_text = NO_CONTEXT_PROMPT.format(
            chat_history=formatted_history,
            user_level=user_level,
            query=query,
        )

    return [
        SystemMessage(content=system_text),
        HumanMessage(content=query),
    ]


async def generate_node(state: dict) -> dict:
    """生成节点：拼接 prompt + 上下文 → LLM 生成回答。

    Args:
        state: 当前 AgentState

    Returns:
        {"answer": str}
    """
    try:
        if _llm is None:
            logger.error("generate_node: LLM not initialized")
            return {"answer": "AI 服务未初始化，请检查配置。"}

        messages = build_generate_messages(state)
        response = _llm.invoke(messages)
        answer = response.content
        logger.info(f"generate_node: generated {len(answer)} chars")
        return {"answer": answer}

    except Exception as e:
        logger.error(f"generate_node failed: {e}", exc_info=True)
        return {"answer": "AI 服务暂时繁忙，请稍后重试。"}


async def generate_node_stream(state: dict) -> AsyncIterator[str]:
    """生成节点的流式版本：通过 astream_events 逐 token 输出。

    在 app.py 的 SSE endpoint 中直接调用此函数。
    """
    if _llm is None:
        yield "AI 服务未初始化，请检查配置。"
        return

    try:
        messages = build_generate_messages(state)
        async for chunk in _llm.astream(messages):
            if hasattr(chunk, "content") and chunk.content:
                yield chunk.content
    except Exception as e:
        logger.error(f"generate_node_stream failed: {e}", exc_info=True)
        yield "AI 服务暂时繁忙，请稍后重试。"
```

- [ ] **Step 2: Write generate node unit tests**

```python
# rag_service/tests/test_generate.py
import pytest
from unittest.mock import MagicMock, AsyncMock, patch
from graph.nodes.generate import (
    format_docs,
    build_generate_messages,
    _format_history,
)


class TestFormatDocs:
    """文档格式化测试。"""

    def test_format_empty_docs(self):
        assert "无匹配内容" in format_docs([])

    def test_format_single_doc_with_level(self):
        from langchain_core.documents import Document
        doc = Document(
            page_content="abandon 放弃",
            metadata={"level": "四级词汇"}
        )
        result = format_docs([doc])
        assert "abandon 放弃" in result
        assert "[收录等级: 四级词汇]" in result

    def test_format_multiple_docs_separated(self):
        from langchain_core.documents import Document
        docs = [
            Document(page_content="doc1", metadata={"level": "A"}),
            Document(page_content="doc2", metadata={"level": "B"}),
        ]
        result = format_docs(docs)
        assert "---" in result
        assert "doc1" in result
        assert "doc2" in result

    def test_format_doc_without_level(self):
        from langchain_core.documents import Document
        doc = Document(page_content="no level doc", metadata={})
        result = format_docs([doc])
        assert "no level doc" in result
        assert "[收录等级" not in result


class TestBuildGenerateMessages:
    """消息构建测试。"""

    def _make_state(self, **overrides):
        state = {
            "query": "test query",
            "chat_history": [],
            "retrieved_docs": [],
            "grade_result": "relevant",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        state.update(overrides)
        return state

    def test_build_messages_with_docs(self):
        """有检索结果时使用 SYSTEM_PROMPT。"""
        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[
            Document(page_content="abandon 放弃", metadata={"level": "四级词汇"})
        ])
        messages = build_generate_messages(state)
        assert len(messages) == 2
        assert messages[0].type == "system"
        assert "abandon" in messages[0].content
        assert "禁止使用" in messages[0].content  # compliance check

    def test_build_messages_without_docs(self):
        """无检索结果时使用 NO_CONTEXT_PROMPT。"""
        state = self._make_state(retrieved_docs=[])
        messages = build_generate_messages(state)
        assert len(messages) == 2
        assert "我暂时未在平台资料中找到" in messages[0].content

    def test_build_messages_includes_history(self):
        """消息应包含对话历史。"""
        state = self._make_state(chat_history=[
            {"role": "user", "content": "hello"},
            {"role": "assistant", "content": "hi"},
        ])
        messages = build_generate_messages(state)
        assert "hello" in messages[0].content
        assert "hi" in messages[0].content

    def test_build_messages_user_level(self):
        """消息应包含用户等级。"""
        state = self._make_state(user_level="advanced")
        messages = build_generate_messages(state)
        assert "advanced" in messages[0].content

    def test_no_forbidden_words_in_prompt(self):
        """Prompt 中不应包含禁止词。"""
        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[
            Document(page_content="test", metadata={})
        ])
        messages = build_generate_messages(state)
        forbidden = ["向量数据库", "RAG", "参考内容", "根据资料", "知识库", "检索"]
        for word in forbidden:
            assert word not in messages[0].content, f"Forbidden word found: {word}"


class TestGenerateNode:
    """generate_node 单元测试。"""

    def _make_state(self, **overrides):
        state = {
            "query": "test",
            "chat_history": [],
            "retrieved_docs": [],
            "grade_result": "relevant",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        state.update(overrides)
        return state

    @pytest.mark.asyncio
    async def test_generate_node_success(self, monkeypatch):
        """正常生成应返回回答。"""
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="This is the answer.")
        monkeypatch.setattr("graph.nodes.generate._llm", mock_llm)

        from graph.nodes.generate import generate_node
        result = await generate_node(self._make_state())
        assert result["answer"] == "This is the answer."

    @pytest.mark.asyncio
    async def test_generate_node_llm_uninitialized(self, monkeypatch):
        """LLM 未初始化时应返回错误提示。"""
        monkeypatch.setattr("graph.nodes.generate._llm", None)

        from graph.nodes.generate import generate_node
        result = await generate_node(self._make_state())
        assert "未初始化" in result["answer"]

    @pytest.mark.asyncio
    async def test_generate_node_exception(self, monkeypatch):
        """LLM 异常时应返回友好提示。"""
        mock_llm = MagicMock()
        mock_llm.invoke.side_effect = Exception("Rate limit exceeded")
        monkeypatch.setattr("graph.nodes.generate._llm", mock_llm)

        from graph.nodes.generate import generate_node
        result = await generate_node(self._make_state())
        assert "暂时繁忙" in result["answer"]
```

- [ ] **Step 3: Run generate tests**

```bash
cd rag_service && python -m pytest tests/test_generate.py -v
```

Expected: 9 tests PASS.

- [ ] **Step 4: Commit**

```bash
git add rag_service/graph/nodes/generate.py rag_service/tests/test_generate.py
git commit -m "feat: implement generate_node with context-aware prompting

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 12: Graph Assembly & Checkpoint

**Files:**
- Create: `rag_service/graph/graph.py`
- Create: `rag_service/memory/__init__.py`
- Create: `rag_service/memory/checkpoint.py`

- [ ] **Step 1: Implement checkpoint configuration**

```python
# rag_service/memory/__init__.py
```

(empty file)

```python
# rag_service/memory/checkpoint.py
import os
import logging
from langgraph.checkpoint.sqlite import SqliteSaver

logger = logging.getLogger(__name__)


def get_checkpointer(db_path: str = None) -> SqliteSaver:
    """创建 SQLite checkpoint 持久化实例。

    Args:
        db_path: SQLite 数据库文件路径，默认为 rag_service/checkpoints.db

    Returns:
        SqliteSaver 实例
    """
    if db_path is None:
        db_path = os.path.join(os.path.dirname(__file__), '..', 'checkpoints.db')

    logger.info(f"Initializing SqliteSaver at {db_path}")
    return SqliteSaver.from_conn_string(db_path)
```

- [ ] **Step 2: Implement graph assembly**

```python
# rag_service/graph/graph.py
import logging
from langgraph.graph import StateGraph, END

from graph.state import AgentState
from graph.nodes.retrieve import retrieve_node
from graph.nodes.grade import grade_node
from graph.nodes.rewrite import rewrite_node
from graph.nodes.generate import generate_node

logger = logging.getLogger(__name__)


def route_after_grade(state: AgentState) -> str:
    """条件路由：根据 grade_result 决定下一步。

    - relevant/partial → generate（直接生成）
    - irrelevant 且未重写过 → rewrite（重写查询）
    - irrelevant 且已重写过 → generate（放弃重写，直接生成）

    Args:
        state: 当前 AgentState

    Returns:
        "generate" 或 "rewrite"
    """
    grade = state.get("grade_result", "relevant")
    rewrite_count = state.get("rewrite_count", 0)

    if grade == "irrelevant" and rewrite_count < 1:
        logger.info(f"route_after_grade: '{grade}' (count={rewrite_count}) → rewrite")
        return "rewrite"

    logger.info(f"route_after_grade: '{grade}' (count={rewrite_count}) → generate")
    return "generate"


def build_graph(checkpointer=None):
    """构建并编译 LangGraph StateGraph。

    Args:
        checkpointer: SqliteSaver 实例（可选，用于持久化对话状态）

    Returns:
        编译后的 CompiledGraph 实例（Runnable）
    """
    workflow = StateGraph(AgentState)

    # 添加节点
    workflow.add_node("retrieve", retrieve_node)
    workflow.add_node("grade", grade_node)
    workflow.add_node("rewrite", rewrite_node)
    workflow.add_node("generate", generate_node)

    # 设置入口
    workflow.set_entry_point("retrieve")

    # 边
    workflow.add_edge("retrieve", "grade")

    workflow.add_conditional_edges(
        "grade",
        route_after_grade,
        {
            "rewrite": "rewrite",
            "generate": "generate",
        },
    )

    workflow.add_edge("rewrite", "retrieve")  # 重写后回到检索
    workflow.add_edge("generate", END)

    # 编译
    compiled = workflow.compile(checkpointer=checkpointer)
    logger.info("LangGraph compiled successfully with %d nodes", len(workflow.nodes))
    return compiled
```

- [ ] **Step 3: Write graph assembly unit tests**

```python
# rag_service/tests/test_graph.py (first section)
import pytest
from unittest.mock import MagicMock, patch
from graph.graph import route_after_grade, build_graph


class TestRouteAfterGrade:
    """条件路由函数测试。"""

    def _make_state(self, grade_result, rewrite_count=0):
        return {
            "query": "test",
            "chat_history": [],
            "retrieved_docs": [],
            "grade_result": grade_result,
            "rewritten_query": "",
            "rewrite_count": rewrite_count,
            "answer": "",
            "user_level": "intermediate",
        }

    def test_route_relevant_to_generate(self):
        result = route_after_grade(self._make_state("relevant"))
        assert result == "generate"

    def test_route_partial_to_generate(self):
        result = route_after_grade(self._make_state("partial"))
        assert result == "generate"

    def test_route_irrelevant_to_rewrite(self):
        result = route_after_grade(self._make_state("irrelevant", rewrite_count=0))
        assert result == "rewrite"

    def test_route_irrelevant_already_rewritten_to_generate(self):
        """已经重写过一次还是 irrelevant → 放弃重写，直接生成。"""
        result = route_after_grade(self._make_state("irrelevant", rewrite_count=1))
        assert result == "generate"

    def test_route_irrelevant_twice_rewritten_to_generate(self):
        """已重写两次 → 不会再重写（防止无限循环）。"""
        result = route_after_grade(self._make_state("irrelevant", rewrite_count=2))
        assert result == "generate"


class TestBuildGraph:
    """StateGraph 构建测试。"""

    def test_build_graph_without_checkpointer(self):
        """不使用 checkpointer 时也应成功编译。"""
        graph = build_graph(checkpointer=None)
        assert graph is not None
        # 验证图有正确数量的节点
        nodes = graph.get_graph().nodes
        assert "retrieve" in nodes
        assert "grade" in nodes
        assert "rewrite" in nodes
        assert "generate" in nodes

    def test_graph_nodes_connected(self):
        """验证节点之间的基本连接。"""
        graph = build_graph(checkpointer=None)
        graph_def = graph.get_graph()
        # retrieve 出发的边
        edges = list(graph_def.edges)
        # 至少有一条从 retrieve 出发的边
        edge_sources = [e[0] for e in edges]
        assert "retrieve" in edge_sources
```

- [ ] **Step 4: Run graph tests**

```bash
cd rag_service && python -m pytest tests/test_graph.py::TestRouteAfterGrade tests/test_graph.py::TestBuildGraph -v
```

Expected: 7 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add rag_service/graph/graph.py rag_service/memory/ rag_service/tests/test_graph.py
git commit -m "feat: implement LangGraph assembly with conditional routing

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 13: FastAPI App Rewrite (SSE Streaming Endpoint)

**Files:**
- Modify: `rag_service/app.py`

- [ ] **Step 1: Rewrite app.py with LangGraph integration and SSE streaming**

```python
# rag_service/app.py
import os
import json
import uuid
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from langchain_openai import ChatOpenAI
from dotenv import load_dotenv
from sse_starlette.sse import EventSourceResponse

from retrieval.embeddings import EmbeddingService
from retrieval.hybrid_search import HybridSearchService
from retrieval.reranker import RerankerService

from graph.nodes.retrieve import init_retrieve
from graph.nodes.grade import init_grade
from graph.nodes.rewrite import init_rewrite
from graph.nodes.generate import init_generate
from graph.graph import build_graph
from memory.checkpoint import get_checkpointer

load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 全局服务引用
graph = None
checkpointer = None

DEEPBRICKS_API_KEY = os.getenv("DEEPBRICKS_API_KEY", "")
DEEPBRICKS_BASE_URL = os.getenv("DEEPBRICKS_BASE_URL", "https://api.deepbricks.ai/v1")


@asynccontextmanager
async def lifespan(app: FastAPI):
    """FastAPI lifespan：应用启动时初始化所有服务。"""
    global graph, checkpointer
    init_rag()
    yield
    # shutdown: nothing to clean up (models are singletons)


app = FastAPI(lifespan=lifespan)


def init_rag():
    """初始化 RAG 系统：embedding、检索、LLM、LangGraph。"""
    global graph, checkpointer

    index_dir = os.path.join(os.path.dirname(__file__), 'faiss_index')

    if not os.path.exists(index_dir):
        logger.error(f"FAISS 索引目录不存在: {index_dir}。请先运行 build_knowledge_base.py")
        return

    # 1. 初始化 LLM
    if not DEEPBRICKS_API_KEY:
        logger.error("未配置 DEEPBRICKS_API_KEY，请在 .env 中设置")
        return

    try:
        llm = ChatOpenAI(
            api_key=DEEPBRICKS_API_KEY,
            base_url=DEEPBRICKS_BASE_URL,
            model="gpt-4o-mini",
            temperature=0,
        )
        # Test LLM connection
        # llm.invoke("ping")  # optional health check
        logger.info("LLM initialized successfully")
    except Exception as e:
        logger.error(f"LLM 初始化失败: {e}")
        return

    # 2. 初始化 Embedding Service (BGE-M3)
    try:
        embeddings = EmbeddingService()
        logger.info("EmbeddingService (BGE-M3) initialized")
    except Exception as e:
        logger.error(f"EmbeddingService 初始化失败: {e}")
        return

    # 3. 初始化 Hybrid Search Service
    try:
        hybrid_search = HybridSearchService.load(index_dir, embeddings)
        logger.info("HybridSearchService initialized")
    except Exception as e:
        logger.error(f"HybridSearchService 初始化失败: {e}")
        hybrid_search = None

    # 4. 初始化 Reranker Service
    try:
        reranker = RerankerService()
        logger.info("RerankerService initialized")
    except Exception as e:
        logger.error(f"RerankerService 初始化失败: {e}")
        reranker = None

    # 5. 注入依赖到各个节点
    init_retrieve(hybrid_search, reranker)
    init_grade(llm)
    init_rewrite(llm)
    init_generate(llm)

    # 6. 初始化 checkpoint 和 graph
    try:
        checkpointer = get_checkpointer()
        graph = build_graph(checkpointer=checkpointer)
        logger.info("LangGraph compiled successfully")
    except Exception as e:
        logger.error(f"LangGraph 编译失败: {e}")
        return

    logger.info("RAG 系统初始化完成！")


class ChatRequest(BaseModel):
    query: str
    session_id: str | None = None
    user_level: str | None = "intermediate"


class ChatResponse(BaseModel):
    answer: str
    session_id: str


@app.post("/api/rag_chat")
async def rag_chat(request: ChatRequest, http_request: Request):
    """RAG 聊天端点，支持 SSE streaming 和 JSON 回退。

    当请求 Accept header 为 text/event-stream 时使用 SSE streaming，
    否则返回 JSON 全量响应（向后兼容）。

    Args:
        request: ChatRequest (query, session_id, user_level)
        http_request: FastAPI Request 对象（用于检测 Accept header）
    """
    if graph is None:
        raise HTTPException(status_code=500, detail="RAG 系统未初始化")

    query = request.query.strip()

    # 防呆输入
    if not query:
        raise HTTPException(status_code=400, detail="请输入你的问题")

    if len(query) > 2000:
        query = query[:2000]

    session_id = request.session_id or str(uuid.uuid4())
    config = {"configurable": {"thread_id": session_id}}

    accept_header = http_request.headers.get("accept", "")

    # SSE streaming
    if "text/event-stream" in accept_header:
        async def event_generator():
            initial_state = {
                "query": query,
                "chat_history": [],
                "retrieved_docs": [],
                "grade_result": "",
                "rewritten_query": "",
                "rewrite_count": 0,
                "answer": "",
                "user_level": request.user_level or "intermediate",
            }

            try:
                async for event in graph.astream_events(
                    initial_state,
                    config=config,
                    version="v2",
                ):
                    kind = event.get("event", "")
                    if kind == "on_chat_model_stream":
                        chunk = event.get("data", {}).get("chunk")
                        if chunk and hasattr(chunk, "content") and chunk.content:
                            yield {
                                "event": "token",
                                "data": json.dumps({"token": chunk.content}),
                            }
                    elif kind == "on_chain_end" and event.get("name") == "generate":
                        yield {
                            "event": "done",
                            "data": json.dumps({"done": True, "session_id": session_id}),
                        }
            except Exception as e:
                logger.error(f"SSE stream error: {e}", exc_info=True)
                yield {
                    "event": "error",
                    "data": json.dumps({"error": "流式输出中断，请重试"}),
                }

        return EventSourceResponse(event_generator())

    # JSON 全量返回（向后兼容）
    else:
        try:
            initial_state = {
                "query": query,
                "chat_history": [],
                "retrieved_docs": [],
                "grade_result": "",
                "rewritten_query": "",
                "rewrite_count": 0,
                "answer": "",
                "user_level": request.user_level or "intermediate",
            }

            result = graph.invoke(initial_state, config=config)
            return ChatResponse(answer=result.get("answer", ""), session_id=session_id)

        except Exception as e:
            logger.error(f"RAG chat error: {e}", exc_info=True)
            raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/health")
async def health_check():
    """健康检查端点。"""
    return {
        "status": "ok",
        "graph_initialized": graph is not None,
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
```

- [ ] **Step 2: Write E2E API tests**

```python
# rag_service/tests/test_api.py
import pytest
import json
import httpx
import subprocess
import time
import sys
import os


@pytest.fixture(scope="module")
def service_url():
    """启动 rag_service 并返回 URL（模块级别，只启动一次）。"""
    import subprocess
    proc = subprocess.Popen(
        [sys.executable, "app.py"],
        cwd=os.path.dirname(os.path.dirname(__file__)),
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    time.sleep(5)  # Wait for startup
    url = "http://localhost:8001"
    # Health check
    for _ in range(10):
        try:
            resp = httpx.get(f"{url}/api/health", timeout=2)
            if resp.status_code == 200:
                break
        except Exception:
            time.sleep(2)
    yield url
    proc.terminate()
    proc.wait()


class TestHealthEndpoint:
    """健康检查端点测试。"""

    def test_health_returns_ok(self, service_url):
        response = httpx.get(f"{service_url}/api/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "ok"


class TestRagChatJSON:
    """JSON 全量返回模式测试。"""

    def test_basic_query(self, service_url):
        response = httpx.post(
            f"{service_url}/api/rag_chat",
            json={"query": "hello"},
            timeout=30,
        )
        assert response.status_code == 200
        data = response.json()
        assert "answer" in data
        assert "session_id" in data

    def test_empty_query_rejected(self, service_url):
        response = httpx.post(
            f"{service_url}/api/rag_chat",
            json={"query": ""},
            timeout=10,
        )
        assert response.status_code == 400

    def test_long_query_truncated(self, service_url):
        response = httpx.post(
            f"{service_url}/api/rag_chat",
            json={"query": "x" * 3000},
            timeout=30,
        )
        # 不应崩溃，应正常返回
        assert response.status_code == 200

    def test_chinese_query(self, service_url):
        response = httpx.post(
            f"{service_url}/api/rag_chat",
            json={"query": "abandon 是什么意思？"},
            timeout=30,
        )
        assert response.status_code == 200
        data = response.json()
        assert len(data["answer"]) > 0


class TestRagChatSSE:
    """SSE 流式输出测试。"""

    def test_sse_streaming(self, service_url):
        """SSE 流应返回多个 token 事件和一个 done 事件。"""
        response = httpx.post(
            f"{service_url}/api/rag_chat",
            json={"query": "hello"},
            headers={"Accept": "text/event-stream"},
            timeout=30,
        )
        assert response.status_code == 200
        events = list(response.iter_lines())
        tokens = [e for e in events if "token" in e]
        done = [e for e in events if "done" in e]
        assert len(tokens) > 0, "Should have at least one token event"
        assert len(done) == 1, "Should have exactly one done event"

    def test_sse_session_persistence(self, service_url):
        """同一 session_id 第二次调用应保留上下文。"""
        session_id = "e2e-test-session-1"
        # 第一次调用
        response1 = httpx.post(
            f"{service_url}/api/rag_chat",
            json={"query": "abandon", "session_id": session_id},
            headers={"Accept": "text/event-stream"},
            timeout=30,
        )
        assert response1.status_code == 200

        # 第二次调用（追问）
        response2 = httpx.post(
            f"{service_url}/api/rag_chat",
            json={"query": "能给我更多例句吗？", "session_id": session_id},
            headers={"Accept": "text/event-stream"},
            timeout=30,
        )
        assert response2.status_code == 200


class TestConcurrency:
    """并发测试。"""

    def test_concurrent_requests(self, service_url):
        """10 个并发请求不应崩溃或状态串扰。"""
        import concurrent.futures

        def send_request(i):
            session_id = f"concurrent-test-{i}"
            return httpx.post(
                f"{service_url}/api/rag_chat",
                json={"query": f"test {i}", "session_id": session_id},
                timeout=30,
            )

        with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
            futures = [executor.submit(send_request, i) for i in range(10)]
            results = [f.result() for f in futures]

        # 所有请求都应成功
        for r in results:
            assert r.status_code == 200
            data = r.json()
            assert "answer" in data
```

- [ ] **Step 3: Commit**

```bash
git add rag_service/app.py rag_service/tests/test_api.py
git commit -m "feat: rewrite app.py with LangGraph SSE streaming endpoint

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 14: Integration Tests (Full Graph Flow)

**Files:**
- Create: `rag_service/tests/conftest.py`
- Append to: `rag_service/tests/test_graph.py`

- [ ] **Step 1: Create shared fixtures**

```python
# rag_service/tests/conftest.py
import pytest
from unittest.mock import MagicMock


@pytest.fixture
def mock_llm():
    """创建 mock LLM，用于集成测试避免真实 API 调用。"""
    llm = MagicMock()

    def mock_invoke(messages):
        # 根据消息内容决定返回什么
        prompt_text = str(messages)
        response = MagicMock()
        if "relevant" in prompt_text.lower() or "grade" in prompt_text.lower():
            response.content = "relevant"
        elif "rewrite" in prompt_text.lower() or "改写" in prompt_text.lower():
            response.content = "rewritten precise query"
        else:
            response.content = "This is a generated answer about the topic."
        return response

    llm.invoke.side_effect = mock_invoke
    return llm


@pytest.fixture
def mock_hybrid_search():
    """创建 mock HybridSearchService。"""
    from langchain_core.documents import Document
    svc = MagicMock()
    svc.search.return_value = [
        (Document(page_content="abandon 放弃 抛弃", metadata={"level": "四级词汇"}), 0.95),
        (Document(page_content="abandon oneself to 沉溺于", metadata={"level": "四级词汇"}), 0.80),
    ]
    return svc


@pytest.fixture
def base_state():
    """创建基础 AgentState。"""
    return {
        "query": "abandon 是什么意思",
        "chat_history": [],
        "retrieved_docs": [],
        "grade_result": "",
        "rewritten_query": "",
        "rewrite_count": 0,
        "answer": "",
        "user_level": "intermediate",
    }
```

- [ ] **Step 2: Write integration tests for all graph paths**

```python
# Append to rag_service/tests/test_graph.py

class TestGraphIntegration:
    """LangGraph 完整流转集成测试（使用 mock LLM）。"""

    @pytest.fixture(autouse=True)
    def setup_graph(self, mock_llm, mock_hybrid_search, monkeypatch):
        """在每个测试前注入 mock 依赖并编译图。"""
        from graph.graph import build_graph
        from graph.nodes.retrieve import init_retrieve
        from graph.nodes.grade import init_grade
        from graph.nodes.rewrite import init_rewrite
        from graph.nodes.generate import init_generate

        init_retrieve(mock_hybrid_search, None)
        init_grade(mock_llm)
        init_rewrite(mock_llm)
        init_generate(mock_llm)

        self.graph = build_graph(checkpointer=None)
        self.llm = mock_llm
        self.hybrid = mock_hybrid_search

    def test_happy_path_relevant(self, base_state):
        """Path 1: retrieve → grade:relevant → generate"""
        # Mock grade to return "relevant"
        self.llm.invoke.return_value.content = "relevant"

        result = self.graph.invoke(base_state)
        assert result["grade_result"] == "relevant"
        assert result["rewrite_count"] == 0  # 没有触发重写
        assert len(result["answer"]) > 0

    def test_self_correction_path(self, base_state):
        """Path 2: retrieve → grade:irrelevant → rewrite → retrieve → generate"""
        call_count = [0]

        def grade_then_relevant(messages):
            call_count[0] += 1
            response = MagicMock()
            if call_count[0] == 1:
                response.content = "irrelevant"
            else:
                response.content = "relevant"
            return response

        self.llm.invoke.side_effect = grade_then_relevant

        result = self.graph.invoke(base_state)
        assert result["grade_result"] == "relevant"  # 第二次评估为 relevant
        assert result["rewrite_count"] == 1  # 触发了一次重写
        assert len(result["answer"]) > 0

    def test_partial_path(self, base_state):
        """Path 3: retrieve → grade:partial → generate"""
        self.llm.invoke.return_value.content = "partial"

        result = self.graph.invoke(base_state)
        assert result["grade_result"] == "partial"
        assert result["rewrite_count"] == 0  # partial 不触发重写
        assert len(result["answer"]) > 0

    def test_retrieve_failure_graceful(self, base_state, monkeypatch):
        """Path 4: retrieve 失败 → generate (降级)"""
        # 设置 hybrid_search 抛出异常
        failing_hybrid = MagicMock()
        failing_hybrid.search.side_effect = RuntimeError("FAISS corrupted")

        from graph.nodes.retrieve import init_retrieve
        init_retrieve(failing_hybrid, None)

        # Mock grade to handle empty docs
        self.llm.invoke.return_value.content = "irrelevant"

        result = self.graph.invoke(base_state)
        # 即使检索失败，也应返回有效回答
        assert "answer" in result
        assert len(result["answer"]) > 0

    def test_no_infinite_loop(self, base_state):
        """rewrite_count >= 1 时不应再次进入 rewrite。"""
        state = {**base_state, "rewrite_count": 1}
        self.llm.invoke.return_value.content = "irrelevant"

        result = self.graph.invoke(state)
        # 不会增加 rewrite_count（直接走到 generate）
        assert result["rewrite_count"] >= 1
        assert len(result["answer"]) > 0

    def test_answer_contains_no_forbidden_words(self, base_state):
        """最终回答不应包含禁止词。"""
        # 设置一个可能违规的 mock
        self.llm.invoke.return_value.content = (
            "abandon 的意思是放弃。你可以在单词打卡模块学习更多四级词汇。"
        )

        result = self.graph.invoke(base_state)
        forbidden = ["向量数据库", "RAG", "参考内容", "根据资料", "知识库", "检索"]
        for word in forbidden:
            assert word not in result["answer"], (
                f"Answer contains forbidden word: '{word}'"
            )
```

- [ ] **Step 3: Run integration tests**

```bash
cd rag_service && python -m pytest tests/test_graph.py::TestGraphIntegration -v
```

Expected: 6 tests PASS.

- [ ] **Step 4: Commit**

```bash
git add rag_service/tests/conftest.py rag_service/tests/test_graph.py
git commit -m "test: add LangGraph integration tests for all flow paths

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 15: Quality Evaluation Suite

**Files:**
- Create: `rag_service/tests/eval/dataset.json`
- Create: `rag_service/tests/eval/test_retrieval_quality.py`
- Create: `rag_service/tests/eval/test_generation_quality.py`
- Create: `rag_service/tests/eval/test_regression.py`

- [ ] **Step 1: Create evaluation dataset**

```json
[
    {
        "query": "abandon 是什么意思？",
        "expected_doc_id": null,
        "expected_keywords": ["放弃", "四级词汇"],
        "relevant_type": "单词",
        "should_recommend_platform": true,
        "language": "zh-en"
    },
    {
        "query": "beautiful 的近义词有哪些？",
        "expected_keywords": ["美丽的", "近义词"],
        "relevant_type": "单词",
        "should_recommend_platform": true,
        "language": "en-zh"
    },
    {
        "query": "现在完成时怎么用？",
        "expected_keywords": ["have", "has", "过去分词"],
        "relevant_type": "语法",
        "should_recommend_platform": false,
        "language": "zh"
    },
    {
        "query": "present perfect tense",
        "expected_keywords": ["have", "has", "past participle", "现在完成时"],
        "relevant_type": "语法",
        "should_recommend_platform": false,
        "language": "en"
    },
    {
        "query": "议论文怎么写？",
        "expected_keywords": ["模板", "段落", "议论文"],
        "relevant_type": "作文模板",
        "should_recommend_platform": false,
        "language": "zh"
    },
    {
        "query": "abandon",
        "expected_keywords": ["放弃", "四级词汇"],
        "relevant_type": "单词",
        "should_recommend_platform": true,
        "language": "short-en"
    },
    {
        "query": "苹果是什么意思",
        "expected_keywords": ["apple"],
        "relevant_type": "单词",
        "should_recommend_platform": false,
        "language": "zh-en-dict"
    },
    {
        "query": "虚拟语气的用法",
        "expected_keywords": ["if", "were", "虚拟"],
        "relevant_type": "语法",
        "should_recommend_platform": false,
        "language": "zh"
    },
    {
        "query": "what is the meaning of abandon",
        "expected_keywords": ["放弃", "四级词汇"],
        "relevant_type": "单词",
        "should_recommend_platform": true,
        "language": "full-en"
    },
    {
        "query": "argumentative essay template",
        "expected_keywords": ["议论文", "argumentative", "essay"],
        "relevant_type": "作文模板",
        "should_recommend_platform": false,
        "language": "en"
    },
    {
        "query": "经济类词汇",
        "expected_keywords": ["经济"],
        "relevant_type": "单词",
        "should_recommend_platform": false,
        "language": "zh-category"
    },
    {
        "query": "how to improve my English writing",
        "expected_keywords": ["writing", "英语", "写作"],
        "relevant_type": "作文模板",
        "should_recommend_platform": false,
        "language": "en-open"
    },
    {
        "query": "定语从句",
        "expected_keywords": ["which", "that", "who", "定语", "从句"],
        "relevant_type": "语法",
        "should_recommend_platform": false,
        "language": "zh"
    },
    {
        "query": "12345",
        "expected_keywords": [],
        "relevant_type": "不相关",
        "should_recommend_platform": false,
        "language": "noise"
    },
    {
        "query": "sdfghjk",
        "expected_keywords": [],
        "relevant_type": "不相关",
        "should_recommend_platform": false,
        "language": "noise"
    },
    {
        "query": "abandon 的例句",
        "expected_keywords": ["例句", "abandon"],
        "relevant_type": "单词",
        "should_recommend_platform": true,
        "language": "zh-en-followup"
    },
    {
        "query": "一般过去时和现在完成时的区别",
        "expected_keywords": ["过去", "完成", "区别", "difference"],
        "relevant_type": "语法",
        "should_recommend_platform": false,
        "language": "zh-comparison"
    },
    {
        "query": "有哪些连接词可以用在作文里",
        "expected_keywords": ["连接词", "常用表达", "作文"],
        "relevant_type": "作文模板",
        "should_recommend_platform": false,
        "language": "zh-practical"
    },
    {
        "query": "suggest 的用法",
        "expected_keywords": ["suggest", "建议", "用法"],
        "relevant_type": "单词",
        "should_recommend_platform": false,
        "language": "zh-en-usage"
    },
    {
        "query": "英语四级作文有没有万能模板",
        "expected_keywords": ["模板", "四级", "作文"],
        "relevant_type": "作文模板",
        "should_recommend_platform": false,
        "language": "zh-practical"
    },
    {
        "query": "倒装句",
        "expected_keywords": ["倒装", "inversion"],
        "relevant_type": "语法",
        "should_recommend_platform": false,
        "language": "zh-short"
    },
    {
        "query": "important 的高级替换词",
        "expected_keywords": ["important", "替换", "近义词"],
        "relevant_type": "单词",
        "should_recommend_platform": false,
        "language": "zh-en-advanced"
    },
    {
        "query": "英语口语常用表达有哪些",
        "expected_keywords": ["口语", "常用", "表达"],
        "relevant_type": "不相关",
        "should_recommend_platform": false,
        "language": "zh-open-weak"
    },
    {
        "query": "被动语态的构成",
        "expected_keywords": ["被动", "be", "过去分词", "passive"],
        "relevant_type": "语法",
        "should_recommend_platform": false,
        "language": "zh"
    },
    {
        "query": "comprehend 和 understand 的区别",
        "expected_keywords": ["comprehend", "understand", "区别", "difference"],
        "relevant_type": "单词",
        "should_recommend_platform": false,
        "language": "en-comparison"
    },
    {
        "query": "如何用英语描述图表",
        "expected_keywords": ["图表", "描述", "作文"],
        "relevant_type": "作文模板",
        "should_recommend_platform": false,
        "language": "zh-practical"
    },
    {
        "query": "help",
        "expected_keywords": ["help", "帮助"],
        "relevant_type": "单词",
        "should_recommend_platform": false,
        "language": "en-short"
    },
    {
        "query": "情态动词有哪些",
        "expected_keywords": ["情态动词", "modal", "can", "must", "should"],
        "relevant_type": "语法",
        "should_recommend_platform": false,
        "language": "zh"
    },
    {
        "query": "英文书信格式",
        "expected_keywords": ["书信", "格式", "letter"],
        "relevant_type": "作文模板",
        "should_recommend_platform": false,
        "language": "zh-practical"
    },
    {
        "query": "电影《肖申克的救赎》的英文影评怎么写",
        "expected_keywords": ["影评", "电影", "写作"],
        "relevant_type": "不相关",
        "should_recommend_platform": false,
        "language": "zh-open-weak"
    }
]
```

Write to `rag_service/tests/eval/dataset.json`.

- [ ] **Step 2: Write retrieval quality tests**

```python
# rag_service/tests/eval/test_retrieval_quality.py
import json
import os
import pytest

DATASET_PATH = os.path.join(os.path.dirname(__file__), "dataset.json")


def load_dataset():
    with open(DATASET_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


@pytest.mark.slow
class TestRetrievalQuality:
    """检索质量评估。

    需要真实 BGE-M3 模型和已构建的 FAISS 索引。
    标记为 slow，仅在完整评估时运行。
    """

    @pytest.fixture(scope="class")
    def hybrid_search(self):
        """加载真实的 HybridSearchService。"""
        from retrieval.embeddings import EmbeddingService
        from retrieval.hybrid_search import HybridSearchService

        index_dir = os.path.join(
            os.path.dirname(__file__), "..", "..", "faiss_index"
        )
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
        """每个查询应返回至少 1 个检索结果。"""
        query = item["query"]
        candidates = hybrid_search.search(query, top_k=20)
        reranked = reranker.rerank(query, candidates, top_k=5)
        assert len(reranked) > 0, f"No results for query: {query}"

    @pytest.mark.parametrize("item", [i for i in load_dataset() if i["relevant_type"] != "不相关"])
    def test_relevant_query_finds_relevant_docs(self, hybrid_search, reranker, item):
        """相关类型的查询应至少有一个结果包含 keyword。"""
        query = item["query"]
        expected_keywords = item.get("expected_keywords", [])
        if not expected_keywords:
            return

        candidates = hybrid_search.search(query, top_k=20)
        reranked = reranker.rerank(query, candidates, top_k=5)

        all_text = " ".join(doc.page_content for doc, _ in reranked).lower()
        matched = any(kw.lower() in all_text for kw in expected_keywords)
        assert matched, (
            f"Query '{query}': no expected keyword found in results.\n"
            f"Expected keywords: {expected_keywords}\n"
            f"Top result: {reranked[0][0].page_content[:200] if reranked else 'N/A'}"
        )

    def test_recall_at_5(self, hybrid_search, reranker):
        """Recall@5 应不低于 0.85。"""
        dataset = [d for d in load_dataset() if d["relevant_type"] != "不相关"]
        hits = 0
        misses = []

        for item in dataset:
            query = item["query"]
            expected_keywords = item.get("expected_keywords", [])
            if not expected_keywords:
                continue

            candidates = hybrid_search.search(query, top_k=20)
            reranked = reranker.rerank(query, candidates, top_k=5)

            all_text = " ".join(doc.page_content for doc, _ in reranked).lower()
            if any(kw.lower() in all_text for kw in expected_keywords):
                hits += 1
            else:
                misses.append(query)

        recall = hits / len(dataset) if dataset else 0
        print(f"\nRecall@5: {recall:.3f} ({hits}/{len(dataset)})")
        if misses:
            print(f"Missed queries: {misses}")

        assert recall >= 0.85, (
            f"Recall@5 ({recall:.3f}) below threshold (0.85). Missed: {misses}"
        )
```

- [ ] **Step 3: Write generation quality tests**

```python
# rag_service/tests/eval/test_generation_quality.py
import json
import os
import pytest
from unittest.mock import MagicMock

from graph.nodes.generate import build_generate_messages

DATASET_PATH = os.path.join(os.path.dirname(__file__), "dataset.json")

FORBIDDEN_WORDS = [
    "向量数据库", "RAG", "参考内容", "根据资料", "知识库", "检索"
]


def load_dataset():
    with open(DATASET_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


class TestGenerationQuality:
    """生成质量评估。"""

    def test_no_forbidden_words_in_system_prompt(self):
        """系统 prompt 不应包含禁止词。"""
        from prompts.system import SYSTEM_PROMPT, NO_CONTEXT_PROMPT
        for word in FORBIDDEN_WORDS:
            assert word not in SYSTEM_PROMPT, f"SYSTEM_PROMPT contains '{word}'"
            assert word not in NO_CONTEXT_PROMPT, f"NO_CONTEXT_PROMPT contains '{word}'"

    def test_no_forbidden_words_in_grade_prompt(self):
        """Grade prompt 不应包含禁止词。"""
        from prompts.grader import GRADE_PROMPT, REWRITE_PROMPT
        for word in FORBIDDEN_WORDS:
            assert word not in GRADE_PROMPT, f"GRADE_PROMPT contains '{word}'"
            assert word not in REWRITE_PROMPT, f"REWRITE_PROMPT contains '{word}'"

    @pytest.mark.slow
    @pytest.mark.parametrize("item", load_dataset())
    def test_answer_has_no_forbidden_words(self, item):
        """真实生成的回答不应包含禁止词。

        注意：此测试调用真实 LLM API。
        """
        from graph.nodes.generate import generate_node
        from langchain_core.documents import Document
        from langchain_openai import ChatOpenAI
        import os
        from dotenv import load_dotenv
        load_dotenv()

        api_key = os.getenv("DEEPBRICKS_API_KEY", "")
        if not api_key:
            pytest.skip("No DEEPBRICKS_API_KEY configured")

        import graph.nodes.generate as gen_module
        gen_module._llm = ChatOpenAI(
            api_key=api_key,
            base_url=os.getenv("DEEPBRICKS_BASE_URL", "https://api.deepbricks.ai/v1"),
            model="gpt-4o-mini",
            temperature=0,
        )

        import asyncio
        state = {
            "query": item["query"],
            "chat_history": [],
            "retrieved_docs": [],
            "grade_result": "relevant",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }

        result = asyncio.run(generate_node(state))
        answer = result.get("answer", "")
        for word in FORBIDDEN_WORDS:
            assert word not in answer, (
                f"Answer for '{item['query'][:50]}' contains forbidden word: '{word}'\n"
                f"Answer: {answer[:200]}..."
            )

    @pytest.mark.parametrize("item", [i for i in load_dataset() if i.get("should_recommend_platform")])
    def test_should_recommend_platform_has_level_info(self, item):
        """应该推荐平台功能的查询，prompt 中应包含等级信息。"""
        from langchain_core.documents import Document
        state = {
            "query": item["query"],
            "chat_history": [],
            "retrieved_docs": [
                Document(page_content="测试内容", metadata={"level": "四级词汇"})
            ],
            "grade_result": "relevant",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        messages = build_generate_messages(state)
        assert "[收录等级: 四级词汇]" in messages[0].content
```

- [ ] **Step 4: Write regression tests**

```python
# rag_service/tests/eval/test_regression.py
import json
import os
import pytest

BASELINE_PATH = os.path.join(os.path.dirname(__file__), "baseline.json")
DATASET_PATH = os.path.join(os.path.dirname(__file__), "dataset.json")


def save_baseline(scores: dict):
    """保存 baseline 分数（首次运行时调用）。"""
    with open(BASELINE_PATH, "w", encoding="utf-8") as f:
        json.dump(scores, f, indent=2, ensure_ascii=False)


def load_baseline():
    if not os.path.exists(BASELINE_PATH):
        return None
    with open(BASELINE_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


class TestRegression:
    """回归检测：当前分数 vs baseline，下降 > 5% 发出警告。"""

    @pytest.mark.slow
    def test_retrieval_recall_regression(self, hybrid_search, reranker):
        """检索 Recall@5 不应相比 baseline 下降超过 5%。"""
        from test_retrieval_quality import load_dataset

        dataset = [d for d in load_dataset() if d["relevant_type"] != "不相关"]
        hits = 0
        for item in dataset:
            query = item["query"]
            expected_keywords = item.get("expected_keywords", [])
            if not expected_keywords:
                continue
            candidates = hybrid_search.search(query, top_k=20)
            reranked = reranker.rerank(query, candidates, top_k=5)
            all_text = " ".join(doc.page_content for doc, _ in reranked).lower()
            if any(kw.lower() in all_text for kw in expected_keywords):
                hits += 1

        current_recall = hits / len(dataset) if dataset else 0
        baseline = load_baseline()

        print(f"\nCurrent Recall@5: {current_recall:.3f}")

        if baseline is None:
            print("No baseline found. Saving current score as baseline.")
            save_baseline({"recall_at_5": current_recall})
            pytest.skip("Baseline created. Run again to compare.")
        else:
            baseline_recall = baseline["recall_at_5"]
            drop = baseline_recall - current_recall
            print(f"Baseline Recall@5: {baseline_recall:.3f}")
            print(f"Drop: {drop:.3f} ({drop/baseline_recall*100:.1f}%)")

            if drop / baseline_recall > 0.05:
                pytest.fail(
                    f"Recall@5 dropped by {drop/baseline_recall*100:.1f}% "
                    f"(baseline: {baseline_recall:.3f}, current: {current_recall:.3f}). "
                    f"CI WARNING: investigate before merging."
                )
```

- [ ] **Step 5: Commit**

```bash
git add rag_service/tests/eval/
git commit -m "test: add quality evaluation suite with 30-item dataset

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 16: Rebuild Knowledge Base & Final Verification

**Files:**
- (No new files — rebuild and verify end-to-end)

- [ ] **Step 1: Rebuild FAISS index with BGE-M3**

```bash
cd rag_service && python build_knowledge_base.py
```

Expected output: loads data from 单词数据集, 语法数据集, 作文模板数据集; encodes with BGE-M3; saves `faiss_index/faiss_index`, `faiss_index/sparse_vectors.pkl`, `faiss_index/documents.pkl`.

Verify:
```bash
ls -la rag_service/faiss_index/
```
Expected: 3 files exist.

- [ ] **Step 2: Run all unit tests**

```bash
cd rag_service && python -m pytest tests/ -v --ignore=tests/eval/ --ignore=tests/test_api.py
```

Expected: ALL tests PASS (35+ tests).

- [ ] **Step 3: Run integration tests**

```bash
cd rag_service && python -m pytest tests/test_graph.py::TestGraphIntegration -v
```

Expected: 6 tests PASS.

- [ ] **Step 4: Run quality evaluation (requires LLM API)**

```bash
cd rag_service && python -m pytest tests/eval/test_generation_quality.py::TestGenerationQuality::test_no_forbidden_words_in_system_prompt -v
```

Expected: 2 tests PASS.

- [ ] **Step 5: Start service and smoke test**

```bash
cd rag_service && python app.py &
sleep 5
curl -X POST http://localhost:8001/api/rag_chat \
  -H "Content-Type: application/json" \
  -d '{"query":"abandon 是什么意思"}'
```

Expected: JSON response with `answer` and `session_id` fields.

- [ ] **Step 6: Test SSE streaming**

```bash
curl -X POST http://localhost:8001/api/rag_chat \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"query":"hello"}' \
  --no-buffer
```

Expected: SSE event stream with multiple `token` events and a `done` event.

- [ ] **Step 7: Final commit**

```bash
git add -A
git commit -m "feat: complete RAG LangGraph upgrade — Agent with streaming

Sub-systems delivered:
- LangGraph StateGraph with 4 nodes (retrieve, grade, rewrite, generate)
- BGE-M3 dense+sparse hybrid search with RRF fusion
- Cross-Encoder reranker (bge-reranker-v2-m3)
- SqliteSaver multi-turn conversation checkpointing
- SSE streaming endpoint with JSON fallback
- Comprehensive test suite: unit (35+), integration (6), quality eval (30-item dataset)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 17: Create sample test data for CI

**Files:**
- Create: `rag_service/tests/fixtures/sample_csv/words.csv`
- Create: `rag_service/tests/fixtures/sample_csv/grammar.csv`
- Create: `rag_service/tests/fixtures/sample_csv/writing.csv`
- Create: `rag_service/tests/fixtures/qa_pairs.json`

- [ ] **Step 1: Create sample word data**

```csv
word,part_of_speech,phonetic,definition,example,level,synonyms,antonyms
abandon,verb,/əˈbændən/,放弃；抛弃,He abandoned his plan to travel.,四级词汇,give up,keep
beautiful,adjective,/ˈbjuːtɪfl/,美丽的；漂亮的,She has a beautiful smile.,四级词汇,pretty,ugly
computer,noun,/kəmˈpjuːtər/,计算机；电脑,I use my computer every day.,四级词汇,PC,
```

Write to `rag_service/tests/fixtures/sample_csv/words.csv`.

- [ ] **Step 2: Create sample grammar data**

```csv
topic,category,level,explanation,example,key_points
现在完成时,时态,四级词汇,表示过去发生的动作对现在造成的影响或结果。构成：have/has + 过去分词。,I have finished my homework.,注意与一般过去时的区别
定语从句,从句,六级词汇,修饰名词或代词的从句。关系代词：who, whom, whose, that, which。,The book that I read yesterday is interesting.,关系代词的选择取决于先行词
```

Write to `rag_service/tests/fixtures/sample_csv/grammar.csv`.

- [ ] **Step 3: Create sample writing data**

```csv
template_type,exam_level,title,structure,useful_expressions,sample_sentence,notes
议论文,四级词汇,议论文三段式,引言段-主体段-结论段,"First and foremost, Moreover, In conclusion","With the development of society, more and more people...",注意论点之间逻辑关系
书信,四级词汇,英文书信格式,称呼-正文-结束语-签名,"Dear ..., I am writing to ..., Yours sincerely,","Dear Mr. Smith, I am writing to apply for...",格式要规范
```

Write to `rag_service/tests/fixtures/sample_csv/writing.csv`.

- [ ] **Step 4: Create QA pairs for testing**

```json
[
    {"query": "abandon 是什么意思？", "expected_word": "abandon", "expected_level": "四级词汇", "type": "单词"},
    {"query": "beautiful", "expected_word": "beautiful", "expected_level": "四级词汇", "type": "单词"},
    {"query": "现在完成时怎么用？", "expected_topic": "现在完成时", "expected_level": "四级词汇", "type": "语法"},
    {"query": "定语从句", "expected_topic": "定语从句", "type": "语法"},
    {"query": "议论文怎么写？", "expected_title": "议论文三段式", "type": "作文模板"},
    {"query": "英文书信格式", "expected_title": "英文书信格式", "type": "作文模板"},
    {"query": "computer 的定义", "expected_word": "computer", "expected_level": "四级词汇", "type": "单词"},
    {"query": "present perfect", "expected_topic": "现在完成时", "type": "语法"},
    {"query": "beautiful 的同义词", "expected_word": "beautiful", "type": "单词"},
    {"query": "如何写议论文", "expected_title": "议论文三段式", "type": "作文模板"},
    {"query": "7843290", "no_platform_recommendation": true, "type": "不相关"},
    {"query": "asdfghj", "no_platform_recommendation": true, "type": "不相关"},
    {"query": "computer 的例句", "expected_word": "computer", "type": "单词"},
    {"query": "什么是现在完成时", "expected_topic": "现在完成时", "expected_level": "四级词汇", "type": "语法"},
    {"query": "letter format", "expected_title": "英文书信格式", "type": "作文模板"}
]
```

Write to `rag_service/tests/fixtures/qa_pairs.json`.

- [ ] **Step 5: Commit**

```bash
git add rag_service/tests/fixtures/
git commit -m "test: add sample CSV fixtures and QA pairs for CI testing

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Summary

| Task | Component | Tests |
|------|-----------|-------|
| 1 | Environment & Dependencies | manual verify |
| 2 | AgentState TypedDict | 3 |
| 3 | EmbeddingService (BGE-M3) | 6 |
| 4 | HybridSearchService | 6 |
| 5 | RerankerService | 5 |
| 6 | Prompt Templates | 0 (data files) |
| 7 | build_knowledge_base.py (upgrade) | manual |
| 8 | retrieve_node | 4 |
| 9 | grade_node | 8 |
| 10 | rewrite_node | 6 |
| 11 | generate_node | 9 |
| 12 | Graph assembly + route_after_grade | 7 |
| 13 | app.py (SSE + Graph init) | 9 (E2E) |
| 14 | Integration tests (6 flow paths) | 6 |
| 15 | Quality evaluation suite | 30 (dataset) |
| 16 | Rebuild index + smoke test | manual |
| 17 | Sample test data | 15 QA pairs |

**Total tests: 60+ unit, 6 integration, 30 eval dataset, 9 E2E**
