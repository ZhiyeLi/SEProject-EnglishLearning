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
    <main class="flex-grow">
      <div class="max-w-4xl mx-auto px-6 py-8">
        <!-- 页面标题 -->
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-gray-900 flex items-center">
            <i class="fas fa-cog text-emerald-600 mr-3" />
            设置
          </h1>
          <p class="text-4xl font-bold mt-4 text-center">
            <span class="text-red-600">自</span><span class="text-orange-600">定</span><span class="text-amber-600">义</span><span class="text-yellow-600">你</span><span class="text-lime-600">的</span><span class="text-emerald-600">学</span><span class="text-teal-600">习</span><span class="text-cyan-600">体</span><span class="text-blue-600">验</span>
          </p>
        </div>

        <!-- 返回按钮 -->
        <button
          class="mb-6 flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
          @click="goHome"
        >
          <i class="fas fa-arrow-left" />
          <span>返回首页</span>
        </button>

        <!-- 设置内容 -->
        <div class="space-y-6">
          <!-- 1. 账户与隐私设置 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 transform transition-all duration-300 hover:shadow-md">
            <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
              <i class="fas fa-user-circle text-emerald-500 mr-3" />
              账户与隐私
            </h2>
            <div class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b border-gray-100 last:border-b-0">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    修改密码
                  </p>
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
              <div class="flex items-center justify-between py-3 border-b border-gray-100 last:border-b-0">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    绑定邮箱
                  </p>
                  <p class="text-sm text-gray-600">
                    {{ userStore.userInfo.email || '未绑定' }}
                  </p>
                </div>
                <button
                  class="px-4 py-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-lg transition-colors flex-shrink-0 ml-4"
                  @click="isEditemailOpen = true"
                >
                  {{ userStore.userInfo.email ? '修改' : '绑定' }}
                </button>
              </div>
              <div class="flex items-center justify-between py-3">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    账户注销
                  </p>
                  <p class="text-sm text-gray-600">
                    永久删除你的账户和所有数据
                  </p>
                </div>
                <button class="px-4 py-2 bg-red-50 hover:bg-red-100 text-red-700 rounded-lg transition-colors flex-shrink-0 ml-4">
                  注销
                </button>
              </div>
            </div>
          </div>

          <!-- 2. 学习偏好设置 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 transform transition-all duration-300 hover:shadow-md">
            <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
              <i class="fas fa-book text-emerald-500 mr-3" />
              学习偏好
            </h2>
            <div class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    词汇难度
                  </p>
                  <p class="text-sm text-gray-600">
                    选择适合你的学习难度
                  </p>
                </div>
                <select
                  v-model="vocabularyDifficulty"
                  class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4"
                >
                  <option
                    v-for="option in vocabularyDifficultyOptions"
                    :key="option"
                    :value="option"
                  >
                    {{ option }}
                  </option>
                </select>
              </div>
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    每日目标单词数
                  </p>
                  <p class="text-sm text-gray-600">
                    设置每天要背的单词数量
                  </p>
                </div>
                <div class="flex items-center gap-3 flex-shrink-0 ml-4">
                  <input
                    v-model="dailyGoal"
                    type="number"
                    min="10"
                    max="500"
                    class="w-20 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  >
                  <span class="text-gray-600">个</span>
                </div>
              </div>
              <div class="flex items-center justify-between py-3">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    复习策略
                  </p>
                  <p class="text-sm text-gray-600">
                    选择艾宾浩斯遗忘曲线周期
                  </p>
                </div>
                <select
                  v-model="reviewStrategy"
                  class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4"
                >
                  <option
                    v-for="option in reviewStrategyOptions"
                    :key="option"
                    :value="option"
                  >
                    {{ option }}
                  </option>
                </select>
              </div>
            </div>
          </div>

          <!-- 3. 通知提醒设置 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 transform transition-all duration-300 hover:shadow-md">
            <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
              <i class="fas fa-bell text-emerald-500 mr-3" />
              通知提醒
            </h2>
            <div class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    打卡提醒
                  </p>
                  <p class="text-sm text-gray-600">
                    每天提醒你去完成单词打卡
                  </p>
                </div>
                <div class="relative inline-flex items-center cursor-pointer flex-shrink-0 ml-4">
                  <input
                    id="checkInReminder"
                    v-model="settings.checkInReminder"
                    type="checkbox"
                    class="sr-only peer"
                  >
                  <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-emerald-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600" />
                </div>
              </div>
              <div
                v-if="settings.checkInReminder"
                class="flex items-center justify-between py-3 border-b border-gray-100"
              >
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    提醒时间
                  </p>
                  <p class="text-sm text-gray-600">
                    每天的提醒时间
                  </p>
                </div>
                <input
                  v-model="remindTime"
                  type="time"
                  class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4"
                >
              </div>
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    学习建议通知
                  </p>
                  <p class="text-sm text-gray-600">
                    接收AI学习建议和推荐
                  </p>
                </div>
                <div class="relative inline-flex items-center cursor-pointer flex-shrink-0 ml-4">
                  <input
                    id="suggestionsReminder"
                    v-model="settings.suggestionsReminder"
                    type="checkbox"
                    class="sr-only peer"
                  >
                  <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-emerald-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600" />
                </div>
              </div>
              <div class="flex items-center justify-between py-3">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    消息提醒
                  </p>
                  <p class="text-sm text-gray-600">
                    接收好友请求和聊天消息通知
                  </p>
                </div>
                <div class="relative inline-flex items-center cursor-pointer flex-shrink-0 ml-4">
                  <input
                    id="messageReminder"
                    v-model="settings.messageReminder"
                    type="checkbox"
                    class="sr-only peer"
                  >
                  <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-emerald-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600" />
                </div>
              </div>
            </div>
          </div>

          <!-- 4. 显示与界面设置 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 transform transition-all duration-300 hover:shadow-md">
            <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
              <i class="fas fa-palette text-emerald-500 mr-3" />
              显示与界面
            </h2>
            <div class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    深色模式
                  </p>
                  <p class="text-sm text-gray-600">
                    更护眼的深色界面
                  </p>
                </div>
                <div class="relative inline-flex items-center cursor-pointer flex-shrink-0 ml-4">
                  <input
                    id="darkMode"
                    v-model="settings.darkMode"
                    type="checkbox"
                    class="sr-only peer"
                  >
                  <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-emerald-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600" />
                </div>
              </div>
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    字体大小
                  </p>
                  <p class="text-sm text-gray-600">
                    调整整个应用的字体大小
                  </p>
                </div>
                <select
                  v-model="settings.fontSize"
                  class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4"
                >
                  <option
                    v-for="option in fontSizeOptions"
                    :key="typeof option === 'object' ? option.value : option"
                    :value="typeof option === 'object' ? option.value : option"
                  >
                    {{ typeof option === 'object' ? option.label : option }}
                  </option>
                </select>
              </div>
              <div class="flex items-center justify-between py-3">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    界面语言
                  </p>
                  <p class="text-sm text-gray-600">
                    选择应用语言
                  </p>
                </div>
                <select
                  v-model="language"
                  class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4"
                >
                  <option
                    v-for="option in languageOptions"
                    :key="option"
                    :value="option"
                  >
                    {{ option }}
                  </option>
                </select>
              </div>
            </div>
          </div>

          <!-- 5. 数据管理 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 transform transition-all duration-300 hover:shadow-md">
            <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
              <i class="fas fa-database text-emerald-500 mr-3" />
              数据管理
            </h2>
            <div class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    导出学习数据
                  </p>
                  <p class="text-sm text-gray-600">
                    将学习记录导出为 CSV 或 JSON 格式
                  </p>
                </div>
                <div class="flex gap-2 flex-shrink-0 ml-4">
                  <button class="px-4 py-2 bg-blue-50 hover:bg-blue-100 text-blue-700 rounded-lg transition-colors text-sm">
                    CSV
                  </button>
                  <button class="px-4 py-2 bg-blue-50 hover:bg-blue-100 text-blue-700 rounded-lg transition-colors text-sm">
                    JSON
                  </button>
                </div>
              </div>
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    清空本地缓存
                  </p>
                  <p class="text-sm text-gray-600">
                    清除本地存储数据但保留账户信息
                  </p>
                </div>
                <button class="px-4 py-2 bg-yellow-50 hover:bg-yellow-100 text-yellow-700 rounded-lg transition-colors flex-shrink-0 ml-4">
                  清空
                </button>
              </div>
              <div class="flex items-center justify-between py-3">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    重置所有数据
                  </p>
                  <p class="text-sm text-gray-600">
                    彻底重置应用（需要确认）
                  </p>
                </div>
                <button class="px-4 py-2 bg-red-50 hover:bg-red-100 text-red-700 rounded-lg transition-colors flex-shrink-0 ml-4">
                  重置
                </button>
              </div>
            </div>
          </div>

          <!-- 6. 隐私与安全 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 transform transition-all duration-300 hover:shadow-md">
            <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
              <i class="fas fa-lock text-emerald-500 mr-3" />
              隐私与安全
            </h2>
            <div class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    个人资料可见范围
                  </p>
                  <p class="text-sm text-gray-600">
                    设置谁可以看到你的学习信息
                  </p>
                </div>
                <select
                  v-model="profileVisibility"
                  class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4"
                >
                  <option
                    v-for="option in profileVisibilityOptions"
                    :key="option"
                    :value="option"
                  >
                    {{ option }}
                  </option>
                </select>
              </div>
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    学习成绩分享
                  </p>
                  <p class="text-sm text-gray-600">
                    是否在排行榜中显示你的成绩
                  </p>
                </div>
                <div class="relative inline-flex items-center cursor-pointer flex-shrink-0 ml-4">
                  <input
                    id="shareScore"
                    v-model="settings.shareScore"
                    type="checkbox"
                    class="sr-only peer"
                  >
                  <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-emerald-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600" />
                </div>
              </div>
              <div class="flex items-center justify-between py-3">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    好友请求管理
                  </p>
                  <p class="text-sm text-gray-600">
                    控制谁可以添加你为好友
                  </p>
                </div>
                <select
                  v-model="friendRequestMode"
                  class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4"
                >
                  <option
                    v-for="option in friendRequestModeOptions"
                    :key="option"
                    :value="option"
                  >
                    {{ option }}
                  </option>
                </select>
              </div>
            </div>
          </div>

          <!-- 7. 关于与反馈 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 transform transition-all duration-300 hover:shadow-md">
            <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
              <i class="fas fa-info-circle text-emerald-500 mr-3" />
              关于与反馈
            </h2>
            <div class="space-y-4">
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    应用版本
                  </p>
                  <p class="text-sm text-gray-600">
                    SEProject-EnglishLearning
                  </p>
                </div>
                <span class="text-gray-700 font-semibold flex-shrink-0 ml-4">v1.0.0</span>
              </div>
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    检查更新
                  </p>
                  <p class="text-sm text-gray-600">
                    查看是否有新版本可用
                  </p>
                </div>
                <button class="px-4 py-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-lg transition-colors flex-shrink-0 ml-4">
                  检查
                </button>
              </div>
              <div class="flex items-center justify-between py-3 border-b border-gray-100">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    意见反馈
                  </p>
                  <p class="text-sm text-gray-600">
                    告诉我们如何改进应用
                  </p>
                </div>
                <button class="px-4 py-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-lg transition-colors flex-shrink-0 ml-4">
                  反馈
                </button>
              </div>
              <div class="flex items-center justify-between py-3">
                <div class="text-left">
                  <p class="text-gray-800 font-medium">
                    用户协议与隐私政策
                  </p>
                  <p class="text-sm text-gray-600">
                    阅读我们的服务条款
                  </p>
                </div>
                <button class="px-4 py-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-lg transition-colors flex-shrink-0 ml-4">
                  查看
                </button>
              </div>
            </div>
          </div>

          <!-- 保存按钮 -->
          <div class="flex gap-4 pt-6">
            <button
              :disabled="isLoading"
              :class="[
                'flex-1 py-3 rounded-lg font-semibold transition-all shadow-md hover:shadow-lg transform hover:-translate-y-0.5',
                isLoading ? 'bg-emerald-400 text-white cursor-not-allowed' : 'bg-emerald-500 hover:bg-emerald-600 text-white'
              ]"
              @click="saveSettings"
            >
              <i :class="['fas', isLoading ? 'fa-spinner fa-spin' : 'fa-save', 'mr-2']" />
              {{ isLoading ? '保存中...' : '保存设置' }}
            </button>
            <button
              :disabled="isLoading"
              :class="[
                'flex-1 py-3 rounded-lg font-semibold transition-all',
                isLoading ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-gray-200 hover:bg-gray-300 text-gray-700'
              ]"
              @click="resetSettings"
            >
              <i :class="['fas', isLoading ? 'fa-spinner fa-spin' : 'fa-undo', 'mr-2']" />
              重置
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <EndBar />
    <!-- 编辑资料对话框（引入组件） -->
    <EditPassword 
      v-model:open="isEditpwdOpen" 
    /><!-- 双向绑定对话框显示状态 -->
    <!-- 编辑资料对话框（引入组件） -->
    <EditEmail 
      v-model:open="isEditemailOpen" 
    /><!-- 双向绑定对话框显示状态 -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import NavBar from '@/components/common/NavBar.vue';
