<template>
  <div class="auth-form-panel">
    <h2 class="auth-title">登录账号</h2>
    <p class="auth-desc">欢迎回来，继续探索英语学习之旅～</p>

    <!-- 登录表单 -->
    <form class="auth-form" @submit.prevent="handleLogin">
      <!-- 用户名输入框 -->
      <div class="form-group">
        <div class="neu-input-wrapper">
          <font-awesome-icon icon="user" class="input-icon" />
          <input
            v-model="form.username"
            type="text"
            required
            placeholder="请输入用户名"
            class="neu-input"
            :disabled="isLoading"
          >
        </div>
        <p class="error-tip" v-if="errors.username">{{ errors.username }}</p>
      </div>

      <!-- 密码输入框（带显示/隐藏） -->
      <div class="form-group">
        <div class="neu-input-wrapper">
          <font-awesome-icon icon="lock" class="input-icon" />
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
        <p class="error-tip" v-if="errors.password">{{ errors.password }}</p>
      </div>

      <!-- 忘记密码链接 -->
      <div class="forgot-password">
        <a href="javascript:;" @click="openForgotPwdModal">忘记密码？</a>
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
        <span class="switch-link" @click="$emit('switch-to-register')">立即注册</span>
      </div>
    </form>

    <!-- 忘记密码弹窗 -->
    <div class="modal-mask" v-if="isForgotPwdModalOpen">
      <div class="modal-container">
        <div class="modal-header">
          <h3 class="modal-title">重置密码</h3>
          <!-- 右上角返回按钮（文本为「返回」，统一关闭入口） -->
          <button
            class="modal-back-btn"
            @click="closeForgotPwdModal"
            :disabled="isForgotLoading"
          >
            返回
          </button>
        </div>
        <div class="modal-content">
          <!-- 步骤1：输入绑定的手机号/邮箱 -->
          <div v-if="forgotStep === 1">
            <div class="form-group">
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
                <span class="switch-account-type" @click="toggleAccountType">
                  {{ isPhone ? '切换邮箱' : '切换手机号' }}
                </span>
              </div>
              <p class="error-tip" v-if="forgotErrors.account">{{ forgotErrors.account }}</p>
            </div>
            <button
              class="neu-btn auth-btn"
              @click="sendVerifyCode"
              :disabled="isForgotLoading || !forgotForm.account"
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
                <font-awesome-icon icon="code" class="input-icon" />
                <input
                  v-model="forgotForm.verifyCode"
                  type="text"
                  required
                  placeholder="请输入收到的验证码"
                  class="neu-input"
                  :disabled="isForgotLoading"
                >
              </div>
              <p class="error-tip" v-if="forgotErrors.verifyCode">{{ forgotErrors.verifyCode }}</p>
            </div>

            <!-- 新密码 -->
            <div class="form-group">
              <div class="neu-input-wrapper">
                <font-awesome-icon icon="lock" class="input-icon" />
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
              <p class="error-tip" v-if="forgotErrors.newPassword">{{ forgotErrors.newPassword }}</p>
            </div>

            <!-- 确认新密码 -->
            <div class="form-group">
              <div class="neu-input-wrapper">
                <font-awesome-icon icon="lock" class="input-icon" />
                <input
                  v-model="forgotForm.confirmPassword"
                  :type="newPwdVisible ? 'text' : 'password'"
                  required
                  placeholder="请确认新密码"
                  class="neu-input"
                  :disabled="isForgotLoading"
                >
              </div>
              <p class="error-tip" v-if="forgotErrors.confirmPassword">
                {{ forgotErrors.confirmPassword || (forgotForm.newPassword && forgotForm.confirmPassword && forgotForm.newPassword !== forgotForm.confirmPassword ? '两次密码输入不一致' : '') }}
              </p>
            </div>

            <div class="btn-group">
              <button
                class="neu-btn cancel-btn"
                @click="forgotStep = 1"
                :disabled="isForgotLoading"
              >
                上一步
              </button>
              <button
                class="neu-btn auth-btn"
                @click="resetPassword"
                :disabled="isForgotLoading || !forgotForm.verifyCode || !forgotForm.newPassword || !forgotForm.confirmPassword"
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
import axios from 'axios';

// 父组件传递的props
const props = defineProps({
  pwdVisible: {
    type: Boolean,
    default: false
  }
});

// 暴露给父组件的事件
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

