# 英语学习平台 - 项目集成指南

本文档指导如何在 Vue 3 前端中集成后端 REST API。

## 📚 项目结构

```
english-learning-platform/
├── src/
│   ├── App.vue                    # 主应用
│   ├── main.js                    # 入口
│   ├── style.css                  # 全局样式
│   ├── components/                # 可复用组件
│   │   ├── common/
│   │   │   └── UrlBar.vue
│   │   ├── layout/
│   │   │   ├── Header.vue
│   │   │   └── Footer.vue
│   │   ├── main/
│   │   │   ├── ArticleList.vue
│   │   │   ├── CheckinCard.vue
│   │   │   └── ScheduleCard.vue
│   │   ├── rightSidebar/
│   │   │   ├── AiAssistant.vue
│   │   │   └── DictionaryCard.vue
│   │   └── sidebar/
│   │       ├── LeftSidebar.vue
│   │       └── RightSidebar.vue
│   └── views/
│       └── AiChat.vue
├── server/                        # Node.js + SQLite 后端
│   ├── index.js                   # Express 服务器
│   ├── init_db.js                 # 数据库初始化
│   ├── data/
│   │   └── elw.sqlite             # SQLite 数据库文件
│   ├── package.json
│   └── README.md
├── ELW_Database.sql               # 原始 MySQL 数据库定义
├── DB_ANALYSIS_AND_RECOMMENDATIONS.md  # 数据库分析报告
├── INTEGRATION_GUIDE.md            # 本文件
├── package.json
├── vite.config.js
└── index.html
```

---

## 🔌 API 集成示例

### 1. 创建 API 客户端工具类

在 `src/api/client.js` 中创建 HTTP 客户端：

```javascript
// src/api/client.js
const API_BASE_URL = 'http://localhost:3000/api';

export const apiClient = {
  async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const response = await fetch(url, {
      method: options.method || 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      body: options.body ? JSON.stringify(options.body) : undefined,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error || `HTTP ${response.status}`);
    }

    return await response.json();
  },

  // 单词相关
  getWords(limit = 100, offset = 0) {
    return this.request(`/words?limit=${limit}&offset=${offset}`);
  },

  getWordDetail(wordId) {
    return this.request(`/words/${wordId}`);
  },

  // 用户相关
  getUser(userId) {
    return this.request(`/users/${userId}`);
  },

  createUser(userData) {
    return this.request('/users', {
      method: 'POST',
      body: userData,
    });
  },

  getUserGroups(userId) {
    return this.request(`/users/${userId}/groups`);
  },

  getUserWordMemory(userId) {
    return this.request(`/users/${userId}/words/memory`);
  },

  updateWordMemory(userId, wordId, isRemembered) {
    return this.request(`/users/${userId}/words/${wordId}/remember`, {
      method: 'POST',
      body: { is_remembered: isRemembered },
    });
  },

  // 系统检查
  healthCheck() {
    return this.request('/health');
  },
};
```

### 2. 在 Vue 组件中使用 API

#### 示例：单词列表组件

```vue
<!-- src/components/main/WordList.vue -->
<template>
  <div class="word-list">
    <h2>单词库</h2>
    
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    
    <ul v-else class="words">
      <li v-for="word in words" :key="word.word_id" class="word-item">
        <span class="word-text">{{ word.word_content }}</span>
        <button @click="viewDetail(word.word_id)">查看详情</button>
      </li>
    </ul>

    <div class="pagination">
      <button @click="previousPage" :disabled="offset === 0">上一页</button>
      <span>第 {{ page }} 页</span>
      <button @click="nextPage">下一页</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { apiClient } from '@/api/client';

export default {
  name: 'WordList',
  setup() {
    const words = ref([]);
    const loading = ref(false);
    const error = ref(null);
    const limit = 20;
    const offset = ref(0);

    const fetchWords = async () => {
      loading.value = true;
      error.value = null;
      try {
        const data = await apiClient.getWords(limit, offset.value);
        words.value = data.data;
      } catch (err) {
        error.value = err.message;
      } finally {
        loading.value = false;
      }
    };

    const viewDetail = (wordId) => {
      // 导航到词详情页或打开弹窗
      console.log('View word detail:', wordId);
    };

    const nextPage = () => {
      if (words.value.length === limit) {
        offset.value += limit;
        fetchWords();
      }
    };

    const previousPage = () => {
      if (offset.value > 0) {
        offset.value -= limit;
        fetchWords();
      }
    };

    onMounted(fetchWords);

    return {
      words,
      loading,
      error,
      page: () => Math.floor(offset.value / limit) + 1,
      nextPage,
      previousPage,
      viewDetail,
    };
  },
};
</script>

<style scoped>
.word-list {
  padding: 20px;
}

.words {
  list-style: none;
  padding: 0;
}

.word-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.word-text {
  font-weight: bold;
  min-width: 150px;
}

button {
  padding: 5px 10px;
  cursor: pointer;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.loading, .error {
  padding: 20px;
  text-align: center;
}

.error {
  color: red;
}
</style>
```

