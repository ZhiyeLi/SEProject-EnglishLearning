<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 复用组件库的 NavBar（完整显示导航项，支持Logo返回首页） -->
    <NavBar 
      :nav-items="navItems" 
      :user-info="userStore.userInfo" 
    />
    
    <!-- 返回首页按钮（保留，放在导航栏下方） -->
    <div class="container mx-auto px-4 py-4">
      <button 
        class="flex items-center gap-2 px-5 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors shadow-sm"
        @click="gotoHome"
      >
        <i class="fas fa-arrow-left" />
        <span>返回首页</span>
      </button>
    </div>
    
    <!-- 个人主页头部横幅（待修改） -->
    <!-- <div class="bg-gradient-to-r from-emerald-500 to-teal-400 h-48 md:h-64 relative"> -->
    <!-- 背景装饰 -->
    <!-- </div> -->
    
    <!-- 个人信息区域 -->
    <div class="container mx-auto px-4 -mt-20 relative z-10">
      <div class="flex flex-col md:flex-row gap-6">
        <!-- 头像区域 -->
        <div class="flex-shrink-0">
          <div class="w-32 h-32 md:w-40 md:h-40 rounded-full border-4 border-white shadow-md overflow-hidden">
            <img 
              :src="userStore.userInfo.avatar"  
              alt="用户头像" 
              class="w-full h-full object-cover"
            ><!-- 绑定Pinia中的头像 -->
          </div>
        </div>
        
        <!-- 用户信息 -->
        <div class="flex-grow">
          <div class="flex flex-col md:flex-row md:items-end justify-between gap-4 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
            <div>
              <h1 class="text-2xl md:text-3xl font-bold text-gray-900">
                {{ userStore.userInfo.name }}
              </h1>
              <p class="text-gray-600 mt-1">
                {{ userStore.userInfo.signature }}
              </p>
              <div>
                email:
                <span class="text-gray-600 mt-1">{{ userStore.userInfo.email }}</span>
              </div>
              <div>
                phone:
                <span class="text-gray-600 mt-1">{{ userStore.userInfo.phone }}</span>
              </div>
              <div class="flex items-center mt-3 text-sm text-gray-500">
                <span class="flex items-center mr-4">
                  <i class="fas fa-calendar-alt mr-1" /> 加入于 {{ userStore.userInfo.joinTime }}
                </span>
                <span class="flex items-center">
                  <i class="fas fa-map-marker-alt mr-1" /> {{ userStore.userInfo.location }}
                </span>
              </div>
            </div>
            
            <button 
              class="self-start md:self-auto px-5 py-2 bg-gray-100 text-gray-800 rounded-lg hover:bg-gray-200 transition-colors"
              @click="isEditOpen = true"
            >
              编辑资料
            </button>
          </div>
        </div>
      </div>
      
      <!-- 个人主页内容区 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6">
        <!-- 左侧边栏 -->
        <div class="lg:col-span-1 space-y-6">
          <!-- 学习统计卡片 -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
            <h2 class="text-lg font-semibold text-gray-800 mb-4">
              学习统计
            </h2>
            <ul class="space-y-3">
              <li class="flex justify-between items-center">
                <span class="text-gray-600">连续打卡</span>
                <span class="font-semibold text-emerald-600">{{ learningStats.consecutiveDays }} 天</span>
              </li>
              <li class="flex justify-between items-center">
                <span class="text-gray-600">总单词量</span>
                <span class="font-semibold text-emerald-600">{{ learningStats.totalWords }} 个</span>
              </li>
              <li class="flex justify-between items-center">
                <span class="text-gray-600">已做题目</span>
                <span class="font-semibold text-emerald-600">{{ learningStats.totalQuestions }} 道</span>
              </li>
              <!-- <li class="flex justify-between items-center">
                <span class="text-gray-600">完成课程</span>
                <span class="font-semibold text-emerald-600">12 门</span>
              </li> -->
            </ul>
          </div>
          
          <!-- 技能标签 -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
            <h2 class="text-lg font-semibold text-gray-800 mb-4">
              擅长领域
            </h2>
            <div class="flex flex-wrap gap-2">
              <span class="px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-sm">英语阅读</span>
              <span class="px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-sm">词汇记忆</span>
              <span class="px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-sm">语法</span>
              <span class="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm">听力</span>
            </div>
          </div>
        </div>
        
        <!-- 右侧主内容 -->
        <div class="lg:col-span-2 space-y-6">
          <!-- 学习进度 -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
            <div class="flex justify-between items-center mb-6">
              <h2 class="text-lg font-semibold text-gray-800">
                单词学习进度
              </h2>
              <button 
                class="text-emerald-600 hover:text-emerald-700 text-sm font-medium"
                @click="gotoWordCheckIn"
              >
                查看全部
              </button>
            </div>
            
            <!-- 词汇学习进度条 -->
            <div class="space-y-6">
              <div
                v-for="type in wordTypes"
                :key="type.typeId"
                class="progress-item"
              >
                <div class="flex justify-between mb-2">
                  <span class="text-sm font-medium text-gray-700">{{ type.name }}</span>
                  <span class="text-sm font-medium text-emerald-600">{{ getWordTypeProgress(type.typeId) }}%</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2.5">
                  <div
                    class="bg-emerald-500 h-2.5 rounded-full transition-all duration-500"
                    :style="{ width: getWordTypeProgress(type.typeId) + '%' }"
                  />
                </div>
                <div class="text-xs text-gray-500 mt-1">
                  {{ (wordProgress[type.typeId]?.passedCount || 0) }} / {{ type.totalWords }} 个单词
                </div>
              </div>
            </div>
          </div>
          
          <!-- 最近活动 -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
            <div class="flex justify-between items-center mb-6">
              <h2 class="text-lg font-semibold text-gray-800">
                最近活动
              </h2>
              <button 
                v-if="activities.length > 0"
                class="text-emerald-600 hover:text-emerald-700 text-sm font-medium"
                @click="toggleShowAllActivities"
              >
                {{ showAllActivities ? '收起' : '更多' }}
              </button>
            </div>
            
            <div class="space-y-4">
              <div
                v-for="(activity, index) in displayedActivities"
                :key="index"
                class="flex gap-3 pb-4 border-b border-gray-100 last:border-b-0"
              >
                <div 
                  class="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 mt-1"
                  :class="`bg-${activity.iconColor}-100`"
                >
                  <i 
                    :class="`${activity.icon} text-${activity.iconColor}-600`" 
                  />
                </div>
                <div>
                  <p class="text-gray-800">
                    {{ activity.title }}
                  </p>
                  <p class="text-sm text-gray-500 mt-1">
                    {{ activity.subtitle }}
                  </p>
                </div>
              </div>
              
              <!-- 无活动提示 -->
              <div 
                v-if="activities.length === 0"
                class="text-center py-8 text-gray-500"
              >
                <i class="fas fa-calendar-alt text-3xl mb-2 block" />
                <p>今日暂无学习活动</p>
                <p class="text-sm">
                  开始学习后，您的活动将显示在这里
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 复用组件库的 EndBar（页脚） -->
    <EndBar class="mt-auto" />
    <!-- 编辑资料对话框（引入组件） -->
    <EditProfile 
      v-model:open="isEditOpen" 
    /><!-- 双向绑定对话框显示状态 -->
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user'; // 引入Pinia用户状态
// 引入组件库的 NavBar 和 EndBar（路径根据实际组件库位置调整）
import NavBar from '@/components/common/NavBar.vue';
import EndBar from '@/components/common/EndBar.vue';
import EditProfile from '@/components/profile/EditProfile.vue'; // 引入编辑组件
import { wordProgressManager } from '@/utils/wordData.js';
import { planApi } from '@/api/plan.js';
import { wordApi } from '@/api/word.js';
import questionApi from '@/api/question.js';

