// src/utils/request.js
import axios from 'axios';

// 创建axios实例
const request = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || '/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    // 处理错误
    if (error.response && error.response.status === 401) {
      // 未授权，清除登录状态并跳转登录页
      localStorage.removeItem('token');
      localStorage.removeItem('userStore');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default request;