@echo off
chcp 65001 >nul
echo ====================================
echo    题库数据库初始化与导入脚本
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
echo [1/3] 开始执行数据表初始化...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < src\main\resources\questionbank_init.sql

if %errorlevel% neq 0 (
    echo [错误] 数据表初始化失败！
    echo 请检查：
    echo   1. MySQL 服务是否已启动
    echo   2. 数据库连接信息是否正确
    echo   3. questionbank_init.sql 文件是否存在
    pause
    exit /b 1
)

echo [成功] 数据表初始化完成！
echo.

echo [2/3] 开始导入题库数据...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < src\main\resources\questionbank_dataimport.sql

if %errorlevel% neq 0 (
    echo [错误] 题库数据导入失败！
    echo 请检查：
    echo   1. questionbank_dataimport.sql 文件是否存在
    echo   2. SQL 语句是否正确
    pause
    exit /b 1
)

echo [成功] 题库数据导入完成！
echo.

echo [3/3] 检查上传目录...
if not exist "uploads" (
    echo [提示] 创建 uploads 目录...
    mkdir uploads
    mkdir uploads\audio
    mkdir uploads\images
    echo [成功] 上传目录创建完成！
) else (
    if not exist "uploads\audio" mkdir uploads\audio
    if not exist "uploads\images" mkdir uploads\images
    echo [提示] 上传目录已存在，跳过创建。
)

echo.
echo ====================================
echo    所有操作已完成！
echo ====================================
echo.
echo 提示：请将音频和图片资源放置到以下目录：
echo   - 音频文件：%cd%\uploads\audio\
echo   - 图片文件：%cd%\uploads\images\
echo.
echo 访问 URL 示例：
echo.
echo   - 音频：http://localhost:8080/api/files/audio/test.mp3
echo   - 图片：http://localhost:8080/api/files/images/test.png
echo.
pause
