@echo off
title BacaLog Server Launcher (MySQL 3306 + API 5000)
cd /d "%~dp0"

echo ============================================================
echo        BACALOG SERVER LAUNCHER (MYSQL + REST API)
echo ============================================================
echo.

:CEK_MYSQL
netstat -ano | findstr ":3306 " | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [OK] MySQL sudah aktif di port 3306.
    goto CEK_API
)

echo [..] MySQL belum aktif. Menyalakan MySQL XAMPP...
if not exist "C:\xampp\mysql\bin\mysqld.exe" (
    echo [!] C:\xampp\mysql\bin\mysqld.exe tidak ditemukan.
    echo     Silakan jalankan MySQL secara manual terlebih dahulu.
    pause
    exit /b 1
)

start "" /min /D "C:\xampp" "C:\xampp\mysql\bin\mysqld.exe" --defaults-file=mysql\bin\my.ini --standalone
timeout /t 3 /nobreak >nul
netstat -ano | findstr ":3306 " | findstr "LISTENING" >nul
if errorlevel 1 (
    echo [!] MySQL gagal menyala otomatis. Silakan klik Start MySQL di XAMPP Control Panel.
    pause
    exit /b 1
)
echo [OK] MySQL berhasil dinyalakan di port 3306.

:CEK_API
netstat -ano | findstr ":5000 " | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo.
    echo [OK] Backend API BacaLog sudah aktif di port 5000.
    echo     Tidak perlu membuka server ganda.
    echo     Jika ingin restart paksa, jalankan: taskkill /F /IM node.exe
    echo.
    pause
    exit /b 0
)

echo [..] Menyalakan Backend REST API BacaLog di port 5000...
echo      JANGAN TUTUP jendela ini selama aplikasi BacaLog dipakai.
echo.
node server.js
echo.
echo [!] Server berhenti.
pause
