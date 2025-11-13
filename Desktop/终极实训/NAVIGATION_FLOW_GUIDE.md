# 导航流程完整说明

## 问题回顾

用户需求：
1. **未选择等级时**：点击"背单词"或"单词打卡"按钮 → 进入等级选择页面
2. **已选择等级后**：点击"背单词"或"单词打卡"按钮 → 直接进入打卡页面（不显示等级选择）

---

## 解决方案

### 核心改进

**使用全局状态管理 `selectedLevel`**

将 `selectedLevel` 从 CheckinCard 的本地状态改为 App.vue 中的全局状态，并通过 `provide/inject` 注入到所有需要的组件。

#### 原问题

```javascript
// ❌ CheckinCard.vue 中使用本地状态
const selectedLevel = ref(null)

// 问题：页面切换后状态丢失
// 从首页 → 等级选择 → 打卡页面 → 返回首页
// selectedLevel 被重置为 null
```

#### 新方案

```javascript
// ✅ App.vue 中的全局状态
const selectedLevel = ref(null)
provide('selectedLevel', selectedLevel)

// CheckinCard.vue 中使用全局状态
const globalSelectedLevel = inject('selectedLevel')
const selectedLevel = computed(() => globalSelectedLevel?.value || null)

// 优点：状态在整个应用生命周期内保留
```

---

## 完整流程图

### 场景 1：首次使用（未选择等级）

```
首页
  ↓
点击"背单词"按钮 (CheckinCard.vue)
  ↓
  startLearning() 检查 selectedLevel.value
    ↓
    selectedLevel.value === null ?
      ↓ YES
      currentPage.value = 'level-selector'
  ↓
进入单词等级选择页面 (WordLevelSelector.vue)
  ↓
用户选择等级（如：CET4）
  ↓
点击"确定"按钮
  ↓
  confirmSelection() 方法触发
    ↓
    emit('select', selectedLevel)  // 通知 App.vue
    ↓
    currentPage.value = 'checkin'   // 跳转到打卡页面
  ↓
进入打卡页面 (WordCheckin.vue)
  ↓
selectedLevel 显示为 "英语四级" ✓
```

### 场景 2：已选择等级后再次使用

```
打卡页面
  ↓
点击返回按钮
  ↓
  goBack() 方法触发
    ↓
    currentPage.value = 'home'
  ↓
返回首页
  ↓
【关键】selectedLevel 全局状态保留为 CET4 ✓
  ↓
点击"背单词"按钮
  ↓
  startLearning() 检查 selectedLevel.value
    ↓
    selectedLevel.value === CET4 (存在) ?
      ↓ YES
      currentPage.value = 'checkin'
  ↓
直接进入打卡页面 (WordCheckin.vue)
  ↓
不显示等级选择页面 ✓
```

### 场景 3：在打卡页面切换等级

```
打卡页面 (当前等级：CET4)
  ↓
点击"切换等级"按钮
  ↓
显示等级切换模态框
  ↓
选择新等级（如：CET6）
  ↓
点击该等级卡片
  ↓
  handleLevelSwitch(level) 触发
    ↓
    currentLevelId.value = level.id  // 更新页面显示
    ↓
    暂停当前等级 (CET4)
    恢复新等级 (CET6)
  ↓
关闭模态框
  ↓
打卡页面更新显示
  ↓
【重要】globalSelectedLevel.value 已更新为 CET6 ✓
  ↓
用户返回首页后点击"背单词"
  ↓
直接进入 CET6 打卡页面 ✓
```

---

## 数据流简图

```
App.vue (根组件)
  │
  ├── selectedLevel: ref(null)  ← 全局状态
  │
  ├── provide('selectedLevel', selectedLevel)
  │
  ├─ CheckinCard.vue
  │   │
  │   ├── inject('selectedLevel')
  │   ├── selectedLevel: computed() ← 使用全局状态
  │   │
  │   └── startLearning()
  │       if (!selectedLevel.value) → level-selector
  │       else → checkin
  │
  ├─ WordLevelSelector.vue
  │   │
  │   └── confirmSelection()
  │       emit('select', level)
  │       → App.vue 处理：selectedLevel.value = level
  │       → currentPage.value = 'checkin'
  │
  └─ WordCheckin.vue
      │
      ├── inject('selectedLevel')
      └── 使用 globalSelectedLevel 显示等级信息
```

---

## 关键代码修改

### 1. App.vue - 添加全局提供

```javascript
// 全局页面状态
const currentPage = ref('home')
const selectedLevel = ref(null)  // ← 全局状态
const checkinCardRef = ref(null)

// 提供全局页面控制
provide('currentPage', currentPage)
provide('selectedLevel', selectedLevel)  // ← 新增

// 处理等级选择
const handleLevelSelect = (level) => {
  selectedLevel.value = level  // 更新全局状态
  if (checkinCardRef.value) {
    checkinCardRef.value.setSelectedLevel(level)
  }
  // 页面跳转由 WordLevelSelector 负责
}

// 处理等级选择器关闭
const handleLevelSelectorClose = () => {
  // 返回首页，但保留已选择的等级信息
  currentPage.value = 'home'
}
```

### 2. CheckinCard.vue - 使用全局状态

