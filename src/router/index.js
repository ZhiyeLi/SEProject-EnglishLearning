import{createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        // avoid importing App.vue as a route component (can cause recursion)
        { path: '/', name: 'home', component: () => import('../views/Home.vue') },
        { path: '/chat', name: 'chat', component: () => import('../views/ChatPanel.vue') },
        { path: '/ai', name: 'ai', component: () => import('../views/AiChat.vue') },
    ]
})

export default router