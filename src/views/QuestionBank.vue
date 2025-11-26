<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 导航栏 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <button
          class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors"
          title="返回首页"
          @click="goHome"
        >
          <i class="fas fa-home text-lg" />
        </button>
      </template>
    </NavBar>

    <!-- 主内容区 -->
    <main class="flex-grow flex">
      <!-- 左侧筛选侧边栏 -->
      <aside
        class="w-72 bg-white border-r border-gray-200 flex-shrink-0 overflow-y-auto hidden lg:block"
      >
        <div class="p-5">
          <!-- 侧边栏标题 -->
          <h2 class="text-lg font-bold text-gray-800 mb-5 flex items-center">
            <i class="fas fa-filter text-emerald-500 mr-2" />
            筛选条件
          </h2>

          <!-- 关联筛选 -->
          <div class="mb-6">
            <h3
              class="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wider"
            >
              关联筛选
            </h3>
            <div class="space-y-3">
              <label class="flex items-center cursor-pointer group">
                <input
                  v-model="filters.currentCourseOnly"
                  type="checkbox"
                  class="w-4 h-4 text-emerald-500 border-gray-300 rounded focus:ring-emerald-500"
                >
                <span
                  class="ml-3 text-sm text-gray-700 group-hover:text-emerald-600 transition-colors"
                >
                  只看当前课程相关
                </span>
              </label>
              <label class="flex items-center cursor-pointer group">
                <input
                  v-model="filters.includeVocabulary"
                  type="checkbox"
                  class="w-4 h-4 text-emerald-500 border-gray-300 rounded focus:ring-emerald-500"
                >
                <span
                  class="ml-3 text-sm text-gray-700 group-hover:text-emerald-600 transition-colors"
                >
                  包含我的生词
                </span>
              </label>
            </div>
          </div>

          <!-- 当前课程选择 -->
          <div
            v-if="filters.currentCourseOnly"
            class="mb-6"
          >
            <h3
              class="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wider"
            >
              选择课程
            </h3>
            <select
              v-model="filters.currentCourseId"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
            >
              <option value="">
                请选择课程
              </option>
              <option
                v-for="course in courses"
                :key="course.id"
                :value="course.id"
              >
                {{ course.name }}
              </option>
            </select>
          </div>

          <!-- 题型筛选 -->
          <div class="mb-6">
            <h3
              class="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wider"
            >
              题型
            </h3>
            <div class="space-y-2">
              <label
                v-for="type in questionTypes"
                :key="type.id"
                class="flex items-center cursor-pointer group"
              >
                <input
                  v-model="filters.types"
                  type="checkbox"
                  :value="type.id"
                  class="w-4 h-4 text-emerald-500 border-gray-300 rounded focus:ring-emerald-500"
                >
                <span
                  class="ml-3 text-sm text-gray-700 group-hover:text-emerald-600 transition-colors flex items-center"
                >
                  <i :class="['fas', type.icon, 'mr-2 w-4 text-gray-400']" />
                  {{ type.name }}
                </span>
              </label>
            </div>
          </div>

          <!-- 难度筛选 -->
          <div class="mb-6">
            <h3
              class="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wider"
            >
              考试/难度
            </h3>
            <div class="space-y-2">
              <label
                v-for="diff in difficultyLevels"
                :key="diff.id"
                class="flex items-center cursor-pointer group"
              >
                <input
                  v-model="filters.difficulties"
                  type="checkbox"
                  :value="diff.id"
                  class="w-4 h-4 text-emerald-500 border-gray-300 rounded focus:ring-emerald-500"
                >
                <span
                  class="ml-3 text-sm text-gray-700 group-hover:text-emerald-600 transition-colors flex items-center"
                >
                  {{ diff.name }}
                  <span class="ml-2 flex">
                    <template
                      v-for="i in diff.stars"
                      :key="i"
                    >
                      <i class="fas fa-star text-xs text-yellow-400" />
                    </template>
                  </span>
                </span>
              </label>
            </div>
          </div>

          <!-- 状态筛选 -->
          <div class="mb-6">
            <h3
              class="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wider"
            >
              状态
            </h3>
            <div class="space-y-2">
              <label
                v-for="status in statusOptions"
                :key="status.value"
                class="flex items-center cursor-pointer group"
              >
                <input
                  v-model="filters.status"
                  type="radio"
                  name="status"
                  :value="status.value"
                  class="w-4 h-4 text-emerald-500 border-gray-300 focus:ring-emerald-500"
                >
                <span
                  class="ml-3 text-sm text-gray-700 group-hover:text-emerald-600 transition-colors flex items-center"
                >
                  <i :class="['mr-2', status.icon, status.iconClass]" />
                  {{ status.label }}
                </span>
              </label>
            </div>
          </div>

          <!-- 重置筛选 -->
          <button
            class="w-full py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium"
            @click="resetFilters"
          >
            <i class="fas fa-undo mr-1.5" />
            重置筛选
          </button>
        </div>
      </aside>

      <!-- 主内容区 -->
      <div class="flex-grow flex flex-col">
        <!-- 顶部搜索与统计栏 -->
        <div
          class="bg-white border-b border-gray-200 px-6 py-4 sticky top-0 z-10"
        >
          <div
            class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4"
          >
            <!-- 全局搜索框 -->
            <div class="relative flex-grow max-w-xl">
              <input
                v-model="filters.keyword"
                type="text"
                placeholder="搜索题目（关键词、语法点、课程名称...）"
                class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent text-sm"
                @input="debouncedSearch"
              >
              <i
                class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
              />
              <button
                v-if="filters.keyword"
                class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                @click="filters.keyword = ''"
              >
                <i class="fas fa-times" />
              </button>
            </div>

            <!-- 统计信息 -->
            <div class="flex items-center gap-6 text-sm">
              <div
                class="flex items-center gap-2 px-3 py-1.5 bg-emerald-50 rounded-lg"
              >
                <i class="fas fa-edit text-emerald-500" />
                <span class="text-gray-600">今日做题：</span>
                <span class="font-semibold text-emerald-600">{{
                  statistics.todayDone || 0
                }}</span>
              </div>
              <div
                class="flex items-center gap-2 px-3 py-1.5 bg-blue-50 rounded-lg"
              >
                <i class="fas fa-percentage text-blue-500" />
                <span class="text-gray-600">正确率：</span>
                <span class="font-semibold text-blue-600">{{ statistics.accuracy }}%</span>
              </div>
              <div
                class="flex items-center gap-2 px-3 py-1.5 bg-purple-50 rounded-lg"
              >
                <i class="fas fa-book text-purple-500" />
                <span class="text-gray-600">掌握单词：</span>
                <span class="font-semibold text-purple-600">{{
                  statistics.masteredWords
                }}</span>
              </div>
            </div>
          </div>

          <!-- 当前筛选标签 -->
          <div
            v-if="hasActiveFilters"
            class="mt-3 flex items-center gap-2 flex-wrap"
          >
            <span class="text-sm text-gray-500">当前筛选：</span>
            <span
              v-for="tag in activeFilterTags"
              :key="tag.key"
              class="inline-flex items-center px-2 py-1 bg-emerald-100 text-emerald-700 text-xs rounded-full"
            >
              {{ tag.label }}
              <button
                class="ml-1 hover:text-emerald-900"
                @click="removeFilter(tag.key)"
              >
                <i class="fas fa-times" />
              </button>
            </span>
            <button
              class="text-xs text-gray-500 hover:text-emerald-600 underline"
              @click="resetFilters"
            >
              清除全部
            </button>
          </div>
        </div>

        <!-- 题目列表区域 -->
        <div class="flex-grow p-6 overflow-y-auto">
          <!-- 结果统计 -->
          <div class="mb-4 flex items-center justify-between">
            <p class="text-sm text-gray-600">
              共找到
              <span class="font-semibold text-emerald-600">{{
                filteredQuestions.length
              }}</span>
              道题目
            </p>
            <!-- 排序选项 -->
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">排序：</span>
              <select
                v-model="sortBy"
                class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
              >
                <option value="newest">
                  最新添加
                </option>
                <option value="difficulty">
                  按难度
                </option>
                <option value="type">
                  按题型
                </option>
              </select>
            </div>
          </div>

          <!-- 题目卡片列表 -->
          <div
            v-if="filteredQuestions.length > 0"
            class="grid grid-cols-1 xl:grid-cols-2 gap-4"
          >
            <QuestionCard
              v-for="question in paginatedQuestions"
              :key="question.id"
              :question="question"
              @start-practice="startPractice"
              @add-to-plan="addToPlan"
              @toggle-favorite="toggleFavorite"
            />
          </div>

          <!-- 分页组件 -->
          <div
            v-if="filteredQuestions.length > 0 && totalPages > 1"
            class="mt-8 flex items-center justify-center gap-2"
          >
            <!-- 上一页按钮 -->
            <button
              class="px-3 py-2 rounded-lg border transition-colors flex items-center gap-1"
              :class="
                currentPage === 1
                  ? 'border-gray-200 text-gray-400 cursor-not-allowed'
                  : 'border-gray-300 text-gray-600 hover:bg-emerald-50 hover:border-emerald-500 hover:text-emerald-600'
              "
              :disabled="currentPage === 1"
              @click="prevPage"
            >
              <i class="fas fa-chevron-left text-sm" />
              <span class="hidden sm:inline">上一页</span>
            </button>

            <!-- 页码按钮 -->
            <div class="flex items-center gap-1">
              <template
                v-for="page in pageNumbers"
                :key="page"
              >
                <button
                  v-if="page !== '...'"
                  class="w-10 h-10 rounded-lg border font-medium transition-colors"
                  :class="
                    currentPage === page
                      ? 'bg-emerald-500 border-emerald-500 text-white'
                      : 'border-gray-300 text-gray-600 hover:bg-emerald-50 hover:border-emerald-500 hover:text-emerald-600'
                  "
                  @click="goToPage(page)"
                >
                  {{ page }}
                </button>
                <span
                  v-else
                  class="w-10 h-10 flex items-center justify-center text-gray-400"
                >
                  ...
                </span>
              </template>
            </div>

            <!-- 下一页按钮 -->
            <button
              class="px-3 py-2 rounded-lg border transition-colors flex items-center gap-1"
              :class="
                currentPage === totalPages
                  ? 'border-gray-200 text-gray-400 cursor-not-allowed'
                  : 'border-gray-300 text-gray-600 hover:bg-emerald-50 hover:border-emerald-500 hover:text-emerald-600'
              "
              :disabled="currentPage === totalPages"
              @click="nextPage"
            >
              <span class="hidden sm:inline">下一页</span>
              <i class="fas fa-chevron-right text-sm" />
            </button>

            <!-- 页码信息 -->
            <span class="ml-4 text-sm text-gray-500 hidden md:inline">
              第 {{ currentPage }} / {{ totalPages }} 页，共
              {{ filteredQuestions.length }} 条
            </span>
          </div>

          <!-- 空状态 -->
          <div
            v-if="filteredQuestions.length === 0"
            class="text-center py-16"
          >
            <i class="fas fa-search text-gray-300 text-6xl mb-4" />
            <p class="text-gray-500 text-lg mb-2">
              没有找到符合条件的题目
            </p>
            <p class="text-gray-400 text-sm">
              尝试调整筛选条件或搜索关键词
            </p>
            <button
              class="mt-4 px-4 py-2 bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 transition-colors"
              @click="resetFilters"
            >
              重置筛选条件
            </button>
          </div>
        </div>
      </div>

      <!-- 错题本浮动按钮 -->
      <button
        class="fixed right-6 bottom-24 w-14 h-14 bg-red-500 text-white rounded-full shadow-lg hover:bg-red-600 hover:shadow-xl transition-all flex items-center justify-center group z-40"
        @click="showWrongQuestions = true"
      >
        <i class="fas fa-times-circle text-2xl" />
        <span
          v-if="wrongQuestionsCount > 0"
          class="absolute -top-1 -right-1 w-6 h-6 bg-yellow-400 text-gray-800 text-xs font-bold rounded-full flex items-center justify-center"
        >
          {{ wrongQuestionsCount > 99 ? "99+" : wrongQuestionsCount }}
        </span>
        <span
          class="absolute right-full mr-3 px-3 py-1.5 bg-gray-800 text-white text-sm rounded-lg opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap"
        >
          错题本
        </span>
      </button>
    </main>

    <!-- 练习详情模态框 -->
    <QuestionPractice
      :visible="showPractice"
      :question="currentPracticeQuestion"
      @close="showPractice = false"
      @complete="handlePracticeComplete"
      @add-vocabulary="handleAddVocabulary"
    />

    <!-- 错题本面板 -->
    <WrongQuestions
      :visible="showWrongQuestions"
      :wrong-questions="wrongQuestions"
      @close="showWrongQuestions = false"
      @go-to-question="goToQuestion"
      @retry="retryQuestion"
      @review-all="reviewAllWrong"
    />

    <!-- 加入计划模态框 -->
    <div
      v-if="showPlanModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click.self="showPlanModal = false"
    >
      <div
        class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6 animate-fadeIn"
      >
        <h2 class="text-xl font-bold text-gray-800 mb-4 flex items-center">
          <i class="fas fa-calendar-plus text-emerald-500 mr-2" />
          加入学习计划
        </h2>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">选择日期</label>
          <input
            v-model="planDate"
            type="date"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
          >
        </div>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">备注（可选）</label>
          <input
            v-model="planNote"
            type="text"
            placeholder="添加备注..."
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
          >
        </div>

        <div class="flex gap-3">
          <button
            class="flex-1 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            @click="showPlanModal = false"
          >
            取消
          </button>
          <button
            class="flex-1 py-2.5 bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 transition-colors"
            @click="confirmAddToPlan"
          >
            确认添加
          </button>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <EndBar />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from "vue";
