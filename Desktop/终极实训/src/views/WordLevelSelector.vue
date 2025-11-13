<template>
  <div class="word-level-selector">
    <!-- 背景遮罩 -->
    <div class="modal-overlay" @click="closeSelector"></div>

    <!-- 选择面板 -->
    <div class="modal-panel">
      <!-- 标题 -->
      <div class="modal-header">
        <h2>选择单词等级</h2>
        <button class="close-btn" @click="closeSelector">
          <span>×</span>
        </button>
      </div>

      <!-- 等级网格 -->
      <div class="levels-grid">
        <div
          v-for="level in wordLevels"
          :key="level.id"
          class="level-card"
          :class="{ active: selectedLevel?.id === level.id }"
          @click="selectLevel(level)"
        >
          <div class="level-icon">
            <span>{{ level.icon }}</span>
          </div>
          <div class="level-info">
            <h3>{{ level.name }}</h3>
            <p>{{ level.description }}</p>
            <span class="word-count">{{ level.wordCount }} 词汇</span>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="modal-footer">
        <button class="btn btn-secondary" @click="closeSelector">取消</button>
        <button
          class="btn btn-primary"
          :disabled="!selectedLevel"
          @click="confirmSelection"
        >
          确定
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { WORD_LEVELS } from '../utils/checkinManager'

// 接收来自父组件的事件处理
const emit = defineEmits(['select', 'close'])
const currentPage = inject('currentPage')

// 单词等级列表
const wordLevels = ref(WORD_LEVELS)

// 选中的等级
const selectedLevel = ref(null)

// 选择等级
const selectLevel = (level) => {
  selectedLevel.value = level
}

// 确认选择
const confirmSelection = () => {
  if (selectedLevel.value) {
    emit('select', selectedLevel.value)
    // 更新当前页面为 'checkin'（打卡页面）
    currentPage.value = 'checkin'
  }
}

// 关闭选择器
const closeSelector = () => {
  emit('close')
}
</script>

<style scoped>
.word-level-selector {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* 背景遮罩 */
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 模态框面板 */
.modal-panel {
  position: relative;
  background: white;
  border-radius: 16px;
  max-width: 900px;
  width: 90%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 模态框头部 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid var(--gray-200);
}

.modal-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--gray-800);
}

.close-btn {
  background: none;
  border: none;
  font-size: 32px;
  color: var(--gray-400);
  cursor: pointer;
  transition: color 0.3s ease;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: var(--gray-600);
}

/* 等级网格 */
.levels-grid {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

/* 等级卡片 */
.level-card {
  padding: 20px;
  border: 2px solid var(--gray-200);
  border-radius: 12px;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
}

.level-card:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
  transform: translateY(-2px);
}

.level-card.active {
  border-color: var(--primary);
  background-color: var(--primary-light);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 等级图标 */
.level-icon {
  font-size: 40px;
  flex-shrink: 0;
}

/* 等级信息 */
.level-info {
  flex: 1;
  min-width: 0;
}

.level-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--gray-800);
  margin-bottom: 4px;
}

.level-info p {
  font-size: 13px;
  color: var(--gray-600);
  margin-bottom: 8px;
  line-height: 1.3;
}

.word-count {
  display: inline-block;
  font-size: 12px;
  background-color: var(--gray-100);
  color: var(--gray-600);
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.level-card.active .word-count {
  background-color: rgba(59, 130, 246, 0.1);
  color: var(--primary);
}

/* 模态框底部 */
.modal-footer {
  display: flex;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid var(--gray-200);
  justify-content: flex-end;
}

/* 按钮样式 */
.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);
}

.btn-primary {
  background-color: var(--primary);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--primary-dark);
  transform: translateY(-1px);
}

.btn-primary:disabled {
  background-color: var(--gray-300);
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-secondary {
  background-color: var(--gray-200);
  color: var(--gray-700);
}

.btn-secondary:hover {
  background-color: var(--gray-300);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-panel {
    width: 95%;
    max-height: 95vh;
  }

  .levels-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .modal-header h2 {
    font-size: 20px;
  }

  .level-card {
    flex-direction: column;
    text-align: center;
  }

  .level-info p {
    font-size: 12px;
  }
}
</style>
