//import components from "@/components";//好像从未使用过，我注释掉了。
import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/",
    name: "Home",
    component: () => import("@/views/Main_interface.vue"),
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/Login.vue"),
  },
  {
    path: "/course",
    name: "Course",
    component: () => import("@/views/Course.vue"),
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
    path: "/profile",
    name: "Profile",
    component: () => import("@/views/Profile.vue"),
  },
  {
    path: "/settings",
    name: "Settings",
    component: () => import("@/views/Settings.vue"),
  },
  {
    path: "/question-bank",
    name: "QuestionBank",
    component: () => import("@/views/QuestionBank.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");
  // 如果去往登录页
  if (to.path === "/login") {
    if (token) {
      // 已登录则跳转首页
      next("/");
    } else {
      next();
    }
  } else {
    // 去往其他页面
    if (token) {
      next();
    } else {
      // 未登录跳转登录页
      next("/login");
    }
  }
});

export default router;
