// src/store/modules/plan.js
import { defineStore } from 'pinia';
import { planApi } from '@/api';

export const usePlanStore = defineStore('plan', {
  state: () => ({
    plans: [],
    initialized: false
  }),
  getters: {
    // 根据日期获取计划
    getPlansByDate: (state) => (date) => {
      const targetDate = new Date(date);
      targetDate.setHours(0, 0, 0, 0);
      
      return state.plans.filter(plan => {
        const planDate = new Date(plan.date);
        planDate.setHours(0, 0, 0, 0);
        return planDate.getTime() === targetDate.getTime();
      });
    }
  },
  actions: {
    // 初始化计划数据
    async initPlans() {
      if (this.initialized) return;
      
      try {
        const response = await planApi.getPlans();
        if (response.code === 200) {
          this.plans = response.data.map(plan => ({
            ...plan,
            date: new Date(plan.date)
          }));
          this.initialized = true;
        }
      } catch (error) {
        console.error('加载计划失败：', error);
      }
    },
    
    // 添加计划
    async addPlan(plan) {
      try {
        const response = await planApi.addPlan(plan);
        if (response.code === 200) {
          const newPlan = {
            ...response.data,
            date: new Date(response.data.date)
          };
          this.plans.push(newPlan);
          return newPlan;
        }
      } catch (error) {
        console.error('添加计划失败：', error);
      }
      return null;
    },
    
    // 更新计划
    async updatePlan(planId, updates) {
      try {
        const response = await planApi.updatePlan(planId, updates);
        if (response.code === 200) {
          const index = this.plans.findIndex(plan => plan.id === planId);
          if (index !== -1) {
            this.plans[index] = {
              ...this.plans[index],
              ...response.data,
              date: new Date(response.data.date)
            };
            return this.plans[index];
          }
        }
      } catch (error) {
        console.error('更新计划失败：', error);
      }
      return null;
    },
    
    // 删除计划
    async deletePlan(planId) {
      try {
        const response = await planApi.deletePlan(planId);
        if (response.code === 200) {
          const index = this.plans.findIndex(plan => plan.id === planId);
          if (index !== -1) {
            this.plans.splice(index, 1);
            return true;
          }
        }
      } catch (error) {
        console.error('删除计划失败：', error);
      }
      return false;
    },
    
    // 批量保存某日的计划
    async saveDayPlans(date, newPlans) {
      try {
        const response = await planApi.saveDayPlans(date, newPlans);
        if (response.code === 200) {
          // 先删除该日期的旧计划
          const targetDate = new Date(date);
          targetDate.setHours(0, 0, 0, 0);
          this.plans = this.plans.filter(plan => {
            const planDate = new Date(plan.date);
            planDate.setHours(0, 0, 0, 0);
            return planDate.getTime() !== targetDate.getTime();
          });
          
          // 添加新计划
          const addedPlans = response.data.map(plan => ({
            ...plan,
            date: new Date(plan.date)
          }));
          this.plans.push(...addedPlans);
          return addedPlans;
        }
      } catch (error) {
        console.error('保存每日计划失败：', error);
      }
      return [];
    }
  }
});