## 🚀 快速开始

### 方式一：使用自动导入脚本初始化数据库（推荐）

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

4. **启动项目**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **（可选但推荐）添加进度视图与索引，支持去重统计**
   为不修改原有表结构、同时提升进度统计与查询性能，提供了额外 SQL：
   `src/main/resources/progress_views.sql`

   执行方式：
   ```bash
   mysql -u root -p english_learning < src/main/resources/progress_views.sql
   ```
   该脚本会：
   - 创建视图 `vw_user_passed_words`（按单词+词性去重的已打卡列表）
   - 创建视图 `vw_user_progress_summary`（每用户每类型的已打卡汇总）
   - 创建基础索引以提升查询性能（不修改原始表结构）

### 方式二：手动执行

1. **启动 MySQL 并创建数据库**

   ```sql
   mysql -u root -p
   CREATE DATABASE english_learning CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE english_learning;
   ```

2. **导入表结构**

   ```bash
   mysql -u root -p english_learning < src/main/resources/schema.sql
   ```

3. **导入数据**

   ```bash
   mysql -u root -p english_learning < src/main/resources/data.sql
   ```

4. **验证导入**
   ```sql
   mysql -u root -p english_learning
   SHOW TABLES;
   SELECT * FROM word_types;
   SELECT * FROM words LIMIT 5;
   ```

### 题库数据补充：

需要运行

```
cd backend\english_learning_platform
.\questionbank_import.bat
```

以导入题库相关数据

## 📝 配置说明

### application.yml 关键配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/english_learning?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here # ⚠️ 必须修改
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update # 开发环境：update，生产环境：validate
    show-sql: true # 开发时显示 SQL
```

### pom.xml 关键依赖

```xml
<!-- MySQL 驱动 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```