import ActionButtons from '@/components/common/ActionButtons.vue';
import EndBar from '@/components/common/EndBar.vue';
import EditPassword from '@/components/profile/EditPassword.vue'; // 引入编辑组件
import EditEmail from '@/components/profile/EditEmail.vue'; // 引入编辑组件
import { getSettings, updateSettings, resetSettings as resetSettingsAPI } from '@/api/settings';

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
const remindTime = ref('08:00');
const vocabularyDifficulty = ref('');
const reviewStrategy = ref('');
const profileVisibility = ref('');
const friendRequestMode = ref('');
const language = ref('');

const settings = ref({
  checkInReminder: true,
  suggestionsReminder: true,
  messageReminder: true,
  darkMode: false,
  fontSize: 'normal',
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
        vocabularyDifficultyOptions.value = opts.vocabularyDifficulty || vocabularyDifficultyOptions.value;
        reviewStrategyOptions.value = opts.reviewStrategy || reviewStrategyOptions.value;
        profileVisibilityOptions.value = opts.profileVisibility || profileVisibilityOptions.value;
        friendRequestModeOptions.value = opts.friendRequestMode || friendRequestModeOptions.value;
        languageOptions.value = opts.language || languageOptions.value;
        fontSizeOptions.value = opts.fontSize || fontSizeOptions.value;
      }
      
      // 加载用户设置
      dailyGoal.value = data.dailyGoal || 50;
      remindTime.value = data.remindTime || '08:00';
      vocabularyDifficulty.value = data.vocabularyDifficulty || vocabularyDifficultyOptions.value[0] || '四级词汇';
      reviewStrategy.value = data.reviewStrategy || reviewStrategyOptions.value[0] || '标准模式（1,3,7,15,30天）';
      profileVisibility.value = data.profileVisibility || profileVisibilityOptions.value[0] || '公开';
      friendRequestMode.value = data.friendRequestMode || friendRequestModeOptions.value[0] || '所有人';
      language.value = data.language || languageOptions.value[0] || '中文简体';
      
      settings.value = {
        checkInReminder: data.checkInReminder !== false,
        suggestionsReminder: data.suggestionsReminder !== false,
        messageReminder: data.messageReminder !== false,
        darkMode: data.darkMode === true,
        fontSize: data.fontSize || 'normal',
        shareScore: data.shareScore !== false,
      };
    } else {
      console.warn('获取设置失败:', response.message);
      // 加载本地缓存作为备选
      loadLocalSettings();
    }
  } catch (error) {
    console.error('Failed to load settings:', error);
    // 加载本地缓存作为备选
    loadLocalSettings();
  } finally {
    isLoading.value = false;
  }
});

