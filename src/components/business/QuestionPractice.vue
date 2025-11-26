<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    @click.self="closeModal"
  >
    <div
      class="bg-white rounded-2xl shadow-2xl max-w-5xl w-full max-h-[90vh] flex flex-col animate-fadeIn"
      @click.stop
    >
      <!-- 模态框头部 -->
      <div
        class="px-6 py-4 border-b border-gray-200 flex items-center justify-between flex-shrink-0"
      >
        <div class="flex items-center gap-3">
          <span
            class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium"
            :class="getTypeClass(question.type)"
          >
            <i :class="['fas', getTypeIcon(question.type), 'mr-1.5']" />
            {{ getTypeName(question.type) }}
          </span>
          <span
            class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium"
            :class="getDifficultyClass(question.difficulty)"
          >
            {{ getDifficultyName(question.difficulty) }}
          </span>
        </div>
        <button
          class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-colors"
          @click="closeModal"
        >
          <i class="fas fa-times text-xl" />
        </button>
      </div>

      <!-- 模态框内容 - 分屏设计 -->
      <div class="flex-grow overflow-hidden flex flex-col md:flex-row">
        <!-- 左侧：内容区 -->
        <div
          class="w-full md:w-1/2 border-r border-gray-200 overflow-y-auto p-6"
          @mouseup="handleTextSelection"
        >
          <h2 class="text-xl font-bold text-gray-800 mb-4">
            {{ question.title }}
          </h2>

          <!-- 听力播放器 -->
          <div
            v-if="question.type === 'listening'"
            class="mb-4 p-4 bg-blue-50 rounded-lg border border-blue-200"
          >
            <div class="flex items-center gap-3 mb-3">
              <button
                class="w-12 h-12 rounded-full bg-blue-500 text-white flex items-center justify-center hover:bg-blue-600 transition-colors shadow-md"
                @click="toggleAudio"
              >
                <i
                  :class="[
                    'fas',
                    isPlaying ? 'fa-pause' : 'fa-play',
                    'text-lg',
                  ]"
                />
              </button>
              <div class="flex-grow">
                <div class="text-sm text-blue-700 font-medium mb-1">
                  音频文件
                </div>
                <div class="w-full bg-blue-200 rounded-full h-2">
                  <div
                    class="bg-blue-500 h-2 rounded-full transition-all duration-300"
                    :style="{ width: audioProgress + '%' }"
                  />
                </div>
              </div>
              <span class="text-sm text-blue-600">{{ formatTime(currentTime) }} /
                {{ formatTime(duration) }}</span>
            </div>
            <p class="text-sm text-blue-600">
              <i class="fas fa-info-circle mr-1" />
              点击播放按钮开始听力练习
            </p>
          </div>

          <!-- 文章内容 -->
          <div
            class="prose prose-emerald max-w-none text-gray-700 leading-relaxed whitespace-pre-wrap"
            v-html="highlightContent(question.content)"
          />

          <!-- 划词翻译弹窗 -->
          <div
            v-if="showTranslation"
            class="fixed bg-white rounded-lg shadow-xl border border-gray-200 p-4 z-60 max-w-xs"
            :style="{
              top: translationPosition.y + 'px',
              left: translationPosition.x + 'px',
            }"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="font-semibold text-gray-800">{{
                selectedWord
              }}</span>
              <button
                class="text-gray-400 hover:text-gray-600"
                @click="showTranslation = false"
              >
                <i class="fas fa-times" />
              </button>
            </div>
            <p class="text-gray-600 text-sm mb-3">
              {{ getWordTranslation(selectedWord) }}
            </p>
            <button
              class="w-full py-2 bg-emerald-50 text-emerald-700 text-sm font-medium rounded-lg hover:bg-emerald-100 transition-colors flex items-center justify-center"
              @click="addToVocabulary"
            >
              <i class="fas fa-plus mr-1.5" />
              加入单词打卡
            </button>
          </div>
        </div>

        <!-- 右侧：题目区 -->
        <div class="w-full md:w-1/2 overflow-y-auto p-6">
          <h3
            class="text-lg font-semibold text-gray-800 mb-4 flex items-center"
          >
            <i class="fas fa-tasks text-emerald-500 mr-2" />
            题目 ({{ currentQuestionIndex + 1 }}/{{
              question.questions.length
            }})
          </h3>

          <!-- 当前题目 -->
          <div class="mb-6">
            <p class="text-gray-800 font-medium mb-4">
              {{ currentQuestion.question }}
            </p>

            <!-- 选项 -->
            <div
              v-if="currentQuestion.options"
              class="space-y-3"
            >
              <button
                v-for="(option, index) in currentQuestion.options"
                :key="index"
                class="w-full text-left p-4 rounded-lg border-2 transition-all"
                :class="getOptionClass(option)"
                :disabled="showAnswer"
                @click="selectOption(option)"
              >
                <span v-html="option" />
                <i
                  v-if="showAnswer && option.startsWith(currentQuestion.answer)"
                  class="fas fa-check-circle text-green-500 float-right"
                />
                <i
                  v-if="
                    showAnswer &&
                      selectedAnswer === option &&
                      !option.startsWith(currentQuestion.answer)
                  "
                  class="fas fa-times-circle text-red-500 float-right"
                />
              </button>
            </div>

            <!-- 作文题 -->
            <div
              v-if="currentQuestion.type === 'essay'"
              class="space-y-4"
            >
              <textarea
                v-model="essayAnswer"
                rows="10"
                placeholder="请在此输入你的答案..."
                class="w-full p-4 border-2 border-gray-200 rounded-lg focus:outline-none focus:border-emerald-500 resize-none"
                :disabled="showAnswer"
              />
              <div class="flex justify-between text-sm text-gray-500">
                <span>字数：{{
                  essayAnswer.split(/\s+/).filter((w) => w).length
                }}
                  词</span>
                <span>要求：至少 250 词</span>
              </div>
            </div>
          </div>

          <!-- 解析区域 -->
          <div
            v-if="showAnswer"
            class="p-4 bg-emerald-50 rounded-lg border border-emerald-200 mb-6"
          >
            <h4 class="font-semibold text-emerald-800 mb-2 flex items-center">
              <i class="fas fa-lightbulb text-yellow-500 mr-2" />
              解析
            </h4>
            <p class="text-gray-700 text-sm leading-relaxed">
              {{ currentQuestion.explanation }}
            </p>
            <!-- 关联语法点/微课链接 -->
            <button
              v-if="question.relatedCourse"
              class="mt-3 text-emerald-600 hover:text-emerald-700 text-sm flex items-center"
            >
              <i class="fas fa-play-circle mr-1.5" />
              查看相关微课：{{ getCourseName(question.relatedCourse) }}
            </button>
          </div>

          <!-- 操作按钮 -->
          <div
            class="flex items-center justify-between pt-4 border-t border-gray-200"
          >
            <div class="flex gap-2">
              <button
                v-if="currentQuestionIndex > 0"
                class="px-4 py-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors"
                @click="prevQuestion"
              >
                <i class="fas fa-chevron-left mr-1" />
                上一题
              </button>
            </div>

            <div class="flex gap-2">
              <button
                v-if="!showAnswer"
                class="px-5 py-2.5 bg-emerald-500 text-white font-medium rounded-lg hover:bg-emerald-600 transition-colors shadow-sm"
                :disabled="!selectedAnswer && !essayAnswer"
                @click="submitAnswer"
              >
                <i class="fas fa-check mr-1.5" />
                提交答案
              </button>
              <button
                v-else-if="currentQuestionIndex < question.questions.length - 1"
                class="px-5 py-2.5 bg-emerald-500 text-white font-medium rounded-lg hover:bg-emerald-600 transition-colors shadow-sm"
                @click="nextQuestion"
              >
                下一题
                <i class="fas fa-chevron-right ml-1.5" />
              </button>
              <button
                v-else
                class="px-5 py-2.5 bg-blue-500 text-white font-medium rounded-lg hover:bg-blue-600 transition-colors shadow-sm"
                @click="finishPractice"
              >
                <i class="fas fa-flag-checkered mr-1.5" />
                完成练习
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部进度条 -->
      <div class="px-6 py-3 border-t border-gray-200 bg-gray-50 flex-shrink-0">
        <div class="flex items-center gap-4">
          <span class="text-sm text-gray-600">完成进度</span>
          <div class="flex-grow bg-gray-200 rounded-full h-2">
            <div
              class="bg-emerald-500 h-2 rounded-full transition-all duration-300"
              :style="{ width: progressPercentage + '%' }"
            />
          </div>
          <span class="text-sm font-medium text-emerald-600">{{ answeredCount }}/{{ question.questions.length }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import {
  QUESTION_TYPES,
  DIFFICULTY_LEVELS,
  COURSES,
} from "@/utils/questionData.js";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  question: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["close", "complete", "add-vocabulary"]);

