<template>
  <div class="auth-page">
    <div id="auth-container" class="auth-container">
      <!-- 合并为一个 transition 组件，通过 key 区分登录/注册面板 -->
      <transition name="slide">
        <!-- 登录面板 -->
        <div v-if="isLoginMode" key="login" class="auth-panel">
          <LoginForm
            :pwd-visible="pwdVisible"
            @toggle-pwd-visible="togglePwdVisible"
            @switch-to-register="switchToRegister"
            @login-success="handleAuthSuccess"
          />
        </div>

        <!-- 注册面板（v-else 与 v-if 相邻） -->
        <div v-else key="register" class="auth-panel">
          <RegisterForm
            :pwd-visible="pwdVisible"
            @toggle-pwd-visible="togglePwdVisible"
            @switch-to-login="switchToLogin"
            @register-success="handleAuthSuccess"
          />
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
// 脚本逻辑不变
import { ref } from "vue";
import { useRouter } from "vue-router";
import LoginForm from "@/components/common/LoginForm.vue";
import RegisterForm from "@/components/common/RegisterForm.vue";

const router = useRouter();
const isLoginMode = ref(true);
const pwdVisible = ref(false);

const switchToRegister = () => {
  isLoginMode.value = false;
  window.scrollTo(0, 0);
};
const switchToLogin = () => {
  isLoginMode.value = true;
  window.scrollTo(0, 0);
};
const togglePwdVisible = () => {
  pwdVisible.value = !pwdVisible.value;
};
const handleAuthSuccess = () => {
  // 如果当前是注册模式，注册成功后切换到登录模式
  if (!isLoginMode.value) {
    switchToLogin();
  } else {
    // 登录成功，跳转到主页
    router.push("/");
  }
};
</script>

<style scoped>
/* 样式不变，仅保留核心动画和定位 */
.auth-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0faf4;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}

#auth-container {
  width: 380px;
  height: 650px;
  background-color: #f0faf4;
  border-radius: 24px;
  box-shadow:
    12px 12px 24px rgba(16, 185, 129, 0.15),
    -12px -12px 24px rgba(255, 255, 255, 0.8);
  overflow: auto;
  position: relative;
}

.auth-panel {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  padding: 50px 30px;
  box-sizing: border-box;
  background-color: #f0faf4;
}

/* 滑入滑出动画 */
.slide-enter-from {
  transform: translateX(100%);
}
.slide-enter-active {
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.slide-enter-to {
  transform: translateX(0);
}

.slide-leave-from {
  transform: translateX(0);
}
.slide-leave-active {
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.slide-leave-to {
  transform: translateX(-100%);
}

/* 反向动画（注册切登录） */
.slide-reverse-enter-from {
  transform: translateX(-100%);
}
.slide-reverse-leave-to {
  transform: translateX(100%);
}

@media (max-width: 480px) {
  #auth-container {
    width: 90vw;
  }
  .auth-panel {
    padding: 40px 20px;
  }
}
</style>