const router = useRouter();
const userStore = useUserStore(); // 注入用户状态
const isEditOpen = ref(false); // 控制编辑对话框显示/隐藏

// 学习统计相关
const learningStats = ref({
  consecutiveDays: 0,
  totalWords: 0,
  totalQuestions: 0
});

// 词汇学习进度相关
const wordTypes = ref([]);
const wordProgress = ref({});

// 最近活动相关
const activities = ref([]);
const showAllActivities = ref(false);
const initialDisplayCount = 4;

// 加载词汇类型和进度数据
onMounted(async () => {
  try {
    // 初始化单词进度管理器
    await wordProgressManager.init();
    
    // 获取词汇类型列表
    const types = await wordProgressManager.getWordTypeList();
    wordTypes.value = types;
    
    // 获取用户进度
    const progress = await wordProgressManager.getProgress();
    wordProgress.value = progress || {};

    // 加载学习统计数据
    try {
      await loadLearningStats();
    } catch (error) {
      console.error('加载学习统计失败:', error);
      // 设置默认值以防API调用失败
      learningStats.value = {
        consecutiveDays: 0,
        totalWords: 0,
        totalQuestions: 0
      };
    }

    // 加载今日活动数据
    await loadTodayActivities();
  } catch (error) {
    console.error('加载数据失败:', error);
  }
});

