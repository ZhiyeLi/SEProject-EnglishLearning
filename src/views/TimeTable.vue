<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 导航栏 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <ActionButtons
          @suggestions="() => {}"
          @settings="gotoSettings"
          @home="gotoHome"
          @notifications="() => {}"
        />
      </template>
    </NavBar>

    <!-- 主内容区 -->
    <main class="flex-grow p-6 overflow-y-auto">
      <div class="max-w-7xl mx-auto space-y-6">
        <!-- 返回按钮
        <button
          class="text-emerald-600 hover:text-emerald-700 flex items-center transition-colors mb-4"
          @click="gotoHome"
        >
          <i class="fas fa-arrow-left mr-2" />
          <span>返回首页</span>
        </button> -->

        <!-- 今日计划模块 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-gray-800 flex items-center">
              <i class="fas fa-calendar-day text-emerald-500 mr-3" />
              今日计划 - {{ todayDateStr }}
            </h2>
            <div class="flex items-center space-x-3">
              <button
                class="text-emerald-600 hover:text-emerald-700 font-medium transition-colors flex items-center"
                @click="showCompletedPlansModal = true"
              >
                <i class="fas fa-list-alt mr-1.5" />
                计划总览
              </button>
              <CustomButton
                type="primary"
                icon="fa-plus"
                text="管理本日计划"
                @click="openPlanModal(today)"
              />
            </div>
          </div>

          <!-- 未完成的计划 -->
          <div
            v-if="todayPlans.some((p) => !p.completed)"
            class="space-y-3 mb-6"
          >
            <h3 class="text-lg font-semibold text-gray-700 mb-3">
              待完成
            </h3>
            <div
              v-for="plan in todayPlans.filter((p) => !p.completed)"
              :key="plan.id"
              class="flex items-center justify-between bg-gray-50 rounded-lg p-4 hover:bg-emerald-50 transition-colors border border-gray-200"
            >
              <div class="flex items-center flex-grow">
                <div
                  class="w-3 h-3 rounded-full mr-3"
                  :class="getPriorityColor(plan.priority)"
                />
                <div>
                  <h4 class="font-medium text-gray-800">
                    {{ plan.title }}
                  </h4>
                  <p class="text-sm text-gray-600 mt-1">
                    {{ plan.description }}
                  </p>
                  <div
                    class="flex items-center mt-2 space-x-4 text-xs text-gray-500"
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
              </div>
              <button
                class="ml-4 px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg transition-all shadow-sm hover:shadow"
                @click="togglePlanComplete(plan)"
              >
                <i class="fas fa-check mr-1" />完成
              </button>
            </div>
          </div>

          <!-- 已完成的计划 -->
          <div
            v-if="todayPlans.some((p) => p.completed)"
            class="space-y-3"
          >
            <h3
              class="text-lg font-semibold text-gray-700 mb-3 flex items-center"
            >
              <i class="fas fa-check-circle text-emerald-500 mr-2" />
              已完成 ({{ todayPlans.filter((p) => p.completed).length }})
            </h3>
            <div
              v-for="plan in todayPlans.filter((p) => p.completed)"
              :key="plan.id"
              class="flex items-center justify-between bg-green-50 rounded-lg p-4 border border-green-200 opacity-75"
            >
              <div class="flex items-center flex-grow">
                <i class="fas fa-check-circle text-emerald-500 mr-3 text-xl" />
                <div>
                  <h4 class="font-medium text-gray-700 line-through">
                    {{ plan.title }}
                  </h4>
                  <p class="text-sm text-gray-500 mt-1">
                    {{ plan.description }}
                  </p>
                </div>
              </div>
              <button
                class="ml-4 px-4 py-2 bg-gray-300 hover:bg-gray-400 text-gray-700 rounded-lg transition-all"
                @click="togglePlanComplete(plan)"
              >
                <i class="fas fa-undo mr-1" />取消完成
              </button>
            </div>
          </div>

          <!-- 空状态 -->
          <div
            v-if="todayPlans.length === 0"
            class="text-center py-12"
          >
            <i class="fas fa-calendar-plus text-gray-300 text-6xl mb-4" />
            <p class="text-gray-500 text-lg">
              今天还没有计划，点击上方按钮添加吧！
            </p>
          </div>
        </div>

        <!-- 月度计划时间表模块 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center space-x-4">
              <button
                class="p-2 rounded-full hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="!canGoPreviousMonth"
                @click="previousMonth"
              >
                <i class="fas fa-chevron-left text-gray-600" />
              </button>
              <h2 class="text-2xl font-bold text-gray-800">
                {{ currentViewYear }}年{{ currentViewMonth }}月
              </h2>
              <button
                class="p-2 rounded-full hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="!canGoNextMonth"
                @click="nextMonth"
              >
                <i class="fas fa-chevron-right text-gray-600" />
              </button>
            </div>
            <CustomButton
              type="secondary"
              icon="fa-calendar-day"
              text="回到当前时间"
              @click="backToToday"
            />
          </div>

          <!-- 月历 -->
          <div class="grid grid-cols-7 gap-2">
            <!-- 星期标题 -->
            <div
              v-for="day in weekDays"
              :key="day"
              class="text-center text-sm font-semibold text-gray-600 py-3"
            >
              {{ day }}
            </div>

            <!-- 日期格子 -->
            <div
              v-for="(date, index) in calendarDays"
              :key="index"
              class="min-h-[120px] border border-gray-200 rounded-lg p-2 transition-all"
              :class="[
                date
                  ? 'bg-white hover:bg-emerald-50 cursor-pointer hover:shadow-md'
                  : 'bg-gray-50',
                isToday(date) ? 'ring-2 ring-emerald-500 bg-emerald-50' : '',
                date && !isCurrentMonth(date) ? 'opacity-40' : '',
              ]"
              @click="date && openPlanModal(date)"
            >
              <div
                v-if="date"
                class="h-full flex flex-col"
              >
                <div class="flex items-center justify-between mb-2">
                  <span
                    class="text-sm font-medium"
                    :class="
                      isToday(date)
                        ? 'text-emerald-600 font-bold'
                        : 'text-gray-700'
                    "
                  >
                    {{ date.getDate() }}
                  </span>
                  <span
                    v-if="getDateUncompletedPlansCount(date) > 0"
                    class="text-xs bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded-full"
                  >
                    {{ getDateUncompletedPlansCount(date) }}
                  </span>
                </div>
                <div class="flex-grow space-y-1 overflow-y-auto">
                  <div
                    v-for="plan in getDateUncompletedPlans(date).slice(0, 3)"
                    :key="plan.id"
                    class="text-xs p-1 rounded truncate flex items-center"
                    :class="getPriorityBgClass(plan.priority)"
                  >
                    <div
                      class="w-1.5 h-1.5 rounded-full mr-1 flex-shrink-0"
                      :class="getPriorityDotClass(plan.priority)"
                    />
                    <span class="truncate">{{ plan.title }}</span>
                  </div>
                  <div
                    v-if="getDateUncompletedPlansCount(date) > 3"
                    class="text-xs text-gray-500 text-center"
                  >
                    +{{ getDateUncompletedPlansCount(date) - 3 }} 更多
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 计划管理弹窗 -->
    <PlanModal
      v-if="showPlanModal"
      :selected-date="selectedDate"
      :plans="getDatePlans(selectedDate)"
      @close="closePlanModal"
      @save="savePlans"
    />

    <!-- 已完成计划管理弹窗 -->
    <CompletedPlansModal
      :visible="showCompletedPlansModal"
      :plans="plans"
      @close="showCompletedPlansModal = false"
      @view-date="handleViewDate"
      @refresh="loadPlans"
    />

    <!-- 底部栏 -->
    <EndBar />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import NavBar from "@/components/common/NavBar.vue";
