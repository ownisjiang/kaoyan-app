// ============================================================
// 用户 API —— 对应后端 /users 模块
// GET    /users/me                当前用户信息
// PUT    /users/me                更新个人信息
// PUT    /users/me/password       修改密码
// PUT    /users/me/target         设置目标方向/院校
// GET    /users/me/stats          学习统计概览
// GET    /users/me/stats/daily    每日统计（含热力图数据）
// GET    /users/me/achievements   成就列表+进度
// GET    /users/me/timeline       学习动态时间线
// DELETE /users/me                注销账户
// ============================================================

import { http } from './request.js'

/** 获取当前用户信息 */
export function getMyProfile() {
  return http.get('/users/me')
}

/** 更新个人信息 */
export function updateMyProfile(data) {
  return http.put('/users/me', data)
}

/** 修改密码 */
export function changePassword(oldPassword, newPassword) {
  return http.put('/users/me/password', { oldPassword, newPassword })
}

/** 设置目标方向/院校 */
export function setTarget(targetDirectionId, targetSchool, examYear) {
  return http.put('/users/me/target', { targetDirectionId, targetSchool, examYear })
}

/**
 * 学习统计概览
 * 返回: { todayStats, overallStats, mastery }
 */
export function getMyStats() {
  return http.get('/users/me/stats')
}

/**
 * 每日统计（含热力图数据）
 * @param {object} params - { startDate, endDate }
 */
export function getMyDailyStats(params) {
  return http.get('/users/me/stats/daily', params)
}

/** 成就列表+进度 → 转发到 yantiku-practice 服务 */
export function getMyAchievements() {
  return http.get('/achievements')
}

/** 学习动态时间线 */
export function getMyTimeline() {
  return http.get('/users/me/timeline')
}

/** 注销账户 */
export function deleteMyAccount() {
  return http.delete('/users/me')
}
