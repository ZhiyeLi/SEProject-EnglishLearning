/**
 * 单词数据管理文件
 * 管理不同类型词汇的数据结构和存储方式
 */
import { wordApi } from "@/api";

// 定义单词类型及其信息（保持向后兼容）
export const WORD_TYPES = {
  ELEMENTARY: {
    id: "elementary",
    name: "初级词汇",
    description: "适合初学者",
    color: "emerald",
    icon: "fa-leaf",
    totalWords: 1000,
  },
  CET46: {
    id: "cet46",
    name: "四六级词汇",
    description: "大学英语四六级",
    color: "blue",
    icon: "fa-book",
    totalWords: 1500,
  },
  POSTGRADUATE: {
    id: "postgraduate",
    name: "考研词汇",
    description: "考研英语必备",
    color: "purple",
    icon: "fa-graduation-cap",
    totalWords: 2000,
  },
  TOEFL_IELTS: {
    id: "toefl_ielts",
    name: "托福雅思词汇",
    description: "出国考试必备",
    color: "orange",
    icon: "fa-globe",
    totalWords: 2500,
  },
  PROFESSIONAL: {
    id: "professional",
    name: "专业术语词汇",
    description: "行业专业用语",
    color: "pink",
    icon: "fa-flask",
    totalWords: 800,
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
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取选择的词汇类型失败:", error);
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
        return { success: true };
      }
    } catch (error) {
      console.error("设置词汇类型失败:", error);
    }
    return { success: false };
  }

  /**
   * 获取打卡计划
   */
  async getPlan() {
    try {
      const response = await wordApi.getUserCheckInPlan();
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取打卡计划失败:", error);
    }
    return null;
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
        return response.data;
      } else {
        return { error: response.message };
      }
    } catch (error) {
      console.error("创建打卡计划失败:", error);
      return { error: "创建失败" };
    }
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
}

// 导出单一实例
export const wordProgressManager = new WordProgressManager();
