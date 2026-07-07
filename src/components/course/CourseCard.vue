<template>
  <div
    class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden transform transition-all duration-300 hover:shadow-md hover:-translate-y-1 cursor-pointer"
    @click="$emit('click', course.id)"
  >
    <div class="flex flex-col md:flex-row">
      <!-- 封面图 -->
      <div class="w-full md:w-1/3 bg-gray-100 flex items-center justify-center p-2 relative">
        <div class="relative w-full aspect-video md:aspect-auto md:h-full">
          <img
            :src="coverSrc"
            :alt="course.name"
            referrerpolicy="no-referrer"
            class="absolute inset-0 w-full h-full object-cover object-center"
            @error="onCoverError"
          />
          <!-- 进度角标 -->
          <span
            v-if="course.status === 'completed'"
            class="absolute top-2 left-2 bg-green-500 text-white text-xs px-2 py-0.5 rounded-full"
          >✓ 已学完</span>
          <span
            v-else-if="course.status === 'learning'"
            class="absolute top-2 left-2 bg-emerald-400 text-white text-xs px-2 py-0.5 rounded-full"
          >● 学习中</span>
        </div>
      </div>

      <!-- 信息区 -->
      <div class="w-full md:w-2/3 p-5 flex flex-col justify-center">
        <!-- 标签 -->
        <div class="mb-2">
          <span class="inline-block px-2 py-1 text-xs font-medium rounded-full"
            :class="tagClass"
          >{{ tagLabel }}</span>
        </div>

        <!-- 标题 -->
        <h3 class="text-xl font-semibold text-gray-800 mb-2 line-clamp-1">
          <span v-html="highlightText(course.name)" />
        </h3>

        <!-- 简介 -->
        <p class="text-gray-600 text-base mb-4 flex-grow line-clamp-2">
          <span v-html="highlightText(course.description)" />
        </p>

        <!-- 收藏按钮 -->
        <button
          class="self-start text-lg transition-colors"
          :class="course.favorite ? 'text-yellow-500' : 'text-gray-300 hover:text-yellow-400'"
          @click.stop="$emit('favorite', course.id)"
          :title="course.favorite ? '取消收藏' : '收藏'"
        >★</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  course: { type: Object, required: true },
  searchQuery: { type: String, default: "" },
});

defineEmits(["click", "favorite"]);

const TAG_MAP = {
  primary: { label: "小学", class: "bg-blue-100 text-blue-800" },
  middle: { label: "中学", class: "bg-purple-100 text-purple-800" },
  college: { label: "大学", class: "bg-green-100 text-green-800" },
  none: { label: "零基础", class: "bg-orange-100 text-orange-800" },
};

const tagLabel = computed(() => TAG_MAP[props.course.tag]?.label || "");
const tagClass = computed(() => TAG_MAP[props.course.tag]?.class || "bg-gray-100 text-gray-800");

const coverSrc = computed(() => {
  if (props.course.coverImage) return props.course.coverImage;
  return "";
});

function onCoverError(e) {
  e.target.style.display = "none";
}

function escapeHtml(text) {
  if (!text) return "";
  return text.replace(/[&<>"']/g, (m) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[m]);
}

function highlightText(text) {
  if (!text) return "";
  const safe = escapeHtml(text);
  const q = props.searchQuery.trim();
  if (!q) return safe;
  const regex = new RegExp(`(${q.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")})`, "gi");
  return safe.replace(regex, '<span class="bg-yellow-100 text-yellow-800 px-1 rounded">$1</span>');
}
</script>
