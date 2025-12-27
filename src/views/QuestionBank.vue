<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <ActionButtons
          @suggestions="handleSuggestions"
          @settings="goToSettings"
          @home="goHome"
          @notifications="handleNotifications"
        />
      </template>
    </NavBar>

    <!-- 主内容区 -->
    <div class="container mx-auto px-4 py-6">
      <!-- 顶部统计栏 -->
      <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <!-- 搜索栏 -->
          <div class="md:col-span-1">
            <div class="relative">
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索标题、题型名称、题型类型..."
                class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                @input="handleSearch"
              >
              <i class="fas fa-search absolute left-3 top-3 text-gray-400" />
            </div>
          </div>

          <!-- 今日做题统计 -->
          <div
            class="flex items-center justify-center bg-gradient-to-r from-blue-50 to-blue-100 rounded-lg p-4"
          >
            <div class="text-center">
              <div class="text-2xl font-bold text-blue-600">
                {{ todayStats.count }}
              </div>
              <div class="text-sm text-gray-600">
                今日做题
              </div>
            </div>
          </div>

          <!-- 正确率统计 -->
          <div
            class="flex items-center justify-center bg-gradient-to-r from-emerald-50 to-emerald-100 rounded-lg p-4"
          >
            <div class="text-center">
              <div class="text-2xl font-bold text-emerald-600">
                {{ todayStats.accuracy }}%
              </div>
              <div class="text-sm text-gray-600">
                今日正确率
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 模式切换和错题入口 -->
      <div class="flex justify-between items-center mb-6">
        <!-- 模式切换按钮 -->
        <div class="bg-white rounded-lg shadow-sm p-1 inline-flex">
          <button
            :class="[
              'px-6 py-2 rounded-md text-sm font-medium transition-all',
              currentMode === 'exam'
                ? 'bg-emerald-500 text-white shadow-sm'
                : 'text-gray-600 hover:text-gray-800',
            ]"
            @click="switchMode('exam')"
          >
            <i class="fas fa-file-alt mr-2" />
            考试模式
          </button>
          <button
            :class="[
              'px-6 py-2 rounded-md text-sm font-medium transition-all',
              currentMode === 'single'
                ? 'bg-emerald-500 text-white shadow-sm'
                : 'text-gray-600 hover:text-gray-800',
            ]"
            @click="switchMode('single')"
          >
            <i class="fas fa-list mr-2" />
            单题模式
          </button>
        </div>

        <!-- 错题本入口 -->
        <button
          class="bg-red-500 hover:bg-red-600 text-white px-6 py-2 rounded-lg shadow-sm transition-colors"
          @click="showWrongQuestions"
        >
          <i class="fas fa-exclamation-circle mr-2" />
          错题本
        </button>
      </div>

      <!-- 主要内容区域 -->
      <div class="flex gap-6">
        <!-- 左侧筛选栏 -->
        <aside class="w-64 bg-white rounded-lg shadow-sm p-6 flex-shrink-0">
          <h3 class="text-lg font-bold text-gray-800 mb-4 flex items-center">
            <i class="fas fa-filter text-emerald-500 mr-2" />
            筛选条件
          </h3>

          <!-- 考试类型筛选 -->
          <div class="mb-6">
            <h4 class="text-sm font-semibold text-gray-700 mb-3">
              考试类型
            </h4>
            <div class="space-y-2">
              <label
                v-for="cat in categories"
                :key="cat.value"
                class="flex items-center cursor-pointer group"
              >
                <input
                  v-model="filters.category"
                  type="radio"
                  :value="cat.value"
                  class="w-4 h-4 text-emerald-500 focus:ring-emerald-500"
                >
                <span
                  class="ml-3 text-sm text-gray-700 group-hover:text-emerald-600"
                >
                  {{ cat.label }}
                </span>
              </label>
            </div>
          </div>

          <!-- 题型筛选 (仅单题模式显示) -->
          <div
            v-if="currentMode === 'single'"
            class="mb-6"
          >
            <h4 class="text-sm font-semibold text-gray-700 mb-3">
              题型
            </h4>
            <div class="space-y-2">
              <label
                v-for="type in sectionTypes"
                :key="type.value"
                class="flex items-center cursor-pointer group"
              >
                <input
                  v-model="filters.sectionType"
                  type="radio"
                  :value="type.value"
                  class="w-4 h-4 text-emerald-500 focus:ring-emerald-500"
                >
                <span
                  class="ml-3 text-sm text-gray-700 group-hover:text-emerald-600"
                >
                  {{ type.label }}
                </span>
              </label>
            </div>
          </div>

          <!-- 题型筛选 (考试模式 - 包含该题型) -->
          <div
            v-if="currentMode === 'exam'"
            class="mb-6"
          >
            <h4 class="text-sm font-semibold text-gray-700 mb-3">
              包含题型
            </h4>
            <div class="space-y-2">
              <label
                v-for="type in sectionTypes"
                :key="type.value"
                class="flex items-center cursor-pointer group"
              >
                <input
                  v-model="filters.containsSectionType"
                  type="checkbox"
                  :value="type.value"
                  class="w-4 h-4 text-emerald-500 focus:ring-emerald-500"
                >
                <span
                  class="ml-3 text-sm text-gray-700 group-hover:text-emerald-600"
                >
                  {{ type.label }}
                </span>
              </label>
            </div>
          </div>

          <!-- 状态筛选 -->
          <div class="mb-6">
            <h4 class="text-sm font-semibold text-gray-700 mb-3">
              状态
            </h4>
            <div class="space-y-2">
              <label
                v-for="status in statuses"
                :key="status.value"
                class="flex items-center cursor-pointer group"
              >
                <input
                  v-model="filters.status"
                  type="radio"
                  :value="status.value"
                  class="w-4 h-4 text-emerald-500 focus:ring-emerald-500"
                >
                <span
                  class="ml-3 text-sm text-gray-700 group-hover:text-emerald-600"
                >
                  {{ status.label }}
                </span>
              </label>
            </div>
          </div>

          <!-- 重置按钮 -->
          <button
            class="w-full bg-gray-100 hover:bg-gray-200 text-gray-700 py-2 rounded-lg transition-colors"
            @click="resetFilters"
          >
            <i class="fas fa-redo mr-2" />
            重置筛选
          </button>
        </aside>

        <!-- 右侧列表区域 -->
        <main class="flex-1">
          <!-- 排序控制 -->
          <div
            class="bg-white rounded-lg shadow-sm p-4 mb-4 flex justify-between items-center"
          >
            <div class="text-sm text-gray-600">
              共
              <span class="font-semibold text-gray-800">{{ total }}</span>
              条结果
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-600">排序:</span>
              <select
                v-model="sortBy"
                class="border border-gray-300 rounded-lg px-3 py-1 text-sm focus:ring-2 focus:ring-emerald-500"
                @change="handleSort"
              >
                <option value="year_desc">
                  年份 (新到旧)
                </option>
                <option value="year_asc">
                  年份 (旧到新)
                </option>
                <option value="created_desc">
                  创建时间 (新到旧)
                </option>
                <option value="created_asc">
                  创建时间 (旧到新)
                </option>
              </select>
            </div>
          </div>

          <!-- 列表内容 -->
          <div
            v-if="loading"
            class="bg-white rounded-lg shadow-sm p-12 text-center"
          >
            <i class="fas fa-spinner fa-spin text-4xl text-emerald-500 mb-4" />
            <div class="text-gray-600">
              加载中...
            </div>
          </div>

          <div
            v-else-if="items.length === 0"
            class="bg-white rounded-lg shadow-sm p-12 text-center"
          >
            <i class="fas fa-inbox text-6xl text-gray-300 mb-4" />
            <div class="text-gray-600">
              暂无数据
            </div>
          </div>

          <div
            v-else
            class="space-y-4"
          >
            <!-- 考试模式 - 试卷列表 -->
            <template v-if="currentMode === 'exam'">
              <div
                v-for="paper in items"
                :key="paper.id"
                class="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow p-6 cursor-pointer relative group"
                @click="startExam(paper.id)"
              >
                <!-- 收藏按钮 -->
                <button
                  class="absolute top-4 right-4 text-2xl transition-colors"
                  :class="
                    paper.isFavorited
                      ? 'text-yellow-400'
                      : 'text-gray-300 group-hover:text-gray-400'
                  "
                  @click.stop="
                    toggleFavorite('paper', paper.id, paper.isFavorited)
                  "
                >
                  <i
                    :class="paper.isFavorited ? 'fas fa-star' : 'far fa-star'"
                  />
                </button>

                <div class="flex items-start gap-4">
                  <div class="flex-1">
                    <h3 class="text-lg font-bold text-gray-800 mb-2">
                      {{ paper.name }}
                    </h3>
                    <div class="flex flex-wrap gap-2 mb-3">
                      <span
                        class="px-3 py-1 bg-blue-100 text-blue-700 text-xs rounded-full"
                      >
                        {{ getCategoryLabel(paper.category) }}
                      </span>
                      <span
                        v-if="paper.status === 'done'"
                        class="px-3 py-1 bg-green-100 text-green-700 text-xs rounded-full"
                      >
                        已完成
                      </span>
                      <span
                        v-else-if="paper.status === 'ongoing'"
                        class="px-3 py-1 bg-yellow-100 text-yellow-700 text-xs rounded-full"
                      >
                        进行中
                      </span>
                    </div>
                    <div class="text-sm text-gray-600">
                      难度: {{ paper.difficulty }}/5
                    </div>
                  </div>
                </div>
              </div>
            </template>

            <!-- 单题模式 - 题目列表 -->
            <template v-if="currentMode === 'single'">
              <div
                v-for="question in items"
                :key="question.id"
                class="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow p-6 cursor-pointer relative group"
                @click="startQuestion(question.id)"
              >
                <!-- 收藏按钮 -->
                <button
                  class="absolute top-4 right-4 text-2xl transition-colors"
                  :class="
                    question.isFavorited
                      ? 'text-yellow-400'
                      : 'text-gray-300 group-hover:text-gray-400'
                  "
                  @click.stop="
                    toggleFavorite(
                      'question',
                      question.id,
                      question.isFavorited
                    )
                  "
                >
                  <i
                    :class="
                      question.isFavorited ? 'fas fa-star' : 'far fa-star'
                    "
                  />
                </button>

                <div class="flex items-start gap-4">
                  <div class="flex-1">
                    <h3 class="text-lg font-bold text-gray-800 mb-1">
                      {{ question.sectionName }}
                    </h3>
                    <p
                      v-if="question.title"
                      class="text-sm text-gray-600 mb-3 line-clamp-2"
                    >
                      {{ question.title }}
                    </p>
                    <div class="flex flex-wrap gap-2 mb-3">
                      <span
                        class="px-3 py-1 bg-blue-100 text-blue-700 text-xs rounded-full"
                      >
                        {{ getCategoryLabel(question.paperCategory) }}
                      </span>
                      <span
                        class="px-3 py-1 bg-purple-100 text-purple-700 text-xs rounded-full"
                      >
                        {{ getSectionTypeLabel(question.sectionType) }}
                      </span>
                      <span
                        v-if="question.status === 'done'"
                        class="px-3 py-1 bg-green-100 text-green-700 text-xs rounded-full"
                      >
                        已完成
                      </span>
                    </div>
                    <div class="text-sm text-gray-500">
                      来自: {{ question.paperName }}
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <!-- 分页 -->
          <div
            v-if="total > pageSize"
            class="bg-white rounded-lg shadow-sm p-4 mt-4 flex justify-center"
          >
            <div class="flex items-center gap-2">
              <button
                :disabled="currentPage === 1"
                class="px-4 py-2 rounded-lg border border-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                @click="changePage(currentPage - 1)"
              >
                <i class="fas fa-chevron-left" />
              </button>
              <span class="px-4 py-2 text-sm text-gray-600">
                第 {{ currentPage }} / {{ totalPages }} 页
              </span>
              <button
                :disabled="currentPage === totalPages"
                class="px-4 py-2 rounded-lg border border-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                @click="changePage(currentPage + 1)"
              >
                <i class="fas fa-chevron-right" />
              </button>
            </div>
          </div>
        </main>
      </div>
    </div>

    <!-- 错题窗口 Modal -->
    <WrongQuestionsModal
      v-if="showWrongModal"
      @close="showWrongModal = false"
      @go-to-question="handleGoToQuestion"
    />
  </div>
