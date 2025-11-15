import { createApp } from 'vue'
import App from './App.vue'

import './assets/style.css'  

import Components from '@/components'; // 导入组件库
import 'font-awesome/css/font-awesome.min.css'; 
import '@/components/styles/_variables.css'; // 导入组件库样式

const app = createApp(App);
app.use(Components); // 全局注册所有组件

createApp(App).mount('#app')
