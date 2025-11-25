//import components from "@/components";//好像从未使用过，我注释掉了。
import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/",
    name: "Home",
    component: () => import("@/views/Main_interface.vue"),
  },
  {
    path: "/course",
    name: "Course",
    component: () => import("@/views/Course.vue"),
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
  }
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

router.beforeEach((to) => {
  let isLogin = false;
  try {
    const userInfo = localStorage.getItem("userInfo");
    if (userInfo) {
      const parsedUser = JSON.parse(userInfo);
      isLogin = Boolean(parsedUser.isLogin);
    }
  } catch (error) {
    console.error("localStorage userInfo 格式错误：", error);
    isLogin = false;
  }

  const whiteList = ["Login", "NotFound"];
  if (!isLogin && !whiteList.includes(to.name)) {
    // 跳转登录页：返回路由对象
    return { name: "Login" };
  }

  if (isLogin && to.name === "Login") {
    // 已登录访问登录页：返回首页的路由对象
    return { name: "Home" };
  }

  // 其他情况：返回 undefined（放行导航，类型与路由对象一致）
  return undefined;
});
export default router;
