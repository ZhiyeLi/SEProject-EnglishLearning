<template>
  <div class="flex items-center space-x-1 relative z-10">
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

    <!-- 提醒按钮 -->
    <button
      class="relative ml-2 text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors group"
      aria-label="查看通知（3条未读）"
      @click="$emit('notifications')"
    >
      <i
        class="fas fa-bell text-lg"
        aria-hidden="true"
      />
      <span
        class="absolute -bottom-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap"
      >提醒</span>
      <span
        class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center animate-pulse"
      >3</span>
    </button>
  </div>
</template>

<script setup>
// 该组件通过emit事件来与父组件通信，使用起来更加灵活
defineEmits(['suggestions', 'settings', 'home', 'notifications']);
</script>

<style scoped>
/* 可以在这里添加额外的样式 */
</style>
