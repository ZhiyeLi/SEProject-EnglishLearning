<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 复用项目公共导航栏 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <ActionButtons
          @suggestions="showSuggestionsModal = true"
          @settings="gotoSettings"
          @home="gotoHome"
          @notifications="() => {}"
        />
      </template>
    </NavBar>

    <!-- 排行榜主内容区：重构为 左侧列表 + 右侧内容（和聊天界面一致） -->
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
                v-if="friendLoading"
                class="p-3 text-center text-gray-500"
              >
                <i class="fas fa-spinner fa-spin mr-2" />加载中...
              </li>
              <!-- 错误提示 -->
              <li
                v-else-if="friendError"
                class="p-3 text-center text-red-500"
              >
                {{ friendError }}
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
                  @click="(showFriendRequestModal = true, fetchFriendRequests())"
                >
                  <i class="fas fa-user-plus mr-2 group-hover:scale-110 transition-transform" />
                  查看好友请求
                  <!-- 未处理请求数小红点 -->
                  <span
                    v-if="pendingRequestCount > 0"
                    class="absolute top-1 right-6 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center animate-pulse"
                  >
                    {{ pendingRequestCount > 99 ? '99+' : pendingRequestCount }}
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
              @click="gotoChatPage"
            >
              <i class="fas fa-comment text-xl mb-1" />
              <span class="text-sm">聊天</span>
            </button>

            <button
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'rank' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
              ]"
              disabled
            >
              <i class="fas fa-trophy text-xl mb-1" />
              <span class="text-sm">排行榜</span>
            </button>
          </div>
        </div>
      </aside>
      
      <!-- 右侧排行榜内容：占满剩余所有空间 -->
      <div class="flex-grow bg-gray-100 overflow-hidden p-4">
        <!-- 排行榜容器：去掉max-w-2xl限制，占满宽度；高度100% -->
        <div class="w-full h-full bg-white rounded-xl shadow-lg overflow-hidden flex flex-col">
          <!-- 排行榜头部 -->
          <div class="px-6 py-4 border-b flex justify-between items-center flex-shrink-0">
            <h3 class="font-bold text-lg text-gray-800">
              好友周学习排行榜
            </h3>
            <div class="flex items-center space-x-4">
              <span class="text-xs text-gray-500">统计周期：{{ weekRange }}</span>
              <template v-if="myRanking">
                <span class="text-sm text-emerald-600 font-medium">我的排名：第{{ myRanking.rank }}名 · {{ myRanking.totalWords }} 个</span>
              </template>
            </div>
          </div>

          <!-- 加载状态（占满剩余高度） -->
          <div
            v-if="loading"
            class="flex-grow flex items-center justify-center py-12"
          >
            <div class="inline-block w-8 h-8 border-2 border-emerald-400 border-t-transparent rounded-full animate-spin" />
            <p class="mt-2 text-sm text-gray-500 ml-2">
              加载排行榜数据中...
            </p>
          </div>

          <!-- 无数据状态（占满剩余高度） -->
          <div
            v-else-if="rankingList.length === 0"
            class="flex-grow flex items-center justify-center py-12"
          >
            <div class="text-center">
              <i class="fas fa-medal text-4xl text-gray-300 mb-2" />
              <p class="text-gray-500">
                暂无好友学习数据
              </p>
            </div>
          </div>

          <!-- 排行榜列表（占满剩余高度，滚动） -->
          <div
            v-else
            class="flex-grow overflow-y-auto"
          >
            <div 
              v-for="(item, index) in rankingList" 
              :key="item.userId"
              :class="['flex items-center px-6 py-3 hover:bg-gray-50 transition-colors border-b border-gray-100 last:border-0', item.userId === myRanking?.userId ? 'bg-emerald-50' : '']"
            >
              <!-- 排名 & 奖牌 -->
              <div class="w-8 text-center mr-4 relative">
                <template v-if="index < 3">
                  <i 
                    class="fas fa-medal text-2xl"
                    :class="[
                      index === 0 ? 'text-yellow-500' : '',
                      index === 1 ? 'text-gray-400' : '',
                      index === 2 ? 'text-amber-700' : ''
                    ]"
                  />
                  <span class="absolute -top-1 -right-1 text-xs text-white bg-black/70 rounded-full w-4 h-4 flex items-center justify-center">
                    {{ index + 1 }}
                  </span>
                </template>
                <template v-else>
                  <span class="font-bold text-gray-600">{{ index + 1 }}</span>
                </template>
              </div>

              <!-- 头像 -->
              <img 
                :src="item.avatar || 'https://picsum.photos/seed/default/100/100'" 
                alt="好友头像" 
                class="w-10 h-10 rounded-full object-cover border border-gray-200 mr-3"
              >

              <!-- 昵称 -->
              <div class="flex-grow">
                <div class="font-medium text-gray-800">
                  {{ item.username }}
                </div>
              </div>

              <!-- 单词总数 -->
              <div class="font-bold text-emerald-600">
                {{ item.totalWords }} 个
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 复用项目公共底部栏 -->
    <EndBar />
    <!-- 添加好友弹窗 -->
    <teleport to="body">
      <div
        v-if="showAddFriendModal"
        class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 animate-fadeIn"
      >
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
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
          <div class="px-6 py-4">
            <div class="space-y-4">
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
          <div class="px-6 py-4 max-h-80 overflow-y-auto">
            <div
              v-if="friendRequestLoading"
              class="p-8 text-center text-gray-500"
            >
              <i class="fas fa-spinner fa-spin text-4xl mb-2 text-gray-300" />
              <p>加载中...</p>
            </div>
            <div
              v-else-if="friendRequests.length === 0"
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
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRouter } from 'vue-router';
import NavBar from '@/components/common/NavBar.vue';
import ActionButtons from '@/components/common/ActionButtons.vue';
import EndBar from '@/components/common/EndBar.vue';
// 引入左侧好友列表组件
import FriendItem from '@/components/business/FriendItem.vue';
import { friendApi } from '@/api/friend';

