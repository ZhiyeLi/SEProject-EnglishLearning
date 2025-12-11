import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus';
import { userApi } from '@/api'
import router from '@/router' // 引入路由实例

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: {
      name: '张三（未登录示例状态）',
      id: 'user',
      password: '123456', // 注意：实际项目中不应存储明文密码
      avatar: 'https://picsum.photos/seed/zhang3/100/100',
      email: 'zhangsan666@qq.com',
      phone: '12345678910',
      signature: '未登录',
      joinTime: '未登录',
      location: '未登录'
    },
    isLogin: false,
    redirectPath: '', // 新增：登录后跳转路径
    token: localStorage.getItem('token') || '' // 新增：单独存储token
  }),
  getters: {
    // 新增：获取用户ID（从userInfo或token解析）
    userId() {
      return this.userInfo.id || (this.token ? this.getUserIdFromToken() : null)
    }
  },
  actions: {
    // 新增：从token解析用户ID（简单实现，实际需配合JWT工具）
    getUserIdFromToken() {
      try {
        if (!this.token) return null
        const base64Url = this.token.split('.')[1]
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => 
          '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        ).join(''))
        return JSON.parse(jsonPayload).userId
      } catch (error) {
        console.error('解析token失败：', error)
        return null
      }
    },

    // 登录方法优化：登录成功后调用用户信息接口
    async login(loginData) {
      try {
        const response = await userApi.login(loginData);
        if (response.code === 200) {
          // 1. 先存储token（必须，因为获取用户信息接口需要token鉴权）
          this.token = response.data.token
          localStorage.setItem('token', this.token)
          
          // 2. 登录成功后，主动调用用户信息接口拉取最新数据
          const fetchSuccess = await this.fetchUserInfo(true); // 强制刷新用户信息
          if (fetchSuccess) {
            this.loginSuccess({ userInfo: this.userInfo }); // 用接口返回的用户信息更新
            // 3. 跳转指定路径（如有）
            if (this.redirectPath) {
              router.push(this.redirectPath).catch(() => {});
              this.redirectPath = ''; // 清空跳转路径
            } else {
              router.push('/').catch(() => {}); // 默认跳首页
            }
            return { success: true };
          } else {
            // 获取用户信息失败，清空token并提示
            this.token = '';
            localStorage.removeItem('token');
            return { success: false, message: '登录成功，但获取用户信息失败' };
          }
        } else {
          return { success: false, message: response.message };
        }
      } catch (error) {
        console.error('登录接口异常：', error);
        return { success: false, message: '登录失败，请稍后重试' };
      }
    },

    // 登录成功处理优化：仅做状态更新和提示
    loginSuccess(userData) {
      this.userInfo = { ...this.userInfo, ...userData.userInfo };
      this.isLogin = true;
      this.persistState(); // 统一持久化方法
      ElMessage.success('登录成功');
    },

    // 登出优化
    logout(redirect = '/login') {
      // 调用API登出（不阻塞后续操作）
      userApi.logout().catch(() => {});
      this.$reset();
      localStorage.removeItem('token');
      localStorage.removeItem('userStore');
      ElMessage.success('登出成功');
      // 登出后跳转
      router.push(redirect).catch(() => {})
    },

    // src/store/modules/user.js 中补充完整fetchUserInfo方法
    // src/store/modules/user.js
// src/store/modules/user.js
async fetchUserInfo(force = false) {
  if (!this.token) {
    console.error('无 Token，无法获取用户信息');
    return false;
  }
  if (this.isLogin && !force) return true;

  try {
    const response = await userApi.getUserInfo();
    if (response.code === 200 && response.data) {
      // 关键：将接口的 username 映射为前端的 name
      const userData = {
        // 前端字段名 ← 接口返回字段名
        name: response.data.username, // 核心映射
        id: response.data.id,
        avatar: response.data.avatar || 'https://picsum.photos/seed/zhang3/100/100',
        email: response.data.email,
        phone: response.data.phone,
        signature: response.data.signature,
        joinTime: response.data.joinTime,
        location: response.data.location
      };
      // 完全替换 userInfo，确保字段匹配
      this.userInfo = userData;
      this.isLogin = true;
      this.persistState();
      console.log('用户信息更新成功（映射后）：', this.userInfo); // 调试日志
      return true;
    } else {
      console.error('用户信息接口返回异常：', response);
      ElMessage.error('获取用户信息失败：' + (response.message || '接口返回异常'));
      return false;
    }
  } catch (error) {
    console.error('获取用户信息失败：', error);
    ElMessage.error('获取用户信息失败，请稍后重试');
    this.logout();
    return false;
  }
},

    // 更新用户信息优化
    async updateUserInfo(newInfo) {
      try {
        const response = await userApi.updateUserInfo(newInfo);
        if (response.code === 200) {
          this.userInfo = { ...this.userInfo, ...response.data };
          this.persistState();
          return { success: true };
        }
        return { success: false, message: response.message };
      } catch (error) {
        console.error('更新用户信息失败：', error);
        return { success: false, message: '更新失败，请稍后重试' };
      }
    },

    // 新增：设置登录后跳转路径
    setRedirectPath(path) {
      this.redirectPath = path;
    },

    // 新增：统一状态持久化方法
    persistState() {
      localStorage.setItem('userStore', JSON.stringify({
        userInfo: this.userInfo,
        isLogin: this.isLogin,
        redirectPath: this.redirectPath
      }));
    },

    // 从本地缓存加载用户信息优化
    loadUserFromLocalStorage() {
      const cachedUser = localStorage.getItem('userStore');
      if (cachedUser) {
        const { userInfo, isLogin, redirectPath } = JSON.parse(cachedUser);
        this.userInfo = userInfo;
        this.isLogin = isLogin;
        this.redirectPath = redirectPath;
      }
      // 从localStorage同步token
      this.token = localStorage.getItem('token') || '';
    },

    // 发送验证码（保持不变）
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
    
    // 重置密码优化
    async resetPassword(params) {
      try {
        const response = await userApi.resetPassword(params);
        if (response.code === 200) {
          if (this.isLogin && 
              (this.userInfo.phone === params.account || this.userInfo.email === params.account)) {
            // 这里应该调用更新密码接口，而不是直接修改
            // 实际项目中需要后端配合返回更新后的信息
            ElMessage.success('密码已重置，请重新登录');
            this.logout(); // 重置当前用户密码后登出
          }
          return { success: true };
        } else {
          return { success: false, message: response.message || '密码重置失败' };
        }
      } catch (error) {
        console.error('重置密码接口异常：', error);
        return { success: false, message: '密码重置失败' };
      }
    }
  }
});