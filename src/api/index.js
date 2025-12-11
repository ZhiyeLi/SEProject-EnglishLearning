// src/api/index.js
import request from '@/utils/request';

// 用户相关API
export const userApi = {
  login: (data) => request.post('/api/auth/login', data),  // 增加/api前缀
  logout: () => request.post('/api/user/logout'),
  register: (data) => request.post('/api/auth/register', data),  // 添加注册API
  getUserInfo: () => request.get('/api/user/info'),
  updateUserInfo: (data) => request.put('/api/user/info', data),
  updatePassword: (data) => request.put('/api/user/password', data),
  sendVerifyCode: (account) => request.post('/api/auth/verify-code', { account }),
  resetPassword: (data) => request.post('/api/auth/reset-password', data)
};
// 计划相关API
export const planApi = {
  getPlans: () => request.get('/plans'),
  getPlansByDate: (date) => request.get(`/plans?date=${date}`),
  addPlan: (data) => request.post('/plans', data),
  updatePlan: (id, data) => request.put(`/plans/${id}`, data),
  deletePlan: (id) => request.delete(`/plans/${id}`),
  saveDayPlans: (date, data) => request.post(`/plans/batch?date=${date}`, data)
};

// 好友相关API
export const friendApi = {
  getFriends: () => request.get('/friends'),
  searchFriends: (keyword) => request.get(`/friends/search?keyword=${keyword}`),
  addFriend: (friendId) => request.post('/friends/request', { friendId }),
  acceptFriendRequest: (requestId) => request.post(`/friends/request/${requestId}/accept`),
  rejectFriendRequest: (requestId) => request.post(`/friends/request/${requestId}/reject`),
  getFriendRequests: () => request.get('/friends/requests')
};

// 其他API...