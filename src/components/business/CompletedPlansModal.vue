<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    @click.self="closeModal"
  >
    <div
      class="bg-white rounded-2xl shadow-2xl w-full max-w-5xl max-h-[90vh] flex flex-col animate-slideIn"
      @click.stop
    >
      <!-- 头部 -->
      <div
        class="px-6 py-4 border-b border-gray-200 flex items-center justify-between flex-shrink-0"
      >
        <h2 class="text-xl font-bold text-gray-800 flex items-center">
          <i class="fas fa-list-alt text-emerald-500 mr-2" />
          计划总览
          <span class="ml-2 text-sm font-normal text-gray-500">
            (共 {{ props.plans.length }} 个计划)
          </span>
        </h2>
        <button
          class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-colors"
          @click="closeModal"
        >
          <i class="fas fa-times text-xl" />
        </button>
      </div>

      <!-- 统计信息 -->
      <div
        class="px-6 py-4 bg-gradient-to-r from-emerald-50 to-teal-50 border-b border-gray-100"
      >
        <div class="grid grid-cols-3 gap-4 text-center">
          <div>
            <div class="text-2xl font-bold text-emerald-600">
              {{ completedPlans.length }}
            </div>
            <div class="text-xs text-gray-600">
              已完成
            </div>
          </div>
          <div>
            <div class="text-2xl font-bold text-orange-600">
              {{ statistics.uncompleted }}
            </div>
            <div class="text-xs text-gray-600">
              未完成
            </div>
          </div>
          <div>
            <div class="text-2xl font-bold text-blue-600">
              {{ statistics.completionRate }}%
            </div>
            <div class="text-xs text-gray-600">
              完成率
            </div>
          </div>
        </div>
      </div>

      <!-- 计划列表 - 左右两栏布局 -->
      <div class="flex-grow overflow-hidden flex">
        <!-- 左栏：未完成计划 -->
        <div class="w-1/2 border-r border-gray-200 flex flex-col">
          <!-- 未完成计划头部 -->
          <div class="px-4 py-3 border-b border-gray-100 flex-shrink-0">
            <div class="flex items-center justify-between mb-2">
              <h3
                class="text-base font-semibold text-gray-700 flex items-center"
              >
                <i class="fas fa-clock text-orange-500 mr-2" />
                未完成 ({{ uncompletedPlans.length }})
              </h3>
            </div>
            <!-- 未完成排序按钮 -->
            <div class="flex items-center gap-2 flex-wrap">
              <span class="text-xs text-gray-500">排序:</span>
              <button
                v-for="filter in filters"
                :key="'uncompleted-' + filter.value"
                class="px-2 py-1 text-xs rounded-full transition-colors"
                :class="
                  uncompletedSortBy === filter.value
                    ? 'bg-orange-500 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                "
                @click="uncompletedSortBy = filter.value"
              >
                {{ filter.label }}
              </button>
            </div>
          </div>

          <!-- 未完成计划列表 -->
          <div class="flex-grow overflow-y-auto p-3">
            <div
              v-if="uncompletedPlans.length === 0"
              class="text-center py-8"
            >
              <i class="fas fa-check-double text-emerald-400 text-4xl mb-3" />
              <p class="text-gray-500 text-sm">
                太棒了！没有未完成的计划
              </p>
            </div>

            <div
              v-else
              class="space-y-2"
            >
              <div
                v-for="plan in paginatedUncompletedPlans"
                :key="plan.id"
                class="p-3 bg-white border border-gray-200 rounded-lg hover:shadow-md transition-all cursor-pointer group"
                @click="viewPlanDate(plan)"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-grow min-w-0">
                    <!-- 优先级和分类标签 -->
                    <div class="flex items-center gap-1.5 mb-1.5 flex-wrap">
                      <span
                        class="inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium"
                        :class="getPriorityClass(plan.priority)"
                      >
                        {{ getPriorityName(plan.priority) }}
                      </span>
                      <span
                        class="inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-700"
                      >
                        {{ plan.category }}
                      </span>
                      <span class="text-xs text-gray-500">
                        {{ formatDate(plan.date) }}
                      </span>
                    </div>

                    <!-- 计划标题 -->
                    <h4
                      class="font-medium text-gray-800 text-sm line-clamp-1 group-hover:text-emerald-600 transition-colors"
                    >
                      {{ plan.title }}
                    </h4>

                    <!-- 时间信息 -->
                    <div
                      v-if="plan.startTime && plan.endTime"
                      class="flex items-center text-xs text-gray-500 mt-1"
                    >
                      <i class="far fa-clock mr-1" />
                      {{ plan.startTime }} - {{ plan.endTime }}
                    </div>
                  </div>

                  <!-- 操作按钮 -->
                  <div
                    class="flex items-center gap-1 ml-2 opacity-0 group-hover:opacity-100"
                  >
                    <button
                      class="p-1.5 text-emerald-500 hover:text-emerald-700 hover:bg-emerald-50 rounded-lg transition-colors"
                      title="标记完成"
                      @click.stop="toggleComplete(plan)"
                    >
                      <i class="fas fa-check text-sm" />
                    </button>
                    <button
                      class="p-1.5 text-red-500 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors"
                      title="删除计划"
                      @click.stop="deletePlan(plan)"
                    >
                      <i class="fas fa-trash-alt text-sm" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 未完成分页 -->
          <div
            v-if="uncompletedTotalPages > 1"
            class="px-4 py-2 border-t border-gray-100 flex items-center justify-center gap-2 flex-shrink-0"
          >
            <button
              class="px-2 py-1 text-xs rounded transition-colors"
              :class="
                uncompletedPage === 1
                  ? 'text-gray-300 cursor-not-allowed'
                  : 'text-gray-600 hover:bg-gray-100'
              "
              :disabled="uncompletedPage === 1"
              @click="uncompletedPage--"
            >
              <i class="fas fa-chevron-left" />
            </button>
            <span class="text-xs text-gray-600">
              {{ uncompletedPage }} / {{ uncompletedTotalPages }}
            </span>
            <button
              class="px-2 py-1 text-xs rounded transition-colors"
              :class="
                uncompletedPage === uncompletedTotalPages
                  ? 'text-gray-300 cursor-not-allowed'
                  : 'text-gray-600 hover:bg-gray-100'
              "
              :disabled="uncompletedPage === uncompletedTotalPages"
              @click="uncompletedPage++"
            >
              <i class="fas fa-chevron-right" />
            </button>
          </div>
        </div>

        <!-- 右栏：已完成计划 -->
        <div class="w-1/2 flex flex-col">
          <!-- 已完成计划头部 -->
          <div class="px-4 py-3 border-b border-gray-100 flex-shrink-0">
            <div class="flex items-center justify-between mb-2">
              <h3
                class="text-base font-semibold text-gray-700 flex items-center"
              >
                <i class="fas fa-check-circle text-emerald-500 mr-2" />
                已完成 ({{ completedPlans.length }})
              </h3>
            </div>
            <!-- 已完成排序按钮 -->
            <div class="flex items-center gap-2 flex-wrap">
              <span class="text-xs text-gray-500">排序:</span>
              <button
                v-for="filter in filters"
                :key="'completed-' + filter.value"
                class="px-2 py-1 text-xs rounded-full transition-colors"
                :class="
                  completedSortBy === filter.value
                    ? 'bg-emerald-500 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                "
                @click="completedSortBy = filter.value"
              >
                {{ filter.label }}
              </button>
            </div>
          </div>

          <!-- 已完成计划列表 -->
          <div class="flex-grow overflow-y-auto p-3">
            <div
              v-if="completedPlans.length === 0"
              class="text-center py-8"
            >
              <i class="fas fa-tasks text-gray-300 text-4xl mb-3" />
              <p class="text-gray-500 text-sm">
                还没有完成任何计划
              </p>
              <p class="text-gray-400 text-xs mt-1">
                加油完成你的第一个计划吧！
              </p>
            </div>

            <div
              v-else
              class="space-y-2"
            >
              <div
                v-for="plan in paginatedCompletedPlans"
                :key="plan.id"
                class="p-3 bg-green-50 border border-green-200 rounded-lg hover:shadow-md transition-all cursor-pointer group opacity-80"
                @click="viewPlanDate(plan)"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-grow min-w-0">
                    <!-- 优先级和分类标签 -->
                    <div class="flex items-center gap-1.5 mb-1.5 flex-wrap">
                      <span
                        class="inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium"
                        :class="getPriorityClass(plan.priority)"
                      >
                        {{ getPriorityName(plan.priority) }}
                      </span>
                      <span
                        class="inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-700"
                      >
                        {{ plan.category }}
                      </span>
                      <span class="text-xs text-gray-500">
                        {{ formatDate(plan.date) }}
                      </span>
                    </div>

                    <!-- 计划标题 -->
                    <h4
                      class="font-medium text-gray-700 text-sm line-clamp-1 line-through"
                    >
                      {{ plan.title }}
                    </h4>

                    <!-- 时间信息 -->
                    <div
                      v-if="plan.startTime && plan.endTime"
                      class="flex items-center text-xs text-gray-500 mt-1"
                    >
                      <i class="far fa-clock mr-1" />
                      {{ plan.startTime }} - {{ plan.endTime }}
                    </div>
                  </div>

                  <!-- 操作按钮 -->
                  <div
                    class="flex items-center gap-1 ml-2 opacity-0 group-hover:opacity-100"
                  >
                    <button
                      class="p-1.5 text-orange-500 hover:text-orange-700 hover:bg-orange-50 rounded-lg transition-colors"
                      title="取消完成"
                      @click.stop="toggleComplete(plan)"
                    >
                      <i class="fas fa-undo text-sm" />
                    </button>
                    <button
                      class="p-1.5 text-red-500 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors"
                      title="删除计划"
                      @click.stop="deletePlan(plan)"
                    >
                      <i class="fas fa-trash-alt text-sm" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 已完成分页 -->
          <div
            v-if="completedTotalPages > 1"
            class="px-4 py-2 border-t border-gray-100 flex items-center justify-center gap-2 flex-shrink-0"
          >
            <button
              class="px-2 py-1 text-xs rounded transition-colors"
              :class="
                completedPage === 1
                  ? 'text-gray-300 cursor-not-allowed'
                  : 'text-gray-600 hover:bg-gray-100'
              "
              :disabled="completedPage === 1"
              @click="completedPage--"
            >
              <i class="fas fa-chevron-left" />
            </button>
            <span class="text-xs text-gray-600">
              {{ completedPage }} / {{ completedTotalPages }}
            </span>
            <button
              class="px-2 py-1 text-xs rounded transition-colors"
              :class="
                completedPage === completedTotalPages
                  ? 'text-gray-300 cursor-not-allowed'
                  : 'text-gray-600 hover:bg-gray-100'
              "
              :disabled="completedPage === completedTotalPages"
              @click="completedPage++"
            >
              <i class="fas fa-chevron-right" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { planManager } from "@/utils/planData.js";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  plans: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["close", "view-date", "refresh"]);

