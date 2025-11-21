<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 引入NavBar组件 -->
    <NavBar :navItems="navItems">
      <template #actions>
        <button class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group">
          <i class="fas fa-lightbulb text-lg"></i>
          <span class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">学习建议</span>
        </button>
        <button class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group">
          <i class="fas fa-cog text-lg"></i>
          <span class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">设置</span>
        </button>
        <button class="relative ml-2 text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors">
          <i class="fas fa-bell text-lg"></i>
          <span class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center animate-pulse">3</span>
        </button>
      </template>
    </NavBar>

    <!-- 主内容区 -->
    <main class="flex-grow flex flex-col md:flex-row">
      <!-- 左侧好友列表 -->
      <aside class="w-full md:w-96 bg-white border-r border-gray-200 shadow-sm md:h-[calc(100vh-64px)] sticky top-[64px] overflow-y-auto flex-shrink-0 z-20">
        <div class="p-5 h-full flex flex-col">
          <!-- 搜索框 -->
          <div class="relative mb-6">
            <input 
              type="text" 
              placeholder="搜索好友..." 
              class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-base"
            >
            <i class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-lg"></i>
          </div>
          
          <!-- 好友列表区域 -->
          <div class="flex-grow overflow-y-auto -mx-2 px-2">
            <h3 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4 mt-2">
              好友列表
            </h3>
            
            <ul class="space-y-2">
              <FriendItem 
                name="示例好友" 
                avatar="https://picsum.photos/seed/friend1/100/100" 
                status="online" 
                extra-info="正在输入..."
                class="bg-emerald-50 border-l-4 border-emerald-500"
              >
                <template #actions>
                  <button class="text-gray-600 hover:text-emerald-600 p-1 rounded-full hover:bg-emerald-50 transition-colors">
                    <i class="fas fa-comment"></i>
                  </button>
                </template>
              </FriendItem>
              <li>
                <button 
                  @click="handleAddFriend"
                  class="w-full flex items-center justify-center p-3 text-emerald-600 text-sm border border-dashed border-emerald-200 rounded-lg hover:bg-emerald-50 transition-all hover:border-emerald-300 group"
                >
                  <i class="fas fa-plus-circle mr-2 group-hover:scale-110 transition-transform"></i>
                  可添加更多好友
                </button>
              </li>
            </ul>
          </div>
          
          <!-- 底部功能选项 -->
          <div class="border-t border-gray-100 mt-4 pt-3 flex justify-around">
            <button
              @click="gotoHome"
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'friends' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
              ]"
            >
              <i class="fas fa-users text-xl mb-1"></i>
              <span class="text-sm">好友</span>
            </button>

            <button
              @click="gotoChat"
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'chat' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
              ]"
            >
              <i class="fas fa-comment text-xl mb-1"></i>
              <span class="text-sm">聊天</span>
            </button>

            <button
              @click="activeTab = 'rank'"
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'rank' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
              ]"
            >
              <i class="fas fa-trophy text-xl mb-1"></i>
              <span class="text-sm">排行榜</span>
            </button>
          </div>
        </div>
      </aside>
      
      <!-- 中间内容区：AI聊天 -->
      <div class="flex-grow flex flex-col bg-white overflow-hidden">
        <!-- 聊天头部 -->
        <div class="bg-gradient-to-r from-emerald-500 to-emerald-600 border-b border-gray-200 p-4 flex items-center shadow-sm">
          <button 
            @click="gotoWordCheckIn"
            class="text-white hover:text-emerald-100 p-2 rounded-full hover:bg-emerald-400 transition-colors mr-3"
          >
            <i class="fas fa-arrow-left text-lg"></i>
          </button>
          <div class="flex-grow">
            <h3 class="font-semibold text-white text-lg flex items-center">
              <i class="fas fa-robot mr-2"></i> AI 学习助手
            </h3>
            <span class="text-emerald-100 text-sm">{{ systemPromptShort }}</span>
          </div>
        </div>
        
        <!-- 聊天消息区域 -->
        <div class="flex-grow p-6 overflow-y-auto space-y-4" ref="chatContainer">
          <div v-for="msg in messages" :key="msg.id" :class="['flex items-start', msg.type === 'user' ? 'justify-end' : 'justify-start']">
            <div v-if="msg.type === 'assistant'" class="flex items-start gap-3 max-w-[80%]">
              <div class="w-8 h-8 rounded-full bg-emerald-100 flex items-center justify-center flex-shrink-0">
                <i class="fas fa-robot text-emerald-600 text-lg"></i>
              </div>
              <div class="bg-gray-100 p-4 rounded-lg rounded-tl-none">
                <p class="text-gray-800 whitespace-pre-wrap break-words" v-html="formatMessage(msg.text)"></p>
                <span class="text-xs text-gray-500 block mt-2">{{ msg.time }}</span>
              </div>
            </div>
            
            <div v-else class="flex items-start gap-3 max-w-[80%] flex-row-reverse">
              <div class="w-8 h-8 rounded-full bg-emerald-500 flex items-center justify-center flex-shrink-0">
                <i class="fas fa-user text-white text-sm"></i>
              </div>
              <div class="bg-emerald-500 text-white p-4 rounded-lg rounded-tr-none">
                <p class="whitespace-pre-wrap break-words">{{ msg.text }}</p>
                <span class="text-xs text-emerald-100 block mt-2">{{ msg.time }}</span>
              </div>
            </div>
          </div>
          
          <!-- 正在输入指示器 -->
          <div v-if="assistantTyping" class="flex items-start justify-start">
            <div class="flex items-start gap-3">
              <div class="w-8 h-8 rounded-full bg-emerald-100 flex items-center justify-center flex-shrink-0">
                <i class="fas fa-robot text-emerald-600 text-lg"></i>
              </div>
              <div class="bg-gray-100 p-4 rounded-lg rounded-tl-none">
                <div class="flex gap-1">
                  <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0s"></div>
                  <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
                  <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.4s"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 消息输入区域 -->
        <div class="border-t border-gray-200 bg-white p-4">
          <!-- 设置行 -->
          <div class="mb-4 flex gap-2">
            <input 
              v-model="apiKeyLocal" 
              type="password" 
              placeholder="API Key（将保存在本地）" 
              class="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
            >
            <input 
              v-model="baseUrlLocal" 
              placeholder="Base URL（例如 https://api.openai.com）" 
              class="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
            >
            <button 
              @click="saveSettings"
              class="bg-gray-200 hover:bg-gray-300 text-gray-700 px-4 py-2 rounded-lg transition-colors text-sm"
            >
              保存
            </button>
          </div>
          
          <!-- 输入行 -->
          <div class="flex gap-2">
            <input 
              v-model="userInput"
              @keyup.enter="onSend"
              placeholder="问我任何关于英语学习的问题..." 
              class="flex-1 border border-gray-300 rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
            >
            <button 
              :disabled="!canSend"
              @click="onSend"
              class="bg-emerald-500 hover:bg-emerald-600 disabled:bg-gray-400 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg font-medium transition-all shadow-sm hover:shadow transform hover:-translate-y-0.5"
            >
              <i class="fas fa-paper-plane mr-2"></i>发送
            </button>
          </div>
        </div>
      </div>
    </main>
    
    <!-- 引入EndBar组件 -->
    <EndBar />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/common/NavBar.vue'
