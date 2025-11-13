<template>
  <div class="app">
    <Header />

    <!-- 全部页面组件由路由渲染，默认路由 '/' 是 Home.vue（Home 负责显示侧边栏和内容） -->
    <router-view />

    <Footer />
  </div>
</template>

<script setup>
import { ref, provide } from 'vue'
import Header from './components/layout/Header.vue'
import Footer from './components/layout/Footer.vue'

// 全局页面状态（Header 与 AiChat 等组件通过 inject 使用）
const currentPage = ref('home')
provide('currentPage', currentPage)
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

/* 响应式布局 */
@media (max-width: 1024px) {
  .content-wrapper {
    flex-wrap: wrap;
  }
}
</style>
