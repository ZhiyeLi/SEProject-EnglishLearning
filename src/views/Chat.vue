<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 导航栏 -->
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
      <!-- 左侧好友列表 -->
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
              <!-- 好友列表 -->
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
              <!-- 添加好友 -->
              <li>
                <button 
                  class="w-full flex items-center justify-center p-3 text-emerald-600 text-sm border border-dashed border-emerald-200 rounded-lg hover:bg-emerald-50 transition-all hover:border-emerald-300 group"
                  @click="showAddFriendModal = true"
                >
                  <i class="fas fa-plus-circle mr-2 group-hover:scale-110 transition-transform" />
                  点击添加更多好友
                </button>
              </li>
              <!-- 好友请求 -->
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
          
          <!-- 底部功能选项 -->
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
              @click="handleRankClick"
            >
              <i class="fas fa-trophy text-xl mb-1" />
              <span class="text-sm">排行榜</span>
            </button>
          </div>
        </div>
      </aside>
      
      <!-- 右侧聊天视图（移除排行榜视图，仅保留聊天） -->
      <div class="flex-grow bg-gray-100 overflow-hidden p-4">
        <div class="w-full h-full flex flex-col bg-white rounded-xl shadow-lg overflow-hidden">
          <!-- 聊天头部 -->
          <div
            v-if="currentFriend"
            class="border-b border-gray-200 p-4 flex items-center shadow-sm"
          >
            <!-- 添加 ?. 保护：currentFriend?.avatar -->
            <img 
              :src="currentFriend?.avatar || 'https://picsum.photos/seed/default/100/100'" 
              :alt="currentFriend?.name || '好友'" 
              class="w-8 h-8 rounded-full object-cover mr-2"
            >
            <h3 class="font-semibold text-gray-800">
              {{ currentFriend?.name || '选择好友' }}
            </h3>
            <span 
              v-if="currentFriend?.status === 'online'"
              class="ml-2 w-2 h-2 bg-green-500 rounded-full"
              title="在线"
            />
          </div>
          
          <!-- 聊天消息区域 -->
          <div
            ref="chatContainer"
            class="flex-grow p-4 overflow-y-auto"
            style="max-height: calc(100vh - 220px);"
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
              v-else-if="messageList.length === 0"
              class="h-full flex items-center justify-center text-gray-500"
            >
              <div class="flex flex-col items-center"> 
                <i class="fas fa-paper-plane text-4xl mb-2 text-gray-300 block" /> 
                <p class="mt-3">
                  暂无消息，开始聊天吧！
                </p> 
              </div>
            </div>

            <!-- 消息列表 -->
            <div 
              v-for="(msg, index) in messageList" 
              :key="msg.id || index" 
              :class="[
                'flex items-start mb-4 animate-fadeIn',
                msg.isMine ? 'justify-end' : ''
              ]"
            >
              <!-- 对方头像 -->
              <img 
                v-if="!msg.isMine && currentFriend"
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
                  <p class="text-xs mt-1 opacity-70">
                    {{ formatTime(msg.time) }}
                  </p>
                </div>
              </div>

              <!-- 自己头像 -->
              <img 
                v-if="msg.isMine"
                :src="userStore.userInfo.avatar"
                alt="我" 
                class="w-8 h-8 rounded-full object-cover ml-2 mt-1 flex-shrink-0"
              >   
            </div>
          </div>
          
          <!-- 消息输入区域 -->
          <div
            v-if="currentFriend"
            class="border-t border-gray-200 p-3"
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
      </div>
    </main>
    
    <!-- 底部栏 -->
    <EndBar />

    <!-- 添加好友弹窗 -->
    <teleport to="body">
      <div
        v-if="showAddFriendModal"
        class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 animate-fadeIn"
      >
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
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
              <!-- 搜索框 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">搜索好友（用户名/ID/邮箱）</label>
                <div class="relative">
                  <input 
                    v-model="searchFriendValue"
                    type="text" 
                    placeholder="请输入好友信息..." 
                    class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent text-base"
                  >
                  <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
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
    
    <!-- 好友请求弹窗 -->
    <teleport to="body">
      <div
        v-if="showFriendRequestModal"
        class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 animate-fadeIn"
      >
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
          <!-- 弹窗头部 -->
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

          <!-- 弹窗内容 -->
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
            <!-- 请求列表 -->
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
                      ID: {{ request.requesterId }}
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
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { onMounted, ref, watch, onBeforeUnmount } from 'vue';
import { useUserStore } from '@/store/modules/user';
import NavBar from '@/components/common/NavBar.vue';
import ActionButtons from '@/components/common/ActionButtons.vue';
import FriendItem from '@/components/business/FriendItem.vue';
import EndBar from '@/components/common/EndBar.vue';
// 移除 FriendRanking 组件引入 ↓
// import FriendRanking from '@/components/business/FriendRanking.vue'; 
import { friendApi } from '@/api/friend';