import { useRouter } from "vue-router";
import NavBar from "@/components/common/NavBar.vue";
import EndBar from "@/components/common/EndBar.vue";
import QuestionCard from "@/components/business/QuestionCard.vue";
import QuestionPractice from "@/components/business/QuestionPractice.vue";
import WrongQuestions from "@/components/business/WrongQuestions.vue";
import {
  QUESTION_TYPES,
  DIFFICULTY_LEVELS,
  COURSES,
  questionBankManager,
} from "@/utils/questionData.js";

const router = useRouter();

// 导航项
const navItems = ref([
  { label: "首页", onClick: () => goHome(), isActive: false },
  { label: "课程", onClick: () => gotoCourse() },
  { label: "题库", path: "#", isActive: true },
  { label: "时间表", onClick: () => gotoTimeTable() },
  { label: "单词打卡", onClick: () => gotoWordCheckIn() },
  { label: "AI伴学", onClick: () => gotoAiChat() },
]);

// 数据
const questionTypes = Object.values(QUESTION_TYPES);
const difficultyLevels = Object.values(DIFFICULTY_LEVELS);
const courses = COURSES;

// 状态选项
const statusOptions = [
  {
    label: "全部",
    value: "all",
    icon: "fas fa-list",
    iconClass: "text-gray-400",
  },
  {
    label: "未做",
    value: "not-done",
    icon: "far fa-circle",
    iconClass: "text-gray-400",
  },
  {
    label: "已做",
    value: "done",
    icon: "fas fa-check-circle",
    iconClass: "text-emerald-500",
  },
  {
    label: "已收藏",
    value: "favorited",
    icon: "fas fa-heart",
    iconClass: "text-red-500",
  },
];

