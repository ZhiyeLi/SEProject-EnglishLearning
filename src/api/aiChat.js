/**
 * AI聊天相关 API
 */
import request from "@/utils/request";

export const aiChatApi = {
  // 获取用户的所有对话会话列表
  getSessions() {
    return request.get("/api/ai-chat/sessions");
  },

  // 创建新的对话会话
  createSession(title = null) {
    return request.post("/api/ai-chat/sessions", { title });
  },

  // 获取指定对话会话的所有消息
  getSessionMessages(sessionId) {
    return request.get(`/api/ai-chat/sessions/${sessionId}/messages`);
  },

  // 保存一条消息到指定的对话会话
  saveMessage(sessionId, role, content) {
    return request.post(`/api/ai-chat/sessions/${sessionId}/messages`, {
      role,
      content,
    });
  },

  // 更新对话会话标题
  updateSessionTitle(sessionId, title) {
    return request.put(`/api/ai-chat/sessions/${sessionId}`, { title });
  },

  // 删除对话会话
  deleteSession(sessionId) {
    return request.delete(`/api/ai-chat/sessions/${sessionId}`);
  },
};
