@echo off
chcp 65001 >nul
title Codegen-Java Web Panel Launcher
cd /d %~dp0

set BACKEND_JAR=codegen-web\target\codegen-web-1.0-SNAPSHOT.jar
set FRONTEND_DIR=codegen-web\src\main\resources\static
set BACKEND_PORT=9000
set FRONTEND_PORT=6000

if not exist "%BACKEND_JAR%" (
    echo [ERROR] 后端 jar 不存在: %BACKEND_JAR%
    echo 请先执行: cd codegen-web ^&^& mvn package -DskipTests
    pause
    exit /b 1
)

echo 启动后端 API 服务 (端口 %BACKEND_PORT%) ...
start "codegen-backend" cmd /k "cd /d %~dp0codegen-web && java -jar target\codegen-web-1.0-SNAPSHOT.jar --server.port=%BACKEND_PORT%"

echo 启动前端页面服务 (端口 %FRONTEND_PORT%) ...
start "codegen-frontend" cmd /k "cd /d %~dp0%FRONTEND_DIR% && python -m http.server %FRONTEND_PORT% --bind 127.0.0.1"

timeout /t 8 /nobreak >nul
start "" "http://localhost:%FRONTEND_PORT%/?api=http://localhost:%BACKEND_PORT%"
echo.
echo 面板地址: http://localhost:%FRONTEND_PORT%/?api=http://localhost:%BACKEND_PORT%
echo 关闭后端/前端窗口即可停止服务。
echo.
pause
