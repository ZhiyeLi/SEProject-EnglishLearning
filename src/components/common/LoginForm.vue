<template>
  <div class="auth-form-panel">
    <h2 class="auth-title">
      登录账号
    </h2>
    <p class="auth-desc">
      欢迎回来，继续探索英语学习之旅～
    </p>

    <!-- 登录表单 -->
    <form
      class="auth-form"
      @submit.prevent="handleLogin"
    >
      <!-- 用户名输入框 -->
      <div class="form-group">
        <div class="neu-input-wrapper">
          <font-awesome-icon
            icon="user"
            class="input-icon"
          />
          <input
            v-model="form.username"
            type="text"
            required
            placeholder="请输入用户名"
            class="neu-input"
            :disabled="isLoading"
          >
        </div>
        <p
          v-if="errors.username"
          class="error-tip"
        >
          {{ errors.username }}
        </p>
      </div>

      <!-- 密码输入框（带显示/隐藏） -->
      <div class="form-group">
        <div class="neu-input-wrapper">
          <font-awesome-icon
            icon="lock"
            class="input-icon"
          />
          <input
            v-model="form.password"
            :type="pwdVisible ? 'text' : 'password'"
            required
            placeholder="请输入密码"
            class="neu-input"
            :disabled="isLoading"
          >
          <font-awesome-icon
            :icon="pwdVisible ? 'eye-slash' : 'eye'"
            class="pwd-toggle-icon"
            @click="togglePwdVisible"
          />
        </div>
        <p
          v-if="errors.password"
          class="error-tip"
        >
          {{ errors.password }}
        </p>
      </div>

      <!-- 忘记密码链接 -->
      <div class="forgot-password">
        <a
          href="javascript:;"
          @click="openForgotPwdModal"
        >忘记密码？</a>
      </div>

      <!-- 登录按钮（带loading状态） -->
      <button
        type="submit"
        class="neu-btn auth-btn login-btn"
        :disabled="isLoading"
      >
        <span v-if="!isLoading">登录</span>
        <span v-if="isLoading">登录中...</span>
      </button>

      <!-- 切换到注册 -->
      <div class="switch-mode">
        还没有账号？
        <span
          class="switch-link"
          @click="$emit('switch-to-register')"
        >立即注册</span>
      </div>
    </form>

    <!-- 忘记密码弹窗 -->
    <div
      v-if="isForgotPwdModalOpen"
      class="modal-mask"
    >
      <div class="modal-container">
        <div class="modal-header">
          <h3 class="modal-title">
            重置密码
          </h3>
          <!-- 右上角返回按钮（文本为「返回」，统一关闭入口） -->
          <button
            class="modal-back-btn"
            :disabled="isForgotLoading"
            @click="closeForgotPwdModal"
          >
            返回
          </button>
        </div>
        <div class="modal-content">
          <!-- 步骤1：输入绑定的手机号/邮箱 -->
          <div v-if="forgotStep === 1">
            <div class="form-group">
              <!-- 外层容器，包含输入框和切换按钮 -->
              <div class="account-input-group">
                <div class="neu-input-wrapper">
                  <font-awesome-icon
                    :icon="isPhone ? 'phone' : 'envelope'"
                    class="input-icon"
                  />
                  <input
                    v-model="forgotForm.account"
                    :type="isPhone ? 'tel' : 'email'"
                    required
                    :placeholder="isPhone ? '请输入绑定的手机号' : '请输入绑定的邮箱'"
                    class="neu-input"
                    :disabled="isForgotLoading"
                  >
                </div>
                <!-- 切换按钮放在输入框右侧 -->
                <button
                  class="switch-account-type-btn"
                  @click="toggleAccountType"
                  :disabled="isForgotLoading"
                >
                  {{ isPhone ? '切换邮箱' : '切换手机号' }}
                </button>
              </div>
              <p
                v-if="forgotErrors.account"
                class="error-tip"
              >
                {{ forgotErrors.account }}
              </p>
            </div>
            <button
              class="neu-btn auth-btn"
              :disabled="isForgotLoading || !forgotForm.account"
              @click="sendVerifyCode"
            >
              <span v-if="!isForgotLoading && !countdown">发送验证码</span>
              <span v-if="isForgotLoading">发送中...</span>
              <span v-if="countdown > 0">{{ countdown }}s后重发</span>
            </button>
          </div>

          <!-- 步骤2：输入验证码+重置密码 -->
          <div v-if="forgotStep === 2">
            <!-- 验证码输入 -->
            <div class="form-group">
              <div class="neu-input-wrapper">
                <font-awesome-icon
                  icon="code"
                  class="input-icon"
                />
                <input
                  v-model="forgotForm.verifyCode"
                  type="text"
                  required
                  placeholder="请输入收到的验证码"
                  class="neu-input"
                  :disabled="isForgotLoading"
                >
              </div>
              <p
                v-if="forgotErrors.verifyCode"
                class="error-tip"
              >
                {{ forgotErrors.verifyCode }}
              </p>
            </div>

            <!-- 新密码 -->
            <div class="form-group">
              <div class="neu-input-wrapper">
                <font-awesome-icon
                  icon="lock"
                  class="input-icon"
                />
                <input
                  v-model="forgotForm.newPassword"
                  :type="newPwdVisible ? 'text' : 'password'"
                  required
                  placeholder="请设置新密码"
                  class="neu-input"
                  :disabled="isForgotLoading"
                >
                <font-awesome-icon
                  :icon="newPwdVisible ? 'eye-slash' : 'eye'"
                  class="pwd-toggle-icon"
                  @click="toggleNewPwdVisible"
                />
              </div>
              <p
                v-if="forgotErrors.newPassword"
                class="error-tip"
              >
                {{ forgotErrors.newPassword }}
              </p>
            </div>

            <!-- 确认新密码 -->
            <div class="form-group">
              <div class="neu-input-wrapper">
                <font-awesome-icon
                  icon="lock"
                  class="input-icon"
                />
                <input
                  v-model="forgotForm.confirmPassword"
                  :type="newPwdVisible ? 'text' : 'password'"
                  required
                  placeholder="请确认新密码"
                  class="neu-input"
                  :disabled="isForgotLoading"
                >
              </div>
              <p
                v-if="forgotErrors.confirmPassword"
                class="error-tip"
              >
                {{ forgotErrors.confirmPassword || (forgotForm.newPassword && forgotForm.confirmPassword && forgotForm.newPassword !== forgotForm.confirmPassword ? '两次密码输入不一致' : '') }}
              </p>
            </div>

            <div class="btn-group">
              <button
                class="neu-btn cancel-btn"
                :disabled="isForgotLoading"
                @click="forgotStep = 1"
              >
                上一步
              </button>
              <button
                class="neu-btn auth-btn"
                :disabled="isForgotLoading || !forgotForm.verifyCode || !forgotForm.newPassword || !forgotForm.confirmPassword"
                @click="resetPassword"
              >
                <span v-if="!isForgotLoading">确认重置</span>
                <span v-if="isForgotLoading">重置中...</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useUserStore } from '../../store/modules/user';
