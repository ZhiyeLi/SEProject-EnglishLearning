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
    <main class="flex-grow">
      <div class="max-w-6xl mx-auto p-6">
        <!-- 顶部信息卡片 -->
        <div
          class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6"
        >
          <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
            <!-- 词汇类型和总数 -->
            <div
              class="bg-gradient-to-br from-emerald-50 to-teal-50 rounded-lg p-4 border border-emerald-200"
            >
              <div class="text-sm text-gray-600 mb-1 flex items-center">
                <i class="fas fa-book mr-2 text-emerald-500" />当前词汇类型
              </div>
              <div class="text-2xl font-bold text-emerald-600">
                {{ currentType?.name }}
              </div>
              <div class="text-sm text-gray-600 mt-2">
                总词汇数：<span class="font-semibold">{{
                  currentType?.totalWords
                }}</span>
              </div>
            </div>

            <!-- 已打卡数 -->
            <div
              class="bg-gradient-to-br from-blue-50 to-cyan-50 rounded-lg p-4 border border-blue-200"
            >
              <div class="text-sm text-gray-600 mb-1 flex items-center">
                <i class="fas fa-check-circle mr-2 text-blue-500" />已打卡数量
              </div>
              <div class="text-2xl font-bold text-blue-600">
                {{ currentProgress.passedCount }}
              </div>
              <div class="text-sm text-gray-600 mt-2">
                占比：<span class="font-semibold">{{ progressPercentage }}%</span>
              </div>
            </div>

            <!-- 进度条 -->
            <div
              class="bg-gradient-to-br from-purple-50 to-pink-50 rounded-lg p-4 border border-purple-200 md:col-span-2"
            >
              <div class="text-sm text-gray-600 mb-2 flex items-center">
                <i class="fas fa-chart-line mr-2 text-purple-500" />学习进度
              </div>
              <div
                class="w-full bg-gray-200 rounded-full h-3 overflow-hidden mb-2"
              >
                <div
                  class="bg-gradient-to-r from-emerald-400 to-teal-500 h-3 rounded-full transition-all duration-500"
                  :style="{ width: progressPercentage + '%' }"
                />
              </div>
              <div class="flex justify-between text-sm text-gray-600">
                <span>{{ currentProgress.passedCount }} /
                  {{ currentType?.totalWords }}</span>
                <span class="font-semibold text-emerald-600">{{ progressPercentage }}%</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 功能按钮行 -->
        <div
          class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6"
        >
          <div class="flex flex-wrap gap-3">
            <!-- 打卡计划按钮 -->
            <button
              class="flex items-center px-4 py-2 bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 transition-all shadow hover:shadow-md transform hover:-translate-y-0.5"
              @click="showPlanModal = true"
            >
              <i class="fas fa-calendar-check mr-2" />
              {{ currentPlan ? "修改打卡计划" : "制订打卡计划" }}
            </button>

            <!-- 复习已打卡单词 -->
            <button
              :disabled="currentProgress.passedCount === 0"
              class="flex items-center px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-all shadow hover:shadow-md transform hover:-translate-y-0.5"
              @click="showReviewModal = true"
            >
              <i class="fas fa-redo mr-2" />
              复习已打卡单词 ({{ currentProgress.passedCount }})
            </button>

            <!-- 切换词汇类型 -->
            <button
              class="flex items-center px-4 py-2 bg-purple-500 text-white rounded-lg hover:bg-purple-600 transition-all shadow hover:shadow-md transform hover:-translate-y-0.5"
              @click="switchWordType"
            >
              <i class="fas fa-exchange-alt mr-2" />
              切换词汇类型
            </button>

            <!-- 暂停/继续按钮 -->
            <button
              :class="[
                'flex items-center px-4 py-2 rounded-lg transition-all shadow hover:shadow-md transform hover:-translate-y-0.5',
                isPaused
                  ? 'bg-green-500 text-white hover:bg-green-600'
                  : 'bg-orange-500 text-white hover:bg-orange-600',
              ]"
              @click="togglePause"
            >
              <i :class="[isPaused ? 'fas fa-play' : 'fas fa-pause', 'mr-2']" />
              {{ isPaused ? "继续打卡" : "暂停打卡" }}
            </button>
          </div>
        </div>

        <!-- 打卡计划信息卡片 -->
        <div
          v-if="currentPlan"
          class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6"
        >
          <h3 class="text-lg font-bold text-gray-800 mb-4 flex items-center">
            <i
              class="fas fa-clipboard-list text-emerald-500 mr-2"
            />当前打卡计划
          </h3>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="bg-emerald-50 rounded-lg p-4 border border-emerald-200">
              <div class="text-sm text-gray-600 mb-1">
                每日打卡数
              </div>
              <div class="text-2xl font-bold text-emerald-600">
                {{ currentPlan.wordsPerDay }}
              </div>
              <div class="text-xs text-gray-600 mt-1">
                个单词/天
              </div>
            </div>
            <div class="bg-blue-50 rounded-lg p-4 border border-blue-200">
              <div class="text-sm text-gray-600 mb-1">
                剩余单词数
              </div>
              <div class="text-2xl font-bold text-blue-600">
                {{ currentPlan.remainingWords }}
              </div>
              <div class="text-xs text-gray-600 mt-1">
                个单词
              </div>
            </div>
            <div class="bg-purple-50 rounded-lg p-4 border border-purple-200">
              <div class="text-sm text-gray-600 mb-1">
                需要天数
              </div>
              <div class="text-2xl font-bold text-purple-600">
                {{ currentPlan.daysNeeded }}
              </div>
              <div class="text-xs text-gray-600 mt-1">
                天
              </div>
            </div>
          </div>
          <div class="mt-4 p-3 bg-blue-50 rounded-lg border border-blue-200">
            <p class="text-sm text-gray-700">
              <i class="fas fa-info-circle text-blue-500 mr-2" />
              <span
                v-if="currentPlan.status === 'paused'"
                class="text-orange-600 font-semibold"
              >
                打卡已暂停
              </span>
              <span
                v-else
                class="text-emerald-600 font-semibold"
              >
                按照计划，您将在
                <strong>{{ currentPlan.daysNeeded }}</strong> 天内完成
                <strong>{{ currentPlan.remainingWords }}</strong> 个单词的打卡。
              </span>
            </p>
          </div>
        </div>

        <!-- 开始打卡按钮 -->
        <div
          v-if="currentProgress.passedCount < currentType?.totalWords"
          class="text-center"
        >
          <button
            :disabled="isPaused || !currentPlan"
            class="px-12 py-4 bg-gradient-to-r from-emerald-500 to-teal-600 text-white text-lg font-bold rounded-xl hover:shadow-lg transform hover:-translate-y-1 transition-all disabled:from-gray-400 disabled:to-gray-500 disabled:cursor-not-allowed shadow-md"
            @click="startCheckIn"
          >
            <i class="fas fa-play mr-2" />
            {{
              isPaused
                ? "已暂停 - 请先继续打卡"
                : currentPlan
                  ? "开始打卡"
                  : "请先制订打卡计划"
            }}
          </button>
          <p class="text-gray-600 text-sm mt-4">
            <i class="fas fa-lightbulb mr-1 text-yellow-500" />
            还剩
            <span class="font-semibold text-emerald-600">{{
              currentType?.totalWords - currentProgress.passedCount
            }}</span>
            个单词未打卡
          </p>
        </div>

        <!-- 完成信息 -->
        <div
          v-else
          class="text-center py-12"
        >
          <div class="inline-block">
            <div
              class="w-20 h-20 rounded-full bg-gradient-to-r from-emerald-400 to-teal-500 flex items-center justify-center mb-4 shadow-lg"
            >
              <i class="fas fa-trophy text-white text-4xl" />
            </div>
            <h2 class="text-3xl font-bold text-gray-800 mb-2">
              恭喜！
            </h2>
            <p class="text-lg text-gray-600 mb-4">
              您已完成 {{ currentType?.name }} 的全部打卡
            </p>
            <button
              class="px-6 py-2 bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 transition-all"
              @click="switchWordType"
            >
              <i class="fas fa-exchange-alt mr-2" />切换其他词汇类型
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- 打卡计划模态框 -->
    <div
      v-if="showPlanModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
    >
      <div
        class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-8 animate-fadeIn"
      >
        <h2 class="text-2xl font-bold text-gray-800 mb-1">
          <i class="fas fa-calendar-check text-emerald-500 mr-2" />制订打卡计划
        </h2>
        <p class="text-gray-600 mb-6">
          选择每天要打卡的单词数量（1-100）
        </p>

        <!-- 错误提示 -->
        <div
          v-if="planError"
          class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg"
        >
          <p class="text-red-700 text-sm flex items-center">
            <i class="fas fa-exclamation-circle mr-2" />{{ planError }}
          </p>
        </div>

        <!-- 输入框 -->
        <div class="mb-6">
          <label
            for="planInput"
            class="block text-sm font-semibold text-gray-700 mb-2"
          >
            每天打卡数量（单词/天）
          </label>
          <input
            id="planInput"
            v-model.number="planWordsPerDay"
            type="number"
            min="1"
            max="100"
            placeholder="请输入1-100之间的数字"
            class="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-200 transition-all"
            @keyup.enter="confirmPlan"
          >
          <p class="text-xs text-gray-600 mt-2">
            💡 根据您选择的数量，系统将自动计算完成打卡所需的天数
          </p>
        </div>

        <!-- 计算结果预览 -->
        <div
          v-if="
            planWordsPerDay && planWordsPerDay >= 1 && planWordsPerDay <= 100
          "
          class="mb-6 p-4 bg-emerald-50 rounded-lg border-2 border-emerald-200"
        >
          <p class="text-sm text-gray-700 mb-2">
            <span class="font-semibold">计划预览：</span>
          </p>
          <ul class="text-sm text-gray-700 space-y-1">
            <li>
              📌 每天打卡：<span class="font-semibold text-emerald-600">{{
                planWordsPerDay
              }}</span>
              个单词
            </li>
            <li>
              📌 剩余单词：<span class="font-semibold text-emerald-600">{{
                currentType?.totalWords - currentProgress.passedCount
              }}</span>
              个单词
            </li>
            <li>
              📌 需要天数：<span class="font-semibold text-emerald-600">{{
                calculateDaysNeeded(planWordsPerDay)
              }}</span>
              天
            </li>
          </ul>
        </div>

        <!-- 按钮 -->
        <div class="flex gap-3">
          <button
            class="flex-1 px-4 py-2 border-2 border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-semibold transition-all"
            @click="showPlanModal = false"
          >
            取消
          </button>
          <button
            :disabled="
              !planWordsPerDay || planWordsPerDay < 1 || planWordsPerDay > 100
            "
            class="flex-1 px-4 py-3 bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 disabled:bg-gray-400 disabled:cursor-not-allowed font-semibold transition-all"
            @click="confirmPlan"
          >
            <i class="fas fa-check mr-2" />确认计划
          </button>
        </div>
      </div>
    </div>

    <!-- 复习单词模态框 -->
    <div
      v-if="showReviewModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
    >
      <div
        class="bg-white rounded-2xl shadow-2xl max-w-2xl w-full p-8 animate-fadeIn max-h-96 overflow-y-auto"
      >
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold text-gray-800">
            <i class="fas fa-redo text-blue-500 mr-2" />复习已打卡单词
          </h2>
          <button
            class="text-gray-600 hover:text-gray-800 p-2"
            @click="showReviewModal = false"
          >
            <i class="fas fa-times text-xl" />
          </button>
        </div>

        <!-- 单词列表 -->
        <div class="space-y-3 mb-6">
          <div
            v-for="wordId in currentProgress.passedWords"
            :key="wordId"
            class="p-4 bg-gray-50 rounded-lg border border-gray-200 hover:bg-gray-100 transition-all flex items-center justify-between"
          >
            <div>
              <p class="font-semibold text-gray-800">
                {{ getWordById(wordId)?.word }}
              </p>
              <p class="text-sm text-gray-600">
                {{ getWordById(wordId)?.translation }}
              </p>
            </div>
            <div class="flex items-center gap-2">
              <span
                class="text-xs bg-emerald-100 text-emerald-700 px-2 py-1 rounded"
              >已打卡</span>
              <button
                class="text-red-500 hover:text-red-700 transition-colors"
                title="取消打卡"
                @click="unmarkWordAsPassed(wordId)"
              >
                <i class="fas fa-undo" />
              </button>
            </div>
          </div>
        </div>

        <p class="text-sm text-gray-600 text-center">
          共
          <span class="font-semibold">{{
            currentProgress.passedWords.length
          }}</span>
          个已打卡单词
        </p>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <EndBar />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { WORD_TYPES, wordProgressManager } from "@/utils/wordData.js";
