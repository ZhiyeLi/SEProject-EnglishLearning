<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 保留NavBar组件 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <button
          class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group"
        >
          <i class="fas fa-lightbulb text-lg" />
          <span
            class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap"
          >学习建议</span>
        </button>
        <button
          class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group"
        >
          <i class="fas fa-cog text-lg" />
          <span
            class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap"
          >设置</span>
        </button>
        <button
          class="relative ml-2 text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors"
        >
          <i class="fas fa-bell text-lg" />
          <span
            class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center animate-pulse"
          >3</span>
        </button>
      </template>
    </NavBar>

    <!-- 主内容区 -->
    <main class="flex-grow flex flex-col md:flex-row">
      <!-- 左侧好友列表：保留 -->
      <aside
        class="w-full md:w-96 bg-white border-r border-gray-200 shadow-sm md:h-[calc(100vh-64px)] sticky top-[64px] overflow-y-auto flex-shrink-0 z-20"
      >
        <div class="p-5 h-full flex flex-col">
          <!-- 搜索框：保留 -->
          <div class="relative mb-6">
            <input
              type="text"
              placeholder="搜索好友..."
              class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-base"
            >
            <i
              class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-lg"
            />
          </div>

          <!-- 好友列表区域 -->
          <div class="flex-grow overflow-y-auto -mx-2 px-2">
            <h3
              class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4 mt-2"
            >
              好友列表
            </h3>

            <ul class="space-y-2">
              <FriendItem
                name="示例好友"
                avatar="https://picsum.photos/seed/friend1/100/100"
                status="online"
                class="bg-emerald-50 border-l-4 border-emerald-500"
              >
                <template #actions>
                  <button
                    class="text-gray-600 hover:text-emerald-600 p-1 rounded-full hover:bg-emerald-50 transition-colors"
                  >
                    <i class="fas fa-comment" />
                  </button>
                </template>
              </FriendItem>
              <li>
                <button
                  class="w-full flex items-center justify-center p-3 text-emerald-600 text-sm border border-dashed border-emerald-200 rounded-lg hover:bg-emerald-50 transition-all hover:border-emerald-300 group"
                  @click="showAddFriendModal = true"
                >
                  <i
                    class="fas fa-plus-circle mr-2 group-hover:scale-110 transition-transform"
                  />
                  点击添加更多好友
                </button>
              </li>

              <!-- 好友请求按钮（放在添加好友按钮下方） -->
              <li>
                <button
                  class="w-full flex items-center justify-center p-3 text-emerald-600 text-sm border border-dashed border-emerald-200 rounded-lg hover:bg-emerald-50 transition-all hover:border-emerald-300 group relative"
                  @click="showFriendRequestModal = true"
                >
                  <i
                    class="fas fa-user-plus mr-2 group-hover:scale-110 transition-transform"
                  />
                  查看好友请求
                  <!-- 未处理请求数小红点 -->
                  <span
                    v-if="friendRequests.length > 0"
                    class="absolute top-1 right-6 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center animate-pulse"
                  >
                    {{ friendRequests.length }}
                  </span>
                </button>
              </li>
            </ul>
          </div>

          <!-- 底部功能选项：添加了点击事件 -->
          <div class="border-t border-gray-100 mt-4 pt-3 flex justify-around">
            <button
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'friends'
                  ? 'text-emerald-600 hover:text-emerald-700'
                  : 'text-gray-600 hover:text-emerald-600',
              ]"
              @click="gotoHome"
            >
              <i class="fas fa-users text-xl mb-1" />
              <span class="text-sm">好友</span>
            </button>

            <button
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'chat'
                  ? 'text-emerald-600 hover:text-emerald-700'
                  : 'text-gray-600 hover:text-emerald-600',
              ]"
              @click="gotoChat"
            >
              <i class="fas fa-comment text-xl mb-1" />
              <span class="text-sm">聊天</span>
            </button>

            <button
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'rank'
                  ? 'text-emerald-600 hover:text-emerald-700'
                  : 'text-gray-600 hover:text-emerald-600',
              ]"
              @click="activeTab = 'rank'"
            >
              <i class="fas fa-trophy text-xl mb-1" />
              <span class="text-sm">排行榜</span>
            </button>
          </div>
        </div>
      </aside>

      <!-- 中间内容区 -->
      <div
        class="flex-grow flex flex-col bg-gray-100 overflow-hidden"
        style="padding-right: 16px; padding-left: 16px; padding-top: 16px"
      >
        <!-- 聊天头部 -->
        <div
          class="bg-white border-b border-gray-200 p-4 flex items-center shadow-sm"
        >
          <button
            class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors mr-2"
            @click="gotoHome"
          >
            <i class="fas fa-arrow-left text-lg" />
          </button>

          <div class="flex-grow">
            <h3 class="font-semibold text-gray-800">
              示例好友
            </h3>
          </div>
        </div>

        <!-- 聊天消息区域 -->
        <div
          ref="chatContainer"
          class="flex-grow p-4 overflow-y-auto"
          style="
            height: 400px; /* 固定消息区高度 */
            max-height: calc(100vh - 220px); /* 适配屏幕最大高度 */
          "
        >
          <!-- 循环渲染消息列表 -->
          <div
            v-for="(msg, index) in messageList"
            :key="index"
            :class="[
              'flex items-start mb-4 animate-fadeIn',
              msg.isMine ? 'justify-end' : '',
            ]"
          >
            <!-- 对方消息头像 -->
            <img
              v-if="!msg.isMine"
              src="https://picsum.photos/seed/friend1/100/100"
              alt="示例好友"
              class="w-8 h-8 rounded-full object-cover mr-2 mt-1 flex-shrink-0"
            >

            <div class="max-w-[70%]">
              <div
                :class="[
                  'p-3 rounded-lg shadow-sm',
                  msg.isMine
                    ? 'bg-emerald-500 text-white rounded-tr-none'
                    : 'bg-white rounded-tl-none',
                ]"
              >
                <p :class="msg.isMine ? 'text-white' : 'text-gray-800'">
                  {{ msg.content }}
                </p>
              </div>
            </div>

            <!-- 我的消息头像（右侧） -->
            <img
              v-if="msg.isMine"
              src="https://picsum.photos/seed/me/100/100"
              alt="我"
              class="w-8 h-8 rounded-full object-cover ml-2 mt-1 flex-shrink-0"
            >
          </div>
        </div>

        <!-- 消息输入区域 -->
        <div class="bg-white border-t border-gray-200 p-3">
          <div class="flex items-center mb-2">
            <button
              class="text-gray-500 hover:text-emerald-600 p-2 transition-colors"
            >
              <i class="fas fa-smile" />
            </button>
            <button
              class="text-gray-500 hover:text-emerald-600 p-2 transition-colors"
            >
              <i class="fas fa-paperclip" />
            </button>
            <button
              class="text-gray-500 hover:text-emerald-600 p-2 transition-colors"
            >
              <i class="fas fa-image" />
            </button>
            <button
              class="text-gray-500 hover:text-emerald-600 p-2 transition-colors"
            >
              <i class="fas fa-microphone" />
            </button>
          </div>

          <div class="flex items-end">
            <textarea
              v-model="message"
              placeholder="输入消息..."
              class="flex-grow border border-gray-200 rounded-lg rounded-tr-none p-3 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all resize-none max-h-40"
              @keydown.enter.exact.prevent="sendMessage"
            />
            <button
              class="bg-emerald-500 hover:bg-emerald-600 text-white px-4 py-3 rounded-lg rounded-tl-none font-medium transition-all shadow-sm hover:shadow transform hover:-translate-y-0.5 ml-1"
              :disabled="!message.trim()"
              @click="sendMessage"
            >
              <i class="fas fa-paper-plane" />
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- 保留EndBar组件 -->
    <EndBar />

    <!-- 添加好友弹窗 -->
    <teleport to="body">
      <div
        v-if="showAddFriendModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fadeIn"
      >
        <div
          class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 transform transition-all scale-100"
        >
          <!-- 弹窗头部 -->
          <div
            class="px-6 py-4 border-b border-gray-200 flex justify-between items-center"
          >
            <h3 class="text-lg font-semibold text-gray-800">
              添加好友
            </h3>
            <button
              class="text-gray-400 hover:text-gray-600 transition-colors"
              @click="showAddFriendModal = false"
            >
              <i class="fas fa-times text-lg" />
            </button>
          </div>

          <!-- 弹窗内容 -->
          <div class="px-6 py-4">
            <div class="space-y-4">
              <!-- 搜索好友输入框 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">搜索好友（用户名/ID）</label>
                <div class="relative">
                  <input
                    v-model="searchFriendValue"
                    type="text"
                    placeholder="请输入好友信息..."
                    class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-base"
                  >
                  <i
                    class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
                  />
                </div>
              </div>

              <!-- 搜索结果 -->
              <div
                v-if="searchFriendValue"
                class="max-h-40 overflow-y-auto border rounded-lg"
              >
                <div
                  v-if="searchResults.length === 0"
                  class="p-4 text-center text-gray-500"
                >
                  未找到相关好友
                </div>
                <div
                  v-else
                  class="divide-y"
                >
                  <div
                    v-for="(friend, index) in searchResults"
                    :key="index"
                    class="flex items-center p-3 hover:bg-gray-50 transition-colors"
                  >
                    <img
                      :src="friend.avatar"
                      alt="好友头像"
                      class="w-10 h-10 rounded-full object-cover mr-3"
                    >
                    <div class="flex-grow">
                      <p class="font-medium text-gray-800">
                        {{ friend.name }}
                      </p>
                      <p class="text-xs text-gray-500">
                        ID: {{ friend.id }}
                      </p>
                    </div>
                    <button
                      class="bg-emerald-500 hover:bg-emerald-600 text-white px-3 py-1 rounded-lg text-sm transition-colors"
                      @click="addFriend(friend)"
                    >
                      添加
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 弹窗底部 -->
          <div
            class="px-6 py-3 border-t border-gray-200 flex justify-end space-x-2"
          >
            <button
              class="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
              @click="showAddFriendModal = false"
            >
              取消
            </button>
            <button
              class="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg transition-colors"
              :disabled="!friendId && !searchFriendValue"
              @click="confirmAddFriend"
            >
              确认添加
            </button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- 好友请求确认弹窗 -->
    <teleport to="body">
      <div
        v-if="showFriendRequestModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fadeIn"
      >
        <div
          class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 transform transition-all scale-100"
        >
          <!-- 弹窗头部 -->
          <div
            class="px-6 py-4 border-b border-gray-200 flex justify-between items-center"
          >
            <h3 class="text-lg font-semibold text-gray-800">
              好友请求
            </h3>
            <button
              class="text-gray-400 hover:text-gray-600 transition-colors"
              @click="showFriendRequestModal = false"
            >
              <i class="fas fa-times text-lg" />
            </button>
          </div>

          <!-- 弹窗内容 -->
          <div class="px-6 py-4 max-h-80 overflow-y-auto">
            <div
              v-if="friendRequests.length === 0"
              class="p-8 text-center text-gray-500"
            >
              <i class="fas fa-inbox text-4xl mb-2 text-gray-300" />
              <p>暂无未处理的好友请求</p>
            </div>
            <div
              v-else
              class="space-y-3 divide-y"
            >
              <div
                v-for="(request, index) in friendRequests"
                :key="index"
                class="py-3 flex items-center justify-between"
              >
                <div class="flex items-center">
                  <img
                    :src="request.avatar"
                    alt="请求者头像"
                    class="w-12 h-12 rounded-full object-cover mr-3"
                  >
                  <div>
                    <p class="font-medium text-gray-800">
                      {{ request.name }}
                    </p>
                    <p class="text-xs text-gray-500">
                      ID: {{ request.id }}
                    </p>
                    <p class="text-xs text-gray-400 mt-1">
                      {{ request.time }}
                    </p>
                  </div>
                </div>
                <div class="flex space-x-2">
                  <button
                    class="bg-green-500 hover:bg-green-600 text-white px-3 py-1 rounded-lg text-sm transition-colors"
                    @click="acceptFriendRequest(index)"
                  >
                    <i class="fas fa-check mr-1" /> 接受
                  </button>
                  <button
                    class="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded-lg text-sm transition-colors"
                    @click="rejectFriendRequest(index)"
                  >
                    <i class="fas fa-times mr-1" /> 拒绝
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 弹窗底部 -->
          <div class="px-6 py-3 border-t border-gray-200 flex justify-end">
            <button
              class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg transition-colors"
              @click="showFriendRequestModal = false"
            >
              关闭
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
// 引入路由
import { useRouter } from "vue-router";
import { onMounted, ref, watch } from "vue";
import NavBar from "@/components/common/NavBar.vue";
import FriendItem from "@/components/business/FriendItem.vue";
import EndBar from "@/components/common/EndBar.vue";