// 加载默认可选项（备选方案）
const loadDefaultOptions = () => {
  vocabularyDifficultyOptions.value = ['四级词汇', '六级词汇', '考研词汇', 'GRE词汇'];
  reviewStrategyOptions.value = ['标准模式（1,3,7,15,30天）', '加速模式（1,2,4,7,15天）', '缓速模式（1,5,10,20,30天）'];
  profileVisibilityOptions.value = ['公开', '仅好友可见', '隐私'];
  friendRequestModeOptions.value = ['所有人', '仅现有好友推荐', '需要通过验证'];
  languageOptions.value = ['中文简体', '中文繁体', 'English'];
  fontSizeOptions.value = [
    { label: '小', value: 'small' },
    { label: '标准', value: 'normal' },
    { label: '大', value: 'large' }
  ];
};

// 加载本地缓存（备选方案）
const loadLocalSettings = () => {
  const savedSettings = localStorage.getItem('appSettings');
  if (savedSettings) {
    try {
      const parsed = JSON.parse(savedSettings);
      settings.value = { ...settings.value, ...parsed };
    } catch (e) {
      console.error('Failed to load local settings:', e);
    }
  }

  const savedGoal = localStorage.getItem('dailyGoal');
  if (savedGoal) {
    dailyGoal.value = Number.parseInt(savedGoal, 10);
  }

  const savedRemindTime = localStorage.getItem('remindTime');
  if (savedRemindTime) {
    remindTime.value = savedRemindTime;
  }
};