const gotoHome = () => {
  router.push('/').catch(() => {});
};

const gotoWordCheckIn = () => {
  router.push({ name: "WordCheckIn" }).catch(() => {});
};

const gotoAiChat = () => {
  router.push({ name: "AiChat" }).catch(() => {});
};

const gotoTimeTable = () => {
  router.push({ name: "TimeTable" }).catch(() => {});
};

const gotoCourse = () => {
  router.push({ name: "Course" }).catch(() => {});
};

const gotoQuestionBank = () => {
  router.push({ name: "QuestionBank" }).catch(() => {});
};

// 加载学习统计数据
const loadLearningStats = async () => {
  try {
    // 获取连续打卡天数
    const consecutiveResponse = await wordApi.getConsecutiveCheckInDays();
    if (consecutiveResponse && consecutiveResponse.code === 200) {
      learningStats.value.consecutiveDays = consecutiveResponse.data || 0;
    }

    // 获取总单词量
    const wordsResponse = await wordApi.getTotalLearnedWords();
    if (wordsResponse && wordsResponse.code === 200) {
      learningStats.value.totalWords = wordsResponse.data || 0;
    }

    // 获取总做题数量
    const questionsResponse = await questionApi.getTotalAnsweredQuestions();
    if (questionsResponse && questionsResponse.code === 200) {
      learningStats.value.totalQuestions = questionsResponse.data || 0;
    }
  } catch (error) {
    console.error('加载学习统计失败:', error);
    // 确保有默认值
    learningStats.value = {
      consecutiveDays: 0,
      totalWords: 0,
      totalQuestions: 0
    };
    throw error; // 重新抛出错误以便catch块处理
  }
};

// 计算词汇类型进度百分比
const getWordTypeProgress = (typeId) => {
  const progress = wordProgress.value[typeId] || { passedCount: 0 };
  const type = wordTypes.value.find(t => t.typeId === typeId);
  if (!type || !type.totalWords) return 0;
  
  const total = Number(type.totalWords) || 0;
  const passed = Number(progress.passedCount) || 0;
  if (total === 0) return 0;
  
  const percentage = Math.min((passed / total) * 100, 100);
  return Math.round(percentage);
};

