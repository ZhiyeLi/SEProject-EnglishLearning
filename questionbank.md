# 题库系统后端 API 需求文档

## 概述

本文档定义了题库系统重构后前端所需的所有后端 API 接口。系统支持两种做题模式：**考试模式**（整套试卷）和**单题模式**（单个篇章/大题）。

## 数据库表说明

相关数据库结构见：`backend/english_learning_platform/src/main/resources/questionbank_init.sql`

主要表：

- `exam_papers` - 试卷表
- `questions` - 题目主表（篇章/大题）
- `question_sub_items` - 小题表
- `user_favorites` - 用户收藏表
- `user_exam_records` - 用户考试记录表
- `user_answer_details` - 用户答题详情表

---

## 一、主界面 API

### 1.1 获取列表（考试模式或单题模式）

**接口**: `GET /api/questionbank/list`

**功能**: 根据模式返回试卷列表或题目列表，支持筛选、搜索、排序和分页

**请求参数**:

```json
{
  "mode": "exam", // 必填，模式: "exam"(考试模式) 或 "single"(单题模式)
  "page": 1, // 必填，页码
  "pageSize": 10, // 必填，每页数量
  "sortBy": "year_desc", // 必填，排序: "year_desc", "year_asc", "created_desc", "created_asc"
  "keyword": "2023", // 可选，搜索关键字（模糊搜索试卷名或题目标题）
  "category": "CET4", // 可选，考试类型: "all", "CET4", "CET6", "KY", "IELTS", "TOEFL"
  "sectionType": "reading", // 可选，题型（仅单题模式）: "all", "listening", "reading", "writing", "speaking"
  "containsSectionType": ["listening", "reading"], // 可选，包含的题型（仅考试模式）
  "status": "not_done" // 可选，状态: "all", "not_done", "done", "favorited"
}
```

**响应数据** (考试模式):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "name": "2023年6月大学英语四级真题（第一套）",
        "category": "CET4", // CET4, CET6, KY, IELTS, TOEFL
        "year": 2023,
        "difficulty": 3, // 1-5
        "status": "not_done", // not_done, done, ongoing
        "isFavorited": false, // 是否已收藏
        "created_at": "2024-01-15T10:30:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 10
  }
}
```

**响应数据** (单题模式):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "title": "Climate Change and Global Warming",
        "section_type": "reading", // listening, reading, writing, speaking
        "section_name": "Section A - Reading Comprehension",
        "paper_id": 1,
        "paper_name": "2023年6月大学英语四级真题（第一套）",
        "paper_category": "CET4",
        "status": "done", // not_done, done
        "isFavorited": true,
        "created_at": "2024-01-15T10:30:00"
      }
    ],
    "total": 120,
    "page": 1,
    "pageSize": 10
  }
}
```

**逻辑说明**:

- 考试模式：查询 `exam_papers` 表
- 单题模式：查询 `questions` 表（以篇章/大题为单位）
- 状态筛选：
  - `not_done/done`: 关联查询 `user_exam_records` 表，检查是否存在记录
  - `favorited`: 关联查询 `user_favorites` 表
- 考试模式包含题型筛选：需要 JOIN `questions` 表，筛选出包含指定题型的试卷

---

### 1.2 获取今日统计

**接口**: `GET /api/questionbank/stats/today`

**功能**: 获取当前用户今日做题数量和正确率

**请求参数**: 无（从 token 获取 user_id）