// 筛选条件
const filters = reactive({
  keyword: "",
  types: [],
  difficulties: [],
  status: "all",
  currentCourseOnly: false,
  currentCourseId: "",
  includeVocabulary: false,
});

// 排序
const sortBy = ref("newest");

// 分页
const currentPage = ref(1);
const pageSize = ref(6); // 每页显示6个题目

// 状态
const showPractice = ref(false);
const currentPracticeQuestion = ref({});
const showWrongQuestions = ref(false);
const showPlanModal = ref(false);
const planDate = ref("");
const planNote = ref("");
const currentPlanQuestion = ref(null);

// 统计数据 - 使用 computed 实现响应式更新
const statistics = computed(() => {
  return questionBankManager.getStatistics();
});

// 计算属性
const filteredQuestions = computed(() => {
  return questionBankManager.getFilteredQuestions(filters);
});

const sortedQuestions = computed(() => {
  const questions = [...filteredQuestions.value];
  const diffOrder = {
    beginner: 1,
    "cet4-6": 2,
    postgraduate: 3,
    "toefl-ielts": 4,
    professional: 5,
  };

  switch (sortBy.value) {
    case "difficulty":
      return questions.sort(
        (a, b) => diffOrder[a.difficulty] - diffOrder[b.difficulty]
      );
    case "type":
      return questions.sort((a, b) => a.type.localeCompare(b.type));
    case "newest":
    default:
      return questions.sort(
        (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
      );
  }
});

// 分页后的题目
const paginatedQuestions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return sortedQuestions.value.slice(start, end);
});

