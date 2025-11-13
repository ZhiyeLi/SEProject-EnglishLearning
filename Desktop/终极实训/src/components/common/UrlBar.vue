<template>
  <div class="url-bar">
    <div class="url-display">http://英语学习平台.com</div>
    <div class="url-actions">
      <button title="字体设置"><i class="fas fa-font"></i></button>
      <button title="收藏本站"><i class="fas fa-star"></i></button>
      <button class="notification-btn" @click="toggleNotification">
        <i class="fas fa-bell"></i>
        <span class="notification-badge">{{ notifications.length }}</span>
        <div class="notification-dropdown" :class="{ active: showNotifications }">
          <a 
            v-for="notification in notifications" 
            :key="notification.id" 
            href="#" 
            class="notification-item"
            :class="{ new: notification.isNew }"
          >
            {{ notification.message }}
            <div class="notification-time">{{ notification.time }}</div>
          </a>
        </div>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showNotifications = ref(false)
const notifications = ref([
  {
    id: 1,
    message: '新单词学习提醒',
    time: '10分钟前',
    isNew: true
  },
  {
    id: 2,
    message: '课程更新通知：新增高考听力技巧课',
    time: '1小时前',
    isNew: true
  },
  {
    id: 3,
    message: '好友小明完成了今日打卡',
    time: '昨天',
    isNew: false
  }
])

const toggleNotification = (event) => {
  event.stopPropagation()
  showNotifications.value = !showNotifications.value
}

// 点击页面其他区域关闭通知
const handleClickOutside = (event) => {
  if (showNotifications.value && !event.target.closest('.notification-btn')) {
    showNotifications.value = false
  }
}

document.addEventListener('click', handleClickOutside)
</script>

<style scoped>
.url-bar {
  padding: 12px 0;
  display: flex;
  align-items: center;
}

.url-display {
  background-color: var(--gray-100);
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  color: var(--gray-600);
  flex-grow: 1;
  max-width: 500px;
  transition: var(--transition);
}

.url-display:hover {
  background-color: var(--gray-200);
}

.url-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.url-actions button {
  background: none;
  border: none;
  color: var(--gray-600);
  cursor: pointer;
  font-size: 16px;
  transition: var(--transition);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.url-actions button:hover {
  color: var(--primary);
  background-color: var(--primary-light);
}

.notification-btn {
  position: relative;
}

.notification-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background-color: var(--danger);
  color: white;
  font-size: 10px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.notification-dropdown {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 8px;
  width: 320px;
  background-color: var(--white);
  border-radius: 8px;
  box-shadow: var(--shadow-md);
  padding: 8px 0;
  z-index: 100;
  display: none;
  transform-origin: top right;
  transform: scale(0.95);
  opacity: 0;
  transition: var(--transition);
}

.notification-dropdown.active {
  display: block;
  transform: scale(1);
  opacity: 1;
}

.notification-dropdown::before {
  content: '';
  position: absolute;
  top: -8px;
  right: 12px;
  width: 16px;
  height: 16px;
  background-color: var(--white);
  transform: rotate(45deg);
  box-shadow: -2px -2px 2px rgba(0,0,0,0.05);
}

.notification-item {
  display: block;
  padding: 12px 16px;
  font-size: 14px;
  color: var(--gray-700);
  text-decoration: none;
  transition: var(--transition);
  border-left: 3px solid transparent;
}

.notification-item:hover {
  background-color: var(--gray-50);
  border-left-color: var(--primary);
}

.notification-item.new {
  background-color: var(--primary-light/50);
}

.notification-time {
  font-size: 12px;
  color: var(--gray-600);
  margin-top: 4px;
}

@media (max-width: 480px) {
  .url-display {
    max-width: 200px;
  }
}
</style>