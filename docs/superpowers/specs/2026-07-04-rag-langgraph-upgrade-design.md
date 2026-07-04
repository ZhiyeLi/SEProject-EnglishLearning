# RAG AI 智能助教 LangGraph 升级 — 设计规格书

**日期:** 2026-07-04
**分支:** `feature/rag-ai-assistant`
**状态:** 待评审

---

## 1. 概述

### 1.1 目标

将现有 RAG 智能助教（`rag_service/`）从简单的 LangChain LCEL 单链架构升级为基于 **LangGraph StateGraph** 的 Agent 系统，全面提升智能程度。

### 1.2 范围

**仅修改 `rag_service/` 目录**，不得改动以下部分：
- Spring Boot 后端 (`backend/`)
- Vue 3 前端 (`src/`)
- Node.js Server (`server/`)

### 1.3 核心改进

| 维度 | 当前 | 升级后 |
|------|------|--------|
| 编排引擎 | LCEL 单链 | LangGraph StateGraph（条件分支、循环、自校正） |
| 检索 | 纯向量 Top-5 (BGE-small-zh) | 混合检索 BGE-M3 dense+sparse + Cross-Encoder 重排序 |
| 对话 | 无历史（单轮） | SqliteSaver 持久化多轮 checkpoint |
| 输出 | 全量返回 | SSE streaming `astream_events()` |
| 质量保证 | 无 | 检索相关度评估 → 不相关则重写查询再检索 |
| Agent 能力 | 无 | 自主决策检索策略，自校正回答 |

---

## 2. 技术决策

### 2.1 Embedding 模型：BAAI/bge-m3

- 中英双语原生支持（100+ 语言）
- 稠密 1024维 + 稀疏输出（同时提供 BM25 级词权重，无需单独维护 BM25 索引）
- 本地 CPU 推理，首次运行自动下载（~2.2GB），后续缓存
- 2024 年开源双语 embedding 事实标准

### 2.2 Vector Store：FAISS（保留）

- 保持文件索引方案，新增稀疏向量索引
- 嵌入模型升级后需重建索引（运行一次 `build_knowledge_base.py`）

### 2.3 对话状态：SQLite

- LangGraph 原生 `SqliteSaver`，零配置零运维
- 单文件存储，随 rag_service 走
- 未来可一行配置切换 Redis（`RedisSaver`）
- 与 Spring Boot MySQL 中的消息历史各司其职：
  - MySQL `AiChatMessage`：展示用历史记录
  - SQLite checkpoint：Agent 运行时内部状态

### 2.4 部署：Docker Compose

- 保持 Python 独立微服务（LangChain/LangGraph 生态用 Python 是最佳选择）
- `docker-compose.yml`：仅 `rag_service` 一个容器（SQLite 是嵌入式的，无需额外服务）

### 2.5 知识库范围：保持现有数据源

Phase 1 聚焦检索和生成质量优化：
- `单词数据集/` — 单词定义、例句、近反义词
- `语法数据集/` — 语法知识点讲解
- `作文模板数据集/` — 写作模板和范文

---

## 3. 架构设计

### 3.1 目录结构

```
rag_service/
├── app.py                    # FastAPI 入口，SSE streaming endpoint
├── graph/
│   ├── __init__.py
│   ├── state.py              # AgentState TypedDict
│   ├── graph.py              # StateGraph 构建（节点 + 边 + 条件）
│   └── nodes/
│       ├── __init__.py
│       ├── retrieve.py       # 检索节点：BGE-M3 dense+sparse 混合检索 + 重排序
│       ├── grade.py          # 评估节点：LLM 评估检索相关度
│       ├── generate.py       # 生成节点：prompt + 上下文 + streaming
│       └── rewrite.py        # 重写节点：模糊查询 → 精准检索查询
├── retrieval/
│   ├── __init__.py
│   ├── hybrid_search.py      # BGE-M3 dense + sparse 双路检索
│   ├── reranker.py           # Cross-Encoder 重排序（bge-reranker-v2-m3）
│   └── embeddings.py         # BGE-M3 embedding 模型封装
├── memory/
│   ├── __init__.py
│   └── checkpoint.py         # SqliteSaver 配置
├── prompts/
│   ├── __init__.py
│   ├── system.py             # 系统 Prompt 模板
│   └── grader.py             # 检索评估 Prompt
├── build_knowledge_base.py   # 保留升级（dense + sparse 双索引）
├── faiss_index/              # FAISS 稠密向量索引
├── checkpoints.db            # SQLite 多轮对话 checkpoint
├── requirements.txt
├── Dockerfile
├── docker-compose.yml
└── .env
```

### 3.2 组件职责

