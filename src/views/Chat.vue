<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 保留NavBar组件 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <ActionButtons
          @suggestions="gotoAiChat"
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
                <div class="relative">
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
                  <!-- 未读消息标志 -->
                  <span
                    v-if="unreadCounts[friend.id] > 0"
                    class="absolute top-2 right-4 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center animate-pulse"
                    :class="{ 'h-6 w-6': unreadCounts[friend.id] > 9 }" 
                  >
                    {{ unreadCounts[friend.id] > 99 ? '99+' : unreadCounts[friend.id] }}
                  </span>
                </div>
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
        <div
          v-if="currentFriend"
          class="bg-white border-b border-gray-200 p-4 flex items-center shadow-sm"
        >
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
            />
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
          <div
            v-if="messageLoading"
            class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-gray-500"
          >
            <i class="fas fa-spinner fa-spin mr-2" />加载消息中...
          </div>

          <!-- 未选择好友提示 -->
          <div
            v-else-if="!currentFriend"
            class="h-full flex items-center justify-center text-gray-500"
          >
            <div class="flex flex-col items-center">
              <i class="fas fa-comment-dots text-4xl text-gray-300 block" />
              <p class="mt-3">
                请选择一个好友开始聊天
              </p>
            </div>
          </div>

          <!-- 无消息提示 -->
          <div
            v-else-if="!messageList || messageList.length === 0"
            class="h-full flex items-center justify-center text-gray-500"
          >
            <div class="flex flex-col items-center"> 
              <i class="fas fa-paper-plane text-4xl mb-2 text-gray-300 block mr-0" /> 
              <p class="ml-0 mt-3">
                暂无消息，开始聊天吧！
              </p> 
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
        <div
          v-if="currentFriend"
          class="bg-white border-t border-gray-200 p-3"
        >
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


// 发送消息（使用后端 HTTP 接口）
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

    // 乐观添加一条临时消息，提升体验
    const tempMsg = { id: `temp-${Date.now()}`, content, isMine: true, time: new Date().toISOString() };
    messageList.value.push(tempMsg);
    scrollToBottom();

    // 使用 HTTP 接口发送消息
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
  await fetchFriendRequests(); // 加载好友请求列表
  await fetchUnreadCounts(); // 加载未读消息数

  // 设置轮询获取未读消息数
  if (!messagePollInterval.value) {
    messagePollInterval.value = setInterval(() => {
      fetchUnreadCounts().catch(err => console.warn('轮询未读数失败', err));
    }, 3000); // 5秒轮询一次
  }
  
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

  // (已移除 Socket.IO 实时逻辑，改为前端轮询以保证兼容性)
});