import NavBar from "@/components/common/NavBar.vue";
import EndBar from "@/components/common/EndBar.vue";

const router = useRouter();

// 数据
const currentType = ref(null);
const currentProgress = ref({});
const currentPlan = ref(null);
const showPlanModal = ref(false);
const showReviewModal = ref(false);
const planWordsPerDay = ref(null);
const planError = ref("");
const isPaused = ref(false);

// 导航项
const navItems = ref([
  { label: "首页", onClick: () => goHome(), isActive: false },
  { label: "课程", onClick: () => gotoCourse() },
  { label: "题库", path: "#" },
  {
    label: "时间表",
    onClick: () => router.push({ name: "TimeTable" }).catch(() => {}),
  },
  { label: "单词打卡", onClick: () => {}, isActive: true },
  { label: "AI伴学", onClick: () => gotoAiChat(), isActive: false },
]);

// 计算属性
const progressPercentage = computed(() => {
  if (!currentType.value) return 0;
  return Math.round(
    (currentProgress.value.passedCount / currentType.value.totalWords) * 100
  );
});

onMounted(() => {
  // 初始化数据
  wordProgressManager.initProgress();

  // 从localStorage获取已选择的类型
  const selectedTypeData = wordProgressManager.getSelectedType();
  const typeId = selectedTypeData?.typeId;

  if (!typeId) {
    // 如果没有选择过类型，重定向到类型选择页面
    router.push({ name: "WordTypeSelection" }).catch(() => {});
    return;
  }

  // 获取当前类型信息
  const typeObj = Object.values(WORD_TYPES).find((t) => t.id === typeId);
  if (typeObj) {
    currentType.value = typeObj;
    currentProgress.value = wordProgressManager.getTypeProgress(typeId);
    currentPlan.value = wordProgressManager.getPlan();

    // 检查计划状态
    if (currentPlan.value?.status === "paused") {
      isPaused.value = true;
    }
  }
});

