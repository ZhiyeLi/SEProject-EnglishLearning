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
          <p class="text-gray-800 font-medium mt-2">
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
                <select class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4">
                  <option>四级词汇</option>
                  <option>六级词汇</option>
                  <option>考研词汇</option>
                  <option>GRE词汇</option>
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
                <select class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4">
                  <option>标准模式（1,3,7,15,30天）</option>
                  <option>加速模式（1,2,4,7,15天）</option>
                  <option>缓速模式（1,5,10,20,30天）</option>
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
                  <option value="small">
                    小
                  </option>
                  <option value="normal">
                    标准
                  </option>
                  <option value="large">
                    大
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
                <select class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4">
                  <option>中文简体</option>
                  <option>中文繁体</option>
                  <option>English</option>
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
                <select class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4">
                  <option>公开</option>
                  <option>仅好友可见</option>
                  <option>隐私</option>
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
                <select class="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent flex-shrink-0 ml-4">
                  <option>所有人</option>
                  <option>仅现有好友推荐</option>
                  <option>需要通过验证</option>
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
              class="flex-1 bg-emerald-500 hover:bg-emerald-600 text-white py-3 rounded-lg font-semibold transition-all shadow-md hover:shadow-lg transform hover:-translate-y-0.5"
              @click="saveSettings"
            >
              <i class="fas fa-save mr-2" />
              保存设置
            </button>
            <button
              class="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-700 py-3 rounded-lg font-semibold transition-all"
              @click="resetSettings"
            >
              <i class="fas fa-undo mr-2" />
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

const router = useRouter();
const userStore = useUserStore();
const isEditpwdOpen = ref(false); // 控制编辑对话框显示/隐藏
const isEditemailOpen = ref(false); // 控制编辑对话框显示/隐藏
// 响应式数据
const dailyGoal = ref(50);
const remindTime = ref('08:00');
const settings = ref({
  checkInReminder: true,
  suggestionsReminder: true,
  messageReminder: true,
  darkMode: false,
  fontSize: 'normal',
  shareScore: true,
});


// 初始化设置（从 localStorage 加载）
onMounted(() => {
  const savedSettings = localStorage.getItem('appSettings');
  if (savedSettings) {
    try {
      const parsed = JSON.parse(savedSettings);
      settings.value = { ...settings.value, ...parsed };
    } catch (e) {
      console.error('Failed to load settings:', e);
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
});

// 保存设置
const saveSettings = () => {
  try {
    localStorage.setItem('appSettings', JSON.stringify(settings.value));
    localStorage.setItem('dailyGoal', dailyGoal.value.toString());
    localStorage.setItem('remindTime', remindTime.value);
    
    // 提示成功
    const message = document.createElement('div');
    message.className = 'fixed top-4 right-4 bg-emerald-500 text-white px-6 py-3 rounded-lg shadow-lg animate-bounce';
    message.textContent = '✓ 设置已保存';
    document.body.appendChild(message);
    
    setTimeout(() => {
      message.remove();
    }, 3000);
  } catch (error) {
    console.error('Failed to save settings:', error);
    alert('保存设置失败！');
  }
};

// 重置设置
const resetSettings = () => {
  if (confirm('确定要重置所有设置为默认值吗？')) {
    settings.value = {
      checkInReminder: true,
      suggestionsReminder: true,
      messageReminder: true,
      darkMode: false,
      fontSize: 'normal',
      shareScore: true,
    };
    dailyGoal.value = 50;
    remindTime.value = '08:00';
    saveSettings();
  }
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
