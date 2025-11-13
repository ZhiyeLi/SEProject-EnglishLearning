<template>
  <div class="app">
    <Header />

    <main>
      <div class="container">
        <!-- AI 聊天页面 -->
        <div v-if="currentPage === 'ai'" class="ai-page-wrapper">
          <AiChat />
        </div>

        <!-- 单词打卡页面 -->
        <div v-else-if="currentPage === 'checkin'" class="checkin-page-wrapper">
          <WordCheckin :selectedLevel="selectedLevel" />
        </div>

        <!-- 单词等级选择页面 -->
        <div v-else-if="currentPage === 'level-selector'" class="level-selector-wrapper">
          <WordLevelSelector
            @select="handleLevelSelect"
            @close="handleLevelSelectorClose"
          />
        </div>

        <!-- 默认布局（主页） -->
        <div v-else class="content-wrapper">
          <LeftSidebar />
          <div class="content-center">
            <CheckinCard ref="checkinCardRef" />
            <ScheduleCard />
            <ArticleList />
          </div>
          <RightSidebar />
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, provide } from 'vue'
import Header from './components/layout/Header.vue'
import Footer from './components/layout/Footer.vue'
import LeftSidebar from './components/sidebar/LeftSidebar.vue'
import RightSidebar from './components/sidebar/RightSidebar.vue'
import CheckinCard from './components/main/CheckinCard.vue'
import ScheduleCard from './components/main/ScheduleCard.vue'
import ArticleList from './components/main/ArticleList.vue'
import AiChat from './views/AiChat.vue'
import WordLevelSelector from './views/WordLevelSelector.vue'
import WordCheckin from './views/WordCheckin.vue'

// 全局页面状态
const currentPage = ref('home')
const selectedLevel = ref(null)
const checkinCardRef = ref(null)

// 提供全局页面控制
provide('currentPage', currentPage)
provide('selectedLevel', selectedLevel)

// 处理等级选择
const handleLevelSelect = (level) => {
  selectedLevel.value = level
  // 更新 CheckinCard 显示的等级
  if (checkinCardRef.value) {
    checkinCardRef.value.setSelectedLevel(level)
  }
  // 页面跳转由 WordLevelSelector 负责
  // 在 WordLevelSelector 中已经设置了 currentPage.value = 'checkin'
}

// 处理等级选择器关闭（用户点击取消按钮）
const handleLevelSelectorClose = () => {
  // 返回首页，但保留已选择的等级信息
  // 下次点击"背单词"按钮会直接进入打卡页面
  currentPage.value = 'home'
}
</script>

<style>
:root {
  /* 主色调 */
  --primary: #3b82f6;
  --primary-light: #eff6ff;
  --primary-dark: #1d4ed8;
  /* 辅助色 */
  --success: #10b981;
  --warning: #f59e0b;
  --info: #06b6d4;
  --danger: #ef4444;
  /* 中性色 */
  --white: #ffffff;
  --gray-50: #f9fafb;
  --gray-100: #f3f4f6;
  --gray-200: #e5e7eb;
  --gray-300: #d1d5db;
  --gray-600: #4b5563;
  --gray-700: #374151;
  --gray-800: #1f2937;
  /* 阴影 */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow: 0 1px 3px rgba(0, 0, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  /* 过渡 */
  --transition: all 0.3s ease;
}

/* 基础样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

body {
  background-color: var(--gray-50);
  color: var(--gray-700);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  line-height: 1.5;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: var(--gray-100);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: var(--gray-300);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--gray-400);
}

.container {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.content-wrapper {
  display: flex;
  gap: 24px;
  padding: 32px 0;
}

/* 打卡和等级选择页面的包装器 */
.checkin-page-wrapper,
.level-selector-wrapper {
  padding: 24px 0;
}

/* 响应式布局 */
@media (max-width: 1024px) {
  .content-wrapper {
    flex-wrap: wrap;
  }
}
</style>