| 组件 | 输入 | 输出 | 职责 |
|------|------|------|------|
| `retrieve` | query (str) | List[Document] | BGE-M3 dense+sparse 双路召回 → Cross-Encoder 重排序 → Top-5 |
| `grade` | query + retrieved_docs | "relevant" / "partial" / "irrelevant" | LLM 轻量判定检索结果能否回答问题 |
| `rewrite` | query + chat_history | rewritten_query (str) | LLM 将模糊/不完整问题改写为精准检索查询 |
| `generate` | query + docs + chat_history + user_level | streaming tokens | 拼接 prompt + 上下文 → LLM SSE 流式输出 |
| `SqliteSaver` | AgentState | checkpoint | 每轮对话完整状态持久化，按 thread_id 隔离 |

---

## 4. Agent 数据流

### 4.1 状态定义

```python
class AgentState(TypedDict):
    query: str              # 用户当前问题
    chat_history: list      # 多轮对话历史 [{"role": "user/assistant", "content": "..."}]
    retrieved_docs: list    # 检索到的文档列表
    grade_result: str       # "relevant" | "partial" | "irrelevant"
    rewritten_query: str    # 改写后的查询（如触发重写）
    answer: str             # 最终回答（流式累积）
    user_level: str         # 用户英语等级（可选，默认 "intermediate"）
```

### 4.2 主循环

```
用户输入 → [RETRIEVE]
                ↓
            [GRADE] ── relevant ──→ [GENERATE] → SSE streaming → 前端
                │                        ↑
           irrelevant                    │
                ↓                        │
           [REWRITE] ───→ [RETRIEVE] ───┘
                              ↑
                          (仅一次)
```

### 4.3 链路说明

| 路径 | 条件 | 行为 |
|------|------|------|
| retrieve → grade:relevant → generate | 检索结果精准 | 直接生成回答 |
| retrieve → grade:partial → generate | 检索结果部分相关 | 带结果生成，标注"部分信息可能不够完整" |
| retrieve → grade:irrelevant → rewrite → retrieve → generate | 检索失败 | LLM 改写查询，二次检索后生成（限 1 次循环） |
| retrieve 失败 → generate | 检索服务异常 | 降级为纯 LLM 回答，标明"暂时无法检索资料" |
| grade 失败 → generate | 评估节点异常 | 跳过重写，直接生成 |

---

## 5. 对外接口

### 5.1 `POST /api/rag_chat`

**请求体：**
```json
{
    "query": "abandon 是什么意思？",
    "session_id": "optional-session-uuid"
}
```

**响应：** SSE 流
```
data: {"token": "abandon"}
data: {"token": " 是"}
data: {"token": "..."}
...
data: {"done": true, "session_id": "abc-123"}
```

### 5.2 向后兼容

- `session_id` 为空时，自动创建新 session
- 响应中返回 `session_id` 供前端后续使用
- 旧版前端如果不处理 SSE，可以用 `Accept: application/json` 头回退到全量返回（Phase 1 兼容保留）

---

## 6. Prompt 设计

### 6.1 系统 Prompt（生成节点）

保留现有 prompt 结构，增强以下能力：
- **多轮感知**：在 system prompt 中注入近 3 轮对话摘要
- **等级自适应**：根据 `user_level` 调整回答语言难度
- **诚实原则**：检索结果不相关时回复"这个问题我暂时无法从学习资料中找到确切答案，但根据我的理解..."

### 6.2 评估 Prompt（grade 节点）

轻量判定（~30 token 输出），只输出 `relevant` / `partial` / `irrelevant`，不做任何额外解释。

### 6.3 重写 Prompt（rewrite 节点）

输入用户原始查询 + 对话上下文，输出一个更适合向量检索的精准查询（如扩写缩写、中英互译、补充关键词）。

---

## 7. 错误处理

### 7.1 降级策略

| 故障场景 | 降级行为 | 用户感知 |
|----------|----------|----------|
| FAISS 索引加载失败 | 纯 LLM 回答 | "暂时无法检索资料，以下是我的理解..." |
| BGE-M3 Embedding 异常 | 纯 LLM 回答 | 同上 |
| LLM API 超时/限流 | 重试 1 次（2s 退避）→ 返回错误 | "AI 服务暂时繁忙，请稍后重试" |
| grade 节点异常 | 跳过重写，直接生成 | 用户无感知 |
| rewrite 节点异常 | 直接用原始查询生成 | 用户无感知 |
| SSE 连接中断 | 前端显示已生成部分 | "连接中断，请重试" |
| checkpoint 写入失败 | 记录日志，对话继续 | 下次对话丢失该轮上下文 |

### 7.2 防呆输入

- 空查询 → HTTP 400
- 超长查询 (>2000 字符) → 截断处理
- 纯标点/纯数字 → 正常处理，让 LLM 自行引导

