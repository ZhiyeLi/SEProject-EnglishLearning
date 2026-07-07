# 📘 英语学习平台 (SEProject-EnglishLearning)

> 基于 Vue.js 生态构建的现代化英语学习前端应用。

## 前端部分

### 第一步：项目初始化 (Setup)

#### 安装依赖 (核心步骤)

下载项目所需的所有第三方库：

```bash
npm install
```

### 第二步：日常开发 (Development)

这是你每天开发时最常用的命令：

```bash
npm run serve
```

### 第三步：代码规范 (Linting)

**⚠️ 重点注意：此步骤在提交代码（Git Commit/Push）前必做！**

在将代码推送到 GitHub 仓库之前，请务必运行此命令来清洗代码：

```bash
npm run lint
```

### 第四步：打包部署 (Production)

当项目开发完成，准备上线时运行：

```bash
npm run build
```

---

## 后端部分

### 第一步：如果你还未初始化或者更新数据库，请参考以下步骤操作：

#### 1. 初始化数据库

在此之前，确保你已经将 MySQL 的 bin 目录加入 PATH，路径通常是：

`C:\Program Files\MySQL\MySQL Server 8.0\bin`

1. **确保 MySQL 服务已启动**

   ```bash
   # 检查服务状态
   sc query MySQL80
   ```

2. **运行导入脚本**

   ```bash
   cd .\backend\english_learning_platform
   .\import_mysql.bat
   ```

   脚本会提示输入 MySQL root 密码，然后自动完成：
   - 创建数据库
   - 导入表结构（具体文件在`.\backend\english_learning_platform\src\main\resources\schema.sql`）
   - 导入数据(具体文件在`.\backend\english_learning_platform\src\main\resources\data.sql`)

3. **更新配置文件**

   编辑 `src/main/resources/application.yml`，修改数据库密码：

   ```yaml
   spring:
     datasource:
       password: your_actual_password # 改为你的 MySQL 密码
   ```

#### 2. 题库数据补充：

需要运行

```
cd backend\english_learning_platform
.\questionbank_import.bat
```

#### 3. 单词数据补充：

需要运行

```
cd backend\english_learning_platform
.\import_words.bat
```

以导入单词相关数据

### 第二步：启动项目

```bash
cd backend\english_learning_platform
mvn clean install
mvn spring-boot:run
```

Spring Boot 将在 `http://localhost:9090` 启动。

---

## Node.js 中间层

### 安装与启动

```bash
cd server
npm install
```

复制 `.env` 文件并配置：

```bash
NODE_ENV=development
PORT=3000
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRE=7d
DB_PATH=./database/english_learning.db
CORS_ORIGIN=http://localhost:8081
```

初始化数据库并启动：

```bash
npm run init-db
npm run dev   # 开发模式（热重载）
npm start     # 生产模式
```

Node.js 服务将在 `http://localhost:3000` 启动。

---

  RAG AI 智能助教

  RAG（检索增强生成）模块为 AI 学习助手提供知识库支持的智能对话功能。

  架构

  Vue 前端 → Spring Boot → Python RAG 服务 → DeepBricks LLM API
                  ↑
            FAISS 向量库（本地）
            - 单词（23,574 条）
            - 语法（27 条）
            - 作文模板（10 条）

  第一步：安装 Python 依赖

  cd rag_service
  python -m venv venv

  # Windows (PowerShell)
  venv\Scripts\Activate

  # Windows (Git Bash) / macOS / Linux
  # source venv/bin/activate

  pip install -r requirements.txt

  ▎ 注意：如果 PowerShell 报 "running scripts is disabled" 错误，先执行：
  ▎ Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

  第二步：配置 API Key

  在 rag_service\ 目录下已有 .env 文件，如需修改 API Key，直接编辑即可：

  DEEPBRICKS_API_KEY=sk-xxxxxxxxxxxxx
  DEEPBRICKS_BASE_URL=https://api.deepbricks.ai/v1

  注意：.env 文件已被 gitignore，不会提交到仓库。

  第三步：构建知识库索引

  # 确保已激活 venv 虚拟环境（提示符显示 (venv)）
  cd rag_service
  python build_knowledge_base.py

  首次运行会自动下载 BAAI/bge-m3 嵌入模型（约 2GB），生成 FAISS 索引。GPU 编码约需 1
  分钟，CPU 编码约需 1.5 小时。

  第四步：启动 RAG 服务

  # 确保已激活 venv 虚拟环境（提示符显示 (venv)）
  # 确保在 rag_service 目录下
  python app.py

  RAG 服务将在 http://localhost:8001 启动。

  验证是否启动成功：

  # PowerShell
  Invoke-WebRequest -Uri http://localhost:8001/api/health | Select-Object -Expand
  Content

  # 或 Git Bash
  curl http://localhost:8001/api/health
  # → {"status":"ok","graph_initialized":true}

  扩充知识库

  向以下 CSV 文件添加新行，然后重新运行 build_knowledge_base.py：

  ┌─────────┬─────────────────────────────────────┬────────────────────────────────┐
  │ 数据类  │                路径                 │              说明              │
  │   型    │                                     │                                │
  ├─────────┼─────────────────────────────────────┼────────────────────────────────┤
  │ 单词    │ 单词数据集/*.csv                    │ 四级/六级/牛津3000/托福雅思词  │
  │         │                                     │ 汇                             │
  ├─────────┼─────────────────────────────────────┼────────────────────────────────┤
  │ 语法    │ 语法数据集/grammar.csv              │ 时态、语态、从句、虚拟语气等   │
  ├─────────┼─────────────────────────────────────┼────────────────────────────────┤
  │ 作文    │ 作文模板数据集/writing_templates.cs │ 议论文、应用文、图表作文模板   │
  │         │ v                                   │                                │
  └─────────┴─────────────────────────────────────┴────────────────────────────────┘

  用 Docker 启动（可选）

  # 先创建缺失的目录
  mkdir rag_service\sparse_index
  cd rag_service
  docker-compose up -d

  启动顺序（全部服务）

  ┌──────┬─────────────────┬────────────────────────────────────┬──────┐
  │ 顺序 │      服务       │                目录                │ 端口 │
  ├──────┼─────────────────┼────────────────────────────────────┼──────┤
  │ 1    │ MySQL           │ 系统服务                           │ 3306 │
  ├──────┼─────────────────┼────────────────────────────────────┼──────┤
  │ 2    │ Python RAG      │ rag_service/                       │ 8001 │
  ├──────┼─────────────────┼────────────────────────────────────┼──────┤
  │ 3    │ Spring Boot     │ backend/english_learning_platform/ │ 9090 │
  ├──────┼─────────────────┼────────────────────────────────────┼──────┤
  │ 4    │ Node.js（可选） │ server/                            │ 3000 │
  ├──────┼─────────────────┼────────────────────────────────────┼──────┤
  │ 5    │ Vue 前端        │ 项目根目录                         │ 8081 │
  └──────┴─────────────────┴────────────────────────────────────┴──────┘

  全部启动后，打开 http://localhost:8081 即可使用。

 ---
