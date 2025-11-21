<template>
  <header
    class="bg-white border-b border-gray-200 shadow-sm sticky top-0 z-30 transition-all duration-300"
    :class="{ 'shadow': isScrolled }"
  >
    <div class="container mx-auto px-2">
      <div class="flex items-center justify-between py-3">
        <div class="flex items-center">
          <!-- 用户信息（可配置是否显示） -->
          <div
            v-if="showUserInfo"
            class="flex items-center mr-8"
          >
            <div class="w-12 h-12 rounded-full bg-gradient-to-r from-emerald-400 to-emerald-600 flex items-center justify-center text-white font-bold text-xl shadow-md hover:shadow-lg transition-shadow">
              {{ userAvatarText }}
            </div>
            <div class="ml-3">
              <div class="bg-emerald-50 px-2 py-0.5 rounded text-base text-emerald-700">
                {{ userName }}
              </div>
              <div class="text-base text-gray-600">
                {{ userTag }}
              </div>
            </div>
          </div>
          
          <!-- 导航菜单（可通过props自定义） -->
          <nav class="hidden md:flex space-x-1">
            <template
              v-for="(item, index) in navItems"
              :key="index"
            >
              <button 
                v-if="item.onClick"
                class="font-medium px-4 py-2 border-b-2 rounded-t-md transition-colors text-base"
                :class="item.isActive ? 'text-emerald-600 border-emerald-600' : 'text-gray-600 hover:text-emerald-600 border-transparent hover:border-emerald-300'" 
                @click="item.onClick"
              >
                {{ item.label }}
              </button>
              <a 
                v-else
                :href="item.path" 
                class="font-medium px-4 py-2 border-b-2 rounded-t-md transition-colors text-base" 
                :class="item.isActive ? 'text-emerald-600 border-emerald-600' : 'text-gray-600 hover:text-emerald-600 border-transparent hover:border-emerald-300'"
              >
                {{ item.label }}
              </a>
            </template>
          </nav>
        </div>
        
        <!-- 右侧功能按钮（通过插槽自定义） -->
        <div class="flex items-center space-x-2">
          <slot name="actions" />
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const props = defineProps({
  showUserInfo: {
    type: Boolean,
    default: true
  },
  userName: {
    type: String,
    default: 'ID_English'
  },
  userAvatarText: {
    type: String,
    default: 'A'
  },
  userTag: {
    type: String,
    default: '每天进步一点点 ✨'
  },
  navItems: {
    type: Array,
    default: () => [
      { label: '首页', path: '#', isActive: true },
      { label: '课程', path: '#' },
      { label: '题库', path: '#' },
      { label: '时间表', path: '#' },
      { label: '单词打卡', path: '#' }
    ]
  }
});

const isScrolled = ref(false);
onMounted(() => {
  window.addEventListener('scroll', () => {
    isScrolled.value = window.scrollY > 10;
  });
});
</script>