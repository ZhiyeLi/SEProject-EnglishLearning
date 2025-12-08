const { dbRun, dbGet, dbAll } = require("../config/database");
const { v4: uuidv4 } = require("uuid");
const ResponseUtil = require("../utils/response");

/**
 * 获取题目列表（支持多条件筛选）
 */
const getQuestions = async (req, res) => {
  try {
    const userId = req.userId;
    const {
      page = 1,
      limit = 10,
      type,
      difficulty,
      status,
      courseId,
      keyword,
    } = req.query;

    let whereConditions = [];
    let params = [];

    // 类型筛选（支持多选）
    if (type) {
      const types = type.split(",");
      whereConditions.push(`q.type IN (${types.map(() => "?").join(",")})`);
      params.push(...types);
    }

    // 难度筛选（支持多选）
    if (difficulty) {
      const difficulties = difficulty.split(",");
      whereConditions.push(
        `q.difficulty IN (${difficulties.map(() => "?").join(",")})`
      );
      params.push(...difficulties);
    }

    // 课程筛选
    if (courseId) {
      whereConditions.push("q.related_course_id = ?");
      params.push(courseId);
    }

    // 关键词搜索
    if (keyword) {
      whereConditions.push("(q.title LIKE ? OR q.content LIKE ?)");
      params.push(`%${keyword}%`, `%${keyword}%`);
    }

    const whereClause =
      whereConditions.length > 0
        ? "WHERE " + whereConditions.join(" AND ")
        : "";

    // 获取总数
    const countResult = await dbGet(
      `SELECT COUNT(DISTINCT q.id) as total FROM questions q ${whereClause}`,
      params
    );

    // 获取题目列表
    const offset = (page - 1) * limit;
    const questions = await dbAll(
      `SELECT 
         q.*,
         r.status as userStatus,
         r.last_result as lastResult,
         r.is_favorited as isFavorited
       FROM questions q
       LEFT JOIN user_question_records r ON q.id = r.question_id AND r.user_id = ?
       ${whereClause}
       ORDER BY q.created_at DESC
       LIMIT ? OFFSET ?`,
      [userId, ...params, parseInt(limit), offset]
    );

    // 解析 JSON 字段
    const processedQuestions = questions.map((q) => ({
      ...q,
      tags: q.tags ? JSON.parse(q.tags) : [],
    }));

    return ResponseUtil.paginate(
      res,
      processedQuestions,
      countResult.total,
      page,
      limit
    );
  } catch (error) {
    console.error("获取题目列表失败:", error);
    return ResponseUtil.error(res, "获取题目列表失败", 500);
  }
};

/**
 * 获取题目详情（包含小题）
 */
const getQuestionDetail = async (req, res) => {
  try {
    const { id } = req.params;

    const question = await dbGet("SELECT * FROM questions WHERE id = ?", [id]);

    if (!question) {
      return ResponseUtil.error(res, "题目不存在", 404);
    }

    // 获取小题
    const items = await dbAll(
      "SELECT * FROM question_items WHERE question_id = ? ORDER BY order_num",
      [id]
    );

    // 处理 JSON 字段
    const processedItems = items.map((item) => ({
      ...item,
      options: item.options ? JSON.parse(item.options) : null,
    }));

    return ResponseUtil.success(res, {
      ...question,
      tags: question.tags ? JSON.parse(question.tags) : [],
      items: processedItems,
    });
  } catch (error) {
    console.error("获取题目详情失败:", error);
    return ResponseUtil.error(res, "获取题目详情失败", 500);
  }
};

/**
 * 提交答题结果
 */
