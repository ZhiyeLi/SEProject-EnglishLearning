<template>
  <button
    class="font-medium rounded-lg transition-all duration-200 flex items-center justify-center shadow-sm hover:shadow focus:outline-none focus:ring-2 focus:ring-offset-2"
    :class="[
      type === 'primary' ? 'bg-emerald-500 text-white hover:bg-emerald-600 focus:ring-emerald-500' : 
      type === 'secondary' ? 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 focus:ring-emerald-300' : 
      'bg-white text-gray-700 border border-gray-200 hover:bg-gray-50 focus:ring-gray-300',
      size === 'large' ? 'px-6 py-3 text-base' : 
      size === 'small' ? 'px-3 py-1 text-sm' : 
      'px-4 py-2 text-base',
      customClass
    ]"
    :disabled="disabled"
    @click="emit('click')"
  >
    <i
      v-if="icon"
      :class="['fas', icon, 'mr-2']"
    />
    <slot>{{ text }}</slot>
  </button>
</template>

<script setup>
const props = defineProps({
  type: {
    type: String,
    default: 'primary',
    validator: (val) => ['primary', 'secondary', 'default'].includes(val)
  },
  size: {
    type: String,
    default: 'medium',
    validator: (val) => ['large', 'medium', 'small'].includes(val)
  },
  icon: {
    type: String,
    default: ''
  },
  text: {
    type: String,
    default: '按钮'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  customClass: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['click']);
</script>