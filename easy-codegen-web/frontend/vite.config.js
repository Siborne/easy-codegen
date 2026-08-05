import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

// 前端构建配置：
// - dev 模式（npm run dev，默认 6000 端口）：/api 代理到后端 9000，无需跨域配置
// - build 模式（npm run build）：产物输出到 Spring Boot 的 static 目录，由后端同源托管
export default defineConfig({
  plugins: [react()],
  server: {
    port: 6000,
    host: '127.0.0.1',
    proxy: {
      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: resolve(__dirname, '../src/main/resources/static'),
    emptyOutDir: true
  }
});
