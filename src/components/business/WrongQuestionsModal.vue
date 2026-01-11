<template>
  <div
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    @click.self="closeModal"
  >
    <div
      class="bg-white rounded-lg shadow-2xl max-w-4xl w-full max-h-[90vh] flex flex-col"
    >
      <!-- 头部 -->
      <div
        class="flex items-center justify-between p-6 border-b border-gray-200"
      >
        <h2 class="text-2xl font-bold text-gray-800 flex items-center gap-2">
          <i class="fas fa-exclamation-circle text-red-500" />
          错题本
        </h2>
        <button
          class="text-gray-400 hover:text-gray-600 text-2xl"
          @click="closeModal"
        >
          <i class="fas fa-times" />
        </button>
      </div>

      <!-- 统计信息 -->
      <div
        class="p-6 bg-gradient-to-r from-red-50 to-orange-50 border-b border-gray-200"
      >
        <div class="grid grid-cols-2 gap-4">
          <div class="text-center">
            <div class="text-3xl font-bold text-red-600">
              {{ stats.total }}
            </div>
            <div class="text-sm text-gray-600 mt-1">
              总错题数
            </div>
          </div>
          <div class="text-center">
            <div class="text-3xl font-bold text-orange-600">
              {{ stats.recent }}
            </div>
            <div class="text-sm text-gray-600 mt-1">
              近7天新增
            </div>
          </div>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div class="px-6 py-4 border-b border-gray-200 bg-gray-50">
        <div class="flex flex-wrap gap-3">
          <select
            v-model="filter.category"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-red-500"
            @change="fetchWrongQuestions"
          >
            <option value="all">
              全部类型
            </option>
            <option value="CET4">
              四级
            </option>
            <option value="CET6">
              六级
            </option>
            <option value="KY">
              考研
            </option>
            <option value="IELTS">
              雅思
            </option>
            <option value="TOEFL">
              托福
            </option>
          </select>

          <select
            v-model="filter.sectionType"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-red-500"
            @change="fetchWrongQuestions"
          >
            <option value="all">
              全部题型
            </option>
            <option value="listening">
              听力
            </option>
            <option value="reading">
              阅读
            </option>
            <option value="writing">
              写作
            </option>
            <option value="speaking">
              口语
            </option>
          </select>

          <select
            v-model="filter.sortBy"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-red-500"
            @change="fetchWrongQuestions"
          >
            <option value="recent">
              最近错题
            </option>
            <option value="frequency">
              错误次数
            </option>
          </select>
        </div>
      </div>

      <!-- 错题列表 -->
      <div class="flex-1 overflow-y-auto p-6">
        <div
          v-if="loading"
          class="text-center py-12"
        >
          <i class="fas fa-spinner fa-spin text-4xl text-red-500 mb-4" />
          <div class="text-gray-600">
            加载中...
          </div>
        </div>

        <div
          v-else-if="wrongQuestions.length === 0"
          class="text-center py-12"
        >
          <i class="fas fa-check-circle text-6xl text-green-300 mb-4" />
          <div class="text-gray-600 text-lg">
            太棒了！暂无错题
          </div>
        </div>

        <div
          v-else
          class="space-y-4"
        >
          <div
            v-for="item in wrongQuestions"
            :key="item.id"
            class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md hover:border-red-300 transition-all cursor-pointer"
            @click="goToQuestion(item.questionId)"
          >
            <!-- 题目标题 -->
            <h3 class="text-lg font-bold text-gray-800 mb-1">
              {{ item.sectionName || "题目" + item.questionId }}
            </h3>

            <!-- 题目预览 -->
            <p
              v-if="item.preview"
              class="text-sm text-gray-600 mb-3 line-clamp-2"
            >
              {{ item.preview }}
            </p>

            <!-- 标签栏 -->
            <div class="flex flex-wrap gap-2 mb-3">
              <span
                class="px-3 py-1 bg-blue-100 text-blue-700 text-xs rounded-full"
              >
                {{ getCategoryLabel(item.category) }}
              </span>
              <span
                class="px-3 py-1 bg-purple-100 text-purple-700 text-xs rounded-full"
              >
                {{ getSectionTypeLabel(item.sectionType) }}
              </span>
              <span
                class="px-3 py-1 bg-red-100 text-red-700 text-xs rounded-full"
              >
                错误 {{ item.wrongCount }} 次
              </span>
              <span
                class="px-3 py-1 bg-gray-100 text-gray-700 text-xs rounded-full"
              >
                {{ formatDate(item.lastWrongDate) }}
              </span>
            </div>

            <!-- 来源 -->
            <div class="text-sm text-gray-500">
              来自: {{ item.paperName }}
            </div>
          </div>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="p-6 border-t border-gray-200 bg-gray-50">
        <div class="flex justify-between items-center">
          <div class="text-sm text-gray-600">
            共
            <span class="font-semibold text-gray-800">{{ total }}</span> 道错题
          </div>
          <button
            class="px-6 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg transition-colors font-medium"
            @click="closeModal"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from "vue";
import questionApi from "@/api/question";

export default {
  name: "WrongQuestionsModal",
  emits: ["close", "go-to-question"],
  setup(props, { emit }) {
    const loading = ref(true);
    const wrongQuestions = ref([]);
    const total = ref(0);

    // 统计信息
    const stats = reactive({
      total: 0,
      recent: 0,
    });

    // 筛选条件
    const filter = reactive({
      category: "all",
      sectionType: "all",
      sortBy: "recent",
    });

    // 获取错题列表
    const fetchWrongQuestions = async () => {
      loading.value = true;
      try {
        const response = await questionApi.getWrongQuestions(filter);
        wrongQuestions.value = response.data.items;
        total.value = response.data.total;
        stats.total = response.data.stats.total;
        stats.recent = response.data.stats.recent;
      } catch (error) {
        console.error("获取错题失败:", error);
      } finally {
        loading.value = false;
      }
    };

    // 关闭模态框
    const closeModal = () => {
      emit("close");
    };

    // 跳转到题目
    const goToQuestion = (questionId) => {
      emit("go-to-question", questionId);
    };

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return "";
      const date = new Date(dateString);
      const now = new Date();
      const diff = now - date;
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));

      if (days === 0) return "今天";
      if (days === 1) return "昨天";
      if (days < 7) return `${days}天前`;
      if (days < 30) return `${Math.floor(days / 7)}周前`;
      return `${date.getMonth() + 1}月${date.getDate()}日`;
    };

    // 获取类别标签
    const getCategoryLabel = (value) => {
      const labels = {
        CET4: "四级",
        CET6: "六级",
        KY: "考研",
        IELTS: "雅思",
        TOEFL: "托福",
      };
      return labels[value] || value;
    };

    // 获取题型标签
    const getSectionTypeLabel = (value) => {
      const labels = {
        listening: "听力",
        reading: "阅读",
        writing: "写作",
        speaking: "口语",
      };
      return labels[value] || value;
    };

    onMounted(() => {
      fetchWrongQuestions();
    });

    return {
      loading,
      wrongQuestions,
      total,
      stats,
      filter,
      fetchWrongQuestions,
      closeModal,
      goToQuestion,
      formatDate,
      getCategoryLabel,
      getSectionTypeLabel,
    };
  },
};
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
