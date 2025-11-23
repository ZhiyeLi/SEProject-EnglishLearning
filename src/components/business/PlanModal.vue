<template>
  <div
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
  >
    <div
      class="bg-white rounded-xl shadow-2xl max-w-5xl w-full max-h-[90vh] flex flex-col"
      @click.stop
    >
      <!-- 弹窗头部 -->
      <div
        class="bg-white border-b border-gray-200 px-6 py-4 flex items-center rounded-t-xl flex-shrink-0"
      >
        <h2 class="text-2xl font-bold text-gray-800 flex items-center">
          <i class="fas fa-calendar-alt text-emerald-500 mr-3" />
          管理计划 - {{ formattedDate }}
        </h2>
      </div>

      <!-- 弹窗内容 -->
      <div class="flex-grow overflow-hidden flex">
        <!-- 左侧：计划列表 -->
        <div class="w-2/5 border-r border-gray-200 overflow-y-auto p-4">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-700 flex items-center">
              <i class="fas fa-list text-emerald-500 mr-2" />
              计划列表 ({{ sortedPlans.length }})
            </h3>
            <button
              class="text-emerald-600 hover:text-emerald-700 text-sm font-medium"
              @click="addNewPlan"
            >
              <i class="fas fa-plus-circle mr-1" />
              新增
            </button>
          </div>

          <!-- 计划项列表 -->
          <div
            v-if="sortedPlans.length > 0"
            class="space-y-2"
          >
            <div
              v-for="(plan, index) in sortedPlans"
              :key="index"
              class="border rounded-lg p-3 cursor-pointer transition-all"
              :class="[
                selectedPlanIndex === getActualIndex(index)
                  ? 'border-emerald-500 bg-emerald-50'
                  : 'border-gray-200 hover:border-emerald-300 hover:bg-gray-50',
                getPriorityBorderClass(plan.priority),
              ]"
              @click="selectPlan(index)"
            >
              <div class="flex items-start justify-between">
                <div class="flex-grow min-w-0">
                  <div class="flex items-center mb-1">
                    <div
                      class="w-2 h-2 rounded-full mr-2 flex-shrink-0"
                      :class="getPriorityDotClass(plan.priority)"
                    />
                    <h4
                      class="font-medium text-gray-800 truncate"
                      :class="{ 'line-through text-gray-500': plan.completed }"
                    >
                      {{ plan.title || "未命名计划" }}
                    </h4>
                  </div>
                  <p
                    v-if="plan.description"
                    class="text-xs text-gray-600 truncate"
                  >
                    {{ plan.description }}
                  </p>
                  <div
                    class="flex items-center mt-2 space-x-3 text-xs text-gray-500"
                  >
                    <span v-if="plan.startTime && plan.endTime">
                      <i class="far fa-clock mr-1" />{{ plan.startTime }} -
                      {{ plan.endTime }}
                    </span>
                    <span>
                      <i class="fas fa-tag mr-1" />{{ plan.category }}
                    </span>
                  </div>
                </div>
                <div class="flex items-center space-x-1 ml-2 flex-shrink-0">
                  <button
                    class="p-1.5 rounded transition-colors"
                    :class="
                      plan.completed
                        ? 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                        : 'text-emerald-500 hover:text-emerald-700 hover:bg-emerald-50'
                    "
                    :title="plan.completed ? '取消完成' : '标记完成'"
                    @click.stop="toggleComplete(index)"
                  >
                    <i
                      class="text-sm"
                      :class="
                        plan.completed
                          ? 'fas fa-undo-alt'
                          : 'fas fa-check-circle'
                      "
                    />
                  </button>
                  <button
                    class="p-1.5 text-red-500 hover:text-red-700 hover:bg-red-50 rounded transition-colors"
                    title="删除"
                    @click.stop="removePlan(index)"
                  >
                    <i class="fas fa-trash-alt text-sm" />
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div
            v-else
            class="text-center py-12"
          >
            <i class="fas fa-calendar-plus text-gray-300 text-5xl mb-3" />
            <p class="text-gray-500">
              暂无计划，点击右上角新增按钮添加
            </p>
          </div>
        </div>

        <!-- 右侧：计划编辑区 -->
        <div class="flex-1 overflow-y-auto p-6">
          <div
            v-if="selectedPlanIndex !== null && localPlans[selectedPlanIndex]"
          >
            <h3
              class="text-lg font-semibold text-gray-700 mb-4 flex items-center"
            >
              <i class="fas fa-edit text-emerald-500 mr-2" />
              编辑计划
            </h3>

            <!-- 编辑表单 -->
            <div class="space-y-4">
              <!-- 标题 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  计划标题 <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="localPlans[selectedPlanIndex].title"
                  type="text"
                  placeholder="请输入计划标题"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                >
              </div>

              <!-- 描述 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  计划描述
                </label>
                <textarea
                  v-model="localPlans[selectedPlanIndex].description"
                  rows="3"
                  placeholder="请输入计划描述（可选）"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 resize-none"
                />
              </div>

              <!-- 时间范围 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  <i class="far fa-clock mr-1" />时间范围
                </label>
                <div class="flex items-center space-x-2">
                  <input
                    v-model="localPlans[selectedPlanIndex].startTime"
                    type="time"
                    class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  >
                  <span class="text-gray-400">至</span>
                  <input
                    v-model="localPlans[selectedPlanIndex].endTime"
                    type="time"
                    class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  >
                </div>
              </div>

              <!-- 分类和优先级 -->
              <div class="grid grid-cols-2 gap-4">
                <!-- 分类 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    <i class="fas fa-tag mr-1" />分类
                  </label>
                  <select
                    v-model="localPlans[selectedPlanIndex].category"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  >
                    <option value="单词学习">
                      单词学习
                    </option>
                    <option value="语法练习">
                      语法练习
                    </option>
                    <option value="阅读训练">
                      阅读训练
                    </option>
                    <option value="听力练习">
                      听力练习
                    </option>
                    <option value="作业">
                      作业
                    </option>
                    <option value="复习">
                      复习
                    </option>
                    <option value="其他">
                      其他
                    </option>
                  </select>
                </div>

                <!-- 优先级 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    <i class="fas fa-flag mr-1" />优先级
                  </label>
                  <select
                    v-model="localPlans[selectedPlanIndex].priority"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  >
                    <option value="high">
                      高
                    </option>
                    <option value="medium">
                      中
                    </option>
                    <option value="low">
                      低
                    </option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <!-- 未选中状态 -->
          <div
            v-else
            class="h-full flex items-center justify-center"
          >
            <div class="text-center text-gray-400">
              <i class="fas fa-hand-pointer text-6xl mb-4" />
              <p class="text-lg">
                请从左侧选择一个计划进行编辑
              </p>
              <p class="text-sm mt-2">
                或点击"新增"按钮创建新计划
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部操作按钮 -->
      <div
        class="border-t border-gray-200 px-6 py-4 flex items-center justify-end space-x-3 flex-shrink-0"
      >
        <button
          class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          @click="closeModal"
        >
          取消
        </button>
        <button
          class="px-6 py-2 bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 transition-colors shadow-sm hover:shadow flex items-center"
          @click="savePlans"
        >
          <i class="fas fa-save mr-2" />
          保存计划
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";

