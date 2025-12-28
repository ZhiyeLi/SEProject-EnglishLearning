<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-cyan-50 flex flex-col">
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
              复习进度
            </p>
            <p class="text-3xl font-bold text-blue-600">
              {{ currentIndex + 1 }}<span class="text-xl text-gray-500"> / {{ totalWords }}</span>
            </p>
          </div>
          <div class="flex-1 mx-4">
            <div class="w-full bg-gray-300 rounded-full h-4 overflow-hidden">
              <div
                class="bg-gradient-to-r from-blue-400 to-cyan-500 h-4 rounded-full transition-all duration-500"
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
          <!-- 填空输入 -->
          <div class="text-center mb-8">
            <div class="mb-6">
              <input
                v-model="userInput"
                type="text"
                placeholder="请输入单词..."
                class="text-4xl font-bold text-center border-b-4 border-blue-300 focus:border-blue-500 outline-none bg-transparent w-full max-w-md"
                @keyup.enter="checkAnswer"
              >
            </div>
            <div v-if="isCorrect !== null" class="mb-4">
              <p v-if="isCorrect" class="text-green-600 font-semibold">正确！</p>
              <p v-else class="text-red-600 font-semibold">{{ feedbackMessage || '不正确，请重新拼写。' }}</p>
            </div>
            <div class="flex justify-center gap-4 mb-6">
              <button
                v-if="!showAnswer"
                class="px-6 py-2 bg-blue-100 text-blue-700 rounded-lg hover:bg-blue-200 transition-all flex items-center gap-2"
                @click="showAnswer = true"
              >
                <i class="fas fa-eye" />
                查看答案
              </button>
              <button
                v-else
                class="px-6 py-2 bg-emerald-100 text-emerald-700 rounded-lg hover:bg-emerald-200 transition-all flex items-center gap-2"
                @click="playAudio"
              >
                <i class="fas fa-volume-up" />
                发音
              </button>
            </div>
            <!-- 显示答案 -->
            <div v-if="showAnswer" class="mb-6">
              <p class="text-4xl font-bold text-blue-600 mb-2">
                {{ currentWord.word }}
              </p>
              <p class="text-xl text-gray-600 font-mono">
                {{ currentWord.phonetic }}
              </p>
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
                {{ displayDefinition }}
              </p>
            </div>
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
              class="flex items-center px-8 py-3 bg-gradient-to-r from-blue-500 to-cyan-600 text-white rounded-lg hover:shadow-lg font-bold text-lg transition-all transform hover:-translate-y-1"
              @click="confirmAnswer"
            >
              <i class="fas fa-check mr-2" />
              确认
            </button>

            <button
              :disabled="currentIndex === totalWords - 1"
              class="flex items-center px-8 py-3 rounded-lg font-semibold transition-all"
              :class="
                currentIndex === totalWords - 1
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
        <i class="fas fa-spinner fa-spin text-4xl text-blue-600 mb-4" />
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
                <span class="text-blue-600">{{
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
                class="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm"
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
                : 'bg-blue-50 text-blue-700 hover:bg-blue-100'
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
                : 'bg-blue-50 text-blue-700 hover:bg-blue-100'
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
const showAnswer = ref(false);
const userInput = ref("");
const isCorrect = ref(null); // null: 未确认, true: 正确, false: 错误
const feedbackMessage = ref("");
const usedStartIndex = ref(0); // 本次任务起始索引
const wordsPerDayRef = ref(0); // 记录计划数量
const passedWordsTotalRef = ref(0); // 已打卡总数（用于指针推进取模）

// 展示用定义（去重/清洗）
const displayDefinition = computed(() => {
  const text = (currentWord.value?.definition ?? currentWord.value?.translation ?? "").trim();
  if (!text) return "";
  // 以中文分隔符与句号切分，去重后再拼接
  const segments = text.split(/[；;。]/).map(s => s.trim()).filter(Boolean);
  const seen = new Set();
  const unique = [];
  for (const s of segments) {
    if (!seen.has(s)) {
      seen.add(s);
      unique.push(s);
    }
  }
  // 重新拼为带分号的串
  return unique.join("；");
});

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
    title: "复习是记忆的关键",
    content:
      "定期复习已经学过的单词是巩固记忆的重要方式。研究表明，复习可以让单词记忆时间延长数倍。\n\n建议：\n• 每天抽出时间复习已打卡单词\n• 结合填空练习，加深印象\n• 多听发音，熟悉单词读法\n\n坚持复习，你会发现单词记得越来越牢！",
    tags: ["复习", "记忆巩固", "填空练习"],
  },
  {
    title: "主动回忆比被动查看更有效",
    content:
      "在复习时，先尝试自己回忆单词，然后再查看答案。这种主动回忆的方式比直接看答案效果更好。\n\n建议：\n• 先想一想这个定义对应的单词是什么\n• 写下来或说出来\n• 然后再检查是否正确\n\n主动回忆能显著提高学习效果。",
    tags: ["主动回忆", "学习方法", "记忆技巧"],
  },
  {
    title: "利用零碎时间高效复习",
    content:
      "你可以充分利用上下班、等车、休息间隙等零碎时间来复习单词。这些时间虽然不长，但积累起来效果显著。\n\n建议：\n• 使用移动设备随时复习\n• 每次复习少量单词，但要专注\n• 在高峰期巩固之前学过的词汇\n\n每天15-20分钟的有效复习胜过一次性的1小时被动复习。",
    tags: ["时间管理", "碎片化学习", "效率"],
  },
  {
    title: "重视拼写和发音",
    content:
      "单纯记忆单词的中文意思容易遗忘。建议同时关注单词的拼写、发音和用法。\n\n建议：\n• 大声朗读单词，加强发音记忆\n• 多做拼写练习，特别是容易混淆的词\n• 学习单词的衍生词和同义词\n\n这样学习的单词记忆时间会延长3倍以上。",
    tags: ["拼写", "发音", "词汇拓展"],
  },
  {
    title: "制定合理的复习计划",
    content:
      "根据你的学习进度，建议制定合理的复习计划。过多会导致疲劳，过少则影响巩固。\n\n建议：\n• 每天复习50-100个已学单词\n• 结合新学和复习\n• 根据个人情况灵活调整\n\n记住：复习质量永远比数量重要！",
    tags: ["复习计划", "学习进度", "科学学习"],
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

// 方法
function goHome() {
  router.push({ name: "Home" }).catch(() => {});
}

function gotoSettings() {
  router.push({ name: "Settings" }).catch(() => {});
}

function nextWord() {
  if (currentIndex.value < words.value.length - 1) {
    currentIndex.value++;
    resetForNewWord();
  } else {
    // 复习完成
    // 推进指针到下一起点（按本次复习数量推进）
    try {
      const nextRawIndex = usedStartIndex.value + totalWords.value;
      saveReviewStartIndex(typeId.value, nextRawIndex);
    } catch (e) {
      // 忽略指针保存失败
    }
    alert(`恭喜！你已经完成了${totalWords.value}个单词的复习！`);
    router.push({ name: "WordCheckIn", params: { typeId: typeId.value } }).catch(() => {});
  }
}

function confirmAnswer() {
  checkAnswer();
  // 如果正确并且是最后一个，自动结束复习
  if (isCorrect.value && currentIndex.value === totalWords.value - 1) {
    // 稍作停顿，允许播音开始
    setTimeout(() => {
      // 推进指针到下一个起点（循环）
      try {
        const nextRawIndex = usedStartIndex.value + totalWords.value;
        saveReviewStartIndex(typeId.value, nextRawIndex);
      } catch (e) {
        // 忽略指针保存失败
      }
      alert(`恭喜！你已经完成了${totalWords.value}个单词的复习！`);
      router.push({ name: "WordCheckIn", params: { typeId: typeId.value } }).catch(() => {});
    }, 200);
  }
}

function previousWord() {
  if (currentIndex.value > 0) {
    currentIndex.value--;
    resetForNewWord();
  }
}

function resetForNewWord() {
  showAnswer.value = false;
  userInput.value = "";
  isCorrect.value = null;
  feedbackMessage.value = "";
}

function checkAnswer() {
  if (!currentWord.value) return;
  if (userInput.value.toLowerCase().trim() === currentWord.value.word.toLowerCase()) {
    // 正确，可以自动下一个或提示
    isCorrect.value = true;
    showAnswer.value = true;
    playAudio();
  } else {
    // 不正确，可以显示答案或提示
    isCorrect.value = false;
    feedbackMessage.value = "不正确，请重新拼写。";
    showAnswer.value = false;
  }
}

function pauseAndExit() {
  // 将指针推进至当前已浏览进度，避免重启后重复
  try {
    const progressed = usedStartIndex.value + Math.max(currentIndex.value, 0);
    saveReviewStartIndex(typeId.value, progressed);
  } catch(e) {
    // ignore
  }
  router.push({ name: "WordCheckIn", params: { typeId: typeId.value } }).catch(() => {});
}

function playAudio() {
  if (!currentWord.value) return;
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
    // 从路由参数获取typeId
    typeId.value = route.params.typeId;

    if (!typeId.value) {
      alert("请先选择单词类型");
      router.push({ name: "WordCheckIn" }).catch(() => {});
      return;
    }

    typeId.value = Number(typeId.value);

    // 获取该词汇类型的打卡计划，获取每日打卡数
    const plan = await wordProgressManager.getPlan(typeId.value);
    
    if (!plan) {
      alert("请先制订打卡计划");
      router.push({ name: "WordCheckIn", params: { typeId: typeId.value } }).catch(() => {});
      return;
    }

    const wordsPerDay = plan.wordsPerDay;
    wordsPerDayRef.value = Number(wordsPerDay) || 0;

    // 获取已打卡的单词
    const progress = await wordProgressManager.getTypeProgress(typeId.value);
    let passedWordIds = progress?.passedWords || [];
    // 保险去重，避免后端或缓存异常导致重复ID
    passedWordIds = Array.from(new Set(passedWordIds.map((id) => Number(id))).values());
    passedWordsTotalRef.value = passedWordIds.length;

    if (passedWordIds.length === 0) {
      alert("没有已打卡的单词可以复习");
      router.push({ name: "WordCheckIn", params: { typeId: typeId.value } }).catch(() => {});
      return;
    }

    // 计算从本地指针开始的选择列表（循环取 wordsPerDay 个）
    const startIndex = loadReviewStartIndex(typeId.value) % Math.max(passedWordIds.length, 1);
    usedStartIndex.value = startIndex;
    const pickCount = Math.min(wordsPerDayRef.value > 0 ? wordsPerDayRef.value : passedWordIds.length, passedWordIds.length);
    const selectedIds = [];
    for (let i = 0; i < pickCount; i++) {
      selectedIds.push(passedWordIds[(startIndex + i) % passedWordIds.length]);
    }

    // 获取单词详情
    const passedWords = [];
    for (const wordId of selectedIds) {
      try {
        const wordDetail = await wordProgressManager.getWordDetail(wordId);
        if (wordDetail) {
          passedWords.push(wordDetail);
        }
      } catch (error) {
        console.error("获取单词详情失败:", error);
      }
    }

    if (passedWords.length === 0) {
      alert("无法加载复习单词");
      router.push({ name: "WordCheckIn", params: { typeId: typeId.value } }).catch(() => {});
      return;
    }

    words.value = passedWords;
    totalWords.value = words.value.length;
  } catch (error) {
    console.error("初始化失败:", error);
    alert("初始化失败，请重试");
    router.push({ name: "WordCheckIn" }).catch(() => {});
  }
});

// —— 本地指针存取 ——
function getCurrentUserId() {
  try {
    const raw = localStorage.getItem("userStore");
    if (raw) {
      const parsed = JSON.parse(raw);
      return parsed?.userInfo?.id || "anon";
    }
  } catch (e) {
    // ignore
  }
  return "anon";
}

function makeReviewKey(typeId) {
  const uid = getCurrentUserId();
  return `wordReviewProgress:${uid}:${typeId}`;
}

function loadReviewStartIndex(typeId) {
  try {
    const key = makeReviewKey(typeId);
    const raw = localStorage.getItem(key);
    if (!raw) return 0;
    const data = JSON.parse(raw);
    const idx = Number(data?.nextStartIndex);
    return Number.isFinite(idx) && idx >= 0 ? idx : 0;
  } catch {
    return 0;
  }
}

function saveReviewStartIndex(typeId, nextStartIndex) {
  try {
    const key = makeReviewKey(typeId);
    const payload = { nextStartIndex: Number(nextStartIndex) || 0, ts: Date.now() };
    localStorage.setItem(key, JSON.stringify(payload));
  } catch {
    // ignore
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