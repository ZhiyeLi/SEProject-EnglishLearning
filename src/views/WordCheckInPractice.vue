<template>
  <div class="min-h-screen bg-gradient-to-br from-emerald-50 to-teal-50 flex flex-col">
    <!-- 导航栏 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <ActionButtons
          @suggestions="showSuggestionsModal = true"
          @settings="gotoSettings"
          @home="goHome"
          @notifications="() => {}"
        />
      </template>
    </NavBar>

    <!-- 主内容区 -->
    <main class="flex-grow flex flex-col items-center justify-center p-6">
      <!-- 进度条信息 -->
      <div class="w-full max-w-4xl mb-8">
        <div class="flex items-center justify-between mb-4">
          <div class="text-center flex-1">
            <p class="text-sm text-gray-600 mb-2">
              打卡进度
            </p>
            <p class="text-3xl font-bold text-emerald-600">
              {{ currentIndex + 1 }}<span class="text-xl text-gray-500"> / {{ totalWords }}</span>
            </p>
          </div>
          <div class="flex-1 mx-4">
            <div class="w-full bg-gray-300 rounded-full h-4 overflow-hidden">
              <div
                class="bg-gradient-to-r from-emerald-400 to-teal-500 h-4 rounded-full transition-all duration-500"
                :style="{ width: progressPercentage + '%' }"
              />
            </div>
            <p class="text-center text-sm text-gray-600 mt-2">
              {{ progressPercentage }}%
            </p>
          </div>
        </div>
      </div>

      <!-- 单词卡片 -->
      <div
        v-if="currentWord"
        class="w-full max-w-2xl"
      >
        <div class="bg-white rounded-2xl shadow-2xl p-12 mb-8 animate-fadeIn">
          <!-- 单词和音标 -->
          <div class="text-center mb-8">
            <p class="text-6xl font-bold text-emerald-600 mb-4">
              {{ currentWord.word }}
            </p>
            <p class="text-2xl text-gray-600 mb-6 font-mono">
              {{ currentWord.phonetic }}
            </p>
            <div class="flex justify-center gap-4 mb-6">
              <button
                class="px-6 py-2 bg-emerald-100 text-emerald-700 rounded-lg hover:bg-emerald-200 transition-all flex items-center gap-2"
                @click="playAudio"
              >
                <i class="fas fa-volume-up" />
                发音
              </button>
            </div>
          </div>

          <!-- 分隔线 -->
          <div class="border-t-2 border-gray-200 my-8" />

          <!-- 词性和定义 -->
          <div class="space-y-4">
            <div class="flex items-start gap-4">
              <span class="inline-block px-4 py-2 bg-blue-100 text-blue-700 rounded-lg font-semibold min-w-fit">
                {{ currentWord.partOfSpeech || '词' }}
              </span>
              <p class="text-xl text-gray-800 leading-relaxed pt-2">
                {{ currentWord.definition || currentWord.translation }}
              </p>
            </div>

            <!-- 例句已隐藏（不再显示） -->
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="space-y-4">
          <div class="flex gap-4 justify-center flex-wrap">
            <button
              :disabled="currentIndex === 0"
              class="flex items-center px-8 py-3 rounded-lg font-semibold transition-all"
              :class="
                currentIndex === 0
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-blue-500 text-white hover:bg-blue-600 transform hover:-translate-y-1 shadow-md hover:shadow-lg'
              "
              @click="previousWord"
            >
              <i class="fas fa-arrow-left mr-2" />
              上一个
            </button>

            <button
              class="flex items-center px-8 py-3 bg-gradient-to-r from-emerald-500 to-teal-600 text-white rounded-lg hover:shadow-lg font-bold text-lg transition-all transform hover:-translate-y-1"
              @click="markAsCheckedIn"
            >
              <i class="fas fa-check mr-2" />
              标记为已打卡
            </button>

            <button
              :disabled="isNextDisabled"
              class="flex items-center px-8 py-3 rounded-lg font-semibold transition-all"
              :class="
                isNextDisabled
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-blue-500 text-white hover:bg-blue-600 transform hover:-translate-y-1 shadow-md hover:shadow-lg'
              "
              @click="nextWord"
            >
              下一个
              <i class="fas fa-arrow-right ml-2" />
            </button>
          </div>

          <div class="flex justify-center">
            <button
              class="px-6 py-4 bg-orange-500 text-white rounded-lg hover:bg-orange-600 hover:shadow-lg font-bold transition-all transform hover:-translate-y-1"
              @click="pauseAndExit"
            >
              <i class="fas fa-pause mr-2" />
              暂停退出
            </button>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div
        v-else
        class="text-center"
      >
        <i class="fas fa-spinner fa-spin text-4xl text-emerald-600 mb-4" />
        <p class="text-xl text-gray-600">
          加载中...
        </p>
      </div>
    </main>

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

        <div class="mb-6">
          <div class="space-y-4">
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
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { wordProgressManager } from "@/utils/wordData.js";
import NavBar from "@/components/common/NavBar.vue";
import ActionButtons from "@/components/common/ActionButtons.vue";
import EndBar from "@/components/common/EndBar.vue";

