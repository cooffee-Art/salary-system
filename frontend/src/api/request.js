import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 统一的 axios 实例。
 * 后端约定响应体：{ code, data, message }
 * 因此 baseURL 写 '/api'，业务调用只写 '/auth/login' 这类相对路径，
 * 最终请求地址为 /api/auth/login。
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

// ---------------- 响应拦截器：统一处理 { code, data, message } ----------------
request.interceptors.response.use(
  (response) => {
    const body = response.data

    // 非标准响应（例如文件流、纯文本）直接放行，避免误判
    if (!body || typeof body !== 'object' || typeof body.code === 'undefined') {
      return body
    }

    // 业务成功：按约定返回 response.data（即完整的 { code, data, message }）
    // 调用方取值方式：const res = await request.post(...); res.data.token
    if (body.code === 200) {
      return body
    }

    // 业务失败：统一弹错并中断 Promise 链
    const message = body.message || `请求失败（code: ${body.code}）`
    ElMessage.error(message)
    if (body.code === 401) {
      localStorage.removeItem('token')
    }
    return Promise.reject(new Error(message))
  },
  (error) => {
    // HTTP 层错误（4xx/5xx、超时、断网）
    const message =
      error?.response?.data?.message || error?.message || '网络异常，请稍后重试'
    ElMessage.error(message)
    if (error?.response?.status === 401) {
      localStorage.removeItem('token')
    }
    return Promise.reject(error)
  },
)

export default request
