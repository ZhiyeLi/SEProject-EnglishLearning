<template>
  <div class="word-checkin-page">
    <!-- 返回按钮 + 等级切换 -->
    <div class="checkin-header">
      <button class="btn-back" @click="goBack">
        <span>←</span> 返回
      </button>
      <h1>单词打卡</h1>
      <div class="header-actions">
        <button class="btn-switch-level" @click="openLevelSwitcher">
          <span>🔄</span> 切换等级
        </button>
      </div>
    </div>

    <!-- 等级切换模态框 -->
    <div v-if="showLevelSwitcher" class="level-switch-modal">
      <div class="modal-overlay" @click="closeLevelSwitcher"></div>
      <div class="modal-content">
        <h3>切换单词等级</h3>
        <div class="level-switch-grid">
          <div
            v-for="level in availableLevels"
            :key="level.id"
            class="level-switch-card"
            :class="{ active: level.id === selectedLevel.id }"
            @click="handleLevelSwitch(level)"
          >
            <div class="level-switch-icon">{{ level.icon }}</div>
            <div class="level-switch-info">
              <p class="level-switch-name">{{ level.name }}</p>
              <p class="level-switch-progress">
                进度: {{ getProgress(level.id).currentProgress }} / {{ level.wordCount }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 打卡内容区域 -->
    <div class="checkin-content">
      <!-- 进度显示 -->
      <div class="progress-section">
        <div class="current-level-badge">
          <span class="level-icon">{{ selectedLevel.icon }}</span>
          <div class="level-details">
            <span class="level-name">{{ selectedLevel.name }}</span>
            <span class="level-total">共 {{ selectedLevel.wordCount }} 词</span>
          </div>
        </div>

        <div class="progress-bar-container">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
          </div>
          <div class="progress-text">
            <span>{{ currentProgress.currentProgress }} / {{ selectedLevel.wordCount }}</span>
            <span>{{ progressPercentage }}%</span>
          </div>
        </div>

        <!-- 进度状态信息 -->
        <div class="progress-status">
          <div v-if="currentProgress.currentProgress === 0" class="status-info empty">
            <i class="fas fa-info-circle"></i>
            当前进度为 0，开始打卡
          </div>
          <div v-else-if="currentProgress.isPaused" class="status-info paused">
            <i class="fas fa-pause-circle"></i>
            已暂停 · 上次进度: {{ currentProgress.currentProgress }}
          </div>
          <div v-else class="status-info active">
            <i class="fas fa-play-circle"></i>
            正在进行中...
          </div>
        </div>
      </div>

      <!-- 操作按钮区域 -->
      <div class="action-buttons">
        <button
          class="btn btn-primary btn-large"
          :disabled="currentProgress.isPaused && currentProgress.currentProgress === 0"
          @click="handleStartOrResume"
        >
          <i class="fas fa-play"></i>
          {{ currentProgress.isPaused ? '继续打卡' : '开始打卡' }}
        </button>

        <button
          v-if="!currentProgress.isPaused && currentProgress.currentProgress > 0"
          class="btn btn-warning btn-large"
          @click="handlePause"
        >
          <i class="fas fa-pause"></i>
          暂停
        </button>

        <button class="btn btn-info btn-large" @click="openReviewPanel">
          <i class="fas fa-history"></i>
          复习已学单词
        </button>
      </div>

      <!-- 复习面板 -->
      <div v-if="showReviewPanel" class="review-panel">
        <div class="review-header">
          <h3>复习已学单词</h3>
          <button class="close-btn" @click="closeReviewPanel">×</button>
        </div>
        <div class="review-content">
          <div class="review-stats">
            <div class="stat-item">
              <span class="stat-label">总共学过</span>
              <span class="stat-value">{{ currentProgress.totalLearned }}</span>
              <span class="stat-unit">个</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">已复习</span>
              <span class="stat-value">{{ currentProgress.totalReviewed }}</span>
              <span class="stat-unit">个</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">需复习</span>
              <span class="stat-value">{{
                currentProgress.totalLearned - currentProgress.totalReviewed
              }}</span>
              <span class="stat-unit">个</span>
            </div>
          </div>

          <div class="review-placeholder">
            <div class="placeholder-icon">📚</div>
            <p>复习功能开发中</p>
            <small>后续将显示需要复习的单词列表</small>
          </div>

          <div class="review-actions">
            <button
              class="btn btn-secondary"
              @click="closeReviewPanel"
            >
              关闭
            </button>
            <button
              class="btn btn-primary"
              :disabled="currentProgress.totalLearned - currentProgress.totalReviewed === 0"
            >
              开始复习
            </button>
          </div>
        </div>
      </div>

      <!-- 主要内容区域占位 -->
      <div v-if="!showReviewPanel" class="placeholder">
        <div class="placeholder-icon">📝</div>
        <h2>单词打卡页面</h2>
        <p>当前等级：{{ selectedLevel.name }}</p>
        <p class="placeholder-subtitle">
          {{ currentProgress.currentProgress > 0
            ? `已打卡 ${currentProgress.currentProgress} 个单词，继续加油！`
            : '点击"开始打卡"按钮开始学习' }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject, watch } from 'vue'
import { checkinManager, generateMockWords, WORD_LEVELS } from '../utils/checkinManager'

const currentPage = inject('currentPage')

// Props
const props = defineProps({
  selectedLevel: {
    type: Object,
    required: true,
  },
})

// 状态
const availableLevels = ref(WORD_LEVELS)
const currentLevelId = ref(props.selectedLevel.id)
const showLevelSwitcher = ref(false)
const showReviewPanel = ref(false)
const currentProgress = ref({
  currentProgress: 0,
  isPaused: false,
  pausedAt: null,
  totalLearned: 0,
  totalReviewed: 0,
  lastCheckInTime: null,
})

// 计算当前等级信息
const selectedLevel = computed(() => {
  return WORD_LEVELS.find((l) => l.id === currentLevelId.value) || props.selectedLevel
})

// 计算进度百分比
const progressPercentage = computed(() => {
  const current = currentProgress.value.currentProgress
  const total = selectedLevel.value.wordCount
  return total > 0 ? Math.round((current / total) * 100) : 0
})

// 获取指定等级的进度
const getProgress = (levelId) => {
  return checkinManager.getProgress(levelId)
}

// 打开等级切换器
const openLevelSwitcher = () => {
  showLevelSwitcher.value = true
}

// 关闭等级切换器
const closeLevelSwitcher = () => {
  showLevelSwitcher.value = false
}

// 处理等级切换
const handleLevelSwitch = (level) => {
  if (level.id === currentLevelId.value) {
    closeLevelSwitcher()
    return
  }

  // 切换等级并保留原有进度
  checkinManager.switchLevel(currentLevelId.value, level.id)
  currentLevelId.value = level.id

  // 更新当前进度显示
  updateCurrentProgress()

  closeLevelSwitcher()
}

// 打开复习面板
const openReviewPanel = () => {
  showReviewPanel.value = true
}

// 关闭复习面板
const closeReviewPanel = () => {
  showReviewPanel.value = false
}

// 处理开始或继续打卡
const handleStartOrResume = () => {
  if (currentProgress.value.isPaused) {
    // 恢复打卡
    checkinManager.resumeCheckin(currentLevelId.value)
  }

  // 模拟打卡进度
  checkinManager.incrementProgress(currentLevelId.value)
  updateCurrentProgress()
}

// 处理暂停
const handlePause = () => {
  checkinManager.pauseCheckin(currentLevelId.value)
  updateCurrentProgress()
}

// 更新当前进度显示
const updateCurrentProgress = () => {
  currentProgress.value = checkinManager.getProgress(currentLevelId.value)
}

// 返回主页
const goBack = () => {
  currentPage.value = 'home'
}

// 监听选中等级变化
watch(
  () => props.selectedLevel.id,
  (newLevelId) => {
    currentLevelId.value = newLevelId
    updateCurrentProgress()
  }
)

// 初始化进度
updateCurrentProgress()
</script>

<style scoped>
.word-checkin-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

/* 打卡页面头部 */
.checkin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: white;
  gap: 16px;
}

