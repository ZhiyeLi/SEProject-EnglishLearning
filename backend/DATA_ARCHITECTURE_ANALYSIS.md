# 英语学习平台 - 后端数据交互架构分析

## 一、技术栈概览

### 框架和工具
- **框架**: Spring Boot 3.2.0（基于Spring Framework）
- **ORM**: Spring Data JPA + Hibernate
- **数据库**: MySQL
- **认证**: Spring Security + JWT
- **构建工具**: Maven
- **Java版本**: JPA 版本

### 数据库配置
```yaml
URL: jdbc:mysql://localhost:3306/english_learning
驱动: MySQL Connector/J (com.mysql.cj.jdbc.Driver)
字符集: utf8mb4_unicode_ci
连接池: HikariCP (最小连接数5，最大连接数20)
```

---

## 二、数据库表结构详析

### 1. **核心用户表**

#### `users` 表 - 用户信息
| 字段 | 类型 | 说明 |
|-----|------|------|
| user_id | BIGINT | 主键，自增 |
| user_name | VARCHAR(255) | 用户名，唯一 |
| user_password | VARCHAR(255) | 加密密码 |
| user_email | VARCHAR(255) | 邮箱，唯一 |
| avatar | VARCHAR(255) | 头像URL |
| user_status | VARCHAR(255) | 用户状态（如"沉迷学习"） |
| signature | VARCHAR(255) | 个性签名 |
| streak | BIGINT | 连续学习天数 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

**数据管理**:
- 密码使用 `PasswordEncoder` 加密存储
- 邮箱和用户名唯一约束
- 创建时自动设置时间戳

---

### 2. **单词学习相关表**

#### `word_types` 表 - 单词分类
| 字段 | 类型 | 说明 |
|-----|------|------|
| type_id | BIGINT | 主键，自增 |
| name | VARCHAR(255) | 分类名称（如"elementary", "cet46", "postgraduate"） |
| description | VARCHAR(500) | 分类描述 |
| total_words | BIGINT | 该分类中的总词汇数 |

**预置数据**:
```
1. elementary       - 适合初学者（1000词）
2. cet46            - 大学英语四六级（1500词）
3. postgraduate     - 考研英语必备（2000词）
4. toefl_ielts      - 出国考试必备（2500词）
5. professional     - 行业专业用语（800词）
```

#### `words` 表 - 单词库
| 字段 | 类型 | 说明 |
|-----|------|------|
| word_id | BIGINT | 主键，自增 |
| word | VARCHAR(255) | 单词内容 |
| part_of_speech | VARCHAR(255) | 词性（如noun, verb, adjective） |
| phonetic | VARCHAR(255) | 音标 |
| definition | VARCHAR(500) | 定义 |
| example | VARCHAR(500) | 例句 |
| type_id | BIGINT | 分类ID（外键关联word_types） |
| synonyms | VARCHAR(500) | 同义词 |
| antonyms | VARCHAR(500) | 反义词 |
| usage_notes | VARCHAR(500) | 使用说明 |
| audio_url | VARCHAR(255) | 音频URL |
| image_url | VARCHAR(255) | 图片URL |

#### `user_word_progress` 表 - 用户单词学习进度
| 字段 | 类型 | 说明 |
|-----|------|------|
| progress_id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID（外键） |
| word_id | BIGINT | 单词ID（外键） |
| type_id | BIGINT | 单词分类ID |
| stage | BIGINT | 学习阶段（0-5，5表示已掌握） |
| last_review_time | TIMESTAMP | 最后复习时间 |
| next_review_time | TIMESTAMP | 下次复习时间 |
| review_count | BIGINT | 复习次数 |
| passed_date | DATE | 掌握日期 |

**学习进度管理机制**:
- stage字段表示学习阶段，共6个阶段（0-5）
- stage=5 表示单词已掌握
- 支持间隔重复(Spaced Repetition)算法：记录复习时间和下次复习时间
- review_count 记录复习次数，可用于分析学习效果

