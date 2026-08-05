# easy-codegen Landing Page

项目主页（静态站），基于 [Astro](https://astro.build) 构建，中英双语，通过 GitHub Actions 自动部署到 GitHub Pages。

## 技术栈

- **Astro**（静态站点框架，纯静态输出）
- **astro-icon + @iconify-json/lucide**：图标全部来自 lucide，npm 打包、构建时内联为 SVG，**不使用任何 emoji**
- 手写 CSS（无 UI 框架）

## 本地开发

```bash
npm install
npm run dev      # 开发服务器 http://localhost:4321
npm run build    # 构建产物输出到 dist/
npm run preview  # 本地预览构建产物
```

> Windows 注意：npm 存在可选依赖 bug（[npm/cli#4828](https://github.com/npm/cli/issues/4828)），
> 可能导致 `rolldown` / `lightningcss` 的 native binding 未安装而构建失败。
> 已通过将 `@rolldown/binding-win32-x64-msvc`、`lightningcss-win32-x64-msvc`
> 显式加入 `dependencies` 规避；Linux CI 不受影响。

## 部署

推送 `master` 分支且改动涉及 `landing/**` 或工作流文件时，GitHub Actions 自动构建并部署到
GitHub Pages：`https://siborne.github.io/easy-codegen/`。

- 工作流：`.github/workflows/deploy-landing.yml`
- 首次使用需在仓库 Settings → Pages 中将 Source 设为 **GitHub Actions**
- 支持 `workflow_dispatch` 手动触发

## 目录结构

```
landing/
├── astro.config.mjs     # base=/easy-codegen、i18n(zh/en)、图标白名单
├── public/              # favicon 等静态资源
└── src/
    ├── i18n/ui.ts       # 中英双语文案字典（唯一内容源）
    ├── layouts/         # BaseLayout
    ├── components/      # Header / Hero / Features / HowItWorks / QuickStart / Modules / CTA / Footer
    ├── pages/           # / 重定向到 /zh/，[lang]/index.astro 生成 /zh/ /en/
    └── styles/          # global.css
```

## 添加内容

- 修改文案：编辑 `src/i18n/ui.ts`，中英两个语言块各加一条 key
- 新增区块：在 `src/components/` 新建组件，于 `src/pages/[lang]/index.astro` 引入
- 新增图标：先在 `astro.config.mjs` 的 `icon.include.lucide` 白名单中登记（lucide 图标名），再在组件中 `<Icon name="lucide:xxx" />` 使用
