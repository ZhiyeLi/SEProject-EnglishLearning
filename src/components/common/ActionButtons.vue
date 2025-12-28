<template>
  <div class="flex items-center space-x-1" ref="root">
    <!-- 学习建议按钮 -->
    <button
      class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group"
      aria-label="学习建议"
      @click="$emit('suggestions')"
    >
      <i
        class="fas fa-lightbulb text-lg"
        aria-hidden="true"
      />
      <span
        class="absolute -bottom-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap"
      >学习建议</span>
    </button>

    <!-- 设置按钮 -->
    <button
      class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group"
      aria-label="设置"
      @click="$emit('settings')"
    >
      <i
        class="fas fa-cog text-lg"
        aria-hidden="true"
      />
      <span
        class="absolute -bottom-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap"
      >设置</span>
    </button>

    <!-- 返回首页按钮 -->
    <button
      class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group"
      aria-label="返回首页"
      @click="$emit('home')"
    >
      <i
        class="fas fa-home text-lg"
        aria-hidden="true"
      />
      <span
        class="absolute -bottom-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap"
      >返回首页</span>
    </button>

    <!-- 提醒按钮（显示未读数，点击显示小弹出层） -->
    <div class="relative ml-2">
      <button
        class="relative text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors group"
        :aria-label="`查看通知（${totalUnread} 条未读）`"
        @click="showBadge = !showBadge"
      >
        <i class="fas fa-bell text-lg" aria-hidden="true" />
        <span class="absolute -bottom-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">提醒</span>
         <span 
          v-if="totalUnread > 0" 
          class="absolute top-1 right-1 bg-red-500 text-white text-[10px] rounded-full h-3 w-3 flex items-center justify-center animate-pulse"
        >{{ totalUnread.value > 99 ? '99+' : totalUnread.value }}</span>
      </button>

      <!-- 简单弹出显示总未读数 -->
      <div v-if="showBadge" class="absolute right-0 mt-2 w-44 bg-white border border-gray-200 rounded-lg shadow-lg z-50 px-3 py-2 text-sm text-gray-700">
        未读消息总数： <span class="font-medium text-emerald-600">{{ totalUnread }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { friendApi } from '@/api/friend';

// 该组件通过emit事件来与父组件通信，使用起来更加灵活
const emit = defineEmits(['suggestions', 'settings', 'home', 'notifications']);

const totalUnread = ref(0);

const showBadge = ref(false);

// 如需父視圖感知點擊，可在 template 中改為 @click="(showBadge = !showBadge, emit('notifications'))"

const fetchUnread = async () => {
  try {
    const res = await friendApi.getUnreadCount();
    if (res?.code === 200) {
      if (Array.isArray(res.data)) {
        totalUnread.value = res.data.reduce((s, i) => s + (Number(i.count) || 0), 0);
      } else if (res.data && typeof res.data === 'object') {
        totalUnread.value = Object.values(res.data).reduce((s, v) => s + (Number(v) || 0), 0);
      } else {
        totalUnread.value = 0;
      }
    }
  } catch (err) {
    console.error('获取未读消息数失败：', err);
    totalUnread.value = 0;
  }
};

onMounted(() => {
  fetchUnread();
});
</script>

<style scoped>
/* 可以在这里添加额外的样式 */
</style>
