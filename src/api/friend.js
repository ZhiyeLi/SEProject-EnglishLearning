/**
 * 好友相关 API
 */
import request from "@/utils/request";

export const friendApi = {
  // 搜索添加好友
  searchFriend(params) {
    return request.get("/api/friends/search", { params });
  },

  // 发送好友请求
  sendFriendRequest(data) {
    return request.post("/api/friends/request", data);
  },

  // 获取好友请求列表
  getFriendRequests() {
    return request.get("/api/friends/requests");
  },

  // 接受好友请求
  acceptFriendRequest(requestId) {
    return request.post("/api/friends/accept", { "requestId": requestId });
  },

  // 拒绝好友请求
  rejectFriendRequest(requestId) {
    return request.post("/api/friends/reject", { "requestId": requestId });
  },

  // 获取好友列表
  getFriendList() {
    return request.get("/api/friends/list");
  },

  // 发送消息
  sendMessage(data) {
    return request.post("/api/friends/message", data);
  },

  // 获取消息列表
  getMessageList(params) {
    return request.get("/api/friends/messages", { params });
  },

  // 获取未读消息数
  getUnreadCount() {
    return request.get("/api/friends/unread-count");
  },

  // 标记消息为已读
  markMessagesAsRead(data) {
    return request.post("/api/friends/mark-as-read", data);
  },
};
