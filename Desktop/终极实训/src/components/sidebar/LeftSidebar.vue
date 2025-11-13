<template>
  <aside class="sidebar-left">
    <div class="sidebar-card">
      <div class="search-box">
        <input 
          type="text" 
          placeholder="搜索单词、课程或好友"
          v-model="searchQuery"
        >
        <i class="fas fa-search"></i>
      </div>
      
      <ul class="sidebar-list">
        <li v-for="item in sidebarItems" :key="item.id">
          <input 
            type="radio" 
            :name="item.id" 
            :id="item.id" 
            :checked="item.checked"
            @change="handleItemChange(item)"
          >
          <label :for="item.id">{{ item.label }}</label>
          <span v-if="item.hasNew" class="new-indicator"></span>
        </li>
      </ul>
      
      <div class="sidebar-actions">
        <button @click="handleChat">
          <i class="fas fa-comment"></i> 聊天
        </button>
        <button @click="handleFriends">
          <i class="fas fa-user-friends"></i> 好友
        </button>
        <button @click="handleEdit">
          <i class="fas fa-edit"></i> 修改
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue'

const searchQuery = ref('')
const sidebarItems = ref([
  { id: 'item1', label: '系统通知 已读', checked: true, hasNew: false },
  { id: 'item2', label: '学习助手 新通知', checked: false, hasNew: true },
  { id: 'item3', label: '好友动态 新通知', checked: false, hasNew: true },
  { id: 'item4', label: '学习小组 讨论', checked: false, hasNew: false },
  { id: 'item5', label: '打卡排行 提醒', checked: false, hasNew: false }
])

const handleItemChange = (item) => {
  sidebarItems.value.forEach(i => i.checked = i.id === item.id)
}

const handleChat = () => {
  console.log('打开聊天')
}

const handleFriends = () => {
  console.log('查看好友列表')
}

const handleEdit = () => {
  console.log('编辑设置')
}
</script>

<style scoped>
.sidebar-left {
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

.search-box {
  position: relative;
  margin-bottom: 20px;
}

.search-box input {
  width: 100%;
  padding: 10px 12px 10px 40px;
  border: 1px solid var(--gray-200);
  border-radius: 8px;
  font-size: 14px;
  transition: var(--transition);
}

.search-box input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-box i {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--gray-500);
  font-size: 16px;
}

.sidebar-list {
  list-style: none;
  margin-bottom: 12px;
}

.sidebar-list li {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: var(--transition);
  margin-bottom: 4px;
}

.sidebar-list li:hover {
  background-color: var(--gray-50);
}

.sidebar-list input {
  margin-right: 12px;
  color: var(--primary);
  width: 18px;
  height: 18px;
}

.new-indicator {
  margin-left: auto;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: var(--danger);
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.sidebar-actions {
  display: flex;
  gap: 10px;
  margin-top: 28px;
}

.sidebar-actions button {
  flex: 1;
  background-color: var(--primary-light);
  color: var(--primary-dark);
  border: none;
  border-radius: 8px;
  padding: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.sidebar-actions button:hover {
  background-color: var(--primary);
  color: var(--white);
}

@media (max-width: 1024px) {
  .sidebar-left {
    width: 100%;
  }
}
</style>