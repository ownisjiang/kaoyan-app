// ============================================================
// 刷题 API —— 对应后端 /practice 模块
// POST   /practice/sessions              创建刷题会话
// GET    /practice/sessions              我的会话列表
// GET    /practice/sessions/{id}         会话详情（含答题进度）
// POST   /practice/sessions/{id}/answers    提交单题答案
// POST   /practice/sessions/{id}/answers/batch  批量提交
// PUT    /practice/sessions/{id}/pause       暂停
// PUT    /practice/sessions/{id}/resume      恢复
// POST   /practice/sessions/{id}/complete    交卷
// DELETE /practice/sessions/{id}             放弃会话
// ============================================================

import { http } from './request.js'

/**
 * 创建刷题会话
 * @param {object} params - { directionId, subjectId, mode, totalQuestions, config }
 * mode: quick_quiz | fill_blank | comprehensive | mock_exam
 */
export function createSession(params) {
  return http.post('/practice/sessions', params)
}

/** 我的会话列表 */
export function getSessions(params) {
  return http.getWithMeta('/practice/sessions', params)
}

/** 会话详情（含答题进度） */
export function getSessionDetail(id) {
  return http.get(`/practice/sessions/${id}`)
}

/**
 * 提交单题答案
 * @param {number} sessionId - 会话ID
 * @param {object} params - { questionId, userAnswer, timeSpent }
 */
export function submitAnswer(sessionId, params) {
  return http.post(`/practice/sessions/${sessionId}/answers`, params)
}

/**
 * 批量提交答案
 * @param {number} sessionId
 * @param {object} params - { answers: [{ questionId, userAnswer, timeSpent }] }
 */
export function submitAnswersBatch(sessionId, params) {
  return http.post(`/practice/sessions/${sessionId}/answers/batch`, params)
}

/** 暂停会话 */
export function pauseSession(id) {
  return http.put(`/practice/sessions/${id}/pause`)
}

/** 恢复会话 */
export function resumeSession(id) {
  return http.put(`/practice/sessions/${id}/resume`)
}

/** 交卷 */
export function completeSession(id) {
  return http.post(`/practice/sessions/${id}/complete`)
}

/** 放弃会话 */
export function abandonSession(id) {
  return http.delete(`/practice/sessions/${id}`)
}

/**
 * 获取下一批题目
 * @param {number} sessionId
 * @param {number} offset - 偏移量
 */
export function getNextBatch(sessionId, offset) {
  return http.get(`/practice/sessions/${sessionId}/next-batch`, { offset })
}

/**
 * 获取用户在各科目下的掌握度
 * 返回 [{subjectId, total, correct, mastery}]
 */
export function getSubjectMastery() {
  return http.get('/practice/mastery')
}