// 加载今日活动数据
const loadTodayActivities = async () => {
  try {
    const todayActivities = [];

    // 1. 获取今日完成的计划
    try {
      const planResponse = await planApi.getTodayPlans();
      if (planResponse.code === 200 && planResponse.data) {
        const completedPlans = planResponse.data.filter(plan => plan.completed);
        completedPlans.forEach(plan => {
          todayActivities.push({
            type: 'plan',
            icon: 'fas fa-check',
            iconColor: 'emerald',
            title: `完成了计划：${plan.title}`,
            subtitle: `计划类型：${plan.category} · ${formatTime(plan.completedAt || plan.updatedAt)}`,
            time: plan.completedAt || plan.updatedAt
          });
        });
      }
    } catch (error) {
      console.error('获取今日计划失败:', error);
    }

    // 2. 获取今日打卡的单词
    try {
      const wordResponse = await wordApi.getTodayCheckInStatus();
      if (wordResponse.code === 200 && wordResponse.data) {
        // API返回的数据结构: { typeStats: { "1": { learn: 10, review: 5 }, ... } }
        const typeStats = wordResponse.data.typeStats || {};
        Object.entries(typeStats).forEach(([typeId, stats]) => {
          const type = wordTypes.value.find(t => t.typeId == typeId);
          const typeName = type ? type.name : `类型${typeId}`;
          
          // 显示学习统计
          if (stats.learn && stats.learn > 0) {
            todayActivities.push({
              type: 'word-learn',
              icon: 'fas fa-book-open',
              iconColor: 'blue',
              title: `单词学习：${typeName}`,
              subtitle: `新学了 ${stats.learn} 个单词`,
              time: new Date().toISOString()
            });
          }
          
          // 显示复习统计
          if (stats.review && stats.review > 0) {
            todayActivities.push({
              type: 'word-review',
              icon: 'fas fa-redo',
              iconColor: 'orange',
              title: `单词复习：${typeName}`,
              subtitle: `复习了 ${stats.review} 个单词`,
              time: new Date().toISOString()
            });
          }
        });
      }
    } catch (error) {
      console.error('获取今日单词打卡失败:', error);
    }

    // 3. 获取今日做的题目
    try {
      const questionResponse = await questionApi.getTodayStatsByType();
      if (questionResponse.code === 200 && questionResponse.data) {
        const questionData = questionResponse.data;
        Object.entries(questionData).forEach(([questionType, count]) => {
          if (count > 0) {
            // 将英文类型转换为中文显示
            const typeDisplayName = getQuestionTypeDisplayName(questionType);
            todayActivities.push({
              type: 'question',
              icon: 'fas fa-question-circle',
              iconColor: 'purple',
              title: `题目练习：${typeDisplayName}`,
              subtitle: `完成了 ${count} 道题目`,
              time: new Date().toISOString() // 使用当前时间作为练习时间
            });
          }
        });
      }
    } catch (error) {
      console.error('获取今日题目统计失败:', error);
    }

    // 按时间倒序排序
    todayActivities.sort((a, b) => new Date(b.time) - new Date(a.time));
    activities.value = todayActivities;
  } catch (error) {
    console.error('加载今日活动失败:', error);
  }
};

// 格式化时间显示
const formatTime = (timeString) => {
  if (!timeString) return '';
  const date = new Date(timeString);
  const now = new Date();
  const diff = now - date;
  const hours = Math.floor(diff / (1000 * 60 * 60));
  
  if (hours < 1) {
    return '刚刚';
  } else if (hours < 24) {
    return `${hours}小时前`;
  } else {
    const days = Math.floor(hours / 24);
    return `${days}天前`;
  }
};

// 切换显示更多活动
const toggleShowAllActivities = () => {
  showAllActivities.value = !showAllActivities.value;
};

// 计算要显示的活动列表
const displayedActivities = computed(() => {
  if (showAllActivities.value) {
    return activities.value;
  }
  return activities.value.slice(0, initialDisplayCount);
});

// 将题目类型英文转换为中文显示
const getQuestionTypeDisplayName = (type) => {
  const typeMap = {
    'CET4': '四级',
    'CET6': '六级',
    'TOEFL': '托福',
    'IELTS': '雅思',
    'KY': '考研'
  };
  return typeMap[type] || type;
};


const navItems = [
  { label: "首页", onClick: gotoHome, isActive: false },
  { label: "课程", onClick: gotoCourse,isActive: false },
  { label: "题库", onClick: gotoQuestionBank,isActive: false },
  { label: "时间表", onClick: gotoTimeTable },
  { label: "单词打卡", onClick: gotoWordCheckIn, isActive: false },
  { label: "AI伴学", onClick: gotoAiChat, isActive: false },
];

</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 优化返回按钮交互体验 */
button:hover {
  transform: translateY(-1px);
}
</style>