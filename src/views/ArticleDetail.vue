<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 复用主界面的NavBar -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <ActionButtons
          @suggestions="showSuggestionsModal = true"
          @settings="gotoSettings"
          @home="gotoHome"
          @notifications="handleNotifications"
        />
      </template>
    </NavBar>

    <!-- 主内容区：复用布局，替换为文章详情 -->
    <main class="flex-grow flex flex-col md:flex-row">
      <!-- 左侧好友列表：保留（和主界面一致） -->
      <aside class="w-full md:w-96 bg-white border-r border-gray-200 shadow-sm md:h-[calc(100vh-64px)] sticky top-[64px] overflow-y-auto flex-shrink-0 z-20" aria-label="好友列表侧边栏">
        <!-- 复制主界面左侧列表代码（好友列表、底部tab等） -->
        <div class="p-5 h-full flex flex-col">
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
              <li v-for="friend in friendList" :key="friend.id">
                <FriendItem 
                  :name="friend.name" 
                  :avatar="friend.avatar" 
                  :status="friend.status"
                >
                  <!-- 未读信息小红点 -->
                  <span
                    v-if="unreadCounts[friend.id] > 0"
                    class="absolute top-1/2 right-2 transform -translate-y-1/2 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center animate-pulse z-10"
                    :class="{ 'h-6 w-6 px-0.5': unreadCounts[friend.id] > 9 }"    
                  >
                    {{ unreadCounts[friend.id] > 99 ? '99+' : unreadCounts[friend.id] }}
                  </span>
                </FriendItem>
              </li>
            </ul>
          </div>
          <!-- 底部tab -->
          <div class="border-t border-gray-100 mt-4 pt-3 flex justify-around">
            <button
              class="flex flex-col items-center py-1 transition-colors text-gray-600 hover:text-emerald-600 transform hover:scale-110"
              @click="gotoHome"
            >
              <i class="fas fa-home text-xl mb-1" />
              <span class="text-sm">首页</span>
            </button>
            <button
              class="flex flex-col items-center py-1 transition-colors text-gray-600 hover:text-emerald-600 transform hover:scale-110"
              @click="gotoChat"
            >
              <i class="fas fa-comment text-xl mb-1" />
              <span class="text-sm">聊天</span>
            </button>
            <button
              class="flex flex-col items-center py-1 transition-colors text-gray-600 hover:text-emerald-600 transform hover:scale-110"
              @click="gotoRank"
            >
              <i class="fas fa-trophy text-xl mb-1" />
              <span class="text-sm">排行榜</span>
            </button>
          </div>
        </div>
      </aside>

      <!-- 中间文章详情区：核心修改部分 -->
      <div class="flex-grow p-6 overflow-y-auto relative">
        <!-- 烟花特效画布 -->
        <canvas ref="fireworksCanvas" class="absolute top-0 left-0 w-full h-full pointer-events-none z-10"></canvas>
        
        <div class="max-w-5xl mx-auto bg-white rounded-xl shadow-sm border border-gray-200 p-8 relative z-20 transform transition-all duration-300 hover:shadow-md">
          <!-- 文章标题 -->
          <h1 class="text-4xl font-bold text-gray-800 mb-4">{{ articleData.title }}</h1>
          
          <!-- 文章元信息（阅读量、发布时间等） -->
          <div class="text-sm text-gray-500 mb-6 flex flex-wrap items-center gap-6 pb-6 border-b border-gray-100">
            <span class="flex items-center">
              <i class="far fa-eye mr-2 text-emerald-500" /> {{ articleData.readCount }} 阅读
            </span>
            <span class="flex items-center">
              <i class="far fa-comment mr-2 text-emerald-500" /> {{ articleData.commentCount }} 评论
            </span>
            <span class="flex items-center">
              <i class="far fa-clock mr-2 text-emerald-500" /> {{ articleData.readTime }}分钟阅读
            </span>
            <span class="flex items-center">
              <i class="far fa-calendar mr-2 text-emerald-500" /> {{ articleData.publishTime }}
            </span>
          </div>
          
          <!-- 点赞按钮 -->
          <div class="mb-8 flex justify-end">
            <button
              class="flex items-center gap-2 px-5 py-2.5 rounded-lg transition-all shadow-sm hover:shadow-md transform hover:-translate-y-0.5"
              :class="isLiked ? 'bg-gradient-to-r from-red-500 to-pink-500 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
              @click="handleLike"
            >
              <i class="fas fa-heart" :class="{ 'fa-solid': isLiked, 'fa-regular': !isLiked }"></i>
              <span class="font-medium">{{ articleData.likeCount }} 点赞</span>
            </button>
          </div>
          
          <!-- 文章封面图 -->
          <div class="w-full h-64 md:h-96 overflow-hidden rounded-lg mb-8 shadow-md bg-gradient-to-br from-gray-100 to-gray-200 relative group">
            <!-- loading占位符 -->
            <div v-if="imageLoading" class="absolute inset-0 flex items-center justify-center bg-gray-100 z-10">
              <div class="flex flex-col items-center">
                <i class="fas fa-image text-gray-400 text-4xl mb-2" />
                <span class="text-gray-500 text-sm">图片加载中...</span>
              </div>
            </div>
            <!-- 错误提示 -->
            <div v-if="imageError" class="absolute inset-0 flex items-center justify-center bg-gray-100 z-10">
              <div class="flex flex-col items-center">
                <i class="fas fa-exclamation-circle text-gray-400 text-4xl mb-2" />
                <span class="text-gray-500 text-sm">图片加载失败</span>
              </div>
            </div>
            <!-- 图片 -->
            <img 
              v-if="!imageError"
              :src="articleData.cover" 
              alt="文章封面" 
              class="w-full h-full object-cover transform group-hover:scale-110 transition-transform duration-500" 
              @load="imageLoading = false"
              @error="imageError = true; imageLoading = false"
            >
          </div>
          
          <!-- 文章正文（支持换行、段落） -->
          <h2 class="text-2xl font-semibold text-gray-800 mb-6 flex items-center border-b-2 border-emerald-500 pb-3">
            <i class="fas fa-book text-emerald-500 mr-2" />文章内容
          </h2>
          <div class="prose prose-emerald max-w-none text-gray-700 leading-relaxed mb-10">
            <!-- 渲染结构化内容 -->
            <template v-for="(item, idx) in paragraphs" :key="idx">
              <!-- h2 标题 -->
              <h2 v-if="item.type === 'h2'" class="text-3xl font-bold text-gray-900 mt-8 mb-4 pt-4 border-t-2 border-emerald-200 flex items-center">
                <span class="w-1 h-8 bg-gradient-to-b from-emerald-500 to-teal-500 mr-3 rounded"></span>
                {{ item.text }}
              </h2>
              
              <!-- h3 标题 -->
              <h3 v-else-if="item.type === 'h3'" class="text-2xl font-semibold text-gray-800 mt-6 mb-3 flex items-center text-emerald-700">
                <span class="w-0.5 h-6 bg-emerald-500 mr-2.5 rounded"></span>
                {{ item.text }}
              </h3>
              
              <!-- h4 标题 -->
              <h4 v-else-if="item.type === 'h4'" class="text-lg font-semibold text-gray-700 mt-4 mb-2 ml-4 flex items-center">
                <span class="inline-block w-1.5 h-1.5 bg-emerald-500 rounded-full mr-2"></span>
                {{ item.text }}
              </h4>
              
              <!-- 段落文本 -->
              <p v-else-if="item.type === 'p'" class="mb-4 text-base text-gray-700 leading-8">
                {{ item.text }}
              </p>
            </template>
          </div>
          
          <!-- 文章标签 -->
          <div v-if="articleData.tags && articleData.tags.length > 0" class="flex flex-wrap gap-2 mt-8 mb-10 pb-6 border-t border-gray-100 pt-6">
            <span
              v-for="tag in articleData.tags"
              :key="tag"
              class="px-4 py-2 bg-emerald-50 text-emerald-700 rounded-full text-sm font-medium border border-emerald-200 hover:bg-emerald-100 transition-colors"
            >
              <i class="fas fa-tag mr-1" />{{ tag }}
            </span>
          </div>
          
          <!-- 相关文章推荐 -->
          <div class="mt-10 border-t border-gray-200 pt-8">
            <h3 class="text-2xl font-semibold mb-6 flex items-center text-gray-800 border-b-2 border-emerald-500 pb-3">
              <i class="fas fa-lightbulb text-emerald-500 mr-2" />推荐阅读
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div 
                v-for="related in relatedArticles" 
                :key="related.id"
                class="p-4 border border-gray-200 rounded-lg hover:bg-emerald-50 cursor-pointer transition-all duration-300 transform hover:-translate-y-1 hover:shadow-md"
                @click="gotoArticleDetail(related.id)"
              >
                <h4 class="font-semibold text-gray-800 mb-2">{{ related.title }}</h4>
                <p class="text-sm text-gray-500 flex items-center">
                  <i class="far fa-clock mr-1 text-emerald-500" />{{ related.readTime }}分钟阅读
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧边栏：保留（和主界面一致） -->
      <aside class="w-full md:w-72 flex-shrink-0 p-6 hidden lg:block" aria-label="右侧工具栏">
        <!-- 复制主界面右侧边栏代码（词典、AI助手等） -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 mb-6 transform transition-all duration-300 hover:shadow-md">
          <h3 class="text-base font-semibold text-gray-800 mb-4 flex items-center">
            <i class="fas fa-book text-emerald-500 mr-2" /> 当前使用词典
          </h3>
          <div class="bg-gradient-to-br from-emerald-50 to-teal-50 rounded-lg p-4 border border-emerald-100">
            <div class="font-semibold text-gray-800 text-base mb-3">
              高考3500词
            </div>
            <div class="text-sm text-gray-600 space-y-3">
              <p class="flex justify-between items-center">
                <span class="text-gray-700">已背单词</span>
                <span class="font-bold text-emerald-600 text-lg">1,280 个</span>
              </p>
              <div class="w-full bg-gray-200 rounded-full h-2">
                <div class="bg-gradient-to-r from-emerald-500 to-teal-500 h-2 rounded-full" style="width: 36%; box-shadow: 0 0 10px rgba(16, 185, 129, 0.3);"></div>
              </div>
              <p class="text-xs text-gray-500 text-right">已完成 36%</p>
            </div>
          </div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden transform transition-all duration-300 hover:shadow-md">
          <div class="bg-gradient-to-r from-emerald-500 to-teal-600 p-4 border-b border-emerald-100">
            <h3 class="text-base font-semibold text-white flex items-center">
              <i class="fas fa-robot mr-2" /> AI学习助手
            </h3>
          </div>
          <div class="p-5 flex flex-col items-center">
            <div class="w-20 h-20 bg-gradient-to-br from-emerald-100 to-teal-100 rounded-full flex items-center justify-center mb-4 shadow-sm border border-emerald-200">
              <i class="fas fa-robot text-emerald-600 text-3xl" />
            </div>
            <p class="text-sm text-gray-600 text-center mb-5 px-2 leading-relaxed">
              有任何语法或作文问题，随时问我哦~
            </p>
            <button class="w-full py-2.5 rounded-lg bg-gradient-to-r from-emerald-500 to-teal-500 text-white transition-all shadow hover:shadow-lg transform hover:-translate-y-1 text-sm font-medium flex items-center justify-center gap-2">
              <i class="fas fa-comments" />开始对话
            </button>
          </div>
        </div>
      </aside>
    </main>

    <!-- 复用主界面的EndBar -->
    <EndBar />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import NavBar from "@/components/common/NavBar.vue";
