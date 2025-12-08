/**
 * 题目相关 API
 */
import request from "@/utils/request";

export const questionApi = {
  // 获取题目列表
  getQuestions(params) {
    return request.get("/api/questions", { params });
  },

  // 获取题目详情
  getQuestionDetail(id) {
    return request.get(`/api/questions/${id}`);
  },

  // 提交答案
  submitAnswer(id, data) {
    return request.post(`/api/questions/${id}/submit`, data);
  },

  // 切换收藏状态
  toggleFavorite(id) {
    return request.post(`/api/questions/${id}/favorite`);
  },

  // 获取统计信息
  getStatistics() {
    return request.get("/api/questions/statistics");
  },

  // 获取错题列表
  getWrongQuestions() {
    return request.get("/api/questions/wrong/list");
  },

  // 标记错题已掌握
  markWrongQuestionMastered(id) {
    return request.post(`/api/questions/wrong/${id}/master`);
  },

  // 获取课程列表
  getCourses() {
    return request.get("/api/questions/courses/list");
  },

  // 获取课程题目
  getCourseQuestions(courseId) {
    return request.get(`/api/questions/courses/${courseId}/questions`);
  },

  // 获取用户生词本
  getUserVocabulary() {
    return request.get("/api/questions/vocabulary/user");
  },

  // 添加到生词本
  addVocabulary(data) {
    return request.post("/api/questions/vocabulary/add", data);
  },
};
