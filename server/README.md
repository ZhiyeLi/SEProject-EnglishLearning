# 英语学习平台 - 后端服务器

轻量级 Node.js + Express + SQLite 后端，为前端 Vue 应用提供数据接口。

## 🚀 快速开始

### 1. 安装依赖
```bash
npm install
```

### 2. 初始化数据库
```bash
npm run init-db
```
此命令会在 `data/` 目录下创建 SQLite 数据库文件 `elw.sqlite`，包含所有必要表结构。

### 3. 启动服务器
```bash
npm start
```
服务器将监听 `http://localhost:3000`

## 📡 API 端点列表

### 健康检查
```
GET /api/health
```
验证服务器和数据库连接状态。

**响应：**
```json
{
  "status": "ok",
  "timestamp": "2025-11-12T12:34:56.789Z"
}
```

---

### 单词列表（分页）
```
GET /api/words?limit=100&offset=0
```
获取单词列表。

**查询参数：**
- `limit` (可选): 返回条数，默认 100，最多 500
- `offset` (可选): 起始位置，默认 0

**响应：**
```json
{
  "data": [
    { "word_id": 1, "word_content": "abandon" },
    { "word_id": 2, "word_content": "ability" }
  ],
  "count": 2,
  "limit": 100,
  "offset": 0
}
```

---

### 单词详情
```
GET /api/words/:id
```
获取单词的详细信息（词性、翻译、例句等）。

**路径参数：**
- `id`: 单词 ID

**响应：**
```json
{
  "word": { "word_id": 1, "word_content": "abandon" },
  "pos_list": [
    { "pos_id": 1, "part_of_speech": "verb" }
  ],
  "translations": [
    {
      "trans_id": 1,
      "chinese_meaning": "放弃",
      "example_sentence": "I will not abandon my dreams.",
      "part_of_speech": "verb"
    }
  ]
}
```

---

### 获取用户信息
```
GET /api/users/:id
```
获取用户基本信息和学习进度摘要。

**路径参数：**
- `id`: 用户 ID

**响应：**
```json
{
  "user_id": 1,
  "username": "john_doe",
  "nickname": "John",
  "email": "john@example.com",
  "avatar": "https://...",
  "create_time": "2025-11-01T10:00:00Z",
  "update_time": "2025-11-12T10:00:00Z",
  "status": 1,
  "progress": {
    "total": 50,
    "completed": 23
  }
}
```

---

### 创建新用户
```
POST /api/users
```
注册新用户（演示用，生产环境应添加密码加密、验证等）。

**请求体：**
```json
{
  "username": "jane_doe",
  "password": "secure_password_hash",
  "nickname": "Jane",
  "email": "jane@example.com",
  "phone": "13800138000",
  "avatar": "https://..."
}
```

**响应：**
```json
{
  "user_id": 2,
  "username": "jane_doe",
  "nickname": "Jane",
  "email": "jane@example.com"
}
```

---

### 获取用户单词记忆状态
```
GET /api/users/:id/words/memory
```
获取用户已记住的单词列表。

**路径参数：**
- `id`: 用户 ID

**响应：**
```json
{
  "data": [
    { "id": 1, "word_id": 1, "word_content": "abandon", "is_remembered": 1 },
    { "id": 2, "word_id": 2, "word_content": "ability", "is_remembered": 0 }
  ],
  "count": 2
}
```

---

### 更新用户单词记忆状态
```
POST /api/users/:id/words/:wordId/remember
```
标记单词为已记住或未记住。

**路径参数：**
- `id`: 用户 ID
- `wordId`: 单词 ID

**请求体：**
```json
{
  "is_remembered": true
}
```

**响应：**
```json
{
  "success": true,
  "message": "Memory status updated"
}
```

---

### 获取用户所在的学习小组
```
GET /api/users/:id/groups
```
获取用户加入的所有活跃学习小组。

**路径参数：**
- `id`: 用户 ID

**响应：**
```json
{
  "data": [
    {
      "group_id": 1,
      "group_name": "托福准备组",
      "creator_id": 5,
      "create_time": "2025-11-01T10:00:00Z",
      "role": 1
    }
  ],
  "count": 1
}
```

---

## 🗄️ 数据库表结构

| 模块 | 表名 | 用途 |
|------|------|------|
| **用户管理** | `user_base` | 用户账户信息 |
| | `user_learning_preference` | 学习偏好设置 |
| | `user_learning_progress` | 学习进度跟踪 |
| | `word_memory_status` | 单词记忆状态 |
| | `user_audit_log` | 用户操作审计日志 |
| **社交功能** | `user_group` | 学习小组 |
| | `user_group_relation` | 小组成员关系 |
| | `group_message` | 群组消息 |
| **词汇库** | `words` | 单词库 |
| | `word_pos` | 词性信息 |
| | `word_translation` | 中英翻译 |
| | `word_pos_changes` | 词形变化 |
| | `word_phrases` | 短语与习语 |

---

## 🔐 生产环境建议

1. **密码加密**: 使用 bcrypt 或 argon2 加密存储密码
2. **身份验证**: 实现 JWT token 机制
3. **CORS**: 配置跨域策略
4. **参数验证**: 使用 `joi` 或 `zod` 库进行输入验证
5. **数据库备份**: 定期备份 SQLite 文件
6. **错误日志**: 集成日志系统（如 Winston）
7. **速率限制**: 添加 `express-rate-limit` 防止滥用
8. **环境变量**: 使用 `.env` 文件管理配置

## 📝 环境变量

创建 `.env` 文件（可选）：
```
PORT=3000
NODE_ENV=development
DB_PATH=./data/elw.sqlite
```

---

## 🐛 故障排除

**数据库找不到？**
```bash
npm run init-db
```

**端口被占用？**
```bash
PORT=3001 npm start
```

**连接错误？**
确保 SQLite 已正确安装：
```bash
npm install sqlite3
```

---

**最后更新：** 2025-11-12  
**维护者：** Your Team

