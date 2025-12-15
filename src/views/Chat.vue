<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 保留NavBar组件 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <ActionButtons
          @suggestions="() => {}"
          @settings="gotoSettings"
          @home="gotoHome"
          @notifications="() => {}"
        />
      </template>
    </NavBar>

    <!-- 主内容区 -->
    <main class="flex-grow flex flex-col md:flex-row">
      <!-- 左侧好友列表：保留 -->
      <aside class="w-full md:w-96 bg-white border-r border-gray-200 shadow-sm md:h-[calc(100vh-64px)] sticky top-[64px] overflow-y-auto flex-shrink-0 z-20">
        <div class="p-5 h-full flex flex-col">
          <!-- 搜索框：保留 -->
          <div class="relative mb-6">
            <input 
              type="text" 
              placeholder="搜索好友..." 
              class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-base"
            >
            <i class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-lg" />
          </div>
          
          <!-- 好友列表区域 -->
          <div class="flex-grow overflow-y-auto -mx-2 px-2">
            <h3 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4 mt-2">
              好友列表
            </h3>
            
            <ul class="space-y-2">
              <!-- 加载状态 -->
              <li
                v-if="loading"
                class="p-3 text-center text-gray-500"
              >
                <i class="fas fa-spinner fa-spin mr-2" />加载中...
              </li>
              <!-- 错误提示 -->
              <li
                v-else-if="error"
                class="p-3 text-center text-red-500"
              >
                {{ error }}
              </li>
              <!-- 无好友提示 -->
              <li
                v-else-if="friendList.length === 0"
                class="p-3 text-center text-gray-500"
              >
                暂无好友，快去添加吧！
              </li>
              <!-- 遍历真实好友列表 -->
              <li
                v-for="friend in friendList"
                :key="friend.id"
              >
                <FriendItem 
                  :name="friend.name" 
                  :avatar="friend.avatar || 'https://picsum.photos/seed/default/100/100'" 
                  :status="friend.status || 'offline'" 
                  :class="friend.id === currentFriendId ? 'bg-emerald-50 border-l-4 border-emerald-500' : ''"
                >
                  <template #actions>
                    <button
                      class="text-gray-600 hover:text-emerald-600 p-1 rounded-full hover:bg-emerald-50 transition-colors"
                      @click="selectFriend(friend)"
                    >
                      <i class="fas fa-comment" />
                    </button>
                  </template>
                </FriendItem>
              </li>
              <li>
                <button 
                  class="w-full flex items-center justify-center p-3 text-emerald-600 text-sm border border-dashed border-emerald-200 rounded-lg hover:bg-emerald-50 transition-all hover:border-emerald-300 group"
                  @click="showAddFriendModal = true"
                >
                  <i class="fas fa-plus-circle mr-2 group-hover:scale-110 transition-transform" />
                  点击添加更多好友
                </button>
              </li>

              <!-- 好友请求按钮（放在添加好友按钮下方） -->
              <li>
                <button 
                  class="w-full flex items-center justify-center p-3 text-emerald-600 text-sm border border-dashed border-emerald-200 rounded-lg hover:bg-emerald-50 transition-all hover:border-emerald-300 group relative"
                  @click="showFriendRequestModal = true"
                >
                  <i class="fas fa-user-plus mr-2 group-hover:scale-110 transition-transform" />
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
                activeTab === 'friends' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
              ]"
              @click="gotoHome"
            >
              <i class="fas fa-home text-xl mb-1" />
              <span class="text-sm">首页</span>
            </button>

            <button
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'chat' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
              ]"
              @click="gotoChat"
            >
              <i class="fas fa-comment text-xl mb-1" />
              <span class="text-sm">聊天</span>
            </button>

            <button
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'rank' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
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
        style="
      padding-right: 16px;
      padding-left: 16px;
      padding-top: 16px;
      "
      >
        <!-- 聊天头部 -->
        <div v-if="currentFriend" class="bg-white border-b border-gray-200 p-4 flex items-center shadow-sm">
          <!-- 当前选中好友的头像和名称 -->
          <div class="flex items-center flex-grow">
            <img 
              :src="currentFriend?.avatar || 'https://picsum.photos/seed/default/100/100'" 
              :alt="currentFriend?.name" 
              class="w-8 h-8 rounded-full object-cover mr-2"
            >
            <h3 class="font-semibold text-gray-800">
              {{ currentFriend?.name || '选择好友' }}
            </h3>
            <!-- 显示好友在线状态 -->
            <span 
            v-if="currentFriend?.status === 'online'"
            class="ml-2 w-2 h-2 bg-green-500 rounded-full"
            title="在线"
          ></span>
        </div>
      </div>
        
        <!-- 聊天消息区域 -->
        <div
          ref="chatContainer"
          class="flex-grow p-4 overflow-y-auto relative"
          style="
          height: 400px; /* 固定消息区高度 */
          max-height: calc(100vh - 220px); /* 适配屏幕最大高度 */
          "
        >
        <!-- 消息加载状态 -->
        <div v-if="messageLoading" class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-gray-500">
          <i class="fas fa-spinner fa-spin mr-2" />加载消息中...
        </div>

        <!-- 未选择好友提示 -->
        <div v-else-if="!currentFriend" class="h-full flex items-center justify-center text-gray-500">
          <div class="flex flex-col items-center">
            <i class="fas fa-comment-dots text-4xl text-gray-300 block"></i>
            <p class="mt-3">请选择一个好友开始聊天</p>
          </div>
        </div>

        <!-- 无消息提示 -->
        <div v-else-if="!messageList || messageList.length === 0" class="h-full flex items-center justify-center text-gray-500">
          <div class="flex flex-col items-center"> 
            <i class="fas fa-paper-plane text-4xl mb-2 text-gray-300 block mr-0"></i> 
            <p class="ml-0 mt-3">暂无消息，开始聊天吧！</p> 
          </div>
        </div>

        <!-- 循环渲染消息列表 -->
        <div 
          v-for="(msg, index) in messageList" 
          :key="msg.id || index" 
          :class="[
            'flex items-start mb-4 animate-fadeIn',
          msg.isMine ? 'justify-end' : ''
        ]"
      >
        <!-- 对方消息头像（用当前选中好友的头像） -->
        <img 
          v-if="!msg.isMine"
          :src="currentFriend?.avatar || 'https://picsum.photos/seed/friend1/100/100'" 
          :alt="currentFriend?.name" 
          class="w-8 h-8 rounded-full object-cover mr-2 mt-1 flex-shrink-0"
        >

        <div class="max-w-[70%]">
          <div 
            :class="[
              'p-3 rounded-lg shadow-sm',
              msg.isMine 
                ? 'bg-emerald-500 text-white rounded-tr-none' 
                : 'bg-white rounded-tl-none'
          ]"
        >
          <p :class="msg.isMine ? 'text-white' : 'text-gray-800'">
            {{ msg.content }}
          </p>
          <!-- 消息时间 -->
          <p class="text-xs mt-1 opacity-70">
            {{ formatTime(msg.time) }}
          </p>
        </div>
      </div>
    
            <!-- 我的消息头像 -->
            <img 
            v-if="msg.isMine"
            src="https://picsum.photos/seed/me/100/100" 
            alt="我" 
            class="w-8 h-8 rounded-full object-cover ml-2 mt-1 flex-shrink-0"
          >   
      </div>
    </div>
        
        <!-- 消息输入区域 -->
        <div v-if="currentFriend" class="bg-white border-t border-gray-200 p-3">
          <div class="flex items-center mb-2">
            <button class="text-gray-500 hover:text-emerald-600 p-2 transition-colors">
              <i class="fas fa-smile" />
            </button>
            <button class="text-gray-500 hover:text-emerald-600 p-2 transition-colors">
              <i class="fas fa-paperclip" />
            </button>
            <button class="text-gray-500 hover:text-emerald-600 p-2 transition-colors">
              <i class="fas fa-image" />
            </button>
            <button class="text-gray-500 hover:text-emerald-600 p-2 transition-colors">
              <i class="fas fa-microphone" />
            </button>
          </div>
          
          <div class="flex items-center">
            <textarea 
              v-model="message"
              placeholder="输入消息..." 
              class="flex-grow border border-gray-200 rounded-lg rounded-tr-none p-3 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all resize-none max-h-40 h-12"
              @keydown.enter.exact.prevent="sendMessage"
            />
            
            <button 
              class="bg-emerald-500 hover:bg-emerald-600 text-white px-4 py-3 rounded-lg rounded-tl-none font-medium transition-all shadow-sm hover:shadow transform hover:-translate-y-0.5 ml-1 h-12 flex items-center justify-center"
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
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 transform transition-all scale-100">
          <!-- 弹窗头部 -->
          <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
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
                <label class="block text-sm font-medium text-gray-700 mb-1">搜索好友（用户名/ID/邮箱）</label>
                <div class="relative">
                  <input 
                    v-model="searchFriendValue"
                    type="text" 
                    placeholder="请输入好友信息..." 
                    class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-base"
                  >
                  <i class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
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
        </div>
      </div>
    </teleport>
    
    <!-- 好友请求确认弹窗 -->
    <teleport to="body">
      <div
        v-if="showFriendRequestModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fadeIn"
      >
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 transform transition-all scale-100">
          <!-- 弹窗头部（原有） -->
          <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
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

          <!-- 弹窗内容（修改：添加加载状态） -->
          <div class="px-6 py-4 max-h-80 overflow-y-auto">
            <!-- 加载状态 -->
            <div
              v-if="friendRequestLoading"
              class="p-8 text-center text-gray-500"
            >
              <i class="fas fa-spinner fa-spin text-4xl mb-2 text-gray-300" />
              <p>加载中...</p>
            </div>
            <!-- 无请求 -->
            <div
              v-else-if="friendRequests.length === 0"
              class="p-8 text-center text-gray-500"
            >
              <i class="fas fa-inbox text-4xl mb-2 text-gray-300" />
              <p>暂无未处理的好友请求</p>
            </div>
            <!-- 有请求列表（原有：保留time显示） -->
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
                      ID: {{ request.requesterId }} <!-- 显示发送者ID（替换原有request.id，因为request.id是请求的主键） -->
                    </p>
                    <p class="text-xs text-gray-400 mt-1">
                      {{ request.time }} <!-- 显示格式化后的时间 -->
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
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
// 引入路由
import { useRouter } from 'vue-router';
import { onMounted, ref, watch, onBeforeUnmount } from 'vue';
import { io } from 'socket.io-client';
import { useUserStore } from '@/store/modules/user';
import NavBar from '@/components/common/NavBar.vue';
import ActionButtons from '@/components/common/ActionButtons.vue';
import FriendItem from '@/components/business/FriendItem.vue';
import EndBar from '@/components/common/EndBar.vue';