// 初始化路由
const router = useRouter();

// 聊天相关数据
const message = ref("");
const chatContainer = ref(null);

// 底部tab状态：只定义一次
const activeTab = ref("chat");

// 添加好友弹窗相关响应式变量
const showAddFriendModal = ref(false);
const searchFriendValue = ref("");
const friendId = ref("");
const searchResults = ref([]);

// 好友请求弹窗相关响应式变量
const showFriendRequestModal = ref(false);

// 模拟消息列表数据，后期改为数据库信息
const messageList = ref([
  {
    content: "测试聊天信息",
    isMine: false, // false=对方消息，true=我的消息
  },
  {
    content: "测试聊天信息",
    isMine: true,
  },
]);

// 路由跳转函数
function gotoChat() {
  activeTab.value = "chat";
  router.push({ name: "Chat" }).catch(() => {});
}

function gotoHome() {
  activeTab.value = "friends";
  router.push({ name: "Home" }).catch(() => {});
}

function gotoAiChat() {
  router.push({ name: "AiChat" }).catch(() => {});
}

const navItems = [
  { label: "首页", onClick: gotoHome, isActive: false },
  { label: "课程", path: "#" },
  {
    label: "题库",
    onClick: () => router.push({ name: "QuestionBank" }).catch(() => {}),
  },
  { label: "时间表", path: "#" },
  { label: "单词打卡", path: "#" },
  { label: "AI伴学", onClick: gotoAiChat },
];

