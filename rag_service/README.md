# RAG 智能学习助教后端服务

基于 LangGraph + BGE-M3 + FAISS 的检索增强生成（RAG）微服务，为英语学习平台提供 AI 驱动的智能问答能力。

## 技术栈

| 组件 | 技术 |
|------|------|
| AI 编排 | LangGraph StateGraph（检索→评估→重写→生成） |
| 大语言模型 | GPT-4o-mini（DeepBricks API） |
| Embedding | BAAI/bge-m3（本地部署，1024 维，GPU fp16） |
| 向量检索 | FAISS 稠密索引 + TF-IDF 稀疏索引 + RRF 融合 |
| 对话记忆 | SQLite（LangGraph SqliteSaver） |
| Web 框架 | FastAPI + SSE 流式输出 |

## 环境准备

Python 3.9+，推荐使用虚拟环境。

```bash
# 创建并激活虚拟环境
python -m venv venv
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

### GPU 支持（Windows）

Windows 下 CUDA 加速需使用特定 PyTorch 版本：

```bash
pip install "torch>=2.5" --index-url https://download.pytorch.org/whl/cu118
```

> **注意**：torch cu124 在 Windows 上存在已知 segfault，请使用 cu118 版本。

## 配置环境变量

在 `rag_service` 目录下创建 `.env` 文件：

```env
DEEPBRICKS_API_KEY=your_api_key_here
DEEPBRICKS_BASE_URL=https://api.deepbricks.ai/v1
```

## 使用步骤

### 1. 构建知识库（首次运行）

```bash
python build_knowledge_base.py
```

脚本会加载 `../单词数据集/`、`../语法数据集/`、`../作文模板数据集/` 中的 CSV 文件，切割后用 BGE-M3 编码，生成 FAISS 索引（保存在 `faiss_index/` 目录）。

GPU 编码约需 1 分钟，CPU 编码约需 1.5 小时。

### 2. 启动服务

```bash
python app.py
```

服务运行在 `http://localhost:8001`。

### 3. 验证

```bash
# 健康检查
curl http://localhost:8001/api/health
# → {"status":"ok","graph_initialized":true}

# JSON 模式
curl -X POST http://localhost:8001/api/rag_chat \
  -H "Content-Type: application/json" \
  -d '{"query":"abandon 是什么意思"}'

# SSE 流式模式
curl -X POST http://localhost:8001/api/rag_chat \
  -H "Accept: text/event-stream" \
  -H "Content-Type: application/json" \
  -d '{"query":"abandon 是什么意思","session_id":"my-session"}'
```

## API 接口

### POST /api/rag_chat

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| query | string | 是 | 用户问题 |
| session_id | string | 否 | 会话 ID，用于多轮对话上下文记忆 |
| user_level | string | 否 | 用户英语等级（默认 intermediate） |

**响应模式：**

- **SSE 流式**（`Accept: text/event-stream`）：逐 token 实时推送
- **JSON 全量**（默认）：一次性返回完整回答

### GET /api/health

服务健康检查，返回 `graph_initialized` 状态。