//好友列表显示部分
//从后端获取好友list
const fetchFriendList = async () => {
  try {
    loading.value = true;
    const res = await friendApi.getFriendList(); // 调用后端接口

    if (res.code === 200) {
      // 后端返回字段为 snake_case（user_id, user_name, user_status）
      friendList.value = res.data.map(friend => ({
        id: String(friend.user_id || friend.userId || friend.id), // 兼容多種命名情況並轉為字串
        name: friend.user_name || friend.userName || friend.name,
        avatar: friend.avatar || friend.user_avatar,
        status: friend.user_status || friend.userStatus || 'offline',
        signature: friend.signature || ''
      })); 
      console.debug('[fetchFriendList] mapped friendList:', friendList.value);
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
        // 兼容後端 user_* 命名
      searchResults.value = res.data.map(user => ({
        id: String(user.user_id || user.userId || user.id),
            name: user.user_name || user.userName || user.name,
            avatar: user.avatar || 'https://picsum.photos/seed/default/100/100',
            status: user.user_status || user.userStatus || 'offline'
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
      // 后端返回字段為 request_id, sender_id, user_name, avatar, created_at
      friendRequests.value = res.data.map(request => ({
        id: String(request.request_id || request.requestId),
        requesterId: String(request.sender_id || request.senderId),
        name: request.user_name || request.senderName || request.name,
        avatar: request.avatar || 'https://picsum.photos/seed/default/100/100',
        time: formatTime(request.created_at || request.createdAt)
      }));
      console.debug('[fetchFriendRequests] mapped friendRequests:', friendRequests.value);
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

// 轮询定时器（提前声明，避免 onMounted 中使用未声明变量）
const messagePollInterval = ref(null);

// 加载指定好友的消息列表
const fetchMessageList = async (friendId) => {
  if (!friendId) return;
  try {
    // messageLoading.value = true;
    // 调用后端接口，传入好友ID
    const res = await friendApi.getMessageList({ friendId });
    if (res.code === 200) {
      const msgs = (res.data || []).map(msg => {
        const senderId = msg.sender_id || msg.senderId;
        return {
          id: msg.message_id || msg.id,
          content: msg.content,
          isMine: String(senderId) === String(currentUserId.value), // 自己发送的消息
          time: msg.sent_at || msg.sentAt, // 数据库的sent_at字段
          ifRead: msg.if_read || msg.ifRead, // 数据库的if_read字段（0=未读，1=已读）
          senderId,
          receiverId: msg.receiver_id || msg.receiverId
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
const clearMessagePoll = () => {
  if (messagePollInterval.value) {
    clearInterval(messagePollInterval.value);
    messagePollInterval.value = null;
  }
};

const selectFriend = async (friend) => {
  currentFriendId.value = friend.id;
  currentFriend.value = friend;
  error.value = '';

  // 调用后端接口，标记该好友的所有消息为已读
  try {
    // 调用后端标记已读接口
    const res = await friendApi.markMessagesAsRead({ friendId: friend.id });
    if (res.code === 200) {
      console.debug(`[selectFriend] 标记好友${friend.id}的消息为已读成功`);
    } else {
      console.warn(`[selectFriend] 标记已读失败：${res.message}`);
    }
  } catch (err) {
    console.error('[selectFriend] 标记已读接口调用失败：', err);
  }

  // 加载该好友的消息列表
  await fetchMessageList(friend.id);

  // 刷新未读计数（此时该好友的未读数会变为0）
  await fetchUnreadCounts();

  // 重启轮询（获取最新消息+最新未读数）
  clearMessagePoll();
  messagePollInterval.value = setInterval(async () => {
    await fetchMessageList(friend.id);
    await fetchUnreadCounts(); // 轮询时也刷新未读数
  }, 3000);
};

onBeforeUnmount(() => {
  // 清理轮询
  try {
    clearMessagePoll();
  } catch (e) {
    console.warn('清理消息轮询失败', e);
  }
});

// 工具函数：校验未读计数数据是否有效
const validateUnreadCountData = (data) => {
  // 数据是数组
  if (Array.isArray(data)) {
    return data.every(item => {
      // 必须包含friendId（数字/字符串）和count（非负整数）
      return (item.friendId !== undefined && (typeof item.friendId === 'number' || typeof item.friendId === 'string')) 
          && (item.count !== undefined && Number.isInteger(item.count) && item.count >= 0);
    });
  }
  // 数据是对象
  else if (typeof data === 'object' && data !== null) {
    return Object.entries(data).every(([friendId, count]) => {
      // friendId是非空字符串/数字，count是非负整数
      return (friendId && (typeof friendId === 'number' || typeof friendId === 'string'))
          && (typeof count === 'number' && Number.isInteger(count) && count >= 0);
    });
  }
  // 无效数据
  return false;
};

// 从后端获取所有好友的未读消息数
const fetchUnreadCounts = async () => {
  try {
    console.debug('[fetchUnreadCounts] 开始请求后端未读计数接口');
    const res = await friendApi.getUnreadCount();
    console.debug('[fetchUnreadCounts] 后端原始返回数据：', res); // 打印原始响应

    if (res.code === 200) {
      // 校验数据格式是否有效
      const isDataValid = validateUnreadCountData(res.data);
      if (!isDataValid) {
        console.error('[fetchUnreadCounts] 后端返回的未读计数数据格式无效：', res.data);
        unreadCounts.value = {};
        return;
      }

      // 处理数据（数组转对象）
      let newUnreadCounts = {};
      if (Array.isArray(res.data)) {
        console.debug('[fetchUnreadCounts] 后端返回数组格式，开始转换为对象');
        res.data.forEach(item => {
          // 验证item是否包含必要字段（兜底）
          if (item.friendId && typeof item.count === 'number') {
            newUnreadCounts[item.friendId] = item.count;
          } else {
            console.warn('[fetchUnreadCounts] 无效的未读计数项：', item);
          }
        });
      } else if (typeof res.data === 'object' && res.data !== null) {
        // 处理对象格式
        console.debug('[fetchUnreadCounts] 后端返回对象格式，直接使用');
        newUnreadCounts = res.data;
        // 验证对象中的值是否为数字（兜底）
        Object.entries(newUnreadCounts).forEach(([friendId, count]) => {
          if (typeof count !== 'number' || count < 0) {
            console.warn('[fetchUnreadCounts] 无效的未读计数：好友ID=', friendId, '，计数=', count);
            // 修正无效数据
            newUnreadCounts[friendId] = 0;
          }
        });
      } else {
        console.warn('[fetchUnreadCounts] 后端返回的data格式不支持：', res.data);
        newUnreadCounts = {};
      }

      unreadCounts.value = newUnreadCounts;
      console.debug('[fetchUnreadCounts] 处理后的未读计数：', unreadCounts.value);
    } else {
      console.warn('[fetchUnreadCounts] 后端返回非200状态：', res.code, res.message);
      unreadCounts.value = {};
    }
  } catch (err) {
    console.error('获取未读消息数失败：', err);
    // 打印错误的详细信息（比如请求地址、响应状态）
    if (err.response) {
      console.error('请求地址：', err.config.url);
      console.error('响应状态：', err.response.status);
      console.error('响应数据：', err.response.data);
    }
    // 错误时重置未读计数，避免前端渲染异常
    unreadCounts.value = {};
  }
};


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