// 登录核心逻辑（对接后端接口）
const handleLogin = async () => {
  // 表单验证
  errors.username = '';
  errors.password = '';
  if (!form.username) {
    errors.username = '请输入用户名';
    return;
  }
  if (!form.password) {
    errors.password = '请输入密码';
    return;
  }

  try {
    isLoading.value = true;
    // 调用后端登录接口
    const response = await axios.post('/api/auth/login', {
      username: form.username,
      password: form.password
    });

    if (response.data.code === 200) {
      localStorage.setItem('token', response.data.data.token);
      localStorage.setItem('userInfo', JSON.stringify(response.data.data.userInfo));
      emit('loginSuccess');
    } else {
      if (response.data.message.includes('用户名')) {
        errors.username = response.data.message;
      } else {
        errors.password = response.data.message;
      }
    }
  } catch (error) {
    errors.password = '登录失败，请稍后重试';
    console.error('登录接口异常：', error);
  } finally {
    isLoading.value = false;
  }
};

// ---------------------- 忘记密码功能 ----------------------
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
const countdown = ref(0);
const isPhone = ref(true);
const newPwdVisible = ref(false);

// 打开忘记密码弹窗
const openForgotPwdModal = () => {
  isForgotPwdModalOpen.value = true;
  forgotForm.account = '';
  forgotForm.verifyCode = '';
  forgotForm.newPassword = '';
  forgotForm.confirmPassword = '';
  forgotErrors.account = '';
  forgotErrors.verifyCode = '';
  forgotErrors.newPassword = '';
  forgotErrors.confirmPassword = '';
  forgotStep.value = 1;
  countdown.value = 0;
};

// 关闭忘记密码弹窗（返回登录界面）
const closeForgotPwdModal = () => {
  isForgotPwdModalOpen.value = false;
};

// 切换账号类型（手机号/邮箱）
const toggleAccountType = () => {
  isPhone.value = !isPhone.value;
  forgotErrors.account = '';
};

// 切换新密码显示/隐藏
const toggleNewPwdVisible = () => {
  newPwdVisible.value = !newPwdVisible.value;
};

// 发送验证码（对接后端接口）
const sendVerifyCode = async () => {
  forgotErrors.account = '';
  const reg = isPhone.value 
    ? /^1[3-9]\d{9}$/ 
    : /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+$/;

  if (!forgotForm.account) {
    forgotErrors.account = isPhone.value ? '请输入手机号' : '请输入邮箱';
    return;
  }
  if (!reg.test(forgotForm.account)) {
    forgotErrors.account = isPhone.value ? '请输入正确的手机号' : '请输入正确的邮箱';
    return;
  }

  try {
    isForgotLoading.value = true;
    const response = await axios.post('/api/auth/send-verify-code', {
      account: forgotForm.account,
      type: isPhone.value ? 'phone' : 'email'
    });

    if (response.data.code === 200) {
      countdown.value = 60;
      const timer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
          clearInterval(timer);
        }
      }, 1000);
    } else {
      forgotErrors.account = response.data.message;
    }
  } catch (error) {
    forgotErrors.account = '验证码发送失败，请稍后重试';
    console.error('发送验证码接口异常：', error);
  } finally {
    isForgotLoading.value = false;
  }
};

// 重置密码（对接后端接口）
const resetPassword = async () => {
  forgotErrors.verifyCode = '';
  forgotErrors.newPassword = '';
  forgotErrors.confirmPassword = '';

  if (!forgotForm.verifyCode) {
    forgotErrors.verifyCode = '请输入验证码';
    return;
  }
  if (!forgotForm.newPassword) {
    forgotErrors.newPassword = '请设置新密码';
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
    const response = await axios.post('/api/auth/reset-password', {
      account: forgotForm.account,
      type: isPhone.value ? 'phone' : 'email',
      verifyCode: forgotForm.verifyCode,
      newPassword: forgotForm.newPassword
    });

    if (response.data.code === 200) {
      alert('密码重置成功，请使用新密码登录');
      closeForgotPwdModal();
    } else {
      forgotErrors.verifyCode = response.data.message;
    }
  } catch (error) {
    forgotErrors.confirmPassword = '密码重置失败，请稍后重试';
    console.error('重置密码接口异常：', error);
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

.switch-account-type {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #10b981;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.switch-account-type:hover {
  color: #059669;
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
}
</style>