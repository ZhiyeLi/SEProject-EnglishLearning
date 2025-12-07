import { defineStore } from 'pinia'
import axios from 'axios';
import { ElMessage } from 'element-plus'; // 引入消息提示组件
import { userApi } from '@/api'
export const useUserStore = defineStore('user', {
  state: () => ({
    // 初始用户信息（实际项目中可从接口加载）
    userInfo: {
      name: '张三（未登录示例状态）',
      id: 'user',
      password: '123456',
      avatar: 'https://picsum.photos/seed/zhang3/100/100',
      email: 'zhangsan666@qq.com',
      phone: '12345678910',
      signature: '未登录', // 个性签名
      joinTime: '未登录', // 加入时间
      location: '未登录' // 地区
    },
    isLogin: false,
  }),
  actions: {
     // 登录方法（包含后端交互）
    async login(loginData) {
      // 测试模式：不调用真实接口，直接返回成功
      const isTestMode = false; // 测试时设为true，实际开发设为false
      if (isTestMode) {
        // 模拟登录成功数据
        const mockUserData = {
          token: 'test_token_' + Date.now(),
          userInfo: {
            name: loginData.username,
            id: 'test_user_' + Math.random().toString(36).substr(2, 9),
            avatar: 'https://picsum.photos/seed/' + loginData.username + '/100/100',
            email: loginData.username + '@test.com',
            phone: '138' + Math.floor(Math.random() * 100000000),
            signature: '测试用户签名',
            joinTime: new Date().toLocaleDateString(),
            location: '测试城市'
          }
        };
        localStorage.setItem('token', mockUserData.token);
        this.loginSuccess(mockUserData);
        return { success: true };
      }

      // 真实接口调用逻辑（保持不变）
      try {
          const response = await userApi.login(loginData);

        if (response.code === 200) {
          localStorage.setItem('token', response.data.token);
          this.loginSuccess(response.data);
          return { success: true };
        } else {
          return { success: false, message: response.message };
        }
      } catch (error) {
        console.error('登录接口异常：', error);
        return { success: false, message: '登录失败，请稍后重试' };
      }
    },

    // 登录成功处理
    loginSuccess(userData) {
      this.userInfo = { ...this.userInfo, ...userData.userInfo };
      this.isLogin = true;
      // 持久化到 localStorage
      localStorage.setItem('userStore', JSON.stringify(this.$state));
      // 显示登录成功提示
      ElMessage.success('登录成功');
    },

    //登出
    logout() {
      // 调用API登出
      userApi.logout().catch(() => {});
      this.$reset(); // 重置为初始状态
      localStorage.removeItem('userStore');
      localStorage.removeItem('token');
      // 显示登出成功提示
      ElMessage.success('登出成功');
    },

    // 获取用户信息
    async fetchUserInfo() {
      try {
        const response = await userApi.getUserInfo();
        if (response.code === 200) {
          this.userInfo = response.data;
          this.isLogin = true;
          localStorage.setItem('userStore', JSON.stringify(this.$state));
          return true;
        }
      } catch (error) {
        console.error('获取用户信息失败：', error);
        this.logout();
      }
      return false;
    },

    // 更新用户信息
    async updateUserInfo(newInfo) {
      try {
        const response = await userApi.updateUserInfo(newInfo);
        if (response.code === 200) {
          this.userInfo = { ...this.userInfo, ...newInfo };
          localStorage.setItem('userStore', JSON.stringify(this.$state));
          return { success: true };
        }
        return { success: false, message: response.message };
      } catch (error) {
        console.error('更新用户信息失败：', error);
        return { success: false, message: '更新失败，请稍后重试' };
      }
    },

    // 更新密码
    async updatePassword(passwordData) {
      // 1. 前置校验：未登录直接返回失败
      if (!this.isLogin) {
        return { success: false, message: '请先登录后再修改密码' };
      }

      // 2. 解构参数：旧密码、新密码、确认新密码
      const { oldPassword, newPassword, confirmNewPassword } = passwordData;

      // 3. 前端校验（避免无效请求）
      if (!oldPassword || !newPassword || !confirmNewPassword) {
        return { success: false, message: '请完整填写所有密码项' };
      }
      if (newPassword !== confirmNewPassword) {
        return { success: false, message: '两次输入的新密码不一致' };
      }
      if (newPassword.length < 6) {
        return { success: false, message: '新密码长度不能少于6位' };
      }
      if (oldPassword === newPassword) {
        return { success: false, message: '新密码不能与旧密码相同' };
      }

      try {
        // 4. 调用后端接口修改密码
        const response = await userApi.updatePassword({
          oldPassword,
          newPassword
        });

        // 5. 接口响应处理
        if (response.code === 200) {
          // 可选：若后端返回更新后的用户信息，同步到本地
          if (response.data.userInfo) {
            this.userInfo = { ...this.userInfo, ...response.data.userInfo };
          }
          // 更新本地缓存
          localStorage.setItem('userStore', JSON.stringify(this.$state));
          return { success: true, message: '密码修改成功，请重新登录' };
        } else {
          return { success: false, message: response.message || '密码修改失败' };
        }
      } catch (error) {
        console.error('修改密码接口异常：', error);
        // 特殊处理：401/403 可能是旧密码错误，针对性提示
        if (error.response?.status === 401 || error.response?.status === 403) {
          return { success: false, message: '旧密码输入错误，请核对' };
        }
        return { success: false, message: '网络异常，密码修改失败，请稍后重试' };
      }
    },

    // 从本地缓存加载用户信息（页面刷新后恢复状态）
    loadUserFromLocalStorage() {
      const cachedUser = localStorage.getItem('userStore');
      if (cachedUser) {
        this.$state = JSON.parse(cachedUser);
      }
    },

    // 发送验证码
    async sendVerifyCode(params) {
      try {
        const response = await userApi.sendVerifyCode(params);
        return { 
          success: response.code === 200, 
          message: response.message 
        };
      } catch (error) {
        console.error('发送验证码接口异常：', error);
        return { success: false, message: '验证码发送失败' };
      }
    },
    
    // 重置密码
    async resetPassword(params) {
      try {
        const response = await userApi.resetPassword(params);

        if (response.code === 200) {
          // 如果是当前登录用户，同步更新本地密码
          if (this.isLogin && 
              (this.userInfo.phone === params.account || this.userInfo.email === params.account)) {
            this.updatePassword(params.newPassword);
          }
          return { success: true };
        } else {
          return { success: false, message: response.data.message };
        }
      } catch (error) {
        console.error('重置密码接口异常：', error);
        return { success: false, message: '密码重置失败' };
      }
    }
  }
});