// 总页数
const totalPages = computed(() => {
  return Math.ceil(sortedQuestions.value.length / pageSize.value);
});

// 页码数组（显示最多5个页码）
const pageNumbers = computed(() => {
  const pages = [];
  const total = totalPages.value;
  const current = currentPage.value;

  if (total <= 5) {
    for (let i = 1; i <= total; i++) {
      pages.push(i);
    }
  } else {
    if (current <= 3) {
      pages.push(1, 2, 3, 4, "...", total);
    } else if (current >= total - 2) {
      pages.push(1, "...", total - 3, total - 2, total - 1, total);
    } else {
      pages.push(1, "...", current - 1, current, current + 1, "...", total);
    }
  }
  return pages;
});

const wrongQuestions = computed(() => {
  return questionBankManager.getWrongQuestions();
});

const wrongQuestionsCount = computed(() => {
  return wrongQuestions.value.length;
});

const hasActiveFilters = computed(() => {
  return (
    filters.keyword ||
    filters.types.length > 0 ||
    filters.difficulties.length > 0 ||
    filters.status !== "all" ||
    filters.currentCourseOnly ||
    filters.includeVocabulary
  );
});

const activeFilterTags = computed(() => {
  const tags = [];

  if (filters.keyword) {
    tags.push({ key: "keyword", label: `关键词: ${filters.keyword}` });
  }

  filters.types.forEach((type) => {
    const typeObj = questionTypes.find((t) => t.id === type);
    if (typeObj) {
      tags.push({ key: `type-${type}`, label: typeObj.name });
    }
  });

  filters.difficulties.forEach((diff) => {
    const diffObj = difficultyLevels.find((d) => d.id === diff);
    if (diffObj) {
      tags.push({ key: `diff-${diff}`, label: diffObj.name });
    }
  });

  if (filters.status !== "all") {
    const statusObj = statusOptions.find((s) => s.value === filters.status);
    if (statusObj) {
      tags.push({ key: "status", label: statusObj.label });
    }
  }

  if (filters.currentCourseOnly) {
    tags.push({ key: "courseOnly", label: "当前课程相关" });
  }

  if (filters.includeVocabulary) {
    tags.push({ key: "vocabulary", label: "包含生词" });
  }

  return tags;
});

