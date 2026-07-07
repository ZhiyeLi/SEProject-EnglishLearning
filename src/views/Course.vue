<template>
  <div v-cloak class="min-h-screen bg-gray-50 flex flex-col">
    <NavBar :nav-items="navItems">
      <template #actions>
        <ActionButtons
          @suggestions="showSuggestionsModal = true"
          @settings="gotoSettings"
          @home="gotoHome"
          @notifications="() => {}"
        />
      </template>
    </NavBar>

    <main class="flex-grow p-6">
      <div class="max-w-7xl mx-auto">
        <!-- 页面标题 -->
        <h1 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
          <i class="fas fa-graduation-cap text-emerald-500 mr-3 text-4xl" />
          <span class="relative">
            课程
            <span class="absolute -bottom-2 left-0 h-1 w-16 bg-gradient-to-r from-emerald-400 to-emerald-600 rounded-full" />
          </span>
        </h1>

        <!-- 搜索和筛选 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-8">
          <div class="flex flex-col md:flex-row gap-4">
            <div class="relative flex-grow">
              <input
                type="text"
                placeholder="搜索课程..."
                class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                @input="onSearchInput"
              />
              <i class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-lg" />
              <button
                v-if="searchQuery"
                class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                @click="clearSearch"
              ><i class="fas fa-times" /></button>
            </div>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="tag in tags" :key="tag.value"
                :class="['px-4 py-2 rounded-lg transition-all text-sm font-medium', activeTag === tag.value ? 'bg-emerald-500 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200']"
                @click="handleTagClick(tag.value)"
              >{{ tag.label }}</button>
              <button
                :class="['px-4 py-2 rounded-lg transition-all text-sm font-medium', activeTag === 'all' ? 'bg-emerald-500 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200']"
                @click="handleTagClick('all')"
              >全部</button>
              <button
                :class="['px-4 py-2 rounded-lg transition-all text-sm font-medium ml-2', showFavoritesOnly ? 'bg-yellow-500 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200']"
                @click="showFavoritesOnly = !showFavoritesOnly"
              >★ 我的收藏</button>
            </div>
          </div>
          <div v-if="searchQuery || activeTag !== 'all' || showFavoritesOnly" class="mt-3 text-sm text-gray-500">
            找到 {{ filteredCourses.length }} 个课程
            <button v-if="searchQuery" class="ml-2 text-emerald-600 hover:text-emerald-700" @click="clearSearch">清除搜索</button>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="text-center py-16">
          <i class="fas fa-spinner fa-spin text-4xl text-emerald-400" />
          <p class="text-gray-500 mt-4">加载中...</p>
        </div>

        <!-- 错误状态 -->
        <div v-else-if="error" class="text-center py-16 bg-white rounded-xl shadow-sm border border-gray-200">
          <i class="fas fa-exclamation-triangle text-red-300 text-6xl mb-4" />
          <p class="text-gray-500 text-lg">{{ error }}</p>
          <button class="mt-4 text-emerald-600 hover:text-emerald-700" @click="loadCourses">重试</button>
        </div>

        <!-- 课程列表 -->
        <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <CourseCard
            v-for="course in filteredCourses"
            :key="course.id"
            :course="course"
            :search-query="searchQuery"
            @click="openPlayer"
            @favorite="onFavorite"
          />
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && !error && filteredCourses.length === 0" class="text-center py-16 bg-white rounded-xl shadow-sm border border-gray-200">
          <i class="fas fa-video text-gray-300 text-6xl mb-4" />
          <p class="text-gray-500 text-lg">没有找到匹配的课程</p>
          <button class="mt-4 text-emerald-600 hover:text-emerald-700" @click="clearSearch(); activeTag = 'all'; showFavoritesOnly = false;">
            清除所有筛选
          </button>
        </div>
      </div>
    </main>

    <EndBar />

    <!-- 播放弹窗 -->
    <CoursePlayerModal
      :course="currentPlayerCourse"
      :visible="showPlayer"
      @close="closePlayer"
      @complete="onComplete"
      @favorite="onFavorite"
    />

    <!-- 学习建议弹窗 -->
    <SuggestionModal
      :visible="showSuggestionsModal"
      title="课程学习建议"
      :items="suggestionsData"
      @close="showSuggestionsModal = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import NavBar from "@/components/common/NavBar.vue";
