# English Learning Platform - 后端服务

基于 Express + SQLite 的英语学习平台后端服务。

## 功能模块

- ✅ 用户认证（注册、登录、密码重置）
- ✅ 学习计划管理
- ✅ 好友系统和聊天功能
- ✅ 单词打卡系统
- ✅ 题库练习系统

## 技术栈

- **Node.js** - 运行环境
- **Express** - Web 框架
- **SQLite3** - 数据库
- **JWT** - 身份认证
- **bcryptjs** - 密码加密

## 快速开始

### 1. 安装依赖

```bash
cd server
npm install
```

### 2. 配置环境变量

复制 `.env` 文件并根据需要修改配置：

```bash
NODE_ENV=development
PORT=3000
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRE=7d
DB_PATH=./database/english_learning.db
CORS_ORIGIN=http://localhost:8080
```

### 3. 初始化数据库

```bash
npm run init-db
```

### 4. 启动服务器

开发模式（支持热重载）：

```bash
npm run dev
```

生产模式：

```bash
npm start
```

服务器将在 `http://localhost:3000` 启动。

## API 接口文档

### 认证模块 (`/api/auth`)

| 方法 | 路径              | 说明             | 认证 |
| ---- | ----------------- | ---------------- | ---- |
| POST | /register         | 用户注册         | ❌   |
| POST | /login            | 用户登录         | ❌   |
| POST | /send-verify-code | 发送验证码       | ❌   |
| POST | /reset-password   | 重置密码         | ❌   |
| GET  | /user             | 获取当前用户信息 | ✅   |
| PUT  | /user             | 更新用户信息     | ✅   |
| POST | /change-password  | 修改密码         | ✅   |

### 学习计划模块 (`/api/plans`)

| 方法   | 路径             | 说明                 | 认证 |
| ------ | ---------------- | -------------------- | ---- |
| GET    | /                | 获取时间范围内的计划 | ✅   |
| GET    | /today           | 获取今日计划         | ✅   |
| GET    | /:date           | 获取指定日期的计划   | ✅   |
| POST   | /                | 创建新计划           | ✅   |
| PUT    | /:id             | 更新计划             | ✅   |
| PUT    | /:id/complete    | 切换计划完成状态     | ✅   |
| DELETE | /:id             | 删除计划             | ✅   |
| DELETE | /batch           | 批量删除计划         | ✅   |
| GET    | /first-plan-date | 获取首次计划日期     | ✅   |
| GET    | /statistics      | 获取计划统计         | ✅   |

### 好友和消息模块 (`/api/friends`)

| 方法 | 路径          | 说明             | 认证 |
| ---- | ------------- | ---------------- | ---- |
| GET  | /search       | 搜索好友         | ✅   |
| POST | /request      | 发送好友请求     | ✅   |
| GET  | /requests     | 获取好友请求列表 | ✅   |
| POST | /accept       | 接受好友请求     | ✅   |
| POST | /reject       | 拒绝好友请求     | ✅   |
| GET  | /list         | 获取好友列表     | ✅   |
| POST | /message      | 发送消息         | ✅   |
| GET  | /messages     | 获取聊天记录     | ✅   |
| GET  | /unread-count | 获取未读消息数   | ✅   |

### 单词打卡模块 (`/api/words`)

| 方法 | 路径           | 说明                 | 认证 |
| ---- | -------------- | -------------------- | ---- |
| GET  | /types         | 获取词汇类型列表     | ✅   |
| GET  | /progress      | 获取用户进度         | ✅   |
| GET  | /list          | 获取单词列表（分页） | ✅   |
| GET  | /passed        | 获取已打卡单词       | ✅   |
| GET  | /:wordId       | 获取单词详情         | ✅   |
| POST | /mark-passed   | 标记单词为已打卡     | ✅   |
| POST | /unmark-passed | 取消单词打卡         | ✅   |
| POST | /batch-mark    | 批量标记单词         | ✅   |
| POST | /plan          | 创建打卡计划         | ✅   |
| GET  | /plan/detail   | 获取打卡计划         | ✅   |
| PUT  | /plan/status   | 更新计划状态         | ✅   |
| GET  | /today-status  | 获取今日打卡状态     | ✅   |
| GET  | /statistics    | 获取打卡统计         | ✅   |