</template>

<script>
import NavBar from "@/components/common/NavBar.vue";
import ActionButtons from "@/components/common/ActionButtons.vue";
import WrongQuestionsModal from "@/components/business/WrongQuestionsModal.vue";
import { ref, reactive, computed, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import questionApi from "@/api/question";

export default {
  name: "QuestionBank",
  components: {
    NavBar,
    ActionButtons,
    WrongQuestionsModal,
  },
  setup() {
    const router = useRouter();
    const route = useRoute();

    // 页面跳转函数
    const goHome = () => {
      router.push("/");
    };

    const gotoCourse = () => {
      router.push({ name: "Course" }).catch(() => {});
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

    const goToSettings = () => {
      router.push("/settings");
    };

    // 导航项 - 与其他页面保持一致
    const navItems = ref([
      { label: "首页", onClick: () => goHome(), isActive: false },
      { label: "课程", onClick: () => gotoCourse() },
      { label: "题库", onClick: () => {}, isActive: true },
      { label: "时间表", onClick: () => gotoTimeTable() },
      { label: "单词打卡", onClick: () => gotoWordCheckIn() },
      { label: "AI伴学", onClick: () => gotoAiChat() },
    ]);

    // 当前模式
    const currentMode = ref("exam"); // 'exam' 或 'single'

    // 搜索关键字
    const searchKeyword = ref("");

    // 今日统计
    const todayStats = reactive({
      count: 0,
      accuracy: 0,
    });

    // 筛选条件
    const filters = reactive({
      category: "all", // 考试类型
      sectionType: "all", // 题型(单题模式)
      containsSectionType: [], // 包含的题型(考试模式)
      status: "all", // 状态
    });

    // 排序
    const sortBy = ref("year_desc");

    // 分页
    const currentPage = ref(1);
    const pageSize = ref(10);
    const total = ref(0);

    // 数据
    const items = ref([]);
    const loading = ref(false);

    // 错题窗口
    const showWrongModal = ref(false);

    // 筛选选项
    const categories = [
      { label: "全部", value: "all" },
      { label: "四级", value: "CET4" },
      { label: "六级", value: "CET6" },
      { label: "考研", value: "KY" },
      { label: "雅思", value: "IELTS" },
      { label: "托福", value: "TOEFL" },
    ];

    const sectionTypes = [
      { label: "全部", value: "all" },
      { label: "听力", value: "listening" },
      { label: "阅读", value: "reading" },
      { label: "写作", value: "writing" },
      { label: "口语", value: "speaking" },
    ];

    const statuses = [
      { label: "全部", value: "all" },
      { label: "未做", value: "not_done" },
      { label: "已做", value: "done" },
      { label: "已收藏", value: "favorited" },
    ];

    // 计算总页数
    const totalPages = computed(() => {
      return Math.ceil(total.value / pageSize.value);
    });

    // 切换模式
    const switchMode = (mode) => {
      if (currentMode.value === mode) return;
      currentMode.value = mode;
      // 重置筛选条件
      filters.sectionType = "all";
      filters.containsSectionType = [];
      currentPage.value = 1;
      fetchData();
    };

    // 重置筛选
    const resetFilters = () => {
      filters.category = "all";
      filters.sectionType = "all";
      filters.containsSectionType = [];
      filters.status = "all";
      searchKeyword.value = "";
      currentPage.value = 1;
      fetchData();
    };

    // 搜索
    const handleSearch = () => {
      currentPage.value = 1;
      fetchData();
    };

    // 排序
    const handleSort = () => {
      currentPage.value = 1;
      fetchData();
    };

    // 分页
    const changePage = (page) => {
      currentPage.value = page;
      fetchData();
    };

    // 获取数据
    const fetchData = async () => {
      loading.value = true;
      try {
        const params = {
          mode: currentMode.value,
          page: currentPage.value,
          pageSize: pageSize.value,
          sortBy: sortBy.value,
          keyword: searchKeyword.value,
          ...filters,
        };

        const response = await questionApi.getList(params);
        items.value = response.data.items;
        total.value = response.data.total;
      } catch (error) {
        console.error("获取数据失败:", error);
      } finally {
        loading.value = false;
      }
    };

    // 获取今日统计
    const fetchTodayStats = async () => {
      try {
        const response = await questionApi.getTodayStats();
        todayStats.count = response.data.count;
        todayStats.accuracy = response.data.accuracy;
      } catch (error) {
        console.error("获取今日统计失败:", error);
      }
    };

    // 开始考试
    const startExam = (paperId) => {
      router.push({
        name: "ExamPractice",
        query: { paperId, mode: "exam" },
      });
    };

    // 开始单题
    const startQuestion = (questionId) => {
      router.push({
        name: "ExamPractice",
        query: { questionId, mode: "single" },
      });
    };

    // 切换收藏
    const toggleFavorite = async (type, id, currentStatus) => {
      try {
        if (currentStatus) {
          await questionApi.removeFavorite(type, id);
        } else {
          await questionApi.addFavorite(type, id);
        }
        // 更新列表
        const item = items.value.find((i) => i.id === id);
        if (item) {
          item.isFavorited = !currentStatus;
        }
      } catch (error) {
        console.error("收藏操作失败:", error);
      }
    };

    // 显示错题本
    const showWrongQuestions = () => {
      showWrongModal.value = true;
    };

    // 跳转到题目
    const handleGoToQuestion = (questionId) => {
      showWrongModal.value = false;
      startQuestion(questionId);
    };

    // 获取类别标签
    const getCategoryLabel = (value) => {
      const cat = categories.find((c) => c.value === value);
      return cat ? cat.label : value;
    };

    // 获取题型标签
    const getSectionTypeLabel = (value) => {
      const type = sectionTypes.find((t) => t.value === value);
      return type ? type.label : value;
    };

    // 获取题型图标
    const getSectionIcon = (type) => {
      const icons = {
        listening: "fa-headphones",
        reading: "fa-book-open",
        writing: "fa-pen",
        speaking: "fa-microphone",
      };
      return icons[type] || "fa-question";
    };

    const handleSuggestions = () => {
      // TODO: 实现学习建议功能
      console.log("学习建议功能待实现");
    };

    const handleNotifications = () => {
      // TODO: 实现通知功能
      console.log("通知功能待实现");
    };

    // 监听筛选条件变化
    watch(
      () => [filters.category, filters.sectionType, filters.status],
      () => {
        currentPage.value = 1;
        fetchData();
      }
    );

    watch(
      () => filters.containsSectionType,
      () => {
        currentPage.value = 1;
        fetchData();
      },
      { deep: true }
    );

    // 初始化
    onMounted(() => {
      // 检查是否有tab参数，如果有则切换到对应的模式
      if (route.query.tab === "exam" || route.query.tab === "single") {
        currentMode.value = route.query.tab;
      }
      fetchData();
      fetchTodayStats();
    });

    return {
      navItems,
      currentMode,
      searchKeyword,
      todayStats,
      filters,
      sortBy,
      currentPage,
      pageSize,
      total,
      totalPages,
      items,
      loading,
      showWrongModal,
      categories,
      sectionTypes,
      statuses,
      switchMode,
      resetFilters,
      handleSearch,
      handleSort,
      changePage,
      startExam,
      startQuestion,
      toggleFavorite,
      showWrongQuestions,
      handleGoToQuestion,
      getCategoryLabel,
      getSectionTypeLabel,
      getSectionIcon,
      goHome,
      goToSettings,
      handleSuggestions,
      handleNotifications,
    };
  },
};
</script>

<style scoped>
/* 添加一些过渡动画 */
.group:hover .group-hover\:text-emerald-600 {
  transition: color 0.2s;
}
</style>
