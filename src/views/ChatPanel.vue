<template>
  <div class="chat-container">
    <!-- 左侧好友列表 -->
    <div class="friends-sidebar">
      <div class="sidebar-header">
        <h2>好友列表</h2>
        <button class="add-friend-btn">+</button>
      </div>
      
      <div class="search-box">
        <input 
          type="text" 
          placeholder="搜索好友..." 
          v-model="searchKeyword"
          class="search-input"
        >
      </div>
      
      <div class="friends-list">
        <h3 class="list-title">在线好友</h3>
        <ul>
          <li 
            v-for="friend in filteredFriends" 
            :key="friend.id"
            @click="selectFriend(friend)"
            :class="{ 'active': selectedFriend?.id === friend.id }"
            class="friend-item"
          >
            <div class="avatar" :style="{ backgroundColor: friend.avatarColor }">
              {{ friend.name.charAt(0) }}
            </div>
            <div class="friend-info">
              <div class="friend-name">{{ friend.name }}</div>
              <div class="last-msg">
                {{ friend.lastMsg || '点击开始聊天' }}
              </div>
            </div>
            <span v-if="friend.unread" class="unread-badge">{{ friend.unread }}</span>
          </li>
        </ul>
      </div>
    </div>
    
    <!-- 右侧聊天区域 -->
    <div class="chat-area" v-if="selectedFriend">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="avatar" :style="{ backgroundColor: selectedFriend.avatarColor }">
          {{ selectedFriend.name.charAt(0) }}
        </div>
        <div class="chat-info">
          <div class="chat-name">{{ selectedFriend.name }}</div>
          <div class="online-status">在线</div>
        </div>
      </div>
      
      <!-- 消息区域 -->
      <div class="messages-container" ref="messagesContainer">
        <div 
          v-for="(msg, index) in currentMessages" 
          :key="index"
          :class="{ 'my-msg': msg.isMe, 'friend-msg': !msg.isMe }"
          class="message-item"
        >
          <div class="msg-content">{{ msg.text }}</div>
          <div class="msg-time">{{ msg.time }}</div>
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="input-area">
        <textarea 
          v-model="newMessage" 
          placeholder="输入消息..." 
          class="msg-input"
          @keydown.enter="sendMessage"
        ></textarea>
        <button @click="sendMessage" class="send-btn" :disabled="!newMessage.trim()">
          发送
        </button>
      </div>
    </div>
    
    <!-- 未选择好友时的提示 -->
    <div class="no-selection" v-else>
      <div class="empty-state">
        <div class="icon">💬</div>
        <p>请选择一个好友开始聊天</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';

// 好友数据
const friends = ref([
  {
    id: 1,
    name: '张三',
    avatarColor: '#3b82f6',
    lastMsg: '明天开会别忘了',
    unread: 2
  },
  {
    id: 2,
    name: '李四',
    avatarColor: '#10b981',
    lastMsg: '文档已发送',
    unread: 0
  },
  {
    id: 3,
    name: '王五',
    avatarColor: '#f59e0b',
    lastMsg: null,
    unread: 1
  },
]);

// 聊天记录（按好友ID分组）
const chatRecords = ref({
  1: [
    { text: '嗨，周一的会议你参加吗？', isMe: false, time: '09:30' },
    { text: '当然参加，已经在日程里了', isMe: true, time: '09:32' },
    { text: '明天开会别忘了', isMe: false, time: '16:45' }
  ],
  2: [
    { text: '项目文档需要今天提交吗？', isMe: true, time: '14:20' },
    { text: '文档已发送', isMe: false, time: '15:05' }
  ],
  3: []
});

// 状态管理
const searchKeyword = ref('');
const selectedFriend = ref(null);
const newMessage = ref('');
const messagesContainer = ref(null);
const filteredFriends = ref([...friends.value]);

// 筛选好友列表
watch(searchKeyword, (val) => {
  filteredFriends.value = friends.value.filter(friend => 
    friend.name.toLowerCase().includes(val.toLowerCase())
  );
});

// 当前聊天消息
const currentMessages = ref([]);

// 选择好友切换聊天
const selectFriend = (friend) => {
  selectedFriend.value = friend;
  // 加载对应好友的聊天记录
  currentMessages.value = [...chatRecords.value[friend.id]];
  // 清空未读消息
  if (friend.unread > 0) {
    friend.unread = 0;
  }
  // 滚动到最新消息
  scrollToBottom();
};