import { ElMessage } from 'element-plus'; // 引入ElementUI消息组件

// 父组件传递的props
const props = defineProps({
  pwdVisible: {
    type: Boolean,
    default: false
  }
});

const userStore = useUserStore();
const emit = defineEmits(['loginSuccess', 'togglePwdVisible', 'switch-to-register']);

// 登录表单数据
const form = reactive({
  username: '',
  password: ''
});

// 登录错误提示
const errors = reactive({
  username: '',
  password: ''
});

// 加载状态
const isLoading = ref(false);

// 切换密码显示/隐藏
const togglePwdVisible = () => {
  emit('togglePwdVisible');
};

// 登录核心逻辑（优化版）
const handleLogin = async () => {
  // 重置错误提示
  errors.username = '';
  errors.password = '';
  
  // 前端表单验证
  if (!form.username.trim()) {
    errors.username = '请输入用户名';
    return;
  }
  if (!form.password) {
    errors.password = '请输入密码';
    return;
  }

  try {
    isLoading.value = true;
    
    // 调用store中的登录方法
    const result = await userStore.login({
      username: form.username.trim(),
      password: form.password
    });

    if (result.success) {
      ElMessage.success('登录成功');
      emit('loginSuccess');
    } else {
      // 根据后端返回的错误信息显示对应提示
      if (result.message.includes('用户名')) {
        errors.username = result.message;
      } else if (result.message.includes('密码')) {
        errors.password = result.message;
      } else {
        ElMessage.error(result.message);
      }
    }
  } catch (error) {
    console.error('登录异常：', error);
    ElMessage.error('登录失败，请稍后重试');
  } finally {
    isLoading.value = false;
  }
};

// 忘记密码相关逻辑（补充完善）
const isForgotPwdModalOpen = ref(false);
const forgotStep = ref(1);
const forgotForm = reactive({
  account: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: ''
});
const forgotErrors = reactive({
  account: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: ''
});
const isForgotLoading = ref(false);
const isPhone = ref(true);
const countdown = ref(0);
const newPwdVisible = ref(false);