// 保存设置
const saveSettings = async () => {
  isLoading.value = true;
  try {
    // 构建完整的负载数据
    const payload = {
      dailyGoal: dailyGoal.value,
      remindTime: remindTime.value,
      vocabularyDifficulty: vocabularyDifficulty.value,
      reviewStrategy: reviewStrategy.value,
      profileVisibility: profileVisibility.value,
      friendRequestMode: friendRequestMode.value,
      language: language.value,
      checkInReminder: settings.value.checkInReminder,
      suggestionsReminder: settings.value.suggestionsReminder,
      messageReminder: settings.value.messageReminder,
      darkMode: settings.value.darkMode,
      fontSize: settings.value.fontSize,
      shareScore: settings.value.shareScore,
    };

    const response = await updateSettings(payload);
    
    if (response.code === 200) {
      // 响应拦截器已经在成功时显示了提示，这里额外显示
      showSuccessMessage('✓ 设置已保存');
      
      // 同时保存到本地存储（作为备选方案）
      const allSettings = {
        ...payload,
        options: {
          vocabularyDifficulty: vocabularyDifficultyOptions.value,
          reviewStrategy: reviewStrategyOptions.value,
          profileVisibility: profileVisibilityOptions.value,
          friendRequestMode: friendRequestModeOptions.value,
          language: languageOptions.value,
          fontSize: fontSizeOptions.value
        }
      };
      localStorage.setItem('appSettings', JSON.stringify(allSettings));
    }
  } catch (error) {
    // 错误已经由响应拦截器处理了，这里额外显示
    console.error('Failed to save settings:', error);
    // 不再重复显示错误，因为拦截器已经显示了
  } finally {
    isLoading.value = false;
  }
};

