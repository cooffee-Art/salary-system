/**
 * Mock 接口（仅开发服务器生效，由 vite-plugin-mock 拦截）
 *
 * 约定响应体：{ code, data, message }
 * 生产构建（vite build）不会打包这些 mock 代码。
 */
export default [
  {
    // 诊断用：浏览器直接访问 http://127.0.0.1:5173/api/health 能看到 JSON，
    // 就说明 mock 插件已生效；看不到则是 dev server 没起或插件没加载。
    url: '/api/health',
    method: 'get',
    response: () => ({
      code: 200,
      data: { mock: true, time: new Date().toISOString() },
      message: 'ok',
    }),
  },
  {
    // 注意：必须与前端请求的最终地址完全一致。
    // request 的 baseURL 为 '/api'，调用 request.post('/auth/login') → /api/auth/login
    url: '/api/auth/login',
    method: 'post',
    response: () => ({
      code: 200,
      data: { token: 'mock-jwt-token' },
      message: 'ok',
    }),
  },
]
