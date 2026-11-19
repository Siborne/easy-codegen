// @ts-check
import { defineConfig } from 'astro/config';
import icon from 'astro-icon';

// GitHub Pages 部署路径：https://siborne.github.io/easy-codegen/
// 本地构建默认与 Pages 路径一致，可用 BASE_PATH 环境变量覆盖
const basePath = process.env.BASE_PATH || '/easy-codegen';

// https://astro.build/config
export default defineConfig({
  site: 'https://siborne.github.io',
  base: basePath,
  i18n: {
    defaultLocale: 'zh',
    locales: ['zh', 'en'],
    routing: {
      // 所有语言均带前缀：/zh/、/en/
      prefixDefaultLocale: true,
    },
  },
  redirects: {
    // Astro 的 redirect 目标是相对站点根路径的绝对路径，需手动包含 base
    '/': `${basePath}/zh/`,
  },
  integrations: [
    icon({
      // 图标数据来自 npm 包 @iconify-json/lucide，构建完全离线
      include: {
        lucide: [
          'zap',
          'sparkles',
          'rocket',
          'github',
          'database',
          'file-code',
          'eye',
          'package',
          'save',
          'palette',
          'plug',
          'table',
          'settings',
          'wand',
          'check',
          'terminal',
          'container',
          'monitor',
          'globe',
          'flask-conical',
          'arrow-right',
          'menu',
          'mail',
          'copy',
          'sun-moon',
          'braces',
          'folder-tree',
          'refresh-cw',
        ],
      },
    }),
  ],
});