.checkin-header h1 {
  font-size: 24px;
  font-weight: 600;
  flex: 1;
  text-align: center;
}

/* 头部操作按钮 */
.header-actions {
  display: flex;
  gap: 8px;
}

/* 返回按钮和切换等级按钮 */
.btn-back,
.btn-switch-level {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition);
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.btn-back:hover,
.btn-switch-level:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

/* 等级切换模态框 */
.level-switch-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.level-switch-modal .modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
}

.level-switch-modal .modal-content {
  position: relative;
  background: white;
  border-radius: 12px;
  padding: 24px;
  max-width: 600px;
  width: 90%;
  max-height: 70vh;
  overflow-y: auto;
  z-index: 1000;
  animation: slideUp 0.3s ease;
}

.level-switch-modal h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--gray-800);
}

.level-switch-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.level-switch-card {
  padding: 16px;
  border: 2px solid var(--gray-200);
  border-radius: 8px;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  gap: 12px;
}

.level-switch-card:hover {
  border-color: var(--primary);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

.level-switch-card.active {
  border-color: var(--primary);
  background-color: var(--primary-light);
}

.level-switch-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.level-switch-info {
  flex: 1;
}

.level-switch-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--gray-800);
  margin: 0;
}

.level-switch-progress {
  font-size: 12px;
  color: var(--gray-600);
  margin: 4px 0 0 0;
}