// 核心响应式变量
const router = useRouter();
const userStore = useUserStore();

// 基础状态
const loading = ref(false);
const error = ref('');
const activeTab = ref('chat');

// 好友相关
const friendList = ref([]);
const currentFriendId = ref('');
const currentFriend = ref(null);
const unreadCounts = ref({});

// 聊天相关
const message = ref('');
const messageList = ref([]);
const messageLoading = ref(false);
const chatContainer = ref(null);
const messagePollInterval = ref(null);

// 弹窗相关
const showAddFriendModal = ref(false);
const showFriendRequestModal = ref(false);
const searchFriendValue = ref('');
const searchResults = ref([]);
const friendRequestLoading = ref(false);
const friendRequests = ref([]);

// 用户ID
const currentUserId = ref(userStore.userInfo?.id || '');
if (!currentUserId.value) {
  try {
    const cached = localStorage.getItem('userStore');
    if (cached) {
      const parsed = JSON.parse(cached);
      currentUserId.value = parsed?.userInfo?.id || '';
    }
  } catch (e) {
    console.warn('解析用户缓存失败', e);
  }
}

// 路由跳转函数
const gotoChat = () => {
  activeTab.value = 'chat';
  router.push({ name: 'Chat' }).catch(() => {});
};

const gotoHome = () => {
  activeTab.value = 'friends';
  router.push({ name: 'Home' }).catch(() => {});
};

// 修改 handleRankClick 为路由跳转 ↓
const handleRankClick = () => {
  activeTab.value = 'rank';
  currentFriend.value = null;
  clearMessagePoll();
  // 跳转到 views/FriendRanking.vue（路由名称需和 router/index.js 中配置一致）
  router.push({ name: 'Rank' }).catch(() => {});
};

const gotoSettings = () => {
  router.push({ name: "Settings" }).catch(() => {});
};

// 导航栏配置
const navItems = [
  { label: '首页', onClick: gotoHome },
  { label: '课程', onClick: () => router.push({ name: 'Course' }).catch(() => {}) },
  { label: '题库',  onClick: () => router.push({ name: 'QuestionBank' }).catch(() => {}) },
  { label: '时间表',  onClick: () => router.push({ name: 'TimeTable' }).catch(() => {}) },
  { label: '单词打卡',  onClick: () => router.push({ name: 'WordCheckIn' }).catch(() => {}) },
  { label: 'AI伴学', onClick: () => router.push({ name: 'AiChat' }).catch(() => {}) }
];

// 时间格式化
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间';
  const date = new Date(timeStr);
  if (isNaN(date.getTime())) return '未知时间';
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};

// 滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    setTimeout(() => {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }, 100);
  }
};

// 消息轮询
const clearMessagePoll = () => {
  if (messagePollInterval.value) {
    clearInterval(messagePollInterval.value);
    messagePollInterval.value = null;
  }
};