---

## 8. 测试策略

### 8.1 Layer 1 — 单元测试

每个节点 ≥6 个测试用例：

| 节点 | 测试覆盖 |
|------|----------|
| `retrieve` | 正常中/英文查询、空结果、纯标点查询、embedding 超时 mock、FAISS 损坏文件 mock、双路检索一致性 |
| `grade` | relevant/irrelevant/partial 判定、LLM 非预期格式、LLM 超时、空检索结果评估 |
| `rewrite` | 模糊查询改写、中→英改写、超短查询、LLM 超时 |
| `generate` | 有/无上下文生成、带历史生成、上下文超长截断、LLM 限流重试、SSE token 格式 |
| `checkpoint` | 保存-恢复一致性、跨 session 隔离、损坏恢复、空历史 |

### 8.2 Layer 2 — 集成测试

验证 StateGraph 完整流转的所有分支：
- `relevant` happy path
- `irrelevant → rewrite → retrieve` 自校正路径
- `partial` 部分相关路径
- `grade 失败 → generate` 降级路径
- `retrieve 失败 → generate` 降级路径
- `generate 失败` 错误提示路径

每条路径验证 checkpoint 状态完整性。

### 8.3 Layer 3 — 质量评估

30 条手工标注评测数据集：

| 类型 | 数量 | 指标 |
|------|------|------|
| 单词查询（英→中、中→英、辨析） | 10 条 | Recall@5 ≥ 0.85, MRR ≥ 0.7 |
| 语法查询（时态、从句、虚拟语气） | 10 条 | Recall@5 ≥ 0.85, MRR ≥ 0.7 |
| 作文模板（推荐、句式建议） | 5 条 | Recall@5 ≥ 0.85, MRR ≥ 0.7 |
| 模糊/不相关查询 | 5 条 | 禁止编造等级推荐 |

生成质量验证：
- 回答包含 expected_keywords
- 回答不含禁止词（"向量数据库"、"RAG"、"参考内容"、"根据资料"、"知识库"、"检索"）
- 等级推荐与检索结果匹配
- 回归检测：全量数据集对比 baseline，分数下降 > 5% → CI 警告（需人工确认，不阻断）

### 8.4 Layer 4 — E2E

- 完整 SSE 流验证（httpx + 真实子进程）
- 多轮对话（同一 session_id 3 轮连贯问答）
- 10 并发请求（无崩溃、无 session 串扰）
- 冷启动验证（首请求 embedding 模型加载）

### 8.5 CI 阻断规则

| 条件 | 动作 |
|------|------|
| 任一单元/集成测试失败 | **阻断** |
| Recall@5 < 0.85 | **阻断** |
| 禁止词出现在回答中 | **阻断** |
| 回归分数下降 > 5% | 警告（不阻断，需人工确认） |

---

## 9. 依赖变更

### 9.1 `requirements.txt`

```diff
- langchain
- langchain-community
- langchain-openai
- langchain-huggingface
+ langchain>=0.3.0
+ langchain-community>=0.3.0
+ langchain-openai>=0.3.0
+ langchain-huggingface>=0.3.0
+ langgraph>=0.2.0
+ langgraph-checkpoint-sqlite>=2.0.0
  sentence-transformers
  faiss-cpu
+ FlagEmbedding>=1.3.0    # BGE-M3 官方封装
  pandas
  python-dotenv
  fastapi
  uvicorn
+ sse-starlette>=2.0.0     # SSE 流式响应
+ httpx                     # 测试用
+ pytest
+ pytest-asyncio
```

### 9.2 移除的依赖

无。全部保留，仅追加新依赖。

---

## 10. 不做的（明确排除）

1. ❌ 不改 Spring Boot 后端 — `AiChatService.java` 不变
2. ❌ 不改 Vue 前端 — 前端后续自行适配 SSE
3. ❌ 不引入新数据库服务 — SQLite 嵌入式，不跑 Redis/PostgreSQL
4. ❌ 不扩展新数据源 — 保持单词/语法/作文三个 CSV 目录
5. ❌ 不引入多 Agent 架构 — Phase 1 单 Agent + 内部决策路由
6. ❌ 不更改 API 路径 — `/api/rag_chat` 不变

---

## 11. 风险与缓解

| 风险 | 概率 | 缓解 |
|------|------|------|
| BGE-M3 首次下载慢/下载失败 | 中 | Dockerfile 预下载模型到镜像 |
| LangGraph API 变化（0.x 版本） | 中 | 锁定版本号，不追 latest |
| BGE-M3 CPU 推理延迟影响体验 | 低 | 当前数据量小（<5000 条），检索 < 500ms |
| 流式输出与现有前端不兼容 | 低 | 保留 JSON 全量返回兜底 |