#### `user_vocabulary` 表 - 用户生词本
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| word_id | BIGINT | 单词ID |
| translation | VARCHAR(255) | 自定义翻译 |
| source_question_id | VARCHAR(255) | 来源题目ID |
| if_mastered | TINYINT(1) | 是否已掌握（0/1） |
| created_at | TIMESTAMP | 创建时间 |

#### `daily_study_record` 表 - 每日学习记录
| 字段 | 类型 | 说明 |
|-----|------|------|
| record_id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| study_date | DATE | 学习日期 |
| new_words | BIGINT | 新学单词数 |
| review_words | BIGINT | 复习单词数 |
| total_words | BIGINT | 总学习单词数 |
| streak | BIGINT | 当日连续学习天数 |
| type_id | BIGINT | 单词分类ID |

**统计功能**:
- 按日期统计学习数据
- 记录新学和复习词汇数量
- 追踪连续学习状态（用于Streak功能）

---

### 3. **打卡计划表**

#### `checkin_plans` 表 - 单词学习打卡计划
| 字段 | 类型 | 说明 |
|-----|------|------|
| plan_id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| type_id | BIGINT | 单词分类ID |
| words_per_day | BIGINT | 每天学习单词数 |
| start_date | DATE | 计划开始日期 |
| status | VARCHAR(255) | 计划状态(active/paused/completed) |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

**管理机制**:
- 用户可创建多个计划，每个计划针对不同的单词分类
- status字段支持计划的生命周期管理

#### `user_selected_types` 表 - 用户选择的学习分类
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| type_id | BIGINT | 单词分类ID |
| selected_date | TIMESTAMP | 选择时间 |

---

### 4. **计划任务表**

#### `plans` 表 - 通用日程计划
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| date | DATE | 计划日期 |
| title | VARCHAR(255) | 计划标题 |
| description | VARCHAR(500) | 计划描述 |
| category | VARCHAR(255) | 分类（如"其他"） |
| priority | VARCHAR(255) | 优先级(medium/high/low) |
| start_time | VARCHAR(255) | 开始时间 |
| end_time | VARCHAR(255) | 结束时间 |
| if_completed | TINYINT(1) | 是否完成(0/1) |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

### 5. **题目练习相关表**

#### `courses` 表 - 课程信息
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键，自增 |
| name | VARCHAR(255) | 课程名称 |
| description | VARCHAR(500) | 课程描述 |
| created_at | TIMESTAMP | 创建时间 |

#### `questions` 表 - 题目库
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键，自增 |
| type | VARCHAR(255) | 题目类型(reading/listening/writing/speaking/vocabulary) |
| difficulty | VARCHAR(255) | 难度(easy/medium/hard) |
| title | VARCHAR(255) | 题目标题 |
| preview | VARCHAR(500) | 题目预览 |
| content | VARCHAR(500) | 题目内容 |
| audio_url | VARCHAR(255) | 音频URL（用于听力题） |
| tags | VARCHAR(500) | 标签 |
| related_course_id | VARCHAR(255) | 关联课程ID |
| related_chapter | VARCHAR(255) | 关联章节 |
| created_at | TIMESTAMP | 创建时间 |

#### `question_items` 表 - 题目选项详情
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键，自增 |
| question_id | VARCHAR(255) | 题目ID |
| question_text | VARCHAR(500) | 题目文本 |
| question_type | VARCHAR(255) | 题型（如选择题、填空题等） |
| options | VARCHAR(500) | 选项(JSON或逗号分隔) |
| answer | VARCHAR(255) | 正确答案 |
| explanation | VARCHAR(500) | 答案解析 |
| order_num | BIGINT | 题目序号 |

