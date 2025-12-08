const { dbRun, dbGet, dbAll } = require("../config/database");
const ResponseUtil = require("../utils/response");

/**
 * 搜索好友
 */
const searchFriend = async (req, res) => {
  try {
    const { name } = req.query;
    const userId = req.userId;

    if (!name) {
      return ResponseUtil.error(res, "搜索关键词不能为空", 400);
    }

    // 搜索用户（排除自己）
    const users = await dbAll(
      `SELECT user_id, user_name, avatar, user_status, signature 
       FROM users 
       WHERE (user_name LIKE ? OR user_email LIKE ?) AND user_id != ?
       LIMIT 20`,
      [`%${name}%`, `%${name}%`, userId]
    );

    return ResponseUtil.success(res, users);
  } catch (error) {
    console.error("搜索好友失败:", error);
    return ResponseUtil.error(res, "搜索好友失败", 500);
  }
};

/**
 * 发送好友请求
 */
const sendFriendRequest = async (req, res) => {
  try {
    const senderId = req.userId;
    const { receiverId } = req.body;

    if (!receiverId) {
      return ResponseUtil.error(res, "接收者ID不能为空", 400);
    }

    if (senderId === receiverId) {
      return ResponseUtil.error(res, "不能添加自己为好友", 400);
    }

    // 检查是否已经是好友
    const friendship = await dbGet(
      "SELECT * FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)",
      [senderId, receiverId, receiverId, senderId]
    );

    if (friendship) {
      return ResponseUtil.success(res, { status: 1 }, "已经是好友");
    }

    // 检查是否已有待处理的请求
    const existingRequest = await dbGet(
      "SELECT * FROM friend_requests WHERE sender_id = ? AND receiver_id = ? AND status = ?",
      [senderId, receiverId, "pending"]
    );

    if (existingRequest) {
      return ResponseUtil.success(res, { status: 2 }, "好友请求已存在");
    }

    // 插入好友请求
    await dbRun(
      "INSERT INTO friend_requests (sender_id, receiver_id) VALUES (?, ?)",
      [senderId, receiverId]
    );

    return ResponseUtil.success(res, { status: 0 }, "好友请求已发送");
  } catch (error) {
    console.error("发送好友请求失败:", error);
    return ResponseUtil.error(res, "发送好友请求失败", 500);
  }
};

/**
 * 获取好友请求列表
 */
const getFriendRequests = async (req, res) => {
  try {
    const userId = req.userId;

    const requests = await dbAll(
      `SELECT 
         fr.request_id,
         fr.sender_id,
         fr.created_at,
         u.user_name,
         u.avatar,
         u.signature
       FROM friend_requests fr
       JOIN users u ON fr.sender_id = u.user_id
       WHERE fr.receiver_id = ? AND fr.status = 'pending'
       ORDER BY fr.created_at DESC`,
      [userId]
    );

    return ResponseUtil.success(res, requests);
  } catch (error) {
    console.error("获取好友请求失败:", error);
    return ResponseUtil.error(res, "获取好友请求失败", 500);
  }
};

/**
 * 接受好友请求
 */
const acceptFriendRequest = async (req, res) => {
  try {
    const userId = req.userId;
    const { requestId } = req.body;

    // 获取请求信息
    const request = await dbGet(
      "SELECT * FROM friend_requests WHERE request_id = ? AND receiver_id = ? AND status = ?",
      [requestId, userId, "pending"]
    );

    if (!request) {
      return ResponseUtil.error(res, "好友请求不存在或已处理", 404);
    }

    // 更新请求状态
    await dbRun("UPDATE friend_requests SET status = ? WHERE request_id = ?", [
      "accepted",
      requestId,
    ]);

    // 添加双向好友关系
    await dbRun(
      "INSERT OR IGNORE INTO friends (user_id, friend_id) VALUES (?, ?)",
      [request.sender_id, request.receiver_id]
    );
    await dbRun(
      "INSERT OR IGNORE INTO friends (user_id, friend_id) VALUES (?, ?)",
      [request.receiver_id, request.sender_id]
    );

    return ResponseUtil.success(res, { success: true }, "已接受好友请求");
  } catch (error) {
    console.error("接受好友请求失败:", error);
    return ResponseUtil.error(res, "接受好友请求失败", 500);
  }
};

