<template>
  <div
    v-cloak
    class="min-h-screen bg-gray-50 flex flex-col"
  >
    <!-- 导航栏 - 复用现有NavBar组件 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <button
          class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group"
          aria-label="学习建议"
        >
          <i
            class="fas fa-lightbulb text-lg"
            aria-hidden="true"
          />
          <span class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">学习建议</span>
        </button>
        <button
          class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group"
          aria-label="设置"
        >
          <i
            class="fas fa-cog text-lg"
            aria-hidden="true"
          />
          <span class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">设置</span>
        </button>
        <button
          class="relative ml-2 text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors"
          aria-label="查看通知（3条未读）"
        >
          <i
            class="fas fa-bell text-lg"
            aria-hidden="true"
          />
          <span class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center animate-pulse">3</span>
        </button>
      </template>
    </NavBar>

    <!-- 主内容区 -->
    <main class="flex-grow p-6">
      <div class="max-w-7xl mx-auto">
        <!-- 返回按钮 -->
        <button
          class="text-emerald-600 hover:text-emerald-700 flex items-center transition-colors mb-6"
          aria-label="返回首页"
          @click="gotoHome"
        >
          <i
            class="fas fa-arrow-left mr-2"
            aria-hidden="true"
          />
          <span>返回首页</span>
        </button>

        <!-- 页面标题 -->
        <h1 class="text-3xl font-bold text-gray-800 mb-8">
          英语课程
        </h1>

        <!-- 搜索和筛选区域 - 优化搜索框样式和提示 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-8">
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
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
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
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
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
              <div class="w-full md:w-1/3 bg-gray-100 flex items-center justify-center p-2">
                <div class="relative w-full aspect-video md:aspect-auto md:h-full">
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
                <h3 class="text-xl font-semibold text-gray-800 mb-2 line-clamp-1">
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import NavBar from '@/components/common/NavBar.vue';
import EndBar from '@/components/common/EndBar.vue';
import courseImg1 from '@/assets/course/id1.png';  
import courseImg2 from '@/assets/course/id2.png';
import courseImg3 from '@/assets/course/id3.png';
import courseImg4 from '@/assets/course/id4.png';
import courseImg5 from '@/assets/course/id5.png';
import courseImg6 from '@/assets/course/id6.png';
// 路由相关
const router = useRouter();

const gotoHome = () => {
  router.push({ name: 'Home' }).catch(() => {});
};

const gotoTimeTable = () => {
  router.push({ name: 'TimeTable' }).catch(() => {});
};

const gotoWordCheckIn = () => {
  router.push({ name: 'WordCheckIn' }).catch(() => {});
};

const gotoAiChat = () => {
  router.push({ name: 'AiChat' }).catch(() => {});
};

// 导航栏项目
const navItems = [
  { label: "首页", onClick: gotoHome, isActive: false },
  { label: "课程", isActive: true },
  { label: "题库", path: "#" },
  { label: "时间表", onClick: gotoTimeTable },
  { label: "单词打卡", onClick: gotoWordCheckIn, isActive: false },
  { label: "AI伴学", onClick: gotoAiChat, isActive: false },
];

// 标签数据（新增零基础标签样式）
const tags = [
  { label: '小学', value: 'primary' },
  { label: '中学', value: 'middle' },
  { label: '大学', value: 'college' },
  { label: '零基础', value: 'none'}
];

// 获取标签显示文本
const getTagLabel = (tagValue) => {
  const tag = tags.find(t => t.value === tagValue);
  return tag ? tag.label : '';
};

// 获取标签样式（补充零基础标签样式）
const getTagClass = (tagValue) => {
  const styles = {
    primary: 'bg-blue-100 text-blue-800',
    middle: 'bg-purple-100 text-purple-800',
    college: 'bg-green-100 text-green-800',
    none: 'bg-orange-100 text-orange-800' // 零基础标签样式
  };
  return styles[tagValue] || 'bg-gray-100 text-gray-800';
};

// 课程数据
const courses = ref([
  {
    id: 1,
    title: "零基础系统学英语",
    description: "从零开始，外教名师教你系统的学习英语。涵盖发音、词汇、句型基础，适合完全没有英语基础的学习者。",
    imageUrl: courseImg1,
    videoUrl: "https://www.bilibili.com/video/BV1Et421u7nq?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "none"
  },
  {
    id: 2,
    title: "中学英语优质公开课",
    description: "【12】全国初中英语优质公开课 | 黄佳妍 ｜八年级｜阅读课｜专家点评：程晓堂 张雪莲",
    imageUrl: courseImg2,
    videoUrl: "https://www.bilibili.com/video/BV17T411u7jj?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "middle"
  },
  {
    id: 3,
    title: "大学英语四六级考试全套精讲课程",
    description: "用最通俗的易懂的方式带你走进英语的世界，不要犹豫，抓紧行动起来， 十天带你打好基础，逐渐走上英语学霸之路。",
    imageUrl: courseImg3,
    videoUrl: "https://www.bilibili.com/video/BV1oD4y1N7uH?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "college"
  },

  {
    id: 4,
    title: "幼儿英语启蒙动画",
    description: "清华幼儿英语语感启蒙 清华附小英语动画启蒙+1-4年级英语课程 适合零基础宝宝的慢速磨耳朵英语动画片",
    imageUrl: courseImg4,
    videoUrl: "https://www.bilibili.com/video/BV1UXq5YWEoT?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "primary"
  },
  {
    id: 5,
    title: "小学生英语对话",
    description: "通过人物对话，深度学习英语",
    imageUrl: courseImg5,
    videoUrl: "https://www.bilibili.com/video/BV1yi4y1P7Ng?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "primary"
  },
  {
    id: 6,
    title: "大学四级词汇",
    description: "从基础写作规范到高级表达技巧，全面提升大学英语写作能力，适合备考四六级及日常学术写作。",
    imageUrl: courseImg6,
    videoUrl: "https://www.bilibili.com/video/BV1Fg411w7Bt?vd_source=2ab0bb504ef7db37f97983a985cddb95",
    tag: "college"
  }
]);

// 搜索相关状态
const searchQuery = ref('');
const activeTag = ref('all');
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
      searchQuery.value = e.target.value.trim().replace(/[^\u4e00-\u9fa5a-zA-Z0-9\s]/g, '');
    }, 300);
  };
});

// 🔥 2. 优化筛选逻辑：支持关键词模糊匹配+结果排序
const filteredCourses = computed(() => {
  const query = searchQuery.value.toLowerCase().trim();
  const tag = activeTag.value;

  return courses.value
    .filter(course => {
      // 标签筛选
      const tagMatch = tag === 'all' || course.tag === tag;
      if (!query) return tagMatch;

      // 关键词匹配：标题+简介+标签（支持部分匹配）
      const titleMatch = course.title.toLowerCase().includes(query);
      const descMatch = course.description.toLowerCase().includes(query);
      const tagLabelMatch = getTagLabel(course.tag).toLowerCase().includes(query);

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
  const regex = new RegExp(`(${query})`, 'gi');
  // 用span标签包裹关键词，添加高亮样式
  return text.replace(regex, '<span class="bg-yellow-100 text-yellow-800 px-1 rounded"> $1 </span>');
};

// 处理标签点击
const handleTagClick = (tagValue) => {
  activeTag.value = tagValue;
};

// 清空搜索
const clearSearch = () => {
  searchQuery.value = '';
  showSearchTips.value = false;
};

// 组件挂载时初始化防抖函数
onMounted(() => {
  debouncedFilterCourses.value = debouncedFilterCourses.value();
});
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
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
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
</style>