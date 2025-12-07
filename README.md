# 📘 英语学习平台 (SEProject-EnglishLearning)

> 基于 Vue.js 生态构建的现代化英语学习前端应用。

## ✅ 环境准备 (Prerequisites)

在开始之前，请确保你的终端环境满足以下硬性要求：

- **Node.js**: `v20` 或 `v22` (推荐)
  - _检查命令:_ `node -v`
- **包管理器 (NPM)**: 通常随 Node.js 一起安装
  - _检查命令:_ `npm -v`

---

## 🛠️ 第一步：项目初始化 (Setup)

### 1. 配置国内加速镜像 (可选但推荐)

为了提升依赖下载速度，建议将 NPM 源切换为淘宝（阿里云）镜像：

```bash
npm config set registry https://registry.npmmirror.com
```

### 2. 安装依赖 (核心步骤)

下载项目所需的所有第三方库：

```bash
npm install
```

> **💡 关于安装日志的解读 (Log Guide)**
>
> 运行命令后，你会看到一大串日志。请不必惊慌，只需关注以下三点：
>
> 1.  ✅ **成功的标志**
>     - 看到类似 `added 6 packages... and audited 949 packages`。
>     - 只要**没有**出现红色的 `ERR!` 或 `Command failed`，即代表安装成功。
> 2.  🛡️ **关于 "vulnerabilities" (漏洞警告)**
>     - _日志:_ `12 vulnerabilities (8 moderate, 4 high)`
>     - _解释:_ 这是开发工具层面的安全提示，**不影响项目运行**，不需要理会。
>     - _警告:_ **千万不要**运行 `npm audit fix --force`，否则会导致版本不兼容。
> 3.  💰 **关于 "funding" (求打赏)**
>     - _日志:_ `141 packages are looking for funding`
>     - _解释:_ 这是开源作者的广告，**直接忽略**即可。

---

## 💻 第二步：日常开发 (Development)

### 🟢 启动开发服务器

这是你每天开发时最常用的命令：

```bash
npm run serve
```

> **💡 命令解释：**
>
> - **功能**: 启动一个本地热更新服务器（默认端口 8080）。
> - **特性 (HMR)**: 修改代码并保存后，浏览器会自动刷新。
> - **注意**: 此模式下生成的文件都在**内存**中，不会写入硬盘。

---

## 🧹 第三步：代码规范 (Linting)

**⚠️ 重点注意：此步骤在提交代码（Git Commit/Push）前必做！**

在将代码推送到 GitHub 仓库之前，请务必运行此命令来清洗代码：

```bash
npm run lint
```

> **💡 命令解释：**
>
> - **功能**: 基于 `ESLint` 扫描全项目代码。
> - **自动修复**: 它会自动把缩进不对、分号缺失、引号混用等**格式问题**全部修好。
> - **错误提示**: 如果有逻辑错误（如定义了变量却没使用），它会在控制台报错提示你手动修改。
> - **作用**: 确保团队成员的代码风格保持高度一致，减少 Git 冲突。

---

## 📦 第四步：打包部署 (Production)

当项目开发完成，准备上线时运行：

```bash
npm run build
```

> **💡 命令解释：**
>
> - **Output**: 会在根目录生成 `dist/` 文件夹。
> - **部署**: 将 `dist` 文件夹内的内容上传到 Nginx、Apache 或 Vercel 服务器。

---

## ❓ 常见问题 (Troubleshooting)

**Q1: 安装时报错 `ERESOLVE unable to resolve dependency tree`？**

- **原因**: 依赖包版本冲突。
- **解决**: 请尝试使用 `npm install --legacy-peer-deps` 命令代替。

**Q2: 运行报错 `node-ipc` 或 `opensslErrorStack`？**

- **原因**: Node.js 版本过高（v17+）与旧版构建工具不兼容。
- **解决**: 确保 Node 版本降级为 v20/v22，或设置环境变量 `NODE_OPTIONS=--openssl-legacy-provider`。

**Q3: 遇到奇怪的报错或跑不起来？**

- **解决**: 执行“重装大法”：
  1. 删除 `node_modules` 文件夹
  2. 删除 `package-lock.json` 文件
  3. 重新运行 `npm install` (或 `npm install --legacy-peer-deps`)
  4. 运行 `npm run serve`

---

## ⚙️ 参考资源

