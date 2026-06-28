// ============================================================
// 题目与考试体系 API —— 对应后端 /directions /subjects /knowledge-points /questions
// ============================================================

import { http } from './request.js'

// ─── 考试方向 ───

/** 全部考试方向 */
export function getDirections() {
  return http.get('/directions')
}

/** 方向详情（含科目列表） */
export function getDirectionDetail(id) {
  return http.get(`/directions/${id}`)
}

// ─── 科目 ───

/** 科目列表 */
export function getSubjects(directionId) {
  const params = directionId ? { directionId } : {}
  return http.get('/subjects', params)
}

/** 科目详情（含考点树） */
export function getSubjectDetail(id) {
  return http.get(`/subjects/${id}`)
}

// ─── 考点 ───

/** 考点列表 */
export function getKnowledgePoints(subjectId) {
  return http.get('/knowledge-points', { subjectId })
}

/** 考点树 */
export function getKnowledgePointTree(subjectId) {
  return http.get('/knowledge-points/tree', { subjectId })
}

// ─── 题目 ───

/**
 * 题目列表（分页+多条件）
 * @param {object} params - { directionId, subjectId, knowledgePointId, questionType, difficulty, examYear, page, pageSize }
 */
export function getQuestions(params) {
  return http.getWithMeta('/questions', params)
}

/** 题目详情 */
export function getQuestionDetail(id) {
  return http.get(`/questions/${id}`)
}

/**
 * 随机抽题
 * @param {object} params - { directionId, subjectId, questionType, difficulty, count }
 */
export function getRandomQuestions(params) {
  return http.get('/questions/random', params)
}

/** 全文搜索（ES） */
export function searchQuestions(params) {
  return http.getWithMeta('/questions/search', params)
}

/** 热门/高频题 */
export function getHotQuestions(params) {
  return http.get('/questions/hot', params)
}

/** 相似题目推荐 */
export function getSimilarQuestions(id) {
  return http.get(`/questions/${id}/similar`)
}

/** 题目报错 */
export function reportQuestion(id, data) {
  return http.post(`/questions/${id}/report`, data)
}
