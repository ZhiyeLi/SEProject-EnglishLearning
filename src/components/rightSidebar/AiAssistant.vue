<template>
  <div class="ai-assistant">
    <div class="assistant-header">
      <h3><i class="fas fa-robot"></i>AI助手</h3>
      <button class="settings-btn" @click="openSettings">
        <i class="fas fa-cog"></i>
      </button>
    </div>

    <div class="chat-container" ref="chatContainer">
      <div v-for="msg in messages" :key="msg.id" :class="['message', msg.type]">
        <div class="avatar">
          <i :class="msg.type === 'user' ? 'fas fa-user' : 'fas fa-robot'"></i>
        </div>
        <div class="message-content">
          <p>{{ msg.text }}</p>
          <span class="time">{{ msg.time }}</span>
        </div>
      </div>
    </div>

    <div class="input-area">
      <div class="input-wrapper">
        <input 
          type="text" 
          placeholder="输入你的问题..."
          v-model="userInput"
          @keyup.enter="sendMessage"
        />
        <button 
          class="send-btn" 
          @click="sendMessage"
          :disabled="!userInput.trim()"
        >
          <i class="fas fa-paper-plane"></i>
        </button>
      </div>

      <div class="suggestions">
        <button 
          v-for="suggestion in suggestions" 
          :key="suggestion"
          @click="useSuggestion(suggestion)"
        >
          {{ suggestion }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const userInput = ref('')
const messages = ref([
  {
    id: 1,
    type: 'assistant',
    text: '你好！我是你的AI学习助手，有什么我可以帮你的吗？',
    time: '14:00'
  }
])

const suggestions = [
  '如何提高英语口语？',
  '推荐一些学习资源',
  '制定学习计划'
]

const sendMessage = () => {
  if (!userInput.value.trim()) return

  // 添加用户消息
  messages.value.push({
    id: Date.now(),
    type: 'user',
    text: userInput.value,
    time: new Date().toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit'
    })
  })

  // 模拟AI回复
  setTimeout(() => {
    messages.value.push({
      id: Date.now() + 1,
      type: 'assistant',
      text: '我正在处理你的问题，很快会给你回复...',
      time: new Date().toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit'
      })
    })
  }, 1000)

  userInput.value = ''
}

const useSuggestion = (suggestion) => {
  userInput.value = suggestion
  sendMessage()
}

const openSettings = () => {
  console.log('打开设置')
}
</script>

<style scoped>
.ai-assistant {
  background: var(--white);
  border-radius: 12px;
  border: 1px solid var(--gray-200);
  display: flex;
  flex-direction: column;
  height: 500px;
}

.assistant-header {
  padding: 16px;
  border-bottom: 1px solid var(--gray-200);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.assistant-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--gray-800);
  display: flex;
  align-items: center;
  gap: 8px;
}

.assistant-header i {
  color: var(--primary);
}

.settings-btn {
  background: none;
  border: none;
  color: var(--gray-500);
  cursor: pointer;
  padding: 4px;
  transition: var(--transition);
}

.settings-btn:hover {
  color: var(--gray-700);
  transform: rotate(45deg);
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message.assistant {
  align-self: flex-start;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message.assistant .avatar {
  background-color: var(--primary-light);
  color: var(--primary);
}

.message.user .avatar {
  background-color: var(--success-light);
  color: var(--success);
}

.message-content {
  background-color: var(--gray-50);
  padding: 12px 16px;
  border-radius: 12px;
  position: relative;
}

.message.assistant .message-content {
  border-top-left-radius: 4px;
}

.message.user .message-content {
  background-color: var(--primary-light);
  border-top-right-radius: 4px;
}

.message p {
  font-size: 14px;
  margin: 0;
  color: var(--gray-800);
}

.message.user p {
  color: var(--primary-dark);
}

.time {
  font-size: 10px;
  color: var(--gray-500);
  margin-top: 4px;
  display: block;
}

.input-area {
  padding: 16px;
  border-top: 1px solid var(--gray-200);
}

.input-wrapper {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.input-wrapper input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid var(--gray-300);
  border-radius: 8px;
  font-size: 14px;
  transition: var(--transition);
}

.input-wrapper input:focus {
  border-color: var(--primary);
  outline: none;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.send-btn {
  padding: 8px 16px;
  background-color: var(--primary);
  color: var(--white);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: var(--transition);
}

.send-btn:hover:not(:disabled) {
  background-color: var(--primary-dark);
}

.send-btn:disabled {
  background-color: var(--gray-300);
  cursor: not-allowed;
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggestions button {
  padding: 6px 12px;
  background-color: var(--gray-50);
  border: 1px solid var(--gray-200);
  border-radius: 16px;
  font-size: 12px;
  color: var(--gray-700);
  cursor: pointer;
  transition: var(--transition);
}

.suggestions button:hover {
  background-color: var(--primary-light);
  border-color: var(--primary);
  color: var(--primary);
}
</style>