const submitAnswer = async (req, res) => {
  try {
    const userId = req.userId;
    const { questionId, answers, timeSpent } = req.body;

    if (!questionId || !answers) {
      return ResponseUtil.error(res, "题目ID和答案不能为空", 400);
    }

    // 获取题目的所有小题
    const items = await dbAll(
      "SELECT * FROM question_items WHERE question_id = ? ORDER BY order_num",
      [questionId]
    );

    if (items.length === 0) {
      return ResponseUtil.error(res, "题目不存在", 404);
    }

    // 创建或获取答题记录
    let record = await dbGet(
      "SELECT * FROM user_question_records WHERE user_id = ? AND question_id = ?",
      [userId, questionId]
    );

    const recordId = record ? record.id : uuidv4();

    if (!record) {
      await dbRun(
        "INSERT INTO user_question_records (id, user_id, question_id, status) VALUES (?, ?, ?, ?)",
        [recordId, userId, questionId, "done"]
      );
    }

    // 批改答案
    let correctCount = 0;
    let totalCount = items.length;
    const results = [];

    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      const userAnswer = answers[i];
      const isCorrect =
        String(userAnswer).trim().toLowerCase() ===
        String(item.answer).trim().toLowerCase();

      if (isCorrect) correctCount++;

      // 保存答题详情
      await dbRun(
        "INSERT INTO user_answer_details (id, record_id, question_item_id, user_answer, is_correct, time_spent) VALUES (?, ?, ?, ?, ?, ?)",
        [
          uuidv4(),
          recordId,
          item.id,
          userAnswer,
          isCorrect ? 1 : 0,
          timeSpent || null,
        ]
      );

      results.push({
        questionItemId: item.id,
        userAnswer,
        correctAnswer: item.answer,
        isCorrect,
        explanation: item.explanation,
      });
    }

    const isAllCorrect = correctCount === totalCount;
    const lastResult = isAllCorrect ? "correct" : "wrong";

    // 更新答题记录
    await dbRun(
      `UPDATE user_question_records SET
         status = 'done',
         last_result = ?,
         last_attempt_date = CURRENT_TIMESTAMP,
         correct_count = correct_count + ?,
         wrong_count = wrong_count + ?
       WHERE id = ?`,
      [lastResult, isAllCorrect ? 1 : 0, isAllCorrect ? 0 : 1, recordId]
    );

    return ResponseUtil.success(res, {
      correctCount,
      totalCount,
      accuracy: Math.round((correctCount / totalCount) * 100),
      isAllCorrect,
      results,
    });
  } catch (error) {
    console.error("提交答案失败:", error);
    return ResponseUtil.error(res, "提交答案失败", 500);
  }
};

/**
 * 收藏/取消收藏题目
 */
const toggleFavorite = async (req, res) => {
  try {
    const userId = req.userId;
    const { questionId } = req.body;

    if (!questionId) {
      return ResponseUtil.error(res, "题目ID不能为空", 400);
    }

    // 获取或创建记录
    let record = await dbGet(
      "SELECT * FROM user_question_records WHERE user_id = ? AND question_id = ?",
      [userId, questionId]
    );

    if (!record) {
      const recordId = uuidv4();
      await dbRun(
        "INSERT INTO user_question_records (id, user_id, question_id, is_favorited) VALUES (?, ?, ?, 1)",
        [recordId, userId, questionId]
      );
      return ResponseUtil.success(res, { isFavorited: true });
    } else {
      const newFavoriteStatus = record.is_favorited ? 0 : 1;
      await dbRun(
        "UPDATE user_question_records SET is_favorited = ? WHERE id = ?",
        [newFavoriteStatus, record.id]
      );
      return ResponseUtil.success(res, {
        isFavorited: newFavoriteStatus === 1,
      });
    }
  } catch (error) {
    console.error("切换收藏状态失败:", error);
    return ResponseUtil.error(res, "切换收藏状态失败", 500);
  }
};

/**
 * 获取做题统计
 */
const getStatistics = async (req, res) => {
  try {
    const userId = req.userId;

    // 今日做题数
    const today = new Date().toISOString().split("T")[0];
    const todayCount = await dbGet(
      `SELECT COUNT(*) as count FROM user_question_records 
       WHERE user_id = ? AND DATE(last_attempt_date) = ?`,
      [userId, today]
    );

    // 总正确率
    const totalStats = await dbGet(
      `SELECT 
         COUNT(*) as total,
         SUM(correct_count) as totalCorrect,
         SUM(wrong_count) as totalWrong
       FROM user_question_records
       WHERE user_id = ? AND status = 'done'`,
      [userId]
    );

    const totalAttempts =
      (totalStats.totalCorrect || 0) + (totalStats.totalWrong || 0);
    const accuracy =
      totalAttempts > 0
        ? Math.round(((totalStats.totalCorrect || 0) / totalAttempts) * 100)
        : 0;

    return ResponseUtil.success(res, {
      todayCount: todayCount.count,
      totalQuestions: totalStats.total || 0,
      totalAccuracy: accuracy,
      totalCorrect: totalStats.totalCorrect || 0,
      totalWrong: totalStats.totalWrong || 0,
    });
  } catch (error) {
    console.error("获取统计信息失败:", error);
    return ResponseUtil.error(res, "获取统计信息失败", 500);
  }
};

/**
 * 获取错题列表
 */
