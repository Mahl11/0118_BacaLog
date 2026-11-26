@echo off
title BacaLog Backend Server (Port 5000)
cd /d "%~dp0"
echo ===================================================
echo     MEMULAI BACALOG BACKEND REST API (PORT 5000)
echo ===================================================
echo Pastikan MySQL di XAMPP sudah berjalan di port 3306.
echo.
node server.js
pause