import ActionButtons from "@/components/common/ActionButtons.vue";
import EndBar from "@/components/common/EndBar.vue";
import CustomButton from "@/components/common/CustomButton.vue";
import PlanModal from "@/components/business/PlanModal.vue";
import CompletedPlansModal from "@/components/business/CompletedPlansModal.vue";
import { planManager } from "@/utils/planData.js";

const router = useRouter();

// 导航项
const navItems = [
  { label: "首页", onClick: () => gotoHome() },
  { label: "课程", onClick: () => gotoCourse() },
  { label: "题库", onClick: () => gotoQuestionBank() },
  { label: "时间表", path: "#", isActive: true },
  { label: "单词打卡", onClick: () => startWordCheckIn() },
  { label: "AI伴学", onClick: () => gotoAiChat() },
];

// 星期数组
const weekDays = ["一", "二", "三", "四", "五", "六", "日"];

// 当前日期
const today = ref(new Date());
today.value.setHours(0, 0, 0, 0);

// 当前视图的年月
const currentViewYear = ref(today.value.getFullYear());
const currentViewMonth = ref(today.value.getMonth() + 1);

// 计划数据（从planManager获取）
const plans = ref([]);

// 弹窗相关
const showPlanModal = ref(false);
const showCompletedPlansModal = ref(false);
const selectedDate = ref(new Date());

