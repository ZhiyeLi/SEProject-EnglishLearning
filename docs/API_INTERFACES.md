# 单词打卡系统 - 后端接口规范文档

## 项目概述

本文档详细说明了单词打卡系统与后端服务的接口定义。系统包含两个主要页面：
1. **单词类型选择页面** - 用户选择要背诵的词汇类型
2. **单词打卡页面** - 用户进行单词打卡学习

---

## 第一部分：数据结构定义

### 1.1 词汇类型数据结构

```json
{
  "typeId": "string (唯一标识)",
  "name": "string (类型名称)",
  "description": "string (类型描述)",
  "totalWords": "number (该类型词汇总数)",
  "icon": "string (图标类名)"
}
```

**现有词汇类型列表：**
| 类型ID | 类型名称 | 描述 | 预期词汇数 |
|--------|--------|------|----------|
| elementary | 初级词汇 | 适合初学者 | 1000 |
| cet46 | 四六级词汇 | 大学英语四六级 | 1500 |
| postgraduate | 考研词汇 | 考研英语必备 | 2000 |
| toefl_ielts | 托福雅思词汇 | 出国考试必备 | 2500 |
| professional | 专业术语词汇 | 行业专业用语 | 800 |

### 1.2 单词数据结构

```json
{
  "id": "number (单词唯一ID)",
  "word": "string (英文单词)",
  "translation": "string (中文翻译)",
  "pronunciation": "string (音标)",
  "example": "string (例句)",
  "audioUrl": "string (音频URL，可选)",
  "imageUrl": "string (配图URL，可选)"
}
```

### 1.3 用户打卡进度数据结构

```json
{
  "typeId": "string (词汇类型ID)",
  "passedCount": "number (已打卡单词数)",
  "totalCount": "number (该类型总单词数)",
  "passedWords": "array<number> (已打卡单词ID列表)",
  "lastPassedDate": "string (最后打卡日期, YYYY-MM-DD格式)",
  "consecutiveDays": "number (连续打卡天数)"
}
```

### 1.4 打卡计划数据结构

```json
{
  "typeId": "string (词汇类型ID)",
  "wordsPerDay": "number (每天打卡个数, 1-100)",
  "remainingWords": "number (剩余未打卡单词数)",
  "daysNeeded": "number (完成所需天数)",
  "lastDayWords": "number (最后一天需打卡单词数)",
  "startDate": "string (计划开始日期, YYYY-MM-DD格式)",
  "status": "string (计划状态: active/paused/completed)"
}
```

---

## 第二部分：核心接口定义

### 2.1 获取词汇类型列表

**端点:** `GET /api/words/types`

**描述:** 获取所有可选的词汇类型列表

**请求参数:** 无

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "types": [
      {
        "typeId": "elementary",
        "name": "初级词汇",
        "description": "适合初学者",
        "totalWords": 1000,
        "icon": "fa-leaf"
      },
      // ... 其他类型
    ]
  }
}
```

**错误响应:**
```json
{
  "code": 500,
  "message": "服务器错误"
}
```

---

### 2.2 获取词汇类型详情及单词列表

**端点:** `GET /api/words/types/{typeId}/words`

**描述:** 获取指定词汇类型的所有单词

**路径参数:**
- `typeId` (string, required): 词汇类型ID

**查询参数:**
- `page` (number, optional): 分页页码，默认为1
- `pageSize` (number, optional): 每页条数，默认为100，最大1000

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "typeId": "elementary",
    "typeName": "初级词汇",
    "totalWords": 1000,
    "page": 1,
    "pageSize": 100,
    "words": [
      {
        "id": 1,
        "word": "apple",
        "translation": "苹果",
        "pronunciation": "/ˈæpl/",
        "example": "An apple a day keeps the doctor away.",
        "audioUrl": "https://example.com/audio/apple.mp3",
        "imageUrl": "https://example.com/images/apple.jpg"
      },
      // ... 其他单词
    ]
  }
}
```

---

