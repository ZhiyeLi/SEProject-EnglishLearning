import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/",
    name: "Home",
    component: () => import("@/views/Main_interface.vue"),
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
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
