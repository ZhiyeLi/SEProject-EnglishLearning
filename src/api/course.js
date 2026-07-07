/**
 * 课程相关 API
 */
import request from "@/utils/request";

export const courseApi = {
  // 获取课程列表（支持筛选和分页）
  getCourses(params) {
    return request.get("/api/courses", { params });
  },

  // 获取单门课程详情
  getCourseDetail(id) {
    return request.get(`/api/courses/${id}`);
  },

  // 获取用户所有课程的学习进度
  getUserProgress() {
    return request.get("/api/courses/progress");
  },

  // 更新课程学习进度
  updateProgress(courseId, status) {
    return request.put(`/api/courses/${courseId}/progress`, { status });
  },

  // 标记课程完成
  markComplete(courseId) {
    return request.post(`/api/courses/${courseId}/complete`);
  },

  // 收藏/取消收藏（toggle）
  toggleFavorite(courseId) {
    return request.post(`/api/courses/${courseId}/favorite`);
  },

  // 获取收藏的课程 ID 列表
  getFavoriteIds() {
    return request.get("/api/courses/favorites");
  },
};