// 初始化
onMounted(() => {
  // 设置默认日期为今天
  const today = new Date();
  planDate.value = today.toISOString().split("T")[0];
});

// 防抖搜索
let searchTimeout = null;
function debouncedSearch() {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    // 搜索逻辑已在计算属性中处理
    // 重置到第一页
    currentPage.value = 1;
  }, 300);
}

// 重置筛选
function resetFilters() {
  filters.keyword = "";
  filters.types = [];
  filters.difficulties = [];
  filters.status = "all";
  filters.currentCourseOnly = false;
  filters.currentCourseId = "";
  filters.includeVocabulary = false;
  currentPage.value = 1; // 重置页码
}

// 移除单个筛选
function removeFilter(key) {
  if (key === "keyword") {
    filters.keyword = "";
  } else if (key.startsWith("type-")) {
    const type = key.replace("type-", "");
    filters.types = filters.types.filter((t) => t !== type);
  } else if (key.startsWith("diff-")) {
    const diff = key.replace("diff-", "");
    filters.difficulties = filters.difficulties.filter((d) => d !== diff);
  } else if (key === "status") {
    filters.status = "all";
  } else if (key === "courseOnly") {
    filters.currentCourseOnly = false;
  } else if (key === "vocabulary") {
    filters.includeVocabulary = false;
  }
  currentPage.value = 1; // 筛选变化时重置页码
}

