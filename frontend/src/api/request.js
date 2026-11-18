import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 统一 axios 实例（真实后端）
 *
 * 后端统一返回：{ code, message, data }
 *  - code === 200：拦截器返回 body.data（业务数据），调用方直接拿 data
 *  - code === 401：清除 token 并跳转登录页
 *  - 其他 code：ElMessage 提示 message 并 reject
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// ---------------- 请求拦截器：自动带上 token ----------------
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

/**
 * 401 统一处理：清 token + 跳登录页。
 * 用动态 import 避免 request.js ↔ router/index.js 的循环依赖。
 */
function redirectToLogin() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')

  if (window.location.pathname === '/login') return

  import('@/router')
    .then(({ default: router }) => {
      if (router.currentRoute.value.path !== '/login') {
        router.replace('/login')
      }
    })
    .catch(() => {
      window.location.href = '/login'
    })
}

// ---------------- 响应拦截器：统一处理 { code, message, data } ----------------
request.interceptors.response.use(
  (response) => {
    const body = response.data

    // 非标准响应（文件流、纯文本等）直接放行
    if (!body || typeof body !== 'object' || typeof body.code === 'undefined') {
      return body
    }

    // 业务成功：解包出 data，调用方直接使用业务数据
    // 例如：const data = await request.post('/auth/login', ...) → data.token
    // 个别接口（如生成工资）需要读取后端 message，调用时传 { raw: true }
    if (body.code === 200) {
      return response.config?.raw ? body : body.data
    }

    // 未登录 / token 过期
    if (body.code === 401) {
      ElMessage.error(body.message || '登录已过期，请重新登录')
      redirectToLogin()
      return Promise.reject(new Error(body.message || '未登录'))
    }

    // 其他业务错误
    const message = body.message || `请求失败（code: ${body.code}）`
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  (error) => {
    // HTTP 层 401（后续接入 JWT / Spring Security 时）
    if (error?.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      redirectToLogin()
      return Promise.reject(error)
    }

    let message = error?.response?.data?.message
    if (!message) {
      message =
        error?.code === 'ECONNABORTED'
          ? '请求超时，请稍后重试'
          : '无法连接后端服务，请确认 http://localhost:8080 已启动'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export default request