#### 示例：用户信息组件

```vue
<!-- src/components/UserProfile.vue -->
<template>
  <div class="user-profile">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    
    <div v-else class="profile-card">
      <img v-if="user.avatar" :src="user.avatar" :alt="user.nickname" class="avatar">
      <div class="info">
        <h2>{{ user.nickname }}</h2>
        <p><strong>账号：</strong> {{ user.username }}</p>
        <p><strong>邮箱：</strong> {{ user.email }}</p>
        <p><strong>学习进度：</strong> {{ user.progress.completed }} / {{ user.progress.total }}</p>
        <div class="progress-bar">
          <div class="progress" :style="{ width: progressPercent + '%' }"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { apiClient } from '@/api/client';

export default {
  name: 'UserProfile',
  props: {
    userId: {
      type: Number,
      required: true,
    },
  },
  setup(props) {
    const user = ref(null);
    const loading = ref(false);
    const error = ref(null);

    const fetchUser = async () => {
      loading.value = true;
      error.value = null;
      try {
        user.value = await apiClient.getUser(props.userId);
      } catch (err) {
        error.value = err.message;
      } finally {
        loading.value = false;
      }
    };

    const progressPercent = computed(() => {
      if (!user.value?.progress) return 0;
      const { total, completed } = user.value.progress;
      return total > 0 ? Math.round((completed / total) * 100) : 0;
    });

    onMounted(fetchUser);

    return {
      user,
      loading,
      error,
      progressPercent,
    };
  },
};
</script>

<style scoped>
.user-profile {
  padding: 20px;
}

.profile-card {
  display: flex;
  gap: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.info {
  flex: 1;
}

.progress-bar {
  width: 100%;
  height: 20px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
  margin-top: 10px;
}

.progress {
  height: 100%;
  background: #4CAF50;
  transition: width 0.3s;
}

.loading, .error {
  text-align: center;
  padding: 20px;
}

.error {
  color: red;
}
</style>
```

#### 示例：单词记忆卡组件

```vue
<!-- src/components/WordMemoryCard.vue -->
<template>
  <div class="memory-card">
    <h3>我的单词记忆</h3>
    
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    
    <div v-else class="card-list">
      <div
        v-for="item in memorizedWords"
        :key="item.id"
        class="card-item"
        :class="{ remembered: item.is_remembered }"
      >
        <span class="word">{{ item.word_content }}</span>
        <button @click="toggleMemory(item)">
          {{ item.is_remembered ? '✓ 已记住' : '未记住' }}
        </button>
      </div>
    </div>

    <div class="stats">
      <p>已记住：{{ rememberCount }} / {{ memorizedWords.length }}</p>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { apiClient } from '@/api/client';

export default {
  name: 'WordMemoryCard',
  props: {
    userId: {
      type: Number,
      required: true,
    },
  },
  setup(props) {
    const memorizedWords = ref([]);
    const loading = ref(false);
    const error = ref(null);

    const fetchMemory = async () => {
      loading.value = true;
      error.value = null;
      try {
        const data = await apiClient.getUserWordMemory(props.userId);
        memorizedWords.value = data.data;
      } catch (err) {
        error.value = err.message;
      } finally {
        loading.value = false;
      }
    };

    const toggleMemory = async (item) => {
      try {
        const newStatus = !item.is_remembered;
        await apiClient.updateWordMemory(props.userId, item.word_id, newStatus);
        item.is_remembered = newStatus ? 1 : 0;
      } catch (err) {
        error.value = err.message;
      }
    };

    const rememberCount = computed(() => {
      return memorizedWords.value.filter(w => w.is_remembered === 1).length;
    });

    onMounted(fetchMemory);

    return {
      memorizedWords,
      loading,
      error,
      rememberCount,
      toggleMemory,
    };
  },
};
</script>

<style scoped>
.memory-card {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.card-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 15px 0;
}

.card-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #f9f9f9;
}

.card-item.remembered {
  background: #e8f5e9;
  border-color: #4CAF50;
}

.word {
  font-weight: bold;
  min-width: 100px;
}

button {
  padding: 5px 10px;
  cursor: pointer;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

button:hover {
  background: #f0f0f0;
}

.stats {
  margin-top: 15px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
  text-align: center;
}

.loading, .error {
  text-align: center;
  padding: 10px;
}

.error {
  color: red;
}
</style>
```