import ActionButtons from "@/components/common/ActionButtons.vue";
import EndBar from "@/components/common/EndBar.vue";
import CourseCard from "@/components/course/CourseCard.vue";
import CoursePlayerModal from "@/components/course/CoursePlayerModal.vue";
import SuggestionModal from "@/components/common/SuggestionModal.vue";
import { courseApi } from "@/api/course";

const router = useRouter();

// 路由跳转
const gotoHome = () => router.push({ name: "Home" }).catch(() => {});
const gotoTimeTable = () => router.push({ name: "TimeTable" }).catch(() => {});
const gotoWordCheckIn = () => router.push({ name: "WordCheckIn" }).catch(() => {});
const gotoAiChat = () => router.push({ name: "AiChat" }).catch(() => {});
const gotoSettings = () => router.push({ name: "Settings" }).catch(() => {});

const navItems = [
  { label: "首页", onClick: gotoHome, isActive: false },
  { label: "课程", isActive: true },
  { label: "题库", onClick: () => router.push({ name: "QuestionBank" }).catch(() => {}) },
  { label: "时间表", onClick: gotoTimeTable },
  { label: "单词打卡", onClick: gotoWordCheckIn, isActive: false },
  { label: "AI伴学", onClick: gotoAiChat, isActive: false },
];

// ===== 状态 =====
const courses = ref([]);
const loading = ref(true);
const error = ref("");
const searchQuery = ref("");
const activeTag = ref("all");
const showFavoritesOnly = ref(false);
const currentPlayerCourse = ref(null);
const showPlayer = ref(false);
const showSuggestionsModal = ref(false);

const tags = [
  { label: "小学", value: "primary" },
  { label: "中学", value: "middle" },
  { label: "大学", value: "college" },
  { label: "零基础", value: "none" },
];

// ===== 数据加载 =====
async function loadCourses() {
  loading.value = true;
  error.value = "";
  try {
    const res = await courseApi.getCourses({ size: 100 });
    if (res.code === 200) {
      courses.value = res.data?.content || res.data || [];
    } else {
      error.value = res.message || "加载失败";
    }
  } catch (e) {
    error.value = "网络错误";
  } finally {
    loading.value = false;
  }
}