const router = useRouter();
const route = useRoute();

// 数据
const words = ref([]);
const currentIndex = ref(0);
const typeId = ref(null);
const totalWords = ref(0);
const showSuggestionsModal = ref(false);
const currentSuggestionIndex = ref(0);
const passedWordIds = ref([]);
const resumedFromStorage = ref(false);

// 导航项
const navItems = ref([
  { label: "首页", onClick: goHome, isActive: false },
  { label: "课程", onClick: () => router.push({ name: "Course" }).catch(() => {}) },
  { label: "题库", onClick: () => router.push({ name: "QuestionBank" }).catch(() => {}) },
  { label: "时间表", onClick: () => router.push({ name: "TimeTable" }).catch(() => {}) },
  { label: "单词打卡", onClick: () => {}, isActive: true },
]);

// 学习建议数据
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

// 计算属性
const currentWord = computed(() => {
  if (words.value.length === 0) return null;
  return words.value[currentIndex.value];
});

const progressPercentage = computed(() => {
  if (totalWords.value === 0) return 0;
  return Math.round(((currentIndex.value + 1) / totalWords.value) * 100);
});

const passedWordIdSet = computed(() => {
  return new Set(passedWordIds.value.map(String));
});

const isCurrentWordPassed = computed(() => {
  if (!currentWord.value) return false;
  return passedWordIdSet.value.has(String(currentWord.value.wordId));
});

const isNextDisabled = computed(() => {
  return currentIndex.value === words.value.length - 1 || !isCurrentWordPassed.value;
});

// 方法
function goHome() {
  router.push({ name: "Home" }).catch(() => {});
}

function gotoSettings() {
  router.push({ name: "Settings" }).catch(() => {});
}

function nextWord() {
  if (currentIndex.value < words.value.length - 1 && !isNextDisabled.value) {
    currentIndex.value++;
    // 边走边存，降低丢失风险
    saveProgress();
  }
}

function previousWord() {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
}

async function markAsCheckedIn() {
  if (!currentWord.value) return;

  try {
    // 标记当前单词为已打卡
    await wordProgressManager.markWordAsPassed(
      typeId.value,
      currentWord.value.wordId
    );

    const progress = await wordProgressManager.getTypeProgress(typeId.value);
    const latestPassed = progress.passedWords || [];
    passedWordIds.value = [...latestPassed];
    const passedSet = new Set(latestPassed.map(String));

    // 如果本次任务中的所有单词都已被打卡，则视为完成
    const allPassed = words.value.every((w) => passedSet.has(String(w.wordId)));
    if (allPassed) {
      // 完成后清除本地进度
      clearProgress();
      // 注意：不再调用API以避免token过期导致的跳转，计划状态将在下次登录后同步
      alert(`恭喜！你已经完成了今天的${totalWords.value}个单词打卡！`);
      router
        .push({ name: "WordCheckIn", params: { typeId: typeId.value } })
        .catch(() => {});
      return;
    }

    nextWord();
  } catch (error) {
    console.error("标记单词失败:", error);
    alert("标记失败，请重试");
  }
}

async function pauseAndExit() {
  try {
    // 保存当前练习进度到本地
    saveProgress();
    // 注意：不再调用API以避免token过期导致的跳转，本地进度已保存
    // 返回到单词打卡页面
    router.push({ name: "WordCheckIn", params: { typeId: typeId.value } }).catch(() => {});
  } catch (error) {
    console.error("退出失败:", error);
    // 即使保存失败，也尝试跳转
    router.push({ name: "WordCheckIn", params: { typeId: typeId.value } }).catch(() => {});
  }
}

