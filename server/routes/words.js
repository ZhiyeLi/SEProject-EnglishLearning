const express = require("express");
const router = express.Router();
const wordController = require("../controllers/wordController");
const { authMiddleware } = require("../middleware/auth");

// 所有路由都需要认证
router.use(authMiddleware);

router.get("/types", wordController.getWordTypeList);
router.get("/progress", wordController.getUserWordProgress);
router.get("/list", wordController.getWordsByType);
router.get("/passed", wordController.getPassedWords);
router.get("/selected-type", wordController.getSelectedWordType);
router.get("/today-status", wordController.getTodayCheckInStatus);
router.get("/statistics", wordController.getCheckInStatistics);
router.get("/unpassed", wordController.getUnpassedWords);
router.get("/:wordId", wordController.getWordDetail);

router.post("/mark-passed", wordController.markWordPassed);
router.post("/unmark-passed", wordController.unmarkWordPassed);
router.post("/batch-mark", wordController.batchMarkWordsPassed);
router.post("/set-selected-type", wordController.setSelectedWordType);

router.post("/plan", wordController.createCheckInPlan);
router.get("/plan/detail", wordController.getUserCheckInPlan);
router.put("/plan/status", wordController.updatePlanStatus);

module.exports = router;
