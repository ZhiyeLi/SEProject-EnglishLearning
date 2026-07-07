<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 导航栏 -->
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
    <main class="flex-grow">
      <div class="max-w-4xl mx-auto px-6 py-8">
        <!-- 页面标题 -->
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-gray-900 flex items-center">
            <i class="fas fa-cog text-emerald-600 mr-3" />
            设置
          </h1>
        </div>

        <!-- 返回按钮 -->
        <button
          class="mb-6 flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
          @click="gotoHome"
        >
          <i class="fas fa-arrow-left" />
          <span>返回首页</span>
        </button>

        <!-- 设置内容 -->
        <div class="space-y-6">
          <!-- 1. 账户与隐私设置 -->
          <div
            class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 transform transition-all duration-300 hover:shadow-md"
          >
            <h2
              class="text-xl font-semibold text-gray-800 mb-4 flex items-center"
            >
              <i class="fas fa-user-circle text-emerald-500 mr-3" />
              账户与隐私
            </h2>
            <div class="space-y-4">
              <div
                class="flex items-center justify-between py-3 border-b border-gray-100 last:border-b-0"
              >
                <div class="text-left">
                  <p class="text-gray-800 font-medium">修改密码</p>
                  <p class="text-sm text-gray-600">
                    定期修改密码以保护账户安全
                  </p>
                </div>
                <button
                  class="px-4 py-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-lg transition-colors flex-shrink-0 ml-4"
                  @click="isEditpwdOpen = true"
                >
                  修改
                </button>
              </div>
              <div
                class="flex items-center justify-between py-3 border-b border-gray-100 last:border-b-0"
              >
                <div class="text-left">
                  <p class="text-gray-800 font-medium">绑定邮箱</p>
                  <p class="text-sm text-gray-600">
                    {{ userStore.userInfo.email || "未绑定" }}
                  </p>
                </div>
                <button
                  class="px-4 py-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-lg transition-colors flex-shrink-0 ml-4"
                  @click="isEditemailOpen = true"
                >
                  {{ userStore.userInfo.email ? "修改" : "绑定" }}
                </button>
              </div>
              <div class="flex items-center justify-between py-3">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">账户注销</p>
                  <p class="text-sm text-gray-600">
                    永久删除你的账户和所有数据
                  </p>
                </div>
                <button
                  class="px-4 py-2 bg-red-50 hover:bg-red-100 text-red-700 rounded-lg transition-colors flex-shrink-0 ml-4"
                >
                  注销
                </button>
              </div>
            </div>
          </div>

          <!-- 学习偏好（已移除） -->

          <!-- 通知提醒（已移除） -->

          <!-- 显示与界面（已移除） -->

          <!-- 数据管理（已移除） -->

          <!-- 隐私与安全（已移除） -->

          <!-- 关于与反馈（已移除） -->

          <!-- 保存/重置 按钮已移除 -->
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <EndBar />
    <!-- 学习建议弹窗 -->
    <SuggestionModal
      :visible="showSuggestionsModal"
      title="学习建议"
      :items="suggestionsData"
      @close="showSuggestionsModal = false"
    />
    <!-- 编辑资料对话框（引入组件） -->
    <EditPassword v-model:open="isEditpwdOpen" /><!-- 双向绑定对话框显示状态 -->
    <!-- 编辑资料对话框（引入组件） -->
    <EditEmail v-model:open="isEditemailOpen" /><!-- 双向绑定对话框显示状态 -->
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/modules/user";
import NavBar from "@/components/common/NavBar.vue";
import ActionButtons from "@/components/common/ActionButtons.vue";
import EndBar from "@/components/common/EndBar.vue";
import EditPassword from "@/components/profile/EditPassword.vue"; // 引入编辑组件
import EditEmail from "@/components/profile/EditEmail.vue"; // 引入编辑组件
import SuggestionModal from "@/components/common/SuggestionModal.vue";
import { getSettings } from "@/api/settings";

const router = useRouter();
const userStore = useUserStore();
const isEditpwdOpen = ref(false); // 控制编辑对话框显示/隐藏
const isEditemailOpen = ref(false); // 控制编辑对话框显示/隐藏
const isLoading = ref(false); // 加载状态

// 可选项数据
const vocabularyDifficultyOptions = ref([]); // 词汇难度选项
const reviewStrategyOptions = ref([]); // 复习策略选项
const profileVisibilityOptions = ref([]); // 个人资料可见范围选项
const friendRequestModeOptions = ref([]); // 好友请求管理选项
const languageOptions = ref([]); // 语言选项
const fontSizeOptions = ref([]); // 字体大小选项

// 响应式数据
const dailyGoal = ref(50);
const remindTime = ref("08:00");
const vocabularyDifficulty = ref("");
const reviewStrategy = ref("");
const profileVisibility = ref("");
const friendRequestMode = ref("");
const language = ref("");

const settings = ref({
  checkInReminder: true,
  suggestionsReminder: true,
  messageReminder: true,
  darkMode: false,
  fontSize: "normal",
  shareScore: true,
});