#### `user_question_records` 表 - 用户做题记录
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| question_id | VARCHAR(255) | 题目ID |
| status | VARCHAR(255) | 答题状态(not-done/done/correct/wrong) |
| last_result | VARCHAR(255) | 最后结果 |
| last_attempt_date | TIMESTAMP | 最后尝试时间 |
| correct_count | BIGINT | 正确次数 |
| wrong_count | BIGINT | 错误次数 |
| is_favorited | TINYINT(1) | 是否收藏(0/1) |

#### `user_answer_details` 表 - 用户答题详情
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键，自增 |
| record_id | VARCHAR(255) | 答题记录ID |
| question_item_id | VARCHAR(255) | 题目选项ID |
| user_answer | VARCHAR(500) | 用户答案 |
| is_correct | TINYINT(1) | 是否正确(0/1) |
| time_spent | BIGINT | 用时（秒） |
| answered_at | TIMESTAMP | 答题时间 |

---

### 6. **社交功能表**

#### `friends` 表 - 好友关系
| 字段 | 类型 | 说明 |
|-----|------|------|
| relation_id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| friend_id | BIGINT | 好友ID |
| created_at | TIMESTAMP | 添加时间 |

#### `friend_requests` 表 - 好友请求
| 字段 | 类型 | 说明 |
|-----|------|------|
| request_id | BIGINT | 主键，自增 |
| sender_id | BIGINT | 发送者ID |
| receiver_id | BIGINT | 接收者ID |
| status | VARCHAR(255) | 状态(pending/accepted/rejected) |
| created_at | TIMESTAMP | 发送时间 |

#### `messages` 表 - 用户消息
| 字段 | 类型 | 说明 |
|-----|------|------|
| message_id | BIGINT | 主键，自增 |
| sender_id | BIGINT | 发送者ID |
| receiver_id | BIGINT | 接收者ID |
| content | VARCHAR(500) | 消息内容 |
| if_read | TINYINT(1) | 是否已读(0/1) |
| sent_at | TIMESTAMP | 发送时间 |

---

## 三、数据交互流程

### 1. **用户认证流程** (AuthService)
```
用户注册 → 验证用户名/邮箱唯一性 → 密码加密 → 保存到users表 → 生成JWT Token

用户登录 → 查询users表 → 验证密码 → 生成JWT Token → 返回用户信息
```

**关键代码流程**:
```java
// 注册
1. 检查 users 表是否存在相同用户名/邮箱
2. 使用 PasswordEncoder 加密密码
3. 创建 User 对象并保存
4. 生成 JWT Token

// 登录
1. 从 users 表查询用户
2. 使用 PasswordEncoder.matches() 验证密码
3. 通过 JwtUtil 生成 Token
```

---

### 2. **单词学习流程** (WordService)
```
用户选择分类 → 查询word_types和words表
                ↓
用户开始学习 → 创建checkin_plan → 记录到daily_study_record
                ↓
用户学习单词 → 更新user_word_progress (stage, last_review_time, next_review_time)
                ↓
用户掌握单词 → passed_date置为当前日期, stage=5
                ↓
统计学习成果 → 计算streak, total_words
```

**相关表交互**:
- `word_types` → 用户选择学习分类
- `words` → 获取单词详情
- `checkin_plans` → 记录学习计划
- `user_word_progress` → 追踪学习进度（支持间隔重复）
- `daily_study_record` → 每日学习统计
- `user_vocabulary` → 用户生词本管理

---

### 3. **题目练习流程** (QuestionService)
```
用户浏览题库 → 查询questions表（按type/difficulty过滤）
                ↓
用户做题 → 创建user_question_records记录
                ↓
提交答题 → 逐个check答案 → 创建user_answer_details记录
                ↓
统计成绩 → 更新correct_count/wrong_count/status
                ↓
可选：收藏题目 → 更新is_favorited字段
```

**相关表交互**:
- `questions` → 题目库
- `question_items` → 题目详情和选项
- `courses` → 题目所属课程
- `user_question_records` → 用户做题记录
- `user_answer_details` → 答题详情（用于错题分析）
- `user_vocabulary` → 根据题目生词自动添加到生词本

