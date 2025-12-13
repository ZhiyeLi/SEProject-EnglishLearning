/**
 * 用户设置 API 调用模块
 */

import request from '@/utils/request'

/**
 * 获取用户设置
 */
export function getSettings() {
  return request.get('/api/settings/get')
}

/**
 * 更新用户设置
 * @param {Object} data 设置数据
 */
export function updateSettings(data) {
  return request.put('/api/settings/update', data)
}

/**
 * 重置用户设置为默认值
 */
export function resetSettings() {
  return request.post('/api/settings/reset')
}

export const settingsApi = {
  getSettings,
  updateSettings,
  resetSettings,
}

export default {
  getSettings,
  updateSettings,
  resetSettings,
}