import { friendApi } from '@/api/friend'; // 后端好友接口

const searchLoading = ref(false); // 搜索用户的加载状态

const loading = ref(false); // 加载状态
const error = ref(''); // 错误提示
const friendList = ref([]); // 后端返回的真实好友列表
const currentFriendId = ref(''); // 当前选中好友ID

// 初始化路由
const router = useRouter();

// 聊天相关数据
const message = ref('');
const chatContainer = ref(null);

// 底部tab状态：只定义一次
const activeTab = ref('chat');

// 添加好友弹窗相关响应式变量
const showAddFriendModal = ref(false);
const searchFriendValue = ref('');
const friendId = ref('');
const searchResults = ref([]);

// 好友请求弹窗相关响应式变量
const showFriendRequestModal = ref(false);

// 路由跳转函数
function gotoChat() {
  activeTab.value = 'chat';
  router.push({ name: 'Chat' }).catch(() => {});
}

function gotoHome() {
  activeTab.value = 'friends';
  router.push({ name: 'Home' }).catch(() => {});
}

function gotoAiChat() {
  router.push({ name: 'AiChat' }).catch(() => {});
}

const gotoWordCheckIn = () => {
  router.push({ name: "WordCheckIn" }).catch(() => {});
};

const gotoTimeTable = () => {
  router.push({ name: "TimeTable" }).catch(() => {});
};

