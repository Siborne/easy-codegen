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
          'rocket',
          'database',
          'table',
          'settings',
          'file-code',
          'package',
          'palette',
          'save',
          'plug',
          'wand',
          'box',
          'globe',
          'monitor',
          'flask-conical',
          'arrow-right',
          'github',
          'menu',
          'check',
          'terminal',
          'sparkles',
          'eye',
          'mail',
        ],
      },
    }),
  ],
});
