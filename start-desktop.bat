@echo off
chcp 65001 >nul
title Easy-Codegen Desktop Launcher
cd /d %~dp0

set DESKTOP_JAR=easy-codegen-desktop\target\easy-codegen-desktop-1.0-SNAPSHOT.jar

if not exist "%DESKTOP_JAR%" (
    echo [ERROR] 桌面版 jar 不存在: %DESKTOP_JAR%
    echo 请先执行: cd easy-codegen-desktop ^&^& mvn package -DskipTests
    pause
    exit /b 1
)

echo 启动 Easy-Codegen 桌面版 ...
java -jar "%DESKTOP_JAR%"
