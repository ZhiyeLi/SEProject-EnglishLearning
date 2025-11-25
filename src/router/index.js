//import components from "@/components";//好像从未使用过，我注释掉了。
import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/",
    name: "Home",
    component: () => import("@/views/Main_interface.vue"),
  },
  {
    path:"/login",
    name: "Login",
    component: () => import("@/views/Login.vue"),
  },
  {
    path: "/chat",
    name: "Chat",
    component: () => import("@/views/Chat.vue"),
  },
  {
    path: "/ai-chat",
    name: "AiChat",
    component: () => import("@/views/AiChat.vue"),
  },
  {
    path: "/word-type-selection",
    name: "WordTypeSelection",
    component: () => import("@/views/WordTypeSelection.vue"),
  },
  {
    path: "/word-check-in/:typeId?",
    name: "WordCheckIn",
    component: () => import("@/views/WordCheckIn.vue"),
  },
  {
    path: "/timetable",
    name: "TimeTable",
    component: () => import("@/views/TimeTable.vue"),
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import("@/views/Profile.vue"),
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import("@/views/Settings.vue"),
  }
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

// 路由守卫：未登录时，除了登录/注册页，其他页面跳登录
router.beforeEach((to, from, next) => {
  const userInfo = localStorage.getItem("userInfo");
  const isLogin = userInfo ? JSON.parse(userInfo).isLogin : false;
  
  // 允许访问的页面：登录页、注册页、已登录的所有页面
  if (to.name === "Login" || to.name === "Register" || isLogin) {
    next();
  } else {
    next("/login");
  }
});

export default router;
