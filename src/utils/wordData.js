/**
 * 单词数据管理文件
 * 管理不同类型词汇的数据结构和存储方式
 */

// 定义单词类型及其信息
export const WORD_TYPES = {
  ELEMENTARY: {
    id: 'elementary',
    name: '初级词汇',
    description: '适合初学者',
    color: 'emerald',
    icon: 'fa-leaf',
    totalWords: 1000, // 示例数据
    words: generateSampleWords('初级词汇', 1000)
  },
  CET46: {
    id: 'cet46',
    name: '四六级词汇',
    description: '大学英语四六级',
    color: 'blue',
    icon: 'fa-book',
    totalWords: 1500,
    words: generateSampleWords('四六级词汇', 1500)
  },
  POSTGRADUATE: {
    id: 'postgraduate',
    name: '考研词汇',
    description: '考研英语必备',
    color: 'purple',
    icon: 'fa-graduation-cap',
    totalWords: 2000,
    words: generateSampleWords('考研词汇', 2000)
  },
  TOEFL_IELTS: {
    id: 'toefl_ielts',
    name: '托福雅思词汇',
    description: '出国考试必备',
    color: 'orange',
    icon: 'fa-globe',
    totalWords: 2500,
    words: generateSampleWords('托福雅思词汇', 2500)
  },
  PROFESSIONAL: {
    id: 'professional',
    name: '专业术语词汇',
    description: '行业专业用语',
    color: 'pink',
    icon: 'fa-flask',
    totalWords: 800,
    words: generateSampleWords('专业术语词汇', 800)
  }
}

/**
 * 生成示例单词数据（模拟从接口获取）
 * @param {string} type - 词汇类型
 * @param {number} count - 单词个数
 * @returns {array} 单词数组
 */
function generateSampleWords(type, count) {
  const sampleWords = {
    '初级词汇': ['apple', 'banana', 'cat', 'dog', 'egg', 'flower', 'green', 'happy', 'idea', 'jump'],
    '四六级词汇': ['abandon', 'ability', 'abolish', 'absorb', 'abstract', 'accelerate', 'access', 'accomplish', 'accurate', 'accuse'],
    '考研词汇': ['analogy', 'analyze', 'ancient', 'animate', 'announce', 'anticipate', 'anxiety', 'apartment', 'apparatus', 'apparent'],
    '托福雅思词汇': ['aberrant', 'abeyance', 'abhor', 'abide', 'aberration', 'abet', 'abeyant', 'abhorrence', 'abiding', 'ability'],
    '专业术语词汇': ['algorithm', 'amplitude', 'bandwidth', 'catalyst', 'coefficient', 'database', 'encryption', 'algorithm', 'framework', 'gradient']
  }

  const words = sampleWords[type] || []
  const result = []
  
  for (let i = 0; i < count; i++) {
    result.push({
      id: i + 1,
      word: words[i % words.length] + (i >= words.length ? i : ''),
      translation: '中文释义' + (i + 1),
      pronunciation: '/pruːn/示例',
      example: '示例句子' + (i + 1),
      isPassed: false, // 是否已打卡
      passedDate: null, // 打卡日期
      reviewCount: 0 // 复习次数
    })
  }
  
  return result
}

/**
 * 用户单词打卡进度存储
 */
export class WordProgressManager {
  constructor() {
    this.storageKey = 'wordProgress'
    this.planKey = 'wordPlan'
    this.selectedTypeKey = 'selectedWordType'
    this.pausedKey = 'wordPaused'
  }

  /**
   * 初始化用户的单词进度
   */
  initProgress() {
    const existing = this.getProgress()
    if (!existing) {
      const progress = {}
      Object.keys(WORD_TYPES).forEach(key => {
        progress[WORD_TYPES[key].id] = {
          typeId: WORD_TYPES[key].id,
          passedCount: 0,
          totalCount: WORD_TYPES[key].totalWords,
          passedWords: [], // 已打卡单词的id列表
          lastPassedDate: null,
          consecutiveDays: 0
        }
      })
      this.setProgress(progress)
    }
  }

  /**
   * 获取所有进度数据
   */
  getProgress() {
    const data = localStorage.getItem(this.storageKey)
    return data ? JSON.parse(data) : null
  }

  /**
   * 设置进度数据
   */
  setProgress(progress) {
    localStorage.setItem(this.storageKey, JSON.stringify(progress))
  }