function playAudio() {
  // 这里可以集成文本转语音功能
  const utterance = new SpeechSynthesisUtterance(currentWord.value.word);
  utterance.lang = "en-US";
  speechSynthesis.speak(utterance);
}

function previousSuggestion() {
  if (currentSuggestionIndex.value > 0) {
    currentSuggestionIndex.value--;
  }
}

function nextSuggestion() {
  if (currentSuggestionIndex.value < suggestionsData.value.length - 1) {
    currentSuggestionIndex.value++;
  }
}

// 初始化
onMounted(async () => {
  try {
    // 从路由参数或查询参数获取typeId
    typeId.value = route.params.typeId || route.query.typeId;

    if (!typeId.value) {
      // 如果没有typeId，尝试从后端获取选中的类型
      const selectedType = await wordProgressManager.getSelectedType();
      typeId.value = selectedType?.typeId || selectedType?.id;
    }

    if (!typeId.value) {
      alert("请先选择单词类型");
      router.push({ name: "WordCheckIn" }).catch(() => {});
      return;
    }

    typeId.value = Number(typeId.value);

    // 先获取该词汇类型的打卡计划，获取每日打卡数
    const plan = await wordProgressManager.getPlan(typeId.value);
    
    if (!plan) {
      alert("请先制订打卡计划");
      router.push({ name: "WordCheckIn", params: { typeId: typeId.value } }).catch(() => {});
      return;
    }

    // 如果请求为恢复进度，优先尝试从本地读取
    if (String(route.query.resume) === "1" && loadProgress()) {
      resumedFromStorage.value = true;
    } else {
      const wordsPerDay = plan.wordsPerDay;
      // 从后端获取未打卡的单词列表
      const allUnpassedWords = await wordProgressManager.getUnpassedWords(typeId.value);
      if (allUnpassedWords.length === 0) {
        alert("没有未打卡的单词");
        router.push({ name: "WordCheckIn", params: { typeId: typeId.value } }).catch(() => {});
        return;
      }
      // 只取每日打卡数量的单词
      words.value = allUnpassedWords.slice(0, wordsPerDay);
      totalWords.value = words.value.length;
      const progress = await wordProgressManager.getTypeProgress(typeId.value);
      passedWordIds.value = [...(progress?.passedWords || [])];
      // 刚开始练习也保存一次，便于恢复
      saveProgress();
    }
  } catch (error) {
    console.error("初始化失败:", error);
    alert("初始化失败，请重试");
    router.push({ name: "WordCheckIn" }).catch(() => {});
  }
});

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

function saveProgress() {
  try {
    const key = makeProgressKey(typeId.value);
    const payload = {
      typeId: typeId.value,
      currentIndex: currentIndex.value,
      totalWords: totalWords.value,
      passedWordIds: [...passedWordIds.value],
      words: words.value.map((w) => ({
        wordId: w.wordId,
        word: w.word,
        phonetic: w.phonetic,
        translation: w.translation,
        partOfSpeech: w.partOfSpeech,
        definition: w.definition,
      })),
      ts: Date.now(),
    };
    localStorage.setItem(key, JSON.stringify(payload));
  } catch (e) {
    console.warn("保存本地进度失败:", e);
  }
}

function loadProgress() {
  try {
    const key = makeProgressKey(typeId.value);
    const raw = localStorage.getItem(key);
    if (!raw) return false;
    const data = JSON.parse(raw);
    if (!data || Number(data.typeId) !== Number(typeId.value)) return false;
    words.value = Array.isArray(data.words) ? data.words : [];
    totalWords.value = Number(data.totalWords) || words.value.length || 0;
    currentIndex.value = Number(data.currentIndex) || 0;
    passedWordIds.value = Array.isArray(data.passedWordIds) ? data.passedWordIds : [];
    return words.value.length > 0;
  } catch (e) {
    console.warn("加载本地进度失败:", e);
    return false;
  }
}

function clearProgress() {
  try {
    const key = makeProgressKey(typeId.value);
    localStorage.removeItem(key);
  } catch (e) {
    console.warn("清除本地进度失败:", e);
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
  animation: fadeIn 0.3s ease-out;
}
</style>