// 重置设置
const resetSettings = async () => {
  if (confirm('确定要重置所有设置为默认值吗？')) {
    isLoading.value = true;
    try {
      const response = await resetSettingsAPI();
      
      if (response.code === 200) {
        const data = response.data;
        
        // 重新加载数据库中的默认值
        dailyGoal.value = data.dailyGoal || 50;
        remindTime.value = data.remindTime || '08:00';
        vocabularyDifficulty.value = data.vocabularyDifficulty || vocabularyDifficultyOptions.value[0] || '四级词汇';
        reviewStrategy.value = data.reviewStrategy || reviewStrategyOptions.value[0] || '标准模式（1,3,7,15,30天）';
        profileVisibility.value = data.profileVisibility || profileVisibilityOptions.value[0] || '公开';
        friendRequestMode.value = data.friendRequestMode || friendRequestModeOptions.value[0] || '所有人';
        language.value = data.language || languageOptions.value[0] || '中文简体';
        
        settings.value = {
          checkInReminder: data.checkInReminder !== false,
          suggestionsReminder: data.suggestionsReminder !== false,
          messageReminder: data.messageReminder !== false,
          darkMode: data.darkMode === true,
          fontSize: data.fontSize || 'normal',
          shareScore: data.shareScore !== false,
        };
        
        // 同步更新本地缓存
        const allSettings = {
          dailyGoal: dailyGoal.value,
          remindTime: remindTime.value,
          vocabularyDifficulty: vocabularyDifficulty.value,
          reviewStrategy: reviewStrategy.value,
          profileVisibility: profileVisibility.value,
          friendRequestMode: friendRequestMode.value,
          language: language.value,
          ...settings.value,
          options: {
            vocabularyDifficulty: vocabularyDifficultyOptions.value,
            reviewStrategy: reviewStrategyOptions.value,
            profileVisibility: profileVisibilityOptions.value,
            friendRequestMode: friendRequestModeOptions.value,
            language: languageOptions.value,
            fontSize: fontSizeOptions.value
          }
        };
        localStorage.setItem('appSettings', JSON.stringify(allSettings));
        
        showSuccessMessage('✓ 设置已重置为默认值');
      }
    } catch (error) {
      console.error('Failed to reset settings:', error);
    } finally {
      isLoading.value = false;
    }
  }
};

// 显示成功消息
const showSuccessMessage = (message) => {
  const messageDiv = document.createElement('div');
  messageDiv.className = 'fixed top-4 right-4 bg-emerald-500 text-white px-6 py-3 rounded-lg shadow-lg animate-bounce z-50';
  messageDiv.textContent = message;
  document.body.appendChild(messageDiv);
  
  setTimeout(() => {
    messageDiv.remove();
  }, 3000);
};

const gotoHome = () => {
  router.push('/').catch(() => {});
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
  { label: "课程", onClick: gotoCourse,isActive: false },
  { label: "题库", onClick: gotoQuestionBank,isActive: false },
  { label: "时间表", onClick: gotoTimeTable },
  { label: "单词打卡", onClick: gotoWordCheckIn, isActive: false },
  { label: "AI伴学", onClick: gotoAiChat, isActive: false },
];
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css");

/* 平滑滚动 */
html {
  scroll-behavior: smooth;
}
</style>
