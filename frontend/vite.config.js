import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'
import { viteMockServe } from 'vite-plugin-mock'

// https://vite.dev/config/
export default defineConfig(({ command }) => ({
  plugins: [
    vue(),
    // 仅在开发服务器启用 Mock：vite build 时自动跳过
    viteMockServe({
      mockPath: 'mock',
      enable: command === 'serve',
      logger: true,
    }),
  ],
  server: {
    host: '127.0.0.1',
    port: 5173,
  },
}))