// 发送消息
const sendMessage = async () => {
  const content = message.value.trim();
  if (!content || !currentFriend.value) {
    alert(!currentFriend.value ? '请选择一个好友' : '消息内容不能为空');
    return;
  }

  try {
    // 乐观更新
    const tempMsg = { id: `temp-${Date.now()}`, content, isMine: true, time: new Date().toISOString() };
    messageList.value.push(tempMsg);
    scrollToBottom();

    // 调用接口
    const res = await friendApi.sendMessage({ receiverId: currentFriend.value.id, content });
    if (res.code === 200) {
      // 替换临时消息
      const idx = messageList.value.findIndex(m => m.id?.startsWith('temp-'));
      if (idx !== -1) messageList.value.splice(idx, 1);
      
      const formattedMsg = {
        id: res.data?.messageId || Date.now(),
        content: res.data?.content || content,
        isMine: true,
        time: res.data?.sentAt || new Date().toISOString()
      };
      messageList.value.push(formattedMsg);
      message.value = '';
      scrollToBottom();
    } else {
      throw new Error(res.message || '发送失败');
    }
  } catch (err) {
    // 回滚乐观更新
    const idx = messageList.value.findIndex(m => m.id?.startsWith('temp-'));
    if (idx !== -1) messageList.value.splice(idx, 1);
    alert(err.message || '网络异常，发送失败');
    console.error('发送消息失败：', err);
  }
};

// 获取好友列表
const fetchFriendList = async () => {
  try {
    loading.value = true;
    const res = await friendApi.getFriendList();
    if (res.code === 200) {
      friendList.value = res.data.map(friend => ({
        id: String(friend.user_id || friend.userId || friend.id),
        name: friend.user_name || friend.userName || friend.name,
        avatar: friend.avatar || friend.user_avatar,
        status: friend.user_status || friend.userStatus || 'offline'
      }));
    } else {
      error.value = res.message || '获取好友列表失败';
    }
  } catch (err) {
    error.value = err.response?.data?.message || '网络异常，获取好友列表失败';
    console.error('获取好友列表失败：', err);
  } finally {
    loading.value = false;
  }
};

// 搜索好友
let searchTimer = null;
watch(searchFriendValue, (val) => {
  clearTimeout(searchTimer);
  if (!val.trim()) {
    searchResults.value = [];
    return;
  }
  searchTimer = setTimeout(async () => {
    try {
      const res = await friendApi.searchFriend({ keyword: val });
      searchResults.value = res.code === 200 
        ? res.data.map(user => ({
            id: String(user.user_id || user.userId || user.id),
            name: user.user_name || user.userName || user.name,
            avatar: user.avatar || 'https://picsum.photos/seed/default/100/100'
          }))
        : [];
    } catch (err) {
      searchResults.value = [];
      console.error('搜索好友失败：', err);
    }
  }, 500);
});

// 添加好友请求
const addFriend = async (friend) => {
  if (!friend?.id) return alert("用户信息无效");
  try {
    const res = await friendApi.sendFriendRequest({ receiverId: friend.id });
    if (res.code === 200) {
      alert('好友请求已发送');
      showAddFriendModal.value = false;
      searchFriendValue.value = '';
      searchResults.value = [];
    } else {
      alert(res.message || '发送请求失败');
    }
  } catch (err) {
    console.error('发送好友请求失败：', err);
    alert('网络异常，发送失败');
  }
};

// 获取好友请求
const fetchFriendRequests = async () => {
  try {
    friendRequestLoading.value = true;
    const res = await friendApi.getFriendRequests();
    if (res.code === 200) {
      friendRequests.value = res.data.map(request => ({
        id: String(request.request_id || request.requestId),
        requesterId: String(request.sender_id || request.senderId),
        name: request.user_name || request.senderName || request.name,
        avatar: request.avatar || 'https://picsum.photos/seed/default/100/100',
        time: formatTime(request.created_at || request.createdAt)
      }));
    } else {
      friendRequests.value = [];
      alert(res.message || '获取好友请求失败');
    }
  } catch (err) {
    friendRequests.value = [];
    console.error('获取好友请求失败：', err);
    alert(err.response?.data?.message || '网络异常，获取失败');
  } finally {
    friendRequestLoading.value = false;
  }
};