### 2.3 获取用户单词打卡进度

**端点:** `GET /api/user/word-progress/{typeId}`

**描述:** 获取用户在指定词汇类型上的打卡进度

**请求头:** 
- `Authorization: Bearer {token}` (required)

**路径参数:**
- `typeId` (string, required): 词汇类型ID

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "typeId": "elementary",
    "passedCount": 250,
    "totalCount": 1000,
    "passedWords": [1, 2, 5, 8, 10, ...],
    "lastPassedDate": "2025-11-21",
    "consecutiveDays": 15
  }
}
```

---

### 2.4 获取所有词汇类型的用户进度

**端点:** `GET /api/user/word-progress`

**描述:** 一次性获取用户在所有词汇类型上的打卡进度

**请求头:** 
- `Authorization: Bearer {token}` (required)

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "progress": [
      {
        "typeId": "elementary",
        "passedCount": 250,
        "totalCount": 1000,
        "passedWords": [1, 2, 5, 8, ...],
        "lastPassedDate": "2025-11-21",
        "consecutiveDays": 15
      },
      // ... 其他类型进度
    ]
  }
}
```

---

### 2.5 标记单词为已打卡

**端点:** `POST /api/user/words/mark-passed`

**描述:** 标记一个或多个单词为已打卡

**请求头:** 
- `Authorization: Bearer {token}` (required)
- `Content-Type: application/json`

**请求体:**
```json
{
  "typeId": "string (词汇类型ID)",
  "wordIds": "array<number> (单词ID列表)",
  "timestamp": "string (打卡时间戳, ISO8601格式, 可选)"
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "打卡成功",
  "data": {
    "typeId": "elementary",
    "markedWords": [1, 2, 5, 8],
    "passedCount": 254,
    "totalCount": 1000,
    "progressPercentage": 25.4
  }
}
```

**错误响应:**
```json
{
  "code": 400,
  "message": "无效的单词ID或词汇类型"
}
```

---

### 2.6 取消单词打卡标记

**端点:** `POST /api/user/words/unmark-passed`

**描述:** 取消单词的打卡标记

**请求头:** 
- `Authorization: Bearer {token}` (required)
- `Content-Type: application/json`

**请求体:**
```json
{
  "typeId": "string (词汇类型ID)",
  "wordIds": "array<number> (单词ID列表)"
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "取消打卡成功",
  "data": {
    "typeId": "elementary",
    "unmarkedWords": [1, 2],
    "passedCount": 252,
    "totalCount": 1000,
    "progressPercentage": 25.2
  }
}
```

---

### 2.7 创建/更新打卡计划

**端点:** `POST /api/user/word-plans`

**描述:** 创建或更新用户的打卡计划

**请求头:** 
- `Authorization: Bearer {token}` (required)
- `Content-Type: application/json`

**请求体:**
```json
{
  "typeId": "string (词汇类型ID)",
  "wordsPerDay": "number (每天打卡个数, 1-100)"
}
```

**业务逻辑:**
1. 获取该类型的总词汇数
2. 获取用户已打卡的单词数
3. 计算剩余单词数：remainingWords = totalWords - passedCount
4. 计算需要的天数：daysNeeded = Math.ceil(remainingWords / wordsPerDay)
5. 计算最后一天的单词数：lastDayWords = remainingWords % wordsPerDay || wordsPerDay

**响应示例:**
```json
{
  "code": 200,
  "message": "计划创建成功",
  "data": {
    "planId": "string (计划ID)",
    "typeId": "elementary",
    "wordsPerDay": 20,
    "remainingWords": 750,
    "daysNeeded": 38,
    "lastDayWords": 10,
    "startDate": "2025-11-21",
    "status": "active"
  }
}
```

**错误响应:**
```json
{
  "code": 400,
  "message": "每日打卡数量必须在1-100之间"
}
```

---

### 2.8 暂停打卡计划

**端点:** `PUT /api/user/word-plans/{planId}/pause`

