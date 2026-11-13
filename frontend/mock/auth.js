/**
 * Mock 接口：登录
 *
 * 由 vite-plugin-mock 在 `vite` 开发服务器（command === 'serve'）中拦截。
 * 生产构建（vite build）不会打包这些 mock 代码。
 *
 * 约定响应体：{ code, data, message }
 */
export default [
  {
    url: '/api/auth/login',
    method: 'post',
    response: () => ({
      code: 200,
      data: { token: 'mock-jwt-token' },
      message: 'ok',
    }),
  },
]
