const express = require("express");
const router = express.Router();
const friendController = require("../controllers/friendController");
const { authMiddleware } = require("../middleware/auth");

// 所有路由都需要认证
router.use(authMiddleware);

// 好友相关
router.get("/search", friendController.searchFriend);
router.post("/request", friendController.sendFriendRequest);
router.get("/requests", friendController.getFriendRequests);
router.post("/accept", friendController.acceptFriendRequest);
router.post("/reject", friendController.rejectFriendRequest);
router.get("/list", friendController.getFriendList);

// 消息相关
router.post("/message", friendController.sendMessage);
router.get("/messages", friendController.getMessageList);
router.get("/unread-count", friendController.getUnreadCount);

module.exports = router;
