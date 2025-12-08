/**
 * 题库API调用封装
 * 用于处理题目相关的后端API调用
 */
import { questionApi } from "@/api";

/**
 * 题库管理类
 */
export class QuestionManager {
  constructor() {
    this.questions = [];
    this.courses = [];
    this.initialized = false;
  }

  /**
   * 获取题目列表
   */
  async getQuestions(params = {}) {
    try {
      const response = await questionApi.getQuestions(params);
      if (response.code === 200) {
        this.questions = response.data;
        return response.data;
      }
    } catch (error) {
      console.error("获取题目列表失败:", error);
    }
    return [];
  }

  /**
   * 获取题目详情
   */
  async getQuestionDetail(id) {
    try {
      const response = await questionApi.getQuestionDetail(id);
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取题目详情失败:", error);
    }
    return null;
  }

  /**
   * 提交答案
   */
  async submitAnswer(questionId, answer) {
    try {
      const response = await questionApi.submitAnswer(questionId, { answer });
      if (response.code === 200) {
        return { success: true, data: response.data };
      }
    } catch (error) {
      console.error("提交答案失败:", error);
    }
    return { success: false };
  }

  /**
   * 切换收藏状态
   */
  async toggleFavorite(questionId) {
    try {
      const response = await questionApi.toggleFavorite(questionId);
      if (response.code === 200) {
        return { success: true, data: response.data };
      }
    } catch (error) {
      console.error("切换收藏失败:", error);
    }
    return { success: false };
  }

  /**
   * 获取统计信息
   */
  async getStatistics() {
    try {
      const response = await questionApi.getStatistics();
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取统计信息失败:", error);
    }
    return null;
  }

  /**
   * 获取错题列表
   */
  async getWrongQuestions() {
    try {
      const response = await questionApi.getWrongQuestions();
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取错题列表失败:", error);
    }
    return [];
  }

  /**
   * 标记错题已掌握
   */
  async markWrongQuestionMastered(questionId) {
    try {
      const response = await questionApi.markWrongQuestionMastered(questionId);
      if (response.code === 200) {
        return { success: true };
      }
    } catch (error) {
      console.error("标记错题失败:", error);
    }
    return { success: false };
  }

  /**
   * 获取课程列表
   */
  async getCourses() {
    try {
      const response = await questionApi.getCourses();
      if (response.code === 200) {
        this.courses = response.data;
        return response.data;
      }
    } catch (error) {
      console.error("获取课程列表失败:", error);
    }
    return [];
  }

  /**
   * 获取课程题目
   */
  async getCourseQuestions(courseId) {
    try {
      const response = await questionApi.getCourseQuestions(courseId);
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取课程题目失败:", error);
    }
    return [];
  }

  /**
   * 获取用户生词本
   */
  async getUserVocabulary() {
    try {
      const response = await questionApi.getUserVocabulary();
      if (response.code === 200) {
        return response.data;
      }
    } catch (error) {
      console.error("获取生词本失败:", error);
    }
    return [];
  }

  /**
   * 添加到生词本
   */
  async addVocabulary(word, translation, context) {
    try {
      const response = await questionApi.addVocabulary({
        word,
        translation,
        context,
      });
      if (response.code === 200) {
        return { success: true, data: response.data };
      }
    } catch (error) {
      console.error("添加生词失败:", error);
    }
    return { success: false };
  }
}

// 创建单例
export const questionManager = new QuestionManager();