**响应数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 25, // 今日做题数（小题数量）
    "accuracy": 82 // 今日正确率（百分比）
  }
}
```

**逻辑说明**:

- 直接统计 `user_answer_details` 表
- 使用 `idx_user_stats` 索引: (user_id, is_correct, created_at)
- 筛选条件：`user_id = ? AND DATE(created_at) = CURDATE()`
- 计算：`count = COUNT(*)`, `accuracy = AVG(is_correct) * 100`

---

### 1.3 添加收藏

**接口**: `POST /api/questionbank/favorite`

**功能**: 添加试卷或题目到收藏

**请求数据**:

```json
{
  "type": "paper", // "paper"(试卷) 或 "question"(题目)
  "id": 1 // 试卷ID或题目ID
}
```

**响应数据**:

```json
{
  "code": 200,
  "message": "收藏成功"
}
```

**逻辑说明**:

- 插入 `user_favorites` 表
- 使用 UNIQUE 约束防止重复收藏
- type="paper" 时收藏的是 exam_papers.id
- type="question" 时收藏的是 questions.id

---

### 1.4 取消收藏

**接口**: `DELETE /api/questionbank/favorite`

**功能**: 取消收藏

**请求数据**:

```json
{
  "type": "question",
  "id": 5
}
```

**响应数据**:

```json
{
  "code": 200,
  "message": "取消收藏成功"
}
```

---

## 二、做题窗口 API

### 2.1 获取试卷详情（考试模式）

**接口**: `GET /api/questionbank/exam/{paperId}`

**功能**: 获取试卷的所有题目和小题数据

**路径参数**:

- `paperId`: 试卷ID

**响应数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "paper": {
      "id": 1,
      "name": "2023年6月大学英语四级真题（第一套）",
      "category": "CET4",
      "year": 2023,
      "difficulty": 3
    },
    "questions": [
      {
        "id": 1,
        "paper_id": 1,
        "section_type": "listening",
        "section_name": "Section A - Short Conversations",
        "title": "Listening Comprehension",
        "material_text": null,
        "material_image": null,
        "audio_url": "http://localhost:8080/api/files/audio/cet4_2023_06_listening.mp3",
        "audio_start_sec": 0,
        "audio_end_sec": 120,
        "sort_order": 1
      },
      {
        "id": 2,
        "paper_id": 1,
        "section_type": "reading",
        "section_name": "Section B - Reading Comprehension",
        "title": "Climate Change",
        "material_text": "The Earth's climate is changing...",
        "material_image": "http://localhost:8080/api/files/images/climate_chart.png",
        "audio_url": null,
        "audio_start_sec": 0,
        "audio_end_sec": 0,
        "sort_order": 2
      }
    ],
    "subItems": [
      {
        "id": 1,
        "parent_question_id": 1,
        "content": "What does the man suggest?",
        "item_type": "choice", // choice, multiple, text, blank, insert
        "options": [
          { "key": "A", "value": "Going to the library" },
          { "key": "B", "value": "Studying at home" },
          { "key": "C", "value": "Taking a break" },
          { "key": "D", "value": "Asking for help" }
        ],
        "answer": ["A"], // 正确答案（仅用于前端参考，不直接返回）
        "explanation": "根据对话中...",
        "score_value": 1.0,
        "sort_order": 1
      },
      {
        "id": 15,
        "parent_question_id": 2,
        "content": "What is the main idea of the passage?",
        "item_type": "choice",
        "options": [
          { "key": "A", "value": "Climate change is not real" },
          { "key": "B", "value": "Human activities cause climate change" },
          { "key": "C", "value": "Climate change is natural" },
          { "key": "D", "value": "Nothing can stop climate change" }
        ],
        "answer": ["B"],
        "explanation": null,
        "score_value": 2.0,
        "sort_order": 1
      }
    ],
    "isFavorited": false
  }
}
```

**字段说明**:

- `questions`: 按 `sort_order` 排序的所有大题
- `subItems`: 所有小题（前端根据 `parent_question_id` 分组）
- `options`: JSON 数组格式 `[{"key":"A","value":"..."},...]`
- `answer`: JSON 数组格式（不要直接发送给前端，仅在提交后校验用）
- `item_type`:
  - `choice`: 单选题
  - `multiple`: 多选题
  - `text`: 简答题
  - `blank`: 填空题
  - `insert`: 插入题（托福特有）

**逻辑说明**:

- 创建一条 `user_exam_records` 记录: `mode='full_paper'`, `paper_id=?`, `status='ongoing'`
- 返回该试卷的所有 questions 和 sub_items

---

### 2.2 获取题目详情（单题模式）

**接口**: `GET /api/questionbank/question/{questionId}`

**功能**: 获取单个题目（篇章/大题）及其小题数据

**路径参数**:

- `questionId`: 题目ID (questions.id)

