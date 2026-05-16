# 📘 英语学习平台 (SEProject-EnglishLearning)

> 基于 Vue.js 生态构建的现代化英语学习前端应用。

## 前端部分

### 第一步：项目初始化 (Setup)

#### 安装依赖 (核心步骤)

下载项目所需的所有第三方库：

```bash
npm install
```

### 第二步：日常开发 (Development)

这是你每天开发时最常用的命令：

```bash
npm run serve
```

### 第三步：代码规范 (Linting)

**⚠️ 重点注意：此步骤在提交代码（Git Commit/Push）前必做！**

在将代码推送到 GitHub 仓库之前，请务必运行此命令来清洗代码：

```bash
npm run lint
```

### 第四步：打包部署 (Production)

当项目开发完成，准备上线时运行：

```bash
npm run build
```

---

## reCAPTCHA 配置（登录/注册）

### 1. 获取密钥

前往：
https://www.google.com/recaptcha/admin/create

请选择 **reCAPTCHA v2（“I’m not a robot”）**。否则会报类型不匹配。

### 2. 白名单域名填写

只填域名，不要带 `http://` 或端口。

必填：
- localhost
- 127.0.0.1

如果用局域网访问（IP 会变），建议用固定本地域名（如 `el.local`）并加入白名单。

### 3. 填写到项目配置

前端（Site Key）：
- `.env.development`
- `.env.production`

填写：
```
VUE_APP_RECAPTCHA_SITE_KEY=你的SiteKey（第一个）
```

后端（Secret Key）：
- `backend/english_learning_platform/src/main/resources/application.yml`

填写：
```
recaptcha:
   secret: 你的SecretKey（第二个）
```

### 4. 重启服务

配置修改后必须重启：
- 前端：`npm run serve`
- 后端：`mvn spring-boot:run`

### 5. 网络说明（可选）

如果网络无法访问 `google.com`，请确保能访问：
- www.recaptcha.net
- www.gstatic.com

---

## 后端部分

### 第一步：如果你还未初始化或者更新数据库，请参考以下步骤操作：

#### 1. 初始化数据库

在此之前，确保你已经将 MySQL 的 bin 目录加入 PATH，路径通常是：

`C:\Program Files\MySQL\MySQL Server 8.0\bin`

1. **确保 MySQL 服务已启动**

   ```bash
   # 检查服务状态
   sc query MySQL80
   ```

2. **运行导入脚本**

   ```bash
   cd .\backend\english_learning_platform
   .\import_mysql.bat
   ```

   脚本会提示输入 MySQL root 密码，然后自动完成：
   - 创建数据库
   - 导入表结构（具体文件在`.\backend\english_learning_platform\src\main\resources\schema.sql`）
   - 导入数据(具体文件在`.\backend\english_learning_platform\src\main\resources\data.sql`)

3. **更新配置文件**

   编辑 `src/main/resources/application.yml`，修改数据库密码：

   ```yaml
   spring:
     datasource:
       password: your_actual_password # 改为你的 MySQL 密码
   ```

#### 2. 题库数据补充：

需要运行

```
cd backend\english_learning_platform
.\questionbank_import.bat
```

#### 3. 单词数据补充：

需要运行

```
cd backend\english_learning_platform
.\import_words.bat
```

以导入单词相关数据

### 第二步：启动项目

```bash
mvn clean install
mvn spring-boot:run
```