# 题库系统后端需求文档

## 一、API 需求表格

| API 路径                          | 方法 | 功能描述                       | 请求参数                                                                                    | 返回数据                           |
| --------------------------------- | ---- | ------------------------------ | ------------------------------------------------------------------------------------------- | ---------------------------------- |
| `/api/questions`                  | GET  | 获取题目列表（支持分页和筛选） | `page`, `limit`, `type`, `difficulty`, `status`, `courseId`, `keyword`, `includeVocabulary` | 题目列表、总数、分页信息           |
| `/api/questions/:id`              | GET  | 获取单个题目详情               | `id` (路径参数)                                                                             | 题目完整信息（含题干、选项、解析） |
| `/api/questions/:id/submit`       | POST | 提交答题结果                   | `questionId`, `answers[]`, `timeSpent`                                                      | 批改结果、正确率、解析             |
| `/api/questions/:id/favorite`     | POST | 收藏/取消收藏题目              | `questionId`                                                                                | 收藏状态                           |
| `/api/questions/statistics`       | GET  | 获取用户做题统计               | `userId` (从 token 获取)                                                                    | 今日做题数、总正确率、累计数据     |
| `/api/questions/wrong`            | GET  | 获取错题列表                   | `page`, `limit`, `type`                                                                     | 错题列表、统计信息                 |
| `/api/questions/wrong/:id/master` | POST | 标记错题为已掌握               | `questionId`                                                                                | 操作结果                           |
| `/api/questions/plan`             | POST | 将题目加入学习计划             | `questionId`, `planDate`, `note`                                                            | 计划项信息                         |
| `/api/courses`                    | GET  | 获取课程列表                   | 无                                                                                          | 课程列表（含章节信息）             |
| `/api/courses/:id/questions`      | GET  | 获取课程关联题目               | `courseId`, `chapterId`                                                                     | 关联题目列表                       |
| `/api/vocabulary/user`            | GET  | 获取用户生词本                 | `userId` (从 token 获取)                                                                    | 用户生词列表                       |
| `/api/vocabulary/add`             | POST | 添加生词到生词本               | `word`, `translation`, `sourceQuestionId`                                                   | 添加结果                           |
| `/api/vocabulary/questions`       | GET  | 获取包含用户生词的题目         | `userId` (从 token 获取)                                                                    | 包含生词的题目列表                 |

### API 补充说明

1. **认证与授权**：所有 API 需要在请求头携带 JWT Token 进行身份验证，用户 ID 从 Token 中解析获取，确保数据隔离和安全性。

2. **分页设计**：列表类 API 统一采用 `page` 和 `limit` 参数进行分页，返回数据包含 `total`（总记录数）、`pages`（总页数）、`currentPage`（当前页）字段。

3. **筛选条件**：`/api/questions` 接口支持多条件组合筛选，`type` 和 `difficulty` 参数支持传入数组（逗号分隔），实现多选筛选功能。

4. **答题提交**：提交答案时需要记录用户答案、答题时间、是否正确等信息，用于生成学习报告和推荐算法。

5. **关联查询**：题目与课程的关联通过 `relatedCourse` 和 `relatedChapter` 字段实现，支持按课程章节精确筛选题目。

6. **生词联动**：生词本功能与单词打卡模块联动，新增生词时可记录来源题目 ID，方便用户追溯学习场景。

---

## 二、数据表需求表格

