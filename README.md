# 📘 英语学习平台 (SEProject-EnglishLearning)

> 基于 Vue.js 生态构建的现代化英语学习前端应用。

## 项目各种服务启动顺序

| 顺序 | 服务            | 目录                                 | 端口 | 命令（注意要在不同的文件夹中执行） |
| ---- | --------------- | ------------------------------------ | ---- | ---------------------------------- |
| 1    | MySQL           | 系统服务                             | 3306 | 略                                 |
| 2    | Python RAG      | `rag_service/`                       | 8001 | `python app.py`                    |
| 3    | Spring Boot     | `backend/english_learning_platform/` | 9090 | `mvn spring-boot:run`              |
| 4    | Node.js（可选） | `server/`                            | 3000 | `npm run dev`                      |
| 5    | Vue 前端        | 项目根目录                           | 8081 | `npm run serve`                    |

全部启动后，打开 `http://localhost:8081` 即可使用。

---

## 一、 RAG AI 智能助教

RAG（检索增强生成）模块为 AI 学习助手提供知识库支持的智能对话功能。

### 架构

```
Vue 前端 → Spr
ing Boot → Python RAG 服务 → DeepBricks LLM API
                ↑
          FAISS 向量库（本地）
          - 单词（23,574 条）
          - 语法（27 条）
          - 作文模板（10 条）
```

### 第一步：安装 Python 依赖

```bash
cd rag_service
python -m venv venv
venv\Scripts\activate       # Windows
# source venv/bin/activate  # macOS/Linux
pip install -r requirements.txt
```

> 我们项目有两个 Python 依赖环境，分别是 根目录的 `venv` 和 `rag_service/venv`，请不要混淆。

### 第二步：配置 API Key

```bash
cp .env.example .env
```

> 在文件夹 rag_service 下按照创建的 .env.example 文件新创建一个 .env 文件

编辑 `rag_service/.env`，填入你的 API Key：

```
DEEPBRICKS_API_KEY=sk-xxxxxxxxxxxxx
DEEPBRICKS_BASE_URL=https://api.deepbricks.ai/v1
```

**注意：`.env` 文件已被 gitignore，不会提交到仓库。每个开发者需要自行创建。**

### 第三步：构建知识库索引

```bash
cd rag_service
python build_knowledge_base.py
```

> 注意项目目录路径需要是全英文，否则在载入 faiss_index 时可能会报错
> 首次运行会自动下载 `BAAI/bge-small-zh-v1.5` 嵌入模型（约 100MB），生成 FAISS 索引。

### 第四步：启动 RAG 服务

```bash
python app.py
```

RAG 服务将在 `http://localhost:8001` 启动。

### 扩充知识库

向以下 CSV 文件添加新行，然后重新运行 `build_knowledge_base.py`：

| 数据类型 | 路径                                   | 说明                            |
| -------- | -------------------------------------- | ------------------------------- |
| 单词     | `单词数据集/*.csv`                     | 四级/六级/牛津3000/托福雅思词汇 |
| 语法     | `语法数据集/grammar.csv`               | 时态、语态、从句、虚拟语气等    |
| 作文     | `作文模板数据集/writing_templates.csv` | 议论文、应用文、图表作文模板    |

---

## 二、 后端部分

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

## 三、 Node.js 中间层

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

## 四、 前端部分

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