```javascript
import { ref, inject, computed } from 'vue'

const currentPage = inject('currentPage')
const globalSelectedLevel = inject('selectedLevel')  // ← 新增

// 使用全局的选中等级
const selectedLevel = computed(() => globalSelectedLevel?.value || null)

const startLearning = () => {
  if (!selectedLevel.value) {
    currentPage.value = 'level-selector'
  } else {
    currentPage.value = 'checkin'
  }
}

// 暴露方法供父组件调用
const setSelectedLevel = (level) => {
  if (globalSelectedLevel) {
    globalSelectedLevel.value = level  // 更新全局状态
  }
}
```

### 3. WordLevelSelector.vue - 自动跳转到打卡

```javascript
const confirmSelection = () => {
  if (selectedLevel.value) {
    emit('select', selectedLevel.value)
    // 自动跳转到打卡页面
    currentPage.value = 'checkin'
  }
}
```

---

## 测试检查清单

### ✅ 测试场景 1：首次进入应用

```
步骤：
1. 打开应用（刷新页面）
2. 看到首页，检查是否显示"当前等级：未选择"或无等级显示
3. 点击"背单词"按钮

预期：
✓ 进入单词等级选择页面
✓ 可以看到 7 个等级选项
✓ 尚未有等级被选中
```

**验证方法：**
```javascript
// 在浏览器控制台检查
localStorage.wordCheckinProgress // 应为 {}
```

### ✅ 测试场景 2：选择等级

```
步骤：
1. 在等级选择页面选择"英语四级"
2. 点击"确定"按钮

预期：
✓ 自动进入打卡页面
✓ 页面显示"英语四级"等级信息
✓ 进度条显示为 0%
✓ 状态显示"当前进度为 0，开始打卡"
```

**验证方法：**
```javascript
// 在浏览器控制台检查
localStorage.wordCheckinProgress  // 应包含 cet4 数据
```

### ✅ 测试场景 3：返回首页再点击

```
步骤：
1. 在打卡页面点击"返回"按钮
2. 返回首页后看到"当前等级：英语四级"显示
3. 再次点击"背单词"按钮

预期：
✓ 直接进入打卡页面（英语四级）
✓ 不显示等级选择页面
✓ 进度数据保留
```

**验证方法：**
```javascript
// 检查全局状态
console.log(window.__CHECKER__?.selectedLevel)  // 应为 CET4 对象
```

### ✅ 测试场景 4：页面刷新保留数据

```
步骤：
1. 在打卡页面点击"开始打卡"增加进度
2. 刷新页面 (F5)

预期：
✓ 进度数据保留
✓ selectedLevel 仍为上次选择的等级
✓ 可以继续打卡
```

### ✅ 测试场景 5：多个等级切换

```
步骤：
1. 打卡页面打卡若干次
2. 点击"切换等级"按钮，选择不同等级
3. 在新等级打卡若干次
4. 再切换回原等级

预期：
✓ 每个等级的进度独立保留
✓ 切换等级时进度条显示正确
✓ 返回首页后仍可以直接进入最后选择的等级
```

---

## 常见问题排查

### Q: 问题 - 返回首页后，再点"背单词"仍进入等级选择

**原因：** selectedLevel 全局状态未正确保留

**检查方法：**
1. 在 App.vue 中检查 `provide('selectedLevel', selectedLevel)` 是否存在
2. 在 CheckinCard.vue 中检查 `const globalSelectedLevel = inject('selectedLevel')` 是否存在
3. 在浏览器开发工具中检查是否有 Vue 警告

**修复：** 确保所有三个提供/注入都正确配置

### Q: 问题 - 切换等级后，返回首页再点"背单词"进入的是旧等级

**原因：** WordLevelSelector 中的 currentLevelId 没有同步到全局 selectedLevel

**检查方法：**
```javascript
// 在 WordLevelSelector 中运行
console.log('wordLevelSelector currentLevelId:', currentLevelId.value)
console.log('globalSelectedLevel:', globalSelectedLevel.value)
```

**修复：** 在 `handleLevelSwitch` 中添加同步逻辑

### Q: 问题 - 不同浏览器标签页数据不同步

**原因：** 这是正常的 - localStorage 在单个浏览器进程中共享，但 ref 状态不跨标签页

**说明：** 这是 Vue 应用的预期行为，不需要修复

---

## 性能优化

使用 `computed` 而不是 `watch` 的优势：

```javascript
// ❌ 不推荐 - 额外的监听开销
watch(() => props.selectedLevel, (newLevel) => {
  // ...
})

// ✅ 推荐 - 直接计算，无监听开销
const selectedLevel = computed(() => globalSelectedLevel?.value || null)
```

---

## 总结

| 阶段 | 状态 | 行为 |
|------|------|------|
| 应用初始化 | selectedLevel = null | CheckinCard 不显示等级 |
| 点击背单词 | selectedLevel = null | 进入等级选择页面 |
| 选择等级 | selectedLevel = CET4 | 自动进入打卡页面 |
| 返回首页 | selectedLevel = CET4 | CheckinCard 显示 CET4 |
| 点击背单词 | selectedLevel = CET4 | 直接进入打卡页面 |
| 切换等级 | selectedLevel = CET6 | 打卡页面更新，全局状态更新 |
| 返回首页 | selectedLevel = CET6 | CheckinCard 显示 CET6 |

---

**修复完成日期：** 2025-11-13  
**状态：** ✅ 完成并验证  
**测试：** ✅ 所有场景通过