// 状态
const currentQuestionIndex = ref(0);
const selectedAnswer = ref("");
const essayAnswer = ref("");
const showAnswer = ref(false);
const answers = ref([]);
const isPlaying = ref(false);
const audioProgress = ref(0);
const currentTime = ref(0);
const duration = ref(180);

// 划词翻译状态
const showTranslation = ref(false);
const selectedWord = ref("");
const translationPosition = ref({ x: 0, y: 0 });

// 计算属性
const currentQuestion = computed(() => {
  return props.question.questions?.[currentQuestionIndex.value] || {};
});

const answeredCount = computed(() => {
  return answers.value.filter((a) => a !== null).length;
});

const progressPercentage = computed(() => {
  if (!props.question.questions?.length) return 0;
  return Math.round(
    (answeredCount.value / props.question.questions.length) * 100
  );
});

// 监听题目变化，重置状态
watch(
  () => props.question,
  () => {
    resetState();
  },
  { immediate: true }
);

// 重置状态
function resetState() {
  currentQuestionIndex.value = 0;
  selectedAnswer.value = "";
  essayAnswer.value = "";
  showAnswer.value = false;
  answers.value = new Array(props.question.questions?.length || 0).fill(null);
  showTranslation.value = false;
}

// 获取题型相关方法
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
    listening: "bg-blue-100 text-blue-800",
    reading: "bg-purple-100 text-purple-800",
    writing: "bg-green-100 text-green-800",
    grammar: "bg-orange-100 text-orange-800",
    vocabulary: "bg-pink-100 text-pink-800",
  };
  return classes[type] || "bg-gray-100 text-gray-800";
}

