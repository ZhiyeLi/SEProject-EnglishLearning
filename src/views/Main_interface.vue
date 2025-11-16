<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 引入NavBar组件，自定义右侧操作按钮 -->
    <NavBar>
      <template #actions>
        <button class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group">
          <i class="fas fa-lightbulb text-lg"></i>
          <span class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">学习建议</span>
        </button>
        <button class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group">
          <i class="fas fa-cog text-lg"></i>
          <span class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">设置</span>
        </button>
        <button class="relative ml-2 text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors">
          <i class="fas fa-bell text-lg"></i>
          <span class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center animate-pulse">3</span>
        </button>
      </template>
    </NavBar>

    <!-- 主内容区 -->
    <main class="flex-grow flex flex-col md:flex-row">
      <!-- 左侧好友列表：使用FriendItem组件 -->
      <aside class="w-full md:w-96 bg-white border-r border-gray-200 shadow-sm md:h-[calc(100vh-64px)] sticky top-[64px] overflow-y-auto flex-shrink-0 z-20">
        <div class="p-5 h-full flex flex-col">
          <!-- 搜索框：保留 -->
          <div class="relative mb-6">
            <input 
              type="text" 
              placeholder="搜索好友..." 
              class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-base"
            >
            <i class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-lg"></i>
          </div>
          
          <!-- 好友列表区域：使用FriendItem -->
          <div class="flex-grow overflow-y-auto -mx-2 px-2">
            <h3 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4 mt-2">
              好友列表
            </h3>
            
            <ul class="space-y-2">
              <FriendItem 
                name="示例好友" 
                avatar="https://picsum.photos/seed/friend1/100/100" 
                status="online" 
                extra-info="今日已背50个单词"
              >
                <template #actions>
                  <button class="text-gray-600 hover:text-emerald-600 p-1 rounded-full hover:bg-emerald-50 transition-colors">
                    <i class="fas fa-comment"></i>
                  </button>
                </template>
              </FriendItem>
              <li>
                <button 
                  @click="handleAddFriend"
                  class="w-full flex items-center justify-center p-3 text-emerald-600 text-sm border border-dashed border-emerald-200 rounded-lg hover:bg-emerald-50 transition-all hover:border-emerald-300 group"
                >
                  <i class="fas fa-plus-circle mr-2 group-hover:scale-110 transition-transform"></i>
                  可添加更多好友
                </button>
              </li>
            </ul>
          </div>
          
          <!-- 底部功能選項：好友 在左、聊天 在中間（預設選中好友） -->
          <div class="border-t border-gray-100 mt-4 pt-3 flex justify-around">
            <button
              @click="gotoHome"
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'friends' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
              ]"
            >
              <i class="fas fa-users text-xl mb-1"></i>
              <span class="text-sm">好友</span>
            </button>

              <button
                @click="gotoChat"
                :class="[
                  'flex flex-col items-center py-1 transition-colors',
                  activeTab === 'chat' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
                ]"
              >
                <i class="fas fa-comment text-xl mb-1"></i>
                <span class="text-sm">聊天</span>
              </button>

            <button
              @click="activeTab = 'rank'"
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'rank' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
              ]"
            >
              <i class="fas fa-trophy text-xl mb-1"></i>
              <span class="text-sm">排行榜</span>
            </button>
          </div>
        </div>
      </aside>
      
      <!-- 中间内容区：清空打卡数字和计划项目 -->
      <div class="flex-grow p-6 overflow-y-auto">
        <div class="max-w-5xl mx-auto space-y-6">
          <!-- 打卡统计卡片：清空具体数字，保留框架 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 relative overflow-hidden transform transition-all duration-300 hover:shadow-md">
            <div class="absolute top-0 right-0 w-40 h-40 bg-gradient-to-br from-emerald-400 via-teal-400 to-cyan-400 rounded-full -mr-16 -mt-16 opacity-20"></i>
            
            <div class="flex flex-col md:flex-row justify-between items-start md:items-center">
              <div>
                <h2 class="text-2xl font-bold text-emerald-600 mb-3 flex items-center">
                  已连续打卡 -- 天！！！
                  <span class="ml-2 text-yellow-500 animate-pulse"><i class="fas fa-fire"></i></span>
                </h2>
                <ul class="space-y-2 text-gray-700 text-base">
                  <li class="flex items-center">
                    <i class="fas fa-plus-circle text-emerald-500 mr-2"></i>今日新学 <span class="font-semibold">-- 个单词</span>
                  </li>
                  <li class="flex items-center">
                    <i class="fas fa-sync text-yellow-500 mr-2"></i>今日复习 <span class="font-semibold">-- 个单词</span>
                  </li>
                  <li class="flex items-center">
                    <i class="fas fa-calendar-alt text-emerald-500 mr-2"></i>明日需复习 <span class="font-semibold">-- 个单词</span>
                  </li>
                </ul>
              </div>
              
              <div class="flex items-center mt-6 md:mt-0">
                <div class="relative mr-6">
                  <div class="w-28 h-28 bg-gradient-to-r from-emerald-300 via-teal-200 to-cyan-300 rounded-full flex items-center justify-center shadow-md">
                    <i class="fas fa-paw text-4xl text-white"></i>
                  </div>
                  <div class="absolute -top-2 left-1/2 transform -translate-x-1/2 w-24 h-6 bg-gradient-to-r from-emerald-400 via-teal-300 to-cyan-400 rounded-full shadow-sm" />
                </div>
                
                <button class="bg-emerald-500 hover:bg-emerald-600 text-white px-6 py-3 rounded-lg font-medium transition-all shadow-md hover:shadow-lg transform hover:-translate-y-0.5 text-base">
                  开始背单词 <i class="fas fa-arrow-right ml-1"></i>
                </button>
              </div>
            </div>
          </div>
          
          <!-- 计划时间表：使用CustomButton -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 transform transition-all duration-300 hover:shadow-md">
            <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center">
              <i class="fas fa-calendar-check text-emerald-500 mr-2"></i>计划时间表
            </h3>
            <p class="text-lg font-bold text-sky-500 mb-4">2025年xx月xx日</p>
            
            <div class="grid grid-cols-7 gap-2 text-center">
              <!-- 星期标题：保留 -->
              <div class="text-sm font-medium text-gray-500 py-2">一</div>
              <div class="text-sm font-medium text-gray-500 py-2">二</div>
              <div class="text-sm font-medium text-gray-500 py-2">三</div>
              <div class="text-sm font-medium text-gray-500 py-2">四</div>
              <div class="text-sm font-medium text-gray-500 py-2">五</div>
              <div class="text-sm font-medium text-gray-500 py-2">六</div>
              <div class="text-sm font-medium text-gray-500 py-2">日</div>
              
              <!-- 清空所有计划项目，替换为占位提示 -->
              <div class="bg-gray-50 rounded-lg p-3 text-sm text-gray-400 shadow-sm border border-gray-100 flex items-center justify-center">
                待添加
              </div>
              <div class="bg-gray-50 rounded-lg p-3 text-sm text-gray-400 shadow-sm border border-gray-100 flex items-center justify-center">
                待添加
              </div>
              <div class="bg-gray-50 rounded-lg p-3 text-sm text-gray-400 shadow-sm border border-gray-100 flex items-center justify-center">
                待添加
              </div>
              <div class="bg-gray-50 rounded-lg p-3 text-sm text-gray-400 shadow-sm border border-gray-100 flex items-center justify-center">
                待添加
              </div>
              <div class="bg-gray-50 rounded-lg p-3 text-sm text-gray-400 shadow-sm border border-gray-100 flex items-center justify-center">
                待添加
              </div>
              <div class="bg-gray-50 rounded-lg p-3 text-sm text-gray-400 shadow-sm border border-gray-100 flex items-center justify-center">
                待添加
              </div>
              <div class="bg-gray-50 rounded-lg p-3 text-sm text-gray-400 shadow-sm border border-gray-100 flex items-center justify-center">
                待添加
              </div>
            </div>
            
            <!-- 新增按钮：使用CustomButton -->
            <CustomButton 
              type="secondary" 
              icon="fa-plus-circle" 
              text="新增学习计划" 
              class="mt-4 text-emerald-600 hover:text-emerald-700 text-base flex items-center transition-colors transform hover:-translate-x-0.5"
            />
          </div>
          
          <!-- 文章推送：保持原样（用户未要求修改） -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 transform transition-all duration-300 hover:shadow-md">
            <h3 class="text-xl font-semibold text-gray-800 mb-5 flex items-center">
              <i class="fas fa-newspaper text-emerald-500 mr-2"></i> 推荐学习文章
            </h3>
            
            <div class="space-y-6">
              <!-- 文章1 -->
              <div class="group flex flex-col md:flex-row gap-5 pb-5 border-b border-gray-100 hover:bg-emerald-50 p-2 rounded-lg transition-all duration-200">
                <div class="w-full md:w-56 h-40 flex-shrink-0 overflow-hidden rounded-lg shadow-sm">
                  <img 
                    src="https://picsum.photos/seed/english1/400/300" 
                    alt="英语听力技巧" 
                    class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                  >
                </div>
                <div class="flex-grow">
                  <h4 class="font-bold text-gray-800 text-xl group-hover:text-emerald-600 transition-colors">
                    如何有效提高英语听力水平
                  </h4>
                  <p class="text-base text-gray-600 mt-2 line-clamp-2">
                    本文介绍了几种实用的英语听力训练方法，帮助学习者快速提升听力理解能力，包括精听与泛听结合、影子跟读法等技巧，适合各阶段英语学习者参考。
                  </p>
                  <div class="mt-3 text-sm text-gray-500 flex items-center justify-between">
                    <div>
                      <span class="mr-4"><i class="far fa-eye mr-1"></i> 2.3k 阅读</span>
                      <span><i class="far fa-comment mr-1"></i> 56 评论</span>
                    </div>
                    <span class="text-emerald-500"><i class="far fa-clock mr-1"></i> 5分钟阅读</span>
                  </div>
                </div>
              </div>
              
              <!-- 文章2 -->
              <div class="group flex flex-col md:flex-row gap-5 pb-5 border-b border-gray-100 hover:bg-emerald-50 p-2 rounded-lg transition-all duration-200">
                <div class="w-full md:w-56 h-40 flex-shrink-0 overflow-hidden rounded-lg shadow-sm">
                  <img 
                    src="https://picsum.photos/seed/english2/400/300" 
                    alt="英语作文技巧" 
                    class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                  >
                </div>
                <div class="flex-grow">
                  <h4 class="font-bold text-gray-800 text-xl group-hover:text-emerald-600 transition-colors">
                    高考英语作文高分技巧
                  </h4>
                  <p class="text-base text-gray-600 mt-2 line-clamp-2">
                    掌握这些写作技巧，让你的英语作文在考试中脱颖而出，轻松获得高分。从结构布局到高级词汇运用，全面解析评分要点和得分技巧。
                  </p>
                  <div class="mt-3 text-sm text-gray-500 flex items-center justify-between">
                    <div>
                      <span class="mr-4"><i class="far fa-eye mr-1"></i> 3.1k 阅读</span>
                      <span><i class="far fa-comment mr-1"></i> 89 评论</span>
                    </div>
                    <span class="text-emerald-500"><i class="far fa-clock mr-1"></i> 7分钟阅读</span>
                  </div>
                </div>
              </div>
              
              <!-- 文章3 -->
              <div class="group flex flex-col md:flex-row gap-5 hover:bg-emerald-50 p-2 rounded-lg transition-all duration-200">
                <div class="w-full md:w-56 h-40 flex-shrink-0 overflow-hidden rounded-lg shadow-sm">
                  <img 
                    src="https://picsum.photos/seed/english3/400/300" 
                    alt="英语语法学习" 
                    class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                  >
                </div>
                <div class="flex-grow">
                  <h4 class="font-bold text-gray-800 text-xl group-hover:text-emerald-600 transition-colors">
                    30天掌握英语语法核心知识点
                  </h4>
                  <p class="text-base text-gray-600 mt-2 line-clamp-2">
                    系统化学习语法知识，30天计划帮助你构建完整的英语语法体系，从基础句型到复杂句式，循序渐进打好语法基础，适合中高考学生。
                  </p>
                  <div class="mt-3 text-sm text-gray-500 flex items-center justify-between">
                    <div>
                      <span class="mr-4"><i class="far fa-eye mr-1"></i> 1.8k 阅读</span>
                      <span><i class="far fa-comment mr-1"></i> 42 评论</span>
                    </div>
                    <span class="text-emerald-500"><i class="far fa-clock mr-1"></i> 6分钟阅读</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧边栏：保持原样（用户未要求修改） -->
      <aside class="w-full md:w-72 flex-shrink-0 p-6 hidden lg:block">
        <!-- 当前使用词典 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 mb-6 transform transition-all duration-300 hover:shadow-md">
          <h3 class="text-base font-semibold text-gray-800 mb-3 flex items-center">
            <i class="fas fa-book text-emerald-500 mr-2"></i> 当前使用词典
          </h3>
          
          <div class="bg-emerald-50 rounded-lg p-3 mb-3 border border-emerald-100">
            <div class="font-medium text-gray-800 text-base">
              高考3500词
            </div>
            <div class="text-base text-gray-600 mt-2 space-y-1">
              <p class="flex justify-between">
                <span>已背单词</span>
                <span class="font-medium text-emerald-600">1,280 个</span>
              </p>
              <div class="w-full bg-gray-200 rounded-full h-1.5 mt-1">
                <div
                  class="bg-emerald-500 h-1.5 rounded-full"
                  style="width: 36%"
                />
              </div>
              <p class="flex justify-between mt-2">
                <span>剩余单词</span>
                <span class="font-medium text-gray-700">2,220 个</span>
              </p>
            </div>
          </div>
          
          <button class="w-full bg-emerald-50 hover:bg-emerald-100 text-emerald-700 py-2 rounded-lg transition-all transform hover:-translate-y-0.5 shadow-sm hover:shadow text-base">
            更换词典 <i class="fas fa-exchange-alt ml-1"></i>
          </button>
        </div>
        
        
        <!-- AI助手：使用CustomButton -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden transform transition-all duration-300 hover:shadow-md">
          <div class="bg-gradient-to-r from-emerald-500 to-emerald-600 p-3 border-b border-gray-200">
            <h3 class="text-base font-semibold text-white flex items-center">
              <i class="fas fa-robot mr-2"></i> AI学习助手
            </h3>
          </div>
          
          <div class="p-4 flex flex-col items-center">
            <div class="w-20 h-20 bg-gradient-to-r from-emerald-100 to-emerald-200 rounded-full flex items-center justify-center mb-3 shadow-sm">
              <i class="fas fa-robot text-emerald-600 text-3xl"></i>
            </div>
            
            <p class="text-base text-gray-600 text-center mb-4 px-2">
              有任何语法或作文问题，随时问我哦~
            </p>
            
            <CustomButton 
              type="primary" 
              icon="fa-comments" 
              text="开始对话" 
              class="w-full py-2.5 rounded-lg transition-all shadow hover:shadow-md transform hover:-translate-y-0.5 text-base"
            />
          </div>
        </div>
      </aside>
    </main>
    
    <!-- 引入EndBar组件 -->
    <EndBar />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import NavBar from '@/components/common/NavBar.vue';
import FriendItem from '@/components/business/FriendItem.vue';
import EndBar from '@/components/common/EndBar.vue';
import CustomButton from '@/components/common/CustomButton.vue';

onMounted(() => {
  // 页面滚动时导航栏样式变化（保持原样）
  const header = document.querySelector('header');
  window.addEventListener('scroll', () => {
    if (window.scrollY > 10) {
      header.classList.add('shadow');
    } else {
      header.classList.remove('shadow');
    }
  });
});

// 底部 tab 狀態：預設選中好友
const activeTab = ref('friends');

const router = useRouter();

function gotoChat() {
  activeTab.value = 'chat';
  // push to chat route
  router.push({ name: 'Chat' }).catch(() => {});
}

function gotoHome() {
  activeTab.value = 'friends';
  router.push({ name: 'Home' }).catch(() => {});
}
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css');
@tailwind base;
@tailwind components;
@tailwind utilities;

/* 自定义滚动条样式（保持原样） */
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
  from { opacity: 0; }
  to { opacity: 1; }
}

.animate-fadeIn 
{
  animation: fadeIn 0.5s ease-in-out;
}
</style>