**描述:** 暂停指定的打卡计划

**请求头:** 
- `Authorization: Bearer {token}` (required)

**路径参数:**
- `planId` (string, required): 计划ID

**响应示例:**
```json
{
  "code": 200,
  "message": "计划已暂停",
  "data": {
    "planId": "string",
    "status": "paused",
    "pausedAt": "2025-11-21T14:30:00Z"
  }
}
```

---

### 2.9 恢复打卡计划

**端点:** `PUT /api/user/word-plans/{planId}/resume`

**描述:** 恢复已暂停的打卡计划

**请求头:** 
- `Authorization: Bearer {token}` (required)

**路径参数:**
- `planId` (string, required): 计划ID

**响应示例:**
```json
{
  "code": 200,
  "message": "计划已恢复",
  "data": {
    "planId": "string",
    "status": "active",
    "resumedAt": "2025-11-21T14:35:00Z"
  }
}
```

---

### 2.10 获取当前打卡计划

**端点:** `GET /api/user/word-plans/current`

**描述:** 获取用户当前活跃的打卡计划

**请求头:** 
- `Authorization: Bearer {token}` (required)

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "planId": "string",
    "typeId": "elementary",
    "wordsPerDay": 20,
    "remainingWords": 750,
    "daysNeeded": 38,
    "lastDayWords": 10,
    "startDate": "2025-11-21",
    "status": "active"
  }
}
```

---

### 2.11 批量获取单词详情

**端点:** `POST /api/words/batch-details`

**描述:** 根据单词ID列表批量获取单词详情（用于复习页面）

**请求头:**
- `Content-Type: application/json`

**请求体:**
```json
{
  "typeId": "string (词汇类型ID)",
  "wordIds": "array<number> (单词ID列表，最多200个)"
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "words": [
      {
        "id": 1,
        "word": "apple",
        "translation": "苹果",
        "pronunciation": "/ˈæpl/",
        "example": "An apple a day keeps the doctor away.",
        "audioUrl": "https://example.com/audio/apple.mp3",
        "imageUrl": "https://example.com/images/apple.jpg"
      },
      // ... 其他单词
    ]
  }
}
```

---

### 2.12 获取用户学习统计

**端点:** `GET /api/user/word-statistics`

**描述:** 获取用户的单词学习统计数据

**请求头:** 
- `Authorization: Bearer {token}` (required)

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalWordsPassed": 1250,
    "consecutiveDays": 15,
    "lastPassedDate": "2025-11-21",
    "typeStatistics": [
      {
        "typeId": "elementary",
        "typeName": "初级词汇",
        "passedCount": 250,
        "totalCount": 1000,
        "progressPercentage": 25
      },
      // ... 其他类型
    ]
  }
}
```

---

## 第三部分：错误处理规范

### 3.1 标准错误响应格式

所有错误响应应遵循以下格式：

```json
{
  "code": "number (HTTP状态码或自定义错误码)",
  "message": "string (人类可读的错误消息)",
  "errors": "object (可选，详细的字段级错误信息)"
}
```

### 3.2 常见错误码定义

| 错误码 | HTTP状态 | 消息 | 说明 |
|-------|---------|------|------|
| 200 | 200 | 请求成功 | 操作成功完成 |
| 400 | 400 | Bad Request | 请求参数有误 |
| 401 | 401 | Unauthorized | 认证失败，需要登录 |
| 403 | 403 | Forbidden | 无权限访问资源 |
| 404 | 404 | Not Found | 资源不存在 |
| 422 | 422 | Unprocessable Entity | 业务逻辑验证失败 |
| 500 | 500 | Internal Server Error | 服务器内部错误 |

### 3.3 特定错误示例

**无效词汇类型:**
```json
{
  "code": 404,
  "message": "词汇类型不存在",
  "errors": {
    "typeId": "无效的词汇类型ID: unknown_type"
  }
}
```