### 题库模块 (`/api/questions`)

| 方法 | 路径                   | 说明                     | 认证 |
| ---- | ---------------------- | ------------------------ | ---- |
| GET  | /                      | 获取题目列表（支持筛选） | ✅   |
| GET  | /:id                   | 获取题目详情             | ✅   |
| POST | /:id/submit            | 提交答题结果             | ✅   |
| POST | /:id/favorite          | 收藏/取消收藏            | ✅   |
| GET  | /statistics            | 获取做题统计             | ✅   |
| GET  | /wrong/list            | 获取错题列表             | ✅   |
| POST | /wrong/:id/master      | 标记错题为已掌握         | ✅   |
| GET  | /courses/list          | 获取课程列表             | ✅   |
| GET  | /courses/:id/questions | 获取课程题目             | ✅   |
| GET  | /vocabulary/user       | 获取用户生词本           | ✅   |
| POST | /vocabulary/add        | 添加生词                 | ✅   |

## 数据库结构

数据库使用 SQLite，包含以下主要表：

- `users` - 用户表
- `plans` - 学习计划表
- `friends` - 好友关系表
- `messages` - 聊天消息表
- `friend_requests` - 好友请求表
- `word_types` - 词汇类型表
- `words` - 单词表
- `user_word_progress` - 用户单词进度表
- `daily_study_record` - 每日打卡记录表
- `checkin_plans` - 打卡计划表
- `courses` - 课程表
- `questions` - 题目表
- `question_items` - 小题表
- `user_question_records` - 做题记录表
- `user_answer_details` - 答题详情表
- `user_vocabulary` - 用户生词表

## 项目结构

```
server/
├── config/           # 配置文件
│   └── database.js   # 数据库配置
├── controllers/      # 控制器
│   ├── authController.js
│   ├── planController.js
│   ├── friendController.js
│   ├── wordController.js
│   └── questionController.js
├── middleware/       # 中间件
│   ├── auth.js       # 认证中间件
│   └── errorHandler.js
├── routes/           # 路由
│   ├── auth.js
│   ├── plans.js
│   ├── friends.js
│   ├── words.js
│   └── questions.js
├── scripts/          # 脚本
│   └── initDatabase.js
├── utils/            # 工具函数
│   └── response.js
├── database/         # 数据库文件目录
├── .env              # 环境变量
├── .gitignore
├── package.json
├── server.js         # 入口文件
└── README.md
```

## 注意事项

1. **JWT Secret**: 生产环境务必修改 `JWT_SECRET` 为强密码
2. **验证码**: 当前验证码功能为模拟实现（固定为 123456），实际使用需接入短信/邮件服务
3. **数据填充**: 单词和题目数据需要后续导入或通过外部 API 获取
4. **CORS**: 根据前端地址修改 `CORS_ORIGIN` 配置

## 开发建议

### 添加示例数据

可以创建脚本向数据库中添加示例单词和题目：

```javascript
// scripts/seedData.js
const { dbRun } = require("../config/database");

async function seedWords() {
  // 添加示例单词...
}

async function seedQuestions() {
  // 添加示例题目...
}
```

### API 测试

推荐使用 Postman 或 Thunder Client 测试 API：

1. 先调用 `/api/auth/register` 注册用户
2. 调用 `/api/auth/login` 获取 token
3. 在后续请求的 Header 中添加：`Authorization: Bearer <token>`

## 常见问题

**Q: 数据库文件在哪里？**
A: 默认在 `server/database/english_learning.db`

**Q: 如何重置数据库？**
A: 删除 `database/english_learning.db` 文件，然后运行 `npm run init-db`

**Q: 端口被占用怎么办？**
A: 修改 `.env` 文件中的 `PORT` 配置

## License

MIT
