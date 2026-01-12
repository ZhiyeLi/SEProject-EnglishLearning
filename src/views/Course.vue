<template>
  <div
    v-cloak
    class="min-h-screen bg-gray-50 flex flex-col"
  >
    <!-- 导航栏 - 复用现有NavBar组件 -->
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

    <!-- 主内容区 -->
    <main class="flex-grow p-6">
      <!-- 原有课程页面内容 -->
      <div class="max-w-7xl mx-auto">
        <!-- 页面标题 - 美化并添加图标 -->
        <h1 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
          <i class="fas fa-graduation-cap text-emerald-500 mr-3 text-4xl"></i>
          <span class="relative">
            课程
            <span class="absolute -bottom-2 left-0 h-1 w-16 bg-gradient-to-r from-emerald-400 to-emerald-600 rounded-full"></span>
          </span>
        </h1>

        <!-- 搜索和筛选区域 - 优化搜索框样式和提示 -->
        <div
          class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-8"
        >
          <div class="flex flex-col md:flex-row gap-4">
            <!-- 搜索框 - 优化交互体验 -->
            <div class="relative flex-grow">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Enter to search"
                class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-base placeholder-gray-400"
                @input="debouncedFilterCourses"
                @focus="showSearchTips = true"
                @blur="handleSearchBlur"
              >
              <i
                class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-lg"
                aria-hidden="true"
              />
              <!-- 清空搜索按钮 -->
              <button
                v-if="searchQuery"
                class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                aria-label="清空搜索"
                @click="clearSearch"
              >
                <i
                  class="fas fa-times"
                  aria-hidden="true"
                />
              </button>
            </div>

            <!-- 标签筛选 -->
            <div class="flex flex-wrap gap-2">
              <button
                v-for="tag in tags"
                :key="tag.value"
                :class="[
                  'px-4 py-2 rounded-lg transition-all text-sm font-medium',
                  activeTag === tag.value
                    ? 'bg-emerald-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200',
                ]"
                :aria-label="`筛选${tag.label}课程`"
                @click="handleTagClick(tag.value)"
              >
                {{ tag.label }}
              </button>
              <button
                :class="[
                  'px-4 py-2 rounded-lg transition-all text-sm font-medium',
                  activeTag === 'all'
                    ? 'bg-emerald-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200',
                ]"
                aria-label="查看全部课程"
                @click="handleTagClick('all')"
              >
                全部
              </button>
            </div>
          </div>

          <!-- 搜索结果统计 -->
          <div
            v-if="searchQuery || activeTag !== 'all'"
            class="mt-3 text-sm text-gray-500"
          >
            找到 {{ filteredCourses.length }} 个相关课程
            <button
              v-if="searchQuery"
              class="ml-2 text-emerald-600 hover:text-emerald-700"
              @click="clearSearch"
            >
              清除搜索
            </button>
          </div>
        </div>

        <!-- 课程列表 - 关键词高亮 -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 课程项 -->
          <div
            v-for="course in filteredCourses"
            :key="course.id"
            class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden transform transition-all duration-300 hover:shadow-md hover:-translate-y-1"
          >
            <div class="flex flex-col md:flex-row">
              <!-- 视频图片 - 优化居中显示 -->
              <div
                class="w-full md:w-1/3 bg-gray-100 flex items-center justify-center p-2"
              >
                <div
                  class="relative w-full aspect-video md:aspect-auto md:h-full"
                >
                  <img
                    :src="course.imageUrl"
                    :alt="course.title"
                    class="absolute inset-0 w-full h-full object-cover object-center"
                  >
                </div>
              </div>

              <!-- 视频信息 - 高亮搜索关键词 -->
              <div class="w-full md:w-2/3 p-5 flex flex-col justify-center">
                <!-- 标签 -->
                <div class="mb-2">
                  <span
                    class="inline-block px-2 py-1 text-xs font-medium rounded-full"
                    :class="getTagClass(course.tag)"
                  >
                    {{ getTagLabel(course.tag) }}
                  </span>
                </div>

                <!-- 标题（高亮关键词） -->
                <h3
                  class="text-xl font-semibold text-gray-800 mb-2 line-clamp-1"
                >
                  <span v-html="highlightKeyword(course.title)" />
                </h3>
                <!-- 简介（高亮关键词） -->
                <p class="text-gray-600 text-base mb-4 flex-grow">
                  <span v-html="highlightKeyword(course.description)" />
                </p>

                <!-- 视频链接 -->
                <a
                  :href="course.videoUrl"
                  target="_blank"
                  class="inline-flex items-center text-emerald-600 hover:text-emerald-700 font-medium transition-colors mt-2"
                  :aria-label="`观看${course.title}视频`"
                >
                  <span>观看视频</span>
                  <i
                    class="fas fa-external-link-alt ml-2 text-sm"
                    aria-hidden="true"
                  />
                </a>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div
          v-if="filteredCourses.length === 0"
          class="text-center py-16 bg-white rounded-xl shadow-sm border border-gray-200"
        >
          <i
            class="fas fa-video text-gray-300 text-6xl mb-4"
            aria-hidden="true"
          />
          <p class="text-gray-500 text-lg">
            没有找到包含「{{ searchQuery }}」的课程，请尝试其他关键词
          </p>
          <button
            class="mt-4 text-emerald-600 hover:text-emerald-700 flex items-center mx-auto"
            @click="clearSearch"
          >
            <i
              class="fas fa-times mr-1"
              aria-hidden="true"
            /> 清除搜索条件
          </button>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <EndBar />

    <!-- 学习建议弹窗 -->
    <teleport to="body">
      <div
        v-if="showSuggestionsModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fadeIn"
        @click="handleSuggestionsBackdropClick"
      >
        <div
          class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl mx-4 max-h-[70vh] overflow-hidden transform transition-all"
          @click.stop
        >
          <!-- 弹窗头部 -->
          <div
            class="px-8 py-6 border-b border-gray-200 bg-gradient-to-r from-emerald-50 to-blue-50"
          >
            <div class="flex justify-between items-center">
              <h2 class="text-2xl font-bold text-gray-900 flex items-center">
                <i class="fas fa-lightbulb text-yellow-500 mr-3" />
                课程学习建议
              </h2>
              <button
                class="text-gray-400 hover:text-gray-600 transition-colors"
                @click="showSuggestionsModal = false"
              >
                <i class="fas fa-times text-2xl" />
              </button>
            </div>
          </div>

          <!-- 弹窗内容 -->
          <div
            class="px-8 py-6 overflow-y-auto"
            style="max-height: calc(70vh - 140px)"
          >
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
          <div
            class="px-8 py-4 border-t border-gray-200 bg-gray-50 flex justify-between items-center"
          >
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
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import NavBar from "@/components/common/NavBar.vue";
import ActionButtons from "@/components/common/ActionButtons.vue";
import EndBar from "@/components/common/EndBar.vue";
import courseImg1 from "@/assets/course/id1.png";
import courseImg2 from "@/assets/course/id2.png";
import courseImg3 from "@/assets/course/id3.png";
import courseImg4 from "@/assets/course/id4.png";
import courseImg5 from "@/assets/course/id5.png";
import courseImg6 from "@/assets/course/id6.png";
// 路由相关
const router = useRouter();