// 初始化设置（从后端加载）
onMounted(async () => {
  isLoading.value = true;

  // 先加载默认选项，确保即使出错也有选项显示
  loadDefaultOptions();

  try {
    const response = await getSettings();
    if (response.code === 200) {
      const data = response.data;

      // 加载选项（覆盖默认值）
      if (data.options) {
        const opts = data.options;
        vocabularyDifficultyOptions.value =
          opts.vocabularyDifficulty || vocabularyDifficultyOptions.value;
        reviewStrategyOptions.value =
          opts.reviewStrategy || reviewStrategyOptions.value;
        profileVisibilityOptions.value =
          opts.profileVisibility || profileVisibilityOptions.value;
        friendRequestModeOptions.value =
          opts.friendRequestMode || friendRequestModeOptions.value;
        languageOptions.value = opts.language || languageOptions.value;
        fontSizeOptions.value = opts.fontSize || fontSizeOptions.value;
      }

      // 加载用户设置
      dailyGoal.value = data.dailyGoal || 50;
      remindTime.value = data.remindTime || "08:00";
      vocabularyDifficulty.value =
        data.vocabularyDifficulty ||
        vocabularyDifficultyOptions.value[0] ||
        "四级词汇";
      reviewStrategy.value =
        data.reviewStrategy ||
        reviewStrategyOptions.value[0] ||
        "标准模式（1,3,7,15,30天）";
      profileVisibility.value =
        data.profileVisibility || profileVisibilityOptions.value[0] || "公开";
      friendRequestMode.value =
        data.friendRequestMode || friendRequestModeOptions.value[0] || "所有人";
      language.value = data.language || languageOptions.value[0] || "中文简体";

      settings.value = {
        checkInReminder: data.checkInReminder !== false,
        suggestionsReminder: data.suggestionsReminder !== false,
        messageReminder: data.messageReminder !== false,
        darkMode: data.darkMode === true,
        fontSize: data.fontSize || "normal",
        shareScore: data.shareScore !== false,
      };
    } else {
      console.warn("获取设置失败:", response.message);
      // 加载本地缓存作为备选
      loadLocalSettings();
    }
  } catch (error) {
    console.error("Failed to load settings:", error);
    // 加载本地缓存作为备选
    loadLocalSettings();
  } finally {
    isLoading.value = false;
  }
});

// 加载默认可选项（备选方案）
const loadDefaultOptions = () => {
  vocabularyDifficultyOptions.value = [
    "四级词汇",
    "六级词汇",
    "考研词汇",
    "GRE词汇",
  ];
  reviewStrategyOptions.value = [
    "标准模式（1,3,7,15,30天）",
    "加速模式（1,2,4,7,15天）",
    "缓速模式（1,5,10,20,30天）",
  ];
  profileVisibilityOptions.value = ["公开", "仅好友可见", "隐私"];
  friendRequestModeOptions.value = ["所有人", "仅现有好友推荐", "需要通过验证"];
  languageOptions.value = ["中文简体", "中文繁体", "English"];
  fontSizeOptions.value = [
    { label: "小", value: "small" },
    { label: "标准", value: "normal" },
    { label: "大", value: "large" },
  ];
};

// 加载本地缓存（备选方案）
const loadLocalSettings = () => {
  const savedSettings = localStorage.getItem("appSettings");
  if (savedSettings) {
    try {
      const parsed = JSON.parse(savedSettings);
      settings.value = { ...settings.value, ...parsed };
    } catch (e) {
      console.error("Failed to load local settings:", e);
    }
  }

  const savedGoal = localStorage.getItem("dailyGoal");
  if (savedGoal) {
    dailyGoal.value = Number.parseInt(savedGoal, 10);
  }

  const savedRemindTime = localStorage.getItem("remindTime");
  if (savedRemindTime) {
    remindTime.value = savedRemindTime;
  }
};

// Note: saveSettings handler removed (UI buttons removed)

// Note: resetSettings handler removed (UI buttons removed)

// Note: toast helper removed (alerts replaced globally in main.js)

const gotoHome = () => {
  router.push("/").catch(() => {});
};

const gotoWordCheckIn = () => {
  router.push({ name: "WordCheckIn" }).catch(() => {});
};

const gotoAiChat = () => {
  router.push({ name: "AiChat" }).catch(() => {});
};

const gotoTimeTable = () => {
  router.push({ name: "TimeTable" }).catch(() => {});
};

const gotoCourse = () => {
  router.push({ name: "Course" }).catch(() => {});
};

const gotoQuestionBank = () => {
  router.push({ name: "QuestionBank" }).catch(() => {});
};

const navItems = [
  { label: "首页", onClick: gotoHome, isActive: false },
  { label: "课程", onClick: gotoCourse, isActive: false },
  { label: "题库", onClick: gotoQuestionBank, isActive: false },
  { label: "时间表", onClick: gotoTimeTable },
  { label: "单词打卡", onClick: gotoWordCheckIn, isActive: false },
  { label: "AI伴学", onClick: gotoAiChat, isActive: false },
];
// ===== 学习建议弹窗相关逻辑 =====
const showSuggestionsModal = ref(false);
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

</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css");

/* 平滑滚动 */
html {
  scroll-behavior: smooth;
}
</style>
