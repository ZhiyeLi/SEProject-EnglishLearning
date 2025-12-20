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

// 获取好友周排行榜
exports.getFriendWeeklyRanking = async (req, res) => {
  try {
    // 1. 从JWT中间件获取当前用户ID（req.user.id 是authMiddleware解析后的值）
    const currentUserId = req.user.id;

    // 2. 计算本周时间范围（周一00:00 至 周日23:59:59）
    const { monday, sunday } = getWeekTimeRange();

    // 3. 查询当前用户的已通过好友ID列表（status=1）
    const [friends] = await db.query(`
      SELECT friend_id FROM friends 
      WHERE user_id = ?
    `, [currentUserId]);

    if (friends.length === 0) {
      return res.json({
        code: 200,
        message: 'success',
        data: []
      });
    }

    // 4. 遍历好友，统计每个好友本周学习的去重单词数
    const rankingList = [];
    for (const friend of friends) {
      const friendId = friend.friend_id;

      // 4.1 查询好友基础信息
      const [user] = await db.query(`
        SELECT userid, user_name, avatar FROM users WHERE user_id = ?
      `, [friendId]);
      if (user.length === 0) continue;
      const friendUser = user[0];

      // 4.2 统计本周去重单词数
      const [wordCount] = await db.query(`
        SELECT COUNT(DISTINCT word_id) as total FROM user_word_progress 
        WHERE user_id = ? AND last_review_time BETWEEN ? AND ?
      `, [friendId, monday, sunday]);
      const totalWords = wordCount[0].total || 0;

      // 4.3 封装数据
      rankingList.push({
        userId: friendUser.id,
        avatar: friendUser.avatar || 'https://picsum.photos/seed/default/100/100', // 前端默认值对齐
        username: friendUser.username,
        totalWords: totalWords
      });
    }

    // 5. 按单词数降序排序（总数相同按用户名升序）
    rankingList.sort((a, b) => {
      if (b.totalWords === a.totalWords) {
        return a.username.localeCompare(b.username);
      }
      return b.totalWords - a.totalWords;
    });

    // 6. 返回结果
    res.json({
      code: 200,
      message: 'success',
      data: rankingList
    });

  } catch (error) {
    console.error('获取好友排行榜失败：', error);
    res.status(500).json({
      code: 500,
      message: '服务器内部错误',
      data: []
    });
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
