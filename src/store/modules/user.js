import { defineStore } from 'pinia'
import axios from 'axios';

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
      const isTestMode = true; // 测试时设为true，实际开发设为false
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
          const response = await axios.post('/api/auth/login', {
          username: loginData.username,
          password: loginData.password
        });

        if (response.data.code === 200) {
          localStorage.setItem('token', response.data.data.token);
          this.loginSuccess(response.data.data);
          return { success: true };
        } else {
          return { success: false, message: response.data.message };
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
    },

    //登出
    logout() {
      this.$reset(); // 重置为初始状态
      localStorage.removeItem('userStore');
      localStorage.removeItem('token');
    },

    // 更新用户信息
    updateUserInfo(newInfo) {
      this.userInfo = { ...this.userInfo, ...newInfo };
      // 同步更新本地缓存
      localStorage.setItem('userStore', JSON.stringify(this.$state));
    },

    // 更新密码
    updatePassword(newPassword) {
      // 实际项目中应加密后再存储
      this.userInfo.password = newPassword;
      // 可同时更新本地存储（如localStorage）
      localStorage.setItem('userStore', JSON.stringify(this.$state));
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
        const response = await axios.post('/api/auth/send-verify-code', {
          account: params.account,
          type: params.type
        });

        if (response.data.code === 200) {
          return { success: true };
        } else {
          return { success: false, message: response.data.message };
        }
      } catch (error) {
        console.error('发送验证码接口异常：', error);
        return { success: false, message: '验证码发送失败' };
      }
    },
    
    // 重置密码
    async resetPassword(params) {
      try {
        const response = await axios.post('/api/auth/reset-password', {
          account: params.account,
          type: params.type,
          verifyCode: params.verifyCode,
          newPassword: params.newPassword
        });

        if (response.data.code === 200) {
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