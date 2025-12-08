<template>
  <div class="auth-form-panel">
    <h2 class="auth-title">创建账号</h2>
    <p class="auth-desc">注册后即可解锁全部英语学习功能～</p>

    <form class="auth-form" @submit.prevent="handleRegister">
      <!-- 注册用户名 -->
      <div class="form-group">
        <div class="neu-input-wrapper">
          <font-awesome-icon icon="user" class="input-icon" />
          <input
            v-model="form.username"
            type="text"
            required
            placeholder="请设置用户名（4-20位）"
            class="neu-input"
            :disabled="isLoading"
          />
        </div>
        <p v-if="errors.username" class="error-tip">
          {{ errors.username }}
        </p>
      </div>

      <!-- 注册邮箱 -->
      <div class="form-group">
        <div class="neu-input-wrapper">
          <font-awesome-icon icon="envelope" class="input-icon" />
          <input
            v-model="form.email"
            type="email"
            required
            placeholder="请输入邮箱地址"
            class="neu-input"
            :disabled="isLoading"
          />
        </div>
        <p v-if="errors.email" class="error-tip">
          {{ errors.email }}
        </p>
      </div>

      <!-- 注册密码 -->
      <div class="form-group">
        <div class="neu-input-wrapper">
          <font-awesome-icon icon="lock" class="input-icon" />
          <input
            v-model="form.password"
            :type="pwdVisible ? 'text' : 'password'"
            required
            placeholder="请设置密码（6-20位）"
            class="neu-input"
            :disabled="isLoading"
          />
          <font-awesome-icon
            :icon="pwdVisible ? 'eye-slash' : 'eye'"
            class="pwd-toggle-icon"
            @click="togglePwdVisible"
          />
        </div>
        <p v-if="errors.password" class="error-tip">
          {{ errors.password }}
        </p>
      </div>

      <!-- 确认密码 -->
      <div class="form-group">
        <div class="neu-input-wrapper">
          <font-awesome-icon icon="lock" class="input-icon" />
          <input
            v-model="form.confirmPwd"
            :type="pwdVisible ? 'text' : 'password'"
            required
            placeholder="请确认密码"
            class="neu-input"
            :disabled="isLoading"
          />
        </div>
        <!-- 密码不一致提示（前端即时校验） -->
        <p
          v-if="
            form.password &&
            form.confirmPwd &&
            form.password !== form.confirmPwd &&
            !errors.confirmPwd
          "
          class="error-tip"
        >
          两次密码输入不一致
        </p>
        <p v-if="errors.confirmPwd" class="error-tip">
          {{ errors.confirmPwd }}
        </p>
      </div>

      <!-- 注册按钮（禁用条件：加载中/表单未通过前端校验） -->
      <button
        type="submit"
        class="neu-btn auth-btn register-btn"
        :disabled="
          isLoading ||
          !form.username ||
          !form.email ||
          !form.password ||
          !form.confirmPwd ||
          form.password !== form.confirmPwd
        "
      >
        <span v-if="!isLoading">注册</span>
        <span v-if="isLoading">注册中...</span>
      </button>

      <!-- 切换到登录模式的按钮 -->
      <div class="switch-mode">
        已有账号？
        <span
          class="switch-link"
          :disabled="isLoading"
          @click="$emit('switch-to-login')"
          >去登录</span
        >
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { authApi } from "@/api";

// 接收父组件传递的 props
const props = defineProps({
  pwdVisible: {
    type: Boolean,
    default: false,
  },
});

// 暴露给父组件的事件
const emit = defineEmits([
  "registerSuccess",
  "togglePwdVisible",
  "switch-to-login",
]);

// 注册表单数据（与后端请求体字段一致）
const form = reactive({
  username: "", // 用户名
  email: "", // 邮箱
  password: "", // 密码（前端仅收集，后端加密存储）
  confirmPwd: "", // 确认密码（仅前端校验，不传给后端）
});

// 错误提示（区分字段，对应后端返回的错误类型）
const errors = reactive({
  username: "",
  email: "",
  password: "",
  confirmPwd: "",
});

// 加载状态（防止重复提交）
const isLoading = ref(false);

// 切换密码显示/隐藏
const togglePwdVisible = () => {
  emit("togglePwdVisible");
};

// 注册核心逻辑（标准化接口交互，方便后端对接）
const handleRegister = async () => {
  // 1. 前端预校验（减少无效接口请求）
  errors.username = "";
  errors.email = "";
  errors.password = "";
  errors.confirmPwd = "";

  // 用户名校验（4-20位，支持字母、数字、下划线）
  const usernameReg = /^[a-zA-Z0-9_]{4,20}$/;
  if (!usernameReg.test(form.username)) {
    errors.username = "用户名需为4-20位字母、数字或下划线";
    return;
  }

  // 邮箱校验
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailReg.test(form.email)) {
    errors.email = "请输入有效的邮箱地址";
    return;
  }

  // 密码校验（6-20位，不限制字符类型）
  if (form.password.length < 6 || form.password.length > 20) {
    errors.password = "密码长度需在6-20位之间";
    return;
  }

  // 两次密码一致性校验
  if (form.password !== form.confirmPwd) {
    errors.confirmPwd = "两次密码输入不一致";
    return;
  }

  try {
    isLoading.value = true;
    // 2. 调用后端注册接口
    const response = await authApi.register({
      username: form.username,
      email: form.email,
      password: form.password,
    });

    // 3. 标准化响应处理
    if (response.code === 200) {
      // 注册成功：通知父组件跳转
      emit("registerSuccess", response.data);
      alert("注册成功！请前往登录");
    } else {
      // 后端返回具体错误（如用户名已存在），按字段显示
      if (response.data.message.includes("用户名")) {
        errors.username = response.data.message;
      } else if (response.data.message.includes("密码")) {
        errors.password = response.data.message;
      } else {
        // 全局错误提示
        alert(response.data.message);
      }
    }
  } catch (error) {
    // 网络错误或后端异常处理
    errors.username = "注册失败，请稍后重试";
    console.error("注册接口异常：", error);
  } finally {
    isLoading.value = false;
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
