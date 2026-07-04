<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <NavBar :nav-items="navItems">
      <template #actions>
        <ActionButtons
          @suggestions="openSuggestions"
          @settings="gotoSettings"
          @home="gotoHome"
          @notifications="() => {}"
        />
      </template>
    </NavBar>

    <main class="flex-grow flex flex-col md:flex-row gap-4 p-6">
      <!-- 左侧历史 -->
      <div class="w-full md:w-64 bg-white rounded-lg shadow-sm flex flex-col">
        <div class="bg-emerald-600 p-4 flex justify-between text-white">
          <span>对话历史</span>
          <button @click="createNewChat">+</button>
        </div>

        <div class="flex-grow overflow-y-auto">
          <div v-if="chatHistory.length === 0" class="p-4 text-gray-400">
            暂无对话
          </div>

          <div v-else>
            <div
              v-for="(chat, index) in chatHistory"
              :key="chat.sessionId"
              class="p-3 cursor-pointer hover:bg-emerald-50"
              :class="currentChatId === chat.sessionId ? 'bg-emerald-50' : ''"
              @click="switchChat(chat.sessionId)"
            >
              <div class="font-medium truncate">
                {{ chat.title || "新对话 " + (chatHistory.length - index) }}
              </div>
              <div class="text-xs text-gray-400">
                {{ formatChatTime(chat.createdAt) }}
              </div>

              <button
                class="text-red-400 text-xs mt-1"
                @click.stop="deleteChat(chat.sessionId)"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 主聊天区 -->
      <div class="flex-grow flex justify-center">
        <div class="w-full h-[600px] bg-white rounded-lg shadow flex flex-col">
          <!-- header -->
          <div class="bg-emerald-600 text-white p-4">AI 学习助手</div>

          <!-- messages -->
          <div
            ref="chatContainer"
            class="flex-grow p-4 overflow-y-auto space-y-3"
          >
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="flex"
              :class="msg.type === 'user' ? 'justify-end' : 'justify-start'"
            >
              <div
                class="max-w-[75%] p-3 rounded-lg"
                :class="
                  msg.type === 'user'
                    ? 'bg-emerald-500 text-white'
                    : 'bg-gray-100'
                "
              >
                <div v-html="formatMessage(msg.text)" />
                <div class="text-xs mt-1 opacity-60">
                  {{ msg.time }}
                </div>
              </div>
            </div>

            <div v-if="assistantTyping" class="text-gray-400 text-sm">
              AI 正在输入...
            </div>
          </div>

          <!-- input -->
          <div class="p-3 border-t flex gap-2">
            <input
              v-model="userInput"
              class="flex-1 border rounded px-3 py-2"
              placeholder="请输入问题..."
              @keyup.enter="onSend"
            />
            <button
              :disabled="!canSend"
              class="bg-emerald-500 text-white px-4 py-2 rounded disabled:bg-gray-300"
              @click="onSend"
            >
              发送
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";

import NavBar from "@/components/common/NavBar.vue";
import ActionButtons from "@/components/common/ActionButtons.vue";

import { aiChatApi } from "@/api/aiChat";
import { sendRagMessage } from "@/api/rag";

const router = useRouter();

/* ===================== 输入 ===================== */
const userInput = ref("");
const canSend = computed(() => userInput.value.trim().length > 0);

/* ===================== 消息 ===================== */
const messages = ref([
  {
    id: Date.now(),
    type: "assistant",
    text: "你好，我是 AI 学习助手。",
    time: new Date().toLocaleTimeString(),
  },
]);

const assistantTyping = ref(false);
const chatContainer = ref(null);

/* ===================== 会话 ===================== */
const chatHistory = ref([]);
const currentChatId = ref(null);

/* ===================== 初始化 ===================== */
onMounted(async () => {
  await initializeChatHistory();
  scrollToBottom();
});

