# AGENTS.md

easy-codegen：带可视化面板的 Java 代码生成器。读取数据库表结构，一键生成 PO/Query/Mapper/Mapper XML/Service/ServiceImpl/Controller 全套代码（对标 MyBatis-Plus / RuoYi 代码生成）。

## Project

- 语言/栈：Java 8+（Maven 多模块，无聚合 pom，各模块独立）、Spring Boot 2.7.5、React 18 + Vite + Ant Design（`easy-codegen-web/frontend`）、JavaFX（desktop）、Astro 7（landing 静态站）
- 入口：`easy-codegen-web`（REST API + 托管打包后的 React 前端）；`easy-codegen-desktop`（JavaFX 内嵌 Web 面板）
- 包根：`org.flyingsparrow`

## Commands

```bash
# 核心库（其他模块依赖它，改动后必须先 install）
cd easy-codegen && mvn install -DskipTests

# Web 模块（后端）
cd easy-codegen-web && mvn install -DskipTests     # 产出 target/xxx-exec.jar

# 前端（产物构建到 src/main/resources/static，随 jar 同源托管）
cd easy-codegen-web/frontend && npm install && npm run build
# 前端热更新：npm run dev → http://localhost:6000

# 测试
cd easy-codegen-web && mvn test                     # ConfigFileStoreTest / SecurityValidationTest

# 启动（需先构建）
start-web.bat                                       # 后端 9000 端口 + 自动开浏览器
start-desktop.bat                                   # JavaFX 桌面版

# Landing 页（独立 npm 项目，部署见 .github/workflows/deploy-landing.yml）
cd landing && npm install && npm run build
cd landing && npm run dev                           # http://localhost:4321

# Docker 一键体验（Web 面板 + MySQL 演示库，无需本地 JDK/Maven/Node/MySQL）
docker compose up -d --build                        # http://localhost:9000；MySQL 映射宿主 3307
docker compose down                                 # 停止（-v 连数据卷一起删除）
```

## Architecture

- `easy-codegen`（核心库）：`bean`（Constants/TableInfo/FieldInfo）、`builder`（BuildBase/Build* 各类型代码生成 + BuildTable 读表 + CodegenPreview 预览/ZIP）、`utils`（PropertiesUtils 读 application.properties）
- `easy-codegen-web`：Spring Boot REST API（controller/config/util）+ React 前端；`ConfigFileStore` 负责配置文件合并写回（保留注释/顺序/行尾）
- `easy-codegen-desktop`：JavaFX 桌面壳，内嵌 Web 面板并自动拉起后端
- `easy-codegen-demo`：生成代码的用法示例（`test/` 目录存放生成结果示例）
- `landing/`：项目主页（Astro 静态站，中英双语 `/zh/` `/en/`，lucide 图标），GitHub Actions 自动部署到 `https://siborne.github.io/easy-codegen/`

## Conventions

- 提交信息：中文 conventional commits（`feat(scope):` / `fix(scope):` / `refactor:`），scope 如 landing、security
- 日志：slf4j `LoggerFactory.getLogger(X.class)`；错误捕获打 `logger.error(..., e)`，不吞异常不 printStackTrace
- 资源关闭：一律 try-with-resources（已全局重构），不用 finally 手动 close
- 安全：SQL 拼接前表名必须过 `BuildTable.isValidTableName`；JSON 输出字段过滤见 Constants.IGNORE_* 配置；改动涉及输入解析/文件 IO 时跑 `mvn test` 并考虑安全 review
- 配置：运行时配置经 `PropertiesUtils`（静态加载 application.properties + `reload()` 热更新）；web 端写配置走 `ConfigFileStore`（保留注释与行尾）
- Landing：文案只改 `landing/src/i18n/ui.ts`（中英两条）；新图标先登记 `astro.config.mjs` 的 `icon.include.lucide` 白名单再使用（禁用 emoji）

## Notes

- 前端 i18n：`easy-codegen-web/frontend/src/i18n.js`（中英两套文案）；语言偏好存 `localStorage` key `ecg-lang`，主题偏好 key `ecg-theme`。后端接口通过请求头 `X-Lang`（`zh`/`en`）返回对应语言的消息（见 `easy-codegen-web/src/main/java/org/flyingsparrow/web/util/I18nMessages.java`）。
- 打包分发：本地便携版用 `package-desktop.bat`（jpackage app-image）；CI 发布用 `.github/workflows/release.yml`（push `v*` tag 触发，产出便携 zip、安装 exe、web exec jar 并发布 Release）。打包态启用 `spring.profiles.active=packaged`，配置/模板写入 `~/.easy-codegen/`，首次运行自动复制 classpath 的 `default-application.properties`。
- 已知限制：桌面模块 JavaFX 依赖写死 `win` classifier，当前仅支持 Windows 桌面打包；服务端/团队部署走 Docker。
