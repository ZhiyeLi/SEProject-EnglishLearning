<template>
  <div class="ai-chat-page">
    <div class="chat-shell">
      <header class="chat-header">
        <div class="left">
          <h2><i class="fas fa-robot"></i> AI 聊天助手</h2>
          <div class="subtitle">固定指令：<span class="prompt-badge">{{ systemPromptShort }}</span></div>
        </div>
        <div class="header-actions">
          <button class="back" @click="goBack" title="返回首页"><i class="fas fa-arrow-left"></i></button>
        </div>
      </header>

      <div class="chat-container-box">
        <div class="chat-flow" ref="chatContainer">
          <div v-for="msg in messages" :key="msg.id" :class="['message', msg.type]">
            <div class="avatar"><i :class="msg.type === 'user' ? 'fas fa-user' : 'fas fa-robot'"></i></div>
            <div class="message-content">
              <p v-html="formatMessage(msg.text)"></p>
              <span class="time">{{ msg.time }}</span>
            </div>
          </div>
          <div v-if="assistantTyping" class="message assistant typing">
            <div class="avatar"><i class="fas fa-robot"></i></div>
            <div class="message-content">
              <p>正在生成回复<span class="dot">.</span><span class="dot">.</span><span class="dot">.</span></p>
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <div class="input-row">
            <input v-model="userInput" @keyup.enter="onSend" placeholder="问我任何关于英语学习的问题..." />
            <button :disabled="!canSend" @click="onSend">发送</button>
          </div>

          <div class="settings-row">
            <input v-model="apiKeyLocal" type="password" placeholder="API Key（将保存在本地）" />
            <input v-model="baseUrlLocal" placeholder="Base URL（例如 https://api.openai.com）" />
            <button @click="saveSettings">保存设置</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted, nextTick, computed } from 'vue'

const currentPage = inject('currentPage')

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
    // 在追加 chunk 之前清理可能的前缀（例如开头多出的 "assistant"）
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

// 如果回复的开头包含 'assistant' 或 'assistant:' 等噪音，去掉它
function sanitizeChunk(chunk, existingText) {
  try {
    if (!chunk || typeof chunk !== 'string') return chunk
    // 只有在当前消息是空的情况下才移除开头的标记，避免中间出现的字样被误删
    if (!existingText || existingText.length === 0) {
      // 匹配开头的 assistant、assistant:、assistant - 等形式（不区分大小写）
      return chunk.replace(/^\s*assistant\s*[:\-\s]*\s*/i, '')
    }
    return chunk
  } catch (e) {
    return chunk
  }
}

// 发送到用户提供的 base URL，尝试兼容 OpenAI Chat Completions API 的流式返回
async function callApiStream(userText, onChunk) {
  const apiKey = apiKeyLocal.value
  const baseUrl = baseUrlLocal.value.replace(/\/$/, '')
  if (!baseUrl) throw new Error('请先设置 Base URL')

  const url = baseUrl + '/v1/chat/completions'
  const payload = {
    model: 'gpt-3.5-turbo',
    messages: [
      { role: 'system', content: SYSTEM_PROMPT },
      // 将历史的 user+assistant 可选发送全部上下文
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

const goBack = () => {
  if (currentPage) currentPage.value = 'home'
}
</script>

<style scoped>
.ai-chat-page { padding: 24px 0; }
.chat-shell { max-width: 960px; margin: 0 auto; }
.chat-header { display:flex; align-items:center; justify-content:space-between; padding:16px 20px; background: linear-gradient(90deg, rgba(255,255,255,0.6), rgba(255,255,255,0.6)); border-radius:12px 12px 0 0; }
.chat-header .left { display:flex; flex-direction:column; }
.chat-header h2 { margin:0; display:flex; align-items:center; gap:8px; color:var(--primary); }
.chat-header .subtitle { font-size:12px; color:var(--gray-600); margin-top:6px }
.prompt-badge { background:var(--primary-light); color:var(--primary-dark); padding:2px 8px; border-radius:8px; font-size:12px }
.chat-container-box { background:var(--white); border:1px solid var(--gray-200); border-radius:12px; overflow:hidden; display:flex; flex-direction:column; height: calc(80vh); }
.chat-flow { padding:16px; flex:1; overflow-y:auto; display:flex; flex-direction:column; gap:12px }
.message { display:flex; gap:12px; max-width:85%; }
.message.assistant { align-self:flex-start }
.message.user { align-self:flex-end; flex-direction:row-reverse }
.avatar { width:36px; height:36px; border-radius:50%; display:flex; align-items:center; justify-content:center; background:var(--gray-100) }
.message-content { background:var(--gray-50); padding:10px 14px; border-radius:10px; }
.message.user .message-content { background:var(--primary-light); color:var(--primary-dark) }
.message .time { display:block; font-size:11px; color:var(--gray-500); margin-top:6px }
.typing .message-content { font-style:italic; color:var(--gray-600) }
.chat-input-area { border-top:1px solid var(--gray-200); padding:12px; background:linear-gradient(180deg, rgba(255,255,255,0.9), rgba(255,255,255,0.9)); }
.input-row { display:flex; gap:8px }
.input-row input { flex:1; padding:10px 12px; border:1px solid var(--gray-300); border-radius:8px }
.input-row button { padding:10px 16px; border-radius:8px; background:var(--primary); color:var(--white); border:none }
.settings-row { display:flex; gap:8px; margin-top:10px }
.settings-row input { flex:1; padding:8px 10px; border:1px solid var(--gray-300); border-radius:6px }
.settings-row button { padding:8px 12px; border-radius:6px; background:var(--gray-100); border:1px solid var(--gray-200) }

/* small responsive */
@media (max-width: 768px) {
  .chat-shell { padding:0 8px }
  .chat-container-box { height: calc(70vh) }
}
</style>
