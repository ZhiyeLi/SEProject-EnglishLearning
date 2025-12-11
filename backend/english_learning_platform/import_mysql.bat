@echo off
chcp 65001 >nul
echo ========================================
echo MySQL 数据库导入脚本
echo ========================================
echo.

:: 设置变量
set DB_NAME=english_learning
set DB_USER=root
set SCRIPT_DIR=%~dp0
set SCHEMA_FILE=%SCRIPT_DIR%src\main\resources\schema.sql
set DATA_FILE=%SCRIPT_DIR%src\main\resources\data.sql

echo 请输入 MySQL root 密码:
set /p DB_PASS=

echo.
echo [1/4] 检查 MySQL 服务状态...
sc query MySQL80 >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ MySQL 服务未运行，请先启动 MySQL 服务
    pause
    exit /b 1
)
echo ✅ MySQL 服务正在运行

echo.
echo [2/4] 创建数据库...
mysql -u %DB_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %errorlevel% equ 0 (
    echo ✅ 数据库 %DB_NAME% 创建成功
) else (
    echo ❌ 数据库创建失败，请检查密码是否正确
    pause
    exit /b 1
)

echo.
echo [3/4] 导入表结构...
mysql -u %DB_USER% -p%DB_PASS% %DB_NAME% < "%SCHEMA_FILE%" 2>nul
if %errorlevel% equ 0 (
    echo ✅ 表结构导入成功
) else (
    echo ❌ 表结构导入失败
    pause
    exit /b 1
)

echo.
echo [4/4] 导入数据...
mysql -u %DB_USER% -p%DB_PASS% %DB_NAME% < "%DATA_FILE%" 2>nul
if %errorlevel% equ 0 (
    echo ✅ 数据导入成功
) else (
    echo ❌ 数据导入失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✨ 数据库导入完成！
echo ========================================
echo.
echo 数据库名: %DB_NAME%
echo 字符集: utf8mb4
echo.
echo 已经完成数据库的初始化！下一步请你手动完成操作:
echo "1. 更新 application.yml 中的数据库密码"
echo "2. 运行 mvn spring-boot:run 启动项目"
echo .
pause
