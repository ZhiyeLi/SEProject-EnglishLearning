const { dbRun, dbGet, dbAll } = require("../config/database");
const ResponseUtil = require("../utils/response");

/**
 * 获取词汇类型列表
 */
const getWordTypeList = async (req, res) => {
  try {
    const types = await dbAll(
      "SELECT type_id, name, description, total_words FROM word_types"
    );

    // 转换为前端期望的格式(驼峰命名)
    const formattedTypes = types.map((type) => ({
      id: type.name, // 使用name作为id,与前端WORD_TYPES一致
      typeId: type.type_id, // 数据库的type_id
      name: formatTypeName(type.name),
      description: type.description,
      totalWords: type.total_words,
    }));

    return ResponseUtil.success(res, formattedTypes);
  } catch (error) {
    console.error("获取词汇类型失败:", error);
    return ResponseUtil.error(res, "获取词汇类型失败", 500);
  }
};

// 辅助函数:格式化类型名称
function formatTypeName(name) {
  const nameMap = {
    elementary: "初级词汇",
    cet46: "四六级词汇",
    postgraduate: "考研词汇",
    toefl_ielts: "托福雅思词汇",
    professional: "专业术语词汇",
  };
  return nameMap[name] || name;
}

/**
 * 获取用户在指定词汇类型的进度
 * 如果不传typeId,返回所有类型的进度
 */
const getUserWordProgress = async (req, res) => {
  try {
    const userId = req.userId;
    const { typeId } = req.query;

    // 如果没有指定typeId,返回所有类型的进度
    if (!typeId) {
      // 获取所有词汇类型
      const wordTypes = await dbAll("SELECT type_id, name FROM word_types");

      const progressData = {};

      for (const type of wordTypes) {
        // 获取用户已打卡的单词
        const progressRecords = await dbAll(
          "SELECT word_id, passed_date FROM user_word_progress WHERE user_id = ? AND type_id = ? AND stage >= 1",
          [userId, type.type_id]
        );

        const passedWords = progressRecords.map((r) => r.word_id);
        const passedCount = passedWords.length;

        // 获取最后打卡日期和连续天数
        const lastRecord = await dbGet(
          "SELECT study_date, streak FROM daily_study_record WHERE user_id = ? AND type_id = ? ORDER BY study_date DESC LIMIT 1",
          [userId, type.type_id]
        );

        progressData[type.name] = {
          passedCount,
          passedWords,
          lastPassedDate: lastRecord?.study_date || null,
          consecutiveDays: lastRecord?.streak || 0,
        };
      }

      return ResponseUtil.success(res, progressData);
    }

    // 如果指定了typeId,返回单个类型的进度
    // 获取该类型的总单词数
    const wordType = await dbGet(
      "SELECT type_id, name, total_words FROM word_types WHERE type_id = ?",
      [typeId]
    );

    if (!wordType) {
      return ResponseUtil.error(res, "词汇类型不存在", 404);
    }

    // 获取用户已打卡的单词
    const progressRecords = await dbAll(
      "SELECT word_id, passed_date FROM user_word_progress WHERE user_id = ? AND type_id = ? AND stage >= 1",
      [userId, typeId]
    );

    const passedWords = progressRecords.map((r) => r.word_id);
    const passedCount = passedWords.length;

    // 获取最后打卡日期和连续天数
    const lastRecord = await dbGet(
      "SELECT study_date, streak FROM daily_study_record WHERE user_id = ? AND type_id = ? ORDER BY study_date DESC LIMIT 1",
      [userId, typeId]
    );

    return ResponseUtil.success(res, {
      typeId,
      passedCount,
      totalCount: wordType.total_words,
      passedWords,
      lastPassedDate: lastRecord?.study_date || null,
      consecutiveDays: lastRecord?.streak || 0,
    });
  } catch (error) {
    console.error("获取用户进度失败:", error);
    return ResponseUtil.error(res, "获取用户进度失败", 500);
  }
};

/**
 * 获取指定类型的单词列表（分页）
 */
