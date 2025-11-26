<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    @click.self="closePanel"
  >
    <div
      class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[85vh] flex flex-col animate-slideIn"
      @click.stop
    >
      <!-- 头部 -->
      <div
        class="px-6 py-4 border-b border-gray-200 flex items-center justify-between flex-shrink-0"
      >
        <h2 class="text-xl font-bold text-gray-800 flex items-center">
          <i class="fas fa-times-circle text-red-500 mr-2" />
          错题本
          <span class="ml-2 text-sm font-normal text-gray-500">
            ({{ wrongQuestions.length }} 道错题)
          </span>
        </h2>
        <button
          class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-colors"
          @click="closePanel"
        >
          <i class="fas fa-times text-xl" />
        </button>
      </div>

      <!-- 统计信息 -->
      <div
        class="px-6 py-4 bg-gradient-to-r from-red-50 to-orange-50 border-b border-gray-100"
      >
        <div class="grid grid-cols-3 gap-4 text-center">
          <div>
            <div class="text-2xl font-bold text-red-600">
              {{ wrongQuestions.length }}
            </div>
            <div class="text-xs text-gray-600">
              累计错题
            </div>
          </div>
          <div>
            <div class="text-2xl font-bold text-orange-600">
              {{ recentWrongCount }}
            </div>
            <div class="text-xs text-gray-600">
              近7天错题
            </div>
          </div>
          <div>
            <div class="text-2xl font-bold text-emerald-600">
              {{ masteredCount }}
            </div>
            <div class="text-xs text-gray-600">
              已掌握
            </div>
          </div>
        </div>
      </div>

      <!-- 筛选条件 -->
      <div
        class="px-6 py-3 border-b border-gray-100 flex items-center gap-3 flex-wrap"
      >
        <span class="text-sm text-gray-600">筛选：</span>
        <button
          v-for="filter in filters"
          :key="filter.value"
          class="px-3 py-1.5 text-sm rounded-full transition-colors"
          :class="
            activeFilter === filter.value
              ? 'bg-red-500 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          "
          @click="activeFilter = filter.value"
        >
          {{ filter.label }}
        </button>
      </div>

      <!-- 错题列表 -->
      <div class="flex-grow overflow-y-auto p-4">
        <div
          v-if="filteredWrongQuestions.length === 0"
          class="text-center py-12"
        >
          <i class="fas fa-check-circle text-emerald-400 text-6xl mb-4" />
          <p class="text-gray-500 text-lg">
            太棒了！没有错题
          </p>
          <p class="text-gray-400 text-sm mt-2">
            继续保持，你做得很好！
          </p>
        </div>

        <div
          v-else
          class="space-y-3"
        >
          <div
            v-for="question in filteredWrongQuestions"
            :key="question.id"
            class="p-4 bg-white border border-gray-200 rounded-xl hover:shadow-md transition-all cursor-pointer group"
            @click="goToQuestion(question)"
          >
            <div class="flex items-start justify-between">
              <div class="flex-grow min-w-0">
                <!-- 题型和难度标签 -->
                <div class="flex items-center gap-2 mb-2">
                  <span
                    class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium"
                    :class="getTypeClass(question.type)"
                  >
                    <i :class="['fas', getTypeIcon(question.type), 'mr-1']" />
                    {{ getTypeName(question.type) }}
                  </span>
                  <span
                    class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium"
                    :class="getDifficultyClass(question.difficulty)"
                  >
                    {{ getDifficultyName(question.difficulty) }}
                  </span>
                  <span class="text-xs text-gray-500">
                    <i class="far fa-clock mr-1" />
                    {{ getRelativeTime(question.lastAttemptDate) }}
                  </span>
                </div>

                <!-- 题目标题 -->
                <h4
                  class="font-medium text-gray-800 mb-1 line-clamp-1 group-hover:text-red-600 transition-colors"
                >
                  {{ question.title }}
                </h4>

                <!-- 题目预览 -->
                <p class="text-sm text-gray-500 line-clamp-1">
                  {{ question.preview }}
                </p>

                <!-- 错题分析标签 -->
                <div class="flex items-center gap-2 mt-2">
                  <span
                    v-for="tag in question.tags.slice(0, 2)"
                    :key="tag"
                    class="px-2 py-0.5 bg-red-50 text-red-600 text-xs rounded"
                  >
                    {{ tag }}
                  </span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div
                class="flex items-center gap-2 ml-3 opacity-0 group-hover:opacity-100 transition-opacity"
              >
                <button
                  class="p-2 text-emerald-500 hover:text-emerald-700 hover:bg-emerald-50 rounded-lg transition-colors"
                  title="重新练习"
                  @click.stop="$emit('retry', question)"
                >
                  <i class="fas fa-redo" />
                </button>
                <button
                  class="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                  title="标记为已掌握"
                  @click.stop="markAsMastered(question.id)"
                >
                  <i class="fas fa-check" />
                </button>
              </div>
            </div>

            <!-- 错误次数指示 -->
            <div
              class="mt-3 pt-3 border-t border-gray-100 flex items-center justify-between"
            >
              <div class="flex items-center text-sm text-red-500">
                <i class="fas fa-exclamation-triangle mr-1.5" />
                错误 {{ question.wrongCount || 1 }} 次
              </div>
              <span class="text-sm text-emerald-600 flex items-center">
                点击重做
                <i class="fas fa-arrow-right ml-1" />
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div
        class="px-6 py-4 border-t border-gray-200 flex items-center justify-between flex-shrink-0"
      >
        <button
          class="px-4 py-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors"
          @click="clearAllMastered"
        >
          <i class="fas fa-broom mr-1.5" />
          清空已掌握
        </button>
        <button
          class="px-5 py-2.5 bg-red-500 text-white font-medium rounded-lg hover:bg-red-600 transition-colors shadow-sm flex items-center"
          :disabled="filteredWrongQuestions.length === 0"
          @click="startReviewAll"
        >
          <i class="fas fa-play mr-1.5" />
          全部重做 ({{ filteredWrongQuestions.length }})
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { QUESTION_TYPES, DIFFICULTY_LEVELS } from "@/utils/questionData.js";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  wrongQuestions: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["close", "go-to-question", "retry", "review-all"]);

