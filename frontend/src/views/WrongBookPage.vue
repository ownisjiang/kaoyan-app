<template>
  <div v-if="loading" class="page-loading">加载中...</div>
  <div v-else class="page">
    <SideNav active-nav="wrongBook" />
    <main class="main">
      <header class="header">
        <h1 class="title">错题本</h1>
        <span class="summary" v-if="stats">{{ stats.totalWrong }} 道错题 · {{ stats.needReview }} 待复习</span>
      </header>

      <!-- 空状态 -->
      <div v-if="!groups.length" class="empty">
        <span class="empty-icon">📖</span>
        <p class="empty-text">暂无错题</p>
        <p class="empty-hint">做完题目后，答错的题会自动加入错题本</p>
        <button class="btn primary" @click="router.push('/')">去刷题</button>
      </div>

      <!-- 按科目+题型分组 -->
      <div v-else class="groups">
        <div v-for="(group, gi) in groups" :key="'g'+gi" class="subject-group">
          <!-- 科目标题 -->
          <div class="subject-header" @click="toggleGroup(gi)">
            <span class="subject-icon">📚</span>
            <span class="subject-name">{{ group.subject }}</span>
            <span class="subject-count">{{ group.total }} 题</span>
            <span class="subject-arrow">{{ openGroups[gi] ? '▾' : '▸' }}</span>
          </div>

          <!-- 题型分组 -->
          <div v-if="openGroups[gi]" class="type-groups">
            <div v-for="(tg, ti) in group.types" :key="'t'+ti" class="type-group">
              <div class="type-header" @click="toggleType(gi, ti)">
                <span class="type-label" :class="'type--'+tg.type">{{ tg.label }}</span>
                <span class="type-count">{{ tg.count }} 题</span>
                <span class="type-arrow">{{ openTypes[gi+'-'+ti] ? '▾' : '▸' }}</span>
              </div>

              <!-- 题目列表 -->
              <div v-if="openTypes[gi+'-'+ti]" class="q-list">
                <div v-for="q in tg.questions" :key="q.id" class="q-item" :class="{ mastered: q.status==='mastered' }">
                  <div class="q-top">
                    <span class="q-diff" :class="'diff--'+q.difficulty">难度 {{ q.difficulty }}</span>
                    <span class="q-wrong">错 {{ q.wrongCount }} 次</span>
                  </div>
                  <p class="q-text">
                    <span v-if="q.questionType === 2" class="fill-content" v-html="renderFillBlank(q.content || q.contentPreview)"></span>
                    <span v-else>{{ q.content || q.contentPreview || '(题目内容加载中...)' }}</span>
                  </p>
                  <!-- 选择题选项 -->
                  <div v-if="q.questionType === 1 && q.options" class="q-options">
                    <span v-for="(opt, oi) in parseOptions(q.options)" :key="oi" class="q-opt">{{ opt }}</span>
                  </div>
                  <!-- 答案 -->
                  <div v-if="q.answer" class="q-answer" @click="toggleAnswer(q.id)">
                    <span class="q-answer-label">{{ expandedAnswers[q.id] ? '🔽 答案' : '▶ 答案' }}</span>
                    <span v-if="expandedAnswers[q.id]" class="q-answer-text">{{ q.answer }}</span>
                  </div>
                  <div class="q-actions">
                    <button class="btn sm" @click="doReview(q.id)">{{ q.lastReviewedAt ? '已复习' : '标记复习' }}</button>
                    <button class="btn sm danger" @click="doRemove(q.questionId, gi, ti, q.id)">移除</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import SideNav from '../components/SideNav.vue'
import { getGroupedWrongBook, getWrongBookStats, markReviewed, removeFromWrongBook } from '../api/wrongBook.js'

const router = useRouter()
const loading = ref(true)
const stats = ref(null)
const groups = ref([])
const openGroups = reactive({})
const openTypes = reactive({})

const expandedAnswers = reactive({})

function toggleAnswer(id) {
  expandedAnswers[id] = !expandedAnswers[id]
}

onMounted(async () => {
  try {
    const [statResult, groupedResult] = await Promise.all([
      getWrongBookStats(),
      getGroupedWrongBook()
    ])
    stats.value = statResult
    groups.value = groupedResult || []
    // 默认展开第一个科目
    if (groups.value.length) openGroups[0] = true
  } catch (e) {
    groups.value = []
    stats.value = { totalWrong: 0, needReview: 0 }
  } finally {
    loading.value = false
  }
})

function toggleGroup(gi) {
  openGroups[gi] = !openGroups[gi]
}

function toggleType(gi, ti) {
  const key = gi + '-' + ti
  openTypes[key] = !openTypes[key]
}

function parseOptions(raw) {
  if (!raw) return []
  try {
    const arr = typeof raw === 'string' ? JSON.parse(raw) : raw
    return Array.isArray(arr) ? arr : Object.values(arr)
  } catch { return [] }
}