const router = useRouter();

// ========== 排行榜核心逻辑（保留） ==========
const loading = ref(false);
const rankingList = ref([]);
const myRanking = ref(null);

// ========== 左侧好友列表逻辑 ==========
const friendLoading = ref(false);
const friendError = ref('');
const friendList = ref([]);
const activeTab = ref('rank'); // 当前选中tab为排行榜
// 当前在左侧列表中高亮的好友 ID（点击后跳转到聊天页）
const currentFriendId = ref('');
// 未读消息统计
const unreadCounts = ref({});
// 轮询定时器
const messagePollInterval = ref(null);

// ========== 导航栏 & 跳转函数（对齐Chat.vue） ==========
const navItems = [
  { label: '首页', onClick: gotoHome },
  { label: '课程', onClick: gotoCourse },
  { label: '题库',  onClick: gotoQuestionBank },
  { label: '时间表',  onClick: gotoTimeTable },
  { label: '单词打卡',  onClick: gotoWordCheckIn },
  { label: 'AI伴学', onClick: gotoAiChat }
];

// 导航跳转函数
function gotoHome() {
  activeTab.value = 'friends';
  router.push({ name: 'Home' }).catch(() => {});
}
function gotoCourse() {
  router.push({ name: 'Course' }).catch(() => {});
}
function gotoQuestionBank() {
  router.push({ name: 'QuestionBank' }).catch(() => {});
}
function gotoTimeTable() {
  router.push({ name: 'TimeTable' }).catch(() => {});
}
function gotoWordCheckIn() {
  router.push({ name: "WordCheckIn" }).catch(() => {});
}
function gotoAiChat() {
  router.push({ name: 'AiChat' }).catch(() => {});
}
function gotoSettings() {
  router.push({ name: "Settings" }).catch(() => {});
}
// 跳转到聊天页面
function gotoChatPage() {
  activeTab.value = 'chat';
  router.push({ name: 'Chat' }).catch(() => {});
}