// 处理好友请求
const acceptFriendRequest = async (index) => {
  const request = friendRequests.value[index];
  if (!request?.id) return alert('请求信息无效');
  try {
    const res = await friendApi.acceptFriendRequest(request.id);
    if (res.code === 200) {
      alert(`已接受 ${request.name} 的好友请求！`);
      friendRequests.value.splice(index, 1);
      await fetchFriendList();
    } else {
      alert(res.message || '接受请求失败');
    }
  } catch (err) {
    console.error('接受好友请求失败：', err);
    alert(err.response?.data?.message || '网络异常，操作失败');
  }
};

const rejectFriendRequest = async (index) => {
  const request = friendRequests.value[index];
  if (!request?.id) return alert('请求信息无效');
  try {
    const res = await friendApi.rejectFriendRequest(request.id);
    if (res.code === 200) {
      alert(`已拒绝 ${request.name} 的好友请求！`);
      friendRequests.value.splice(index, 1);
    } else {
      alert(res.message || '拒绝请求失败');
    }
  } catch (err) {
    console.error('拒绝好友请求失败：', err);
    alert(err.response?.data?.message || '网络异常，操作失败');
  }
};

// 获取消息列表
const fetchMessageList = async (friendId) => {
  if (!friendId) return;
  try {
    messageLoading.value = true;
    const res = await friendApi.getMessageList({ friendId });
    if (res.code === 200) {
      messageList.value = (res.data || []).map(msg => ({
        id: msg.message_id || msg.id,
        content: msg.content,
        isMine: String(msg.sender_id || msg.senderId) === String(currentUserId.value),
        time: msg.sent_at || msg.sentAt
      })).sort((a, b) => Date.parse(a.time) - Date.parse(b.time));
    } else {
      messageList.value = [];
      error.value = res.message || '获取消息列表失败';
    }
  } catch (err) {
    messageList.value = [];
    error.value = '网络异常，获取消息列表失败';
    console.error('获取消息列表失败：', err);
  } finally {
    messageLoading.value = false;
    scrollToBottom();
  }
};

// 获取未读消息数
const fetchUnreadCounts = async () => {
  try {
    const res = await friendApi.getUnreadCount();
    if (res.code === 200) {
      unreadCounts.value = Array.isArray(res.data) 
        ? res.data.reduce((acc, item) => ({ ...acc, [item.friendId]: item.count }), {})
        : res.data || {};
    } else {
      unreadCounts.value = {};
    }
  } catch (err) {
    unreadCounts.value = {};
    console.error('获取未读消息数失败：', err);
  }
};

// 选择好友聊天
const selectFriend = async (friend) => {
  currentFriendId.value = friend.id;
  currentFriend.value = friend;
  
  // 标记已读
  try {
    await friendApi.markMessagesAsRead({ friendId: friend.id });
  } catch (err) {
    console.warn('标记消息已读失败：', err);
  }

  // 加载消息和未读数
  await fetchMessageList(friend.id);
  await fetchUnreadCounts();

  // 重启轮询
  clearMessagePoll();
  messagePollInterval.value = setInterval(async () => {
    await fetchMessageList(friend.id);
    await fetchUnreadCounts();
  }, 3000);
};

// 监听Tab切换
watch(activeTab, (val) => {
  if (val !== 'chat') {
    clearMessagePoll();
  } else if (currentFriend.value) {
    clearMessagePoll();
    messagePollInterval.value = setInterval(async () => {
      await fetchMessageList(currentFriend.value.id);
      await fetchUnreadCounts();
    }, 3000);
  }
});

// 生命周期
onMounted(async () => {
  await fetchFriendList();
  await fetchFriendRequests();
  await fetchUnreadCounts();
  
  // 启动未读数轮询
  if (!messagePollInterval.value) {
    messagePollInterval.value = setInterval(fetchUnreadCounts, 3000);
  }
  scrollToBottom();
});

onBeforeUnmount(() => {
  clearMessagePoll();
  if (searchTimer) clearTimeout(searchTimer);
});
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css');
@tailwind base;
@tailwind components;
@tailwind utilities;

/* 自定义滚动条 */
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

/* 淡入动画 */
.animate-fadeIn {
  animation: fadeIn 0.3s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 聊天输入框样式 */
textarea {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}
textarea::-webkit-scrollbar {
  width: 6px;
}
</style>