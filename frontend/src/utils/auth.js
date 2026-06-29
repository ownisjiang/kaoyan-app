// ============================================================
// Token 管理 —— Access Token / Refresh Token 存取与轮换
// ============================================================

const ACCESS_TOKEN_KEY = 'yantiku_access_token'
const REFRESH_TOKEN_KEY = 'yantiku_refresh_token'
const USER_KEY = 'yantiku_user'

/** 获取 Access Token */
export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY) || ''
}

/** 获取 Refresh Token */
export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || ''
}

/** 存储 Token 对 */
export function setTokens(accessToken, refreshToken) {
  if (accessToken) localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  if (refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

/** 获取已登录用户基本信息（缓存在本地） */
export function getLocalUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

/** 缓存用户信息 */
export function setLocalUser(user) {
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  }
}

/** 清除所有认证信息（登出） */
export function clearAuth() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

/** 是否已登录 */
export function isLoggedIn() {
  return !!getAccessToken()
}

/** 是否管理员 */
export function isAdmin() {
  const user = getLocalUser()
  return user && user.role === 'admin'
}