// 发送消息
const sendMessage = () => {
  const content = message.value.trim();
  if (content) {
    // 1. 添加消息到消息列表
    messageList.value.push({
      content: content,
      isMine: true,
    });

    // 2. 模拟对方回复（可选：不需要则删除此段）
    setTimeout(() => {
      messageList.value.push({
        content: `我收到了：${content}`,
        isMine: false,
      });
      scrollToBottom();
    }, 1000);

    // 3. 清空输入框
    message.value = "";

    // 4. 滚动到底部
    scrollToBottom();

    // 5. 控制台日志
    console.log("发送消息:", content);
  }
};

// 滚动到聊天底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    // 加个小延迟确保DOM已更新
    setTimeout(() => {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }, 100);
  }
};

// 监听消息变化，自动滚动到底部
watch(message, () => {
  scrollToBottom();
});

onMounted(() => {
  // 页面滚动时导航栏样式变化
  const header = document.querySelector("header");
  window.addEventListener("scroll", () => {
    if (window.scrollY > 10) {
      header?.classList.add("shadow");
    } else {
      header?.classList.remove("shadow");
    }
  });

  // 初始滚动到底部
  scrollToBottom();
});

//添加好友弹窗部分

// 模拟好友数据,后期改为数据库调用
const mockFriends = [
  {
    id: "1001",
    name: "模拟用户1",
    avatar: "https://picsum.photos/seed/friend1001/100/100",
  },
  {
    id: "1002",
    name: "模拟用户2",
    avatar: "https://picsum.photos/seed/friend1002/100/100",
  },
  {
    id: "1003",
    name: "模拟用户3",
    avatar: "https://picsum.photos/seed/friend1003/100/100",
  },
  {
    id: "1004",
    name: "模拟用户4",
    avatar: "https://picsum.photos/seed/friend1004/100/100",
  },
  {
    id: "1005",
    name: "模拟用户5",
    avatar: "https://picsum.photos/seed/friend1005/100/100",
  },
];