---

### 4. **日程管理流程** (PlanService)
```
用户创建计划 → 保存到plans表（包含date, category, priority等）
                ↓
用户更新计划 → 更新plans表的if_completed, updated_at等
                ↓
用户查询计划 → 按date或category查询
```

**相关表交互**:
- `plans` → 通用日程计划存储

---

### 5. **社交互动流程** (FriendService)
```
用户搜索好友 → 查询users表，排除自己和已有好友
                ↓
发送好友请求 → 创建friend_requests记录(status='pending')
                ↓
接收好友请求 → 审批后创建friends记录
                ↓
发送消息 → 创建messages记录
                ↓
查询消息 → 按sender_id/receiver_id查询，支持标记已读
```

**相关表交互**:
- `users` → 用户搜索和基本信息
- `friend_requests` → 好友请求管理
- `friends` → 好友关系存储
- `messages` → 消息存储

---

## 四、数据访问层架构 (Repository)

### Spring Data JPA Repository 模式
所有表都对应一个 Repository 接口（继承 JpaRepository）：

```java
// 通用 CRUD 接口
JpaRepository<Entity, ID> 提供:
- save()           // 保存或更新
- findById()       // 主键查询
- findAll()        // 查询所有
- delete()         // 删除
- deleteById()     // 按主键删除

// 自定义查询方法
findByXxx()        // 按单个字段查询
findByXxxAndYyy()  // 多条件AND查询
findByXxxOrYyy()   // 多条件OR查询
existsByXxx()      // 存在性检查
```

### 主要Repository列表
| Repository | Entity | 主要功能 |
|-----------|--------|--------|
| UserRepository | User | 用户信息查询、身份验证 |
| WordRepository | Word | 单词库查询 |
| WordTypeRepository | WordType | 单词分类查询 |
| UserWordProgressRepository | UserWordProgress | 用户学习进度管理 |
| DailyStudyRecordRepository | DailyStudyRecord | 每日学习统计 |
| CheckinPlanRepository | CheckinPlan | 打卡计划管理 |
| QuestionRepository | Question | 题目查询 |
| QuestionItemRepository | QuestionItem | 题目详情查询 |
| UserQuestionRecordRepository | UserQuestionRecord | 用户答题记录 |
| UserAnswerDetailRepository | UserAnswerDetail | 答题详情 |
| FriendRepository | Friend | 好友关系管理 |
| FriendRequestRepository | FriendRequest | 好友请求管理 |
| MessageRepository | Message | 消息管理 |

---

## 五、Service 层业务逻辑

### AuthService - 认证服务
```java
功能:
- register(RegisterRequest)      // 用户注册
- login(LoginRequest)            // 用户登录
- getUserInfo(userId)            // 获取用户信息
- updateUserInfo(userId, User)   // 更新用户信息
- changePassword(userId, oldPwd, newPwd)  // 修改密码

关键实现:
- 使用 PasswordEncoder 进行密码加密/验证
- 使用 JwtUtil 生成/验证 Token
- 事务注解 @Transactional 保证数据一致性
```

### WordService - 单词学习服务
```java
功能:
- getWordTypes()                 // 获取所有单词分类
- getWordsByType(typeId, limit)  // 按分类获取单词
- getUserWordProgress(userId)    // 获取用户学习进度
- markWordPassed(userId, wordId, typeId)  // 标记单词已掌握
- unmarkWordPassed(userId, wordId)        // 取消掌握标记
- getTodayCheckInStatus(userId)  // 获取今日打卡状态
- updateDailyRecord(userId, typeId, newWords, reviewWords)

关键实现:
- 使用 Optional 进行空值检查
- 使用 LocalDate/LocalDateTime 处理时间
- 支持间隔重复算法（记录复习时间）
- 记录每日学习统计数据
```