const getWordsByType = async (req, res) => {
  try {
    const { typeId, page = 1, pageSize = 20 } = req.query;

    if (!typeId) {
      console.warn("getWordsByType: 缺少typeId参数");
      return ResponseUtil.error(res, "请先选择词汇类型", 400);
    }

    const offset = (page - 1) * pageSize;

    // 获取总数
    const countResult = await dbGet(
      "SELECT COUNT(*) as total FROM words WHERE type_id = ?",
      [typeId]
    );

    // 获取单词列表
    const words = await dbAll(
      `SELECT word_id, word, part_of_speech, phonetic, definition, example, audio_url, image_url
       FROM words 
       WHERE type_id = ?
       LIMIT ? OFFSET ?`,
      [typeId, parseInt(pageSize), offset]
    );

    return ResponseUtil.paginate(res, words, countResult.total, page, pageSize);
  } catch (error) {
    console.error("获取单词列表失败:", error);
    return ResponseUtil.error(res, "获取单词列表失败", 500);
  }
};

/**
 * 标记单词为已打卡
 */
const markWordPassed = async (req, res) => {
  try {
    const userId = req.userId;
    const { typeId, wordId } = req.body;

    if (!typeId || !wordId) {
      return ResponseUtil.error(res, "类型ID和单词ID不能为空", 400);
    }

    const now = new Date();
    const today = now.toISOString().split("T")[0];

    // 插入或更新进度记录
    await dbRun(
      `INSERT INTO user_word_progress (user_id, word_id, type_id, stage, last_review_time, passed_date)
       VALUES (?, ?, ?, 1, ?, ?)
       ON CONFLICT(user_id, word_id, type_id) DO UPDATE SET
         stage = CASE WHEN stage = 0 THEN 1 ELSE stage END,
         last_review_time = ?,
         passed_date = CASE WHEN passed_date IS NULL THEN ? ELSE passed_date END`,
      [
        userId,
        wordId,
        typeId,
        now.toISOString(),
        today,
        now.toISOString(),
        today,
      ]
    );

    // 更新每日打卡记录
    const existingRecord = await dbGet(
      "SELECT * FROM daily_study_record WHERE user_id = ? AND study_date = ? AND type_id = ?",
      [userId, today, typeId]
    );

    if (existingRecord) {
      await dbRun(
        "UPDATE daily_study_record SET total_words = total_words + 1, new_words = new_words + 1 WHERE user_id = ? AND study_date = ? AND type_id = ?",
        [userId, today, typeId]
      );
    } else {
      // 计算连续天数
      const yesterday = new Date(now);
      yesterday.setDate(yesterday.getDate() - 1);
      const yesterdayStr = yesterday.toISOString().split("T")[0];

      const yesterdayRecord = await dbGet(
        "SELECT streak FROM daily_study_record WHERE user_id = ? AND study_date = ? AND type_id = ?",
        [userId, yesterdayStr, typeId]
      );

      const streak = yesterdayRecord ? yesterdayRecord.streak + 1 : 1;

      await dbRun(
        "INSERT INTO daily_study_record (user_id, study_date, type_id, new_words, total_words, streak) VALUES (?, ?, ?, 1, 1, ?)",
        [userId, today, typeId, streak]
      );
    }

    // 获取新的已打卡数量
    const progressResult = await dbGet(
      "SELECT COUNT(*) as count FROM user_word_progress WHERE user_id = ? AND type_id = ? AND stage >= 1",
      [userId, typeId]
    );

    return ResponseUtil.success(res, {
      success: true,
      passedDate: today,
      newPassedCount: progressResult.count,
    });
  } catch (error) {
    console.error("标记单词失败:", error);
    return ResponseUtil.error(res, "标记单词失败", 500);
  }
};

/**
 * 取消单词打卡
 */
const unmarkWordPassed = async (req, res) => {
  try {
    const userId = req.userId;
    const { typeId, wordId } = req.body;

    if (!typeId || !wordId) {
      return ResponseUtil.error(res, "类型ID和单词ID不能为空", 400);
    }

    // 更新进度记录
    await dbRun(
      "UPDATE user_word_progress SET stage = 0, passed_date = NULL WHERE user_id = ? AND word_id = ? AND type_id = ?",
      [userId, wordId, typeId]
    );

    // 获取新的已打卡数量
    const progressResult = await dbGet(
      "SELECT COUNT(*) as count FROM user_word_progress WHERE user_id = ? AND type_id = ? AND stage >= 1",
      [userId, typeId]
    );

    return ResponseUtil.success(res, {
      success: true,
      newPassedCount: progressResult.count,
    });
  } catch (error) {
    console.error("取消打卡失败:", error);
    return ResponseUtil.error(res, "取消打卡失败", 500);
  }
};

