import { createApp } from 'vue'
import App from './App.vue'
import './style.css'

// 添加Font Awesome
const link = document.createElement('link')
link.rel = 'stylesheet'
link.href = 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css'
document.head.appendChild(link)

// 添加Inter字体
const fontLink = document.createElement('link')
fontLink.rel = 'stylesheet'
fontLink.href = 'https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap'
document.head.appendChild(fontLink)

createApp(App).mount('#app')
