<template>
  <header class="bg-white border-b border-gray-200 shadow-sm transition-all duration-300">
    <div class="flex items-center justify-between h-20 px-4">
      <!-- 左侧Logo和导航链接 -->
      <div class="flex items-center space-x-6">
        <!-- Logo -->
        <div
          class="flex items-center"
          @click="gotoHome"
        >
          <i class="fas fa-language text-emerald-600 text-3xl mr-3" />
          <span class="text-2xl font-bold text-gray-800">英语学习平台</span>
        </div>

        <!-- 导航链接 -->
        <nav class="flex space-x-2 ml-10">
          <button
            v-for="(item, index) in navItems"
            :key="index"
            class="px-4 py-3 text-sm font-medium rounded-md transition-colors"
            :class="item.isActive 
              ? 'text-emerald-600 bg-emerald-50' 
              : 'text-gray-600 hover:text-emerald-600 hover:bg-emerald-50'"
            @click="handleNavClick(item)"
          >
            {{ item.label }}
          </button>
        </nav>
      </div>

      <!-- 右侧用户操作区 -->
      <div class="flex items-center space-x-1">
        <!-- 插槽：用于自定义右侧操作按钮 -->
        <slot name="actions" />

        <!-- 用户头像和下拉菜单 -->
        <div class="relative ml-4">
          <button 
            class="flex items-center justify-center w-10 h-10 rounded-full overflow-hidden border-2 border-transparent hover:border-emerald-500 transition-colors"
            @click="toggleDropdown"
          >
            <img 
              :src="userStore.userInfo.avatar" 
              alt="用户头像" 
              class="w-full h-full object-cover"
            >
          </button>
          
          <!-- 下拉菜单 -->
          <div 
            v-if="showDropdown"
            ref="dropdownRef"
            class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50 animate-fadeIn"
          >
            <div class="px-5 py-4 border-b border-gray-100">
              <div class="flex items-center">
                <img 
                  :src="userStore.userInfo.avatar" 
                  alt="用户头像" 
                  class="w-12 h-12 rounded-full mr-4 object-cover"
                >
                <div>
                  <p class="text-base font-medium text-gray-800">
                    {{ userStore.userInfo.name }}
                  </p>
                  <p class="text-sm text-gray-500">
                    {{ userStore.userInfo.id }}
                  </p> <!-- 用户ID -->
                </div>
              </div>
            </div>  
            <button 
              class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
              @click="gotoProfile"
            >
              <i class="fas fa-user mr-3" />个人主页
            </button>
            <button 
              class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
            >
              <i class="fas fa-comment mr-2" />设置最近状态
            </button>
            <div class="border-t border-gray-100 my-1" />
            <button 
              class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
              @click="gotoSettings"
            >
              <i class="fas fa-cog mr-2" />设置
            </button>
            <div class="border-t border-gray-100 my-1" />
            <button 
              class="w-full text-left px-4 py-2 text-sm text-green-600 hover:bg-green-50 transition-colors"
              @click="gotoLogin"
            >
              <i class="fas fa-right-to-bracket mr-2" />登录
            </button>
            <button 
              class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
              @click="gotoLogout"
            >
              <i class="fas fa-sign-out-alt mr-2" />登出
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watchEffect } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user'; // 引入Pinia用户状态

// 接收父组件传递的导航项
const props = defineProps({
  navItems: {
    type: Array,
    default: () => []
  }
});

const router = useRouter();
const showDropdown = ref(false);
const dropdownRef = ref(null);
const userStore = useUserStore(); // 注入用户状态

const gotoHome = () => {
  router.push('/').catch(() => {});
  showDropdown.value = false; // 关闭下拉菜单（如果打开）
};

// 切换下拉菜单显示状态
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

// 跳转到个人主页
const gotoProfile = () => {
  showDropdown.value = false;
  router.push({ name: 'Profile' }).catch(() => {});
};

// 跳转到登录
const gotoLogin = () => {
  showDropdown.value = false;
  router.push({ name: 'Login' }).catch(() => {});
};

// 跳转到登出
const gotoLogout = () => {
  // showDropdown.value = false;
  // router.push({ name: 'Logout' }).catch(() => {});
};

// 跳转到设置
const gotoSettings = () => {
  showDropdown.value = false;
  router.push({ name: 'Settings' }).catch(() => {});
};

// 处理导航项点击
const handleNavClick = (item) => {
  if (item.onClick) {
    item.onClick();
  } else if (item.path && item.path !== '#') {
    router.push(item.path).catch(() => {});
  }
  // 关闭下拉菜单（如果打开）
  showDropdown.value = false;
};

// 点击外部关闭下拉菜单
const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target) &&
      !event.target.closest('.rounded-full.overflow-hidden')) {
    showDropdown.value = false;
  }
};

// 监听滚动事件，改变导航栏样式
const handleScroll = () => {
  const header = document.querySelector('header');
  if (header) {
    if (window.scrollY > 10) {
      header.classList.add('shadow');
    } else {
      header.classList.remove('shadow');
    }
  }
};

// 组件挂载时添加事件监听
onMounted(() => {
  document.addEventListener('click', handleClickOutside);
  window.addEventListener('scroll', handleScroll);
  
  // 初始检查滚动位置
  handleScroll();
});

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
  window.removeEventListener('scroll', handleScroll);
});

// 监听导航项变化，确保正确高亮当前页
watchEffect(() => {
  // 可以在这里添加路由与导航项的同步逻辑
});
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-5px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fadeIn {
  animation: fadeIn 0.2s ease-out;
}
</style>