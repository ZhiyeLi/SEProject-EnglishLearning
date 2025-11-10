<template>
  <header :class="{ scrolled: isScrolled }" id="mainHeader">
    <div class="container">
      <UrlBar />
      <div class="main-nav">
        <div class="nav-left">
          <button class="mobile-menu-btn" @click="toggleMobileMenu">
            <i :class="['fas', mobileMenuOpen ? 'fa-times' : 'fa-bars']"></i>
          </button>
          
          <div class="user-info">
            <div class="avatar">A</div>
            <div class="user-details">
              <div class="user-id">ID_English123</div>
              <div class="user-signature">坚持学习，每天进步一点点</div>
            </div>
          </div>
          
          <nav class="nav-links" :class="{ active: mobileMenuOpen }">
            <a 
              v-for="(link, index) in navLinks" 
              :key="index"
              :href="link.url"
              :class="{ active: link.isActive }"
            >
              {{ link.text }}
            </a>
          </nav>
        </div>
        
        <div class="right-actions">
          <button title="学习建议"><i class="fas fa-lightbulb"></i></button>
          <button title="设置"><i class="fas fa-cog"></i></button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import UrlBar from '../common/UrlBar.vue'

const isScrolled = ref(false)
const mobileMenuOpen = ref(false)

const navLinks = ref([
  { text: '首页', url: '#', isActive: true },
  { text: '课程', url: '#', isActive: false },
  { text: '题库', url: '#', isActive: false },
  { text: '时间表', url: '#', isActive: false },
  { text: '单词打卡', url: '#', isActive: false },
])

const handleScroll = () => {
  isScrolled.value = window.scrollY > 10
}

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

// 点击其他区域关闭菜单
const handleClickOutside = (event) => {
  const nav = document.querySelector('.nav-links')
  if (mobileMenuOpen.value && nav && !nav.contains(event.target) && !event.target.closest('.mobile-menu-btn')) {
    mobileMenuOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
header {
  background-color: var(--white);
  border-bottom: 1px solid var(--gray-200);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: var(--transition);
}

header.scrolled {
  box-shadow: var(--shadow);
}

.main-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
}

.nav-left {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  margin-right: 32px;
}

.avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--info));
  color: var(--white);
  font-weight: 600;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
  transition: var(--transition);
}

.avatar:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
}

.user-details {
  margin-left: 14px;
}

.user-id {
  background-color: var(--primary-light);
  color: var(--primary-dark);
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}

.user-signature {
  font-size: 13px;
  color: var(--gray-600);
  margin-top: 3px;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-links a {
  text-decoration: none;
  color: var(--gray-600);
  font-size: 15px;
  font-weight: 500;
  padding: 8px 0;
  transition: var(--transition);
  position: relative;
}

.nav-links a:hover {
  color: var(--primary);
}

.nav-links a.active {
  color: var(--primary);
}

.nav-links a.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: var(--primary);
  border-radius: 3px;
}

.right-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.right-actions button {
  background: none;
  border: none;
  color: var(--gray-600);
  cursor: pointer;
  font-size: 18px;
  transition: var(--transition);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.right-actions button:hover {
  color: var(--primary);
  background-color: var(--primary-light);
}

.mobile-menu-btn {
  display: none;
  background: none;
  border: none;
  color: var(--gray-700);
  font-size: 24px;
  cursor: pointer;
  margin-right: 12px;
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background-color: var(--white);
    box-shadow: var(--shadow-md);
    padding: 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    z-index: 100;
  }

  .nav-links.active {
    display: flex;
  }

  .nav-links a {
    width: 100%;
    padding: 10px 0;
  }

  .mobile-menu-btn {
    display: block;
  }
}
</style>