// ========== 计算本周日期范围（保留） ==========
const weekRange = computed(() => {
  const now = new Date();
  const monday = new Date(now);
  monday.setDate(now.getDate() - (now.getDay() || 7) + 1);
  const sunday = new Date(now);
  sunday.setDate(monday.getDate() + 6);
  const format = (date) => date.toISOString().split('T')[0];
  return `${format(monday)} ~ ${format(sunday)}`;
});

// ========== 获取好友列表 ==========
const fetchFriendList = async () => {
  try {
    friendLoading.value = true;
    const res = await friendApi.getFriendList();
    if (res.code === 200) {
      friendList.value = res.data.map(friend => ({
        id: String(friend.user_id || friend.userId || friend.id),
        name: friend.user_name || friend.userName || friend.name,
        avatar: friend.avatar || friend.user_avatar,
        status: friend.user_status || friend.userStatus || 'offline'
      }));
    } else {
      friendError.value = res.message || '获取好友列表失败';
    }
  } catch (err) {
    friendError.value = err.response?.data?.message || '网络异常，获取好友列表失败';
    console.error('获取好友列表失败：', err);
  } finally {
    friendLoading.value = false;
  }
};

// 获取未读消息数
const validateUnreadCountData = (data) => {
  if (Array.isArray(data)) return data.every(item => item && (item.friendId || item.friendId === 0) && typeof item.count === 'number');
  if (data && typeof data === 'object') return true;
  return false;
};

const fetchUnreadCounts = async () => {
  try {
    const res = await friendApi.getUnreadCount();
    if (res.code === 200 && validateUnreadCountData(res.data)) {
      if (Array.isArray(res.data)) {
        const map = {};
        res.data.forEach(i => { map[String(i.friendId)] = i.count; });
        unreadCounts.value = map;
      } else if (typeof res.data === 'object' && res.data !== null) {
        // assume friendId->count map
        const map = {};
        Object.entries(res.data).forEach(([k, v]) => { map[String(k)] = Number(v) || 0; });
        unreadCounts.value = map;
      } else {
        unreadCounts.value = {};
      }
    } else {
      unreadCounts.value = {};
    }
  } catch (err) {
    console.error('获取未读消息数失败：', err);
    unreadCounts.value = {};
  }
};

// 获取未处理的好友请求数量（用于左侧小红点）
const pendingRequestCount = ref(0);
const fetchPendingRequestCount = async () => {
  try {
    const res = await friendApi.getFriendRequests();
    if (res.code === 200 && Array.isArray(res.data)) {
      pendingRequestCount.value = res.data.length;
    } else {
      pendingRequestCount.value = 0;
    }
  } catch (err) {
    console.error('获取好友请求数量失败：', err);
    pendingRequestCount.value = 0;
  }
};

// 弹窗相关
const showAddFriendModal = ref(false);
const showFriendRequestModal = ref(false);
const searchFriendValue = ref('');
const searchResults = ref([]);
const friendRequestLoading = ref(false);
const friendRequests = ref([]);

// 搜索好友（用于添加好友弹窗）
let rankSearchTimer = null;
watch(searchFriendValue, (val) => {
  if (val === undefined) return;
  clearTimeout(rankSearchTimer);
  if (!val.trim()) {
    searchResults.value = [];
    return;
  }
  rankSearchTimer = setTimeout(async () => {
    try {
      const res = await friendApi.searchFriend({ keyword: val });
      searchResults.value = res.code === 200 ? res.data.map(user => ({
        id: String(user.user_id || user.userId || user.id),
        name: user.user_name || user.userName || user.name,
        avatar: user.avatar || 'https://picsum.photos/seed/default/100/100'
      })) : [];
    } catch (err) {
      searchResults.value = [];
      console.error('排行榜-搜索好友失败：', err);
    }
  }, 500);
});

