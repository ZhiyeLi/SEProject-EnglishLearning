<template>
  <div class="card">
    <h3 class="articles-title">
      <i class="fas fa-newspaper"></i>精选学习文章
    </h3>
    
    <div class="articles-list">
      <div 
        v-for="article in articles" 
        :key="article.id" 
        class="article-item"
      >
        <div class="article-img">
          <i :class="article.icon"></i>
        </div>
        <div class="article-content">
          <h4>{{ article.title }}</h4>
          <p>{{ article.description }}</p>
          <div class="article-meta">
            <span><i class="far fa-clock"></i> {{ article.readTime }}</span>
            <span><i class="far fa-eye"></i> {{ article.views }}阅读</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const articles = ref([
  {
    id: 1,
    title: '如何有效提高英语听力水平',
    description: '本文介绍了几种实用的英语听力训练方法，帮助学习者快速提升听力理解能力，包括精听与泛听结合、影子跟读法等技巧...',
    icon: 'fas fa-headphones-alt fa-2x',
    readTime: '5分钟阅读',
    views: '1.2k'
  },
  {
    id: 2,
    title: '高考英语作文高分技巧',
    description: '掌握这些写作技巧，让你的英语作文在考试中脱颖而出，轻松获得高分。从结构布局到高级词汇运用，全面解析评分要点...',
    icon: 'fas fa-pen-fancy fa-2x',
    readTime: '8分钟阅读',
    views: '3.5k'
  },
  {
    id: 3,
    title: '30天掌握英语语法核心知识点',
    description: '系统化学习语法知识，30天计划帮助你构建完整的英语语法体系，从基础句型到复杂句式，循序渐进打好语法基础...',
    icon: 'fas fa-book-open fa-2x',
    readTime: '6分钟阅读',
    views: '2.1k'
  }
])
</script>

<style scoped>
.articles-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--gray-800);
  display: flex;
  align-items: center;
  gap: 8px;
}

.articles-title i {
  color: var(--info);
}

.articles-list {
  display: grid;
  gap: 20px;
}

.article-item {
  padding: 16px;
  border-radius: 10px;
  transition: var(--transition);
  border-left: 3px solid transparent;
  display: flex;
  gap: 16px;
}

.article-item:hover {
  background-color: var(--gray-50);
  border-left-color: var(--info);
  transform: translateX(4px);
}

.article-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  background-color: var(--gray-100);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gray-400);
}

.article-content {
  flex-grow: 1;
}

.article-item h4 {
  font-weight: 600;
  color: var(--gray-800);
  margin-bottom: 6px;
  transition: var(--transition);
}

.article-item:hover h4 {
  color: var(--info);
}

.article-item p {
  font-size: 14px;
  color: var(--gray-600);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  margin-top: 8px;
  font-size: 12px;
  color: var(--gray-500);
  display: flex;
  align-items: center;
  gap: 12px;
}

@media (max-width: 768px) {
  .articles-list {
    gap: 16px;
  }

  .article-item {
    flex-direction: column;
    gap: 12px;
  }

  .article-img {
    width: 100%;
    height: 140px;
  }
}
</style>