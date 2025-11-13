/**
 * 单词打卡数据管理模块
 * 用于管理不同等级的打卡进度、复习数据等
 */

// 单词等级列表
export const WORD_LEVELS = [
  {
    id: 'pre_university',
    name: '大学前词汇',
    description: '初中高中常用词汇',
    icon: '📚',
    wordCount: 2500,
  },
  {
    id: 'cet4',
    name: '英语四级词汇',
    description: 'CET-4 考试范围',
    icon: '📖',
    wordCount: 3500,
  },
  {
    id: 'cet6',
    name: '英语六级词汇',
    description: 'CET-6 考试范围',
    icon: '📕',
    wordCount: 5000,
  },
  {
    id: 'graduate',
    name: '考研词汇',
    description: '考研英语重点词汇',
    icon: '🎓',
    wordCount: 5500,
  },
  {
    id: 'toefl',
    name: '托福词汇',
    description: 'TOEFL 考试范围',
    icon: '🌍',
    wordCount: 6000,
  },
  {
    id: 'ielts',
    name: '雅思词汇',
    description: 'IELTS 考试范围',
    icon: '🌏',
    wordCount: 6500,
  },
  {
    id: 'professional',
    name: '专业性词汇',
    description: '行业领域专业用语',
    icon: '💼',
    wordCount: 3000,
  },
]

// 模拟词汇数据生成器
export const generateMockWords = (levelId, count) => {
  const sampleWords = [
    { word: 'abandon', meaning: '放弃', pronunciation: '/əˈbændən/' },
    { word: 'ability', meaning: '能力', pronunciation: '/əˈbɪləti/' },
    { word: 'absolute', meaning: '绝对的', pronunciation: '/ˈæbsəluːt/' },
    { word: 'accelerate', meaning: '加速', pronunciation: '/əkˈseləreɪt/' },
    { word: 'accept', meaning: '接受', pronunciation: '/əkˈsept/' },
    { word: 'access', meaning: '获取', pronunciation: '/ˈækses/' },
    { word: 'accompany', meaning: '陪伴', pronunciation: '/əˈkʌmpəni/' },
    { word: 'accomplish', meaning: '完成', pronunciation: '/əˈkɑːmplɪʃ/' },
    { word: 'according', meaning: '根据', pronunciation: '/əˈkɔːrdɪŋ/' },
    { word: 'account', meaning: '账户', pronunciation: '/əˈkaʊnt/' },
  ]

  const words = []
  for (let i = 0; i < count; i++) {
    const sample = sampleWords[i % sampleWords.length]
    words.push({
      id: `${levelId}_word_${i}`,
      levelId,
      word: sample.word + (i > 0 ? ` ${i}` : ''),
      meaning: sample.meaning,
      pronunciation: sample.pronunciation,
      status: 'new', // new, learned, review
      learnedDate: null,
      reviewCount: 0,
    })
  }
  return words
}

// 打卡进度存储结构
export class CheckinProgressManager {
  constructor() {
    // 按等级存储进度数据
    this.progressData = this.loadProgressData()
  }

  /**
   * 从 localStorage 加载进度数据
   */
  loadProgressData() {
    const stored = localStorage.getItem('wordCheckinProgress')
    if (stored) {
      try {
        return JSON.parse(stored)
      } catch (e) {
        console.warn('Failed to parse progress data:', e)
        return this.initializeProgressData()
      }
    }
    return this.initializeProgressData()
  }

  /**
   * 初始化进度数据结构
   */
  initializeProgressData() {
    const data = {}
    WORD_LEVELS.forEach((level) => {
      data[level.id] = {
        levelId: level.id,
        currentProgress: 0, // 当前打卡进度（已打卡词汇数）
        isPaused: false, // 是否暂停
        pausedAt: null, // 暂停时间
        totalLearned: 0, // 总共学过的词汇数
        totalReviewed: 0, // 总共复习过的词汇数
        lastCheckInTime: null, // 最后打卡时间
      }
    })
    return data
  }

  /**
   * 保存进度数据到 localStorage
   */
  saveProgressData() {
    localStorage.setItem('wordCheckinProgress', JSON.stringify(this.progressData))
  }

  /**
   * 获取指定等级的进度信息
   */
  getProgress(levelId) {
    return (
      this.progressData[levelId] || {
        levelId,
        currentProgress: 0,
        isPaused: false,
        pausedAt: null,
        totalLearned: 0,
        totalReviewed: 0,
        lastCheckInTime: null,
      }
    )
  }

  /**
   * 更新进度
   */
  updateProgress(levelId, updates) {
    if (!this.progressData[levelId]) {
      this.progressData[levelId] = this.getProgress(levelId)
    }
    this.progressData[levelId] = {
      ...this.progressData[levelId],
      ...updates,
      lastCheckInTime: new Date().toISOString(),
    }
    this.saveProgressData()
    return this.progressData[levelId]
  }

  /**
   * 增加当前打卡进度
   */
  incrementProgress(levelId, amount = 1) {
    const progress = this.getProgress(levelId)
    return this.updateProgress(levelId, {
      currentProgress: progress.currentProgress + amount,
      totalLearned: progress.totalLearned + amount,
    })
  }

  /**
   * 暂停打卡任务
   */
  pauseCheckin(levelId) {
    return this.updateProgress(levelId, {
      isPaused: true,
      pausedAt: new Date().toISOString(),
    })
  }

  /**
   * 恢复打卡任务
   */
  resumeCheckin(levelId) {
    return this.updateProgress(levelId, {
      isPaused: false,
      pausedAt: null,
    })
  }

  /**
   * 重置指定等级的进度
   */
  resetProgress(levelId) {
    return this.updateProgress(levelId, {
      currentProgress: 0,
      isPaused: false,
      pausedAt: null,
      totalLearned: 0,
      totalReviewed: 0,
    })
  }

  /**
   * 切换等级（不影响原有进度）
   */
  switchLevel(fromLevelId, toLevelId) {
    // 暂停当前等级
    this.pauseCheckin(fromLevelId)
    // 恢复目标等级
    const targetProgress = this.getProgress(toLevelId)
    if (targetProgress.isPaused) {
      this.resumeCheckin(toLevelId)
    }
    return this.getProgress(toLevelId)
  }

  /**
   * 标记单词为已学
   */
  markWordAsLearned(levelId, wordId) {
    // 这里可以存储单个单词的学习状态
    const key = `word_learned_${levelId}_${wordId}`
    localStorage.setItem(key, JSON.stringify({ learned: true, date: new Date().toISOString() }))
  }

  /**
   * 获取单词学习状态
   */
  getWordLearningStatus(levelId, wordId) {
    const key = `word_learned_${levelId}_${wordId}`
    const data = localStorage.getItem(key)
    return data ? JSON.parse(data) : null
  }

  /**
   * 获取需要复习的单词列表
   */
  getReviewWords(levelId, limit = 10) {
    const learned = []
    // 获取已学过的单词（可以从后端 API 获取）
    return learned.slice(0, limit)
  }

  /**
   * 清除所有数据（用于测试）
   */
  clearAllData() {
    localStorage.clear()
    this.progressData = this.initializeProgressData()
  }
}

// 导出单例
export const checkinManager = new CheckinProgressManager()