**响应数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "question": {
      "id": 2,
      "paper_id": 1,
      "section_type": "reading",
      "section_name": "Section B - Reading Comprehension",
      "title": "Climate Change",
      "material_text": "The Earth's climate is changing...",
      "material_image": "http://localhost:8080/api/files/images/climate_chart.png",
      "audio_url": null,
      "audio_start_sec": 0,
      "audio_end_sec": 0,
      "category": "CET4", // 从 paper 表获取
      "paper_name": "2023年6月大学英语四级真题（第一套）"
    },
    "subItems": [
      {
        "id": 15,
        "parent_question_id": 2,
        "content": "What is the main idea?",
        "item_type": "choice",
        "options": [
          { "key": "A", "value": "Option A" },
          { "key": "B", "value": "Option B" },
          { "key": "C", "value": "Option C" },
          { "key": "D", "value": "Option D" }
        ],
        "answer": ["B"],
        "explanation": "解析内容...",
        "score_value": 2.0,
        "sort_order": 1
      }
    ],
    "isFavorited": true
  }
}
```

**逻辑说明**:

- 创建一条 `user_exam_records` 记录: `mode='single_part'`, `paper_id=?`, `target_question_id=?`, `status='ongoing'`
- 返回该 question 及其所有 sub_items

---

### 2.3 提交答案

**接口**: `POST /api/questionbank/submit`

**功能**: 提交答案并计算得分

**请求数据**:

```json
{
  "mode": "exam", // "exam" 或 "single"
  "paperId": 1, // 试卷ID（考试模式必填）
  "questionId": 2, // 题目ID（单题模式必填）
  "answers": [
    {
      "subItemId": 1,
      "answer": "A" // 单选: "A", 多选: ["A","C"], 文本: "答案内容"
    },
    {
      "subItemId": 15,
      "answer": "B"
    },
    {
      "subItemId": 20,
      "answer": ["A", "C"] // 多选题
    },
    {
      "subItemId": 25,
      "answer": "This is my essay..." // 写作题
    }
  ]
}
```

**响应数据**:

```json
{
  "code": 200,
  "message": "提交成功",
  "data": {
    "recordId": 123, // user_exam_records.id
    "score": 85.5, // 总得分
    "accuracy": 82, // 正确率（百分比）
    "totalQuestions": 30, // 总题数
    "correctCount": 25, // 正确数
    "details": [
      {
        "subItemId": 1,
        "userAnswer": "A",
        "correctAnswer": ["A"],
        "isCorrect": true,
        "scoreObtained": 1.0,
        "explanation": "解析内容..."
      },
      {
        "subItemId": 15,
        "userAnswer": "B",
        "correctAnswer": ["B"],
        "isCorrect": true,
        "scoreObtained": 2.0,
        "explanation": null
      }
    ]
  }
}
```

**逻辑说明**:

1. 查找对应的 `user_exam_records` 记录（根据 user_id, paper_id, mode, target_question_id）
2. 遍历 answers，对比 question_sub_items.answer:
   - 单选/多选：直接对比（忽略大小写和顺序）
   - 文本/填空：可使用相似度算法或人工评分（初版可标记为待评分）
3. 将每道题的答题详情插入 `user_answer_details` 表:
   - `record_id`: user_exam_records.id
   - `sub_item_id`: 小题ID
   - `user_id`: 当前用户ID（冗余字段，便于统计）
   - `user_content`: 用户答案（JSON 字符串）
   - `is_correct`: 是否正确（0/1）
   - `score_obtained`: 得分
4. 更新 `user_exam_records`:
   - `status='completed'`
   - `total_score=SUM(score_obtained)`
   - `completed_at=NOW()`
5. 返回得分和详情

**注意事项**:

- 写作题 (`item_type='text'`) 暂时可标记 `is_correct=0`，`score_obtained=0`，待人工评分
- 插入题需要前端发送选中的位置索引

---

## 三、错题本 API

### 3.1 获取错题列表

**接口**: `GET /api/questionbank/wrong`

**功能**: 获取当前用户的错题列表

**请求参数**:

```json
{
  "category": "CET4", // 可选，筛选类型
  "sectionType": "reading", // 可选，筛选题型
  "sortBy": "recent" // 排序: "recent"(最近错题), "frequency"(错误次数)
}
```

**响应数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "stats": {
      "total": 45, // 总错题数
      "recent": 8, // 近7天新增
      "accuracy": 68 // 整体正确率
    },
    "items": [
      {
        "id": 123, // user_answer_details.id
        "question_id": 2, // questions.id
        "paper_name": "2023年6月大学英语四级真题（第一套）",
        "section_name": "Section B - Reading Comprehension",
        "section_type": "reading",
        "category": "CET4",
        "preview": "What is the main idea...", // 题目预览（content 前50字）
        "wrong_count": 2, // 错误次数
        "last_wrong_date": "2024-01-20T15:30:00"
      }
    ],
    "total": 45
  }
}
```