watch(searchFriendValue, (val) => {
  if (val) {
    // 模拟搜索延迟
    setTimeout(() => {
      const results = mockFriends.filter(
        (friend) => friend.name.includes(val) || friend.id.includes(val)
      );
      searchResults.value = results;
    }, 300);
  } else {
    searchResults.value = [];
  }
});

// 新增添加好友相关方法
const searchFriendById = () => {
  if (friendId.value) {
    const friend = mockFriends.find((f) => f.id === friendId.value);
    if (friend) {
      searchResults.value = [friend];
      searchFriendValue.value = friend.name;
    } else {
      searchResults.value = [];
      // 提示未找到
      alert("未找到该ID的好友");
    }
  }
};

const addFriend = (friend) => {
  // 模拟添加好友逻辑，可替换为真实API调用
  console.log("添加好友:", friend);
  alert(`已发送好友请求给 ${friend.name}`);
  // 关闭弹窗并重置表单
  showAddFriendModal.value = false;
  searchFriendValue.value = "";
  friendId.value = "";
  searchResults.value = [];
};

const confirmAddFriend = () => {
  if (searchResults.value.length > 0) {
    addFriend(searchResults.value[0]);
  } else if (friendId.value) {
    searchFriendById();
  }
};

// 好友请求弹窗部分
// 模拟未处理的好友请求数据
const friendRequests = ref([
  {
    id: "2001",
    name: "模拟请求1",
    avatar: "https://picsum.photos/seed/friend2001/100/100",
  },
  {
    id: "2002",
    name: "模拟请求2",
    avatar: "https://picsum.photos/seed/friend2002/100/100",
  },
]);

// 接受好友请求
const acceptFriendRequest = (index) => {
  const request = friendRequests.value[index];
  console.log("接受好友请求:", request);
  // 模拟添加到好友列表逻辑
  alert(`已接受 ${request.name} 的好友请求，对方已添加到你的好友列表`);
  // 从请求列表中移除
  friendRequests.value.splice(index, 1);
  // 可在这里调用真实API：接受好友请求
};

// 拒绝好友请求
const rejectFriendRequest = (index) => {
  const request = friendRequests.value[index];
  console.log("拒绝好友请求:", request);
  // 模拟拒绝逻辑
  alert(`已拒绝 ${request.name} 的好友请求`);
  // 从请求列表中移除
  friendRequests.value.splice(index, 1);
  // 可在这里调用真实API：拒绝好友请求
};
</script>

<style>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css");
@tailwind base;
@tailwind components;
@tailwind utilities;

/* 自定义滚动条样式 */
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
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fadeIn {
  animation: fadeIn 0.3s ease-out;
}

/* 聊天区域样式 */
textarea {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}

textarea::-webkit-scrollbar {
  width: 6px;
}
</style>