// 发送好友请求
const addFriend = async (friend) => {
  if (!friend?.id) return alert('用户信息无效');
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
      friendRequests.value = (res.data || []).map(request => ({
        id: String(request.requestId || request.request_id || request.id),
        requesterId: String(request.sender_id || request.senderId),
        name: request.user_name || request.userName || request.name || request.senderName,
        avatar: request.avatar || 'https://picsum.photos/seed/default/100/100',
        time: new Date(request.created_at || request.createdAt || Date.now()).toISOString()
      }));
      // 同步 pending count
      pendingRequestCount.value = Array.isArray(res.data) ? res.data.length : 0;
    } else {
      friendRequests.value = [];
    }
  } catch (err) {
    friendRequests.value = [];
    console.error('获取好友请求失败：', err);
  } finally {
    friendRequestLoading.value = false;
  }
};

// 接受/拒绝
const acceptFriendRequest = async (index) => {
  const request = friendRequests.value[index];
  if (!request?.id) return alert('请求信息无效');
  try {
    const res = await friendApi.acceptFriendRequest(request.id);
    if (res.code === 200) {
      alert(`已接受 ${request.name} 的好友请求！`);
      friendRequests.value.splice(index, 1);
      pendingRequestCount.value = Math.max(0, pendingRequestCount.value - 1);
      await fetchFriendList();
    } else {
      alert(res.message || '接受请求失败');
    }
  } catch (err) {
    console.error('接受好友请求失败：', err);
    alert('网络异常，操作失败');
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
      pendingRequestCount.value = Math.max(0, pendingRequestCount.value - 1);
    } else {
      alert(res.message || '拒绝请求失败');
    }
  } catch (err) {
    console.error('拒绝好友请求失败：', err);
    alert('网络异常，操作失败');
  }
};

onBeforeUnmount(() => {
  if (rankSearchTimer) clearTimeout(rankSearchTimer);
  if (messagePollInterval.value) clearInterval(messagePollInterval.value);
});

// 选择好友并跳转到聊天
function selectFriend(friend) {
  if (!friend || !friend.id) return;
  currentFriendId.value = friend.id;
  router.push({ name: 'Chat', query: { friendId: friend.id } }).catch(() => {});
}

// ========== 获取排行榜数据（保留，优化错误处理） ==========
const fetchRanking = async () => {
  loading.value = true;
  try {
    const res = await friendApi.getFriendWeeklyRanking();
    if (res.data && res.data.rankingList) {
      rankingList.value = res.data.rankingList;
      myRanking.value = res.data.my || null;
    } else {
      rankingList.value = res.data || [];
      myRanking.value = null;
    }
  } catch (error) {
    console.error('获取排行榜失败：', error);
  } finally {
    loading.value = false;
  }
};

// ========== 生命周期：同时加载好友列表和排行榜 ==========
onMounted(() => {
  fetchFriendList(); // 加载左侧好友列表
  fetchRanking();    // 加载排行榜数据
  fetchPendingRequestCount(); // 加载未处理好友请求数量
  // 拉取未读数并启动轮询
  fetchUnreadCounts();
  if (!messagePollInterval.value) {
    messagePollInterval.value = setInterval(fetchUnreadCounts, 3000);
  }
});
</script>

<style scoped>
/* 全局滚动条优化（作用于当前组件） */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* 排行榜列表滚动条单独优化 */
:deep(.flex-grow.overflow-y-auto)::-webkit-scrollbar {
  width: 4px;
}
:deep(.flex-grow.overflow-y-auto)::-webkit-scrollbar-track {
  background: #f8f8f8;
  border-radius: 2px;
}
:deep(.flex-grow.overflow-y-auto)::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 2px;
}
</style>