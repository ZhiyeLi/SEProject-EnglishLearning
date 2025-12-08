const express = require("express");
const router = express.Router();
const questionController = require("../controllers/questionController");
const { authMiddleware } = require("../middleware/auth");

// 所有路由都需要认证
router.use(authMiddleware);

// 题目相关
router.get("/", questionController.getQuestions);
router.get("/:id", questionController.getQuestionDetail);
router.post("/:id/submit", questionController.submitAnswer);
router.post("/:id/favorite", questionController.toggleFavorite);

// 统计相关
router.get("/statistics", questionController.getStatistics);

// 错题相关
router.get("/wrong/list", questionController.getWrongQuestions);
router.post("/wrong/:id/master", questionController.markWrongQuestionMastered);

// 课程相关
router.get("/courses/list", questionController.getCourses);
router.get("/courses/:id/questions", questionController.getCourseQuestions);

// 生词本相关
router.get("/vocabulary/user", questionController.getUserVocabulary);
router.post("/vocabulary/add", questionController.addVocabulary);

module.exports = router;
