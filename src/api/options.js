/**
 * 设置选项 API - 获取各种设置的可选项
 */
import request from '@/utils/request';

export const optionsApi = {
  // 获取所有设置选项
  getAllOptions() {
    return request.get('/api/options/all');
  },

  // 获取词汇难度选项
  getVocabularyDifficultyOptions() {
    return request.get('/api/options/vocabulary-difficulty');
  },

  // 获取复习策略选项
  getReviewStrategyOptions() {
    return request.get('/api/options/review-strategy');
  },

  // 获取个人资料可见范围选项
  getProfileVisibilityOptions() {
    return request.get('/api/options/profile-visibility');
  },

  // 获取好友请求管理选项
  getFriendRequestModeOptions() {
    return request.get('/api/options/friend-request-mode');
  },

  // 获取语言选项
  getLanguageOptions() {
    return request.get('/api/options/language');
  },

  // 获取字体大小选项
  getFontSizeOptions() {
    return request.get('/api/options/font-size');
  },
};
