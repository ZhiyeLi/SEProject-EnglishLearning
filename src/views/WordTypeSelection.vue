<template>
  <div class="min-h-screen bg-gradient-to-br from-emerald-50 via-teal-50 to-cyan-50 flex flex-col items-center justify-center p-4">
    <!-- 返回按钮 -->
    <div class="absolute top-6 left-6">
      <button 
        @click="goBack"
        class="flex items-center text-gray-600 hover:text-emerald-600 transition-colors p-2 rounded-lg hover:bg-white/50"
      >
        <i class="fas fa-arrow-left mr-2"></i>
        返回
      </button>
    </div>

    <!-- 头部标题 -->
    <div class="text-center mb-12 mt-8">
      <h1 class="text-4xl font-bold text-gray-800 mb-3">
        <i class="fas fa-book-open text-emerald-500 mr-2"></i>选择词汇类型
      </h1>
      <p class="text-lg text-gray-600">选择您想要背诵的单词类型</p>
    </div>

    <!-- 词汇类型选择卡片网格 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl">
      <div
        v-for="(type, key) in wordTypes"
        :key="key"
        @click="selectType(type)"
        class="group cursor-pointer"
      >
        <!-- 卡片容器 -->
        <div
          class="relative h-full bg-white rounded-2xl shadow-lg hover:shadow-2xl transform transition-all duration-300 hover:-translate-y-2 overflow-hidden border-2 border-transparent"
          :class="selectedType?.id === type.id ? `border-${type.color}-500` : 'hover:border-' + type.color + '-200'"
        >
          <!-- 背景装饰 -->
          <div
            class="absolute top-0 right-0 w-32 h-32 rounded-full opacity-20 -mr-16 -mt-16"
            :class="`bg-${type.color}-400`"
          ></div>

          <!-- 选中标记 -->
          <div
            v-if="selectedType?.id === type.id"
            class="absolute top-4 right-4 w-8 h-8 rounded-full bg-emerald-500 flex items-center justify-center text-white shadow-md"
          >
            <i class="fas fa-check"></i>
          </div>

          <!-- 卡片内容 -->
          <div class="relative p-8 h-full flex flex-col justify-between">
            <!-- 上半部分：图标和文字 -->
            <div>
              <!-- 图标 -->
              <div
                class="w-20 h-20 rounded-xl flex items-center justify-center mb-4 shadow-md group-hover:scale-110 transition-transform duration-300"
                :class="`bg-${type.color}-100`"
              >
                <i
                  class="text-4xl"
                  :class="[`fas ${type.icon}`, `text-${type.color}-600`]"
                ></i>
              </div>

              <!-- 标题和描述 -->
              <h2 class="text-2xl font-bold text-gray-800 mb-2">{{ type.name }}</h2>
              <p class="text-gray-600 text-base mb-4">{{ type.description }}</p>
            </div>

            <!-- 下半部分：统计信息 -->
            <div class="pt-4 border-t border-gray-100">
              <div class="flex items-center justify-between mb-3">
                <span class="text-gray-600 font-medium">总词汇数</span>
                <span class="text-2xl font-bold" :class="`text-${type.color}-600`">
                  {{ type.totalWords }}
                </span>
              </div>

              <!-- 进度条 -->
              <div class="mb-3">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-sm text-gray-600">学习进度</span>
                  <span class="text-sm font-semibold text-gray-700">
                    {{ getTypeProgress(type.id).passedCount }} / {{ type.totalWords }}
                  </span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                  <div
                    class="h-full rounded-full transition-all duration-500"
                    :class="`bg-${type.color}-500`"
                    :style="{
                      width: getProgressPercentage(type.id) + '%'
                    }"
                  ></div>
                </div>
              </div>

              <!-- 进度百分比 -->
              <p class="text-sm text-gray-600">
                已完成：<span class="font-semibold text-emerald-600">{{ getProgressPercentage(type.id) }}%</span>
              </p>
            </div>
          </div>

          <!-- 悬停效果：点击提示 -->
          <div
            class="absolute inset-0 bg-black/0 group-hover:bg-black/5 transition-colors duration-300 rounded-2xl flex items-center justify-center opacity-0 group-hover:opacity-100"
          >
            <span class="text-emerald-600 font-bold text-lg bg-white/90 px-4 py-2 rounded-lg">
              点击选择
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <div class="mt-12 flex gap-4">
      <button
        @click="goBack"
        class="px-8 py-3 rounded-lg border-2 border-gray-300 text-gray-700 font-medium hover:bg-gray-50 transition-all transform hover:-translate-y-0.5"
      >
        <i class="fas fa-times mr-2"></i>取消
      </button>
      <button
        @click="confirmSelection"
        :disabled="!selectedType"
        class="px-8 py-3 rounded-lg bg-emerald-500 text-white font-medium hover:bg-emerald-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-all transform hover:-translate-y-0.5 shadow-lg hover:shadow-xl"
      >
        <i class="fas fa-check-circle mr-2"></i>确认选择
      </button>
    </div>

    <!-- 提示信息 -->
    <div v-if="!selectedType" class="mt-8 text-center text-gray-600">
      <p class="text-base">
        <i class="fas fa-info-circle mr-2"></i>请选择一个词汇类型开始学习
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { WORD_TYPES, wordProgressManager } from '@/utils/wordData.js'

const router = useRouter()
const selectedType = ref(null)
const wordTypes = ref({})

onMounted(() => {
  // 初始化数据
  wordProgressManager.initProgress()
  wordTypes.value = WORD_TYPES
})

/**
 * 获取指定类型的学习进度
 */
function getTypeProgress(typeId) {
  const progress = wordProgressManager.getTypeProgress(typeId)
  return progress || { passedCount: 0 }
}

/**
 * 计算进度百分比
 */
function getProgressPercentage(typeId) {
  const progress = getTypeProgress(typeId)
  const type = WORD_TYPES[Object.keys(WORD_TYPES).find(k => WORD_TYPES[k].id === typeId)]
  if (!type) return 0
  const percentage = Math.round((progress.passedCount / type.totalWords) * 100)
  return Math.min(percentage, 100)
}

/**
 * 选择词汇类型
 */
function selectType(type) {
  selectedType.value = type
}

/**
 * 确认选择
 */
function confirmSelection() {
  if (!selectedType.value) return

  // 保存已选择的类型
  wordProgressManager.setSelectedType(selectedType.value.id)

  // 导航到单词打卡页面
  router.push({
    name: 'WordCheckIn',
    params: { typeId: selectedType.value.id }
  }).catch(() => {})
}

/**
 * 返回上一页
 */
function goBack() {
  router.back()
}
</script>

<style scoped>
/* 动态颜色类（Tailwind会动态生成） */
/* 这里确保颜色类能够被正确应用 */
</style>
