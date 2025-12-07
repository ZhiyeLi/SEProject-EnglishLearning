// src/api/index.js
import request from '@/utils/request';

// 用户相关API
export const userApi = {
  login: (data) => request.post('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  getUserInfo: () => request.get('/user/info'),
  updateUserInfo: (data) => request.put('/user/info', data),
  updatePassword: (data) => request.put('/user/password', data),
  sendVerifyCode: (data) => request.post('/auth/verify-code', data),
  resetPassword: (data) => request.post('/auth/reset-password', data)
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