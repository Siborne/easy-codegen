# easy-codegen Java代码生成器（可视化面板）

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)

## 介绍

easy-codegen 是一个带可视化面板的 Java 代码生成器，用于根据数据库表结构自动生成对应的 Java 实体类（PO）、查询对象（Query）、Mapper 接口及 XML 映射文件、Service / ServiceImpl、Controller 等全套代码。它设计用来简化开发过程中繁琐的重复性代码编写工作，对标 MyBatis-Plus 与 RuoYi 的代码生成能力，提供更直观的图形化操作。

## 目录结构

- [easy-codegen] - 核心代码生成模块（数据库结构读取、代码生成引擎）
    - `/src/main/java/org/flyingsparrow/bean` - 实体类（Constants/FieldInfo/TableInfo）
    - `/src/main/java/org/flyingsparrow/builder` - 生成器核心类（Build* 系列 + GenerateEngine）
    - `/src/main/java/org/flyingsparrow/utils` - 工具类
    - `/src/main/resources/template` - 生成模板
- [easy-codegen-web] - 可视化面板模块（Spring Boot + React 前端）
    - `/src/main/java/org/flyingsparrow/web` - REST API（配置/表/生成/预览/模板）
    - `/src/main/resources/static` - 前端构建产物
    - `/frontend` - React 前端源码（Vite + Ant Design）
- [easy-codegen-desktop] - 桌面版（JavaFX 窗口内嵌 Web 面板，自动拉起后端服务）
- [easy-codegen-demo] - 示例模块（演示生成代码的用法）
- [landing] - 项目主页（Astro 静态站，中英双语，GitHub Actions 自动部署到 GitHub Pages，见 [landing/README.md](landing/README.md)）

## 安装与配置

### 前提条件
- JDK 8 或更高版本
- Maven 3.x
- Node.js 16+（仅前端开发模式需要）
- MySQL 数据库

### Docker 快速体验（推荐）

无需本地安装 JDK / Maven / Node / MySQL，一条命令即可启动 **Web 面板 + MySQL 演示库**：

```
docker compose up -d --build
```

1. 构建并启动后，浏览器打开 <http://localhost:9000>
2. 在面板「数据库连接」页签填入：主机 `mysql`、端口 `3306`、用户名 `root`、密码 `123456`、库名 `linklite`（容器首次启动已自动建好 `user_info` 演示表；JDBC 在 app 容器内执行，主机填 compose 服务名 `mysql` 而非 localhost）
3. 「测试连接」通过后，加载数据表 → 一键生成代码

其他说明：

- 镜像为多阶段构建（node 构建 React 前端 → maven 打包后端 → JRE 运行），产物为 `easy-codegen-web/target/*-exec.jar`；完整流程见 `Dockerfile`
- 只跑面板、连接你自己的 MySQL：`docker build -t easy-codegen:latest . && docker run -p 9000:9000 easy-codegen:latest`
- 运行时配置：容器内 `/app/config/application.properties`（即仓库 `easy-codegen/src/main/resources/application.properties`），可用 `CODEGEN_CONFIG_FILE` 环境变量覆盖；模板目录 `/app/config-templates`
- 生成输出路径：默认配置 `path.base` 指向开发机 Windows 路径，容器内生成前请在面板「输出与包名」页签改为容器内可写目录（如 `/app/generated`），或将宿主目录挂载进容器后指定
- 停止：`docker compose down`（保留 MySQL 数据）；连数据卷一起清理：`docker compose down -v`
- 容器内 Web 面板绑定 `0.0.0.0:9000`（覆盖 application.yml 默认的仅本机 127.0.0.1:8090）

### 一键启动

1. 构建（首次或代码更新后）：
   ```
   cd easy-codegen && mvn install -DskipTests
   cd ../easy-codegen-web && mvn install -DskipTests
   cd ../easy-codegen-desktop && mvn package -DskipTests
   ```

2. 两种使用方式任选：
   - **桌面版**：双击 `start-desktop.bat`（JavaFX 窗口，自动拉起后端服务，优先 9000 端口）
   - **浏览器版**：双击 `start-web.bat`（后端 9000 端口 + 自动打开浏览器）

### 开发模式（前端热更新）

```
cd easy-codegen-web/frontend
npm install
npm run dev     # 访问 http://localhost:6000，/api 自动代理到 9000
```

## 使用方法

1. 打开面板，在「数据库连接」页签填入数据库连接信息并保存
2. 点击「测试连接」验证连通性
3. 在「数据表」页签加载表列表，勾选需要生成的表（不勾选 = 全部）
4. 按需调整「生成参数」「输出与包名」配置
5. 在「代码生成」页签一键生成，可预览代码、按类型生成、下载 zip
6. 支持将多套配置保存为模板，随时切换

## 核心能力

- 数据库表结构自动读取（表名/注释/字段/索引）
- 一键生成 PO / Query / Mapper / Mapper XML / Service / ServiceImpl / Controller
- 代码在线预览、按类型（Entity/Mapper/Service/Controller 等）选择性生成
- 生成结果 zip 打包下载
- 配置模板保存 / 加载 / 删除
- 亮色 / 暗色主题切换

## 开发者指南

核心生成逻辑位于 [GenerateEngine](easy-codegen/src/main/java/org/flyingsparrow/builder/GenerateEngine.java)，扩展生成类型可继承 `Build*` 系列类。前端组件位于 `easy-codegen-web/frontend/src/components`。

## 许可证

本项目采用 [MIT License](https://opensource.org/licenses/MIT)，详情请查看 LICENSE 文件

## 联系信息

如需联系开发者，请发送邮件至 [bo7785888@gmail.com]
