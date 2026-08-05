@echo off
chcp 65001 >nul
title Codegen-Java Web Panel Launcher
cd /d %~dp0

set BACKEND_JAR=codegen-web\target\codegen-web-1.0-SNAPSHOT.jar
set BACKEND_PORT=9000

if not exist "%BACKEND_JAR%" (
    echo [ERROR] 后端 jar 不存在: %BACKEND_JAR%
    echo 请先执行: cd codegen-web ^&^& mvn package -DskipTests
    pause
    exit /b 1
)

echo 启动后端服务 (端口 %BACKEND_PORT%) ...
start "codegen-backend" cmd /k "cd /d %~dp0codegen-web && java -jar target\codegen-web-1.0-SNAPSHOT.jar --server.port=%BACKEND_PORT%"

timeout /t 8 /nobreak >nul
start "" "http://localhost:%BACKEND_PORT%/"
echo.
echo 面板地址: http://localhost:%BACKEND_PORT%/  (React 前端已打包进 jar，由后端同源托管)
echo.
echo 开发模式(热更新，可选): cd codegen-web\frontend ^&^& npm run dev  访问 http://localhost:6000
echo 关闭后端窗口即可停止服务。
echo.
pause
