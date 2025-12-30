<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 导航栏 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <ActionButtons
          @suggestions="showSuggestions"
          @settings="gotoSettings"
          @home="goHome"
          @notifications="showNotifications"
        />
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
              @click="startReview"
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

            <!-- 恢复上次打卡进度 -->
            <button
              :disabled="!resumeAvailable || !currentPlan"
              class="flex items-center px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-all shadow hover:shadow-md transform hover:-translate-y-0.5"
              @click="resumeCheckIn"
            >
              <i class="fas fa-history mr-2" />
              恢复上次进度
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
                每次打卡数
              </div>
              <div class="text-2xl font-bold text-emerald-600">
                {{ currentPlan.wordsPerDay }}
              </div>
              <div class="text-xs text-gray-600 mt-1">
                个单词/次
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
                需要打卡次数
              </div>
              <div class="text-2xl font-bold text-purple-600">
                {{ currentPlan.daysNeeded }}
              </div>
              <div class="text-xs text-gray-600 mt-1">
                次
              </div>
            </div>
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
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-40 p-4 pointer-events-none"
      @click="showPlanModal = false"
    >
      <div class="pointer-events-auto">
        <div
          class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-8 animate-fadeIn z-50"
          @click.stop
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
                📌 每次打卡：<span class="font-semibold text-emerald-600">{{
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
                📌 需要打卡次数：<span class="font-semibold text-emerald-600">{{
                  calculateDaysNeeded(planWordsPerDay)
                }}</span>
                次
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
    </div>



    <!-- 学习建议弹窗 -->
    <div
      v-if="showSuggestionsModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-40 p-4 pointer-events-none"
      @click="showSuggestionsModal = false"
    >
      <div
        class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl p-8 animate-fadeIn z-50 pointer-events-auto"
        @click.stop
      >
        <!-- 弹窗头部 -->
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-2xl font-bold text-gray-900 flex items-center">
            <i class="fas fa-lightbulb text-yellow-500 mr-3" />
            学习建议
          </h2>
          <button
            class="text-gray-400 hover:text-gray-600 transition-colors"
            @click="showSuggestionsModal = false"
          >
            <i class="fas fa-times text-2xl" />
          </button>
        </div>

        <!-- 弹窗内容 -->
        <div class="mb-6">
          <div class="space-y-4">
            <!-- 建议内容 -->
            <div>
              <h3 class="text-lg font-semibold text-gray-800 mb-3">
                <span class="text-emerald-600">{{
                  suggestionsData[currentSuggestionIndex].title
                }}</span>
              </h3>
              <p class="text-gray-700 leading-relaxed whitespace-pre-wrap">
                {{ suggestionsData[currentSuggestionIndex].content }}
              </p>
            </div>

            <!-- 建议标签 -->
            <div class="flex flex-wrap gap-2 pt-4">
              <span
                v-for="tag in suggestionsData[currentSuggestionIndex].tags"
                :key="tag"
                class="px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-sm"
              >
                {{ tag }}
              </span>
            </div>
          </div>
        </div>

        <!-- 弹窗底部 - 翻页控制 -->
        <div class="flex justify-between items-center border-t border-gray-200 pt-4">
          <button
            :disabled="currentSuggestionIndex === 0"
            class="px-6 py-2 rounded-lg font-medium transition-all"
            :class="
              currentSuggestionIndex === 0
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
            "
            @click="previousSuggestion"
          >
            <i class="fas fa-chevron-left mr-2" />上一条
          </button>

          <div class="text-gray-600 font-medium">
            {{ currentSuggestionIndex + 1 }} / {{ suggestionsData.length }}
          </div>

          <button
            :disabled="currentSuggestionIndex === suggestionsData.length - 1"
            class="px-6 py-2 rounded-lg font-medium transition-all"
            :class="
              currentSuggestionIndex === suggestionsData.length - 1
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
            "
            @click="nextSuggestion"
          >
            下一条<i class="fas fa-chevron-right ml-2" />
          </button>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <EndBar />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { wordProgressManager } from "@/utils/wordData.js";
import NavBar from "@/components/common/NavBar.vue";
import ActionButtons from "@/components/common/ActionButtons.vue";
import EndBar from "@/components/common/EndBar.vue";

const router = useRouter();
const route = useRoute();

// 数据
const currentType = ref(null);
const currentProgress = ref({});
const currentPlan = ref(null);
const showPlanModal = ref(false);
const planWordsPerDay = ref(null);
const planError = ref("");
const isPaused = ref(false);
const showSuggestionsModal = ref(false);
const currentSuggestionIndex = ref(0);
const resumeAvailable = ref(false);
const suggestionsData = ref([
  {
    title: "坚持打卡是关键",
    content:
      "根据你最近的学习数据，我发现你有几天没有坚持打卡。研究表明，每日坚持背单词比一次性背很多个词更能提高长期记忆效果。\n\n建议：\n• 每天固定时间打卡，形成习惯\n• 选择在精力最充沛的时候\n• 即使只有10分钟，也要坚持打卡\n\n相信你能做到！",
    tags: ["打卡习惯", "坚持", "记忆法"],
  },
  {
    title: "利用零碎时间高效学习",
    content:
      "你可以充分利用上下班、等车、休息间隙等零碎时间来复习单词。这些时间虽然不长，但积累起来效果显著。\n\n建议：\n• 使用移动设备随时复习\n• 利用碎片化时间做单词练习\n• 在高峰期巩固之前学过的词汇\n\n每天15-20分钟的有效学习胜过一次性的1小时被动学习。",
    tags: ["时间管理", "碎片化学习", "效率"],
  },
  {
    title: "制定合理的每日目标",
    content:
      "根据你的学习进度，建议适当调整每日学习单词数量。过多会导致疲劳，过少则影响进度。\n\n建议：\n• 四级备考阶段：每天50-100个单词\n• 六级备考阶段：每天80-120个单词\n• 根据个人吸收情况灵活调整\n\n记住：质量永远比数量重要！",
    tags: ["目标设置", "学习计划", "进度管理"],
  },
  {
    title: "重视拼写和发音",
    content:
      "单纯记忆单词的中文意思容易遗忘。建议同时关注单词的拼写、发音和用法。\n\n建议：\n• 大声朗读单词，加强发音记忆\n• 多做拼写练习，特别是容易混淆的词\n• 学习单词的衍生词和同义词\n\n这样学习的单词记忆时间会延长3倍以上。",
    tags: ["拼写", "发音", "词汇拓展"],
  },
  {
    title: "利用艾宾浩斯遗忘曲线",
    content:
      "我们的应用已经内置了艾宾浩斯遗忘曲线复习算法。系统会在最佳时间提醒你复习之前学过的单词。\n\n黄金复习时间点：\n• 第1次：学习后的1天\n• 第2次：学习后的3天\n• 第3次：学习后的7天\n• 第4次：学习后的15天\n• 第5次：学习后的30天\n\n按照系统提示复习，学习效果可提升5倍！",
    tags: ["遗忘曲线", "复习计划", "科学学习"],
  },
]);

// 导航项
const navItems = ref([
  { label: "首页", onClick: () => goHome(), isActive: false },
  { label: "课程", onClick: () => gotoCourse() },
  {
    label: "题库",
    onClick: () => router.push({ name: "QuestionBank" }).catch(() => {}),
  },
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
  const total = Number(currentType.value.totalWords) || 0;
  const passed = Number(currentProgress.value.passedCount) || 0;
  if (total === 0) return 0;
  const raw = (passed / total) * 100;
  let formatted = Number.parseFloat(raw.toFixed(1));
  // 如果有已打卡但显示为 0.0，则至少显示 0.1%，避免误导用户
  if (passed > 0 && formatted === 0) formatted = 0.1;
  return formatted;
});

onMounted(async () => {
  try {
    // 初始化数据
    await wordProgressManager.init();

    // 优先从路由参数获取typeId，然后从后端获取已选择的类型
    let typeId = route.params.typeId;
    console.log("从路由获取的typeId:", typeId, "类型:", typeof typeId);

    if (!typeId) {
      // 如果路由没有typeId，则从后端获取已选择的类型
      try {
        const selectedTypeData = await wordProgressManager.getSelectedType();
        console.log("从后端获取的选择数据:", selectedTypeData);
        typeId = selectedTypeData?.typeId || selectedTypeData?.id;
        console.log("提取的typeId:", typeId);
      } catch (error) {
        console.error("获取已选择的类型失败:", error);
      }
    }

    if (!typeId) {
      // 如果仍然没有typeId，显示提示并重定向到类型选择页面
      console.warn("未选择词汇类型，重定向到类型选择页面");
      router.push({ name: "WordTypeSelection" }).catch(() => {});
      return;
    }

    // 确保typeId是数字
    typeId = Number(typeId);

    // 从后端获取类型列表并找到当前类型
    const typeList = await wordProgressManager.getWordTypeList();
    console.log("类型列表:", typeList);
    console.log("查找typeId:", typeId, "类型:", typeof typeId);
    const typeObj = typeList.find((t) => Number(t.typeId) === Number(typeId));
    
    if (!typeObj) {
      // 如果找不到类型，重定向到选择页面
      console.warn(`找不到对应的词汇类型: ${typeId}`);
      console.warn("可用的类型ID:", typeList.map(t => t.typeId));
      router.push({ name: "WordTypeSelection" }).catch(() => {});
      return;
    }
    
    console.log("找到的类型对象:", typeObj);

    // 设置当前类型，使用后端返回的数据格式
    currentType.value = {
      id: typeObj.typeId || typeObj.id,
      name: typeObj.name,
      description: typeObj.description,
      totalWords: typeObj.totalWords,
    };
    
    currentProgress.value = await wordProgressManager.getTypeProgress(typeObj.typeId || typeObj.id);
    if (!currentProgress.value) {
      currentProgress.value = { passedCount: 0, passedWords: [] };
    }
    
    // 获取该词汇类型的打卡计划
    const typeIdForPlan = typeObj.typeId || typeObj.id;
    currentPlan.value = await wordProgressManager.getPlan(typeIdForPlan);

    // 如果是本地计划，更新计算字段
    if (currentPlan.value?.isLocal) {
      updateLocalPlanCalculations();
    }

    // 检查计划状态
    if (currentPlan.value?.status === "paused") {
      isPaused.value = true;
    }

    // 检查是否有可恢复的本地进度
    updateResumeAvailable();
  } catch (error) {
    console.error("初始化失败:", error);
    console.error("详细信息:", error.message);
    router.push({ name: "WordTypeSelection" }).catch(() => {});
  }
});

/**
 * 当返回到此页面时刷新打卡进度
 */
onActivated(async () => {
  try {
    if (currentType.value?.id) {
      // 重新获取该词汇类型的打卡进度
      currentProgress.value = await wordProgressManager.getTypeProgress(currentType.value.id);
      if (!currentProgress.value) {
        currentProgress.value = { passedCount: 0, passedWords: [] };
      }
      updateResumeAvailable();
    }
  } catch (error) {
    console.error("刷新打卡进度失败:", error);
  }
});

/**
 * 监听路由参数变化，当typeId改变时重新加载数据
 */
watch(() => route.params.typeId, async (newTypeId) => {
  if (newTypeId) {
    try {
      // 重新初始化数据
      await loadDataForType(newTypeId);
    } catch (error) {
      console.error("切换词汇类型失败:", error);
      router.push({ name: "WordTypeSelection" }).catch(() => {});
    }
  }
}, { immediate: false });

/**
 * 加载指定类型的打卡数据
 */
async function loadDataForType(typeId) {
  // 确保typeId是数字
  typeId = Number(typeId);

  // 从后端获取类型列表并找到当前类型
  const typeList = await wordProgressManager.getWordTypeList();
  const typeObj = typeList.find((t) => Number(t.typeId) === Number(typeId));
  
  if (!typeObj) {
    console.warn(`找不到对应的词汇类型: ${typeId}`);
    router.push({ name: "WordTypeSelection" }).catch(() => {});
    return;
  }

  // 设置当前类型
  currentType.value = {
    id: typeObj.typeId || typeObj.id,
    name: typeObj.name,
    description: typeObj.description,
    totalWords: typeObj.totalWords,
  };
  
  currentProgress.value = await wordProgressManager.getTypeProgress(typeObj.typeId || typeObj.id);
  if (!currentProgress.value) {
    currentProgress.value = { passedCount: 0, passedWords: [] };
  }
  
  // 获取该词汇类型的打卡计划
  const typeIdForPlan = typeObj.typeId || typeObj.id;
  currentPlan.value = await wordProgressManager.getPlan(typeIdForPlan);

  // 检查计划状态
  if (currentPlan.value?.status === "paused") {
    isPaused.value = true;
  } else {
    isPaused.value = false;
  }

  // 检查是否有可恢复的本地进度
  updateResumeAvailable();
}

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
async function confirmPlan() {
  planError.value = "";

  if (
    !planWordsPerDay.value ||
    planWordsPerDay.value < 1 ||
    planWordsPerDay.value > 100
  ) {
    planError.value = "请输入1-100之间的数字";
    return;
  }

  // 确保使用数字typeId
  const typeId = Number(currentType.value.id || currentType.value.typeId);
  console.log("即将创建计划，typeId=", typeId, "wordsPerDay=", planWordsPerDay.value);
  
  const plan = await wordProgressManager.createPlan(
    typeId,
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
 * 开始复习
 */
function startReview() {
  const typeIdForReview = currentType.value.id || currentType.value.typeId;
  router.push({ 
    name: "WordReview", 
    params: { typeId: typeIdForReview } 
  }).catch(() => {});
}

/**
 * 开始打卡
 */
function startCheckIn() {
  const typeIdForPractice = currentType.value.id || currentType.value.typeId;
  router.push({
    name: "WordCheckInPractice",
    params: { typeId: typeIdForPractice },
  }).catch(() => {});
}

/**
 * 恢复打卡进度
 */
function resumeCheckIn() {
  const typeIdForPractice = currentType.value.id || currentType.value.typeId;
  router
    .push({
      name: "WordCheckInPractice",
      params: { typeId: typeIdForPractice },
      query: { resume: "1" },
    })
    .catch(() => {});
}

function getCurrentUserId() {
  try {
    const raw = localStorage.getItem("userStore");
    if (raw) {
      const parsed = JSON.parse(raw);
      return parsed?.userInfo?.id || "anon";
    }
  } catch (e) {
    console.warn("读取用户ID失败，使用 anon:", e);
  }
  return "anon";
}

function makeProgressKey(typeId) {
  const uid = getCurrentUserId();
  return `wordCheckInProgress:${uid}:${typeId}`;
}

function updateResumeAvailable() {
  try {
    const typeId = currentType.value?.id || currentType.value?.typeId;
    if (!typeId) {
      resumeAvailable.value = false;
      return;
    }
    const key = makeProgressKey(typeId);
    const raw = localStorage.getItem(key);
    resumeAvailable.value = !!raw;
  } catch (e) {
    console.warn("检查恢复进度失败:", e);
    resumeAvailable.value = false;
  }
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

/**
 * 跳转到设置页面
 */
function gotoSettings() {
  router.push({ name: "Settings" }).catch(() => {});
}

/**
 * 显示学习建议
 */
function showSuggestions() {
  showSuggestionsModal.value = true;
  currentSuggestionIndex.value = 0;
}

/**
 * 下一条建议
 */
function nextSuggestion() {
  if (currentSuggestionIndex.value < suggestionsData.value.length - 1) {
    currentSuggestionIndex.value++;
  }
}

/**
 * 上一条建议
 */
function previousSuggestion() {
  if (currentSuggestionIndex.value > 0) {
    currentSuggestionIndex.value--;
  }
}

/**
 * 显示通知
 */
function showNotifications() {
  alert("通知功能待实现");
}

function gotoAiChat() {
  router.push({ name: "AiChat" }).catch(() => {});
}

function gotoCourse() {
  router.push({ name: "Course" }).catch(() => {});
}
/**
 * 更新本地计划的计算字段
 */
function updateLocalPlanCalculations() {
  if (!currentPlan.value || !currentProgress.value || !currentType.value) return;

  const remainingWords = currentType.value.totalWords - currentProgress.value.passedCount;
  const daysNeeded = Math.ceil(remainingWords / currentPlan.value.wordsPerDay);

  currentPlan.value.remainingWords = remainingWords;
  currentPlan.value.daysNeeded = daysNeeded;
  currentPlan.value.updatedAt = new Date().toISOString();

  // 保存更新后的本地计划
  wordProgressManager.savePlanToLocal(currentType.value.id, currentPlan.value);
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
