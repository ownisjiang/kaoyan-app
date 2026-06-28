// ============================================================
// 诊断报告 API —— 对应后端 /diagnostic 模块
// GET    /diagnostic/reports        报告列表
// GET    /diagnostic/reports/{id}   报告详情
// POST   /diagnostic/generate       生成报告（从会话）
// ============================================================

import { http } from './request.js'

/** 报告列表 */
export function getReports(params) {
  return http.getWithMeta('/diagnostic/reports', params)
}

/**
 * 报告详情
 * 返回: { overallScore, abilityRating, radarData, weakPoints, strengths, suggestions, timeAnalysis }
 */
export function getReportDetail(id) {
  return http.get(`/diagnostic/reports/${id}`)
}

/**
 * 生成报告（从会话）
 * @param {object} params - { sessionId, reportType }
 */
export function generateReport(params) {
  return http.post('/diagnostic/generate', params)
}
