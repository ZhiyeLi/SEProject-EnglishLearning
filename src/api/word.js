/**
 * 单词相关 API
 */
import request from "@/utils/request";

export const wordApi = {
  // 获取单词类型列表
  getWordTypeList() {
    return request.get("/api/words/types");
  },

  // 获取用户单词学习进度
  getUserWordProgress() {
    return request.get("/api/words/progress");
  },

  // 获取指定类型的单词列表
  getWordsByType(params) {
    return request.get("/api/words/list", { params });
  },

  // 获取已掌握的单词
  getPassedWords() {
    return request.get("/api/words/passed");
  },

  // 获取用户选择的单词类型
  getSelectedWordType() {
    return request.get("/api/words/selected-type");
  },

  // 获取今日打卡状态
  getTodayCheckInStatus() {
    return request.get("/api/words/today-status");
  },

  // 获取打卡统计
  getCheckInStatistics() {
    return request.get("/api/words/statistics");
  },

  // 获取单词详情
  getWordDetail(wordId) {
    return request.get(`/api/words/${wordId}`);
  },

  // 标记单词已掌握
  markWordPassed(data) {
    return request.post("/api/words/mark-passed", data);
  },

  // 取消标记单词已掌握
  unmarkWordPassed(data) {
    return request.post("/api/words/unmark-passed", data);
  },

  // 批量标记单词已掌握
  batchMarkWordsPassed(data) {
    return request.post("/api/words/batch-mark", data);
  },

  // 设置选中的单词类型
  setSelectedWordType(data) {
    return request.post("/api/words/set-selected-type", data);
  },

  // 创建打卡计划
  createCheckInPlan(data) {
    return request.post("/api/words/plan", data);
  },

  // 获取用户打卡计划详情
  getUserCheckInPlan(typeId) {
    const params = {};
    if (typeId) {
      params.typeId = typeId;
    }
    return request.get("/api/words/plan/detail", { params });
  },

  // 更新打卡计划状态
  updatePlanStatus(data) {
    return request.put("/api/words/plan/status", data);
  },

  // 获取未打卡的单词列表
  getUnpassedWords(params) {
    return request.get("/api/words/unpassed", { params });
  },

  // 获取连续打卡天数
  getConsecutiveCheckInDays() {
    return request.get("/api/words/consecutive-days");
  },

  // 获取总单词量
  getTotalLearnedWords() {
    return request.get("/api/words/total-learned");
  },
};
