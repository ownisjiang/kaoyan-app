// ============================================================
// HTTP 请求客户端 —— 统一封装 fetch，带 JWT 拦截 / 自动刷新 / 错误处理
// 对应后端统一响应格式: { code, message, data, meta }
// ============================================================

import { getAccessToken, getRefreshToken, setTokens, clearAuth, getLocalUser, setLocalUser } from '../utils/auth.js'

// 后端 API 基础路径（Vite 代理 /api → http://localhost:8080/api/v1）
const BASE_URL = '/api/v1'

// Token 刷新状态（防止并发刷新）
let isRefreshing = false
let refreshPromise = null

/**
 * 刷新 Access Token
 * 使用 Refresh Token 调用 /auth/refresh，成功后更新本地存储
 * @returns {Promise<string|null>} 新的 Access Token 或 null
 */
async function doRefreshToken() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return null

  try {
    const res = await fetch(`${BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    })
    if (!res.ok) return null

    const json = await res.json()
    if (json.code !== 0) return null

    const { accessToken, refreshToken: newRefreshToken } = json.data
    setTokens(accessToken, newRefreshToken)
    return accessToken
  } catch {
    return null
  }
}

/**
 * 确保同一时间只有一个刷新请求
 */
async function refreshAccessToken() {
  if (isRefreshing) return refreshPromise

  isRefreshing = true
  refreshPromise = doRefreshToken().finally(() => {
    isRefreshing = false
  })
  return refreshPromise
}

/**
 * 核心 fetch 封装
 * @param {string} path - API 路径（不含 /api/v1 前缀）
 * @param {object} options - fetch options
 * @param {boolean} options._retry - 内部标记，是否为重试请求（避免无限循环）
 * @returns {Promise<any>} 后端 data 字段内容
 */
export async function request(path, options = {}) {
  const { method = 'GET', body, headers = {}, _retry = false, isFormData = false } = options

  // 构建 Headers
  const reqHeaders = { ...headers }
  if (!isFormData && body !== undefined) {
    reqHeaders['Content-Type'] = 'application/json'
  }

  // 注入 JWT
  const token = getAccessToken()
  if (token) {
    reqHeaders['Authorization'] = `Bearer ${token}`
  }

  const config = {
    method,
    headers: reqHeaders,
    credentials: 'same-origin'
  }

  if (body !== undefined) {
    config.body = isFormData ? body : JSON.stringify(body)
  }

  let res
  try {
    res = await fetch(`${BASE_URL}${path}`, config)
  } catch (err) {
    throw new ApiError(50001, '网络连接失败，请检查网络', err)
  }

  // 401 → 尝试刷新 Token 后重试
  if (res.status === 401 && !_retry) {
    const newToken = await refreshAccessToken()
    if (newToken) {
      return request(path, { ...options, _retry: true })
    }
    // 刷新失败 → 清除登录状态，跳转登录页
    clearAuth()
    redirectToLogin()
    throw new ApiError(10002, '登录已过期，请重新登录')
  }

  // 解析响应体
  let json
  try {
    json = await res.json()
  } catch {
    throw new ApiError(50001, '服务器响应格式错误')
  }

  // 后端统一格式: { code, message, data, meta }
  if (json.code === 0) {
    return json.data
  }

  // 业务错误
  throw new ApiError(json.code, json.message || '请求失败')
}

/**
 * 带分页元数据的请求（返回 { data, meta }）
 */
export async function requestWithMeta(path, options = {}) {
  const { method = 'GET', body, headers = {}, _retry = false } = options

  const reqHeaders = { ...headers }
  if (body !== undefined) {
    reqHeaders['Content-Type'] = 'application/json'
  }

  const token = getAccessToken()
  if (token) {
    reqHeaders['Authorization'] = `Bearer ${token}`
  }

  const config = {
    method,
    headers: reqHeaders,
    credentials: 'same-origin'
  }

  if (body !== undefined) {
    config.body = JSON.stringify(body)
  }

  let res
  try {
    res = await fetch(`${BASE_URL}${path}`, config)
  } catch (err) {
    throw new ApiError(50001, '网络连接失败，请检查网络', err)
  }

  if (res.status === 401 && !_retry) {
    const newToken = await refreshAccessToken()
    if (newToken) {
      return requestWithMeta(path, { ...options, _retry: true })
    }
    clearAuth()
    redirectToLogin()
    throw new ApiError(10002, '登录已过期，请重新登录')
  }

  let json
  try {
    json = await res.json()
  } catch {
    throw new ApiError(50001, '服务器响应格式错误')
  }

  if (json.code === 0) {
    return { data: json.data, meta: json.meta }
  }

  throw new ApiError(json.code, json.message || '请求失败')
}

/**
 * 统一错误类
 */
export class ApiError extends Error {
  constructor(code, message, cause) {
    super(message)
    this.code = code
    this.cause = cause
  }
}

/** 重定向到登录页 */
function redirectToLogin() {
  if (typeof window !== 'undefined' && !window.location.hash.includes('/login')) {
    window.location.hash = '#/login'
  }
}

// ============================================================
// 便捷方法
// ============================================================

export const http = {
  get: (path, params) => {
    const query = params ? '?' + new URLSearchParams(params).toString() : ''
    return request(path + query)
  },
  getWithMeta: (path, params) => {
    const query = params ? '?' + new URLSearchParams(params).toString() : ''
    return requestWithMeta(path + query)
  },
  post: (path, body, options) => request(path, { ...options, method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  patch: (path, body) => request(path, { method: 'PATCH', body }),
  delete: (path) => request(path, { method: 'DELETE' }),
  upload: (path, formData) => request(path, { method: 'POST', body: formData, isFormData: true })
}