// 发送消息
const sendMessage = (e) => {
  if (e) e.preventDefault(); // 阻止回车换行
  if (!newMessage.value.trim() || !selectedFriend.value) return;

  const now = new Date();
  const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
  
  // 生成新消息
  const newMsg = {
    text: newMessage.value.trim(),
    isMe: true,
    time: timeStr
  };

  // 添加到当前聊天记录
  currentMessages.value.push(newMsg);
  // 同步更新到聊天记录存储
  chatRecords.value[selectedFriend.value.id].push(newMsg);
  
  // 清空输入框
  newMessage.value = '';

  // 模拟好友回复
  setTimeout(() => {
    const replyMsg = {
      text: '收到，我稍后回复你~',
      isMe: false,
      time: timeStr
    };
    currentMessages.value.push(replyMsg);
    chatRecords.value[selectedFriend.value.id].push(replyMsg);
    scrollToBottom();
  }, 1000);

  scrollToBottom();
};

// 滚动到最新消息
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};
</script>

<style scoped>
.chat-container {
  padding-top: 40px;
  display: flex;
  height: 100vh;
  width: 1200px;
  background-color: var(--gray-50);
  overflow: hidden;
}

/* 左侧好友列表 */
.friends-sidebar {
  width: 300px;
  background-color: var(--white);
  border-right: 1px solid var(--gray-200);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid var(--gray-200);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-header h2 {
  font-size: 18px;
  font-weight: 600;
}

.add-friend-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: var(--primary);
  color: white;
  border: none;
  font-size: 20px;
  cursor: pointer;
  transition: var(--transition);
}

.add-friend-btn:hover {
  background-color: var(--primary-dark);
}

.search-box {
  padding: 12px;
  border-bottom: 1px solid var(--gray-200);
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--gray-200);
  border-radius: 20px;
  outline: none;
  transition: var(--transition);
}

.search-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.friends-list {
  flex: 1;
  overflow-y: auto;
}

.list-title {
  padding: 12px 16px;
  font-size: 14px;
  color: var(--gray-600);
  background-color: var(--gray-50);
}

.friend-item {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: var(--transition);
  border-bottom: 1px solid var(--gray-100);
}

.friend-item:hover {
  background-color: var(--gray-50);
}

.friend-item.active {
  background-color: var(--primary-light);
  border-left: 3px solid var(--primary);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.friend-name {
  font-weight: 500;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.last-msg {
  font-size: 12px;
  color: var(--gray-600);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-badge {
  background-color: var(--danger);
  color: white;
  font-size: 12px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: auto;
}

/* 右侧聊天区域 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--white);
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid var(--gray-200);
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: var(--gray-50);
}

.chat-info {
  flex: 1;
}

.chat-name {
  font-weight: 500;
  margin-bottom: 2px;
}

.online-status {
  font-size: 12px;
  color: var(--success);
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.my-msg {
  align-self: flex-end;
}

.friend-msg {
  align-self: flex-start;
}

.msg-content {
  padding: 10px 14px;
  border-radius: 10px;
  word-wrap: break-word;
}

.my-msg .msg-content {
  background-color: var(--primary);
  color: white;
  border-top-right-radius: 2px;
}

.friend-msg .msg-content {
  background-color: var(--white);
  border: 1px solid var(--gray-200);
  border-top-left-radius: 2px;
}

.msg-time {
  font-size: 11px;
  color: var(--gray-500);
  margin-top: 4px;
  align-self: flex-end;
}

.input-area {
  padding: 12px 16px;
  border-top: 1px solid var(--gray-200);
  display: flex;
  gap: 12px;
}

.msg-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid var(--gray-200);
  border-radius: 8px;
  resize: none;
  outline: none;
  min-height: 48px;
  max-height: 120px;
  transition: var(--transition);
}

.msg-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.send-btn {
  padding: 0 16px;
  background-color: var(--primary);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: var(--transition);
  white-space: nowrap;
}

.send-btn:hover {
  background-color: var(--primary-dark);
}

.send-btn:disabled {
  background-color: var(--gray-300);
  cursor: not-allowed;
}

/* 未选择好友提示 */
.no-selection {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--white);
}

.empty-state {
  text-align: center;
}

.icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  color: var(--gray-500);
  font-size: 16px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .friends-sidebar {
    width: 100%;
    position: absolute;
    height: 100%;
    z-index: 10;
  }
  
  .chat-area {
    width: 100%;
    position: absolute;
    height: 100%;
  }
}
</style>