// ===== 搜索和筛选 =====
function debounce(fn, delay) {
  let timer;
  return function (...args) {
    clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}

const onSearchInput = debounce((e) => {
  searchQuery.value = e.target.value.trim();
}, 300);

const filteredCourses = computed(() => {
  let list = courses.value;

  if (activeTag.value !== "all") {
    list = list.filter((c) => c.tag === activeTag.value);
  }

  if (showFavoritesOnly.value) {
    list = list.filter((c) => c.favorite);
  }

  const q = searchQuery.value.toLowerCase();
  if (q) {
    list = list.filter((c) =>
      (c.name && c.name.toLowerCase().includes(q)) ||
      (c.description && c.description.toLowerCase().includes(q))
    );
  }

  return list;
});

function handleTagClick(tagValue) {
  activeTag.value = tagValue;
}

function clearSearch() {
  searchQuery.value = "";
}

// ===== 播放弹窗 =====
function openPlayer(courseId) {
  const c = courses.value.find((c) => c.id === courseId);
  if (c) {
    currentPlayerCourse.value = c;
    showPlayer.value = true;
    courseApi.updateProgress(courseId, "learning").catch(() => {});
    c.status = "learning";
  }
}

function closePlayer() {
  showPlayer.value = false;
  currentPlayerCourse.value = null;
}

function onComplete(courseId) {
  courseApi.markComplete(courseId).then(() => {
    const c = courses.value.find((c) => c.id === courseId);
    if (c) c.status = "completed";
  }).catch(() => {});
}

// ===== 收藏 =====
function onFavorite(courseId) {
  courseApi.toggleFavorite(courseId).then((res) => {
    if (res.code === 200) {
      const c = courses.value.find((c) => c.id === courseId);
      if (c) c.favorite = res.data?.favorite ?? !c.favorite;
    }
  }).catch(() => {});
}

// ===== 学习建议 =====
const suggestionsData = [
  {
    title: "根据水平选择合适课程",
    content: "不同阶段的英语学习重点不同，选择匹配自己水平的课程能事半功倍：\n\n• 零基础：重点学习发音、基础词汇和简单句型\n• 小学阶段：注重听说训练和兴趣培养\n• 中学阶段：强化语法和阅读理解能力\n• 大学阶段：聚焦四六级考试技巧和实用英语\n\n建议先完成基础课程，再逐步进阶学习。",
    tags: ["课程选择", "学习阶段", "基础优先"],
  },
  {
    title: "高效观看课程视频的方法",
    content: "单纯观看视频效果有限，结合以下方法能提升学习效率：\n\n• 提前预习：了解课程主题和重点词汇\n• 边看边记：记录关键知识点和不懂的地方\n• 暂停练习：重要知识点暂停视频，自己尝试复述\n• 及时复习：看完视频后24小时内复习笔记\n• 实际应用：将学到的内容用在日常对话或写作中\n\n每周回顾一次所学内容，强化记忆效果。",
    tags: ["视频学习", "学习效率", "复习技巧"],
  },
  {
    title: "零基础学习者的学习节奏",
    content: "零基础学习英语需要循序渐进，不要急于求成：\n\n• 每日学习时间：建议30-60分钟，避免疲劳\n• 学习频率：每天坚持比周末集中学习效果好\n• 重点掌握：26个字母→音标→基础词汇→简单句型\n• 辅助工具：利用动画和儿歌培养语感\n• 心态调整：接受初期的不熟练，多听多说是关键\n\n坚持3个月，你会看到明显的进步！",
    tags: ["零基础", "学习节奏", "心态调整"],
  },
  {
    title: "结合课程和单词打卡效果更佳",
    content: "课程学习和单词打卡是相辅相成的：\n\n• 课前：通过单词打卡预习课程相关词汇\n• 课中：结合课程内容理解单词用法\n• 课后：复习当天课程中的重点单词\n• 定期：将课程中学到的句型和单词结合练习\n\n建议每天先完成单词打卡，再观看对应水平的课程视频。",
    tags: ["单词打卡", "课程结合", "综合学习"],
  },
  {
    title: "利用碎片时间复习课程内容",
    content: "课程内容需要反复复习才能掌握：\n\n• 通勤时间：回顾课程笔记或重点单词\n• 休息间隙：观看课程片段，强化记忆\n• 睡前10分钟：总结当天学到的知识点\n• 周末：完整复习本周所学课程",
    tags: ["碎片时间", "复习方法", "课程复习"],
  },
];

onMounted(() => {
  loadCourses();
});
</script>

<style scoped>
[v-cloak] {
  display: none !important;
}

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

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.grid > div {
  animation: fadeIn 0.3s ease-out forwards;
}

.grid > div:nth-child(2n) {
  animation-delay: 0.1s;
}

.grid > div:nth-child(3n) {
  animation-delay: 0.2s;
}

.bg-yellow-100 {
  background-color: #fef3c7 !important;
}

.text-yellow-800 {
  color: #78350f !important;
}

h1 span.relative {
  display: inline-block;
  padding-bottom: 8px;
}

.animate-fadeIn {
  animation: fadeIn 0.3s ease-out;
}

@media (max-width: 768px) {
  .md\:w-96 {
    width: 100%;
    max-height: 40vh;
  }
}
</style>
