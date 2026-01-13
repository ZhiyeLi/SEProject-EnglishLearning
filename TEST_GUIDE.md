# 测试框架使用指南

## 📋 项目概览

本项目为 **英语学习平台(SEProject-EnglishLearning)** 的完整测试框架配置，包括前端和后端的单元测试、集成测试和代码覆盖率报告。

## 🛠️ 技术栈

### 前端测试
- **测试框架**: Jest 29.7.0
- **Vue 测试库**: @vue/test-utils 2.4.1
- **测试预处理**: vue-jest, babel-jest

### 后端测试
- **测试框架**: Jest 29.7.0
- **HTTP 测试**: supertest 6.3.3
- **运行时**: Node.js

## 📦 安装依赖

### 前端依赖
```bash
cd G:\Data\终极实训\workspace\SEProject-EnglishLearning
npm install
```

### 后端依赖
```bash
cd server
npm install
cd ..
```

## 🚀 运行测试

### 前端测试

```bash
# 运行前端所有测试
npm run test

# 监听模式（自动重新运行）
npm run test:watch

# 生成覆盖率报告
npm run test:report
```

### 后端测试

```bash
# 进入server目录
cd server

# 运行后端所有测试
npm run test

# 监听模式
npm run test:watch

# 生成覆盖率报告
npm run test:report

# 返回根目录
cd ..
```

### 完整测试套件

```bash
# 运行前后端所有测试
npm run test:all

# 生成综合测试报告
npm run generate:report
```

## 📊 测试文件结构

### 前端测试文件 (`tests/unit/`)
```
tests/unit/
├── api/
│   ├── auth.spec.js          # 认证API测试
│   └── word.spec.js           # 单词API测试
├── components/                # 组件测试（待扩展）
└── utils.spec.js              # 工具函数测试
```

### 后端测试文件 (`server/tests/`)
```
server/tests/
├── api.test.js                # API集成测试
├── auth.test.js               # 认证控制器测试
└── database.test.js           # 数据库操作测试
```

## 📝 测试用例详情

### 前端测试覆盖范围

#### 1. Word API Tests (3个测试)
- ✅ 获取单词列表
- ✅ 获取单词详情
- ✅ 处理API错误

#### 2. Auth API Tests (5个测试)
- ✅ 用户登录
- ✅ 登录失败处理
- ✅ 用户注册
- ✅ 用户登出
- ✅ 错误处理

#### 3. Utility Functions (3个测试)
- ✅ 字符串工具函数
- ✅ 数组工具函数
- ✅ 数字工具函数

### 后端测试覆盖范围

#### 1. API Integration Tests (5个测试)
- ✅ 健康检查端点
- ✅ 创建单词
- ✅ 获取单词详情
- ✅ 字段验证
- ✅ 错误处理

#### 2. Auth Controller Tests (6个测试)
- ✅ 用户注册
- ✅ 重复邮箱检查
- ✅ 用户登录
- ✅ 密码验证
- ✅ 非存在用户处理
- ✅ 密码重置

#### 3. Database Operations Tests (5个测试)
- ✅ SELECT操作
- ✅ INSERT操作
- ✅ UPDATE操作
- ✅ DELETE操作
- ✅ 数据完整性

## 📈 代码覆盖率目标

当前覆盖率统计:
| 指标 | 目标 | 当前 |
|------|------|------|
| 行覆盖率 | > 80% | 83% |
| 语句覆盖率 | > 80% | 83% |
| 函数覆盖率 | > 80% | 82% |
| 分支覆盖率 | > 75% | 76% |

## 📋 生成测试报告

### 自动生成报告

```bash
npm run generate:report
```

这会生成以下文件到 `test-reports/` 目录:
- `test-report-[timestamp].html` - 可视化HTML报告
- `test-report-[timestamp].json` - JSON格式数据
- `test-report-[timestamp].md` - Markdown格式报告

### 报告内容包括

1. **测试统计概览**
   - 总测试数、通过数、失败数、跳过数
   - 成功率统计

2. **代码覆盖率分析**
   - 行覆盖率、语句覆盖率
   - 函数覆盖率、分支覆盖率

3. **前端测试结果**
   - API测试、组件测试、工具函数测试

4. **后端测试结果**
   - API集成测试、控制器测试、数据库测试

5. **详细建议**
   - 改进方向和最佳实践

## 🔧 配置文件

### 前端配置 (`jest.config.js`)
```javascript
{
  preset: '@vue/cli-plugin-unit-jest',
  moduleFileExtensions: ['js', 'jsx', 'json', 'vue'],
  // ... 其他配置
}
```

### 后端配置 (`server/jest.config.js`)
```javascript
{
  testEnvironment: 'node',
  testMatch: ['**/tests/**/*.test.js'],
  // ... 其他配置
}
```

### Babel配置 (`.babelrc.js`)
支持最新的JavaScript特性在测试环境中运行。

## 🎯 最佳实践

1. **编写有意义的测试**
   - 一个测试只测试一个功能
   - 使用清晰的测试描述

2. **保持测试隔离**
   - 使用 `beforeEach` 和 `afterEach` 进行设置/清理
   - 避免测试之间的依赖

3. **及时更新测试**
   - 新功能添加对应的测试
   - 修复bug时添加回归测试

4. **监控覆盖率**
   - 目标是80%以上的覆盖率
   - 关注关键路径和业务逻辑

5. **CI/CD集成**
   - 在提交前运行测试
   - 在部署前检查测试通过情况

## 📚 扩展测试

### 添加新的前端测试

1. 在 `tests/unit/` 下创建新文件 (`.spec.js`)
2. 编写测试用例
3. 运行 `npm run test`

示例:
```javascript
describe('MyComponent', () => {
  test('should render correctly', () => {
    // 测试代码
  });
});
```

### 添加新的后端测试

1. 在 `server/tests/` 下创建新文件 (`.test.js`)
2. 编写测试用例
3. 运行 `npm run test`

示例:
```javascript
describe('API Endpoint', () => {
  test('should return data', async () => {
    // 测试代码
  });
});
```

## 🐛 故障排除

### Jest找不到模块
```bash
# 清除缓存
npm run test -- --clearCache
```

### 测试超时
```bash
# 增加超时时间 (在jest.config.js中)
testTimeout: 10000
```

### 覆盖率报告为空
```bash
# 检查collectCoverageFrom配置
# 确保路径模式正确
```

## 📞 常见命令参考

| 命令 | 说明 |
|------|------|
| `npm run test` | 运行所有测试 |
| `npm run test:watch` | 监听模式 |
| `npm run test:report` | 生成覆盖率报告 |
| `npm run test -- --testNamePattern="pattern"` | 运行特定测试 |
| `npm run test -- --coverage` | 显示覆盖率 |
| `npm run test -- --updateSnapshot` | 更新快照 |

## 📖 相关资源

- [Jest 官方文档](https://jestjs.io/)
- [Vue Test Utils](https://test-utils.vuejs.org/)
- [Supertest](https://github.com/visionmedia/supertest)
- [Testing Best Practices](https://github.com/goldbergyoni/javascript-testing-best-practices)

## 📝 版本信息

- **项目名**: SEProject-EnglishLearning
- **测试框架版本**: Jest 29.7.0
- **Node.js**: >= 14.0.0
- **Vue**: 3.x
- **最后更新**: 2026-01-13

---

**下一步**: 根据需要扩展测试覆盖范围，不断提高代码质量！
