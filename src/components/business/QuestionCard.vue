<template>
  <div
    class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden transform transition-all duration-300 hover:shadow-md hover:-translate-y-1"
    :class="{ 'border-l-4 border-l-emerald-500': question.relatedCourse }"
  >
    <!-- 卡片头部 - 元数据 -->
    <div
      class="px-5 py-3 bg-gray-50 border-b border-gray-100 flex items-center justify-between flex-wrap gap-2"
    >
      <div class="flex items-center gap-2 flex-wrap">
        <!-- 题型标签 -->
        <span
          class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium"
          :class="getTypeClass(question.type)"
        >
          <i :class="['fas', getTypeIcon(question.type), 'mr-1']" />
          {{ getTypeName(question.type) }}
        </span>

        <!-- 难度标签 -->
        <span
          class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium"
          :class="getDifficultyClass(question.difficulty)"
        >
          {{ getDifficultyName(question.difficulty) }}
        </span>

        <!-- 课程关联标记 -->
        <span
          v-if="question.relatedCourse"
          class="inline-flex items-center text-xs text-gray-600"
        >
          <i class="fas fa-link mr-1 text-emerald-500" />
          关联课程：{{ getCourseName(question.relatedCourse) }}
          {{ question.relatedChapter }}
        </span>
      </div>

      <!-- 难度星级 -->
      <div class="flex items-center">
        <template
          v-for="i in 5"
          :key="i"
        >
          <i
            class="fas fa-star text-sm"
            :class="
              i <= getDifficultyStars(question.difficulty)
                ? 'text-yellow-400'
                : 'text-gray-300'
            "
          />
        </template>
      </div>
    </div>

    <!-- 卡片内容 - 题目预览 -->
    <div class="p-5">
      <!-- 标题 -->
      <h3 class="text-lg font-semibold text-gray-800 mb-2 line-clamp-1">
        {{ question.title }}
      </h3>

      <!-- 预览内容 -->
      <p class="text-gray-600 text-sm mb-3 line-clamp-2">
        <span v-html="highlightVocabulary(question.preview)" />
      </p>

      <!-- 标签和关键词 -->
      <div class="flex flex-wrap gap-1.5 mb-3">
        <span
          v-for="tag in question.tags.slice(0, 3)"
          :key="tag"
          class="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded"
        >
          #{{ tag }}
        </span>
        <!-- 高亮生词标记 -->
        <span
          v-if="hasVocabularyWord"
          class="inline-flex items-center px-2 py-0.5 bg-yellow-50 text-yellow-700 text-xs rounded"
        >
          <i class="fas fa-search mr-1" />
          包含生词
        </span>
      </div>
    </div>

    <!-- 卡片底部 - 操作区 -->
    <div
      class="px-5 py-3 bg-gray-50 border-t border-gray-100 flex items-center justify-between flex-wrap gap-2"
    >
      <!-- 状态反馈 -->
      <div class="flex items-center">
        <span
          v-if="question.status === 'done' && question.lastResult === 'correct'"
          class="inline-flex items-center text-sm text-green-600"
        >
          <i class="fas fa-check-circle mr-1" />
          正确
        </span>
        <span
          v-else-if="
            question.status === 'done' && question.lastResult === 'wrong'
          "
          class="inline-flex items-center text-sm text-red-500"
        >
          <i class="fas fa-times-circle mr-1" />
          上次错误 ({{ getRelativeTime(question.lastAttemptDate) }})
        </span>
        <span
          v-else
          class="inline-flex items-center text-sm text-gray-500"
        >
          <i class="far fa-circle mr-1" />
          未做
        </span>
      </div>

      <!-- 操作按钮 -->
      <div class="flex items-center gap-2">
        <!-- 加入计划 -->
        <button
          class="p-2 text-gray-500 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
          title="加入计划"
          @click.stop="$emit('add-to-plan', question)"
        >
          <i class="far fa-calendar-plus" />
        </button>

        <!-- 收藏 -->
        <button
          class="p-2 rounded-lg transition-colors"
          :class="
            question.favorited
              ? 'text-red-500 hover:text-red-600 hover:bg-red-50'
              : 'text-gray-500 hover:text-red-500 hover:bg-red-50'
          "
          title="收藏"
          @click.stop="$emit('toggle-favorite', question.id)"
        >
          <i :class="question.favorited ? 'fas fa-heart' : 'far fa-heart'" />
        </button>

        <!-- 开始练习按钮 -->
        <button
          class="px-4 py-2 bg-emerald-500 text-white text-sm font-medium rounded-lg hover:bg-emerald-600 transition-colors shadow-sm hover:shadow flex items-center"
          @click.stop="$emit('start-practice', question)"
        >
          <i class="fas fa-play mr-1.5" />
          开始练习
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import {
  QUESTION_TYPES,
  DIFFICULTY_LEVELS,
  COURSES,
  USER_VOCABULARY,
} from "@/utils/questionData.js";

