<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 复用项目公共导航栏 -->
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

    <!-- 排行榜主内容区：重构为 左侧列表 + 右侧内容（和聊天界面一致） -->
    <main class="flex-grow flex flex-col md:flex-row">
      <!-- 左侧好友列表（和Chat.vue完全一致） -->
      <aside class="w-full md:w-96 bg-white border-r border-gray-200 shadow-sm md:h-[calc(100vh-64px)] sticky top-[64px] overflow-y-auto flex-shrink-0 z-20">
        <div class="p-5 h-full flex flex-col">
          <!-- 好友列表区域 -->
          <div class="flex-grow overflow-y-auto -mx-2 px-2">
            <h3 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4 mt-2">
              好友列表
            </h3>
            
            <ul class="space-y-2">
              <!-- 加载状态 -->
              <li v-if="friendLoading" class="p-3 text-center text-gray-500">
                <i class="fas fa-spinner fa-spin mr-2" />加载中...
              </li>
              <!-- 错误提示 -->
              <li v-else-if="friendError" class="p-3 text-center text-red-500">
                {{ friendError }}
              </li>
              <!-- 无好友提示 -->
              <li v-else-if="friendList.length === 0" class="p-3 text-center text-gray-500">
                暂无好友，快去添加吧！
              </li>
              <!-- 好友列表 -->
              <li v-for="friend in friendList" :key="friend.id">
                <div class="relative">
                  <FriendItem 
                    :name="friend.name" 
                    :avatar="friend.avatar || 'https://picsum.photos/seed/default/100/100'" 
                    :status="friend.status || 'offline'"
                  >
                    <template #actions>
                      <button
                        class="text-gray-600 hover:text-emerald-600 p-1 rounded-full hover:bg-emerald-50 transition-colors"
                        @click="gotoChatWithFriend(friend)"
                      >
                        <i class="fas fa-comment" />
                      </button>
                    </template>
                  </FriendItem>
                </div>
              </li>
              <!-- 返回聊天按钮 -->
              <li>
                <button 
                  class="w-full flex items-center justify-center p-3 text-emerald-600 text-sm border border-dashed border-emerald-200 rounded-lg hover:bg-emerald-50 transition-all hover:border-emerald-300 group"
                  @click="gotoChatPage"
                >
                  <i class="fas fa-arrow-left-circle mr-2 group-hover:scale-110 transition-transform" />
                  返回聊天界面
                </button>
              </li>
            </ul>
          </div>
          
          <!-- 底部功能选项（和Chat.vue一致） -->
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
            <h3 class="font-bold text-lg text-gray-800">好友周学习排行榜</h3>
            <span class="text-xs text-gray-500">统计周期：{{ weekRange }}</span>
          </div>

          <!-- 加载状态（占满剩余高度） -->
          <div v-if="loading" class="flex-grow flex items-center justify-center py-12">
            <div class="inline-block w-8 h-8 border-2 border-emerald-400 border-t-transparent rounded-full animate-spin"></div>
            <p class="mt-2 text-sm text-gray-500 ml-2">加载排行榜数据中...</p>
          </div>

          <!-- 无数据状态（占满剩余高度） -->
          <div v-else-if="rankingList.length === 0" class="flex-grow flex items-center justify-center py-12">
            <div class="text-center">
              <i class="fas fa-medal text-4xl text-gray-300 mb-2"></i>
              <p class="text-gray-500">暂无好友学习数据</p>
            </div>
          </div>

          <!-- 排行榜列表（占满剩余高度，滚动） -->
          <div v-else class="flex-grow overflow-y-auto">
            <div 
              v-for="(item, index) in rankingList" 
              :key="item.userId"
              class="flex items-center px-6 py-3 hover:bg-gray-50 transition-colors border-b border-gray-100 last:border-0"
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
                  ></i>
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
                <div class="font-medium text-gray-800">{{ item.username }}</div>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
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

// ========== 新增：左侧好友列表逻辑（对齐Chat.vue） ==========
const friendLoading = ref(false);
const friendError = ref('');
const friendList = ref([]);
const activeTab = ref('rank'); // 当前选中tab为排行榜

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
// 跳转到和指定好友的聊天
function gotoChatWithFriend(friend) {
  router.push({ 
    name: 'Chat',
    query: { friendId: friend.id } // 可选：传递好友ID，Chat.vue可接收并自动选中
  }).catch(() => {});
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

// ========== 新增：获取好友列表（对齐Chat.vue） ==========
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

// ========== 获取排行榜数据（保留，优化错误处理） ==========
const fetchRanking = async () => {
  loading.value = true;
  try {
    const res = await friendApi.getFriendWeeklyRanking();
    rankingList.value = res.data || [];
  } catch (error) {
    console.error('获取排行榜失败：', error);
    // 仅控制台报错，不弹框（避免干扰体验）
  } finally {
    loading.value = false;
  }
};

// ========== 生命周期：同时加载好友列表和排行榜 ==========
onMounted(() => {
  fetchFriendList(); // 加载左侧好友列表
  fetchRanking();    // 加载排行榜数据
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