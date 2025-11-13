<template>
  <main>
    <div class="container">
      <div class="content-wrapper">
        <LeftSidebar />
        <div class="content-center">
          <CheckinCard />
          <ScheduleCard />
          <ArticleList />
        </div>
        <RightSidebar />
      </div>
    </div>
  </main>
</template>

<script setup>
import LeftSidebar from '../components/sidebar/LeftSidebar.vue'
import RightSidebar from '../components/sidebar/RightSidebar.vue'
import CheckinCard from '../components/main/CheckinCard.vue'
import ScheduleCard from '../components/main/ScheduleCard.vue'
import ArticleList from '../components/main/ArticleList.vue'
</script>

<style scoped>
.container {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.content-wrapper {
  display: flex;
  gap: 24px;
  padding: 32px 0;
}

/* 响应式布局 */
@media (max-width: 1024px) {
  .content-wrapper {
    flex-wrap: wrap;
  }
}
</style>
