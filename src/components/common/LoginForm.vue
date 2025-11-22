<template>
  <div class="auth-form-panel">
    <h2 class="auth-title">
      登录账号
    </h2>
    <p class="auth-desc">
      欢迎回来，继续探索英语学习之旅～
    </p>

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
            v-model="username"
            type="text"
            required
            placeholder="请输入用户名"
            class="neu-input"
          >
        </div>
      </div>

      <!-- 密码输入框（带显示/隐藏） -->
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
            placeholder="请输入密码"
            class="neu-input"
          >
          <font-awesome-icon
            :icon="pwdVisible ? 'eye-slash' : 'eye'"
            class="pwd-toggle-icon"
            @click="togglePwdVisible"
          />
        </div>
      </div>

      <!-- 忘记密码链接 -->
      <div class="forgot-password">
        <a href="javascript:;">忘记密码？</a>
      </div>

      <!-- 登录按钮 -->
      <button
        type="submit"
        class="neu-btn auth-btn login-btn"
      >
        登录
      </button>

      <!-- 切换到注册（调用父组件传递的方法） -->
      <div class="switch-mode">
        还没有账号？
        <span
          class="switch-link"
          @click="$emit('switch-to-register')"
        >立即注册</span>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// 接收父组件传递的 props
const props = defineProps({
  // 密码显示状态（由父组件统一管理，可选，也可子组件独立管理）
  pwdVisible: {
    type: Boolean,
    default: false
  },
  // 切换到注册模式的方法（父组件传递）
  onSwitchToRegister: {
    type: Function,
    required: true
  }
});

// 🔥 核心修复：显式声明组件所有要触发的事件（包括之前缺失的2个）
const emit = defineEmits([
  'loginSuccess', // 原有事件
  'switch-to-register', // 模板中 $emit 触发的事件
  'togglePwdVisible' // 方法中 emit 触发的事件
]);

// 表单数据
const username = ref('');
const password = ref('');

// 切换密码显示/隐藏（子组件内部逻辑）
const togglePwdVisible = () => {
  emit('togglePwdVisible'); // 通知父组件切换状态（逻辑不变）
};

// 登录核心逻辑
const handleLogin = () => {
  if (username.value && password.value) {
    // 存储登录状态（也可后续移到父组件，子组件仅传递数据）
    localStorage.setItem('userInfo', JSON.stringify({
      username: username.value,
      isLogin: true
    }));
    emit('loginSuccess'); // 通知父组件登录成功，触发跳转（逻辑不变）
  } else {
    alert('请输入完整的用户名和密码');
  }
};
</script>

<style scoped>
/* 登录表单样式（仅表单内部样式，布局样式在父组件） */
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

.forgot-password {
  text-align: right;
  margin-bottom: 32px;
}

.forgot-password a {
  font-size: 14px;
  color: #10b981;
  text-decoration: none;
  transition: all 0.3s ease;
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