// 用户首次添加计划的日期（改为响应式ref）
const firstPlanDate = ref(new Date());
const lastPlanDatePlusYear = ref(new Date());

// 计算lastPlanDatePlusYear
function updateDateRange() {
  if (plans.value.length === 0) {
    const result = new Date(today.value);
    result.setFullYear(result.getFullYear() + 1);
    lastPlanDatePlusYear.value = result;
  } else {
    const lastDate = new Date(
      Math.max(...plans.value.map((p) => new Date(p.date)))
    );
    const result = new Date(lastDate);
    result.setFullYear(result.getFullYear() + 1);
    lastPlanDatePlusYear.value = result;
  }
}

// 今日日期字符串
const todayDateStr = computed(() => {
  const year = today.value.getFullYear();
  const month = today.value.getMonth() + 1;
  const day = today.value.getDate();
  return `${year}年${month}月${day}日`;
});

// 今日计划
const todayPlans = computed(() => {
  const priorityOrder = { high: 1, medium: 2, low: 3 };
  return plans.value
    .filter((plan) => {
      const planDate = new Date(plan.date);
      planDate.setHours(0, 0, 0, 0);
      return planDate.getTime() === today.value.getTime();
    })
    .sort((a, b) => {
      // 未完成的排在前面
      if (a.completed !== b.completed) {
        return a.completed ? 1 : -1;
      }
      // 相同完成状态下，按优先级排序
      return priorityOrder[a.priority] - priorityOrder[b.priority];
    });
});

// 判断是否可以向前翻月
const canGoPreviousMonth = computed(() => {
  const viewDate = new Date(
    currentViewYear.value,
    currentViewMonth.value - 1,
    1
  );
  return viewDate >= firstPlanDate.value;
});

// 判断是否可以向后翻月
const canGoNextMonth = computed(() => {
  const viewDate = new Date(
    currentViewYear.value,
    currentViewMonth.value - 1,
    1
  );
  return viewDate < lastPlanDatePlusYear.value;
});

// 生成月历数据
const calendarDays = computed(() => {
  const year = currentViewYear.value;
  const month = currentViewMonth.value - 1; // JavaScript月份从0开始

  // 当月第一天
  const firstDay = new Date(year, month, 1);
  // 当月最后一天
  const lastDay = new Date(year, month + 1, 0);

  // 第一天是星期几（0=周日，需要转换为1=周一）
  let firstDayOfWeek = firstDay.getDay();
  firstDayOfWeek = firstDayOfWeek === 0 ? 7 : firstDayOfWeek;

  const days = [];

  // 填充上个月的日期
  const prevMonthLastDay = new Date(year, month, 0).getDate();
  for (let i = firstDayOfWeek - 1; i > 0; i--) {
    days.push(new Date(year, month - 1, prevMonthLastDay - i + 1));
  }

  // 填充当月日期
  for (let i = 1; i <= lastDay.getDate(); i++) {
    days.push(new Date(year, month, i));
  }

  // 填充下个月的日期，确保6行
  const remainingDays = 42 - days.length;
  for (let i = 1; i <= remainingDays; i++) {
    days.push(new Date(year, month + 1, i));
  }

  return days;
});

// 获取优先级颜色
function getPriorityColor(priority) {
  const colors = {
    high: "bg-red-500",
    medium: "bg-yellow-500",
    low: "bg-blue-500",
  };
  return colors[priority] || "bg-gray-500";
}

// 判断是否是今天
function isToday(date) {
  if (!date) return false;
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  return d.getTime() === today.value.getTime();
}

