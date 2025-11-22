<template>
  <div class="auth-form-panel">
    <h2 class="auth-title">
      创建账号
    </h2>
    <p class="auth-desc">
      注册后即可解锁全部英语学习功能～
    </p>

    <form
      class="auth-form"
      @submit.prevent="handleRegister"
    >
      <!-- 注册用户名 -->
      <div class="form-group">
        <div class="neu-input-wrapper">
          <font-awesome-icon
            icon="user"
            class="input-icon"
          />
          <input
            v-model="username"
            type="text"
            required
            placeholder="请设置用户名"
            class="neu-input"
          >
        </div>
      </div>

      <!-- 注册密码 -->
      <div class="form-group">
        <div class="neu-input-wrapper">
          <font-awesome-icon
            icon="lock"
            class="input-icon"
          />
          <input
            v-model="password"
            :type="pwdVisible ? 'text' : 'password'"
            required
            placeholder="请设置密码"
            class="neu-input"
          >
          <font-awesome-icon
            :icon="pwdVisible ? 'eye-slash' : 'eye'"
            class="pwd-toggle-icon"
            @click="togglePwdVisible"
          />
        </div>
      </div>

      <!-- 确认密码 -->
      <div class="form-group">
        <div class="neu-input-wrapper">
          <font-awesome-icon
            icon="lock"
            class="input-icon"
          />
          <input
            v-model="confirmPwd"
            :type="pwdVisible ? 'text' : 'password'"
            required
            placeholder="请确认密码"
            class="neu-input"
          >
        </div>
        <!-- 密码不一致提示 -->
        <p
          v-if="password && confirmPwd && password !== confirmPwd"
          class="error-tip"
        >
          两次密码输入不一致
        </p>
      </div>

      <!-- 注册按钮（禁用条件：密码不一致） -->
      <button
        type="submit"
        class="neu-btn auth-btn register-btn"
        :disabled="password !== confirmPwd"
      >
        注册
      </button>

      <!-- 切换到登录模式的按钮 -->
      <div class="switch-mode">
        已有账号？
        <span
          class="switch-link"
          @click="$emit('switch-to-login')"
        >去登录</span>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// 接收父组件传递的 props
const props = defineProps({
  pwdVisible: {
    type: Boolean,
    default: false
  },
  onSwitchToLogin: {
    type: Function,
    required: true
  }
});

// 🔥 核心修复：补充缺失的 'switch-to-login' 事件声明
const emit = defineEmits([
  'registerSuccess', 
  'togglePwdVisible',
  'switch-to-login' // 对应模板中 $emit('switch-to-login')
]);

// 表单数据
const username = ref('');
const password = ref('');
const confirmPwd = ref('');

// 切换密码显示/隐藏
const togglePwdVisible = () => {
  emit('togglePwdVisible');
};

// 注册核心逻辑
const handleRegister = () => {
  if (username.value && password.value) {
    // 存储注册用户信息
    localStorage.setItem('registeredUser', JSON.stringify({ username: username.value, password: password.value }));
    // 存储登录状态
    localStorage.setItem('userInfo', JSON.stringify({ username: username.value, isLogin: true }));
    emit('registerSuccess'); // 通知父组件注册成功，触发跳转
    alert('注册成功！已自动为你登录～');
  }
};
</script>

<style scoped>
/* 与 LoginForm 样式复用，仅差异部分修改 */
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

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: 
    4px 4px 8px rgba(16, 185, 129, 0.1),
    -4px -4px 8px rgba(255, 255, 255, 0.7);
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
  .auth-btn {
    height: 52px;
    font-size: 15px;
  }
  .form-group {
    margin-bottom: 24px;
  }
}
</style>