const props = defineProps({
  selectedDate: {
    type: Date,
    required: true,
  },
  plans: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["close", "save"]);

// 本地计划数据
const localPlans = ref([]);
// 选中的计划索引
const selectedPlanIndex = ref(null);

// 格式化日期
const formattedDate = computed(() => {
  const year = props.selectedDate.getFullYear();
  const month = props.selectedDate.getMonth() + 1;
  const day = props.selectedDate.getDate();
  return `${year}年${month}月${day}日`;
});

// 排序后的计划列表（按优先级和完成状态）
const sortedPlans = computed(() => {
  const priorityOrder = { high: 1, medium: 2, low: 3 };
  return [...localPlans.value].sort((a, b) => {
    // 未完成的排在前面
    if (a.completed !== b.completed) {
      return a.completed ? 1 : -1;
    }
    // 相同完成状态下，按优先级排序
    return priorityOrder[a.priority] - priorityOrder[b.priority];
  });
});

// 监听props变化
watch(
  () => props.plans,
  (newPlans) => {
    localPlans.value = structuredClone(newPlans);
    // 如果有计划且没有选中，默认选中第一个
    if (localPlans.value.length > 0 && selectedPlanIndex.value === null) {
      selectedPlanIndex.value = 0;
    }
  },
  { immediate: true }
);

// 获取优先级圆点颜色类
function getPriorityDotClass(priority) {
  const colors = {
    high: "bg-red-500",
    medium: "bg-yellow-500",
    low: "bg-blue-500",
  };
  return colors[priority] || "bg-gray-500";
}

// 获取优先级边框颜色类
function getPriorityBorderClass(priority) {
  const colors = {
    high: "border-l-4 border-l-red-400",
    medium: "border-l-4 border-l-yellow-400",
    low: "border-l-4 border-l-blue-400",
  };
  return colors[priority] || "border-l-4 border-l-gray-400";
}

// 获取实际索引（从排序后的数组找到原始数组的索引）
function getActualIndex(sortedIndex) {
  const plan = sortedPlans.value[sortedIndex];
  return localPlans.value.indexOf(plan);
}

// 选择计划
function selectPlan(sortedIndex) {
  selectedPlanIndex.value = getActualIndex(sortedIndex);
}

// 添加新计划
function addNewPlan() {
  localPlans.value.push({
    title: "",
    description: "",
    category: "其他",
    priority: "medium",
    startTime: "",
    endTime: "",
    completed: false,
  });
  // 选中新添加的计划
  selectedPlanIndex.value = localPlans.value.length - 1;
}

// 删除计划
function removePlan(sortedIndex) {
  const plan = sortedPlans.value[sortedIndex];
  const actualIndex = getActualIndex(sortedIndex);

  if (confirm(`确定要删除计划"${plan.title || "未命名计划"}"吗？`)) {
    localPlans.value.splice(actualIndex, 1);

    // 调整选中索引
    if (selectedPlanIndex.value === actualIndex) {
      selectedPlanIndex.value =
        localPlans.value.length > 0
          ? Math.min(actualIndex, localPlans.value.length - 1)
          : null;
    } else if (selectedPlanIndex.value > actualIndex) {
      selectedPlanIndex.value--;
    }
  }
}

// 切换完成状态
function toggleComplete(sortedIndex) {
  const actualIndex = getActualIndex(sortedIndex);
  const plan = localPlans.value[actualIndex];
  // 使用Vue的响应式方式更新
  plan.completed = !plan.completed;
  // 强制触发视图更新
  localPlans.value = [...localPlans.value];
}

// 保存计划
function savePlans() {
  // 验证计划
  const validPlans = localPlans.value.filter((plan) => {
    return plan.title && plan.title.trim() !== "";
  });

  const emptyPlans = localPlans.value.filter((plan) => {
    return !plan.title || plan.title.trim() === "";
  });

  if (emptyPlans.length > 0) {
    alert("存在未填写标题的计划，保存时将自动忽略！");
  }

  // 检查是否有重复标题的计划
  const titleMap = new Map();
  const duplicateTitles = [];

  for (const plan of validPlans) {
    const normalizedTitle = plan.title.trim().toLowerCase();
    if (titleMap.has(normalizedTitle)) {
      if (!duplicateTitles.includes(plan.title.trim())) {
        duplicateTitles.push(plan.title.trim());
      }
    } else {
      titleMap.set(normalizedTitle, true);
    }
  }

  if (duplicateTitles.length > 0) {
    alert(
      `检测到重复的计划标题：\n"${duplicateTitles.join('", "')}"\n\n同一天内不能有重复标题的计划，请修改后再保存！`
    );
    return;
  }

  emit("save", validPlans);
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

.fixed > div {
  animation: fadeIn 0.2s ease-out;
}
</style>