const initializeChatHistory = async () => {
  const res = await aiChatApi.getSessions();
  if (res?.code === 200) {
    chatHistory.value = res.data || [];

    if (chatHistory.value.length === 0) {
      await createNewChat();
    } else {
      currentChatId.value = chatHistory.value[0].sessionId;
      loadSessionMessages(currentChatId.value);
    }
  }
};

/* ===================== 加载消息 ===================== */
const loadSessionMessages = async (sessionId) => {
  const res = await aiChatApi.getSessionMessages(sessionId);

  if (res?.code === 200) {
    messages.value = (res.data || []).map((m) => ({
      id: m.messageId,
      type: m.role === "user" ? "user" : "assistant",
      text: m.content,
      time: new Date(m.createdAt).toLocaleTimeString(),
    }));
  }

  await nextTick();
  scrollToBottom();
};

/* ===================== 新建会话 ===================== */
const createNewChat = async () => {
  const res = await aiChatApi.createSession();
  if (res?.code === 200) {
    chatHistory.value.unshift(res.data);
    currentChatId.value = res.data.sessionId;

    messages.value = [
      {
        id: Date.now(),
        type: "assistant",
        text: "你好，我是 AI 学习助手。",
        time: new Date().toLocaleTimeString(),
      },
    ];
  }
};

/* ===================== 切换 ===================== */
const switchChat = (id) => {
  currentChatId.value = id;
  loadSessionMessages(id);
};

/* ===================== 删除 ===================== */
const deleteChat = async (id) => {
  await aiChatApi.deleteSession(id);

  chatHistory.value = chatHistory.value.filter((c) => c.sessionId !== id);

  if (currentChatId.value === id && chatHistory.value.length > 0) {
    switchChat(chatHistory.value[0].sessionId);
  }
};

/* ===================== 发送 ===================== */
async function onSend() {
  if (!canSend.value) return;

  const text = userInput.value.trim();

  appendUser(text);
  userInput.value = "";

  assistantTyping.value = true;

  const assistantMsg = appendAssistant("");

  await scrollToBottom();

  let success = false;

  try {
    const res = await sendRagMessage(text);

    if (res?.code === 200 && res.data?.reply) {
      assistantMsg.text = res.data.reply;
      success = true;
    } else {
      assistantMsg.text = "请求失败";
    }
  } catch (e) {
    assistantMsg.text = "错误：" + e.message;
  } finally {
    assistantTyping.value = false;
    await scrollToBottom();

    if (success && currentChatId.value) {
      aiChatApi.saveMessage(
        currentChatId.value,
        "assistant",
        assistantMsg.text,
      );
    }

    const session = chatHistory.value.find(
      (c) => c.sessionId === currentChatId.value,
    );

    if (session && !session.title) {
      const first = messages.value.find((m) => m.type === "user");
      if (first) {
        session.title = first.text.slice(0, 30);
        aiChatApi.updateSessionTitle(currentChatId.value, session.title);
      }
    }

    if (session) session.messageCount = messages.value.length;
  }
}

/* ===================== message utils ===================== */
const appendUser = (text) => {
  messages.value.push({
    id: Date.now(),
    type: "user",
    text,
    time: new Date().toLocaleTimeString(),
  });

  aiChatApi.saveMessage(currentChatId.value, "user", text);
};

const appendAssistant = (text) => {
  const msg = {
    id: Date.now() + 1,
    type: "assistant",
    text,
    time: new Date().toLocaleTimeString(),
  };

  messages.value.push(msg);
  return msg;
};

/* ===================== UI ===================== */
const scrollToBottom = async () => {
  await nextTick();
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

const formatMessage = (t) => t.replace(/\n/g, "<br/>");

const formatChatTime = (t) => new Date(t).toLocaleString();

/* ===================== 路由 ===================== */
const gotoHome = () => router.push({ name: "Home" });
const gotoSettings = () => router.push({ name: "Settings" });

const navItems = [
  { label: "首页", onClick: gotoHome },
  { label: "AI伴学", isActive: true },
];
</script>
