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
- [easy-codegen-demo] - 示例模块（演示生成代码的用法）

## 安装与配置

### 前提条件
- JDK 8 或更高版本
- Maven 3.x
- Node.js 16+（仅前端开发模式需要）
- MySQL 数据库

### 一键启动

1. 构建（首次或代码更新后）：
   ```
   cd easy-codegen && mvn install -DskipTests
   cd ../easy-codegen-web && mvn package -DskipTests
   ```

2. 双击项目根目录的 `start-web.bat`，自动启动后端并打开浏览器：
   - 面板地址：http://localhost:9000

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
