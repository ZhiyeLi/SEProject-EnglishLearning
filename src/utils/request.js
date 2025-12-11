// src/utils/request.js
import axios from 'axios';

const request = axios.create({
  baseURL: '/', // 改为相对路径，请求会自动通过代理转发
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  },
  // 关键：禁止跟随重定向（避免跳转到后端登录页）
  maxRedirects: 0,
  // 核心：禁止代理跟随 302 重定向（让前端处理）
  followRedirects: false
});

// 请求拦截器：添加token（登录后存储）
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    // 强制保证格式：Bearer + 空格 + Token
    config.headers['Authorization'] = `Bearer ${token.trim()}`;
  }
  return config;
});


// 响应拦截器：处理错误
// 响应拦截器：处理 302 重定向
request.interceptors.response.use(
  response => response.data,
  error => {
    console.error('请求错误：', error);
    // 捕获 302 状态码
    if (error.response?.status === 302) {
      ElMessage.error('登录状态失效，请重新登录');
      // 清空无效 Token 并跳转前端登录页
      localStorage.removeItem('token');
      router.push('/login').catch(() => {});
      return Promise.reject(new Error('登录状态失效'));
    }
    // 其他状态码处理
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('权限不足或登录过期');
      localStorage.removeItem('token');
      router.push('/login').catch(() => {});
    }
    return Promise.reject(error);
  }
);

export default request;