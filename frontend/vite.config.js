import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'
import { viteMockServe } from 'vite-plugin-mock'

// https://vite.dev/config/
export default defineConfig(({ command }) => {
  // `vite`（npm run dev）时 command === 'serve'；`vite build` 时 command === 'build'
  const isDevServer = command === 'serve'

  return {
    plugins: [
      vue(),
      viteMockServe({
        mockPath: 'mock', // mock 目录，相对 frontend 根目录，与 mock/auth.js 对应
        enable: isDevServer, // 仅开发服务器启用；build 时关闭
        logger: true, // 命中时终端打印 [vite:mock] request invoke /api/...
      }),
    ],
    server: {
      host: '127.0.0.1',
      port: 5173,

      // ⚠️ 开发期不要为 '/api' 配置 proxy。
      // proxy 会在 mock 插件之前把请求转发给后端，导致 Mock 永远不命中。
      // 等真实后端联调时再打开下面这段（并同时关闭上面的 Mock）：
      //
      // proxy: {
      //   '/api': {
      //     target: 'http://localhost:8080',
      //     changeOrigin: true,
      //   },
      // },
    },
  }
})