// 开始练习
function startPractice(question) {
  currentPracticeQuestion.value = question;
  showPractice.value = true;
}

// 处理练习完成
function handlePracticeComplete(result) {
  const status = result.isAllCorrect ? "correct" : "wrong";
  questionBankManager.updateQuestionStatus(result.questionId, "done", status);
  showPractice.value = false;
  // 统计数据会通过 computed 自动更新

  // 显示结果提示
  const message = result.isAllCorrect
    ? `太棒了！全部正确 (${result.correctCount}/${result.totalCount})`
    : `完成练习！正确 ${result.correctCount}/${result.totalCount} 题`;
  alert(message);
}

// 添加生词
function handleAddVocabulary(word) {
  // 这里可以调用生词本的API
  console.log("添加生词:", word);
}

// 添加到计划
function addToPlan(question) {
  currentPlanQuestion.value = question;
  showPlanModal.value = true;
}

// 确认添加到计划
function confirmAddToPlan() {
  if (!planDate.value) {
    alert("请选择日期");
    return;
  }

  // 这里可以调用计划管理的API
  console.log("添加计划:", {
    question: currentPlanQuestion.value,
    date: planDate.value,
    note: planNote.value,
  });

  alert(`已将题目加入 ${planDate.value} 的学习计划`);
  showPlanModal.value = false;
  planNote.value = "";
}

// 切换收藏
function toggleFavorite(questionId) {
  const isFavorited = questionBankManager.toggleFavorite(questionId);
  alert(isFavorited ? "已添加到收藏" : "已取消收藏");
}

// 跳转到题目（从错题本）
function goToQuestion(question) {
  showWrongQuestions.value = false;
  startPractice(question);
}

// 重试题目
function retryQuestion(question) {
  showWrongQuestions.value = false;
  startPractice(question);
}

// 全部重做错题
function reviewAllWrong(questions) {
  if (questions.length > 0) {
    showWrongQuestions.value = false;
    startPractice(questions[0]);
  }
}

// 导航方法
function goHome() {
  router.push({ name: "Home" }).catch(() => {});
}

function gotoCourse() {
  router.push({ name: "Course" }).catch(() => {});
}

function gotoTimeTable() {
  router.push({ name: "TimeTable" }).catch(() => {});
}

function gotoWordCheckIn() {
  router.push({ name: "WordCheckIn" }).catch(() => {});
}

function gotoAiChat() {
  router.push({ name: "AiChat" }).catch(() => {});
}

// 分页方法
function goToPage(page) {
  if (page === "..." || page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  // 滚动到题目列表顶部
  document
    .querySelector(".flex-grow.p-6")
    ?.scrollTo({ top: 0, behavior: "smooth" });
}

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
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

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

/* 复选框样式 */
input[type="checkbox"]:checked {
  background-color: #10b981;
  border-color: #10b981;
}

input[type="radio"]:checked {
  border-color: #10b981;
}
</style>
