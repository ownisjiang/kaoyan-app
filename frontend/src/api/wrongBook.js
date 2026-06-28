// ============================================================
// 错题本 API —— 对应后端 /wrong-book 模块
// GET    /wrong-book                 错题列表（按考点分组+分页）
// GET    /wrong-book/stats           错题统计（各考点错题数）
// POST   /wrong-book/{questionId}/review    标记已复习
// POST   /wrong-book/batch-review           批量标记复习
// POST   /wrong-book/generate-paper         生成错题专项卷
// DELETE /wrong-book/{questionId}           移出错题本（已掌握）
// ============================================================

import { http } from './request.js'

/**
 * 错题列表（按考点分组+分页）
 * @param {object} params - { subjectId, knowledgePointId, status, cursor, pageSize }
 */
export function getWrongQuestions(params) {
  return http.getWithMeta('/wrong-book', params)
}

/** 错题统计（各考点错题数） */
export function getWrongBookStats() {
  return http.get('/wrong-book/stats')
}

/** 标记已复习 */
export function markReviewed(questionId) {
  return http.post(`/wrong-book/${questionId}/review`)
}

/** 批量标记复习 */
export function batchReview(questionIds) {
  return http.post('/wrong-book/batch-review', { questionIds })
}

/**
 * 生成错题专项卷
 * @param {object} params - { subjectId, knowledgePointId, count }
 */
export function generatePaper(params) {
  return http.post('/wrong-book/generate-paper', params)
}

/** 移出错题本（已掌握） */
export function removeFromWrongBook(questionId) {
  return http.delete(`/wrong-book/${questionId}`)
}

/** 按科目→题型分组获取错题 */
export function getGroupedWrongBook() {
  return http.get('/wrong-book/grouped')
}