**打卡数量超出范围:**
```json
{
  "code": 422,
  "message": "每日打卡数量必须在1-100之间",
  "errors": {
    "wordsPerDay": "提交的值: 150, 允许范围: 1-100"
  }
}
```

---

## 第四部分：认证与安全

### 4.1 认证方式

使用 JWT Bearer Token 进行认证

**请求示例:**
```
GET /api/user/word-progress HTTP/1.1
Host: api.example.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4.2 令牌刷新

**端点:** `POST /api/auth/refresh-token`

**请求体:**
```json
{
  "refreshToken": "string"
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "令牌刷新成功",
  "data": {
    "accessToken": "string (新的访问令牌)",
    "refreshToken": "string (新的刷新令牌，可选)",
    "expiresIn": 3600
  }
}
```

---

## 第五部分：性能考虑

### 5.1 分页建议

- 对于大型列表（如单词列表），使用分页
- 默认每页100条记录，最多1000条
- 使用游标分页会更有效率

### 5.2 缓存策略

- 词汇类型列表：可缓存24小时
- 单词详情：可缓存7天
- 用户进度：不缓存（实时）
- 打卡计划：不缓存（实时）

### 5.3 速率限制

建议实施以下速率限制：
- 普通用户：100 请求/分钟
- 认证用户：1000 请求/小时
- 特殊端点（标记打卡）：50 请求/分钟

---

## 第六部分：前端集成指南

### 6.1 环境配置

```javascript
// .env 文件
VITE_API_BASE_URL=https://api.example.com
VITE_API_TIMEOUT=10000
```

### 6.2 API调用示例

```javascript
// 使用 axios 或 fetch API

// 获取词汇类型列表
async function getWordTypes() {
  const response = await fetch('/api/words/types');
  return response.json();
}

// 标记单词为已打卡
async function markWordsAsPassed(typeId, wordIds, token) {
  const response = await fetch('/api/user/words/mark-passed', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      typeId,
      wordIds
    })
  });
  return response.json();
}
```

### 6.3 错误处理

```javascript
// 统一错误处理
function handleApiError(error) {
  if (error.code === 401) {
    // 处理认证失败，跳转到登录页
    redirectToLogin();
  } else if (error.code === 422) {
    // 处理业务验证错误
    showValidationErrors(error.errors);
  } else {
    // 其他错误
    showErrorMessage(error.message);
  }
}
```

---

## 第七部分：后续功能扩展建议

### 7.1 待开发功能

1. **单词音频学习**
   - 集成文本转语音（TTS）服务
   - 提供发音练习功能

2. **单词联想记忆**
   - 根据词根、词缀的相关单词
   - 单词关联学习

3. **学习成就系统**
   - 勋章和成就解锁
   - 学习排行榜

4. **群组学习**
   - 邀请好友加入学习小组
   - 小组排行榜和挑战

5. **AI个性化推荐**
   - 根据学习进度推荐复习内容
   - 基于遗忘曲线的智能复习

6. **数据分析报告**
   - 学习进度图表
   - 周/月学习报告

---

## 文档版本

| 版本 | 日期 | 作者 | 更新内容 |
|-----|------|------|--------|
| 1.0 | 2025-11-21 | 系统 | 初始版本 |

---

## 附录：技术栈建议

### 后端技术栈建议

- **语言**: Node.js (TypeScript) / Python / Java / Go
- **框架**: Express / Flask / Spring Boot / Gin
- **数据库**: PostgreSQL / MySQL
- **缓存**: Redis
- **消息队列**: RabbitMQ / Kafka（用于异步任务）
- **认证**: JWT / OAuth 2.0
- **API文档**: Swagger/OpenAPI

### 部署建议

- **容器化**: Docker
- **编排**: Kubernetes / Docker Compose
- **CI/CD**: GitHub Actions / GitLab CI / Jenkins
- **监控**: Prometheus + Grafana
- **日志**: ELK Stack / Loki

---

## 联系方式

如有任何关于此API文档的问题，请联系技术团队。

**最后更新**: 2025年11月21日
