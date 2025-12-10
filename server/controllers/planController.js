const { dbRun, dbGet, dbAll } = require("../config/database");
const ResponseUtil = require("../utils/response");

/**
 * 将数据库的计划记录转换为前端期望的格式
 * 主要是将 if_completed、start_time、end_time 等字段名转换
 */
function formatPlan(plan) {
  if (!plan) return null;
  return {
    id: plan.id,
    userId: plan.user_id,
    date: plan.date,
    title: plan.title,
    description: plan.description,
    category: plan.category,
    priority: plan.priority,
    startTime: plan.start_time,
    endTime: plan.end_time,
    completed: plan.if_completed === 1,
    createdAt: plan.created_at,
    updatedAt: plan.updated_at,
  };
}

/**
 * 将多个计划记录转换为前端格式
 */
function formatPlans(plans) {
  return plans.map(formatPlan);
}

/**
 * 获取指定时间范围内的计划
 * 如果不传日期参数,返回所有计划
 */
const getPlans = async (req, res) => {
  try {
    const { startDate, endDate } = req.query;
    const userId = req.userId;

    let plans;

    if (!startDate || !endDate) {
      // 如果没有传日期参数,返回所有计划
      plans = await dbAll(
        `SELECT * FROM plans 
         WHERE user_id = ?
         ORDER BY date DESC, 
           if_completed ASC,
           CASE priority 
             WHEN 'high' THEN 1 
             WHEN 'medium' THEN 2 
             WHEN 'low' THEN 3 
           END,
           start_time ASC`,
        [userId]
      );
    } else {
      // 有日期参数,返回指定范围的计划
      plans = await dbAll(
        `SELECT * FROM plans 
         WHERE user_id = ? AND date BETWEEN ? AND ?
         ORDER BY date DESC,
           if_completed ASC,
           CASE priority 
             WHEN 'high' THEN 1 
             WHEN 'medium' THEN 2 
             WHEN 'low' THEN 3 
           END,
           start_time ASC`,
        [userId, startDate, endDate]
      );
    }

    return ResponseUtil.success(res, formatPlans(plans));
  } catch (error) {
    console.error("获取计划失败:", error);
    return ResponseUtil.error(res, "获取计划失败", 500);
  }
};

/**
 * 获取今日计划
 */
const getTodayPlans = async (req, res) => {
  try {
    const userId = req.userId;
    const today = new Date().toISOString().split("T")[0];

    const plans = await dbAll(
      `SELECT * FROM plans 
       WHERE user_id = ? AND date = ?
       ORDER BY 
         if_completed ASC,
         CASE priority 
           WHEN 'high' THEN 1 
           WHEN 'medium' THEN 2 
           WHEN 'low' THEN 3 
         END,
         start_time ASC`,
      [userId, today]
    );

    return ResponseUtil.success(res, formatPlans(plans));
  } catch (error) {
    console.error("获取今日计划失败:", error);
    return ResponseUtil.error(res, "获取今日计划失败", 500);
  }
};

/**
 * 获取指定日期的计划
 */
const getPlansByDate = async (req, res) => {
  try {
    const { date } = req.params;
    const userId = req.userId;

    const plans = await dbAll(
      `SELECT * FROM plans 
       WHERE user_id = ? AND date = ?
       ORDER BY 
         if_completed ASC,
         CASE priority 
           WHEN 'high' THEN 1 
           WHEN 'medium' THEN 2 
           WHEN 'low' THEN 3 
         END,
         start_time ASC`,
      [userId, date]
    );

    return ResponseUtil.success(res, formatPlans(plans));
  } catch (error) {
    console.error("获取指定日期计划失败:", error);
    return ResponseUtil.error(res, "获取指定日期计划失败", 500);
  }
};

/**
 * 创建新计划
 */