import FriendItem from '@/components/business/FriendItem.vue'
import EndBar from '@/components/common/EndBar.vue'

const router = useRouter()

// 系统固定 prompt（可根据需要修改）
const SYSTEM_PROMPT = `你是一个友好且实用的英语学习助手，提供具体的建议、例句和练习题，尽量简洁且有步骤。`

const systemPromptShort = SYSTEM_PROMPT.slice(0, 40) + (SYSTEM_PROMPT.length > 40 ? '...' : '')

const userInput = ref('')
const messages = ref([
  { id: Date.now(), type: 'assistant', text: '你好！我是 AI 学习助手，可以在这里和我聊天。', time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }
])
const assistantTyping = ref(false)

// 本地保存的 API 设置
const apiKeyLocal = ref(localStorage.getItem('ai_api_key') || '')
const baseUrlLocal = ref(localStorage.getItem('ai_base_url') || '')

const canSend = computed(() => userInput.value.trim().length > 0 && (apiKeyLocal.value || baseUrlLocal.value))

const saveSettings = () => {
  localStorage.setItem('ai_api_key', apiKeyLocal.value)
  localStorage.setItem('ai_base_url', baseUrlLocal.value)
  alert('已保存到本地存储（仅本机）。')
}

const formatMessage = (text) => {
  // 简单换行支持
  return text.replace(/\n/g, '<br/>')
}

const scrollToBottom = async () => {
  await nextTick()
  const el = chatContainer.value
  if (el) el.scrollTop = el.scrollHeight
}

const chatContainer = ref(null)

