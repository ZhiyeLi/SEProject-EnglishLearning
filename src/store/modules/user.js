import { defineStore } from "pinia";
import { authApi } from "@/api";

export const useUserStore = defineStore("user", {
  state: () => ({
    // 初始用户信息
    userInfo: {
      name: "",
      id: "",
      avatar: "",
      email: "",
      phone: "",
      signature: "", // 个性签名
      joinTime: "", // 加入时间
      location: "", // 地区
    },
    isLogin: false,
  }),
  actions: {
    // 登录方法
    async login(loginData) {
      try {
        const response = await authApi.login({
          username: loginData.username,
          password: loginData.password,
        });

        if (response.code === 200) {
          localStorage.setItem("token", response.data.token);
          this.loginSuccess(response.data);
          return { success: true };
        } else {
          return { success: false, message: response.message };
        }
      } catch (error) {
        console.error("登录接口异常：", error);
        return { success: false, message: "登录失败，请稍后重试" };
      }
    },

    // 登录成功处理
    loginSuccess(userData) {
      this.userInfo = { ...this.userInfo, ...userData.userInfo };
      this.isLogin = true;
      // 持久化到 localStorage
      localStorage.setItem("userStore", JSON.stringify(this.$state));
    },

    //登出
    logout() {
      this.$reset(); // 重置为初始状态
      localStorage.removeItem("userStore");
      localStorage.removeItem("token");
    },

    // 更新用户信息
    async updateUserInfo(newInfo) {
      try {
        const response = await authApi.updateUserInfo(newInfo);
        if (response.code === 200) {
          this.userInfo = { ...this.userInfo, ...newInfo };
          // 同步更新本地缓存
          localStorage.setItem("userStore", JSON.stringify(this.$state));
          return { success: true };
        } else {
          return { success: false, message: response.message };
        }
      } catch (error) {
        console.error("更新用户信息异常：", error);
        return { success: false, message: "更新失败，请稍后重试" };
      }
    },

    // 修改密码
    async changePassword(oldPassword, newPassword) {
      try {
        const response = await authApi.changePassword({
          oldPassword,
          newPassword,
        });
        if (response.code === 200) {
          return { success: true };
        } else {
          return { success: false, message: response.message };
        }
      } catch (error) {
        console.error("修改密码异常：", error);
        return { success: false, message: "修改失败，请稍后重试" };
      }
    },

    // 从本地缓存加载用户信息（页面刷新后恢复状态）
    loadUserFromLocalStorage() {
      const cachedUser = localStorage.getItem("userStore");
      if (cachedUser) {
        this.$state = JSON.parse(cachedUser);
      }
    },

    // 获取当前用户信息
    async fetchUserInfo() {
      try {
        const response = await authApi.getCurrentUser();
        if (response.code === 200) {
          this.userInfo = response.data;
          this.isLogin = true;
          localStorage.setItem("userStore", JSON.stringify(this.$state));
          return { success: true };
        } else {
          return { success: false, message: response.message };
        }
      } catch (error) {
        console.error("获取用户信息异常：", error);
        return { success: false, message: "获取失败" };
      }
    },

    // 发送验证码
    async sendVerifyCode(params) {
      try {
        const response = await authApi.sendVerifyCode({
          account: params.account,
          type: params.type,
        });

        if (response.code === 200) {
          return { success: true };
        } else {
          return { success: false, message: response.message };
        }
      } catch (error) {
        console.error("发送验证码接口异常：", error);
        return { success: false, message: "验证码发送失败" };
      }
    },

    // 重置密码
    async resetPassword(params) {
      try {
        const response = await authApi.resetPassword({
          account: params.account,
          type: params.type,
          verifyCode: params.verifyCode,
          newPassword: params.newPassword,
        });

        if (response.code === 200) {
          return { success: true };
        } else {
          return { success: false, message: response.message };
        }
      } catch (error) {
        console.error("重置密码接口异常：", error);
        return { success: false, message: "密码重置失败" };
      }
    },
  },
});