### QuestionService - 题目练习服务
```java
功能:
- getQuestions(type, difficulty) // 题目查询和筛选
- getQuestionDetail(id, userId)  // 获取题目详情
- toggleFavorite(userId, questionId)  // 收藏/取消收藏
- submitAnswers(userId, answers) // 提交答卷
- getWrongQuestions(userId)      // 获取错题集

关键实现:
- 按type/difficulty多条件过滤
- 记录用户答题记录和答题详情
- 自动统计正确/错误次数
- 支持收藏功能
```

### FriendService - 社交服务
```java
功能:
- searchNewFriends(keyword, currentUserId)  // 搜索新好友
- sendFriendRequest(senderId, receiverId)   // 发送好友请求
- getFriendRequests(userId)                 // 获取待处理请求
- acceptFriendRequest(requestId, userId)    // 接受好友请求
- rejectFriendRequest(requestId, userId)    // 拒绝好友请求
- removeFriend(userId, friendId)            // 删除好友
- sendMessage(senderId, receiverId, content) // 发送消息
- getMessages(userId)                       // 获取消息

关键实现:
- 防止重复好友请求检查
- 排除已有好友和自己
- 支持消息已读标记
```

### PlanService - 计划管理服务
```java
功能:
- createPlan(userId, planData)   // 创建计划
- getPlan(userId)                // 查询计划
- updatePlan(planId, planData)   // 更新计划
- deletePlan(planId)             // 删除计划
- completePlan(planId)           // 标记完成

关键实现:
- 按日期和分类组织计划
- 支持优先级设置
- 记录创建和更新时间
```

---

## 六、数据流转关键特性

### 1. **时间戳管理** (@PrePersist, @PreUpdate)
```java
// 创建时自动设置
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

// 更新时自动更新
@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```
**作用**: 自动追踪数据的创建和修改时间

### 2. **事务管理** (@Transactional)
```java
@Transactional
public void importantOperation() {
    // 多个数据库操作
    // 如果任何一个失败，整个事务回滚
}
```
**作用**: 保证多表操作的原子性和一致性

### 3. **密码安全** (PasswordEncoder)
```java
// 注册时加密
user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));

// 登录时验证
passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())
```
**作用**: 用户密码不能以明文存储

### 4. **JWT Token 认证**
```
用户登录 → 生成 Token → 客户端保存
每次请求 → 在 Header 中携带 Token → 服务器验证
Token 包含: userId + userName + 签名 + 过期时间
```

### 5. **学习进度管理** (Spaced Repetition)
```
stage 字段: 0 → 1 → 2 → 3 → 4 → 5 (已掌握)
- last_review_time: 最后复习时间
- next_review_time: 根据遗忘曲线计算下次复习时间
- review_count: 复习次数
```

---

## 七、核心数据流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                     用户学习全流程                              │
└─────────────────────────────────────────────────────────────────┘

1. 注册登录
   ┌──────────────┐
   │   输入信息    │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐      ┌────────────────┐
   │ AuthService  │─────▶│  users 表       │
   └──────┬───────┘      └────────────────┘
          │
          ▼
   ┌──────────────┐
   │  生成 Token  │
   └──────────────┘

2. 选择学习计划
   ┌──────────────┐      ┌────────────────┐
   │ 浏览分类      │─────▶│ word_types 表  │
   └──────┬───────┘      └────────────────┘
          │
          ▼
   ┌──────────────┐      ┌────────────────┐
   │ 创建打卡计划  │─────▶│ checkin_plans  │
   └──────────────┘      └────────────────┘

