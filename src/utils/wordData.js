/**
 * 单词数据管理文件
 * 管理不同类型词汇的数据结构和存储方式
 */
import { wordApi } from "@/api";

// 定义单词类型及其信息（保持向后兼容）
export const WORD_TYPES = {
  ELEMENTARY: {
    id: "elementary",
    name: "基础词汇",
    description: "初级词汇基础",
    color: "emerald",
    icon: "fa-leaf",
    totalWords: 8245,
  },
  CET4: {
    id: "cet4",
    name: "四级词汇",
    description: "大学英语四级",
    color: "blue",
    icon: "fa-book",
    totalWords: 13134,
  },
  CET6: {
    id: "cet6",
    name: "六级词汇",
    description: "大学英语六级",
    color: "purple",
    icon: "fa-graduation-cap",
    totalWords: 1619,
  },
  TOEFL_IELTS: {
    id: "toefl_ielts",
    name: "托福雅思词汇",
    description: "出国考试必备",
    color: "orange",
    icon: "fa-globe",
    totalWords: 576,
  },
};

/**
 * 用户单词打卡进度存储
 */
export class WordProgressManager {
  constructor() {
    this.initialized = false;
    this.typeList = [];
  }

  /**
   * 初始化 - 从后端获取单词类型列表
   */
  async init() {
    if (this.initialized) return;

    try {
      const response = await wordApi.getWordTypeList();
      if (response.code === 200) {
        this.typeList = response.data;
        this.initialized = true;
      }
    } catch (error) {
      console.error("初始化单词类型失败:", error);
      // 使用本地WORD_TYPES作为备份
      this.typeList = Object.values(WORD_TYPES);
    }
  }

  /**
   * 获取单词类型列表
   */
  async getWordTypeList() {
    if (!this.initialized) {
      await this.init();
    }
    return this.typeList;
  }

  /**
   * 获取用户进度
   */
  async getProgress() {
    try {
      const response = await wordApi.getUserWordProgress();
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取用户进度失败:", error);
    }
    return null;
  }

  /**
   * 获取指定类型的进度
   */
  async getTypeProgress(typeId) {
    try {
      const allProgress = await this.getProgress();
      if (allProgress && allProgress[typeId]) {
        return allProgress[typeId];
      }
      // 返回默认值
      return { passedCount: 0, passedWords: [] };
    } catch (error) {
      console.error("获取类型进度失败:", error);
      return { passedCount: 0, passedWords: [] };
    }
  }

  /**
   * 获取指定类型的单词列表
   */
  async getWordsByType(typeId, params = {}) {
    try {
      const response = await wordApi.getWordsByType({
        typeId,
        ...params,
      });
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取单词列表失败:", error);
    }
    return [];
  }

  /**
   * 获取已掌握的单词
   */
  async getPassedWords() {
    try {
      const response = await wordApi.getPassedWords();
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取已掌握单词失败:", error);
    }
    return [];
  }

  /**
   * 标记单词为已打卡
   */
  async markWordAsPassed(typeId, wordId) {
    try {
      const response = await wordApi.markWordPassed({
        typeId,
        wordId,
      });
      if (response.code === 200) {
        return { success: true, data: response.data };
      }
    } catch (error) {
      console.error("标记单词失败:", error);
    }
    return { success: false };
  }

  /**
   * 取消标记单词
   */
  async unmarkWordAsPassed(typeId, wordId) {
    try {
      const response = await wordApi.unmarkWordPassed({
        typeId,
        wordId,
      });
      if (response.code === 200) {
        return { success: true };
      }
    } catch (error) {
      console.error("取消标记单词失败:", error);
    }
    return { success: false };
  }

  /**
   * 批量标记单词
   */
  async batchMarkWords(typeId, wordIds) {
    try {
      const response = await wordApi.batchMarkWordsPassed({
        typeId,
        wordIds,
      });
      if (response.code === 200) {
        return { success: true, data: response.data };
      }
    } catch (error) {
      console.error("批量标记单词失败:", error);
    }
    return { success: false };
  }

  /**
   * 获取单词详情
   */
  async getWordDetail(wordId) {
    try {
      const response = await wordApi.getWordDetail(wordId);
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取单词详情失败:", error);
    }
    return null;
  }

  /**
   * 获取已选择的词汇类型
   */
  async getSelectedType() {
    try {
      const response = await wordApi.getSelectedWordType();
      console.log("getSelectedType 响应:", response);
      if (response.code === 200 && response.data) {
        // 后端直接返回 typeId（数字）
        // 需要转换为对象格式 { typeId: number, id: number }
        const typeId = response.data;
        console.log("后端返回的typeId:", typeId, "类型:", typeof typeId);
        if (typeId) {
          // 将选择同时缓存在 localStorage，作为客户端回退
          try {
            localStorage.setItem("selectedWordType", JSON.stringify({ typeId }));
          } catch (e) {
            // ignore
          }
          return { typeId: typeId, id: typeId };
        }
      }
    } catch (error) {
      console.error("获取选择的词汇类型失败:", error);
    }
    // 如果后端不可用或未返回，尝试从 localStorage 中读取已选择类型作为回退
    try {
      const raw = localStorage.getItem("selectedWordType");
      if (raw) {
        const parsed = JSON.parse(raw);
        if (parsed?.typeId) {
          return { typeId: parsed.typeId, id: parsed.typeId };
        }
      }
    } catch (e) {
      console.error("从 localStorage 读取 selectedWordType 失败:", e);
    }
    return null;
  }

