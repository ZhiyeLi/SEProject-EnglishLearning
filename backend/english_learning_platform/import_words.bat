@echo off
chcp 65001 >nul
echo ====================================
echo    单词数据库初始化与导入脚本
echo ====================================
echo.

REM 数据库配置（从 application.yml 读取）
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=english_learning
set DB_USER=root

REM 提示用户输入密码
set /p DB_PASSWORD=请输入数据库密码: 

echo.
echo [1/2] 开始执行数据表初始化...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < src\main\resources\reimport.sql

if %errorlevel% neq 0 (
    echo [错误] 数据表初始化失败！
    echo 请检查：
    echo   1. MySQL 服务是否已启动
    echo   2. 数据库连接信息是否正确
    echo   3. reimport.sql 文件是否存在
    pause
    exit /b 1
)

echo [成功] 数据表初始化完成！
echo.

echo [2/2] 开始导入单词数据...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < src\main\resources\words_dataimport.sql

if %errorlevel% neq 0 (
    echo [错误] 单词数据导入失败！
    echo 请检查：
    echo   1. words_dataimport.sql 文件是否存在
    echo   2. SQL 语句是否正确
    pause
    exit /b 1
)

echo [成功] 单词数据导入完成！
echo.


echo.
echo ====================================
echo    所有操作已完成！
echo ====================================
echo.
pause
