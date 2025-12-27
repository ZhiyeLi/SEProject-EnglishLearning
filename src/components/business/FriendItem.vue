<template>
  <li class="flex items-center p-3 hover:bg-emerald-50 rounded-lg transition-colors cursor-pointer relative">
    <div class="relative">
      <img
        :src="avatar"
        alt="好友头像"
        class="w-10 h-10 rounded-full object-cover border-2"
        :class="status === 'online' ? 'border-emerald-400' : 'border-gray-200'"
      >
      <div
        class="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full border-2 border-white"
        :class="status === 'online' ? 'bg-emerald-500' : 'bg-gray-300'"
      />
    </div>
    <div class="ml-3 flex-grow">
      <div class="font-medium text-gray-800">
        {{ name }}
      </div>
      <div class="text-sm text-gray-500">
        {{ extraInfo }}
      </div>
    </div>
    <slot name="actions" />
    <!-- 默认插槽，可放置小红点等内容 -->
    <slot />
  </li>
</template>

<script setup>
defineProps({
  name: {
    type: String,
    required: true
  },
  avatar: {
    type: String,
    default: 'https://picsum.photos/seed/friend/100/100'
  },
  status: {
    type: String,
    default: 'online',
    validator: (val) => ['online', 'offline'].includes(val)
  },
  extraInfo: {
    type: String,
    default: '在线'
  }
});
</script>