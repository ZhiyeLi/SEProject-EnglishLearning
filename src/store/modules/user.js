import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 初始用户信息（实际项目中可从接口加载）
    userInfo: {
      name: '张三',
      id: 'user123456',
      avatar: 'https://picsum.photos/seed/zhang3/100/100',
      email: 'zhangsan666@qq.com',
      phone: '12345678910',
      signature: '正在学习英语四级词汇', // 个性签名
      joinTime: '2023年', // 加入时间
      location: '学习区' // 地区
    },
  }),
  actions: {
    login(userData){//登录
      this.userInfo = {...this.userInfo,...userData.userInfo};
      this.isLogin = true;

      // 持久化到 localStorage（避免页面刷新丢失）
      localStorage.setItem('userStore', JSON.stringify(this.$state));
    },
    logout(){//登出
      this.$reset();// Pinia 内置方法，重置为 state 初始值
      localStorage.removeItem('userStore'); // 清除本地缓存
    },
    // 更新用户信息的方法（供编辑表单调用）
    updateUserInfo(newInfo) {
      this.userInfo = { ...this.userInfo, ...newInfo };
      // 同步更新本地缓存
      localStorage.setItem('userStore', JSON.stringify(this.$state));
    },
    // 从本地缓存加载用户信息（页面刷新后恢复状态）
    loadUserFromLocalStorage() {
      const cachedUser = localStorage.getItem('userStore');
      if (cachedUser) {
        this.$state = JSON.parse(cachedUser);
      }
    },
  }
});