3. 学习单词
   ┌──────────────┐      ┌────────────────┐
   │ 获取单词      │─────▶│   words 表     │
   └──────┬───────┘      └────────────────┘
          │
          ▼
   ┌──────────────┐
   │ 学习单词      │
   └──────┬───────┘
          │
          ▼
   ┌──────────────────────┐      ┌─────────────────────┐
   │ 更新学习进度          │─────▶│ user_word_progress  │
   │ (stage/复习时间等)    │      └─────────────────────┘
   └──────┬───────────────┘
          │
          ▼
   ┌──────────────────────┐      ┌────────────────────┐
   │ 掌握单词              │─────▶│ passed_date = 今日  │
   │ (stage = 5)          │      └────────────────────┘
   └──────┬───────────────┘
          │
          ▼
   ┌──────────────────────┐      ┌────────────────────┐
   │ 记录每日学习成果      │─────▶│ daily_study_record │
   └──────────────────────┘      └────────────────────┘

4. 做题练习
   ┌──────────────┐      ┌────────────────┐
   │ 浏览题库      │─────▶│  questions 表  │
   └──────┬───────┘      └────────────────┘
          │
          ▼
   ┌──────────────┐      ┌────────────────┐
   │ 获取题目详情  │─────▶│ question_items │
   └──────┬───────┘      └────────────────┘
          │
          ▼
   ┌──────────────┐
   │ 做题          │
   └──────┬───────┘
          │
          ▼
   ┌──────────────────────┐      ┌─────────────────────┐
   │ 提交答卷              │─────▶│ user_question_records
   └──────┬───────────────┘      └─────────────────────┘
          │
          ▼
   ┌──────────────────────┐      ┌──────────────────────┐
   │ 记录答题详情          │─────▶│ user_answer_details  │
   │ (答案/用时/正确性)    │      └──────────────────────┘
   └──────────────────────┘

5. 社交互动
   ┌──────────────┐      ┌────────────────┐
   │ 搜索好友      │─────▶│  users 表      │
   └──────┬───────┘      └────────────────┘
          │
          ▼
   ┌──────────────┐      ┌────────────────┐
   │ 发送好友请求  │─────▶│ friend_requests│
   └──────┬───────┘      └────────────────┘
          │
          ▼
   ┌──────────────┐      ┌────────────────┐
   │ 审批请求      │─────▶│   friends 表   │
   └──────┬───────┘      └────────────────┘
          │
          ▼
   ┌──────────────┐      ┌────────────────┐
   │ 发送消息      │─────▶│  messages 表   │
   └──────────────┘      └────────────────┘
```

---

## 八、数据库设计亮点

### 1. **灵活的学习进度追踪**
- 使用 stage 字段实现多阶段学习
- 支持间隔重复的复习安排
- 记录掌握日期用于成就统计

### 2. **完整的学习数据分析**
- daily_study_record 支持按日期统计
- user_word_progress 记录复习次数和最后复习时间
- user_answer_details 记录答题用时，支持性能分析

### 3. **灵活的分类和组织**
- word_types 支持多种单词分类
- checkin_plans 支持按分类创建多个学习计划
- questions 支持多维度查询（type/difficulty/course）

### 4. **完善的社交功能**
- friend_requests 支持请求审批流程
- messages 支持已读标记
- 搜索功能排除重复关系

### 5. **安全的用户认证**
- 密码加密存储
- JWT Token 无状态认证
- 唯一约束防止重复注册

---

## 九、总结

该英语学习平台的后端采用 **Spring Boot + JPA + MySQL** 的现代Java技术栈，核心特点：

| 方面 | 特点 |
|------|------|
| **数据结构** | 17张表，覆盖用户、单词、题目、计划、社交全业务链条 |
| **数据管理** | 使用JPA的Repository模式，简化数据访问，支持事务管理 |
| **学习追踪** | 间隔重复算法支持，多维度学习数据统计 |
| **安全性** | 密码加密，JWT认证，事务一致性保障 |
| **扩展性** | 模块化服务层设计，易于功能扩展 |
| **时间管理** | 自动时间戳，完整的创建和更新时间追踪 |

数据流转遵循**单向依赖**原则：Controller → Service → Repository → Database，实现了清晰的分层架构。