const gotoHome = () => {
  router.push({ name: "Home" }).catch(() => {});
};

const gotoTimeTable = () => {
  router.push({ name: "TimeTable" }).catch(() => {});
};

const gotoWordCheckIn = () => {
  router.push({ name: "WordCheckIn" }).catch(() => {});
};

const gotoAiChat = () => {
  router.push({ name: "AiChat" }).catch(() => {});
};

const gotoSettings = () => {
  router.push({ name: "Settings" }).catch(() => {});
};

// 导航栏项目 - 为课程标签添加图标
const navItems = [
  { label: "首页", onClick: gotoHome, isActive: false },
  { 
    label: "课程", 
    icon: "fas fa-graduation-cap", // 添加毕业帽图标
    isActive: true 
  },
  {
    label: "题库",
    onClick: () => router.push({ name: "QuestionBank" }).catch(() => {}),
  },
  { label: "时间表", onClick: gotoTimeTable },
  { label: "单词打卡", onClick: gotoWordCheckIn, isActive: false },
  { label: "AI伴学", onClick: gotoAiChat, isActive: false },
];

// 标签数据（新增零基础标签样式）
const tags = [
  { label: "小学", value: "primary" },
  { label: "中学", value: "middle" },
  { label: "大学", value: "college" },
  { label: "零基础", value: "none" },
];

// 获取标签显示文本
const getTagLabel = (tagValue) => {
  const tag = tags.find((t) => t.value === tagValue);
  return tag ? tag.label : "";
};

// 获取标签样式（补充零基础标签样式）
const getTagClass = (tagValue) => {
  const styles = {
    primary: "bg-blue-100 text-blue-800",
    middle: "bg-purple-100 text-purple-800",
    college: "bg-green-100 text-green-800",
    none: "bg-orange-100 text-orange-800", // 零基础标签样式
  };
  return styles[tagValue] || "bg-gray-100 text-gray-800";
};