- [Vue CLI 配置指南](https://cli.vuejs.org/zh/config/)
- [ESLint 规则说明](https://eslint.org/docs/user-guide/rules)




英语学习平台后端服务文档

# 📘 英语学习平台后端服务 (English Learning Platform)

> 基于 Spring Boot 生态构建的英语学习平台后端 API 服务，提供用户管理、学习计划等核心功能。

✅ 环境准备 (Prerequisites)

在开始之前，请确保你的开发环境满足以下硬性要求：

- JDK: `17` 或更高版本
- _检查命令:_ `java -version`

- MySQL: `8.0` 或更高版本
- _检查命令 (MySQL客户端):_ `mysql --version`

- Maven: `3.6` 或更高版本 (或使用项目内置 mvnw)
- _检查命令:_ `mvn -v`


---
🛠️ 第一步：项目初始化 (Setup)

1. 配置国内加速镜像 (可选但推荐)

为了提升依赖下载速度，建议将 Maven 源切换为阿里云镜像：

<!-- 在项目根目录的 pom.xml 中添加以下配置 -->
<repositories>
    <repository>
        <id>aliyun</id>
        <url>https://maven.aliyun.com/repository/public</url>
    </repository>
</repositories>

2. 克隆代码仓库 (核心步骤)

将项目代码拉取到本地：

git clone <仓库地址>
cd english_learning_platform

3. 数据库准备 (核心步骤)

创建项目所需数据库并导入初始化脚本：

-- 创建数据库（字符集适配多语言）
CREATE DATABASE IF NOT EXISTS db_for_engnet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

关于初始化脚本的说明 (Script Guide)
- 初始化脚本 `schema.sql` 需联系管理员获取
- 执行脚本可通过 MySQL 客户端、Navicat 等工具完成，确保脚本执行无报错

4. 配置文件修改 (核心步骤)

编辑 `src/main/resources/application.yml`，修改数据库连接及JWT配置：

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db_for_engnet?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: 你的数据库用户名
    password: 你的数据库密码

# 可选（生产环境推荐配置）
jwt:
  secret: 你的密钥字符串（建议至少32位）
  expiration: 86400000  # 24小时（毫秒）


---
💻 第二步：日常开发 (Development)

🟢 启动开发服务器

这是你日常开发时最常用的命令：

# Linux/Mac 系统
./mvnw spring-boot:run

# Windows 系统
mvnw.cmd spring-boot:run

命令解释：
- 功能: 启动 Spring Boot 内置服务器（默认端口 8080）。
- 特性 (热更新): 开发中修改代码后，部分变更可实时生效（视配置而定）。
- 访问地址: 服务默认启动在 `http://localhost:8080`。


---
📋 核心API接口说明 (API List)

🔐 认证接口（无需认证）

- - `POST /api/auth/login` - 用户登录

- - `POST /api/auth/verify-code` - 发送验证码

📝 计划管理接口

- - `POST /api/plans` - 创建学习计划

- - `GET /api/plans/date/{date}` - 获取指定日期的计划

- - `PUT /api/plans/{id}/complete` - 切换计划完成状态

- - `PUT /api/plans/{id}` - 更新计划

- - `DELETE /api/plans/{id}` - 删除计划

- - `POST /api/plans/batch/{date}` - 批量保存某日计划

- - `GET /api/plans/range` - 查询日期范围内的计划

👤 用户接口（需认证）

- - `PUT /api/user/info` - 更新用户信息


---
📁 项目结构说明 (Project Structure)

src/main/java/com/example/english_learning_platform/
├── config/           # 配置类（安全配置、数据库配置等）
├── controller/       # API接口层
├── dto/              # 数据传输对象
├── entity/           # 数据库实体类
├── exception/        # 异常处理
├── repository/       # 数据访问层
├── service/          # 业务逻辑层
│   └── impl/         # 业务逻辑实现
└── util/             # 工具类（JWT、响应结果等）


---
🧱 技术栈说明 (Tech Stack)

- - 框架: Spring Boot 4.0.0

- - 安全: Spring Security + JWT

- - ORM: Spring Data JPA

- - 数据库: MySQL

- -构建工具: Maven 3.9.11

- - JDK: Java 17


---
❓ 常见问题 (Troubleshooting)

Q1: 启动时报数据库连接错误？

- 原因: 数据库未启动、账号密码错误或数据库未创建。
- 解决:
 1. 检查 MySQL 服务是否正常运行
 2. 确认 `application.yml` 中的数据库用户名/密码正确
 3. 确保 `db_for_engnet` 数据库已创建

Q2: JWT 验证失败？

- 原因: Token 过期或密钥不一致。
- 解决:
 1. 检查 Token 是否超过默认 24 小时有效期
 2. 确认前后端使用的 JWT 密钥完全一致

Q3: Maven 依赖下载缓慢？

- 原因: 默认镜像源访问速度慢。
- 解决: 配置阿里云 Maven 镜像（参考第一步第1点）。
