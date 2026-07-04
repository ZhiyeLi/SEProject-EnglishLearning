import request from '../utils/request';

/**
 * 发送消息给 RAG 智能助教
 * @param {string} message 用户问题
 * @returns {Promise} 响应 promise
 */
export function sendRagMessage(message) {
  return request({
    url: '/api/rag/rag_chat',
    method: 'post',
    timeout: 120000, // RAG 回复较慢，设置 2 分钟超时
    data: {
      message
    }
  });
}