// 课程数据
const courses = ref([
  {
    id: 1,
    title: "零基础系统学英语",
    description:
      "从零开始，外教名师教你系统的学习英语。涵盖发音、词汇、句型基础，适合完全没有英语基础的学习者。",
    imageUrl: courseImg1,
    videoUrl:
      "https://www.bilibili.com/video/BV1Et421u7nq?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "none",
  },
  {
    id: 2,
    title: "中学英语优质公开课",
    description:
      "【12】全国初中英语优质公开课 | 黄佳妍 ｜八年级｜阅读课｜专家点评：程晓堂 张雪莲",
    imageUrl: courseImg2,
    videoUrl:
      "https://www.bilibili.com/video/BV17T411u7jj?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "middle",
  },
  {
    id: 3,
    title: "大学英语四六级考试全套精讲课程",
    description:
      "用最通俗的易懂的方式带你走进英语的世界，不要犹豫，抓紧行动起来， 十天带你打好基础，逐渐走上英语学霸之路。",
    imageUrl: courseImg3,
    videoUrl:
      "https://www.bilibili.com/video/BV1oD4y1N7uH?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "college",
  },

  {
    id: 4,
    title: "幼儿英语启蒙动画",
    description:
      "清华幼儿英语语感启蒙 清华附小英语动画启蒙+1-4年级英语课程 适合零基础宝宝的慢速磨耳朵英语动画片",
    imageUrl: courseImg4,
    videoUrl:
      "https://www.bilibili.com/video/BV1UXq5YWEoT?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "primary",
  },
  {
    id: 5,
    title: "小学生英语对话",
    description: "通过人物对话，深度学习英语",
    imageUrl: courseImg5,
    videoUrl:
      "https://www.bilibili.com/video/BV1yi4y1P7Ng?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "primary",
  },
  {
    id: 6,
    title: "大学四级词汇",
    description:
      "从基础写作规范到高级表达技巧，全面提升大学英语写作能力，适合备考四六级及日常学术写作。",
    imageUrl: courseImg6,
    videoUrl:
      "https://www.bilibili.com/video/BV1Fg411w7Bt?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "college",
  },
]);

// 搜索相关状态
const searchQuery = ref("");
const activeTag = ref("all");
const showSearchTips = ref(false);
// 处理搜索框失焦：延迟隐藏提示
const handleSearchBlur = () => {
  window.setTimeout(() => {
    showSearchTips.value = false;
  }, 200);
};

// 🔥 1. 防抖优化：避免输入时频繁触发筛选（延迟300ms）
const debouncedFilterCourses = ref(() => {
  let timeout;
  return (e) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => {
      // 自动去除首尾空格和多余标点
      searchQuery.value = e.target.value
        .trim()
        .replace(/[^\u4e00-\u9fa5a-zA-Z0-9\s]/g, "");
    }, 300);
  };
});

// 🔥 2. 优化筛选逻辑：支持关键词模糊匹配+结果排序
const filteredCourses = computed(() => {
  const query = searchQuery.value.toLowerCase().trim();
  const tag = activeTag.value;

  return courses.value
    .filter((course) => {
      // 标签筛选
      const tagMatch = tag === "all" || course.tag === tag;
      if (!query) return tagMatch;

      // 关键词匹配：标题+简介+标签（支持部分匹配）
      const titleMatch = course.title.toLowerCase().includes(query);
      const descMatch = course.description.toLowerCase().includes(query);
      const tagLabelMatch = getTagLabel(course.tag)
        .toLowerCase()
        .includes(query);

      return tagMatch && (titleMatch || descMatch || tagLabelMatch);
    })
    .sort((a, b) => {
      // 🔥 排序优化：按匹配度排序（标题匹配 > 简介匹配 > 标签匹配）
      if (!query) return 0;

      const aTitleMatch = a.title.toLowerCase().includes(query) ? 2 : 0;
      const aDescMatch = a.description.toLowerCase().includes(query) ? 1 : 0;
      const aTotal = aTitleMatch + aDescMatch;

      const bTitleMatch = b.title.toLowerCase().includes(query) ? 2 : 0;
      const bDescMatch = b.description.toLowerCase().includes(query) ? 1 : 0;
      const bTotal = bTitleMatch + bDescMatch;

      return bTotal - aTotal; // 匹配度高的排在前面
    });
});

// 🔥 3. 关键词高亮功能：在标题和简介中高亮显示搜索关键词
const highlightKeyword = (text) => {
  const query = searchQuery.value.trim();
  if (!query) return text;

  // 构建正则表达式，忽略大小写
  const regex = new RegExp(`(${query})`, "gi");
  // 用span标签包裹关键词，添加高亮样式
  return text.replace(
    regex,
    '<span class="bg-yellow-100 text-yellow-800 px-1 rounded"> $1 </span>'
  );
};