// 打开忘记密码弹窗
const openForgotPwdModal = () => {
  isForgotPwdModalOpen.value = true;
  forgotStep.value = 1;
  // 重置表单
  Object.keys(forgotForm).forEach(key => {
    forgotForm[key] = '';
  });
  Object.keys(forgotErrors).forEach(key => {
    forgotErrors[key] = '';
  });
};

// 关闭忘记密码弹窗
const closeForgotPwdModal = () => {
  isForgotPwdModalOpen.value = false;
  countdown.value = 0;
};

// 切换账号类型（手机/邮箱）
const toggleAccountType = () => {
  isPhone.value = !isPhone.value;
  forgotForm.account = '';
  forgotErrors.account = '';
};

// 切换新密码显示/隐藏
const toggleNewPwdVisible = () => {
  newPwdVisible.value = !newPwdVisible.value;
};

// 发送验证码
const sendVerifyCode = async () => {
  forgotErrors.account = '';
  
  if (!forgotForm.account.trim()) {
    forgotErrors.account = `请输入${isPhone.value ? '手机号' : '邮箱'}`;
    return;
  }

  try {
    isForgotLoading.value = true;
    const result = await userStore.sendVerifyCode({
      account: forgotForm.account.trim(),
      type: isPhone.value ? 'phone' : 'email'
    });

    if (result.success) {
      ElMessage.success('验证码发送成功');
      // 开始倒计时
      countdown.value = 60;
      const timer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
          clearInterval(timer);
        }
      }, 1000);
      // 进入下一步
      forgotStep.value = 2;
    } else {
      forgotErrors.account = result.message;
    }
  } catch (error) {
    console.error('发送验证码异常：', error);
    ElMessage.error('发送失败，请稍后重试');
  } finally {
    isForgotLoading.value = false;
  }
};

// 重置密码
const resetPassword = async () => {
  // 重置错误提示
  Object.keys(forgotErrors).forEach(key => {
    forgotErrors[key] = '';
  });

  // 前端验证
  if (!forgotForm.verifyCode.trim()) {
    forgotErrors.verifyCode = '请输入验证码';
    return;
  }
  if (forgotForm.newPassword.length < 6) {
    forgotErrors.newPassword = '密码长度不能少于6位';
    return;
  }
  if (forgotForm.newPassword !== forgotForm.confirmPassword) {
    forgotErrors.confirmPassword = '两次密码输入不一致';
    return;
  }

  try {
    isForgotLoading.value = true;
    const result = await userStore.resetPassword({
      account: forgotForm.account.trim(),
      type: isPhone.value ? 'phone' : 'email',
      verifyCode: forgotForm.verifyCode.trim(),
      newPassword: forgotForm.newPassword
    });

    if (result.success) {
      ElMessage.success('密码重置成功，请使用新密码登录');
      closeForgotPwdModal();
    } else {
      forgotErrors.verifyCode = result.message;
    }
  } catch (error) {
    console.error('重置密码异常：', error);
    ElMessage.error('重置失败，请稍后重试');
  } finally {
    isForgotLoading.value = false;
  }
};
</script>

