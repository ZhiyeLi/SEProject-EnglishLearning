# 单词打卡增强功能文档

## 📋 新增功能总览

实现了完整的打卡流程管理，包括：

1. ✅ **等级切换** - 随时切换单词等级，保留原有进度
2. ✅ **进度保存** - 使用 localStorage 持久化存储进度数据
3. ✅ **暂停/继续** - 支持暂停打卡任务并随后恢复
4. ✅ **复习功能** - 查看和复习已学单词
5. ✅ **进度显示** - 实时显示打卡进度和统计信息

---

## 🎯 功能详解

### 1. 等级切换功能

**触发方式：**
- 点击打卡页面头部的"🔄 切换等级"按钮
- 弹出等级选择模态框

**功能特点：**
- 显示所有 7 个等级及其当前进度
- 保留每个等级的独立打卡进度
- 切换等级时自动暂停当前等级，恢复目标等级

**数据流程：**
```
用户点击切换等级
  ↓
显示等级模态框
  ↓
用户选择新等级
  ↓
pauseCheckin(当前等级)  // 暂停当前
  ↓
resumeCheckin(目标等级)  // 恢复目标
  ↓
更新 UI 显示
```

**保存的数据：**
```javascript
progressData = {
  'cet4': {
    levelId: 'cet4',
    currentProgress: 25,        // 已打卡数
    isPaused: true,             // 是否暂停
    pausedAt: '2025-11-13T...',
    totalLearned: 25,           // 总学过数
    totalReviewed: 10,          // 总复习数
    lastCheckInTime: '2025-11-13T...'
  },
  'cet6': { ... }
}
```

---

### 2. 暂停/继续功能

**打卡状态显示：**

| 状态 | 图标 | 说明 |
|------|------|------|
| 未开始 | 📋 | "当前进度为 0，开始打卡" |
| 进行中 | ▶️ | "正在进行中..." |
| 已暂停 | ⏸️ | "已暂停·上次进度: X" |

**操作：**
- **开始打卡** - 当进度为 0 时显示
- **继续打卡** - 当暂停时显示（每次点击进度 +1）
- **暂停** - 当正在进行时显示

**代码实现：**
```javascript
// 暂停
handlePause() {
  checkinManager.pauseCheckin(currentLevelId.value)
  updateCurrentProgress()
}

// 继续
handleStartOrResume() {
  if (currentProgress.value.isPaused) {
    checkinManager.resumeCheckin(currentLevelId.value)
  }
  checkinManager.incrementProgress(currentLevelId.value)
  updateCurrentProgress()
}
```

---

### 3. 复习功能

**功能界面：**
- 点击"📚 复习已学单词"打开复习面板
- 显示学习统计：
  - 总共学过：XX 个
  - 已复习：XX 个
  - 需复习：XX 个

**后续开发：**
- 连接后端 API 获取需要复习的单词列表
- 实现复习流程（显示单词、用户选择等）
- 记录复习进度

---

### 4. 进度显示

**进度条组件：**
```
┌────────────────────────────────────────┐
│ 📖 英语四级词汇                         │
│      共 3500 词                         │
├────────────────────────────────────────┤
│ ████████░░░░░░░░░░░░░░░░░░░░░░░░░░    │
│ 875 / 3500                    25%      │
├────────────────────────────────────────┤
│ ⏸️  已暂停·上次进度: 875                │
└────────────────────────────────────────┘
```

**进度计算：**
```javascript
progressPercentage = (currentProgress / wordCount) * 100
```

---

## 💾 数据持久化

### localStorage 存储结构

**Key:** `wordCheckinProgress`

**Value 格式：**
```json
{
  "cet4": {
    "levelId": "cet4",
    "currentProgress": 25,
    "isPaused": false,
    "pausedAt": null,
    "totalLearned": 25,
    "totalReviewed": 5,
    "lastCheckInTime": "2025-11-13T12:34:56.789Z"
  },
  "cet6": { ... }
}
```

**单词学习状态存储：**
- **Key:** `word_learned_{levelId}_{wordId}`
- **Value:** `{ "learned": true, "date": "2025-11-13T..." }`

### 数据管理方法

```javascript
// 加载进度数据
loadProgressData()

// 获取指定等级的进度
getProgress(levelId)

// 更新进度
updateProgress(levelId, updates)

// 增加打卡进度
incrementProgress(levelId, amount = 1)

// 暂停
pauseCheckin(levelId)

// 继续
resumeCheckin(levelId)

// 切换等级
switchLevel(fromLevelId, toLevelId)

// 重置进度
resetProgress(levelId)

// 清除所有数据（测试用）
clearAllData()
```

---

## 🎨 UI 组件

### 主要组件树

```
WordCheckin.vue
├── 打卡头部 (.checkin-header)
│   ├── 返回按钮
│   ├── 标题
│   └── 切换等级按钮
│
├── 等级切换模态框 (.level-switch-modal)
│   └── 等级卡片网格
│
└── 打卡内容 (.checkin-content)
    ├── 进度区域 (.progress-section)
    │   ├── 当前等级徽章
    │   ├── 进度条
    │   └── 进度状态信息
    │
    ├── 操作按钮 (.action-buttons)
    │   ├── 开始/继续按钮
    │   ├── 暂停按钮（条件显示）
    │   └── 复习按钮
    │
    ├── 复习面板 (.review-panel) [可选显示]
    │   ├── 统计信息
    │   ├── 复习列表占位符
    │   └── 操作按钮
    │
    └── 占位符 (.placeholder)
        └── 内容区域预留
```

