const { dbRun } = require("../config/database");

/**
 * 初始化数据库表结构
 */
async function initDatabase() {
  console.log("🚀 开始初始化数据库...");

  try {
    // 1. 用户表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS users (
        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_name VARCHAR(50) NOT NULL,
        user_password VARCHAR(255) NOT NULL,
        user_email VARCHAR(70) UNIQUE NOT NULL,
        avatar VARCHAR(255) DEFAULT 'https://picsum.photos/seed/default/100/100',
        user_status VARCHAR(20) DEFAULT '沉迷学习',
        signature VARCHAR(200) DEFAULT '这个人很懒，什么都没写',
        streak INTEGER DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )
    `);
    console.log("✅ users 表创建成功");

    // 2. 学习计划表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS plans (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        date DATE NOT NULL,
        title VARCHAR(255) NOT NULL,
        description TEXT,
        category VARCHAR(50) NOT NULL DEFAULT '其他',
        priority VARCHAR(10) NOT NULL DEFAULT 'medium',
        start_time TIME,
        end_time TIME,
        if_completed BOOLEAN NOT NULL DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
        UNIQUE(user_id, date, title)
      )
    `);
    console.log("✅ plans 表创建成功");

    // 3. 好友关系表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS friends (
        relation_id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        friend_id INTEGER NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
        FOREIGN KEY (friend_id) REFERENCES users(user_id) ON DELETE CASCADE,
        UNIQUE(user_id, friend_id)
      )
    `);
    console.log("✅ friends 表创建成功");

    // 4. 聊天消息表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS messages (
        message_id INTEGER PRIMARY KEY AUTOINCREMENT,
        sender_id INTEGER NOT NULL,
        receiver_id INTEGER NOT NULL,
        content TEXT NOT NULL,
        if_read BOOLEAN DEFAULT 0,
        sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
        FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE
      )
    `);
    console.log("✅ messages 表创建成功");

    // 5. 好友请求表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS friend_requests (
        request_id INTEGER PRIMARY KEY AUTOINCREMENT,
        sender_id INTEGER NOT NULL,
        receiver_id INTEGER NOT NULL,
        status VARCHAR(10) DEFAULT 'pending',
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
        FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE,
        UNIQUE(sender_id, receiver_id)
      )
    `);
    console.log("✅ friend_requests 表创建成功");

    // 6. 词汇类型表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS word_types (
        type_id INTEGER PRIMARY KEY AUTOINCREMENT,
        name VARCHAR(50) NOT NULL UNIQUE,
        description TEXT,
        total_words INTEGER DEFAULT 0
      )
    `);
    console.log("✅ word_types 表创建成功");

    // 7. 单词表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS words (
        word_id INTEGER PRIMARY KEY AUTOINCREMENT,
        word VARCHAR(80) NOT NULL,
        part_of_speech VARCHAR(20),
        phonetic VARCHAR(100),
        definition TEXT,
        example TEXT,
        type_id INTEGER NOT NULL,
        synonyms TEXT,
        antonyms TEXT,
        usage_notes TEXT,
        audio_url VARCHAR(500),
        image_url VARCHAR(500),
        FOREIGN KEY (type_id) REFERENCES word_types(type_id) ON DELETE CASCADE
      )
    `);
    console.log("✅ words 表创建成功");

    // 8. 用户单词进度表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS user_word_progress (
        progress_id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        word_id INTEGER NOT NULL,
        type_id INTEGER NOT NULL,
        stage INTEGER DEFAULT 0,
        last_review_time DATETIME,
        next_review_time DATETIME,
        review_count INTEGER DEFAULT 0,
        passed_date DATE,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
        FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE,
        FOREIGN KEY (type_id) REFERENCES word_types(type_id) ON DELETE CASCADE,
        UNIQUE(user_id, word_id, type_id)
      )
    `);
    console.log("✅ user_word_progress 表创建成功");

    // 9. 每日打卡记录表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS daily_study_record (
        record_id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        study_date DATE NOT NULL,
        new_words INTEGER DEFAULT 0,
        review_words INTEGER DEFAULT 0,
        total_words INTEGER DEFAULT 0,
        streak INTEGER DEFAULT 0,
        type_id INTEGER,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
        FOREIGN KEY (type_id) REFERENCES word_types(type_id) ON DELETE CASCADE,
        UNIQUE(user_id, study_date, type_id)
      )
    `);
    console.log("✅ daily_study_record 表创建成功");

    // 10. 打卡计划表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS checkin_plans (
        plan_id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        type_id INTEGER NOT NULL,
        words_per_day INTEGER NOT NULL,
        start_date DATE NOT NULL,
        status VARCHAR(20) DEFAULT 'active',
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
        FOREIGN KEY (type_id) REFERENCES word_types(type_id) ON DELETE CASCADE,
        UNIQUE(user_id, type_id)
      )
    `);
    console.log("✅ checkin_plans 表创建成功");

    // 11. 用户选择的词汇类型表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS user_selected_types (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        type_id INTEGER NOT NULL,
        selected_date DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
        FOREIGN KEY (type_id) REFERENCES word_types(type_id) ON DELETE CASCADE,
        UNIQUE(user_id)
      )
    `);
    console.log("✅ user_selected_types 表创建成功");

    // 12. 课程表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS courses (
        id VARCHAR(36) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )
    `);
    console.log("✅ courses 表创建成功");

    // 13. 题目表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS questions (
        id VARCHAR(36) PRIMARY KEY,
        type VARCHAR(20) NOT NULL,
        difficulty VARCHAR(20) NOT NULL,
        title VARCHAR(255) NOT NULL,
        preview TEXT,
        content TEXT NOT NULL,
        audio_url VARCHAR(500),
        tags TEXT,
        related_course_id VARCHAR(36),
        related_chapter VARCHAR(100),
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (related_course_id) REFERENCES courses(id) ON DELETE SET NULL
      )
    `);
    console.log("✅ questions 表创建成功");

    // 14. 小题表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS question_items (
        id VARCHAR(36) PRIMARY KEY,
        question_id VARCHAR(36) NOT NULL,
        question_text TEXT NOT NULL,
        question_type VARCHAR(20) NOT NULL,
        options TEXT,
        answer VARCHAR(50) NOT NULL,
        explanation TEXT,
        order_num INTEGER NOT NULL,
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
      )
    `);
    console.log("✅ question_items 表创建成功");

    // 15. 做题记录表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS user_question_records (
        id VARCHAR(36) PRIMARY KEY,
        user_id INTEGER NOT NULL,
        question_id VARCHAR(36) NOT NULL,
        status VARCHAR(20) DEFAULT 'not-done',
        last_result VARCHAR(20),
        last_attempt_date DATETIME,
        correct_count INTEGER DEFAULT 0,
        wrong_count INTEGER DEFAULT 0,
        is_favorited BOOLEAN DEFAULT 0,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
        UNIQUE(user_id, question_id)
      )
    `);
    console.log("✅ user_question_records 表创建成功");

    // 16. 答题详情表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS user_answer_details (
        id VARCHAR(36) PRIMARY KEY,
        record_id VARCHAR(36) NOT NULL,
        question_item_id VARCHAR(36) NOT NULL,
        user_answer TEXT NOT NULL,
        is_correct BOOLEAN NOT NULL,
        time_spent INTEGER,
        answered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (record_id) REFERENCES user_question_records(id) ON DELETE CASCADE,
        FOREIGN KEY (question_item_id) REFERENCES question_items(id) ON DELETE CASCADE
      )
    `);
    console.log("✅ user_answer_details 表创建成功");

    // 17. 生词表
    await dbRun(`
      CREATE TABLE IF NOT EXISTS user_vocabulary (
        id VARCHAR(36) PRIMARY KEY,
        user_id INTEGER NOT NULL,
        word_id INTEGER NOT NULL,
        translation VARCHAR(255),
        source_question_id VARCHAR(36),
        if_mastered BOOLEAN DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
        FOREIGN KEY (word_id) REFERENCES words(word_id) ON DELETE CASCADE,
        FOREIGN KEY (source_question_id) REFERENCES questions(id) ON DELETE SET NULL,
        UNIQUE(user_id, word_id)
      )
    `);
    console.log("✅ user_vocabulary 表创建成功");

    // 创建索引以提升查询性能
    await dbRun(
      "CREATE INDEX IF NOT EXISTS idx_plans_user_date ON plans(user_id, date)"
    );
    await dbRun(
      "CREATE INDEX IF NOT EXISTS idx_messages_sender_receiver ON messages(sender_id, receiver_id)"
    );
    await dbRun(
      "CREATE INDEX IF NOT EXISTS idx_user_word_progress_user_type ON user_word_progress(user_id, type_id)"
    );
    await dbRun(
      "CREATE INDEX IF NOT EXISTS idx_questions_type_difficulty ON questions(type, difficulty)"
    );
    await dbRun(
      "CREATE INDEX IF NOT EXISTS idx_user_question_records_user ON user_question_records(user_id)"
    );
    console.log("✅ 索引创建成功");

    // 初始化词汇类型数据
    const wordTypes = [
      { name: "elementary", description: "适合初学者", total_words: 1000 },
      { name: "cet46", description: "大学英语四六级", total_words: 1500 },
      { name: "postgraduate", description: "考研英语必备", total_words: 2000 },
      { name: "toefl_ielts", description: "出国考试必备", total_words: 2500 },
      { name: "professional", description: "行业专业用语", total_words: 800 },
    ];

    for (const type of wordTypes) {
      await dbRun(
        `INSERT OR IGNORE INTO word_types (name, description, total_words) VALUES (?, ?, ?)`,
        [type.name, type.description, type.total_words]
      );
    }
    console.log("✅ 词汇类型初始数据插入成功");

    console.log("🎉 数据库初始化完成！");
  } catch (error) {
    console.error("❌ 数据库初始化失败:", error);
    throw error;
  }
}

module.exports = { initDatabase };

// 如果直接运行此脚本，执行初始化
if (require.main === module) {
  initDatabase()
    .then(() => {
      console.log("数据库初始化脚本执行完成");
      process.exit(0);
    })
    .catch((err) => {
      console.error("数据库初始化脚本执行失败:", err);
      process.exit(1);
    });
}
