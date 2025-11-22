import { createApp } from 'vue'
import App from './App.vue'
import router from '@/router'
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

import './assets/style.css'

import Components from '@/components' // 导入组件库
import 'font-awesome/css/font-awesome.min.css'
import '@/components/styles/_variables.css' // 导入组件库样式
import { pinia } from './store'

const app = createApp(App)
// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}
app.use(ElementPlus)
app.use(pinia)
app.use(Components) // 全局注册所有组件
app.use(router)

app.mount('#app')