onMounted(() => {
  scrollToBottom()
})

const appendUserMessage = (text) => {
  messages.value.push({ id: Date.now(), type: 'user', text, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) })
}

const appendAssistantMessagePlaceholder = () => {
  const msg = { id: Date.now() + 1, type: 'assistant', text: '', time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }
  messages.value.push(msg)
  return msg
}

async function onSend() {
  if (!canSend.value) return
  const text = userInput.value.trim()
  appendUserMessage(text)
  userInput.value = ''
  assistantTyping.value = true
  const assistantMsg = appendAssistantMessagePlaceholder()
  await scrollToBottom()

  try {
    // 在追加 chunk 之前清理可能的前缀
    await callApiStream(text, (chunk) => {
      const cleaned = sanitizeChunk(chunk, assistantMsg.text)
      assistantMsg.text += cleaned
      scrollToBottom()
    })
  } catch (err) {
    assistantMsg.text += '\n[错误] ' + (err.message || err)
  } finally {
    assistantTyping.value = false
    await scrollToBottom()
  }
}

// 清理回复中的噪音前缀
function sanitizeChunk(chunk, existingText) {
  try {
    if (!chunk || typeof chunk !== 'string') return chunk
    if (!existingText || existingText.length === 0) {
      return chunk.replace(/^\s*assistant\s*[:\-\s]*\s*/i, '')
    }
    return chunk
  } catch (e) {
    return chunk
  }
}

// 发送到用户提供的 base URL
async function callApiStream(userText, onChunk) {
  const apiKey = apiKeyLocal.value
  const baseUrl = baseUrlLocal.value.replace(/\/$/, '')
  if (!baseUrl) throw new Error('请先设置 Base URL')

  const url = baseUrl + '/v1/chat/completions'
  const payload = {
    model: 'gpt-3.5-turbo',
    messages: [
      { role: 'system', content: SYSTEM_PROMPT },
      { role: 'user', content: userText }
    ],
    stream: true
  }

  const res = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(apiKey ? { Authorization: `Bearer ${apiKey}` } : {})
    },
    body: JSON.stringify(payload)
  })

  if (!res.ok) {
    const text = await res.text()
    throw new Error(`API 错误: ${res.status} ${text}`)
  }

  const reader = res.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let done = false
  let buffer = ''

  while (!done) {
    const { value, done: d } = await reader.read()
    done = d
    if (value) {
      buffer += decoder.decode(value, { stream: true })
      // 尝试解析 data: 事件（OpenAI 风格）
      const parts = buffer.split('\n\n')
      buffer = parts.pop() // 留下不完整的部分
      for (const part of parts) {
        const line = part.trim()
        if (!line) continue
        if (line.startsWith('data:')) {
          const data = line.replace(/^data:\s?/, '')
          if (data === '[DONE]') {
            return
          }
          try {
            const parsed = JSON.parse(data)
            const delta = parsed.choices && parsed.choices[0] && (parsed.choices[0].delta?.content || parsed.choices[0].delta?.role)
            if (delta) onChunk(delta)
            else if (parsed.choices && parsed.choices[0] && parsed.choices[0].text) {
              onChunk(parsed.choices[0].text)
            }
          } catch (e) {
            // 非 JSON：可能是普通文本
            onChunk(data)
          }
        } else {
          // 直接文本块
          onChunk(line)
        }
      }
    }
  }
}

// 路由跳转
const activeTab = ref('ai-chat')

const gotoHome = () => {
  router.push({ name: 'Home' }).catch(() => {})
}

const gotoWordCheckIn = () => {
  router.push({ name: 'WordCheckIn' }).catch(() => {})
}

const gotoChat = () => {
  activeTab.value = 'chat'
  router.push({ name: 'Chat' }).catch(() => {})
}

const gotoAiChat = () => {
  router.push({ name: 'AiChat' }).catch(() => {})
}

const navItems = [
  { label: '首页', onClick: gotoHome, isActive: false },
  { label: '课程', path: '#' },
  { label: '题库', path: '#' },
  { label: '时间表', path: '#' },
  { label: '单词打卡', onClick: gotoWordCheckIn, isActive: false },
  { label: 'AI伴学', onClick: gotoAiChat, isActive: true }
]

const handleAddFriend = () => {
  console.log('添加好友')
}
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css');

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fadeIn {
  animation: fadeIn 0.3s ease-out;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .md\:w-96 {
    width: 100%;
    max-height: 40vh;
  }
}
</style>