// 处理标签点击
const handleTagClick = (tagValue) => {
  activeTag.value = tagValue;
};

// 清空搜索
const clearSearch = () => {
  searchQuery.value = "";
  showSearchTips.value = false;
};

// 组件挂载时初始化防抖函数
onMounted(() => {
  debouncedFilterCourses.value = debouncedFilterCourses.value();
});

// ===== 学习建议弹窗相关逻辑 =====
const showSuggestionsModal = ref(false);
const currentSuggestionIndex = ref(0);
// 课程相关的学习建议数据
const suggestionsData = ref([
  {
    title: "根据水平选择合适课程",
    content:
      "不同阶段的英语学习重点不同，选择匹配自己水平的课程能事半功倍：\n\n• 零基础：重点学习发音、基础词汇和简单句型\n• 小学阶段：注重听说训练和兴趣培养\n• 中学阶段：强化语法和阅读理解能力\n• 大学阶段：聚焦四六级考试技巧和实用英语\n\n建议先完成基础课程，再逐步进阶学习。",
    tags: ["课程选择", "学习阶段", "基础优先"],
  },
  {
    title: "高效观看课程视频的方法",
    content:
      "单纯观看视频效果有限，结合以下方法能提升学习效率：\n\n• 提前预习：了解课程主题和重点词汇\n• 边看边记：记录关键知识点和不懂的地方\n• 暂停练习：重要知识点暂停视频，自己尝试复述\n• 及时复习：看完视频后24小时内复习笔记\n• 实际应用：将学到的内容用在日常对话或写作中\n\n每周回顾一次所学内容，强化记忆效果。",
    tags: ["视频学习", "学习效率", "复习技巧"],
  },
  {
    title: "零基础学习者的学习节奏",
    content:
      "零基础学习英语需要循序渐进，不要急于求成：\n\n• 每日学习时间：建议30-60分钟，避免疲劳\n• 学习频率：每天坚持比周末集中学习效果好\n• 重点掌握：26个字母→音标→基础词汇→简单句型\n• 辅助工具：利用动画和儿歌培养语感\n• 心态调整：接受初期的不熟练，多听多说是关键\n\n坚持3个月，你会看到明显的进步！",
    tags: ["零基础", "学习节奏", "心态调整"],
  },
  {
    title: "结合课程和单词打卡效果更佳",
    content:
      "课程学习和单词打卡是相辅相成的：\n\n• 课前：通过单词打卡预习课程相关词汇\n• 课中：结合课程内容理解单词用法\n• 课后：复习当天课程中的重点单词\n• 定期：将课程中学到的句型和单词结合练习\n\n建议每天先完成单词打卡，再观看对应水平的课程视频。",
    tags: ["单词打卡", "课程结合", "综合学习"],
  },
  {
    title: "利用碎片时间复习课程内容",
    content:
      "课程内容需要反复复习才能掌握：\n\n• 通勤时间：回顾课程笔记或重点单词\n• 休息间隙：观看课程片段，强化记忆\n• 睡前10分钟：总结当天学到的知识点\n• 周末：完整复习本周所学课程\n\n我们的课程支持倍速播放，适合碎片时间快速复习。",
    tags: ["碎片时间", "复习方法", "课程复习"],
  },
]);

// 下一条建议
const nextSuggestion = () => {
  if (currentSuggestionIndex.value < suggestionsData.value.length - 1) {
    currentSuggestionIndex.value++;
  }
};

// 上一条建议
const previousSuggestion = () => {
  if (currentSuggestionIndex.value > 0) {
    currentSuggestionIndex.value--;
  }
};

// 点击弹窗背景关闭
const handleSuggestionsBackdropClick = () => {
  showSuggestionsModal.value = false;
  currentSuggestionIndex.value = 0;
};
</script>

<style scoped>
/* 修复v-cloak样式，确保初始隐藏未渲染完成的内容 */
[v-cloak] {
  display: none !important;
}

/* 保持与项目其他页面一致的滚动条样式 */
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

/* 动画效果 */
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

/* 课程项动画 */
.grid > div {
  animation: fadeIn 0.3s ease-out forwards;
}

/* 错开动画时间 */
.grid > div:nth-child(2n) {
  animation-delay: 0.1s;
}

.grid > div:nth-child(3n) {
  animation-delay: 0.2s;
}

/* 关键词高亮样式优化 */
.bg-yellow-100 {
  background-color: #fef3c7 !important;
}

.text-yellow-800 {
  color: #78350f !important;
}

/* 标题下划线样式优化 */
h1 span.relative {
  display: inline-block;
  padding-bottom: 8px;
}

/* 弹窗动画 */
.animate-fadeIn {
  animation: fadeIn 0.3s ease-out;
}
</style>