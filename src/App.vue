<template>
  <router-view />
</template>

<script setup>
import { onMounted } from "vue";
import { useUserStore } from "@/store/modules/user";
import { useRouter } from "vue-router";

const userStore = useUserStore();
const router = useRouter();

onMounted(async () => {
  // 页面加载时恢复用户登录状态
  const token = localStorage.getItem("token");
  if (token) {
    // 如果有token,尝试获取用户信息
    const result = await userStore.fetchUserInfo();
    if (!result.success) {
      // 如果获取失败(token过期等),清除token并跳转登录
      localStorage.removeItem("token");
      userStore.logout();
      // 只在非登录页时跳转
      if (router.currentRoute.value.name !== "Login") {
        router.push({ name: "Login" });
      }
    }
  } else {
    // 没有token,尝试从localStorage恢复
    userStore.loadUserFromLocalStorage();
  }
});
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
