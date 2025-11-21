/**
 * 计划管理工具类
 * 用于管理学习计划的本地数据和API调用
 */

class PlanManager {
  constructor() {
    this.plans = [];
    this.initialized = false;
  }

  /**
   * 初始化计划数据
   */
  async initPlans() {
    if (this.initialized) return;

    // 从localStorage加载计划（临时方案，实际应从后端加载）
    const savedPlans = localStorage.getItem("englishLearningPlans");
    if (savedPlans) {
      try {
        const parsedPlans = JSON.parse(savedPlans);
        this.plans = parsedPlans.map((plan) => ({
          ...plan,
          date: new Date(plan.date),
        }));
      } catch (e) {
        console.error("Failed to parse saved plans:", e);
        this.plans = [];
      }
    }

    this.initialized = true;
    return this.plans;
  }

  /**
   * 保存计划到localStorage（临时方案）
   */
  savePlansToStorage() {
    const plansToSave = this.plans.map((plan) => ({
      ...plan,
      date: plan.date.toISOString(),
    }));
    localStorage.setItem("englishLearningPlans", JSON.stringify(plansToSave));
  }

  /**
   * 获取所有计划
   */
  getAllPlans() {
    return this.plans;
  }

  /**
   * 获取指定日期的计划
   */
  getPlansByDate(date) {
    const targetDate = new Date(date);
    targetDate.setHours(0, 0, 0, 0);

    return this.plans.filter((plan) => {
      const planDate = new Date(plan.date);
      planDate.setHours(0, 0, 0, 0);
      return planDate.getTime() === targetDate.getTime();
    });
  }

  /**
   * 获取指定日期范围的计划
   */
  getPlansByDateRange(startDate, endDate) {
    const start = new Date(startDate);
    start.setHours(0, 0, 0, 0);
    const end = new Date(endDate);
    end.setHours(23, 59, 59, 999);

    return this.plans.filter((plan) => {
      const planDate = new Date(plan.date);
      return planDate >= start && planDate <= end;
    });
  }

  /**
   * 添加计划
   */
  addPlan(plan) {
    const maxId =
      this.plans.length > 0 ? Math.max(...this.plans.map((p) => p.id)) : 0;
    const newPlan = {
      ...plan,
      id: maxId + 1,
      completed: plan.completed || false,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    this.plans.push(newPlan);
    this.savePlansToStorage();

    // 实际应调用: POST /api/plans
    console.log("Add plan:", newPlan);

    return newPlan;
  }

  /**
   * 批量添加计划
   */
  addPlans(plansArray) {
    const addedPlans = [];
    for (const plan of plansArray) {
      const addedPlan = this.addPlan(plan);
      addedPlans.push(addedPlan);
    }
    return addedPlans;
  }

  /**
   * 更新计划
   */
  updatePlan(planId, updates) {
    const planIndex = this.plans.findIndex((p) => p.id === planId);
    if (planIndex === -1) return null;

    this.plans[planIndex] = {
      ...this.plans[planIndex],
      ...updates,
      updatedAt: new Date().toISOString(),
    };

    this.savePlansToStorage();

    // 实际应调用: PUT /api/plans/:id
    console.log("Update plan:", planId, updates);

    return this.plans[planIndex];
  }

  /**
   * 切换计划完成状态
   */
  togglePlanComplete(planId) {
    const plan = this.plans.find((p) => p.id === planId);
    if (!plan) return null;

    plan.completed = !plan.completed;
    plan.updatedAt = new Date().toISOString();

    this.savePlansToStorage();

    // 实际应调用: PUT /api/plans/:id/complete
    console.log("Toggle plan completion:", planId, plan.completed);

    return plan;
  }

  /**
   * 删除计划
   */
  deletePlan(planId) {
    const planIndex = this.plans.findIndex((p) => p.id === planId);
    if (planIndex === -1) return false;

    this.plans.splice(planIndex, 1);
    this.savePlansToStorage();

    // 实际应调用: DELETE /api/plans/:id
    console.log("Delete plan:", planId);

    return true;
  }

  /**
   * 删除指定日期的所有计划
   */
  deletePlansByDate(date) {
    const targetDate = new Date(date);
    targetDate.setHours(0, 0, 0, 0);

    const beforeCount = this.plans.length;
    this.plans = this.plans.filter((plan) => {
      const planDate = new Date(plan.date);
      planDate.setHours(0, 0, 0, 0);
      return planDate.getTime() !== targetDate.getTime();
    });

    const deletedCount = beforeCount - this.plans.length;
    if (deletedCount > 0) {
      this.savePlansToStorage();

      // 实际应调用: DELETE /api/plans/batch
      console.log("Delete plans by date:", date, deletedCount);
    }

    return deletedCount;
  }

  /**
   * 批量保存某日的计划（先删除旧的，再添加新的）
   */
  saveDayPlans(date, newPlans) {
    this.deletePlansByDate(date);
    const addedPlans = this.addPlans(
      newPlans.map((plan) => ({
        ...plan,
        date: new Date(date),
      }))
    );

    return addedPlans;
  }

  /**
   * 获取用户第一次添加计划的日期
   */
  getFirstPlanDate() {
    if (this.plans.length === 0) return new Date();

    const dates = this.plans.map((p) => new Date(p.date).getTime());
    return new Date(Math.min(...dates));
  }

  /**
   * 获取计划统计信息
   */
  getStatistics(startDate = null, endDate = null) {
    let targetPlans = this.plans;

    if (startDate && endDate) {
      targetPlans = this.getPlansByDateRange(startDate, endDate);
    }

    const totalPlans = targetPlans.length;
    const completedPlans = targetPlans.filter((p) => p.completed).length;
    const completionRate =
      totalPlans > 0 ? ((completedPlans / totalPlans) * 100).toFixed(1) : 0;

    // 分类统计
    const categoryStats = {};
    for (const plan of targetPlans) {
      if (!categoryStats[plan.category]) {
        categoryStats[plan.category] = {
          total: 0,
          completed: 0,
        };
      }
      categoryStats[plan.category].total++;
      if (plan.completed) {
        categoryStats[plan.category].completed++;
      }
    }

    return {
      totalPlans,
      completedPlans,
      completionRate,
      categoryStats,
    };
  }
}

// 创建单例
export const planManager = new PlanManager();
