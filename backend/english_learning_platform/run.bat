@echo off
chcp 65001 >nul
echo ========================================
echo MySQL 后端快速测试
echo ========================================
echo.

cd /d %~dp0

echo [1/3] 检查 MySQL 数据库...
mysql -u root -p -e "USE english_learning; SELECT COUNT(*) as word_count FROM words; SELECT COUNT(*) as type_count FROM word_types;" 2>nul
if %errorlevel% neq 0 (
    echo ❌ 数据库未准备好，请先运行 import_mysql.bat
    pause
    exit /b 1
)

echo.
echo [2/3] 编译项目...
call mvn clean compile
if %errorlevel% neq 0 (
    echo ❌ 编译失败，请检查代码
    pause
    exit /b 1
)

echo.
echo [3/3] 启动 Spring Boot 应用...
echo.
echo ========================================
echo 🚀 服务正在启动...
echo ========================================
echo.
echo 启动后可以访问以下测试端点：
echo   - http://localhost:8080/api/test/health
echo   - http://localhost:8080/api/test/stats  
echo   - http://localhost:8080/api/test/words
echo   - http://localhost:8080/api/test/word-types
echo.
echo 按 Ctrl+C 停止服务
echo ========================================
echo.

call mvn spring-boot:run