const gotoQuestionBank = () => {
  router.push({ name: "QuestionBank" }).catch(() => {});
};

const gotoCourse = () => {
  router.push({ name: "Course" }).catch(() => {});
};

function gotoSettings() {
  router.push({ name: "Settings" }).catch(() => {});
}

const navItems = [
  { label: '首页', onClick: gotoHome, isActive: false },
  { label: '课程', onClick: gotoCourse },
  { label: '题库',  onClick: gotoQuestionBank },
  { label: '时间表',  onClick: gotoTimeTable },
  { label: '单词打卡',  onClick: gotoWordCheckIn },
  { label: 'AI伴学', onClick: gotoAiChat }
];

// Socket.IO
const socket = ref(null);

// 发送消息（优先使用 Socket.IO，回退到 HTTP）
const messageSending = ref(false);
const sendMessage = async () => {
  const content = message.value.trim();
  if (!content) return;
  if (!currentFriend.value || !currentFriend.value.id) {
    alert('请选择一个好友');
    return;
  }

  try {
    messageSending.value = true;

    // 可选：乐观添加（带临时标记），提高响应感知
    const tempMsg = { id: `temp-${Date.now()}`, content, isMine: true, time: new Date().toISOString() };
    messageList.value.push(tempMsg);
    scrollToBottom();

    // 优先使用 socket 发消息
    if (socket.value && socket.value.connected) {
      // 发送事件并让后端广播
      socket.value.emit('send_message', { receiverId: currentFriend.value.id, content });
      // 留下乐观消息（会由 server 返回的 message_sent/receive_message 补正）
      message.value = '';
    } else {
      // 回退到 HTTP 接口
      const res = await friendApi.sendMessage({ receiverId: currentFriend.value.id, content });

      if (res && res.code === 200) {
        const serverMsg = res.data || res;
        const idx = messageList.value.findIndex(m => m.id && String(m.id).startsWith('temp-'));
        if (idx !== -1) messageList.value.splice(idx, 1);

        const formatted = {
          id: serverMsg.messageId || serverMsg.message_id || serverMsg.id || Date.now(),
          content: serverMsg.content || content,
          isMine: true,
          time: serverMsg.sentAt || serverMsg.sent_at || serverMsg.createTime || serverMsg.time || new Date().toISOString(),
        };
        messageList.value.push(formatted);
        message.value = '';
        scrollToBottom();
      } else {
        const idx = messageList.value.findIndex(m => m.id && String(m.id).startsWith('temp-'));
        if (idx !== -1) messageList.value.splice(idx, 1);
        alert(res?.message || '发送消息失败');
      }
    }
  } catch (err) {
    console.error('发送消息异常：', err);
    // 移除乐观消息
    const idx = messageList.value.findIndex(m => m.id && String(m.id).startsWith('temp-'));
    if (idx !== -1) messageList.value.splice(idx, 1);
    alert('网络异常，发送失败');
  } finally {
    messageSending.value = false;
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

onMounted(async () => {
  await fetchFriendList(); // 加载好友列表
  await fetchFriendRequests(); // 新增：加载好友请求列表
  
  // 页面滚动时导航栏样式变化
  const header = document.querySelector('header');
  window.addEventListener('scroll', () => {
    if (window.scrollY > 10) {
      header?.classList.add('shadow');
    } else {
      header?.classList.remove('shadow');
    }
  });
  
  // 滚动到底部等逻辑
  scrollToBottom();

  // 初始化 Socket.IO 连接（如果 token 可用）
  try {
    const token = localStorage.getItem('token');
    if (token) {
      socket.value = io(process.env.VUE_APP_SOCKET_URL || 'http://localhost:3000', {
        auth: { token },
      });

      socket.value.on('connect', () => {
        console.log('Socket connected:', socket.value.id);
      });

      const handleIncomingMessage = (msg) => {
        // 兼容字段
        const senderId = msg.senderId || msg.sender_id || msg.sender || msg.from;
        const receiverId = msg.receiverId || msg.receiver_id || msg.to;
        const id = msg.messageId || msg.message_id || msg.id || msg.message_id;
        const time = msg.sentAt || msg.sent_at || msg.createTime || msg.time;

        const formatted = {
          id,
          content: msg.content || msg.body || msg.message || '',
          isMine: String(senderId) === String(currentUserId.value),
          time: time || new Date().toISOString(),
          senderId: senderId,
          receiverId: receiverId,
        };

        // 去重：如果已存在则跳过
        if (formatted.id && messageList.value.some(m => String(m.id) === String(formatted.id))) return;

        // 只有当当前对话相关时才追加到 messageList
        if (currentFriend.value && (String(formatted.senderId) === String(currentFriend.value.id) || String(formatted.receiverId) === String(currentFriend.value.id))) {
          messageList.value.push(formatted);
          scrollToBottom();
        } else {
          // 未选中该好友时增加本地未读计数
          const key = String(formatted.senderId || formatted.receiverId || 'unknown');
          unreadCounts.value[key] = (unreadCounts.value[key] || 0) + 1;
          console.log('收到与当前会话无关的消息，未读计数：', key, unreadCounts.value[key]);
        }
      };

      socket.value.on('receive_message', handleIncomingMessage);
      socket.value.on('message_sent', handleIncomingMessage);

      socket.value.on('disconnect', () => {
        console.log('Socket disconnected');
      });
    }
  } catch (e) {
    console.warn('Socket init failed', e);
  }
});

//好友列表显示部分
//从后端获取好友list
const fetchFriendList = async () => {
  try {
    loading.value = true;
    const res = await friendApi.getFriendList(); // 调用后端接口

    if (res.code === 200) {
      friendList.value = res.data.map(friend => ({
        id: friend.userId, // 后端userId → 前端id
        name: friend.userName, // 后端userName → 前端name
        avatar: friend.avatar, // 头像字段直接复用
        status: friend.userStatus || 'offline', // 后端userStatus → 前端status，兜底默认值
        signature: friend.signature // 保留个性签名字段
      })); 
    } else {
      // 处理接口返回成功但code非200的情况
      error.value = res.message || '获取好友列表失败';
      console.error('获取好友列表失败：', res.message);
    } 
  } catch (err) {
    // 完善错误处理：区分网络错误、接口响应错误
    error.value = '网络异常，获取好友列表失败';
    console.error('获取好友列表异常：', err);
    if (err.response) {
      error.value = err.response.data.message || error.value;
      console.error('后端返回的错误信息：', err.response.data.message);
    }
  } finally {
    loading.value = false;
  }
};


//添加好友弹窗部分

let searchTimer = null; // 搜索防抖定时器
// 监听搜索关键词，调用后端接口搜索用户
watch(searchFriendValue, (val) => {
  // 1. 清空之前的定时器（防抖）
  if (searchTimer) clearTimeout(searchTimer);

  // 2. 如果输入为空，清空结果
  if (!val.trim()) {
    searchResults.value = [];
    return;
  }

  // 3. 开启新的定时器，延迟 500ms 执行搜索
  searchTimer = setTimeout(async () => {
    try {
      searchLoading.value = true;
      
      const res = await friendApi.searchFriend({ keyword: val });
      
      if (res.code === 200) {
        // 格式化后端返回的用户数据
        searchResults.value = res.data.map(user => ({
          id: user.userId, 
          name: user.userName, 
          avatar: user.avatar || 'https://picsum.photos/seed/default/100/100', 
          status: user.userStatus || 'offline'
        }));
      } else {
        searchResults.value = [];
      }
    } catch (err) {
      searchResults.value = [];
      console.error('搜索用户异常：', err);
    } finally {
      searchLoading.value = false;
    }
  }, 500); // 500ms 防抖时间
});


// 发送好友请求逻辑
const addFriend = async (friend) => {
  if (!friend || !friend.id) {
    alert("用户信息无效");
    return;
  }

  try {
    // 调用后端接口
    const res = await friendApi.sendFriendRequest({
      receiverId: friend.id // 传递目标用户ID
    });

    if (res.code === 200) {
      alert(res.message || '好友请求已发送');
      
      // 发送成功后关闭弹窗并清空搜索
      showAddFriendModal.value = false;
      searchFriendValue.value = '';
      searchResults.value = [];
      friendId.value = '';
    } else {
      // 处理业务错误（如：已经是好友、已发送过请求等）
      alert(res.message || '发送请求失败');
    }
  } catch (err) {
    console.error('发送好友请求异常:', err);
  }
};

// 好友请求弹窗部分

const friendRequestLoading = ref(false); // 好友请求加载状态
const friendRequests = ref([]); // 好友请求列表

// 时间格式化工具函数（将后端的时间戳/日期字符串转为友好格式）
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间';
  const date = new Date(timeStr);
  // 格式：2025-12-12 14:30
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};

// 从后端获取好友请求列表
const fetchFriendRequests = async () => {
  try {
    friendRequestLoading.value = true;
    const res = await friendApi.getFriendRequests(); // 调用新增的接口
    console.log('后端返回的好友请求原始数据：', res.data);
    if (res.code === 200) {
      // 格式化后端返回的请求数据（适配前端渲染）
      friendRequests.value = res.data.map(request => ({
        id: request.requestId, // 好友请求的唯一ID（后端主键）
        requesterId: request.senderId, // 发送请求的用户ID
        name: request.senderName, // 发送者昵称
        avatar: request.senderAvatar || 'https://picsum.photos/seed/default/100/100', // 发送者头像
        time: formatTime(request.createdAt), // 发送时间（格式化）
      }));
    } else {
      friendRequests.value = [];
      alert(res.message || '获取好友请求失败');
    }
  } catch (err) {
    friendRequests.value = [];
    console.error('获取好友请求异常:', err);
    const errMsg = err.response?.data?.message;
    alert(errMsg);
  } finally {
    friendRequestLoading.value = false;
  }
};

// 接受好友请求
const acceptFriendRequest = async (index) => {
  const request = friendRequests.value[index];
  if (!request || !request.id) {
    alert('请求信息无效');
    return;
  }

  try {
    // 调用接受请求的接口
    const res = await friendApi.acceptFriendRequest(request.id);
    if (res.code === 200) {
      alert(`已接受 ${request.name} 的好友请求！`);
      // 1. 从请求列表中移除该请求
      friendRequests.value.splice(index, 1);
      // 2. 刷新好友列表（可选：让新好友立即显示在列表中）
      await fetchFriendList();
    } else {
      alert(res.message || '接受好友请求失败');
    }
  } catch (err) {
    console.error('接受好友请求异常:', err);
    const errMsg = err.response?.data?.message || '网络异常，无法接受请求';
    alert(errMsg);
  }
};

// 拒绝好友请求
const rejectFriendRequest = async (index) => {
  const request = friendRequests.value[index];
  if (!request || !request.id) {
    alert('请求信息无效');
    return;
  }

  try {
    // 调用拒绝请求的接口
    const res = await friendApi.rejectFriendRequest(request.id);
    if (res.code === 200) {
      alert(`已拒绝 ${request.name} 的好友请求！`);
      // 从请求列表中移除该请求
      friendRequests.value.splice(index, 1);
    } else {
      alert(res.message || '拒绝好友请求失败');
    }
  } catch (err) {
    console.error('拒绝好友请求异常:', err);
    const errMsg = err.response?.data?.message || '网络异常，无法拒绝请求';
    alert(errMsg);
  }
};

// 选择好友聊天

// 当前选中的好友信息
const currentFriend = ref(null);
// 消息列表加载状态
const messageLoading = ref(false);
// 当前用户 ID：优先使用 Pinia userStore，其次回退到 localStorage 的 userStore
const userStore = useUserStore();
const currentUserId = ref(userStore.userInfo?.id || '');

// 如果 store 还没加载（例如刷新后），尝试从 localStorage 恢复
if (!currentUserId.value) {
  try {
    const cached = localStorage.getItem('userStore');
    if (cached) {
      const parsed = JSON.parse(cached);
      currentUserId.value = parsed?.userInfo?.id || '';
    }
  } catch (e) {
    console.warn('解析 localStorage userStore 失败', e);
  }
}

// 监听 store 变化，保持 currentUserId 同步
watch(
  () => userStore.userInfo?.id,
  (val) => {
    if (val) currentUserId.value = val;
  }
);

const messageList = ref([]);

// 未读计数（简单本地实现）
const unreadCounts = ref({});

// 加载指定好友的消息列表
const fetchMessageList = async (friendId) => {
  if (!friendId) return;
  try {
    messageLoading.value = true;
    // 调用后端接口，传入好友ID
    const res = await friendApi.getMessageList({ friendId });
    if (res.code === 200) {
      // 格式化消息：兼容 snake_case / camelCase 字段，并正确判定 isMine
      const msgs = (res.data || []).map(msg => {
        const senderId = msg.senderId || msg.sender_id || msg.from || msg.userId || msg.user_id;
        const id = msg.messageId || msg.message_id || msg.id || undefined;
        const time = msg.sentAt || msg.sent_at || msg.createTime || msg.create_time || msg.time || msg.updatedAt || msg.updated_at;

        return {
          id,
          ...msg,
          isMine: String(senderId) === String(currentUserId.value), // 字符串比較，避免型別不一致
          content: msg.content || msg.body || msg.message || '',
          time: time || '',
          senderId: senderId
        };
      });

      // 将消息按时间从旧到新排序，确保最新消息在数组末尾（以便渲染时出现在底部）
      msgs.sort((a, b) => {
        const ta = Date.parse(a.time) || 0;
        const tb = Date.parse(b.time) || 0;
        return ta - tb;
      });

      messageList.value = msgs;
    } else {
      messageList.value = [];
      error.value = res.message || '获取消息列表失败';
    }
  } catch (err) {
    messageList.value = [];
    error.value = '网络异常，获取消息列表失败';
    console.error('获取消息列表异常：', err);
  } finally {
    messageLoading.value = false;
    scrollToBottom(); // 加载完成后滚动到底部
  }
};

// 轮询相关
// const messagePollInterval = ref(null);

// const clearMessagePoll = () => {
//   if (messagePollInterval.value) {
//     clearInterval(messagePollInterval.value);
//     messagePollInterval.value = null;
//   }
// };

const selectFriend = async (friend) => {
  currentFriendId.value = friend.id; // 保留原有ID记录
  currentFriend.value = friend; // 存储当前好友完整信息
  error.value = ''; // 清空错误提示

  // 立即加载历史消息
  await fetchMessageList(friend.id);
  // 切换会话时清除该好友的未读计数
  unreadCounts.value[friend.id] = 0;
};

onBeforeUnmount(() => {
  // 断开 socket 连接并清理
  try {
    if (socket.value) {
      socket.value.disconnect();
      socket.value = null;
    }
  } catch (e) {
    console.warn('Socket disconnect error', e);
  }
});




</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css');
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


@keyframes fadeIn 
{
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fadeIn 
{
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