<template>
  <div class="card">
    <h3 class="schedule-title">
      <i class="fas fa-calendar-week"></i>计划时间表
    </h3>
    <p class="schedule-desc">{{ currentDate }} → 当前时间 → 对应课程计划表 → 未完成的事红色标记</p>
    
    <div class="schedule-grid">
      <div v-for="day in weekDays" :key="day" class="weekday">{{ day }}</div>
      
      <div 
        v-for="(day, index) in schedule" 
        :key="index"
        class="day-schedule"
        :class="{ today: day.isToday }"
      >
        <p v-for="task in day.tasks" :key="task.id">
          <i :class="task.icon" :style="{ color: task.color }"></i>
          {{ task.text }}
        </p>
      </div>
    </div>
    
    <button class="add-schedule" @click="addSchedule">
      <i class="fas fa-plus-circle"></i> 新增计划
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const currentDate = ref('2025年11月10日')
const weekDays = ['一', '二', '三', '四', '五', '六', '日']

const schedule = ref([
  {
    tasks: [
      { id: 1, text: '背30个单词', icon: 'fas fa-plus', color: 'var(--success)' },
      { id: 2, text: '看网课', icon: 'fas fa-book', color: 'var(--primary)' }
    ],
    isToday: false
  },
  {
    tasks: [],
    isToday: false
  },
  {
    tasks: [
      { id: 3, text: '做练习', icon: 'fas fa-pencil', color: 'var(--warning)' }
    ],
    isToday: true
  },
  {
    tasks: [
      { id: 4, text: '背30个单词', icon: 'fas fa-plus', color: 'var(--success)' }
    ],
    isToday: false
  },
  {
    tasks: [],
    isToday: false
  },
  {
    tasks: [
      { id: 5, text: '复习单词', icon: 'fas fa-sync', color: 'var(--warning)' }
    ],
    isToday: false
  },
  {
    tasks: [
      { id: 6, text: '复习单词', icon: 'fas fa-sync', color: 'var(--warning)' }
    ],
    isToday: false
  }
])

const addSchedule = () => {
  console.log('添加新计划')
}
</script>

<style scoped>
.schedule-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--gray-800);
  display: flex;
  align-items: center;
  gap: 8px;
}

.schedule-title i {
  color: var(--primary);
}

.schedule-desc {
  font-size: 14px;
  color: var(--gray-600);
  margin-bottom: 20px;
  padding-left: 28px;
  position: relative;
}

.schedule-desc::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 4px;
  height: 16px;
  background-color: var(--primary-light);
  border-radius: 2px;
}

.schedule-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  text-align: center;
}

.weekday {
  font-size: 13px;
  font-weight: 600;
  color: var(--gray-600);
  padding: 8px 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.day-schedule {
  background-color: var(--gray-50);
  border-radius: 8px;
  padding: 12px;
  text-align: left;
  font-size: 13px;
  min-height: 100px;
  transition: var(--transition);
  border: 1px solid transparent;
}

.day-schedule:hover {
  background-color: var(--white);
  border-color: var(--gray-200);
  transform: translateY(-2px);
}

.day-schedule.today {
  background-color: var(--primary-light);
  border-color: var(--primary);
}

.day-schedule p {
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  padding: 2px 0;
}

.day-schedule i {
  margin-right: 6px;
  font-size: 12px;
  width: 16px;
  text-align: center;
}

.add-schedule {
  margin-top: 20px;
  color: var(--primary);
  background: none;
  border: 1px dashed var(--primary);
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: var(--transition);
}

.add-schedule:hover {
  background-color: var(--primary-light);
  border-style: solid;
}

@media (max-width: 480px) {
  .schedule-grid {
    gap: 4px;
  }

  .day-schedule {
    padding: 8px;
    min-height: auto;
  }

  .day-schedule p {
    font-size: 12px;
    margin-bottom: 4px;
  }
}
</style>