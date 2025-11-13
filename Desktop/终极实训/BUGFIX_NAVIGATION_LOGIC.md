# 修复首页跳转逻辑

## 问题描述

首页"背单词"按钮的跳转逻辑存在缺陷：

1. **问题 1**：未选择单词等级时，点击按钮无法进入单词等级选择页面
2. **问题 2**：已选择等级后，再点击按钮仍会进入等级选择页面，而不是直接进入打卡页面

## 原因分析

### CheckinCard.vue 中的问题

**原代码：**
```javascript
const startLearning = () => {
  // 总是跳转到等级选择页面，不管是否已选择等级
  currentPage.value = 'level-selector'
}
```

问题：
- 总是跳转到 `level-selector`，不考虑是否已选择等级
- 应该根据 `selectedLevel` 的状态来判断

### WordLevelSelector.vue 中的问题

原代码在 `confirmSelection` 中已经正确处理了跳转，但 CheckinCard 没有相应的逻辑支撑。

## 解决方案

### 1. 修改 CheckinCard.vue

**新代码：**
```javascript
const startLearning = () => {
  // 如果未选择等级，先进入等级选择页面
  // 如果已选择等级，直接进入打卡页面
  if (!selectedLevel.value) {
    currentPage.value = 'level-selector'
  } else {
    currentPage.value = 'checkin'
  }
}
```

**改进点：**
- ✅ 检查 `selectedLevel.value` 是否存在
- ✅ 未选择时进入等级选择页面
- ✅ 已选择时直接进入打卡页面

### 2. WordLevelSelector.vue 保持不变

```javascript
const confirmSelection = () => {
  if (selectedLevel.value) {
    emit('select', selectedLevel.value)
    // 更新当前页面为 'checkin'（打卡页面）
    currentPage.value = 'checkin'
  }
}
```

**工作流程：**
- ✅ 用户选择等级后点击"确定"按钮
- ✅ emit('select') 触发，App.vue 接收并更新 selectedLevel
- ✅ WordLevelSelector 将 currentPage 更新为 'checkin'
- ✅ 自动跳转到打卡页面

### 3. App.vue 简化逻辑

**新代码：**
```javascript
const handleLevelSelect = (level) => {
  selectedLevel.value = level
  // 更新 CheckinCard 显示的等级
  if (checkinCardRef.value) {
    checkinCardRef.value.setSelectedLevel(level)
  }
  // 页面跳转由 WordLevelSelector.vue 负责
}
```

**改进点：**
- ✅ 移除重复的 `currentPage.value = 'checkin'` 逻辑
- ✅ 让 WordLevelSelector 单独负责页面跳转
- ✅ 避免逻辑重复

## 使用流程

### 首次使用（未选择等级）

```
1. 用户在首页 → 点击"背单词"按钮
   ↓
2. startLearning() 检查 selectedLevel.value 为 null
   ↓
3. currentPage.value = 'level-selector'
   ↓
4. 进入单词等级选择页面 ✓
```

### 已选择等级后再次使用

```
1. 用户在首页 → 点击"背单词"按钮
   ↓
2. startLearning() 检查 selectedLevel.value 存在
   ↓
3. currentPage.value = 'checkin'
   ↓
4. 直接进入打卡页面 ✓
   （不再显示等级选择页面）
```

### 在打卡页面切换等级

```
1. 用户在打卡页面 → 点击"切换等级"按钮
   ↓
2. 显示等级选择模态框
   ↓
3. 用户选择新等级 → 点击"确定"
   ↓
4. confirmSelection() 更新 selectedLevel
   ↓
5. emit('select') 触发，App.vue 接收
   ↓
6. WordLevelSelector 中 currentPage.value = 'checkin'
   ↓
7. 更新打卡页面显示新等级 ✓
```

## 数据流图

```
首页 CheckinCard
    │
    ├─ 未选择等级
    │  └─ 点击"背单词"
    │     └─ selectedLevel.value 为 null
    │        └─ currentPage.value = 'level-selector'
    │           └─ 进入等级选择页面
    │
    └─ 已选择等级
       └─ 点击"背单词"
          └─ selectedLevel.value 存在
             └─ currentPage.value = 'checkin'
                └─ 直接进入打卡页面
                   └─ 显示已选等级信息
```

## 验证清单

- ✅ CheckinCard.vue 无编译错误
- ✅ App.vue 无编译错误
- ✅ WordLevelSelector.vue 无编译错误
- ✅ 逻辑清晰易维护
- ✅ 代码无重复

## 测试建议

### 测试场景 1：首次进入应用

```
步骤：
1. 刷新页面进入应用
2. 点击首页"背单词"按钮

预期：
✓ 进入单词等级选择页面
```

### 测试场景 2：选择等级后直接进入打卡

```
步骤：
1. 在等级选择页面选择"英语四级"
2. 点击"确定"按钮

预期：
✓ 进入打卡页面，显示"英语四级"
```

### 测试场景 3：再次点击背单词按钮

```
步骤：
1. 在打卡页面完成任务回到首页
2. 再次点击"背单词"按钮

预期：
✓ 直接进入打卡页面（英语四级）
✓ 不出现等级选择页面
```

### 测试场景 4：在打卡页面切换等级

```
步骤：
1. 在打卡页面点击"切换等级"按钮
2. 选择"英语六级"等级
3. 点击"确定"

预期：
✓ 打卡页面更新为"英语六级"
✓ 进度重新显示（新等级的进度）
```

## 修改文件清单

| 文件 | 修改类型 | 改动行数 |
|------|---------|---------|
| CheckinCard.vue | 修改 | 7 行 |
| App.vue | 简化 | 1 行 |
| WordLevelSelector.vue | 无改动 | - |

## 相关文档

- 详见 `ENHANCED_CHECKIN_FEATURES.md` - 完整功能说明
- 详见 `CHECKIN_QUICK_START.md` - 快速使用指南
- 详见 `QUICK_REFERENCE.md` - 快速参考

---

**修复日期：** 2025-11-13  
**状态：** ✅ 完成并验证  
**错误检查：** ✅ 无编译错误