// 筛选条件
const filters = [
  { label: "全部", value: "all" },
  { label: "听力", value: "listening" },
  { label: "阅读", value: "reading" },
  { label: "语法", value: "grammar" },
  { label: "词汇", value: "vocabulary" },
];

const activeFilter = ref("all");
const masteredIds = ref([]);

// 计算属性
const filteredWrongQuestions = computed(() => {
  let result = props.wrongQuestions.filter(
    (q) => !masteredIds.value.includes(q.id)
  );

  if (activeFilter.value !== "all") {
    result = result.filter((q) => q.type === activeFilter.value);
  }

  return result;
});

const recentWrongCount = computed(() => {
  const sevenDaysAgo = new Date();
  sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

  return props.wrongQuestions.filter((q) => {
    if (!q.lastAttemptDate) return false;
    return new Date(q.lastAttemptDate) >= sevenDaysAgo;
  }).length;
});

const masteredCount = computed(() => {
  return masteredIds.value.length;
});

// 获取题型相关信息
function getTypeName(type) {
  const typeObj = Object.values(QUESTION_TYPES).find((t) => t.id === type);
  return typeObj ? typeObj.name : type;
}

function getTypeIcon(type) {
  const typeObj = Object.values(QUESTION_TYPES).find((t) => t.id === type);
  return typeObj ? typeObj.icon : "fa-question";
}

function getTypeClass(type) {
  const classes = {
    listening: "bg-blue-100 text-blue-700",
    reading: "bg-purple-100 text-purple-700",
    writing: "bg-green-100 text-green-700",
    grammar: "bg-orange-100 text-orange-700",
    vocabulary: "bg-pink-100 text-pink-700",
  };
  return classes[type] || "bg-gray-100 text-gray-700";
}

function getDifficultyName(difficulty) {
  const diffObj = Object.values(DIFFICULTY_LEVELS).find(
    (d) => d.id === difficulty
  );
  return diffObj ? diffObj.name : difficulty;
}

function getDifficultyClass(difficulty) {
  const classes = {
    beginner: "bg-green-100 text-green-700",
    "cet4-6": "bg-blue-100 text-blue-700",
    postgraduate: "bg-purple-100 text-purple-700",
    "toefl-ielts": "bg-orange-100 text-orange-700",
    professional: "bg-red-100 text-red-700",
  };
  return classes[difficulty] || "bg-gray-100 text-gray-700";
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

// 跳转到题目
function goToQuestion(question) {
  emit("go-to-question", question);
}

// 标记为已掌握
function markAsMastered(questionId) {
  if (!masteredIds.value.includes(questionId)) {
    masteredIds.value.push(questionId);
  }
}

// 清空已掌握
function clearAllMastered() {
  if (confirm('确定要清空所有"已掌握"标记吗？')) {
    masteredIds.value = [];
  }
}

// 全部重做
function startReviewAll() {
  emit("review-all", filteredWrongQuestions.value);
}

// 关闭面板
function closePanel() {
  emit("close");
}
</script>

<style scoped>
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.animate-slideIn {
  animation: slideIn 0.3s ease-out;
}

.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #e5e5e5;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #d0d0d0;
}
</style>
