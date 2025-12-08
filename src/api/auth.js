/**
 * 认证相关 API
 */
import request from "@/utils/request";

export const authApi = {
  // 用户注册
  register(data) {
    return request.post("/api/auth/register", data);
  },

  // 用户登录
  login(data) {
    return request.post("/api/auth/login", data);
  },

  // 发送验证码
  sendVerifyCode(data) {
    return request.post("/api/auth/send-verify-code", data);
  },

  // 重置密码
  resetPassword(data) {
    return request.post("/api/auth/reset-password", data);
  },

  // 获取当前用户信息
  getCurrentUser() {
    return request.get("/api/auth/user");
  },

  // 更新用户信息
  updateUserInfo(data) {
    return request.put("/api/auth/user", data);
  },

  // 修改密码
  changePassword(data) {
    return request.post("/api/auth/change-password", data);
  },
};
