/**
 * API 客户端工具类
 * 用于与后端 REST API 通信
 * 
 * 使用示例：
 *   import { apiClient } from '@/api/client'
 *   const words = await apiClient.getWords(20, 0)
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:3000/api';

/**
 * 通用请求方法
 */
async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  
  try {
    const response = await fetch(url, {
      method: options.method || 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      body: options.body ? JSON.stringify(options.body) : undefined,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ error: `HTTP ${response.status}` }));
      throw new Error(error.error || `Request failed: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error(`API Error [${endpoint}]:`, error.message);
    throw error;
  }
}

/**
 * API 客户端对象
 */
export const apiClient = {
  /**
   * 系统接口
   */
  
  /**
   * 健康检查
   * @returns {Promise<{status: string, timestamp: string}>}
   */
  healthCheck() {
    return request('/health');
  },

  /**
   * 单词相关接口
   */

  /**
   * 获取单词列表（分页）
   * @param {number} limit - 返回条数，默认 100，最多 500
   * @param {number} offset - 起始位置，默认 0
   * @returns {Promise<{data: Array, count: number, limit: number, offset: number}>}
   */
  getWords(limit = 100, offset = 0) {
    return request(`/words?limit=${limit}&offset=${offset}`);
  },

  /**
   * 获取单词详情
   * @param {number} wordId - 单词 ID
   * @returns {Promise<{word: Object, pos_list: Array, translations: Array}>}
   */
  getWordDetail(wordId) {
    return request(`/words/${wordId}`);
  },

  /**
   * 用户相关接口
   */

  /**
   * 获取用户信息
   * @param {number} userId - 用户 ID
   * @returns {Promise<{user_id: number, username: string, nickname: string, email: string, ...}>}
   */
  getUser(userId) {
    return request(`/users/${userId}`);
  },

  /**
   * 创建新用户
   * @param {Object} userData - 用户数据
   *   - username: string (必需)
   *   - password: string (必需)
   *   - nickname: string (必需)
   *   - email: string (必需)
   *   - phone: string (可选)
   *   - avatar: string (可选)
   * @returns {Promise<{user_id: number, username: string, nickname: string, email: string}>}
   */
  createUser(userData) {
    return request('/users', {
      method: 'POST',
      body: userData,
    });
  },

  /**
   * 获取用户加入的学习小组
   * @param {number} userId - 用户 ID
   * @returns {Promise<{data: Array, count: number}>}
   */
  getUserGroups(userId) {
    return request(`/users/${userId}/groups`);
  },

  /**
   * 单词记忆相关接口
   */

  /**
   * 获取用户的单词记忆状态
   * @param {number} userId - 用户 ID
   * @returns {Promise<{data: Array, count: number}>}
   */
  getUserWordMemory(userId) {
    return request(`/users/${userId}/words/memory`);
  },

  /**
   * 更新用户单词记忆状态
   * @param {number} userId - 用户 ID
   * @param {number} wordId - 单词 ID
   * @param {boolean} isRemembered - 是否已记住
   * @returns {Promise<{success: boolean, message: string}>}
   */
  updateWordMemory(userId, wordId, isRemembered) {
    return request(`/users/${userId}/words/${wordId}/remember`, {
      method: 'POST',
      body: { is_remembered: isRemembered },
    });
  },

  /**
   * 批量标记单词记忆（实用工具方法）
   * @param {number} userId - 用户 ID
   * @param {Array<{wordId: number, isRemembered: boolean}>} updates - 更新列表
   * @returns {Promise<Array>} 所有请求的结果
   */
  async updateMultipleWords(userId, updates) {
    return Promise.all(
      updates.map(({ wordId, isRemembered }) =>
        this.updateWordMemory(userId, wordId, isRemembered)
      )
    );
  },

  /**
   * 工具方法
   */

  /**
   * 获取单词详情列表（一次性获取多个单词）
   * @param {Array<number>} wordIds - 单词 ID 数组
   * @returns {Promise<Array>}
   */
  async getWordDetails(wordIds) {
    return Promise.all(
      wordIds.map(id => this.getWordDetail(id).catch(() => null))
    );
  },

  /**
   * 搜索单词（模拟客户端搜索，可后续添加后端搜索端点）
   * @param {string} keyword - 搜索关键词
   * @param {number} limit - 返回条数
   * @returns {Promise<Array>}
   */
  async searchWords(keyword, limit = 50) {
    if (!keyword || keyword.length === 0) return [];
    
    const data = await this.getWords(limit, 0);
    return data.data.filter(word =>
      word.word_content.toLowerCase().includes(keyword.toLowerCase())
    );
  },

  /**
   * 设置 API 基础 URL（用于多环境）
   * @param {string} url - 新的 API 基础 URL
   */
  setBaseUrl(url) {
    // 无法修改常量，但可在 .env 中配置 VITE_API_URL
    console.warn('请通过 .env 文件配置 VITE_API_URL，或在调用时手动构建 URL');
  },
};

/**
 * 错误处理包装器
 */
export function withErrorHandling(asyncFn) {
  return async (...args) => {
    try {
      return await asyncFn(...args);
    } catch (error) {
      console.error('API Call Error:', error);
      throw error;
    }
  };
}

/**
 * 数据转换工具
 */
export const dataTransformers = {
  /**
   * 将 API 单词数据转换为前端格式
   */
  transformWord(apiWord) {
    return {
      id: apiWord.word_id,
      content: apiWord.word_content,
      ...apiWord,
    };
  },

  /**
   * 将 API 用户数据转换为前端格式
   */
  transformUser(apiUser) {
    return {
      id: apiUser.user_id,
      name: apiUser.nickname,
      progress: apiUser.progress || { total: 0, completed: 0 },
      ...apiUser,
    };
  },

  /**
   * 将多个单词转换
   */
  transformWords(apiWords) {
    return apiWords.map(word => this.transformWord(word));
  },
};

/**
 * Mock 数据（开发时离线使用）
 */
export const mockData = {
  words: [
    { word_id: 1, word_content: 'abandon' },
    { word_id: 2, word_content: 'ability' },
    { word_id: 3, word_content: 'absence' },
  ],
  user: {
    user_id: 1,
    username: 'testuser',
    nickname: 'Test User',
    email: 'test@example.com',
    progress: { total: 100, completed: 45 },
  },
};

/**
 * 离线模式（开发时测试）
 */
export function enableMockMode() {
  console.warn('⚠️ Mock mode enabled - using sample data instead of API');
  
  return {
    ...apiClient,
    getWords: () => Promise.resolve({
      data: mockData.words,
      count: mockData.words.length,
      limit: 100,
      offset: 0,
    }),
    getUser: () => Promise.resolve(mockData.user),
  };
}

export default apiClient;