// 每页显示数量
const PAGE_SIZE = 8;

// 未完成计划的排序和分页
const uncompletedSortBy = ref("date-desc");
const uncompletedPage = ref(1);

// 已完成计划的排序和分页
const completedSortBy = ref("date-desc");
const completedPage = ref(1);

// 筛选选项
const filters = [
  { label: "最新", value: "date-desc" },
  { label: "最早", value: "date-asc" },
  { label: "优先级", value: "priority" },
];

// 已完成的计划列表
const completedPlans = computed(() => {
  return props.plans.filter((plan) => plan.completed);
});

// 未完成的计划列表
const uncompletedPlans = computed(() => {
  return props.plans.filter((plan) => !plan.completed);
});

// 统计信息
const statistics = computed(() => {
  const total = props.plans.length;
  const completed = completedPlans.value.length;
  const uncompleted = total - completed;
  const completionRate = total > 0 ? Math.round((completed / total) * 100) : 0;

  return {
    total,
    completed,
    uncompleted,
    completionRate,
  };
});

// 排序函数
function sortPlans(plans, sortBy) {
  const sorted = [...plans];

  switch (sortBy) {
    case "date-desc":
      return sorted.sort((a, b) => new Date(b.date) - new Date(a.date));
    case "date-asc":
      return sorted.sort((a, b) => new Date(a.date) - new Date(b.date));
    case "priority": {
      const priorityOrder = { high: 1, medium: 2, low: 3 };
      return sorted.sort((a, b) => {
        const priorityDiff =
          priorityOrder[a.priority] - priorityOrder[b.priority];
        if (priorityDiff !== 0) return priorityDiff;
        return new Date(b.date) - new Date(a.date);
      });
    }
    default:
      return sorted;
  }
}

