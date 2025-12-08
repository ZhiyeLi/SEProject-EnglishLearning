/**
 * 计划管理工具类
 * 用于管理学习计划的数据和API调用
 */
import { planApi } from "@/api";

class PlanManager {
  constructor() {
    this.plans = [];
    this.initialized = false;
  }

  /**
   * 初始化计划数据 - 从后端加载
   */
  async initPlans() {
    if (this.initialized) return this.plans;

    try {
      const response = await planApi.getPlans();
      if (response.code === 200) {
        this.plans = response.data.map((plan) => ({
          ...plan,
          date: new Date(plan.date),
        }));
        this.initialized = true;
      }
    } catch (error) {
      console.error("加载计划失败:", error);
      this.plans = [];
    }

    return this.plans;
  }

  /**
   * 获取所有计划
   */
  async getAllPlans() {
    if (!this.initialized) {
      await this.initPlans();
    }
    return this.plans;
  }

  /**
   * 获取指定日期的计划
   */
  async getPlansByDate(date) {
    try {
      const dateStr = new Date(date).toISOString().split("T")[0];
      const response = await planApi.getPlansByDate(dateStr);
      if (response.code === 200) {
        return response.data.map((plan) => ({
          ...plan,
          date: new Date(plan.date),
        }));
      }
    } catch (error) {
      console.error("获取日期计划失败:", error);
    }
    return [];
  }

  /**
   * 获取今日计划
   */
  async getTodayPlans() {
    try {
      const response = await planApi.getTodayPlans();
      if (response.code === 200) {
        return response.data.map((plan) => ({
          ...plan,
          date: new Date(plan.date),
        }));
      }
    } catch (error) {
      console.error("获取今日计划失败:", error);
    }
    return [];
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
  async addPlan(plan) {
    try {
      const response = await planApi.createPlan({
        ...plan,
        date: new Date(plan.date).toISOString().split("T")[0],
      });

      if (response.code === 200) {
        const newPlan = {
          ...response.data,
          date: new Date(response.data.date),
        };
        this.plans.push(newPlan);
        return newPlan;
      }
    } catch (error) {
      console.error("添加计划失败:", error);
      throw error;
    }
    return null;
  }

  /**
   * 批量添加计划
   */
  async addPlans(plansArray) {
    const addedPlans = [];
    for (const plan of plansArray) {
      try {
        const addedPlan = await this.addPlan(plan);
        if (addedPlan) {
          addedPlans.push(addedPlan);
        }
      } catch (error) {
        console.error("批量添加计划失败:", error);
      }
    }
    return addedPlans;
  }

  /**
   * 更新计划
   */
  async updatePlan(planId, updates) {
    try {
      const response = await planApi.updatePlan(planId, updates);

      if (response.code === 200) {
        const planIndex = this.plans.findIndex((p) => p.id === planId);
        if (planIndex !== -1) {
          this.plans[planIndex] = {
            ...this.plans[planIndex],
            ...response.data,
            date: new Date(response.data.date),
          };
          return this.plans[planIndex];
        }
      }
    } catch (error) {
      console.error("更新计划失败:", error);
      throw error;
    }
    return null;
  }

  /**
   * 切换计划完成状态
   */
  async togglePlanComplete(planId) {
    try {
      const response = await planApi.togglePlanComplete(planId);

      if (response.code === 200) {
        const plan = this.plans.find((p) => p.id === planId);
        if (plan) {
          plan.completed = response.data.completed;
          plan.updatedAt = response.data.updatedAt;
          return plan;
        }
      }
    } catch (error) {
      console.error("切换计划状态失败:", error);
      throw error;
    }
    return null;
  }

  /**
   * 删除计划
   */
  async deletePlan(planId) {
    try {
      const response = await planApi.deletePlan(planId);

      if (response.code === 200) {
        const planIndex = this.plans.findIndex((p) => p.id === planId);
        if (planIndex !== -1) {
          this.plans.splice(planIndex, 1);
          return true;
        }
      }
    } catch (error) {
      console.error("删除计划失败:", error);
      throw error;
    }
    return false;
  }

  /**
   * 删除指定日期的所有计划
   */
  async deletePlansByDate(date) {
    const plansToDelete = await this.getPlansByDate(date);
    const ids = plansToDelete.map((p) => p.id);

    if (ids.length === 0) return 0;

    try {
      const response = await planApi.batchDeletePlans(ids);

      if (response.code === 200) {
        this.plans = this.plans.filter((p) => !ids.includes(p.id));
        return ids.length;
      }
    } catch (error) {
      console.error("批量删除计划失败:", error);
      throw error;
    }
    return 0;
  }

  /**
   * 批量保存某日的计划（先删除旧的，再添加新的）
   */
  async saveDayPlans(date, newPlans) {
    await this.deletePlansByDate(date);
    const addedPlans = await this.addPlans(
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
  async getFirstPlanDate() {
    try {
      const response = await planApi.getFirstPlanDate();
      if (response.code === 200 && response.data.firstDate) {
        return new Date(response.data.firstDate);
      }
    } catch (error) {
      console.error("获取首次计划日期失败:", error);
    }
    return new Date();
  }

  /**
   * 获取计划统计信息
   */
  async getStatistics(startDate = null, endDate = null) {
    try {
      const response = await planApi.getPlanStatistics();
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取计划统计失败:", error);
    }

    // 本地计算作为备用方案
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