const props = defineProps({
  question: {
    type: Object,
    required: true,
  },
});

defineEmits(["start-practice", "add-to-plan", "toggle-favorite"]);

// 获取题型名称
function getTypeName(type) {
  const typeObj = Object.values(QUESTION_TYPES).find((t) => t.id === type);
  return typeObj ? typeObj.name : type;
}

// 获取题型图标
function getTypeIcon(type) {
  const typeObj = Object.values(QUESTION_TYPES).find((t) => t.id === type);
  return typeObj ? typeObj.icon : "fa-question";
}

// 获取题型样式类
function getTypeClass(type) {
  const classes = {
    listening: "bg-blue-100 text-blue-800",
    reading: "bg-purple-100 text-purple-800",
    writing: "bg-green-100 text-green-800",
    grammar: "bg-orange-100 text-orange-800",
    vocabulary: "bg-pink-100 text-pink-800",
  };
  return classes[type] || "bg-gray-100 text-gray-800";
}

// 获取难度名称
function getDifficultyName(difficulty) {
  const diffObj = Object.values(DIFFICULTY_LEVELS).find(
    (d) => d.id === difficulty
  );
  return diffObj ? diffObj.name : difficulty;
}

// 获取难度星级
function getDifficultyStars(difficulty) {
  const diffObj = Object.values(DIFFICULTY_LEVELS).find(
    (d) => d.id === difficulty
  );
  return diffObj ? diffObj.stars : 1;
}

// 获取难度样式类
function getDifficultyClass(difficulty) {
  const classes = {
    beginner: "bg-green-100 text-green-800",
    "cet4-6": "bg-blue-100 text-blue-800",
    postgraduate: "bg-purple-100 text-purple-800",
    "toefl-ielts": "bg-orange-100 text-orange-800",
    professional: "bg-red-100 text-red-800",
  };
  return classes[difficulty] || "bg-gray-100 text-gray-800";
}

// 获取课程名称
function getCourseName(courseId) {
  const course = COURSES.find((c) => c.id === courseId);
  return course ? course.name : "";
}

// 检查是否包含生词
const hasVocabularyWord = computed(() => {
  return props.question.highlightWords.some((word) =>
    USER_VOCABULARY.some(
      (vocab) =>
        word.toLowerCase().includes(vocab.toLowerCase()) ||
        vocab.toLowerCase().includes(word.toLowerCase())
    )
  );
});

// 高亮生词
function highlightVocabulary(text) {
  let result = text;
  USER_VOCABULARY.forEach((vocab) => {
    const regex = new RegExp(`(${vocab})`, "gi");
    result = result.replace(
      regex,
      '<span class="bg-yellow-100 text-yellow-800 px-0.5 rounded">$1</span>'
    );
  });
  return result;
}

// 获取相对时间
function getRelativeTime(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  const now = new Date();
  const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return "今天";
  if (diffDays === 1) return "1天前";
  if (diffDays < 7) return `${diffDays}天前`;
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}周前`;
  return `${Math.floor(diffDays / 30)}月前`;
}
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
