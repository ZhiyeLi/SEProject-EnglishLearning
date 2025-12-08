const express = require("express");
const router = express.Router();
const planController = require("../controllers/planController");
const { authMiddleware } = require("../middleware/auth");

// 所有路由都需要认证
router.use(authMiddleware);

router.get("/", planController.getPlans);
router.get("/today", planController.getTodayPlans);
router.get("/first-plan-date", planController.getFirstPlanDate);
router.get("/statistics", planController.getPlanStatistics);
router.get("/:date", planController.getPlansByDate);
router.post("/", planController.createPlan);
router.put("/:id", planController.updatePlan);
router.put("/:id/complete", planController.togglePlanComplete);
router.delete("/:id", planController.deletePlan);
router.delete("/batch", planController.batchDeletePlans);

module.exports = router;