  /**
   * 获取指定类型的进度
   */
  getTypeProgress(typeId) {
    const progress = this.getProgress()
    return progress ? progress[typeId] : null
  }

  /**
   * 更新指定类型的进度
   */
  updateTypeProgress(typeId, data) {
    const progress = this.getProgress()
    if (progress && progress[typeId]) {
      progress[typeId] = { ...progress[typeId], ...data }
      this.setProgress(progress)
      return progress[typeId]
    }
  }

  /**
   * 标记单词为已打卡
   */
  markWordAsPassed(typeId, wordId) {
    const progress = this.getTypeProgress(typeId)
    if (progress) {
      if (!progress.passedWords.includes(wordId)) {
        progress.passedWords.push(wordId)
        progress.passedCount = progress.passedWords.length
        progress.lastPassedDate = new Date().toISOString().split('T')[0]
        this.updateTypeProgress(typeId, progress)
      }
    }
  }

  /**
   * 标记单词为未打卡
   */
  unmarkWordAsPassed(typeId, wordId) {
    const progress = this.getTypeProgress(typeId)
    if (progress) {
      const index = progress.passedWords.indexOf(wordId)
      if (index > -1) {
        progress.passedWords.splice(index, 1)
        progress.passedCount = progress.passedWords.length
        this.updateTypeProgress(typeId, progress)
      }
    }
  }

  /**
   * 获取打卡计划
   */
  getPlan() {
    const data = localStorage.getItem(this.planKey)
    return data ? JSON.parse(data) : null
  }

  /**
   * 设置打卡计划
   */
  setPlan(plan) {
    localStorage.setItem(this.planKey, JSON.stringify(plan))
  }

  /**
   * 创建打卡计划
   * @param {string} typeId - 词汇类型ID
   * @param {number} wordsPerDay - 每天打卡个数
   * @returns {object} 计划信息
   */
  createPlan(typeId, wordsPerDay) {
    const progress = this.getTypeProgress(typeId)
    if (!progress) return null

    // 验证输入范围
    if (wordsPerDay < 1 || wordsPerDay > 100) {
      return { error: '每日打卡数量必须在1-100之间' }
    }

    // 计算剩余单词数和完成所需天数
    const remainingWords = progress.totalCount - progress.passedCount
    const daysNeeded = Math.ceil(remainingWords / wordsPerDay)
    const lastDayWords = remainingWords % wordsPerDay || wordsPerDay

    const plan = {
      typeId,
      wordsPerDay,
      remainingWords,
      daysNeeded,
      lastDayWords,
      startDate: new Date().toISOString().split('T')[0],
      status: 'active' // active, paused, completed
    }

    this.setPlan(plan)
    return plan
  }

  /**
   * 更新计划
   */
  updatePlan(updates) {
    const plan = this.getPlan()
    if (plan) {
      const updated = { ...plan, ...updates }
      this.setPlan(updated)
      return updated
    }
  }

  /**
   * 暂停计划
   */
  pausePlan() {
    const plan = this.getPlan()
    if (plan) {
      plan.status = 'paused'
      this.setPlan(plan)
    }
  }

  /**
   * 恢复计划
   */
  resumePlan() {
    const plan = this.getPlan()
    if (plan) {
      plan.status = 'active'
      this.setPlan(plan)
    }
  }

  /**
   * 获取已选择的词汇类型
   */
  getSelectedType() {
    const data = localStorage.getItem(this.selectedTypeKey)
    return data ? JSON.parse(data) : null
  }

  /**
   * 设置已选择的词汇类型
   * 当切换类型时，自动清空旧类型的打卡计划
   */
  setSelectedType(typeId) {
    // 检查是否有已选择的类型且与新类型不同
    const currentSelected = this.getSelectedType()
    if (currentSelected && currentSelected.typeId !== typeId) {
      // 切换了类型，清空旧的打卡计划
      this.clearPlan()
    }
    
    localStorage.setItem(this.selectedTypeKey, JSON.stringify({
      typeId,
      selectedDate: new Date().toISOString().split('T')[0]
    }))
  }

  /**
   * 清空打卡计划
   */
  clearPlan() {
    localStorage.removeItem(this.planKey)
  }

  /**
   * 清空所有数据（仅用于测试）
   */
  clearAll() {
    localStorage.removeItem(this.storageKey)
    localStorage.removeItem(this.planKey)
    localStorage.removeItem(this.selectedTypeKey)
  }
}

// 导出单一实例
export const wordProgressManager = new WordProgressManager()
