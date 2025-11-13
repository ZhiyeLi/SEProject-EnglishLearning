<template>
  <aside class="sidebar-right">
    <div class="sidebar-card">
      <h3 class="dictionary-title">
        <i class="fas fa-book"></i>当前使用词典
      </h3>
      
      <DictionaryCard />
      
      <button class="change-dict" @click="changeDictionary">
        <i class="fas fa-exchange-alt"></i> 更换词典
      </button>
    </div>
    
    <!-- AI 辅助小面板已移除：改为顶部 AI 标签页跳转到独立聊天页面 -->
  </aside>
</template>

<script setup>
import DictionaryCard from '../rightSidebar/DictionaryCard.vue'

const changeDictionary = () => {
  console.log('更换词典')
}
</script>

<style scoped>
.sidebar-right {
  width: 280px;
  flex-shrink: 0;
}

.sidebar-card {
  background-color: var(--white);
  border-radius: 12px;
  border: 1px solid var(--gray-200);
  box-shadow: var(--shadow-sm);
  padding: 20px;
  margin-bottom: 24px;
  transition: var(--transition);
}

.sidebar-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.dictionary-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--gray-800);
  display: flex;
  align-items: center;
  gap: 8px;
}

.dictionary-title i {
  color: var(--warning);
}

.change-dict {
  width: 100%;
  background-color: var(--primary-light);
  color: var(--primary-dark);
  border: none;
  border-radius: 8px;
  padding: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.change-dict:hover {
  background-color: var(--primary);
  color: var(--white);
}

@media (max-width: 1024px) {
  .sidebar-right {
    width: 100%;
    order: 2;
    margin-top: 20px;
  }
}
</style>