# 单词打卡流程设计文档

## 📋 功能概述

实现了一个完整的单词学习打卡系统，包括：
1. **等级选择页面** - 用户选择要学习的单词等级
2. **打卡页面** - 正式的单词打卡学习界面
3. **首页集成** - CheckinCard 显示已选等级和统计信息

---

## 🎯 用户流程

```
主页 → 点击"背单词"按钮 → 等级选择页面 → 选择等级 → 打卡页面 → 返回主页
```

---

## 📂 文件结构

### 新增/修改的文件

```
src/
├── views/
│   ├── WordLevelSelector.vue    ✨ 新增 - 单词等级选择组件
│   └── WordCheckin.vue          ✨ 新增 - 单词打卡页面组件
├── components/
│   └── main/
│       └── CheckinCard.vue      🔄 修改 - 新增等级显示和触发功能
└── App.vue                      🔄 修改 - 添加路由逻辑
```

---

## 🧩 组件详解

### 1. WordLevelSelector.vue （单词等级选择）

**功能：**
- 展示 7 种单词等级卡片
- 用户点击卡片选择等级
- 确认选择后跳转到打卡页面

**等级类型：**
| ID | 名称 | 描述 | 词汇数 |
|-----|------|------|--------|
| pre_university | 大学前词汇 | 初中高中常用词汇 | 2500 |
| cet4 | 英语四级词汇 | CET-4 考试范围 | 3500 |
| cet6 | 英语六级词汇 | CET-6 考试范围 | 5000 |
| graduate | 考研词汇 | 考研英语重点词汇 | 5500 |
| toefl | 托福词汇 | TOEFL 考试范围 | 6000 |
| ielts | 雅思词汇 | IELTS 考试范围 | 6500 |
| professional | 专业性词汇 | 行业领域专业用语 | 3000 |

**关键事件：**
```javascript
// 选择等级后触发
emit('select', selectedLevel)

// 关闭选择器
emit('close')
```

**样式特点：**
- 全屏模态框设计
- 半透明背景遮罩 + 毛玻璃效果
- 响应式网格布局（3列→1列）
- 鼠标悬停和选中状态动画

---

### 2. WordCheckin.vue （单词打卡页面）

**功能：**
- 显示已选等级信息（名称和图标）
- 提供返回主页按钮
- 内容区域预留给后续功能开发

**特点：**
- 蓝色渐变头部
- 清晰的等级徽章显示
- 返回按钮支持返回主页

**Props：**
```javascript
{
  selectedLevel: {
    id: 'cet4',
    name: '英语四级词汇',
    icon: '📖',
    wordCount: 3500
  }
}
```

---

### 3. CheckinCard.vue （首页打卡卡片）

**修改内容：**
- ✅ 新增"当前等级"显示行
- ✅ 等级显示带有背景高亮和左边框
- ✅ 点击"背单词"按钮触发等级选择

**新增状态：**
```javascript
selectedLevel: null  // 存储用户选择的等级
```

**新增方法：**
```javascript
setSelectedLevel(level)  // 由父组件调用以更新等级显示
```

**UI 样式：**
```
┌─────────────────────────────────────┐
│ 已连续打卡 69 天！！！               │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│ 📕 当前等级：英语四级词汇           │
│ ✚ 今日新学 30 个单词               │
│ ↻ 今日复习 50 个单词               │
│ 📅 明日需复习 50 个单词             │
│                                     │
│        [打卡头像]     [背单词按钮]  │
└─────────────────────────────────────┘
```

---

### 4. App.vue （应用主容器）

**修改内容：**
- ✅ 新增 `selectedLevel` 全局状态
- ✅ 新增 `handleLevelSelect()` 方法处理等级选择
- ✅ 新增 `handleLevelSelectorClose()` 方法处理选择器关闭
- ✅ 添加路由逻辑：`home` → `level-selector` → `checkin`

**状态管理：**
```javascript
currentPage: 'home'              // 当前页面
selectedLevel: null              // 选中的等级
checkinCardRef: null             // CheckinCard 组件引用
```

**页面状态：**
- `'home'` - 主页（默认）
- `'ai'` - AI 聊天页面
- `'level-selector'` - 等级选择页面
- `'checkin'` - 打卡页面

---

## 🔄 数据流

```
用户点击"背单词"
    ↓
CheckinCard 触发 startLearning()
    ↓
currentPage = 'level-selector'
    ↓
WordLevelSelector 显示
    ↓
用户选择等级
    ↓
WordLevelSelector 触发 select 事件
    ↓
App.vue handleLevelSelect()
    - selectedLevel = 选中的等级
    - checkinCardRef.setSelectedLevel() 更新首页显示
    - currentPage = 'checkin'
    ↓
WordCheckin 显示打卡页面
    ↓
用户点击返回
    ↓
currentPage = 'home'
    ↓
首页显示（已选等级保持）
```

---

## 🎨 设计特点

### 色彩方案
- **主色** (#3b82f6) - 蓝色系，用于强调和主要操作
- **次色** - 等级卡片有不同的图标以区分

### 动画效果
- 模态框淡入动画
- 等级卡片选中时的亮度变化
- 按钮悬停提升效果
- 返回按钮移动动画

### 响应式设计
- **桌面端** (≥1024px)：3列网格布局
- **平板端** (768px-1024px)：2列网格布局  
- **手机端** (<768px)：1列全宽布局

---

## 🚀 使用步骤

### 1. 启动应用
```powershell
npm run dev
```

### 2. 测试流程
1. 打开主页
2. 点击打卡卡片中的"背单词"按钮
3. 在等级选择页面选择一个等级
4. 点击"确定"进入打卡页面
5. 点击"返回"回到主页
6. 验证首页显示已选等级

---

## 📝 后续开发建议

### 近期任务
- [ ] 实现打卡页面的实际功能（单词展示、音频播放等）
- [ ] 连接后端 API 获取对应等级的单词列表
- [ ] 实现用户选择的持久化存储（localStorage）
- [ ] 添加打卡记录统计

### 进阶功能
- [ ] 子分类功能（如四级词汇按话题分类）
- [ ] 学习进度追踪
- [ ] 复习计划生成
- [ ] 成就系统

---

## 🔧 技术栈

- **前端框架**: Vue 3 (Composition API)
- **样式**: Scoped CSS + CSS Variables
- **状态管理**: Provide/Inject (简单场景)
- **动画**: CSS Transitions & Keyframes

---

## ✨ 亮点总结

✅ **完整的用户流程** - 从主页→选择→打卡→返回，逻辑清晰  
✅ **美观的 UI 设计** - 模态框、卡片、动画等现代 Web 设计  
✅ **响应式布局** - 适配多种设备尺寸  
✅ **模块化组件** - 易于维护和扩展  
✅ **交互反馈** - 悬停、选中、加载等清晰的用户反馈  

---

## 📞 常见问题

**Q: 如何修改等级列表？**
A: 编辑 `WordLevelSelector.vue` 中的 `wordLevels` ref 数组

**Q: 如何改变返回的页面？**
A: 修改 `WordCheckin.vue` 中 `goBack()` 函数的 `currentPage.value` 值

**Q: 数据是否持久化？**
A: 当前未持久化，刷新页面后会重置。可添加 localStorage 实现持久化

**Q: 如何添加更多等级？**
A: 在 `wordLevels` 数组中添加新对象，遵循现有的结构即可