/**
 * 创建或更新打卡计划
 */
const createCheckInPlan = async (req, res) => {
  try {
    const userId = req.userId;
    const { typeId, wordsPerDay } = req.body;

    if (!typeId || !wordsPerDay) {
      return ResponseUtil.error(res, "类型ID和每日单词数不能为空", 400);
    }

    if (wordsPerDay < 1 || wordsPerDay > 100) {
      return ResponseUtil.error(res, "每日单词数必须在1-100之间", 400);
    }

    const startDate = new Date().toISOString().split("T")[0];

    // 获取剩余单词数
    const wordType = await dbGet(
      "SELECT total_words FROM word_types WHERE type_id = ?",
      [typeId]
    );
    const progressResult = await dbGet(
      "SELECT COUNT(*) as passed FROM user_word_progress WHERE user_id = ? AND type_id = ? AND stage >= 1",
      [userId, typeId]
    );

    const remainingWords = wordType.total_words - (progressResult?.passed || 0);
    const daysNeeded = Math.ceil(remainingWords / wordsPerDay);

    // 插入或更新计划
    await dbRun(
      `INSERT INTO checkin_plans (user_id, type_id, words_per_day, start_date, status)
       VALUES (?, ?, ?, ?, 'active')
       ON CONFLICT(user_id, type_id) DO UPDATE SET
         words_per_day = ?,
         start_date = ?,
         status = 'active',
         updated_at = CURRENT_TIMESTAMP`,
      [userId, typeId, wordsPerDay, startDate, wordsPerDay, startDate]
    );

    const plan = await dbGet(
      "SELECT * FROM checkin_plans WHERE user_id = ? AND type_id = ?",
      [userId, typeId]
    );

    return ResponseUtil.success(res, {
      planId: plan.plan_id,
      typeId,
      wordsPerDay,
      remainingWords,
      daysNeeded,
      startDate,
      status: "active",
    });
  } catch (error) {
    console.error("创建打卡计划失败:", error);
    return ResponseUtil.error(res, "创建打卡计划失败", 500);
  }
};

/**
 * 获取用户打卡计划
 */
const getUserCheckInPlan = async (req, res) => {
  try {
    const userId = req.userId;
    const { typeId } = req.query;

    if (!typeId) {
      return ResponseUtil.error(res, "类型ID不能为空", 400);
    }

    const plan = await dbGet(
      "SELECT * FROM checkin_plans WHERE user_id = ? AND type_id = ?",
      [userId, typeId]
    );

    if (!plan) {
      return ResponseUtil.success(res, null, "暂无打卡计划");
    }

    // 计算剩余单词数
    const wordType = await dbGet(
      "SELECT total_words FROM word_types WHERE type_id = ?",
      [typeId]
    );
    const progressResult = await dbGet(
      "SELECT COUNT(*) as passed FROM user_word_progress WHERE user_id = ? AND type_id = ? AND stage >= 1",
      [userId, typeId]
    );

    const remainingWords = wordType.total_words - (progressResult?.passed || 0);
    const daysNeeded = Math.ceil(remainingWords / plan.words_per_day);

    return ResponseUtil.success(res, {
      planId: plan.plan_id,
      typeId: plan.type_id,
      wordsPerDay: plan.words_per_day,
      remainingWords,
      daysNeeded,
      startDate: plan.start_date,
      status: plan.status,
    });
  } catch (error) {
    console.error("获取打卡计划失败:", error);
    return ResponseUtil.error(res, "获取打卡计划失败", 500);
  }
};

/**
 * 更新计划状态
 */
const updatePlanStatus = async (req, res) => {
  try {
    const userId = req.userId;
    const { planId, status } = req.body;

    if (!planId || !status) {
      return ResponseUtil.error(res, "计划ID和状态不能为空", 400);
    }

    if (!["active", "paused", "completed"].includes(status)) {
      return ResponseUtil.error(res, "无效的状态值", 400);
    }

    await dbRun(
      "UPDATE checkin_plans SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE plan_id = ? AND user_id = ?",
      [status, planId, userId]
    );

    return ResponseUtil.success(res, {
      success: true,
      status,
      updatedAt: new Date().toISOString(),
    });
  } catch (error) {
    console.error("更新计划状态失败:", error);
    return ResponseUtil.error(res, "更新计划状态失败", 500);
  }
};

