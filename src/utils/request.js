/**
 * Axios 统一配置
 * 包含请求拦截器、响应拦截器、错误处理等
 */
import axios from "axios";
import { ElMessage } from "element-plus";

// 动态获取 API 基础 URL
function getBaseURL() {
  // 优先使用环境变量（仅在设置了非空值时使用）
  if (process.env.VUE_APP_API_BASE_URL && process.env.VUE_APP_API_BASE_URL.trim()) {
    console.log(`[API Debug] Using VUE_APP_API_BASE_URL from env: ${process.env.VUE_APP_API_BASE_URL}`);
    return process.env.VUE_APP_API_BASE_URL;
  }
  
  // 在浏览器环境中，使用当前访问的主机
  if (typeof window !== "undefined") {
    const protocol = window.location.protocol; // http: 或 https:
    const hostname = window.location.hostname; // 127.0.0.1, localhost, 192.168.x.x 等
    const port = 8080; // 后端服务端口
    const baseURL = `${protocol}//${hostname}:${port}`;
    console.log(`[API Debug] baseURL from window.location: ${baseURL}`);
    return baseURL;
  }
  
  // 默认值
  console.log(`[API Debug] Using default baseURL: http://localhost:8080`);
  return "http://localhost:8080";
}

// 创建 axios 实例
const apiClient = axios.create({
  baseURL: getBaseURL(),
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器 - 添加token
apiClient.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem("token");
    console.log(`[API Debug] ${config.method?.toUpperCase()} ${config.url} - baseURL: ${config.baseURL}`);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error("请求错误:", error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 统一处理响应
apiClient.interceptors.response.use(
  (response) => {
    const res = response.data;

    // 后端统一返回格式: { code, message, data }
    if (res.code === 200) {
      return res;
    } else {
      // 业务错误
      ElMessage.error(res.message || "请求失败");
      return Promise.reject(new Error(res.message || "请求失败"));
    }
  },
  (error) => {
    console.error("响应错误:", error);

    // HTTP 错误处理
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error("未授权,请重新登录");
          // 清除token并跳转到登录页
          localStorage.removeItem("token");
          localStorage.removeItem("userStore");
          window.location.href = "/login";
          break;
        case 403:
          ElMessage.error("登录已过期或拒绝访问，请重新登录");
          // 清除token并跳转到登录页
          localStorage.removeItem("token");
          localStorage.removeItem("userStore");
          window.location.href = "/login";
          break;
        case 404:
          ElMessage.error("请求的资源不存在");
          break;
        case 500:
          ElMessage.error("服务器错误");
          break;
        default:
          ElMessage.error(error.response.data?.message || "请求失败");
      }
    } else if (error.request) {
      ElMessage.error("网络错误,请检查网络连接");
    } else {
      ElMessage.error("请求配置错误");
    }

    return Promise.reject(error);
  }
);

export default apiClient;
