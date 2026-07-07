<template>
  <teleport to="body">
    <div
      v-if="visible"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fadeIn"
      @click="handleBackdrop"
    >
      <div
        class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl mx-4 max-h-[70vh] overflow-hidden transform transition-all"
        @click.stop
      >
        <!-- 头部 -->
        <div class="px-8 py-6 border-b border-gray-200 bg-gradient-to-r from-emerald-50 to-blue-50">
          <div class="flex justify-between items-center">
            <h2 class="text-2xl font-bold text-gray-900 flex items-center">
              <i class="fas fa-lightbulb text-yellow-500 mr-3" />
              {{ title }}
            </h2>
            <button class="text-gray-400 hover:text-gray-600 transition-colors" @click="close">
              <i class="fas fa-times text-2xl" />
            </button>
          </div>
        </div>

        <!-- 内容 -->
        <div class="px-8 py-6 overflow-y-auto" style="max-height: calc(70vh - 140px)">
          <div class="space-y-4">
            <div>
              <h3 class="text-lg font-semibold text-gray-800 mb-3">
                <span class="text-emerald-600">{{ current.title }}</span>
              </h3>
              <p class="text-gray-700 leading-relaxed whitespace-pre-wrap">{{ current.content }}</p>
            </div>
            <div class="flex flex-wrap gap-2 pt-4">
              <span
                v-for="tag in current.tags"
                :key="tag"
                class="px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-sm"
              >{{ tag }}</span>
            </div>
          </div>
        </div>

        <!-- 底部翻页 -->
        <div class="px-8 py-4 border-t border-gray-200 bg-gray-50 flex justify-between items-center">
          <button
            :disabled="index === 0"
            class="px-6 py-2 rounded-lg font-medium transition-all"
            :class="index === 0 ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'"
            @click="prev"
          ><i class="fas fa-chevron-left mr-2" />上一条</button>
          <div class="text-gray-600 font-medium">{{ index + 1 }} / {{ items.length }}</div>
          <button
            :disabled="index === items.length - 1"
            class="px-6 py-2 rounded-lg font-medium transition-all"
            :class="index === items.length - 1 ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'"
            @click="next"
          >下一条<i class="fas fa-chevron-right ml-2" /></button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, computed, watch } from "vue";

const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: "学习建议" },
  items: { type: Array, default: () => [] },
});

const emit = defineEmits(["close"]);

const index = ref(0);
const current = computed(() => props.items[index.value] || {});

watch(() => props.visible, (v) => {
  if (v) index.value = 0;
});

function prev() { if (index.value > 0) index.value--; }
function next() { if (index.value < props.items.length - 1) index.value++; }
function close() { emit("close"); }
function handleBackdrop() { close(); }
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fadeIn { animation: fadeIn 0.3s ease-out; }
</style>