/**
 * 获取已打卡的单词列表
 */
const getPassedWords = async (req, res) => {
  try {
    const userId = req.userId;
    const { typeId } = req.query;

    if (!typeId) {
      return ResponseUtil.error(res, "类型ID不能为空", 400);
    }

    const words = await dbAll(
      `SELECT 
         w.word_id, w.word, w.phonetic, w.definition, w.example,
         p.passed_date, p.review_count
       FROM user_word_progress p
       JOIN words w ON p.word_id = w.word_id
       WHERE p.user_id = ? AND p.type_id = ? AND p.stage >= 1
       ORDER BY p.passed_date DESC`,
      [userId, typeId]
    );

    return ResponseUtil.success(res, words);
  } catch (error) {
    console.error("获取已打卡单词失败:", error);
    return ResponseUtil.error(res, "获取已打卡单词失败", 500);
  }
};

/**
 * 获取/设置用户选择的词汇类型
 */
const getSelectedWordType = async (req, res) => {
  try {
    const userId = req.userId;

    const selected = await dbGet(
      `SELECT ust.type_id, ust.selected_date, wt.name 
       FROM user_selected_types ust
       JOIN word_types wt ON ust.type_id = wt.type_id
       WHERE ust.user_id = ?`,
      [userId]
    );

    if (selected) {
      // 返回名称作为typeId,保持与前端一致
      return ResponseUtil.success(res, {
        typeId: selected.name, // 返回名称(如 'elementary')
        selectedDate: selected.selected_date,
      });
    }

    return ResponseUtil.success(res, null);
  } catch (error) {
    console.error("获取选择的词汇类型失败:", error);
    return ResponseUtil.error(res, "获取选择的词汇类型失败", 500);
  }
};

const setSelectedWordType = async (req, res) => {
  try {
    const userId = req.userId;
    const { typeId } = req.body;

    if (!typeId) {
      return ResponseUtil.error(res, "类型ID不能为空", 400);
    }

    // 如果typeId是字符串名称,需要先查找对应的数据库ID
    let dbTypeId = typeId;
    if (isNaN(typeId)) {
      // typeId是字符串,查找对应的type_id
      const wordType = await dbGet(
        "SELECT type_id FROM word_types WHERE name = ?",
        [typeId]
      );

      if (!wordType) {
        return ResponseUtil.error(res, "词汇类型不存在", 404);
      }

      dbTypeId = wordType.type_id;
    }

    await dbRun(
      `INSERT INTO user_selected_types (user_id, type_id)
       VALUES (?, ?)
       ON CONFLICT(user_id) DO UPDATE SET
         type_id = ?,
         selected_date = CURRENT_TIMESTAMP`,
      [userId, dbTypeId, dbTypeId]
    );

    return ResponseUtil.success(res, {
      success: true,
      typeId,
      selectedDate: new Date().toISOString(),
    });
  } catch (error) {
    console.error("设置选择的词汇类型失败:", error);
    return ResponseUtil.error(res, "设置选择的词汇类型失败", 500);
  }
};

/**
 * 批量标记单词为已打卡
 */
const batchMarkWordsPassed = async (req, res) => {
  try {
    const userId = req.userId;
    const { typeId, wordIds } = req.body;

    if (!typeId || !wordIds || !Array.isArray(wordIds)) {
      return ResponseUtil.error(res, "类型ID和单词ID数组不能为空", 400);
    }

    const now = new Date();
    const today = now.toISOString().split("T")[0];
    let passedCount = 0;
    const failedWords = [];

    for (const wordId of wordIds) {
      try {
        await dbRun(
          `INSERT INTO user_word_progress (user_id, word_id, type_id, stage, last_review_time, passed_date)
           VALUES (?, ?, ?, 1, ?, ?)
           ON CONFLICT(user_id, word_id, type_id) DO UPDATE SET
             stage = CASE WHEN stage = 0 THEN 1 ELSE stage END,
             last_review_time = ?,
             passed_date = CASE WHEN passed_date IS NULL THEN ? ELSE passed_date END`,
          [
            userId,
            wordId,
            typeId,
            now.toISOString(),
            today,
            now.toISOString(),
            today,
          ]
        );
        passedCount++;
      } catch (error) {
        failedWords.push(wordId);
      }
    }

    return ResponseUtil.success(res, {
      success: true,
      passedCount,
      failedWords,
    });
  } catch (error) {
    console.error("批量标记单词失败:", error);
    return ResponseUtil.error(res, "批量标记单词失败", 500);
  }
};

