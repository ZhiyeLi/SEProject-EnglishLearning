# 打卡功能快速使用指南

## 🚀 快速开始

### 1. 启动应用
```bash
npm run dev
```

### 2. 进入打卡流程
```
主页 → 点击"背单词"按钮 → 选择等级 → 进入打卡页面
```

---

## ✨ 新功能演示

### 功能 1：打卡进度管理

打卡页面会显示：
```
┌─ 当前等级 ──────────────────────┐
│ 📖 英语四级词汇 | 共 3500 词    │
├──────────────────────────────────┤
│ 进度条: ████░░░░░░░░░░░░░░░░    │
│ 875 / 3500 词              25%   │
├──────────────────────────────────┤
│ 状态: ⏸️  已暂停·上次进度: 875   │
└──────────────────────────────────┘
```

### 功能 2：切换等级 🔄

1. 点击"🔄 切换等级"按钮
2. 选择新的等级
3. 原等级进度保留，新等级继续打卡

**数据保留示例：**
```
之前：CET4 已打卡 50 个 (已暂停)
切换后：
  - CET4：进度 50 (保持暂停)
  - CET6：进度 0 (恢复) → 可继续打卡
再切换回：
  - CET4：进度 50 (恢复)
```

### 功能 3：暂停/继续 ⏸️ ▶️

**按钮状态转换：**
```
开始打卡
    ↓
[继续打卡] [暂停]
    ↓
(暂停后)
[继续打卡]
    ↓
[继续打卡] [暂停]
```

**示例步骤：**
1. 点击"开始打卡" → 进度变为 1
2. 继续点击"继续打卡" 4 次 → 进度变为 5
3. 点击"暂停" → 进度保持 5，状态改为"已暂停"
4. 刷新浏览器 → 进度仍为 5（数据已保存）
5. 点击"继续打卡" → 进度变为 6

### 功能 4：复习面板 📚

1. 点击"📚 复习已学单词"按钮
2. 查看统计信息：
   - 总共学过：XX 个
   - 已复习：XX 个
   - 需复习：XX 个
3. 点击"开始复习"（后续功能开发）

---

## 📊 数据持久化

### 自动保存

所有操作自动保存到浏览器 localStorage：
- ✅ 打卡进度
- ✅ 暂停/继续状态
- ✅ 等级切换记录
- ✅ 学习统计

### 查看保存的数据

打开浏览器开发者工具 (F12)，在控制台执行：

```javascript
// 查看所有进度数据
console.log(JSON.parse(localStorage.wordCheckinProgress))

// 查看特定等级的进度
console.log(JSON.parse(localStorage.wordCheckinProgress).cet4)

// 导出数据（复制到剪贴板）
copy(localStorage.wordCheckinProgress)
```

### 重置数据

在控制台执行：

```javascript
// 重置特定等级
checkinManager.resetProgress('cet4')

// 清空所有数据
checkinManager.clearAllData()
localStorage.clear()
location.reload()
```

---

## 🎮 完整操作流程

### 场景 A：首次打卡

```
1. 进入打卡页面
   ↓
2. 显示：「当前进度为 0，开始打卡」
   ↓
3. 点击"开始打卡"按钮
   ↓
4. 显示：进度 1/3500 (按下按钮次数)
   ↓
5. 继续点击"继续打卡"多次
   ↓
6. 进度持续增加
```

### 场景 B：暂停并切换等级

```
1. CET4 已打卡到进度 50
   ↓
2. 点击"暂停"按钮
   ↓
3. 显示：「已暂停·上次进度: 50」
   ↓
4. 点击"切换等级"按钮
   ↓
5. 选择 CET6
   ↓
6. CET4 保持暂停 (进度 50)
   CET6 状态恢复 (进度 0)
   ↓
7. 可在 CET6 继续打卡
```

### 场景 C：多等级交替打卡

```
① CET4 打卡 10 个 → 暂停
   ↓
② 切换到 CET6 → 打卡 20 个 → 暂停
   ↓
③ 切换到托福 → 打卡 5 个 → 暂停
   ↓
④ 切换回 CET4 → 恢复 (显示进度 10)
   ↓
⑤ 继续打卡 CET4
```

---

## 🎯 核心特性

| 特性 | 说明 | 示例 |
|------|------|------|
| 等级切换 | 随时切换，进度独立保存 | CET4→CET6→考研 |
| 暂停恢复 | 保留当前进度 | 暂停在 100，恢复后仍是 100 |
| 进度显示 | 实时显示进度条和百分比 | 25% (875/3500) |
| 状态提示 | 清晰的状态信息 | 「已暂停·上次进度: 50」 |
| 数据持久化 | 自动保存到 localStorage | 刷新后进度保持 |
| 复习统计 | 显示学习数据统计 | 总学 50，已复习 20 |

---

## 🔍 故障排查

### 问题 1：进度不保存

**症状：** 刷新后进度变为 0

**解决方案：**
```javascript
// 检查 localStorage 是否启用
console.log(localStorage.length)

// 检查进度数据
console.log(localStorage.wordCheckinProgress)

// 如果为空，重新初始化
checkinManager.progressData = checkinManager.initializeProgressData()
checkinManager.saveProgressData()
```

### 问题 2：切换等级后无法恢复

**症状：** 切换等级后原等级数据丢失

**解决方案：**
```javascript
// 检查切换逻辑
const progress = checkinManager.getProgress('cet4')
console.log(progress)  // 应该显示非零进度

// 手动触发恢复
checkinManager.resumeCheckin('cet4')
```

### 问题 3：暂停按钮不显示

**症状：** 进行中时暂停按钮未显示

**排查代码：**
```javascript
// 检查进度状态
console.log(currentProgress.value)

// 应该显示：
// {
//   currentProgress: > 0
//   isPaused: false
//   ...
// }
```

---

## 📈 性能指标

| 指标 | 值 | 说明 |
|------|-----|------|
| localStorage 占用 | < 1KB | 进度数据很轻量 |
| 加载时间 | < 10ms | 数据恢复快速 |
| 响应时间 | < 100ms | UI 更新流畅 |
| 最大单词数 | 6500 | 雅思词汇 |

---

## 🎓 学习建议

1. **建立打卡习惯** - 每天固定时间打卡
2. **合理分配时间** - 不要一次打卡太多
3. **按时复习** - 定期复习已学单词
4. **循环学习** - 在不同等级间循环巩固
5. **记录统计** - 定期查看学习进度

---

## 📝 记录模板

在浏览器控制台运行以记录学习数据：

```javascript
// 生成学习报告
const progress = JSON.parse(localStorage.wordCheckinProgress)
Object.entries(progress).forEach(([level, data]) => {
  console.log(`
${level}:
  已打卡: ${data.currentProgress}
  状态: ${data.isPaused ? '暂停' : '进行中'}
  上次打卡: ${new Date(data.lastCheckInTime).toLocaleString()}
  `)
})
```

---

## 💬 反馈和改进

如果发现任何问题或有改进建议，请记录以下信息：

- 操作步骤
- 预期结果
- 实际结果
- 浏览器和版本
- localStorage 中的数据状态

---

**祝你打卡愉快！🎉**