| 表名                      | 字段名             | 类型         | 说明                                                        | 约束                  |
| ------------------------- | ------------------ | ------------ | ----------------------------------------------------------- | --------------------- |
| **questions**             | id                 | VARCHAR(36)  | 题目唯一标识                                                | PRIMARY KEY           |
|                           | type               | ENUM         | 题型：listening/reading/writing/grammar/vocabulary          | NOT NULL              |
|                           | difficulty         | ENUM         | 难度：beginner/cet4-6/postgraduate/toefl-ielts/professional | NOT NULL              |
|                           | title              | VARCHAR(255) | 题目标题                                                    | NOT NULL              |
|                           | preview            | TEXT         | 题目预览内容                                                |                       |
|                           | content            | TEXT         | 题目完整内容（文章/音频脚本等）                             | NOT NULL              |
|                           | audio_url          | VARCHAR(500) | 听力音频URL（听力题专用）                                   |                       |
|                           | related_course_id  | VARCHAR(36)  | 关联课程ID                                                  | FOREIGN KEY           |
|                           | related_chapter    | VARCHAR(100) | 关联章节名称                                                |                       |
|                           | highlight_words    | JSON         | 高亮关键词数组                                              |                       |
|                           | tags               | JSON         | 标签数组                                                    |                       |
|                           | created_at         | DATETIME     | 创建时间                                                    | NOT NULL              |
|                           | updated_at         | DATETIME     | 更新时间                                                    |                       |
| **question_items**        | id                 | VARCHAR(36)  | 小题唯一标识                                                | PRIMARY KEY           |
|                           | question_id        | VARCHAR(36)  | 所属题目ID                                                  | FOREIGN KEY, NOT NULL |
|                           | question_text      | TEXT         | 小题题干                                                    | NOT NULL              |
|                           | question_type      | ENUM         | 题型：choice/essay/fill                                     | NOT NULL              |
|                           | options            | JSON         | 选项数组（选择题）                                          |                       |
|                           | answer             | VARCHAR(50)  | 正确答案                                                    | NOT NULL              |
|                           | explanation        | TEXT         | 答案解析                                                    |                       |
|                           | order_num          | INT          | 题目顺序                                                    | NOT NULL              |
| **user_question_records** | id                 | VARCHAR(36)  | 记录唯一标识                                                | PRIMARY KEY           |
|                           | user_id            | VARCHAR(36)  | 用户ID                                                      | FOREIGN KEY, NOT NULL |
|                           | question_id        | VARCHAR(36)  | 题目ID                                                      | FOREIGN KEY, NOT NULL |
|                           | status             | ENUM         | 状态：not-done/done                                         | NOT NULL              |
|                           | last_result        | ENUM         | 最近结果：correct/wrong                                     |                       |
|                           | last_attempt_date  | DATETIME     | 最近答题时间                                                |                       |
|                           | correct_count      | INT          | 正确次数                                                    | DEFAULT 0             |
|                           | wrong_count        | INT          | 错误次数                                                    | DEFAULT 0             |
|                           | is_favorited       | BOOLEAN      | 是否收藏                                                    | DEFAULT FALSE         |
|                           | created_at         | DATETIME     | 首次答题时间                                                | NOT NULL              |
| **user_answer_details**   | id                 | VARCHAR(36)  | 答案记录ID                                                  | PRIMARY KEY           |
|                           | record_id          | VARCHAR(36)  | 关联的答题记录ID                                            | FOREIGN KEY, NOT NULL |
|                           | question_item_id   | VARCHAR(36)  | 小题ID                                                      | FOREIGN KEY, NOT NULL |
|                           | user_answer        | TEXT         | 用户答案                                                    | NOT NULL              |
|                           | is_correct         | BOOLEAN      | 是否正确                                                    | NOT NULL              |
|                           | time_spent         | INT          | 答题耗时（秒）                                              |                       |
|                           | answered_at        | DATETIME     | 答题时间                                                    | NOT NULL              |
| **courses**               | id                 | VARCHAR(36)  | 课程唯一标识                                                | PRIMARY KEY           |
|                           | name               | VARCHAR(255) | 课程名称                                                    | NOT NULL              |
|                           | description        | TEXT         | 课程描述                                                    |                       |
|                           | chapters           | JSON         | 章节列表数组                                                |                       |
|                           | created_at         | DATETIME     | 创建时间                                                    | NOT NULL              |
| **user_vocabulary**       | id                 | VARCHAR(36)  | 生词记录ID                                                  | PRIMARY KEY           |
|                           | user_id            | VARCHAR(36)  | 用户ID                                                      | FOREIGN KEY, NOT NULL |
|                           | word               | VARCHAR(100) | 单词                                                        | NOT NULL              |
|                           | translation        | VARCHAR(255) | 翻译释义                                                    |                       |
|                           | source_question_id | VARCHAR(36)  | 来源题目ID                                                  | FOREIGN KEY           |
|                           | mastered           | BOOLEAN      | 是否已掌握                                                  | DEFAULT FALSE         |
|                           | created_at         | DATETIME     | 添加时间                                                    | NOT NULL              |
| **question_plans**        | id                 | VARCHAR(36)  | 计划项ID                                                    | PRIMARY KEY           |
|                           | user_id            | VARCHAR(36)  | 用户ID                                                      | FOREIGN KEY, NOT NULL |
|                           | question_id        | VARCHAR(36)  | 题目ID                                                      | FOREIGN KEY, NOT NULL |
|                           | plan_date          | DATE         | 计划日期                                                    | NOT NULL              |
|                           | note               | VARCHAR(255) | 备注                                                        |                       |
|                           | completed          | BOOLEAN      | 是否完成                                                    | DEFAULT FALSE         |
|                           | created_at         | DATETIME     | 创建时间                                                    | NOT NULL              |

