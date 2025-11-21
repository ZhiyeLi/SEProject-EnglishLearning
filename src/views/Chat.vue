<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 保留NavBar组件 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <button class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group">
          <i class="fas fa-lightbulb text-lg" />
          <span class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">学习建议</span>
        </button>
        <button class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group">
          <i class="fas fa-cog text-lg" />
          <span class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">设置</span>
        </button>
        <button class="relative ml-2 text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors">
          <i class="fas fa-bell text-lg" />
          <span class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center animate-pulse">3</span>
        </button>
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
              <FriendItem 
                name="示例好友" 
                avatar="https://picsum.photos/seed/friend1/100/100" 
                status="online" 
                extra-info="正在输入..."
                class="bg-emerald-50 border-l-4 border-emerald-500"
              >
                <template #actions>
                  <button class="text-gray-600 hover:text-emerald-600 p-1 rounded-full hover:bg-emerald-50 transition-colors">
                    <i class="fas fa-comment" />
                  </button>
                </template>
              </FriendItem>
              <li>
                <button 
                  class="w-full flex items-center justify-center p-3 text-emerald-600 text-sm border border-dashed border-emerald-200 rounded-lg hover:bg-emerald-50 transition-all hover:border-emerald-300 group"
                  @click="handleAddFriend"
                >
                  <i class="fas fa-plus-circle mr-2 group-hover:scale-110 transition-transform" />
                  可添加更多好友
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
              <i class="fas fa-users text-xl mb-1" />
              <span class="text-sm">好友</span>
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
      <div class="flex-grow flex flex-col bg-gray-100 overflow-hidden">
        <!-- 聊天头部 -->
        <div class="bg-white border-b border-gray-200 p-4 flex items-center shadow-sm">
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
        >
          <!-- 对方消息 -->
          <div class="flex items-start mb-4 animate-fadeIn">
            <img 
              src="https://picsum.photos/seed/friend1/100/100" 
              alt="示例好友" 
              class="w-8 h-8 rounded-full object-cover mr-2 mt-1 flex-shrink-0"
            >
            <div class="max-w-[70%]">
              <div class="bg-white p-3 rounded-lg rounded-tl-none shadow-sm">
                <p class="text-gray-800">
                  测试聊天信息
                </p>
              </div>
            </div>
          </div>
          
          <!-- 我的消息 -->
          <div class="flex items-start justify-end mb-4 animate-fadeIn">
            <div class="max-w-[70%]">
              <div class="bg-emerald-500 text-white p-3 rounded-lg rounded-tr-none shadow-sm">
                <p>测试聊天信息</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 消息输入区域 -->
        <div class="bg-white border-t border-gray-200 p-3">
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
          
          <div class="flex items-end">
            <textarea 
              v-model="message"
              placeholder="输入消息..." 
              class="flex-grow border border-gray-200 rounded-lg rounded-tr-none p-3 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all resize-none max-h-40"
              @keydown.enter.exact.prevent="sendMessage"
            />
            <button 
              class="bg-emerald-500 hover:bg-emerald-600 text-white px-4 py-3 rounded-lg rounded-tl-none font-medium transition-all shadow-sm hover:shadow transform hover:-translate-y-0.5 ml-1"
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
  </div>
</template>

<script setup>
// 引入路由
import { useRouter } from 'vue-router';
import { onMounted, ref, watch } from 'vue';
import NavBar from '@/components/common/NavBar.vue';
import FriendItem from '@/components/business/FriendItem.vue';
import EndBar from '@/components/common/EndBar.vue';

// 初始化路由
const router = useRouter();

// 聊天相关数据
const message = ref('');
const chatContainer = ref(null);

// 底部tab状态：只定义一次
const activeTab = ref('chat');

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

const navItems = [
  { label: '首页', onClick: gotoHome, isActive: false },
  { label: '课程', path: '#' },
  { label: '题库', path: '#' },
  { label: '时间表', path: '#' },
  { label: '单词打卡', path: '#' },
  { label: 'AI伴学', onClick: gotoAiChat }
];

// 发送消息
const sendMessage = () => {
  if (message.value.trim()) {
    // 这里可以添加发送消息的逻辑
    console.log('发送消息:', message.value);
    // 清空输入框
    message.value = '';
    // 滚动到底部
    scrollToBottom();
  }
};

// 滚动到聊天底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

// 监听消息变化，自动滚动到底部
watch(message, () => {
  scrollToBottom();
});

onMounted(() => {
  // 页面滚动时导航栏样式变化
  const header = document.querySelector('header');
  window.addEventListener('scroll', () => {
    if (window.scrollY > 10) {
      header?.classList.add('shadow');
    } else {
      header?.classList.remove('shadow');
    }
  });
  
  // 初始滚动到底部
  scrollToBottom();
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