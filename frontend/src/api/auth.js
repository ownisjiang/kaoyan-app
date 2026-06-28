// ============================================================
// 认证 API —— 对应后端 /auth 模块
// POST /auth/register          注册（手机号+SMS验证码+密码）
// POST /auth/login             登录（手机号+密码/短信验证码）
// POST /auth/refresh           刷新 Access Token
// POST /auth/logout            登出
// POST /auth/send-sms-code     发送短信验证码
// POST /auth/reset-password    重置密码
// ============================================================

import { http } from './request.js'
import { setTokens, setLocalUser, clearAuth } from '../utils/auth.js'

/**
 * 发送短信验证码
 * @param {string} phone - 手机号
 * @param {string} type - 验证码类型: register | login | reset
 */
export function sendSmsCode(phone, type = 'register') {
  return http.post('/auth/send-sms-code', { phone, type })
}

/**
 * 注册
 * @param {object} params - { phone, smsCode, password, nickname }
 */
export async function register(params) {
  const data = await http.post('/auth/register', params)
  if (data?.accessToken) {
    setTokens(data.accessToken, data.refreshToken)
    if (data.user) setLocalUser(data.user)
  }
  return data
}

/**
 * 登录
 * @param {object} params - { phone, password, smsCode }
 */
export async function login(params) {
  const data = await http.post('/auth/login', params)
  if (data?.accessToken) {
    setTokens(data.accessToken, data.refreshToken)
    if (data.user) setLocalUser(data.user)
  }
  return data
}

/**
 * 登出
 */
export async function logout() {
  try {
    await http.post('/auth/logout')
  } finally {
    clearAuth()
  }
}

/**
 * 重置密码
 * @param {object} params - { phone, smsCode, newPassword }
 */
export function resetPassword(params) {
  return http.post('/auth/reset-password', params)
}