### 数据表补充说明

1. **主键设计**：所有表采用 UUID（VARCHAR(36)）作为主键，避免自增 ID 带来的分布式问题，同时增强安全性（不易被猜测）。

2. **题目拆分设计**：将题目主体（questions）和小题（question_items）分离存储，支持一道大题包含多个小题的场景（如阅读理解一篇文章对应多道选择题）。

3. **用户答题记录**：`user_question_records` 表记录用户对每道题目的整体答题情况，`user_answer_details` 表记录每个小题的具体答案，便于错题分析和学习报告生成。

4. **JSON 字段**：`options`、`highlight_words`、`tags`、`chapters` 等字段使用 JSON 类型存储数组数据，简化数据结构，MySQL 5.7+ 和 PostgreSQL 均原生支持 JSON 类型及其查询。

5. **索引建议**：
   - `questions` 表建议在 `type`、`difficulty`、`related_course_id` 字段上建立索引
   - `user_question_records` 表建议在 `(user_id, question_id)` 上建立联合唯一索引
   - `user_vocabulary` 表建议在 `(user_id, word)` 上建立联合唯一索引

6. **软删除**：生产环境建议为重要表添加 `deleted_at` 字段实现软删除，保留历史数据用于分析。

7. **关联关系**：
   - questions → courses：多对一（一道题关联一个课程）
   - questions → question_items：一对多（一道大题包含多个小题）
   - users → user_question_records → questions：多对多关系通过中间表实现
   - users → user_vocabulary：一对多（一个用户有多个生词）
   - users → question_plans → questions：多对多关系实现学习计划功能

---

## 三、功能模块关系图

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端题库界面                               │
├─────────────────────────────────────────────────────────────────┤
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐    │
│  │ 筛选侧边栏 │  │ 题目列表  │  │ 练习模态框│  │  错题本   │    │
│  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘    │
└────────┼──────────────┼──────────────┼──────────────┼──────────┘
         │              │              │              │
         ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         API 接口层                               │
├─────────────────────────────────────────────────────────────────┤
│  GET /questions  POST /submit  GET /wrong  POST /favorite       │
└─────────────────────────────────────────────────────────────────┘
         │              │              │              │
         ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         数据库层                                 │
├─────────────────────────────────────────────────────────────────┤
│  questions | question_items | user_question_records | courses   │
│  user_vocabulary | user_answer_details | question_plans         │
└─────────────────────────────────────────────────────────────────┘
```

---

_文档生成日期：2025年11月26日_