// 判断是否是当前查看的月份
function isCurrentMonth(date) {
  if (!date) return false;
  return date.getMonth() === currentViewMonth.value - 1;
}

// 获取指定日期的计划
function getDatePlans(date) {
  if (!date) return [];
  const targetDate = new Date(date);
  targetDate.setHours(0, 0, 0, 0);

  return plans.value.filter((plan) => {
    const planDate = new Date(plan.date);
    planDate.setHours(0, 0, 0, 0);
    return planDate.getTime() === targetDate.getTime();
  });
}

// 获取指定日期未完成的计划（用于月历显示）
function getDateUncompletedPlans(date) {
  const priorityOrder = { high: 1, medium: 2, low: 3 };
  return getDatePlans(date)
    .filter((plan) => !plan.completed)
    .sort((a, b) => priorityOrder[a.priority] - priorityOrder[b.priority]);
}

// 获取指定日期未完成的计划数量
function getDateUncompletedPlansCount(date) {
  return getDateUncompletedPlans(date).length;
}

// 获取优先级背景颜色类
function getPriorityBgClass(priority) {
  const colors = {
    high: "bg-red-50 text-red-700 border-red-200",
    medium: "bg-yellow-50 text-yellow-700 border-yellow-200",
    low: "bg-blue-50 text-blue-700 border-blue-200",
  };
  return colors[priority] || "bg-gray-50 text-gray-700 border-gray-200";
}

// 获取优先级圆点颜色类
function getPriorityDotClass(priority) {
  const colors = {
    high: "bg-red-500",
    medium: "bg-yellow-500",
    low: "bg-blue-500",
  };
  return colors[priority] || "bg-gray-500";
}

// 切换计划完成状态
async function togglePlanComplete(plan) {
  try {
    await planManager.togglePlanComplete(plan.id);
    // 更新本地plans数据
    await loadPlans();
  } catch (error) {
    console.error("切换计划状态失败:", error);
    alert("操作失败，请重试");
  }
}

// 查看指定日期的计划
function handleViewDate(date) {
  showCompletedPlansModal.value = false;
  openPlanModal(date);
}

// 上一个月
function previousMonth() {
  if (!canGoPreviousMonth.value) return;

  if (currentViewMonth.value === 1) {
    currentViewMonth.value = 12;
    currentViewYear.value--;
  } else {
    currentViewMonth.value--;
  }
}

// 下一个月
function nextMonth() {
  if (!canGoNextMonth.value) return;

  if (currentViewMonth.value === 12) {
    currentViewMonth.value = 1;
    currentViewYear.value++;
  } else {
    currentViewMonth.value++;
  }
}

// 回到今天
function backToToday() {
  currentViewYear.value = today.value.getFullYear();
  currentViewMonth.value = today.value.getMonth() + 1;
}

// 打开计划管理弹窗
function openPlanModal(date) {
  selectedDate.value = new Date(date);
  showPlanModal.value = true;
}

// 关闭计划管理弹窗
function closePlanModal() {
  showPlanModal.value = false;
}

// 保存计划
async function savePlans(newPlans) {
  // 使用planManager保存计划
  await planManager.saveDayPlans(selectedDate.value, newPlans);

  // 更新本地plans数据
  await loadPlans();

  closePlanModal();
}

// 返回首页
function gotoHome() {
  router.push({ name: "Home" }).catch(() => {});
}

// 跳转到AI伴学
function gotoAiChat() {
  router.push({ name: "AiChat" }).catch(() => {});
}

// 跳转到单词打卡
function startWordCheckIn() {
  router.push({ name: "WordCheckIn" }).catch(() => {});
}

//跳转课程
function gotoCourse() {
  router.push({ name: "Course" }).catch(() => {});
}

// 跳转到题库
function gotoQuestionBank() {
  router.push({ name: "QuestionBank" }).catch(() => {});
}
// 加载计划数据
async function loadPlans() {
  // 使用扩展运算符创建新数组,确保Vue能检测到变化
  plans.value = [...(await planManager.getAllPlans())];

  // 加载后更新日期范围
  firstPlanDate.value = await planManager.getFirstPlanDate();
  updateDateRange();
}

// 初始化
onMounted(async () => {
  // 从planManager初始化并加载计划数据
  await planManager.initPlans();
  await loadPlans();
});
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
</style>
