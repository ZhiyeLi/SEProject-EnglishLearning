<template>
  <div class="card">
    <div class="gradient-bg"></div>
    <div class="checkin-stats">
      <div class="checkin-info">
        <h2><i class="fas fa-trophy"></i>已连续打卡 {{ streak }} 天！！！</h2>
        <ul class="checkin-details">
          <li>
            <i class="fas fa-plus-circle new-word"></i>
            今日新学 {{ todayStats.newWords }} 个单词
          </li>
          <li>
            <i class="fas fa-sync review-word"></i>
            今日复习 {{ todayStats.reviewWords }} 个单词
          </li>
          <li>
            <i class="fas fa-calendar-alt tomorrow-review"></i>
            明日需复习 {{ tomorrowReview }} 个单词
          </li>
        </ul>
      </div>
      
      <div class="checkin-actions">
        <div class="alpaca-avatar">
          <div class="alpaca-circle">
            <i class="fas fa-paw"></i>
          </div>
          <div class="alpaca-rainbow"></div>
        </div>
        
        <button class="start-learning" @click="startLearning">
          <i class="fas fa-book-reader"></i> 背单词
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const streak = ref(69)
const todayStats = ref({
  newWords: 30,
  reviewWords: 50
})
const tomorrowReview = ref(50)

const startLearning = () => {
  console.log('开始背单词')
}
</script>

<style scoped>
.card {
  background-color: var(--white);
  border-radius: 12px;
  border: 1px solid var(--gray-200);
  box-shadow: var(--shadow-sm);
  padding: 24px;
  margin-bottom: 28px;
  transition: var(--transition);
  position: relative;
  overflow: hidden;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.gradient-bg {
  position: absolute;
  top: 0;
  right: 0;
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #93c5fd, #c084fc, #fda4af);
  border-radius: 50%;
  transform: translate(30%, -30%);
  opacity: 0.15;
  z-index: 1;
}

.checkin-stats {
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: relative;
  z-index: 2;
}

.checkin-info h2 {
  color: var(--primary-dark);
  font-size: 24px;
  margin-bottom: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.checkin-info h2 i {
  color: var(--warning);
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.checkin-details {
  list-style: none;
}

.checkin-details li {
  font-size: 15px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  padding: 6px 0;
}

.checkin-details i {
  margin-right: 10px;
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.new-word {
  color: var(--success);
}

.review-word {
  color: var(--warning);
}

.tomorrow-review {
  color: var(--primary);
}

.checkin-actions {
  display: flex;
  align-items: center;
  gap: 32px;
}

.alpaca-avatar {
  position: relative;
}

.alpaca-circle {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: linear-gradient(90deg, #fecaca, #fef3c7, #bfdbfe);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(254, 226, 226, 0.4);
  transition: var(--transition);
}

.alpaca-circle:hover {
  transform: rotate(5deg) scale(1.05);
}

.alpaca-circle i {
  color: var(--white);
  font-size: 52px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.alpaca-rainbow {
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 90px;
  height: 28px;
  background: linear-gradient(90deg, #f87171, #fbbf24, #a3e635, #60a5fa, #a78bfa);
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 2;
}

.start-learning {
  background-color: var(--primary);
  color: var(--white);
  border: none;
  border-radius: 8px;
  padding: 12px 32px;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: var(--transition);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
  display: flex;
  align-items: center;
  gap: 8px;
}

.start-learning:hover {
  background-color: var(--primary-dark);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
  transform: translateY(-2px);
}

.start-learning:active {
  transform: translateY(0);
}

@media (min-width: 768px) {
  .checkin-stats {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .checkin-actions {
    flex-direction: column;
    gap: 20px;
  }
  
  .checkin-info h2 {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .card {
    padding: 16px;
  }
}
</style>