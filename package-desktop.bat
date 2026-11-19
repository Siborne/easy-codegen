@echo off
chcp 65001 >nul
title Easy-Codegen 便携版打包器
cd /d %~dp0

set DESKTOP_JAR=easy-codegen-desktop\target\easy-codegen-desktop-1.0-SNAPSHOT.jar

if not exist "%DESKTOP_JAR%" (
    echo [ERROR] 桌面版 jar 不存在: %DESKTOP_JAR%
    echo 请先执行: cd easy-codegen-desktop ^&^& mvn package -DskipTests
    pause
    exit /b 1
)

if "%JAVA_HOME%"=="" (
    echo [ERROR] 请先设置 JAVA_HOME 指向 JDK 17+
    pause
    exit /b 1
)

set JPACKAGE=%JAVA_HOME%\bin\jpackage.exe
if not exist "%JPACKAGE%" (
    echo [ERROR] 当前 JDK 未找到 jpackage: %JPACKAGE%
    pause
    exit /b 1
)

set VERSION=1.0.0
set OUT=dist

echo 正在生成免安装便携版 ...
"%JPACKAGE%" --type app-image ^
    --name easy-codegen ^
    --app-version %VERSION% ^
    --input easy-codegen-desktop\target ^
    --main-jar easy-codegen-desktop-1.0-SNAPSHOT.jar ^
    --main-class org.springframework.boot.loader.JarLauncher ^
    --java-options "-Dspring.profiles.active=packaged" ^
    --dest %OUT%\app-image

if errorlevel 1 (
    echo [ERROR] 打包失败
    pause
    exit /b 1
)

echo.
echo 打包完成: %OUT%\app-image\easy-codegen
echo 整个 easy-codegen 目录可直接拷贝给他人使用（免安装、无需 Java）。
pause
