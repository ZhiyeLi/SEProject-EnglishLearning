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
                <span class="font-semibold text-emerald-600">42 天</span>
              </li>
              <li class="flex justify-between items-center">
                <span class="text-gray-600">总单词量</span>
                <span class="font-semibold text-emerald-600">1,258 个</span>
              </li>
              <li class="flex justify-between items-center">
                <span class="text-gray-600">学习时长</span>
                <span class="font-semibold text-emerald-600">86 小时</span>
              </li>
              <li class="flex justify-between items-center">
                <span class="text-gray-600">完成课程</span>
                <span class="font-semibold text-emerald-600">12 门</span>
              </li>
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
                学习进度
              </h2>
              <button class="text-emerald-600 hover:text-emerald-700 text-sm font-medium">
                查看全部
              </button>
            </div>
            
            <!-- 进度条示例 -->
            <div class="space-y-6">
              <div>
                <div class="flex justify-between mb-2">
                  <span class="text-sm font-medium text-gray-700">四级词汇</span>
                  <span class="text-sm font-medium text-emerald-600">68%</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2.5">
                  <div
                    class="bg-emerald-500 h-2.5 rounded-full"
                    style="width: 68%"
                  />
                </div>
              </div>
              
              <div>
                <div class="flex justify-between mb-2">
                  <span class="text-sm font-medium text-gray-700">语法精通</span>
                  <span class="text-sm font-medium text-emerald-600">42%</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2.5">
                  <div
                    class="bg-emerald-500 h-2.5 rounded-full"
                    style="width: 42%"
                  />
                </div>
              </div>
              
              <div>
                <div class="flex justify-between mb-2">
                  <span class="text-sm font-medium text-gray-700">阅读理解</span>
                  <span class="text-sm font-medium text-emerald-600">75%</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2.5">
                  <div
                    class="bg-emerald-500 h-2.5 rounded-full"
                    style="width: 75%"
                  />
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
              <button class="text-emerald-600 hover:text-emerald-700 text-sm font-medium">
                更多
              </button>
            </div>
            
            <div class="space-y-4">
              <div class="flex gap-3 pb-4 border-b border-gray-100">
                <div class="w-8 h-8 rounded-full bg-emerald-100 flex items-center justify-center flex-shrink-0 mt-1">
                  <i class="fas fa-check text-emerald-600" />
                </div>
                <div>
                  <p class="text-gray-800">
                    完成了 <span class="font-medium">英语四级单词</span> 学习任务
                  </p>
                  <p class="text-sm text-gray-500 mt-1">
                    今天 09:45
                  </p>
                </div>
              </div>
              
              <div class="flex gap-3 pb-4 border-b border-gray-100">
                <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0 mt-1">
                  <i class="fas fa-comment text-blue-600" />
                </div>
                <div>
                  <p class="text-gray-800">
                    向 <span class="font-medium">AI 学习助手</span> 咨询了语法问题
                  </p>
                  <p class="text-sm text-gray-500 mt-1">
                    昨天 16:20
                  </p>
                </div>
              </div>
              
              <div class="flex gap-3">
                <div class="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center flex-shrink-0 mt-1">
                  <i class="fas fa-trophy text-purple-600" />
                </div>
                <div>
                  <p class="text-gray-800">
                    在 <span class="font-medium">周排行榜</span> 中获得第三名
                  </p>
                  <p class="text-sm text-gray-500 mt-1">
                    3天前
                  </p>
                </div>
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
import { ref, onMounted } from 'vue';  
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user'; // 引入Pinia用户状态
// 引入组件库的 NavBar 和 EndBar（路径根据实际组件库位置调整）
import NavBar from '@/components/common/NavBar.vue';
import EndBar from '@/components/common/EndBar.vue';
import EditProfile from '@/components/profile/EditProfile.vue'; // 引入编辑组件


const router = useRouter();
const userStore = useUserStore(); // 注入用户状态
const isEditOpen = ref(false); // 控制编辑对话框显示/隐藏

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


const navItems = [
  { label: "首页", onClick: gotoHome, isActive: false },
  { label: "课程", onClick: gotoCourse,isActive: false },
  { label: "题库", onClick: gotoQuestionBank,isActive: false },
  { label: "时间表", onClick: gotoTimeTable },
  { label: "单词打卡", onClick: gotoWordCheckIn, isActive: false },
  { label: "AI伴学", onClick: gotoAiChat, isActive: false },
];

onMounted(async () => {
  // 强制刷新用户信息，确保最新
  await userStore.fetchUserInfo(true);
  console.log('Profile页面用户信息：', userStore.userInfo); // 调试日志
});
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