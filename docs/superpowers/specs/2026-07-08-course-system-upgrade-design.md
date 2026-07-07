# 课程学习系统升级 — 设计文档

> 日期：2026-07-08
> 状态：已确认
> 关联：[[RAG-AI助教-个人项目汇报]]

---

## 一、目标

将课程页面从"硬编码静态视频链接列表"升级为完整的课程学习系统，包含：后端 API 打通、B 站视频内嵌播放、学习进度半自动追踪、课程收藏。

---

## 二、架构决策

- **后端**：Java Spring Boot（`backend/`），复用现有 Security + JPA 体系
- **前端**：Vue 3 + Element Plus，基于现有项目结构，`src/api/course.js` 封装 API
- **数据流**：前端 → `/api/courses/*` → CourseController → CourseService → MySQL

---

## 三、数据库设计

### 3.1 扩展 `courses` 表

在现有表基础上新增字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| tag | VARCHAR(32) | 分类：primary / middle / college / none |
| cover_image | VARCHAR(255) | 封面图路径（本地或 URL） |
| video_url | VARCHAR(500) | B 站视频链接 |
| level | VARCHAR(16) | 难度：beginner / intermediate / advanced |

原有字段 `id`, `name`, `description`, `created_at` 保持不变。

### 3.2 新建 `user_course_progress` 表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 自增主键 |
| user_id | BIGINT FK → users | 用户 |
| course_id | BIGINT FK → courses | 课程 |
| status | VARCHAR(16) | not_started / learning / completed |
| last_accessed_at | DATETIME | 最后打开时间 |
| completed_at | DATETIME | 完成时间（可为空） |

联合唯一索引 `(user_id, course_id)`。

### 3.3 新建 `user_course_favorite` 表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 自增主键 |
| user_id | BIGINT FK → users | 用户 |
| course_id | BIGINT FK → courses | 课程 |
| created_at | DATETIME | 收藏时间 |

联合唯一索引 `(user_id, course_id)`，防重复收藏。

### 3.4 初始数据

通过 Java Seed 脚本（`src/main/resources/data.sql` 或独立 migration）写入 6 门现有课程数据。后续课程新增暂通过脚本维护，API 已预留管理端 CRUD 扩展空间。

---

## 四、API 设计

所有接口挂载在 `/api/courses`，需登录认证（`SecurityConfig` 中 `/api/courses/**` 走 `authenticated()`）。

### 课程查询

```
GET /api/courses?keyword=&tag=&page=&size=
```
返回分页课程列表。keyword 模糊匹配 title + description，tag 精确筛选。

```
GET /api/courses/{id}
```
返回单门课程详情（含当前用户的学习进度状态和收藏状态）。

### 学习进度

```
GET /api/courses/progress
```
返回当前用户所有课程进度列表。

```
PUT /api/courses/{id}/progress
```
Body: `{ "status": "learning" | "completed" }`
首次访问自动创建 `learning` 记录；已完成的课可以重新标记为 learning。

```
POST /api/courses/{id}/complete
```
快捷标记完成。与 `PUT .../progress {status: "completed"}` 等价。

### 收藏

```
POST /api/courses/{id}/favorite
```
Toggle：已收藏则取消，未收藏则收藏。返回当前收藏状态。

```
GET /api/courses/favorites
```
返回当前用户收藏的课程 ID 列表。

---

## 五、前端设计

### 5.1 文件结构

| 文件 | 职责 |
|---|---|
| `src/api/course.js` | 封装全部 `/api/courses/*` 请求 |
| `src/components/course/CourseCard.vue` | 课程卡片：封面、标题、标签、进度条、收藏按钮 |
| `src/components/course/CoursePlayerModal.vue` | 播放弹窗：B 站 iframe + 进度/收藏操作 |
| `src/components/common/SuggestionModal.vue` | **通用**学习建议弹窗（从 Course.vue / AiChat.vue / Chat.vue 抽离） |
| `src/views/Course.vue` | 课程列表页：数据加载、搜索筛选、卡片列表、弹窗控制 |

### 5.2 页面交互

1. 页面挂载 → 调用 `GET /api/courses` + `GET /api/courses/progress` + `GET /api/courses/favorites` → 渲染卡片
2. 点击卡片 → 打开 `CoursePlayerModal` → B 站 iframe 加载嵌入播放器（BV 号从 video_url 提取，拼接 `player.bilibili.com`）
3. 弹窗打开 → 自动调用 `PUT /api/courses/{id}/progress` 标记 `learning`
4. 关闭弹窗 → 弹出确认"已学完了吗？" → 用户确认则调用 `POST /api/courses/{id}/complete`
5. 点击 ⭐ → 调用 `POST /api/courses/{id}/favorite` → 即时更新 UI
6. 搜索 / 标签切换 → 前端过滤列表（课程总量小），高亮关键词
7. "我的收藏"筛选 → 仅展示已收藏课程

### 5.3 B 站嵌入

从 `video_url` 提取 BV 号（正则匹配 `BV[a-zA-Z0-9]+`），拼接：
```
https://player.bilibili.com/player.html?bvid={bv号}&page=1
```
iframe 尺寸自适应弹窗宽度，保持 16:9 比例。

### 5.4 进度状态展示

每张课程卡片左上角角标：
- 无角标 = 未开始
- 🟢 学习中（绿色圆点）
- ✅ 已完成（绿色对勾）

---

## 六、不在本次范围

- 管理后台（课程 CRUD 表单页面）
- 完整视频进度条（记录到第几分钟）
- 课程与 AI 助教的联动
- 课程评论/评分

以上功能 API 已预留扩展空间，后续可追加。

---

## 七、风险与注意事项

- `application.yml` 中 `ddl-auto: update` 会自动执行 ALTER TABLE，需确认生产环境不会误删数据；建议后续改为 `validate` + 手动 migration
- B 站 iframe 的跨域限制：postMessage 可获取基本播放事件但不稳定，不做深度依赖
- `CoursePlayerModal` 关闭时弹出的"是否完成"确认用 `ElMessageBox`，避免和页面弹窗层级冲突