const createPlan = async (req, res) => {
  try {
    const userId = req.userId;
    const { date, title, description, category, priority, startTime, endTime } =
      req.body;

    if (!date || !title) {
      return ResponseUtil.error(res, "日期和标题不能为空", 400);
    }

    // 检查同一天是否已有相同标题的计划
    const existing = await dbGet(
      "SELECT id FROM plans WHERE user_id = ? AND date = ? AND LOWER(title) = LOWER(?)",
      [userId, date, title]
    );

    if (existing) {
      return ResponseUtil.error(res, "该日期已存在相同标题的计划", 400);
    }

    const result = await dbRun(
      `INSERT INTO plans (user_id, date, title, description, category, priority, start_time, end_time)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        userId,
        date,
        title,
        description || "",
        category || "其他",
        priority || "medium",
        startTime,
        endTime,
      ]
    );

    const plan = await dbGet("SELECT * FROM plans WHERE id = ?", [
      result.lastID,
    ]);

    return ResponseUtil.success(res, formatPlan(plan), "计划创建成功");
  } catch (error) {
    console.error("创建计划失败:", error);
    return ResponseUtil.error(res, "创建计划失败", 500);
  }
};

/**
 * 更新计划
 */
const updatePlan = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.userId;
    const {
      title,
      description,
      category,
      priority,
      startTime,
      endTime,
      completed,
    } = req.body;

    // 验证计划是否属于当前用户
    const plan = await dbGet(
      "SELECT * FROM plans WHERE id = ? AND user_id = ?",
      [id, userId]
    );

    if (!plan) {
      return ResponseUtil.error(res, "计划不存在或无权限", 404);
    }

    const updates = [];
    const params = [];

    if (title !== undefined) {
      // 检查标题唯一性
      const existing = await dbGet(
        "SELECT id FROM plans WHERE user_id = ? AND date = ? AND LOWER(title) = LOWER(?) AND id != ?",
        [userId, plan.date, title, id]
      );
      if (existing) {
        return ResponseUtil.error(res, "该日期已存在相同标题的计划", 400);
      }
      updates.push("title = ?");
      params.push(title);
    }
    if (description !== undefined) {
      updates.push("description = ?");
      params.push(description);
    }
    if (category !== undefined) {
      updates.push("category = ?");
      params.push(category);
    }
    if (priority !== undefined) {
      updates.push("priority = ?");
      params.push(priority);
    }
    if (startTime !== undefined) {
      updates.push("start_time = ?");
      params.push(startTime);
    }
    if (endTime !== undefined) {
      updates.push("end_time = ?");
      params.push(endTime);
    }
    if (completed !== undefined) {
      updates.push("if_completed = ?");
      params.push(completed ? 1 : 0);
    }

    if (updates.length === 0) {
      return ResponseUtil.error(res, "没有要更新的字段", 400);
    }

    updates.push("updated_at = CURRENT_TIMESTAMP");
    params.push(id, userId);

    await dbRun(
      `UPDATE plans SET ${updates.join(", ")} WHERE id = ? AND user_id = ?`,
      params
    );

    const updatedPlan = await dbGet("SELECT * FROM plans WHERE id = ?", [id]);

    return ResponseUtil.success(res, formatPlan(updatedPlan), "计划更新成功");
  } catch (error) {
    console.error("更新计划失败:", error);
    return ResponseUtil.error(res, "更新计划失败", 500);
  }
};

/**
 * 切换计划完成状态
 */
const togglePlanComplete = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.userId;

    const plan = await dbGet(
      "SELECT * FROM plans WHERE id = ? AND user_id = ?",
      [id, userId]
    );

    if (!plan) {
      return ResponseUtil.error(res, "计划不存在或无权限", 404);
    }

    // 切换完成状态（如果当前是完成则变成未完成，反之亦然）
    const newCompleted = plan.if_completed === 1 ? 0 : 1;

    await dbRun(
      "UPDATE plans SET if_completed = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
      [newCompleted, id]
    );

    // 获取更新后的计划并返回
    const updatedPlan = await dbGet("SELECT * FROM plans WHERE id = ?", [id]);

    return ResponseUtil.success(res, formatPlan(updatedPlan), "状态更新成功");
  } catch (error) {
    console.error("切换计划状态失败:", error);
    return ResponseUtil.error(res, "切换计划状态失败", 500);
  }
};

/**
 * 删除计划
 */
const deletePlan = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.userId;

    const plan = await dbGet(
      "SELECT id FROM plans WHERE id = ? AND user_id = ?",
      [id, userId]
    );

    if (!plan) {
      return ResponseUtil.error(res, "计划不存在或无权限", 404);
    }

    await dbRun("DELETE FROM plans WHERE id = ?", [id]);

    return ResponseUtil.success(res, { success: true }, "计划删除成功");
  } catch (error) {
    console.error("删除计划失败:", error);
    return ResponseUtil.error(res, "删除计划失败", 500);
  }
};

/**
 * 批量删除计划
 */
const batchDeletePlans = async (req, res) => {
  try {
    const userId = req.userId;
    const { date, ids } = req.body;

    let deletedCount = 0;

    if (ids && ids.length > 0) {
      // 删除指定ID的计划
      const placeholders = ids.map(() => "?").join(",");
      const result = await dbRun(
        `DELETE FROM plans WHERE user_id = ? AND id IN (${placeholders})`,
        [userId, ...ids]
      );
      deletedCount = result.changes;
    } else if (date) {
      // 删除指定日期的所有计划
      const result = await dbRun(
        "DELETE FROM plans WHERE user_id = ? AND date = ?",
        [userId, date]
      );
      deletedCount = result.changes;
    } else {
      return ResponseUtil.error(res, "必须提供日期或ID列表", 400);
    }

    return ResponseUtil.success(
      res,
      { success: true, deletedCount },
      "批量删除成功"
    );
  } catch (error) {
    console.error("批量删除计划失败:", error);
    return ResponseUtil.error(res, "批量删除计划失败", 500);
  }
};

/**
 * 获取第一次添加计划的日期
 */
const getFirstPlanDate = async (req, res) => {
  try {
    const userId = req.userId;

    const result = await dbGet(
      "SELECT MIN(date) as firstDate FROM plans WHERE user_id = ?",
      [userId]
    );

    return ResponseUtil.success(res, { firstDate: result?.firstDate || null });
  } catch (error) {
    console.error("获取首次计划日期失败:", error);
    return ResponseUtil.error(res, "获取首次计划日期失败", 500);
  }
};

/**
 * 获取计划统计信息
 */
const getPlanStatistics = async (req, res) => {
  try {
    const userId = req.userId;
    const { startDate, endDate } = req.query;

    let dateFilter = "";
    let params = [userId];

    if (startDate && endDate) {
      dateFilter = "AND date BETWEEN ? AND ?";
      params.push(startDate, endDate);
    }

    // 总计划数和已完成数
    const stats = await dbGet(
      `SELECT 
         COUNT(*) as totalPlans,
         SUM(CASE WHEN if_completed = 1 THEN 1 ELSE 0 END) as completedPlans
       FROM plans 
       WHERE user_id = ? ${dateFilter}`,
      params
    );

    // 各分类统计
    const categoryStats = await dbAll(
      `SELECT 
         category,
         COUNT(*) as count,
         SUM(CASE WHEN if_completed = 1 THEN 1 ELSE 0 END) as completed
       FROM plans 
       WHERE user_id = ? ${dateFilter}
       GROUP BY category`,
      params
    );

    const completionRate =
      stats.totalPlans > 0
        ? (stats.completedPlans / stats.totalPlans) * 100
        : 0;

    return ResponseUtil.success(res, {
      totalPlans: stats.totalPlans,
      completedPlans: stats.completedPlans,
      completionRate: Math.round(completionRate * 100) / 100,
      categoryStats,
    });
  } catch (error) {
    console.error("获取计划统计失败:", error);
    return ResponseUtil.error(res, "获取计划统计失败", 500);
  }
};

module.exports = {
  getPlans,
  getTodayPlans,
  getPlansByDate,
  createPlan,
  updatePlan,
  togglePlanComplete,
  deletePlan,
  batchDeletePlans,
  getFirstPlanDate,
  getPlanStatistics,
};
