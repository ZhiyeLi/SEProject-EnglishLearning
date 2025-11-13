# 单词数据导入指南

## 📋 概述

`import_words.js` 脚本支持将大量单词数据导入到 SQLite 数据库中。脚本会自动创建所有相关的表记录，包括：

- ✅ 单词基础信息 (`words` 表)
- ✅ 难度等级 (`note` 表)
- ✅ 词性信息 (`word_pos` 表)
- ✅ 翻译及例句 (`word_translation` 表)
- ✅ 词形变化 (`word_pos_changes` 表)
- ✅ 短语搭配 (`word_phrases` 表)

---

## 🚀 快速开始

### 前置条件

```powershell
cd server
npm install csv-parse
```

### 方式 1：使用 JSON 格式（推荐）

#### 数据结构

```json
[
  {
    "word_content": "run",           // 必填：单词本身
    "word_level": "A1",               // 可选：难度等级 (A1-C2)
    "part_of_speech": ["verb", "noun"], // 可选：词性列表
    "chinese_meaning": "跑；运行",    // 可选：中文含义
    "example_sentence": "I run every morning.", // 可选：例句
    "pos_changes": [                  // 可选：词形变化
      { "change_form": "running", "change_name": "现在分词" },
      { "change_form": "ran", "change_name": "过去式" }
    ],
    "phrases": [                       // 可选：短语搭配
      { 
        "phrase_content": "run away", 
        "phrase_function": "动词短语", 
        "phrase_meaning": "逃跑" 
      }
    ]
  },
  // ... 更多单词
]
```

#### 执行导入

```powershell
node import_words.js words.json
```

**预期输出：**
```
✓ 数据库连接成功
📖 读取了 100 条记录
✅ 导入完成！
   成功: 98
   失败: 2
```

---

### 方式 2：使用 CSV 格式

#### 数据结构

CSV 文件包含以下列（使用分号 `;` 分隔多值）：

```csv
word_content,word_level,part_of_speech,chinese_meaning,example_sentence,pos_changes,phrases
```

#### 具体字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| `word_content` | 单词 | `run` |
| `word_level` | 难度等级 | `A1` |
| `part_of_speech` | 词性（多个用`;`分隔） | `verb;noun` |
| `chinese_meaning` | 中文含义 | `跑；运行` |
| `example_sentence` | 例句 | `I run every morning.` |
| `pos_changes` | 词形变化（格式：`form\|name;form\|name`） | `running\|现在分词;ran\|过去式` |
| `phrases` | 短语（格式：`phrase\|function\|meaning;...`） | `run away\|动词短语\|逃跑;run into\|动词短语\|碰上` |

#### 完整示例

```csv
word_content,word_level,part_of_speech,chinese_meaning,example_sentence,pos_changes,phrases
abandon,B1,verb;noun,放弃；遗弃,I will not abandon my dreams.,abandoned|过去式;abandoning|现在分词,abandon hope|动词短语|放弃希望;abandon ship|动词短语|弃船
run,A1,verb;noun,跑；运行,I run every morning.,running|现在分词;ran|过去式,run away|动词短语|逃跑;run into|动词短语|碰上
```

#### 执行导入

```powershell
node import_words.js words.csv
```

---

## 📝 数据准备建议

### 1. Excel 转换到 JSON

使用 Python 快速转换：

```python
import json
import pandas as pd

# 读取 Excel 文件
df = pd.read_excel('words.xlsx')

# 转换为 JSON
data = df.to_dict(orient='records')

# 保存为 JSON
with open('words.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)
```

### 2. Excel 转换到 CSV

```powershell
# 在 Excel 中直接另存为 CSV (UTF-8)
```

### 3. 数据验证

导入前建议检查：

- ✅ 单词是否为空
- ✅ 中文含义是否正确
- ✅ 词性是否标准（noun, verb, adjective 等）
- ✅ 难度等级是否有效（A1, A2, B1, B2, C1, C2）

---

## 🔍 常见问题

### Q1: 如何处理重复的单词？

脚本使用 `INSERT OR IGNORE`，相同的单词只会导入一次。

### Q2: 导入失败如何恢复？

所有操作都在事务中进行。如果导入中断，数据库状态保持不变。

### Q3: 如何批量导入多个文件？

```powershell
# 方案 1：创建脚本
Get-ChildItem *.json | ForEach-Object { node import_words.js $_.FullName }

# 方案 2：合并多个 JSON 文件后导入
```

### Q4: 如何查看导入结果？

```powershell
# 在 Node.js 中查询
cd ..
node -e "
const sqlite3 = require('sqlite3');
const db = new sqlite3.Database('server/data/elw.sqlite');
db.all('SELECT COUNT(*) as count FROM words', (err, rows) => {
  console.log('单词总数:', rows[0].count);
  db.close();
});
"
```

---

## 🎯 导入流程图

```
数据文件 (JSON/CSV)
    ↓
验证格式和字段
    ↓
开启数据库事务
    ↓
逐条插入单词及相关数据
  ├→ words 表
  ├→ note 表（难度）
  ├→ word_pos 表（词性）
  ├→ word_translation 表（翻译）
  ├→ word_pos_changes 表（词形变化）
  └→ word_phrases 表（短语）
    ↓
提交事务
    ↓
输出导入统计
```

---

## 📊 表结构关系

```
words (单词)
├── note (难度等级)
├── word_pos (词性)
│   ├── word_translation (翻译)
│   └── word_pos_changes (词形变化)
└── word_phrases (短语)
```

---

## 💡 最佳实践

1. **小规模测试**：先用 10 条单词测试导入
2. **验证数据质量**：确保无空值和格式错误
3. **使用事务**：脚本自动使用事务，确保数据一致性
4. **备份数据库**：大规模导入前备份 `elw.sqlite`
5. **检查日志**：注意导入过程中的任何警告或错误

---

## 📞 技术支持

如遇到问题，请检查：
- 数据库连接是否正常
- CSV/JSON 格式是否正确
- 字符编码是否为 UTF-8
- 文件路径是否正确