  /**
   * 设置已选择的词汇类型
   */
  async setSelectedType(typeId) {
    try {
      const response = await wordApi.setSelectedWordType({ typeId });
      if (response.code === 200) {
        try {
          localStorage.setItem("selectedWordType", JSON.stringify({ typeId }));
        } catch (e) {
          // ignore
        }
        return { success: true };
      }
    } catch (error) {
      console.error("设置词汇类型失败:", error);
    }
    // 即使后端设置失败，也在本地保存一下，方便客户端恢复选择
    try {
      localStorage.setItem("selectedWordType", JSON.stringify({ typeId }));
    } catch (e) {
      // ignore
    }
    return { success: false };
  }

  /**
   * 获取打卡计划
   */
  async getPlan(typeId) {
    try {
      const response = await wordApi.getUserCheckInPlan(typeId);
      if (response.code === 200) {
        // 成功获取后，同步保存到本地作为缓存
        this.savePlanToLocal(typeId, response.data);
        return response.data;
      }
    } catch (error) {
      console.error("获取打卡计划失败:", error);
    }
    // API失败时，尝试从本地缓存获取
    return this.getPlanFromLocal(typeId);
  }

  /**
   * 创建打卡计划
   */
  async createPlan(typeId, wordsPerDay) {
    // 验证输入范围
    if (wordsPerDay < 1 || wordsPerDay > 100) {
      return { error: "每日打卡数量必须在1-100之间" };
    }

    try {
      const response = await wordApi.createCheckInPlan({
        typeId,
        wordsPerDay,
      });
      if (response.code === 200) {
        // 成功创建后，保存到本地缓存
        this.savePlanToLocal(typeId, response.data);
        return response.data;
      } else {
        return { error: response.message };
      }
    } catch (error) {
      console.error("创建打卡计划失败:", error);
      // API失败时，创建本地计划作为回退
      const localPlan = this.createLocalPlan(typeId, wordsPerDay);
      this.savePlanToLocal(typeId, localPlan);
      return localPlan;
    }
  }

  /**
   * 从本地获取计划
   */
  getPlanFromLocal(typeId) {
    try {
      const uid = this.getCurrentUserId();
      const key = `wordCheckInPlan:${uid}:${typeId}`;
      const raw = localStorage.getItem(key);
      if (raw) {
        const plan = JSON.parse(raw);
        // 检查是否为当前typeId的计划
        if (plan && Number(plan.typeId) === Number(typeId)) {
          return plan;
        }
      }
    } catch (e) {
      console.error("从本地获取计划失败:", e);
    }
    return null;
  }

  /**
   * 保存计划到本地
   */
  savePlanToLocal(typeId, plan) {
    try {
      const uid = this.getCurrentUserId();
      const key = `wordCheckInPlan:${uid}:${typeId}`;
      localStorage.setItem(key, JSON.stringify(plan));
    } catch (e) {
      console.error("保存计划到本地失败:", e);
    }
  }

  /**
   * 创建本地计划（当API失败时的回退）
   */
  createLocalPlan(typeId, wordsPerDay) {
    const now = new Date().toISOString();
    return {
      id: `local_${typeId}_${Date.now()}`,
      typeId: Number(typeId),
      wordsPerDay: wordsPerDay,
      remainingWords: 0, // 需要后续计算
      daysNeeded: 0, // 需要后续计算
      status: "active",
      createdAt: now,
      updatedAt: now,
      isLocal: true, // 标记为本地创建的计划
    };
  }

  /**
   * 获取当前用户ID
   */
  getCurrentUserId() {
    try {
      const raw = localStorage.getItem("userStore");
      if (raw) {
        const parsed = JSON.parse(raw);
        return parsed?.userInfo?.id || "anon";
      }
    } catch (e) {
      // ignore
    }
    return "anon";
  }

  /**
   * 更新计划状态
   */
  async updatePlanStatus(status) {
    try {
      const response = await wordApi.updatePlanStatus({ status });
      if (response.code === 200) {
        return { success: true, data: response.data };
      }
    } catch (error) {
      console.error("更新计划状态失败:", error);
    }
    return { success: false };
  }

  /**
   * 暂停计划
   */
  async pausePlan() {
    return await this.updatePlanStatus("paused");
  }

  /**
   * 恢复计划
   */
  async resumePlan() {
    return await this.updatePlanStatus("active");
  }

  /**
   * 获取今日打卡状态
   */
  async getTodayCheckInStatus() {
    try {
      const response = await wordApi.getTodayCheckInStatus();
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取今日打卡状态失败:", error);
    }
    return null;
  }

  /**
   * 获取打卡统计
   */
  async getCheckInStatistics() {
    try {
      const response = await wordApi.getCheckInStatistics();
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取打卡统计失败:", error);
    }
    return null;
  }

  /**
   * 获取未打卡的单词列表
   */
  async getUnpassedWords(typeId) {
    try {
      const response = await wordApi.getUnpassedWords({
        typeId,
      });
      if (response.code === 200) {
        return response.data || [];
      }
    } catch (error) {
      console.error("获取未打卡单词列表失败:", error);
    }
    return [];
  }
}

// 导出单一实例
export const wordProgressManager = new WordProgressManager();