### 3. 环境配置

创建 `src/config/api.js` 来管理 API 配置：

```javascript
// src/config/api.js
const isDev = import.meta.env.DEV;

export const API_CONFIG = {
  development: {
    baseUrl: 'http://localhost:3000/api',
    timeout: 10000,
  },
  production: {
    baseUrl: 'https://api.your-domain.com/api',
    timeout: 10000,
  },
};

export const currentConfig = isDev ? API_CONFIG.development : API_CONFIG.production;
```

### 4. 在 main.js 中全局注册 API

```javascript
// src/main.js
import { createApp } from 'vue'
import App from './App.vue'
import { apiClient } from './api/client'

const app = createApp(App)

// 全局注册 API 客户端
app.config.globalProperties.$api = apiClient

app.mount('#app')
```

---

## 🚀 启动步骤（完整流程）

### 第一次运行

```bash
# 1. 安装前端依赖
npm install

# 2. 安装后端依赖
cd server
npm install

# 3. 初始化数据库
npm run init-db

# 4. 启动后端（在一个终端中）
npm start
# 输出: ✓ Server listening on http://localhost:3000

# 5. 在另一个终端启动前端
cd ..
npm run dev
# 输出: ✓ VITE v7.2.2  ready in XXX ms
#       ➜  Local:   http://localhost:5173/
```

### 后续运行

```bash
# 后端（第一个终端）
cd server
npm start

# 前端（第二个终端）
npm run dev
```

---

## 🔧 开发时常用命令

```bash
# 构建前端
npm run build

# 预览生产构建
npm run preview

# 停止服务器
# 按 Ctrl+C 即可

# 重置数据库
cd server
npm run init-db
npm start
```

---

## 📊 数据库状态检查

如果遇到 DB 问题，运行初始化脚本：

```bash
cd server
npm run init-db
npm start
```

数据库位置：`server/data/elw.sqlite`

---

## ⚠️ 常见问题

### Q: 前端无法连接到后端？
**A:** 
1. 确保后端在 `http://localhost:3000` 运行
2. 检查防火墙是否阻止了 3000 端口
3. 浏览器控制台（F12）查看具体错误

### Q: 数据库文件不存在？
**A:** 运行 `cd server && npm run init-db`

### Q: 如何修改后端端口？
**A:** `PORT=3001 npm start`

### Q: 前端和后端在不同端口，如何调整 API 地址？
**A:** 修改 `src/config/api.js` 中的 `baseUrl`

---

## 🎯 下一步改进建议

1. **完整的登录/认证系统**
   - 实现 JWT token 认证
   - 在后端添加登录端点

2. **实时聊天功能**
   - 使用 Socket.IO 实现群组消息实时推送
   - 前端监听消息事件

3. **数据持久化**
   - 使用 localStorage 缓存用户登录状态
   - 记录用户浏览历史

4. **错误处理与日志**
   - 统一的错误处理机制
   - 前端请求日志记录

5. **性能优化**
   - API 响应缓存
   - 分页加载优化
   - 图片懒加载

6. **单元测试**
   - 后端 API 单元测试
   - 前端组件测试

---

**更新时间：** 2025-11-12  
**状态：** ✅ 后端运行中，API 可用  
**前端状态：** 待集成示例组件到实际项目