// 排序后的未完成计划
const sortedUncompletedPlans = computed(() => {
  return sortPlans(uncompletedPlans.value, uncompletedSortBy.value);
});

// 排序后的已完成计划
const sortedCompletedPlans = computed(() => {
  return sortPlans(completedPlans.value, completedSortBy.value);
});

// 未完成计划总页数
const uncompletedTotalPages = computed(() => {
  return Math.ceil(sortedUncompletedPlans.value.length / PAGE_SIZE) || 1;
});

// 已完成计划总页数
const completedTotalPages = computed(() => {
  return Math.ceil(sortedCompletedPlans.value.length / PAGE_SIZE) || 1;
});

// 分页后的未完成计划
const paginatedUncompletedPlans = computed(() => {
  const start = (uncompletedPage.value - 1) * PAGE_SIZE;
  const end = start + PAGE_SIZE;
  return sortedUncompletedPlans.value.slice(start, end);
});

// 分页后的已完成计划
const paginatedCompletedPlans = computed(() => {
  const start = (completedPage.value - 1) * PAGE_SIZE;
  const end = start + PAGE_SIZE;
  return sortedCompletedPlans.value.slice(start, end);
});

// 当排序改变时重置页码
watch(uncompletedSortBy, () => {
  uncompletedPage.value = 1;
});