const getWrongQuestions = async (req, res) => {
  try {
    const userId = req.userId;
    const { page = 1, limit = 10, type } = req.query;

    let whereConditions = ["r.user_id = ?", "r.last_result = ?"];
    let params = [userId, "wrong"];

    if (type) {
      whereConditions.push("q.type = ?");
      params.push(type);
    }

    const whereClause = "WHERE " + whereConditions.join(" AND ");

    // 获取总数
    const countResult = await dbGet(
      `SELECT COUNT(*) as total FROM user_question_records r
       JOIN questions q ON r.question_id = q.id
       ${whereClause}`,
      params
    );

    // 获取错题列表
    const offset = (page - 1) * limit;
    const wrongQuestions = await dbAll(
      `SELECT 
         q.*,
         r.last_attempt_date,
         r.wrong_count
       FROM user_question_records r
       JOIN questions q ON r.question_id = q.id
       ${whereClause}
       ORDER BY r.last_attempt_date DESC
       LIMIT ? OFFSET ?`,
      [...params, parseInt(limit), offset]
    );

    return ResponseUtil.paginate(
      res,
      wrongQuestions,
      countResult.total,
      page,
      limit
    );
  } catch (error) {
    console.error("获取错题列表失败:", error);
    return ResponseUtil.error(res, "获取错题列表失败", 500);
  }
};

/**
 * 标记错题为已掌握
 */
const markWrongQuestionMastered = async (req, res) => {
  try {
    const userId = req.userId;
    const { questionId } = req.body;

    if (!questionId) {
      return ResponseUtil.error(res, "题目ID不能为空", 400);
    }

    const record = await dbGet(
      "SELECT * FROM user_question_records WHERE user_id = ? AND question_id = ?",
      [userId, questionId]
    );

    if (!record) {
      return ResponseUtil.error(res, "记录不存在", 404);
    }

    // 将状态标记为正确
    await dbRun(
      "UPDATE user_question_records SET last_result = ? WHERE id = ?",
      ["correct", record.id]
    );

    return ResponseUtil.success(res, { success: true });
  } catch (error) {
    console.error("标记错题失败:", error);
    return ResponseUtil.error(res, "标记错题失败", 500);
  }
};

/**
 * 获取课程列表
 */
const getCourses = async (req, res) => {
  try {
    const courses = await dbAll(
      "SELECT * FROM courses ORDER BY created_at DESC"
    );

    return ResponseUtil.success(res, courses);
  } catch (error) {
    console.error("获取课程列表失败:", error);
    return ResponseUtil.error(res, "获取课程列表失败", 500);
  }
};

/**
 * 获取课程关联的题目
 */
const getCourseQuestions = async (req, res) => {
  try {
    const { courseId, chapterId } = req.query;

    if (!courseId) {
      return ResponseUtil.error(res, "课程ID不能为空", 400);
    }

    let whereConditions = ["related_course_id = ?"];
    let params = [courseId];

    if (chapterId) {
      whereConditions.push("related_chapter = ?");
      params.push(chapterId);
    }

    const questions = await dbAll(
      `SELECT * FROM questions WHERE ${whereConditions.join(" AND ")} ORDER BY created_at DESC`,
      params
    );

    return ResponseUtil.success(res, questions);
  } catch (error) {
    console.error("获取课程题目失败:", error);
    return ResponseUtil.error(res, "获取课程题目失败", 500);
  }
};

/**
 * 获取用户生词本
 */
const getUserVocabulary = async (req, res) => {
  try {
    const userId = req.userId;

    const vocabulary = await dbAll(
      `SELECT 
         v.*,
         w.word,
         w.phonetic,
         w.definition
       FROM user_vocabulary v
       JOIN words w ON v.word_id = w.word_id
       WHERE v.user_id = ? AND v.if_mastered = 0
       ORDER BY v.created_at DESC`,
      [userId]
    );

    return ResponseUtil.success(res, vocabulary);
  } catch (error) {
    console.error("获取生词本失败:", error);
    return ResponseUtil.error(res, "获取生词本失败", 500);
  }
};

/**
 * 添加生词
 */
const addVocabulary = async (req, res) => {
  try {
    const userId = req.userId;
    const { wordId, translation, sourceQuestionId } = req.body;

    if (!wordId) {
      return ResponseUtil.error(res, "单词ID不能为空", 400);
    }

    // 检查是否已存在
    const existing = await dbGet(
      "SELECT * FROM user_vocabulary WHERE user_id = ? AND word_id = ?",
      [userId, wordId]
    );

    if (existing) {
      return ResponseUtil.error(res, "该单词已在生词本中", 400);
    }

    const id = uuidv4();
    await dbRun(
      "INSERT INTO user_vocabulary (id, user_id, word_id, translation, source_question_id) VALUES (?, ?, ?, ?, ?)",
      [id, userId, wordId, translation, sourceQuestionId]
    );

    return ResponseUtil.success(res, { id, success: true });
  } catch (error) {
    console.error("添加生词失败:", error);
    return ResponseUtil.error(res, "添加生词失败", 500);
  }
};

module.exports = {
  getQuestions,
  getQuestionDetail,
  submitAnswer,
  toggleFavorite,
  getStatistics,
  getWrongQuestions,
  markWrongQuestionMastered,
  getCourses,
  getCourseQuestions,
  getUserVocabulary,
  addVocabulary,
};
