const express = require("express");
const router = express.Router();
const authController = require("../controllers/authController");
const { authMiddleware } = require("../middleware/auth");

// 公开路由（无需认证）
router.post("/register", authController.register);
router.post("/login", authController.login);
router.post("/send-verify-code", authController.sendVerifyCode);
router.post("/reset-password", authController.resetPassword);

// 需要认证的路由
router.get("/user", authMiddleware, authController.getCurrentUser);
router.put("/user", authMiddleware, authController.updateUserInfo);
router.post("/change-password", authMiddleware, authController.changePassword);

module.exports = router;
