# syntax=docker/dockerfile:1
#
# easy-codegen 多阶段构建：
#   阶段 1  node      构建 React 前端（vite outDir 直接输出到 src/main/resources/static）
#   阶段 2  maven     先 install 核心库 easy-codegen，再 package easy-codegen-web（产出 -exec.jar）
#   阶段 3  temurin8  运行 Web 面板（含托管的前端）

# ---------- 阶段 1：构建 React 前端 ----------
FROM node:22-alpine AS frontend-build
WORKDIR /app
COPY easy-codegen-web/frontend/package.json easy-codegen-web/frontend/package-lock.json ./easy-codegen-web/frontend/
RUN cd easy-codegen-web/frontend && npm ci
COPY easy-codegen-web/frontend/ ./easy-codegen-web/frontend/
RUN cd easy-codegen-web/frontend && npm run build

# ---------- 阶段 2：构建后端（核心库 + Web 模块） ----------
FROM maven:3.9-eclipse-temurin-8 AS backend-build
WORKDIR /build
COPY easy-codegen easy-codegen
COPY easy-codegen-web easy-codegen-web
# 前端产物从阶段 1 带入（覆盖本地可能存在的旧产物）
COPY --from=frontend-build /app/easy-codegen-web/src/main/resources/static easy-codegen-web/src/main/resources/static
# 无聚合 pom：必须先 install 核心库（其他模块依赖它的 1.0-SNAPSHOT）
RUN mvn -q -f easy-codegen/pom.xml install -DskipTests \
    && mvn -q -f easy-codegen-web/pom.xml package -DskipTests

# ---------- 阶段 3：运行 ----------
FROM eclipse-temurin:8-jre
WORKDIR /app
ENV TZ=Asia/Shanghai \
    # 运行时配置：容器内固定路径（默认指向仓库相对路径，容器中不存在）
    CODEGEN_CONFIG_FILE=/app/config/application.properties \
    CODEGEN_TEMPLATE_DIR=/app/config-templates
COPY --from=backend-build /build/easy-codegen-web/target/*-exec.jar /app/app.jar
COPY easy-codegen/src/main/resources/application.properties /app/config/application.properties
COPY easy-codegen-web/config-templates /app/config-templates
# application.yml 默认只绑定 127.0.0.1:8090，容器内需放开为 0.0.0.0:9000
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--server.address=0.0.0.0", "--server.port=9000"]