import ActionButtons from "@/components/common/ActionButtons.vue";
import EndBar from "@/components/common/EndBar.vue";
import FriendItem from "@/components/business/FriendItem.vue";
import { getArticleById, getRelatedArticles } from "@/data/articleData.js";
import { friendApi } from '@/api/friend';

const router = useRouter();
const route = useRoute();
// eslint-disable-next-line no-unused-vars
const articleId = route.params.articleId;

// 文章数据
const articleData = ref({});
const relatedArticles = ref([]);

// 安全拆分段落，避免 content 未定义时报错
const paragraphs = computed(() => {
  const content = articleData.value?.content;
  if (typeof content !== 'string') return [];
  
  const lines = content.split('\n');
  const result = [];
  
  lines.forEach(line => {
    const trimmedLine = line.trim();
    if (!trimmedLine) return; // 跳过空行
    
    // 检测Markdown标题
    if (trimmedLine.startsWith('####')) {
      result.push({
        type: 'h4',
        text: trimmedLine.replace(/^####\s*/, '').trim()
      });
    } else if (trimmedLine.startsWith('###')) {
      result.push({
        type: 'h3',
        text: trimmedLine.replace(/^###\s*/, '').trim()
      });
    } else if (trimmedLine.startsWith('##')) {
      result.push({
        type: 'h2',
        text: trimmedLine.replace(/^##\s*/, '').trim()
      });
    } else {
      result.push({
        type: 'p',
        text: trimmedLine
      });
    }
  });
  
  return result;
});

// 好友列表数据
const friendList = ref([]);
const loading = ref(false);
const error = ref('');
const unreadCounts = ref({});
// eslint-disable-next-line no-unused-vars
const showAddFriendModal = ref(false);
// eslint-disable-next-line no-unused-vars
const showFriendRequestModal = ref(false);
// eslint-disable-next-line no-unused-vars
const friendRequests = ref([]);

// 点赞状态
const isLiked = ref(false);
// 烟花画布
const fireworksCanvas = ref(null);
let canvasCtx = null;
let fireworks = [];
let resizeHandler = null;

// 未读计数轮询定时器
let messagePollInterval = null;

// 初始化文章数据
onMounted(() => {
  // 获取文章数据
  articleData.value = getArticleById(articleId);
  relatedArticles.value = getRelatedArticles(articleId);
  
  // 从本地存储读取点赞状态
  const likedArticles = JSON.parse(localStorage.getItem('likedArticles') || '[]');
  isLiked.value = likedArticles.includes(articleId);
  
  // 加载好友列表
  fetchFriendList();
  fetchUnreadCounts();
  
  // 开始轮询未读消息数
  if (!messagePollInterval) {
    messagePollInterval = setInterval(() => {
      fetchUnreadCounts().catch(err => console.warn('轮询未读数失败', err));
    }, 3000);
  }
  
  // 初始化烟花画布
  initFireworksCanvas();
  
  // 监听路由变化，清理画布
  watch(
    () => route.params.articleId,
    (newId) => {
      fireworks = [];
      if (canvasCtx && fireworksCanvas.value) {
        canvasCtx.clearRect(0, 0, fireworksCanvas.value.width, fireworksCanvas.value.height);
      }
      // 刷新文章数据和相关推荐
      articleData.value = getArticleById(newId);
      relatedArticles.value = getRelatedArticles(newId);
      const likedArticles = JSON.parse(localStorage.getItem('likedArticles') || '[]');
      isLiked.value = likedArticles.includes(newId);
    }
  );
});

// 销毁时清理画布
onUnmounted(() => {
  fireworks = [];
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler);
    resizeHandler = null;
  }
  // 清理轮询定时器
  if (messagePollInterval) {
    clearInterval(messagePollInterval);
    messagePollInterval = null;
  }
});

// 一次性加载好友数据
const fetchFriendList = async () => {
  try {
    loading.value = true;
    const res = await friendApi.getFriendList();
    if (res.code === 200) {
      friendList.value = res.data.map(friend => ({
        id: friend.id,
        name: friend.name,
        avatar: friend.avatar,
        status: friend.status
      }));
    } else {
      error.value = res.message || '获取好友列表失败';
    }
  } catch (err) {
    error.value = '网络异常，获取好友列表失败';
    console.error('获取好友列表异常：', err);
  } finally {
    loading.value = false;
  }
};

// 从后端获取所有好友的未读消息数
const fetchUnreadCounts = async () => {
  try {
    const res = await friendApi.getUnreadCount();
    if (res.code === 200) {
      if (Array.isArray(res.data)) {
        res.data.forEach(item => {
          unreadCounts.value[item.friendId] = item.count || 0;
        });
      } else if (typeof res.data === 'object' && res.data !== null) {
        unreadCounts.value = { ...res.data };
      }
    }
  } catch (err) {
    console.error('获取未读消息数失败：', err);
    unreadCounts.value = {};
  }
};

// 初始化烟花画布
const initFireworksCanvas = () => {
  if (!fireworksCanvas.value) return;
  const canvas = fireworksCanvas.value;
  // 设置画布尺寸为容器大小
  const container = canvas.parentElement;
  canvas.width = container.offsetWidth;
  canvas.height = container.offsetHeight;
  canvasCtx = canvas.getContext('2d');
  
  // 监听窗口大小变化，重置画布尺寸
  resizeHandler = () => {
    canvas.width = container.offsetWidth;
    canvas.height = container.offsetHeight;
  };
  window.addEventListener('resize', resizeHandler);
};

// 点赞处理函数
const handleLike = () => {
  if (isLiked.value) {
    // 取消点赞
    articleData.value.likeCount -= 1;
    isLiked.value = false;
    // 更新本地存储
    let likedArticles = JSON.parse(localStorage.getItem('likedArticles') || '[]');
    likedArticles = likedArticles.filter(id => id !== articleId);
    localStorage.setItem('likedArticles', JSON.stringify(likedArticles));
  } else {
    // 点赞 + 触发烟花特效
    articleData.value.likeCount += 1;
    isLiked.value = true;
    // 更新本地存储
    let likedArticles = JSON.parse(localStorage.getItem('likedArticles') || '[]');
    likedArticles.push(articleId);
    localStorage.setItem('likedArticles', JSON.stringify(likedArticles));
    // 触发烟花
    createFirework();
  }
};

// 创建烟花特效
const createFirework = () => {
  if (!canvasCtx) return;
  
  // 点赞按钮位置（作为烟花发射点），若获取失败则回退到画布中心
  const likeIcon = document.querySelector('.fa-heart');
  let x = fireworksCanvas.value.width / 2;
  let y = fireworksCanvas.value.height / 2;
  if (likeIcon && fireworksCanvas.value) {
    const likeBtn = likeIcon.parentElement;
    if (likeBtn) {
      const rect = likeBtn.getBoundingClientRect();
      const container = fireworksCanvas.value.parentElement;
      const containerRect = container.getBoundingClientRect();
      // 发射点坐标（相对画布）
      x = rect.left - containerRect.left + rect.width / 2;
      y = rect.top - containerRect.top + rect.height / 2;
    }
  }
  
  // 生成10-15个烟花粒子
  const particleCount = Math.floor(Math.random() * 6) + 10;
  for (let i = 0; i < particleCount; i++) {
    const angle = (Math.PI * 2 * i) / particleCount;
    const speed = Math.random() * 4 + 2;
    const hue = Math.floor(Math.random() * 360);

    fireworks.push({
      x,
      y,
      sx: Math.cos(angle) * speed,
      sy: Math.sin(angle) * speed,
      hue,
      size: Math.random() * 3 + 2,
      alpha: 1,
      decay: Math.random() * 0.01 + 0.005
    });
  }
  
  // 动画循环
  const animate = () => {
    // 半透明背景，实现拖影效果
    canvasCtx.clearRect(0, 0, fireworksCanvas.value.width, fireworksCanvas.value.height);
    canvasCtx.fillStyle = 'rgba(255,255,255,0.1)';
    canvasCtx.fillRect(0, 0, fireworksCanvas.value.width, fireworksCanvas.value.height);
    
    // 更新烟花粒子
    for (let i = fireworks.length - 1; i >= 0; i--) {
      const p = fireworks[i];
      p.x += p.sx;
      p.y += p.sy;
      p.alpha -= p.decay;
      p.sy += 0.05; // 重力
      
      // 绘制粒子
      canvasCtx.beginPath();
      canvasCtx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
      canvasCtx.fillStyle = `hsla(${p.hue}, 100%, 50%, ${Math.max(p.alpha, 0)})`;
      canvasCtx.fill();
      
      // 移除透明度为0的粒子
      if (p.alpha <= 0) {
        fireworks.splice(i, 1);
      }
    }
    
    // 还有粒子就继续动画
    if (fireworks.length > 0) {
      requestAnimationFrame(animate);
    }
  };
  
  animate();
};

// 跳转函数
const gotoHome = () => {
  router.push({ name: "Home" }).catch(() => {});
};

const gotoChat = () => {
  router.push({ name: "Chat" }).catch(() => {});
};

const gotoRank = () => {
  router.push({ name: "Rank" }).catch(() => {});
};

const gotoSettings = () => {
  router.push({ name: "Settings" }).catch(() => {});
};

const handleNotifications = () => {
  // 复用主界面逻辑
};

const gotoArticleDetail = (id) => {
  router.push({ name: "ArticleDetail", params: { articleId: id } }).catch(() => {});
};

// 复用主界面的navItems
const navItems = [
  { label: "首页", onClick: gotoHome, isActive: true },
  { label: "课程", onClick: () => router.push({ name: "Course" }).catch(() => {}) },
  { label: "题库", onClick: () => router.push({ name: "QuestionBank" }).catch(() => {}) },
  { label: "时间表", onClick: () => router.push({ name: "TimeTable" }).catch(() => {}) },
  { label: "单词打卡", onClick: () => router.push({ name: "WordCheckIn" }).catch(() => {}) },
  { label: "AI伴学", onClick: () => router.push({ name: "AiChat" }).catch(() => {}) },
];

// 学习建议弹窗相关（复用主界面逻辑）
const showSuggestionsModal = ref(false);

// 图片加载状态
const imageLoading = ref(true);
const imageError = ref(false);
</script>

<style scoped>
/* 文章正文样式优化 */
.prose p {
  margin-bottom: 1.25rem;
  line-height: 1.8;
  font-size: 1rem;
}

.prose h2 {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 2rem 0 1rem;
  color: #1f2937;
}

.prose h3 {
  font-size: 1.35rem;
  font-weight: 600;
  margin: 1.5rem 0 0.75rem;
  color: #374151;
}

/* Markdown 标题层级样式 */
.prose h2:first-of-type {
  margin-top: 0;
  padding-top: 0;
  border-top: none;
}

.prose h4 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 1rem 0 0.5rem 1rem;
  color: #4b5563;
}

/* 点赞按钮动画 */
button {
  transition: all 0.3s ease;
}

/* 烟花画布层级 */
canvas {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 10;
}

/* 自定义滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f3f4f6;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 4px;
  transition: background 0.3s;
}

::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

/* 渐变文本效果 */
.gradient-text {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>