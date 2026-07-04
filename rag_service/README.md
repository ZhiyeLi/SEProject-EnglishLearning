# RAG 智能学习助教后端服务

这是一个基于 FastAPI 和 LangChain 构建的 Python 微服务，用于为英语学习平台提供检索增强生成 (RAG) 的问答能力。

## 环境准备

建议使用 Python 3.9 或以上版本。

1. **创建并激活虚拟环境 (推荐)**:
   ```bash
   python -m venv venv
   # Windows:
   venv\Scripts\activate
   # Linux/Mac:
   source venv/bin/activate
   ```

2. **安装依赖**:
   ```bash
   pip install -r requirements.txt
   ```

3. **配置环境变量**:
   在 `rag_service` 目录下创建一个 `.env` 文件，并配置你的 Gemini API Key:
   ```env
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

## 使用步骤

1. **构建知识库 (只需运行一次，或在数据更新时运行)**:
   这个脚本会去 `../单词数据集/` 加载所有的 CSV 文件，进行切割和 Embedding 计算，最后在当前目录生成 `faiss_index` 文件夹。
   ```bash
   python build_knowledge_base.py
   ```

2. **启动 RAG 聊天服务**:
   ```bash
   python app.py
   # 或者使用 uvicorn
   # uvicorn app:app --host 0.0.0.0 --port 8001 --reload
   ```

服务将会运行在 `http://127.0.0.0:8001`，提供 `/api/rag_chat` 接口用于接收问答请求。