<style scoped>
.auth-form-panel {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.auth-title {
  font-size: 28px;
  color: #0f766e;
  margin: 0 0 12px 0;
  font-weight: 600;
  text-align: center;
}

.auth-desc {
  font-size: 14px;
  color: #374151;
  text-align: center;
  margin: 0 0 40px 0;
}

.form-group {
  margin-bottom: 28px;
  position: relative;
}

/* 新增：账号输入框+切换按钮外层容器 */
.account-input-group {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.neu-input-wrapper {
  position: relative;
  width: 100%;
  background-color: #f0faf4;
  border-radius: 16px;
  box-shadow: 
    inset 4px 4px 8px rgba(16, 185, 129, 0.1),
    inset -4px -4px 8px rgba(255, 255, 255, 0.7);
  padding: 0 20px;
  box-sizing: border-box;
  flex: 1; /* 新增：占满剩余宽度 */
}

.neu-input {
  width: 100%;
  height: 56px;
  line-height: 56px;
  background: transparent;
  border: none;
  outline: none;
  font-size: 16px;
  color: #374151;
  padding-left: 40px;
  box-sizing: border-box; /* 新增：确保padding不超出宽度 */
}

/* 账号输入框特殊处理：右侧留空间 */
.account-input-group .neu-input {
  padding-right: 20px; /* 避免内容贴边 */
}

.neu-input:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.neu-input:focus + .input-icon {
  color: #10b981;
}

.neu-input:focus ~ .pwd-toggle-icon {
  color: #10b981;
}

.input-icon {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #6b7280;
  width: 22px;
  height: 22px;
  transition: color 0.3s ease;
}

.pwd-toggle-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #6b7280;
  width: 22px;
  height: 22px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.pwd-toggle-icon:hover {
  color: #10b981;
}

.error-tip {
  font-size: 12px;
  color: #ef4444;
  margin-top: 8px;
  margin-left: 12px;
  height: 16px;
}

.forgot-password {
  text-align: right;
  margin-bottom: 32px;
}

.forgot-password a {
  font-size: 14px;
  color: #10b981;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.forgot-password a:hover {
  color: #059669;
  text-decoration: underline;
}

.neu-btn {
  border: none;
  outline: none;
  cursor: pointer;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 
    6px 6px 12px rgba(16, 185, 129, 0.15),
    -6px -6px 12px rgba(255, 255, 255, 0.8);
}

.neu-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: 
    4px 4px 8px rgba(16, 185, 129, 0.1),
    -4px -4px 8px rgba(255, 255, 255, 0.7);
}

.neu-btn:active {
  box-shadow: 
    inset 4px 4px 8px rgba(16, 185, 129, 0.2),
    inset -4px -4px 8px rgba(255, 255, 255, 0.7);
}

.auth-btn {
  height: 56px;
  width: 100%;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #fff;
}

.switch-mode {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #374151;
}

.switch-link {
  color: #10b981;
  cursor: pointer;
  font-weight: 500;
  margin-left: 4px;
  transition: color 0.3s ease;
}

.switch-link:hover {
  color: #059669;
}

/* 忘记密码弹窗样式 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  box-sizing: border-box;
}

.modal-container {
  width: 100%;
  max-width: 380px;
  background-color: #f0faf4;
  border-radius: 24px;
  box-shadow: 
    12px 12px 24px rgba(16, 185, 129, 0.2),
    -12px -12px 24px rgba(255, 255, 255, 0.9);
  padding: 30px;
  box-sizing: border-box;
  position: relative;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.modal-title {
  font-size: 22px;
  color: #0f766e;
  margin: 0;
  font-weight: 600;
}

/* 右上角返回按钮样式（新拟态风格，与界面统一） */
.modal-back-btn {
  background-color: #f0faf4;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  color: #6b7280;
  cursor: pointer;
  padding: 6px 12px;
  box-shadow: 
    3px 3px 6px rgba(16, 185, 129, 0.1),
    -3px -3px 6px rgba(255, 255, 255, 0.7);
  transition: all 0.3s ease;
}

.modal-back-btn:hover {
  color: #10b981;
  box-shadow: 
    4px 4px 8px rgba(16, 185, 129, 0.15),
    -4px -4px 8px rgba(255, 255, 255, 0.8);
}

.modal-back-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.modal-content {
  width: 100%;
}

/* 新增：切换账号类型按钮样式（独立按钮） */
.switch-account-type-btn {
  white-space: nowrap;
  padding: 10px 16px;
  background-color: #f0faf4;
  border: none;
  border-radius: 12px;
  color: #10b981;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 
    3px 3px 6px rgba(16, 185, 129, 0.1),
    -3px -3px 6px rgba(255, 255, 255, 0.7);
  transition: all 0.3s ease;
  height: 56px; /* 与输入框同高 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.switch-account-type-btn:hover {
  color: #059669;
  box-shadow: 
    4px 4px 8px rgba(16, 185, 129, 0.15),
    -4px -4px 8px rgba(255, 255, 255, 0.8);
}

.switch-account-type-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 废弃原有绝对定位按钮样式 */
.switch-account-type {
  display: none;
}

.btn-group {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.cancel-btn {
  flex: 1;
  height: 56px;
  background-color: #f0faf4;
  color: #374151;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .auth-title {
    font-size: 24px;
  }
  .neu-input {
    height: 52px;
    line-height: 52px;
    font-size: 15px;
  }
  .switch-account-type-btn {
    height: 52px;
    padding: 8px 12px;
    font-size: 13px;
  }
  .auth-btn, .cancel-btn {
    height: 52px;
    font-size: 15px;
  }
  .form-group {
    margin-bottom: 24px;
  }
  .modal-container {
    padding: 24px 20px;
  }
  .modal-title {
    font-size: 20px;
  }
  .modal-back-btn {
    padding: 5px 10px;
    font-size: 15px;
  }
  /* 移动端弹性布局换行 */
  .account-input-group {
    flex-wrap: wrap;
  }
  .switch-account-type-btn {
    width: 100%;
    margin-top: 10px;
  }
}
</style>