/**
 * 计算需要的天数
 */
function calculateDaysNeeded(wordsPerDay) {
  if (!currentType.value || !currentProgress.value) return 0;
  const remainingWords =
    currentType.value.totalWords - currentProgress.value.passedCount;
  return Math.ceil(remainingWords / wordsPerDay);
}

/**
 * 确认打卡计划
 */
function confirmPlan() {
  planError.value = "";

  if (
    !planWordsPerDay.value ||
    planWordsPerDay.value < 1 ||
    planWordsPerDay.value > 100
  ) {
    planError.value = "请输入1-100之间的数字";
    return;
  }

  const plan = wordProgressManager.createPlan(
    currentType.value.id,
    planWordsPerDay.value
  );

  if (plan.error) {
    planError.value = plan.error;
    return;
  }

  currentPlan.value = plan;
  showPlanModal.value = false;
  planWordsPerDay.value = null;
}

/**
 * 开始打卡
 */
function startCheckIn() {
  // 这里可以导航到实际的单词打卡界面
  // 暂时只是显示提示
  alert("单词打卡功能待实现");
}

/**
 * 暂停/继续打卡
 */
function togglePause() {
  if (!currentPlan.value) return;

  if (isPaused.value) {
    wordProgressManager.resumePlan();
    isPaused.value = false;
  } else {
    wordProgressManager.pausePlan();
    isPaused.value = true;
  }

  currentPlan.value = wordProgressManager.getPlan();
}

/**
 * 切换词汇类型
 */
function switchWordType() {
  router.push({ name: "WordTypeSelection" }).catch(() => {});
}

/**
 * 返回首页
 */
function goHome() {
  router.push({ name: "Home" }).catch(() => {});
}

function gotoAiChat() {
  router.push({ name: "AiChat" }).catch(() => {});
}

function gotoCourse() {
  router.push({ name: "Course"}).catch(() => {});
}
/**
 * 根据单词ID获取单词信息
 */
function getWordById(wordId) {
  if (!currentType.value) return null;
  return currentType.value.words.find((w) => w.id === wordId);
}

/**
 * 取消单词打卡标记
 */
function unmarkWordAsPassed(wordId) {
  wordProgressManager.unmarkWordAsPassed(currentType.value.id, wordId);
  currentProgress.value = wordProgressManager.getTypeProgress(
    currentType.value.id
  );
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
  animation: fadeIn 0.3s ease-out;
}
</style>