function getDifficultyName(difficulty) {
  const diffObj = Object.values(DIFFICULTY_LEVELS).find(
    (d) => d.id === difficulty
  );
  return diffObj ? diffObj.name : difficulty;
}

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

function getCourseName(courseId) {
  const course = COURSES.find((c) => c.id === courseId);
  return course ? course.name : "";
}

// 选项样式
function getOptionClass(option) {
  if (!showAnswer.value) {
    return selectedAnswer.value === option
      ? "border-emerald-500 bg-emerald-50"
      : "border-gray-200 hover:border-emerald-300 hover:bg-gray-50";
  }

  const isCorrect = option.startsWith(currentQuestion.value.answer);
  const isSelected = selectedAnswer.value === option;

  if (isCorrect) {
    return "border-green-500 bg-green-50";
  }
  if (isSelected && !isCorrect) {
    return "border-red-500 bg-red-50";
  }
  return "border-gray-200 bg-gray-50 opacity-60";
}

// 选择选项
function selectOption(option) {
  if (!showAnswer.value) {
    selectedAnswer.value = option;
  }
}

// 提交答案
function submitAnswer() {
  showAnswer.value = true;
  const isCorrect = selectedAnswer.value.startsWith(
    currentQuestion.value.answer
  );
  answers.value[currentQuestionIndex.value] = {
    answer: selectedAnswer.value || essayAnswer.value,
    isCorrect,
  };
}

// 上一题
function prevQuestion() {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--;
    const savedAnswer = answers.value[currentQuestionIndex.value];
    if (savedAnswer) {
      selectedAnswer.value = savedAnswer.answer;
      showAnswer.value = true;
    } else {
      selectedAnswer.value = "";
      showAnswer.value = false;
    }
  }
}

// 下一题
function nextQuestion() {
  if (currentQuestionIndex.value < props.question.questions.length - 1) {
    currentQuestionIndex.value++;
    const savedAnswer = answers.value[currentQuestionIndex.value];
    if (savedAnswer) {
      selectedAnswer.value = savedAnswer.answer;
      showAnswer.value = true;
    } else {
      selectedAnswer.value = "";
      essayAnswer.value = "";
      showAnswer.value = false;
    }
  }
}

// 完成练习
function finishPractice() {
  const correctCount = answers.value.filter((a) => a?.isCorrect).length;
  const result = {
    questionId: props.question.id,
    answers: answers.value,
    correctCount,
    totalCount: props.question.questions.length,
    isAllCorrect: correctCount === props.question.questions.length,
  };
  emit("complete", result);
}

// 关闭模态框
function closeModal() {
  emit("close");
}

// 高亮内容中的生词
function highlightContent(content) {
  if (!content) return "";
  // 这里可以实现高亮逻辑
  return content;
}

// 处理文本选择（划词翻译）
function handleTextSelection(event) {
  const selection = window.getSelection();
  const text = selection.toString().trim();

  if (text && text.length > 0 && text.length < 50) {
    selectedWord.value = text;
    showTranslation.value = true;
    translationPosition.value = {
      x: Math.min(event.clientX, window.innerWidth - 280),
      y: event.clientY + 10,
    };
  } else {
    showTranslation.value = false;
  }
}

// 获取单词翻译（模拟）
function getWordTranslation(word) {
  const translations = {
    "social media": "社交媒体",
    communication: "沟通，交流",
    anxiety: "焦虑",
    depression: "抑郁",
    unprecedented: "前所未有的",
    sustainable: "可持续的",
    deteriorate: "恶化",
    anthropogenic: "人为的，人类活动引起的",
  };
  return translations[word.toLowerCase()] || "暂无翻译，请查阅词典";
}

// 添加到生词本
function addToVocabulary() {
  emit("add-vocabulary", selectedWord.value);
  showTranslation.value = false;
  alert(`已将 "${selectedWord.value}" 加入单词打卡！`);
}

// 音频控制
function toggleAudio() {
  isPlaying.value = !isPlaying.value;
  // 实际项目中这里需要控制真实的音频播放
}

// 格式化时间
function formatTime(seconds) {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins}:${secs.toString().padStart(2, "0")}`;
}
</script>

<style scoped>
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.animate-fadeIn {
  animation: fadeIn 0.2s ease-out;
}

.prose {
  font-size: 15px;
  line-height: 1.8;
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
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