**逻辑说明**:

- 查询 `user_answer_details` 表，条件：`user_id=? AND is_correct=0`
- JOIN `question_sub_items` 获取 `parent_question_id`
- JOIN `questions` 获取题目信息
- JOIN `exam_papers` 获取试卷信息
- 统计每个 `parent_question_id` 的错误次数（GROUP BY）
- 按要求排序（最近错题或错误次数）

---

## 四、数据库索引建议

为提升性能，建议添加以下索引（部分已在 schema 中定义）：

```sql
-- user_exam_records 表
CREATE INDEX idx_user_paper ON user_exam_records(user_id, paper_id);
CREATE INDEX idx_user_mode ON user_exam_records(user_id, mode);

-- user_answer_details 表
CREATE INDEX idx_user_stats ON user_answer_details(user_id, is_correct, created_at);

-- user_favorites 表
CREATE UNIQUE INDEX uk_user_question ON user_favorites(user_id, question_id);

-- questions 表
CREATE INDEX idx_paper_section ON questions(paper_id, section_type);
```

---

## 五、前端期望字段总结

### 试卷列表项字段

```
id, name, category, year, difficulty, status, isFavorited, created_at
```

### 题目列表项字段

```
id, title, section_type, section_name, paper_id, paper_name, paper_category,
status, isFavorited, created_at
```

### 试卷详情字段

```
paper: { id, name, category, year, difficulty }
questions: [{ id, paper_id, section_type, section_name, title, material_text,
             material_image, audio_url, audio_start_sec, audio_end_sec, sort_order }]
subItems: [{ id, parent_question_id, content, item_type, options, answer,
            explanation, score_value, sort_order }]
isFavorited: boolean
```

### 题目详情字段

```
question: { id, paper_id, section_type, section_name, title, material_text,
           material_image, audio_url, audio_start_sec, audio_end_sec, category, paper_name }
subItems: [{ ... 同上 }]
isFavorited: boolean
```

### 错题列表项字段

```
id, question_id, paper_name, section_name, section_type, category,
preview, wrong_count, last_wrong_date
```

---

## 六、特殊说明

### 6.1 JSON 字段处理

- `options`: 存储为 JSON 字符串，格式 `[{"key":"A","value":"..."},...]`
- `answer`: 存储为 JSON 数组，如 `["A"]` 或 `["A","C"]`

后端返回时可直接返回 JSON 对象，Spring Boot 会自动序列化。

### 6.2 音频片段处理

- 听力题的音频使用 `audio_start_sec` 和 `audio_end_sec` 标记片段
- 前端使用 HTML5 Audio API 跳转到指定时间并在结束时暂停

### 6.3 插入题处理

- `material_text` 中的 `■` 符号标记插入点
- 前端将其替换为可点击的交互元素
- 用户选择位置后，前端发送位置索引给后端

### 6.4 写作题评分

- 初版可暂时标记为 0 分，待后续人工评分或接入 AI 评分
- 或直接返回满分，仅用于练习统计

---

## 七、文件资源管理

音频和图片文件通过文件上传 API 管理：

- 上传音频：`POST /api/files/audio`
- 上传图片：`POST /api/files/images`
- 访问路径：`http://localhost:8080/api/files/audio/xxx.mp3`

详见文件上传相关配置（WebConfig.java, FileService.java）。
