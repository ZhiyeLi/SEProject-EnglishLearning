<template>
  <div class="dictionary-card">
    <div class="search-bar">
      <input 
        type="text" 
        placeholder="输入单词或短语搜索" 
        v-model="searchQuery"
        @keyup.enter="searchDictionary"
      />
      <button @click="searchDictionary">
        <i class="fas fa-search"></i>
      </button>
    </div>

    <div v-if="wordDetails" class="word-details">
      <h3>
        {{ wordDetails.word }}
        <button class="play-audio" @click="playPronunciation">
          <i class="fas fa-volume-up"></i>
        </button>
        <span class="phonetic">/{{ wordDetails.phonetic }}/</span>
      </h3>

      <div class="definitions">
        <div v-for="(def, index) in wordDetails.definitions" :key="index" class="definition">
          <span class="part-of-speech">{{ def.partOfSpeech }}</span>
          <p>{{ def.meaning }}</p>
          <p v-if="def.example" class="example">
            <i class="fas fa-quote-left"></i>
            {{ def.example }}
          </p>
        </div>
      </div>
    </div>

    <div v-else-if="searching" class="loading">
      <i class="fas fa-spinner fa-spin"></i>
      正在查询...
    </div>

    <div v-else class="empty-state">
      <i class="fas fa-book"></i>
      <p>在此查询单词</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const searchQuery = ref('')
const searching = ref(false)
const wordDetails = ref(null)

const searchDictionary = async () => {
  if (!searchQuery.value) return
  
  searching.value = true
  wordDetails.value = null
  
  // 模拟API调用
  setTimeout(() => {
    wordDetails.value = {
      word: searchQuery.value,
      phonetic: 'həˈləʊ',
      definitions: [
        {
          partOfSpeech: 'n.',
          meaning: '用作问候语或打招呼',
          example: 'Hello, how are you today?'
        },
        {
          partOfSpeech: 'interj.',
          meaning: '表示惊讶或引起注意',
          example: "Hello! What's going on here?"
        }
      ]
    }
    searching.value = false
  }, 1000)
}

const playPronunciation = () => {
  console.log('播放发音')
}
</script>

<style scoped>
.dictionary-card {
  padding: 16px;
  background: var(--white);
  border-radius: 12px;
  border: 1px solid var(--gray-200);
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.search-bar input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid var(--gray-300);
  border-radius: 6px;
  font-size: 14px;
  transition: var(--transition);
}

.search-bar input:focus {
  border-color: var(--primary);
  outline: none;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.search-bar button {
  padding: 8px 16px;
  background-color: var(--primary);
  color: var(--white);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: var(--transition);
}

.search-bar button:hover {
  background-color: var(--primary-dark);
}

.word-details {
  padding-top: 12px;
}

.word-details h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  color: var(--gray-900);
  margin-bottom: 16px;
}

.play-audio {
  padding: 4px;
  background: none;
  border: none;
  color: var(--primary);
  cursor: pointer;
  transition: var(--transition);
}

.play-audio:hover {
  color: var(--primary-dark);
  transform: scale(1.1);
}

.phonetic {
  font-size: 14px;
  color: var(--gray-600);
  font-weight: normal;
}

.definitions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.definition {
  padding-left: 12px;
  border-left: 2px solid var(--primary-light);
}

.part-of-speech {
  font-size: 12px;
  font-weight: 600;
  color: var(--primary);
  text-transform: uppercase;
  margin-bottom: 4px;
  display: block;
}

.example {
  font-size: 13px;
  color: var(--gray-600);
  margin-top: 4px;
  font-style: italic;
}

.example i {
  font-size: 10px;
  margin-right: 4px;
}

.loading, .empty-state {
  padding: 32px 0;
  text-align: center;
  color: var(--gray-500);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.loading i, .empty-state i {
  font-size: 24px;
}
</style>