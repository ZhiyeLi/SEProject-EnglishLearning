/**
 * 计划相关 API
 */
import request from "@/utils/request";

export const planApi = {
  // 获取所有计划
  getPlans() {
    return request.get("/api/plans");
  },

  // 获取今日计划
  getTodayPlans() {
    return request.get("/api/plans/today");
  },

  // 获取指定日期的计划
  getPlansByDate(date) {
    return request.get(`/api/plans/${date}`);
  },

  // 获取第一个计划日期
  getFirstPlanDate() {
    return request.get("/api/plans/first-plan-date");
  },

  // 获取计划统计信息
  getPlanStatistics() {
    return request.get("/api/plans/statistics");
  },

  // 创建计划
  createPlan(data) {
    return request.post("/api/plans", data);
  },

  // 更新计划
  updatePlan(id, data) {
    return request.put(`/api/plans/${id}`, data);
  },

  // 切换计划完成状态
  togglePlanComplete(id) {
    return request.put(`/api/plans/${id}/complete`);
  },

  // 删除计划
  deletePlan(id) {
    return request.delete(`/api/plans/${id}`);
  },

  // 批量删除计划
  batchDeletePlans(ids) {
    return request.delete("/api/plans/batch", { data: { ids } });
  },
};