/**
 * 获取单词详情
 */
const getWordDetail = async (req, res) => {
  try {
    const { wordId } = req.params;

    const word = await dbGet("SELECT * FROM words WHERE word_id = ?", [wordId]);

    if (!word) {
      return ResponseUtil.error(res, "单词不存在", 404);
    }

    // 解析 JSON 字段
    const wordDetail = {
      ...word,
      synonyms: word.synonyms ? JSON.parse(word.synonyms) : [],
      antonyms: word.antonyms ? JSON.parse(word.antonyms) : [],
    };

    return ResponseUtil.success(res, wordDetail);
  } catch (error) {
    console.error("获取单词详情失败:", error);
    return ResponseUtil.error(res, "获取单词详情失败", 500);
  }
};

/**
 * 获取今日打卡状态
 */
const getTodayCheckInStatus = async (req, res) => {
  try {
    const userId = req.userId;
    const { typeId } = req.query;

    if (!typeId) {
      return ResponseUtil.error(res, "类型ID不能为空", 400);
    }

    const today = new Date().toISOString().split("T")[0];

    // 获取今日打卡记录
    const todayRecord = await dbGet(
      "SELECT * FROM daily_study_record WHERE user_id = ? AND study_date = ? AND type_id = ?",
      [userId, today, typeId]
    );

    // 获取计划
    const plan = await dbGet(
      "SELECT words_per_day FROM checkin_plans WHERE user_id = ? AND type_id = ? AND status = ?",
      [userId, typeId, "active"]
    );

    const todayCheckedCount = todayRecord?.total_words || 0;
    const todayTarget = plan?.words_per_day || 0;

    return ResponseUtil.success(res, {
      todayCheckedCount,
      todayTarget,
      isCompleted: todayTarget > 0 && todayCheckedCount >= todayTarget,
      checkedWords: [],
    });
  } catch (error) {
    console.error("获取今日打卡状态失败:", error);
    return ResponseUtil.error(res, "获取今日打卡状态失败", 500);
  }
};

/**
 * 获取打卡统计信息
 */
const getCheckInStatistics = async (req, res) => {
  try {
    const userId = req.userId;
    const { typeId } = req.query;

    if (!typeId) {
      return ResponseUtil.error(res, "类型ID不能为空", 400);
    }

    // 获取总打卡天数
    const daysResult = await dbGet(
      "SELECT COUNT(DISTINCT study_date) as totalDays FROM daily_study_record WHERE user_id = ? AND type_id = ?",
      [userId, typeId]
    );

    // 获取连续天数
    const latestRecord = await dbGet(
      "SELECT streak, study_date FROM daily_study_record WHERE user_id = ? AND type_id = ? ORDER BY study_date DESC LIMIT 1",
      [userId, typeId]
    );

    // 获取总单词数
    const wordsResult = await dbGet(
      "SELECT COUNT(*) as totalWords FROM user_word_progress WHERE user_id = ? AND type_id = ? AND stage >= 1",
      [userId, typeId]
    );

    // 计算平均每天单词数
    const averagePerDay =
      daysResult.totalDays > 0
        ? Math.round((wordsResult.totalWords / daysResult.totalDays) * 10) / 10
        : 0;

    return ResponseUtil.success(res, {
      totalDays: daysResult.totalDays,
      consecutiveDays: latestRecord?.streak || 0,
      totalWords: wordsResult.totalWords,
      averagePerDay,
      lastCheckInDate: latestRecord?.study_date || null,
    });
  } catch (error) {
    console.error("获取打卡统计失败:", error);
    return ResponseUtil.error(res, "获取打卡统计失败", 500);
  }
};

module.exports = {
  getWordTypeList,
  getUserWordProgress,
  getWordsByType,
  markWordPassed,
  unmarkWordPassed,
  createCheckInPlan,
  getUserCheckInPlan,
  updatePlanStatus,
  getPassedWords,
  getSelectedWordType,
  setSelectedWordType,
  batchMarkWordsPassed,
  getWordDetail,
  getTodayCheckInStatus,
  getCheckInStatistics,
};