/**
 * 拒绝好友请求
 */
const rejectFriendRequest = async (req, res) => {
  try {
    const userId = req.userId;
    const { requestId } = req.body;

    const request = await dbGet(
      "SELECT * FROM friend_requests WHERE request_id = ? AND receiver_id = ? AND status = ?",
      [requestId, userId, "pending"]
    );

    if (!request) {
      return ResponseUtil.error(res, "好友请求不存在或已处理", 404);
    }

    await dbRun("UPDATE friend_requests SET status = ? WHERE request_id = ?", [
      "rejected",
      requestId,
    ]);

    return ResponseUtil.success(res, { success: true }, "已拒绝好友请求");
  } catch (error) {
    console.error("拒绝好友请求失败:", error);
    return ResponseUtil.error(res, "拒绝好友请求失败", 500);
  }
};

/**
 * 获取好友列表
 */
const getFriendList = async (req, res) => {
  try {
    const userId = req.userId;

    const friends = await dbAll(
      `SELECT 
         u.user_id,
         u.user_name,
         u.avatar,
         u.user_status,
         u.signature,
         f.created_at as friend_since
       FROM friends f
       JOIN users u ON f.friend_id = u.user_id
       WHERE f.user_id = ?
       ORDER BY u.user_name`,
      [userId]
    );

    return ResponseUtil.success(res, friends);
  } catch (error) {
    console.error("获取好友列表失败:", error);
    return ResponseUtil.error(res, "获取好友列表失败", 500);
  }
};

/**
 * 发送消息
 */
const sendMessage = async (req, res) => {
  try {
    const senderId = req.userId;
    const { receiverId, content } = req.body;

    if (!receiverId || !content) {
      return ResponseUtil.error(res, "接收者和消息内容不能为空", 400);
    }

    // 验证是否为好友
    const friendship = await dbGet(
      "SELECT * FROM friends WHERE user_id = ? AND friend_id = ?",
      [senderId, receiverId]
    );

    if (!friendship) {
      return ResponseUtil.error(res, "只能向好友发送消息", 403);
    }

    const result = await dbRun(
      "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)",
      [senderId, receiverId, content]
    );

    const message = await dbGet("SELECT * FROM messages WHERE message_id = ?", [
      result.lastID,
    ]);

    return ResponseUtil.success(res, message, "消息发送成功");
  } catch (error) {
    console.error("发送消息失败:", error);
    return ResponseUtil.error(res, "发送消息失败", 500);
  }
};

/**
 * 获取聊天记录
 */
const getMessageList = async (req, res) => {
  try {
    const userId = req.userId;
    const { friendId } = req.query;

    if (!friendId) {
      return ResponseUtil.error(res, "好友ID不能为空", 400);
    }

    const messages = await dbAll(
      `SELECT * FROM messages 
       WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)
       ORDER BY sent_at ASC`,
      [userId, friendId, friendId, userId]
    );

    // 标记消息为已读
    await dbRun(
      "UPDATE messages SET if_read = 1 WHERE sender_id = ? AND receiver_id = ? AND if_read = 0",
      [friendId, userId]
    );

    return ResponseUtil.success(res, messages);
  } catch (error) {
    console.error("获取聊天记录失败:", error);
    return ResponseUtil.error(res, "获取聊天记录失败", 500);
  }
};

/**
 * 获取未读消息数
 */
const getUnreadCount = async (req, res) => {
  try {
    const userId = req.userId;

    const result = await dbGet(
      "SELECT COUNT(*) as count FROM messages WHERE receiver_id = ? AND if_read = 0",
      [userId]
    );

    return ResponseUtil.success(res, { unreadCount: result.count });
  } catch (error) {
    console.error("获取未读消息数失败:", error);
    return ResponseUtil.error(res, "获取未读消息数失败", 500);
  }
};

module.exports = {
  searchFriend,
  sendFriendRequest,
  getFriendRequests,
  acceptFriendRequest,
  rejectFriendRequest,
  getFriendList,
  sendMessage,
  getMessageList,
  getUnreadCount,
};