function renderFillBlank(text) {
  if (!text) return ''
  return text.replace(/_{2,}/g, '<span class="blank-mark">___</span>')
}

async function doReview(wrongBookId) {
  try {
    await markReviewed(wrongBookId)
  } catch (e) { alert('操作失败') }
}

async function doRemove(questionId, gi, ti, wbId) {
  try {
    await removeFromWrongBook(questionId)
    // remove from local data
    const tg = groups.value[gi]?.types?.[ti]
    if (tg) {
      tg.questions = tg.questions.filter(q => q.id !== wbId)
      tg.count = tg.questions.length
      groups.value[gi].total = groups.value[gi].types.reduce((s, t) => s + t.count, 0)
      if (!groups.value[gi].total) groups.value.splice(gi, 1)
    }
    if (stats.value) stats.value.totalWrong = Math.max(0, stats.value.totalWrong - 1)
  } catch (e) { alert('操作失败') }
}
</script>

<style scoped>
.page { display: flex; width: 100%; height: 100vh; background: #f5f7fa; }
.main { flex: 1; overflow-y: auto; padding: 32px 40px; }
.header { display: flex; align-items: baseline; gap: 16px; margin-bottom: 24px; }
.title { font-size: 22px; font-weight: 700; color: #1e293b; margin: 0; }
.summary { font-size: 14px; color: #64748b; }

.empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px 0; }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-text { font-size: 18px; color: #64748b; margin-bottom: 8px; }
.empty-hint { font-size: 14px; color: #94a3b8; margin-bottom: 24px; }

/* 科目分组 */
.subject-group { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; margin-bottom: 12px; overflow: hidden; }
.subject-header { display: flex; align-items: center; gap: 10px; padding: 14px 18px; cursor: pointer; background: #f8fafc; }
.subject-header:hover { background: #f1f5f9; }
.subject-icon { font-size: 16px; }
.subject-name { font-size: 16px; font-weight: 600; color: #1e293b; flex: 1; }
.subject-count { font-size: 13px; color: #6366f1; font-weight: 500; }
.subject-arrow { font-size: 14px; color: #94a3b8; }

/* 题型分组 */
.type-group { border-top: 1px solid #f1f5f9; }
.type-header { display: flex; align-items: center; gap: 10px; padding: 10px 18px 10px 32px; cursor: pointer; }
.type-header:hover { background: #fafbfd; }
.type-label { font-size: 13px; font-weight: 500; padding: 2px 10px; border-radius: 10px; }
.type--1 { background: #e0f2fe; color: #0369a1; }
.type--2 { background: #fef9c3; color: #854d0e; }
.type--3 { background: #ede9fe; color: #5b21b6; }
.type-count { font-size: 12px; color: #94a3b8; flex: 1; }
.type-arrow { font-size: 12px; color: #94a3b8; }

/* 题目列表 */
.q-list { padding: 0 18px 12px 32px; display: flex; flex-direction: column; gap: 8px; }
.q-item { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px 14px; }
.q-item.mastered { opacity: 0.5; }
.q-top { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.q-diff { font-size: 11px; padding: 1px 8px; border-radius: 8px; }
.diff--1 { background: #e0f2fe; color: #0369a1; }
.diff--2 { background: #dcfce7; color: #166534; }
.diff--3 { background: #fef9c3; color: #854d0e; }
.diff--4 { background: #fed7aa; color: #9a3412; }
.diff--5 { background: #fecaca; color: #991b1b; }
.q-wrong { margin-left: auto; font-size: 12px; color: #ef4444; font-weight: 500; }
.q-text { font-size: 13px; line-height: 1.6; color: #334155; margin: 0 0 8px 0; }
.q-options { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; }
.q-opt { font-size: 12px; padding: 2px 10px; background: #e2e8f0; border-radius: 6px; color: #475569; }
.q-answer { margin-bottom: 8px; font-size: 12px; cursor: pointer; color: #6366f1; }
.q-answer-label { font-weight: 500; }
.q-answer-text { display: block; margin-top: 4px; padding: 6px 10px; background: #f0fdf4; border-radius: 6px; color: #166534; }
.fill-content { line-height: 2; }
:deep(.blank-mark) { display: inline-block; min-width: 40px; padding: 0 6px; border-bottom: 2px solid #6366f1; color: #6366f1; text-align: center; margin: 0 2px; }
.q-actions { display: flex; gap: 6px; }
.btn { padding: 8px 20px; border: 1px solid #e2e8f0; border-radius: 8px; background: #fff; color: #475569; font-size: 14px; cursor: pointer; }
.btn:hover { background: #f8fafc; }
.btn.primary { background: #6366f1; color: #fff; border-color: #6366f1; }
.btn.sm { padding: 3px 10px; font-size: 11px; }
.btn.danger { color: #ef4444; border-color: #fecaca; }
.btn.danger:hover { background: #fef2f2; }
.page-loading { display: flex; align-items: center; justify-content: center; height: 100vh; color: #64748b; font-size: 16px; }
</style>
