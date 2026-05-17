/**
 * 题库相关 API (重构版)
 */
import request from "@/utils/request";

export default {
  // ==================== 主界面 API ====================

  /**
   * 获取列表（考试模式或单题模式）
   * @param {Object} params - 查询参数
   * @param {String} params.mode - 模式: 'exam' 或 'single'
   * @param {Number} params.page - 页码
   * @param {Number} params.pageSize - 每页数量
   * @param {String} params.sortBy - 排序方式
   * @param {String} params.keyword - 搜索关键字
   * @param {String} params.category - 考试类型
   * @param {String} params.sectionType - 题型（单题模式）
   * @param {Array} params.containsSectionType - 包含的题型（考试模式）
   * @param {String} params.status - 状态
   */
  getList(params) {
    return request.get("/api/questionbank/list", { params });
  },

  /**
   * 获取今日统计
   */
  getTodayStats() {
    return request.get("/api/questionbank/stats/today");
  },

  /**
   * 获取今日按类型分组的统计
   */
  getTodayStatsByType() {
    return request.get("/api/questionbank/stats/today/by-type");
  },

  /**
   * 添加收藏
   * @param {String} type - 'paper' 或 'question'
   * @param {Number} id - ID
   */
  addFavorite(type, id) {
    return request.post("/api/questionbank/favorite", { type, id });
  },

  /**
   * 取消收藏
   * @param {String} type - 'paper' 或 'question'
   * @param {Number} id - ID
   */
  removeFavorite(type, id) {
    return request.delete("/api/questionbank/favorite", { data: { type, id } });
  },

  // ==================== 做题窗口 API ====================

  /**
   * 获取试卷详情（考试模式）
   * @param {Number} paperId - 试卷ID
   */
  getExamPaper(paperId) {
    return request.get(`/api/questionbank/exam/${paperId}`);
  },

  /**
   * 获取题目详情（单题模式）
   * @param {Number} questionId - 题目ID
   */
  getQuestion(questionId) {
    return request.get(`/api/questionbank/question/${questionId}`);
  },

  /**
   * 提交答案
   * @param {Object} data - 提交数据
   * @param {String} data.mode - 模式
   * @param {Number} data.paperId - 试卷ID（考试模式）
   * @param {Number} data.questionId - 题目ID（单题模式）
   * @param {Array} data.answers - 答案列表
   */
  submitAnswers(data) {
    return request.post("/api/questionbank/submit", data);
  },

  // ==================== 错题本 API ====================

  /**
   * 获取错题列表
   * @param {Object} filter - 筛选条件
   */
  getWrongQuestions(filter) {
    return request.get("/api/questionbank/wrong", { params: filter });
  },

  // 获取用户生词本
  getUserVocabulary() {
    return request.get("/api/questions/vocabulary/user");
  },

  // 添加到生词本
  addVocabulary(data) {
    return request.post("/api/questions/vocabulary/add", data);
  },

  /**
   * 获取用户总做题数量
   */
  getTotalAnsweredQuestions() {
    return request.get("/api/questionbank/stats/total");
  },
};