---

## 🔄 用户交互流程

### 场景 1：首次打卡

```
打开打卡页面
  ↓
显示"开始打卡"按钮
显示"当前进度为 0，开始打卡"
  ↓
用户点击"开始打卡"
  ↓
incrementProgress()
进度 → 1
  ↓
按钮变为"继续打卡"和"暂停"
状态变为"正在进行中..."
```

### 场景 2：暂停并切换等级

```
已打卡 50 个单词（进行中）
  ↓
用户点击"暂停"
  ↓
pauseCheckin()
状态 → 已暂停
  ↓
用户点击"切换等级"
  ↓
当前等级保持暂停状态（进度 50）
目标等级状态恢复
  ↓
继续打卡新等级
```

### 场景 3：复习功能

```
已学 100 个单词，复习 30 个
  ↓
用户点击"复习已学单词"
  ↓
显示面板
- 总共学过：100
- 已复习：30
- 需复习：70
  ↓
用户点击"开始复习"
  ↓
后续功能开发...
```

---

## 🔧 技术实现

### 核心类：CheckinProgressManager

```javascript
class CheckinProgressManager {
  constructor()
  loadProgressData()              // 加载数据
  initializeProgressData()        // 初始化
  saveProgressData()              // 保存数据
  getProgress(levelId)            // 获取进度
  updateProgress(levelId, updates)
  incrementProgress(levelId)      // 增加进度
  pauseCheckin(levelId)           // 暂停
  resumeCheckin(levelId)          // 继续
  switchLevel(from, to)           // 切换等级
  resetProgress(levelId)          // 重置
  clearAllData()                  // 清空
}
```

### 文件结构

```
src/
├── utils/
│   └── checkinManager.js        ✨ 新增 - 数据管理
├── views/
│   ├── WordCheckin.vue          🔄 修改 - 打卡页面
│   └── WordLevelSelector.vue    🔄 修改 - 使用共享数据
└── App.vue                      无变化
```

---

## 📊 状态转移图

```
              ┌─────────────┐
              │  未开始     │ (progress === 0)
              └──────┬──────┘
                     │ 点击开始
                     ↓
              ┌─────────────┐
           ┌──│ 进行中      │
           │  └─────────────┘
    暂停   │        │
           │        │ 点击暂停
           │        ↓
           │  ┌─────────────┐
           └─→│  已暂停     │
              └─────────────┘
                     │
                     │ 点击继续
                     └────→ (回到进行中)
```

---

## 🎯 后续开发计划

### Phase 2：完整打卡界面
- [ ] 显示单词详情（发音、例句等）
- [ ] 四选一选择题
- [ ] 实时反馈（对/错）
- [ ] 自动进度更新

### Phase 3：复习系统
- [ ] 获取需要复习的单词列表
- [ ] 复习统计和分析
- [ ] 遗忘曲线算法
- [ ] 智能复习推荐

### Phase 4：进阶功能
- [ ] 子分类功能
- [ ] 学习计划生成
- [ ] 成就系统
- [ ] 导出学习数据

---

## 🧪 测试场景

### 测试 1：基本打卡流程
```
1. 打开打卡页面
2. 验证显示"开始打卡"
3. 点击开始，验证进度 +1
4. 继续点击 5 次，验证进度为 5
5. 点击暂停，验证状态改变
6. 刷新页面，验证进度保持为 5
```

### 测试 2：等级切换
```
1. CET4 打卡到进度 10
2. 点击切换等级
3. 选择 CET6
4. 验证 CET6 进度为 0，CET4 暂停状态保持
5. CET6 打卡到进度 5
6. 再切换回 CET4
7. 验证 CET4 进度仍为 10
```

### 测试 3：复习面板
```
1. 打卡 30 个单词
2. 点击复习按钮
3. 验证面板显示
   - 总共学过：30
   - 已复习：0
   - 需复习：30
4. 关闭面板
```

---

## 💡 使用提示

1. **进度永久保存** - 所有进度自动保存到 localStorage
2. **多等级独立管理** - 每个等级有独立的进度和状态
3. **灵活暂停** - 可随时暂停并切换等级
4. **数据恢复** - 浏览器清空缓存后数据会重置

---

## 🐛 常见问题

**Q: 浏览器关闭后进度会丢失吗？**
A: 不会，所有数据保存在 localStorage 中，直到用户手动清除浏览器数据

**Q: 如何重置进度？**
A: 可在开发者工具中调用 `checkinManager.resetProgress('cet4')`

**Q: 如何导出数据？**
A: 可在开发者工具中执行 `copy(localStorage.wordCheckinProgress)`

**Q: 最多可以打卡多少个单词？**
A: 每个等级最多支持 6500 个单词（雅思词汇）