watch(completedSortBy, () => {
  completedPage.value = 1;
});

// 当计划列表改变时，确保页码有效
watch(
  () => props.plans,
  () => {
    if (uncompletedPage.value > uncompletedTotalPages.value) {
      uncompletedPage.value = Math.max(1, uncompletedTotalPages.value);
    }
    if (completedPage.value > completedTotalPages.value) {
      completedPage.value = Math.max(1, completedTotalPages.value);
    }
  }
);

// 获取优先级样式类
function getPriorityClass(priority) {
  const classes = {
    high: "bg-red-100 text-red-700",
    medium: "bg-yellow-100 text-yellow-700",
    low: "bg-blue-100 text-blue-700",
  };
  return classes[priority] || "bg-gray-100 text-gray-700";
}

// 获取优先级文本
function getPriorityName(priority) {
  const names = {
    high: "高",
    medium: "中",
    low: "低",
  };
  return names[priority] || "未知";
}

// 格式化日期
function formatDate(date) {
  const d = new Date(date);
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${month}-${day}`;
}

// 查看计划所在日期
function viewPlanDate(plan) {
  emit("view-date", new Date(plan.date));
}

// 切换完成状态
async function toggleComplete(plan) {
  const action = plan.completed ? "未完成" : "已完成";
  if (confirm(`确定要将计划"${plan.title}"标记为${action}吗？`)) {
    try {
      await planManager.togglePlanComplete(plan.id);
      emit("refresh");
    } catch (error) {
      console.error("切换计划状态失败:", error);
      alert("操作失败，请重试");
    }
  }
}

// 删除计划
async function deletePlan(plan) {
  if (confirm(`确定要删除计划"${plan.title}"吗？此操作不可撤销！`)) {
    try {
      await planManager.deletePlan(plan.id);
      emit("refresh");
    } catch (error) {
      console.error("删除计划失败:", error);
      alert("删除失败，请重试");
    }
  }
}

// 关闭弹窗
function closeModal() {
  emit("close");
}
</script>

<style scoped>
/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

/* 动画 */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.animate-slideIn {
  animation: slideIn 0.3s ease-out;
}

/* 行截断 */
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
