<template>
  <footer>
    <div class="container">
      <div class="footer-content">
        <div class="footer-links">
          <a 
            v-for="link in footerLinks" 
            :key="link.id" 
            :href="link.url"
          >
            {{ link.text }}
          </a>
        </div>
        <p class="footer-text">英语学习平台 &copy; 2025 版权所有 | 让学习更高效</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue'

const footerLinks = ref([
  { id: 1, text: '关于我们', url: '#' },
  { id: 2, text: '学习资源', url: '#' },
  { id: 3, text: '帮助中心', url: '#' },
  { id: 4, text: '联系客服', url: '#' },
  { id: 5, text: '隐私政策', url: '#' }
])
</script>

<style scoped>
footer {
  background-color: var(--white);
  border-top: 1px solid var(--gray-200);
  padding: 24px 0;
  margin-top: auto;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.footer-links {
  display: flex;
  gap: 24px;
}

.footer-links a {
  color: var(--gray-600);
  text-decoration: none;
  font-size: 14px;
  transition: var(--transition);
}

.footer-links a:hover {
  color: var(--primary);
}

.footer-text {
  text-align: center;
  font-size: 13px;
  color: var(--gray-500);
}

@media (max-width: 768px) {
  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
    gap: 16px;
  }
}
</style>