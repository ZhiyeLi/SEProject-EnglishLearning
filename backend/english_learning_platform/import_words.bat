@echo off
chcp 65001 > nul
cd /d "C:\Users\31662\Desktop\SEProject-EnglishLearning\backend\english_learning_platform"
mysql -u root -pqwer1234 english_learning --default-character-set=utf8mb4 < import_words.sql
echo.
echo Import completed.
pause