/* 内容区域 */
.checkin-content {
  flex: 1;
  overflow-y: auto;
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 进度区域 */
.progress-section {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(147, 197, 253, 0.05));
  padding: 24px;
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.1);
}

.current-level-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.level-icon {
  font-size: 28px;
}

.level-details {
  display: flex;
  flex-direction: column;
}

.level-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--gray-800);
}

.level-total {
  font-size: 12px;
  color: var(--gray-600);
}

/* 进度条 */
.progress-bar-container {
  margin-bottom: 16px;
}

.progress-bar {
  width: 100%;
  height: 12px;
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), var(--info));
  border-radius: 6px;
  transition: width 0.6s ease;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--gray-600);
  font-weight: 500;
}

/* 进度状态 */
.progress-status {
  margin-top: 16px;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
}

.status-info i {
  font-size: 16px;
}

.status-info.empty {
  background-color: var(--gray-100);
  color: var(--gray-600);
}

.status-info.paused {
  background-color: rgba(245, 158, 11, 0.1);
  color: var(--warning);
}

.status-info.active {
  background-color: rgba(16, 185, 129, 0.1);
  color: var(--success);
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-large {
  padding: 14px 24px;
  font-size: 15px;
}

.btn-primary {
  background-color: var(--primary);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--primary-dark);
  transform: translateY(-2px);
}

.btn-primary:disabled {
  background-color: var(--gray-300);
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-warning {
  background-color: var(--warning);
  color: white;
}

.btn-warning:hover {
  background-color: #d97706;
  transform: translateY(-2px);
}

.btn-info {
  background-color: var(--info);
  color: white;
}

.btn-info:hover {
  background-color: #0891b2;
  transform: translateY(-2px);
}

.btn-secondary {
  background-color: var(--gray-200);
  color: var(--gray-700);
}

.btn-secondary:hover {
  background-color: var(--gray-300);
}

/* 复习面板 */
.review-panel {
  background: white;
  border: 1px solid var(--gray-200);
  border-radius: 12px;
  overflow: hidden;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--gray-200);
}

.review-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--gray-800);
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--gray-400);
  transition: color 0.3s ease;
}

.close-btn:hover {
  color: var(--gray-600);
}

.review-content {
  padding: 24px;
}

.review-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background-color: var(--gray-50);
  border-radius: 8px;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: var(--gray-600);
  margin-bottom: 8px;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: var(--primary);
}

.stat-unit {
  display: block;
  font-size: 12px;
  color: var(--gray-600);
  margin-top: 4px;
}

.review-placeholder {
  text-align: center;
  padding: 40px 20px;
  color: var(--gray-600);
}

.review-placeholder .placeholder-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.8;
}

.review-placeholder p {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--gray-800);
}

.review-placeholder small {
  font-size: 12px;
  color: var(--gray-600);
}

.review-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--gray-200);
}

/* 占位符 */
.placeholder {
  text-align: center;
  padding: 60px 24px;
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.8;
}

.placeholder h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--gray-800);
  margin-bottom: 8px;
}

.placeholder p {
  font-size: 14px;
  color: var(--gray-600);
  margin: 0;
}

.placeholder-subtitle {
  font-size: 13px;
  color: var(--primary);
  margin-top: 12px;
  font-weight: 500;
}

/* 动画 */
@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .checkin-header {
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }

  .checkin-header h1 {
    font-size: 20px;
  }

  .header-actions {
    width: 100%;
  }

  .btn-back,
  .btn-switch-level {
    flex: 1;
  }

  .action-buttons {
    flex-direction: column;
  }

  .review-stats {
    grid-template-columns: 1fr;
  }

  .level-switch-grid {
    grid-template-columns: 1fr;
  }

  .placeholder {
    padding: 40px 20px;
  }

  .placeholder-icon {
    font-size: 48px;
  }

  .placeholder h2 {
    font-size: 20px;
  }
}

</style>
