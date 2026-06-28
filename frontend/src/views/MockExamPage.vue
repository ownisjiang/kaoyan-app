<template>
  <div v-if="loading" class="page" style="display:flex;align-items:center;justify-content:center">
    <span style="color:var(--color-text-secondary);font-size:var(--font-size-lg)">加载中...</span>
  </div>
  <div v-else-if="error" class="page" style="display:flex;align-items:center;justify-content:center;flex-direction:column;gap:16px">
    <span style="color:var(--color-error);font-size:var(--font-size-lg)">{{ error }}</span>
    <button class="btn btn--outline" @click="router.push(subjectId ? `/subject/${subjectId}` : '/')">返回题库</button>
  </div>

  <!-- 提交后：概览模式 -->
  <div v-else-if="allSubmitted" class="page">
    <aside class="quiz-sidebar">
      <div class="quiz-sidebar__back" @click="router.push(subjectId ? `/subject/${subjectId}` : '/')">
        <span>← 返回题库</span>
      </div>
      <div class="quiz-sidebar__progress">
        <span class="quiz-sidebar__progress-label">作答结果</span>
        <span class="quiz-sidebar__progress-value" style="color:var(--color-success)">{{ batchCorrect }}/{{ qCount }}</span>
        <div class="progress-bar progress-bar--small">
          <div class="progress-bar__fill" :style="{ width: (batchCorrect/qCount*100)+'%', background: 'var(--color-success)' }"></div>
        </div>
      </div>
      <div class="quiz-sidebar__list">
        <div v-for="(item, i) in overviewList" :key="i" class="quiz-sidebar__item" :class="`quiz-sidebar__item--${item.status}`" @click="cur=i; overviewDetail=true">
          {{ i+1 }} {{ item.status === 'done' ? '✓' : item.status === 'wrong' ? '✗' : '⏭' }}
        </div>
      </div>
    </aside>

    <main class="main">
      <header class="main__toolbar">
        <h1 class="main__title">模考 · 答题结果</h1>
        <div class="timer"><span class="timer__icon">⏱</span><span class="timer__value">{{ clock }}</span></div>
      </header>

      <div v-if="!overviewDetail" class="main__content">
        <div class="overview-grid">
          <div v-for="(q, i) in questions" :key="'ov-'+q.id" class="ov-card" :class="{ 'ov-card--ok': rs[i]?.isCorrect, 'ov-card--ng': rs[i] && !rs[i].isCorrect }" @click="cur=i; overviewDetail=true">
            <span class="ov-num">{{ i+1 }}</span>
            <span class="ov-stem">{{ typeBadge(q) }} {{ (q.content||'').slice(0,36) }}{{ (q.content||'').length>36?'...':'' }}</span>
            <span class="ov-res">{{ rs[i] ? (rs[i].isCorrect ? '✅' : '❌') : '⏭' }}</span>
          </div>
        </div>
        <div class="action-row">
          <button class="btn btn--outline" @click="doRedo" v-if="qCount>0">重新作答</button>
          <button class="btn btn--primary" @click="doFinish">{{ hasMore ? '完成 · 下一组' : '完成' }}</button>
        </div>
      </div>

      <div v-else class="main__content">
        <div class="q-detail">
          <div class="tags-row">
            <span class="tag" :class="typeTagClass(currentQ)">{{ typeLabel(currentQ) }}</span>
            <span class="tag tag--gray">难度 {{ currentQ?.difficulty ?? 3 }}/5</span>
          </div>
          <p class="question-text">{{ cur+1 }}. {{ currentQ?.content }}</p>
          <div v-if="isChoice(currentQ)" class="options-readonly">
            <div v-for="opt in toOpts(currentQ?.options)" :key="opt.key" class="opt-line" :class="{ 'opt--pick': ans[cur]===opt.key, 'opt--correct': rs[cur]?.correctAnswer===opt.key }">
              {{ opt.key }}. {{ opt.text }}
            </div>
          </div>
          <div v-else class="answer-card">
            <div class="answer-card__hd">我的答案</div>
            <div class="answer-card__body">{{ ans[cur] || '(未作答)' }}</div>
          </div>
          <div class="result" v-if="rs[cur]" :class="rs[cur].isCorrect?'result--ok':'result--ng'">
            <div class="result-head">{{ rs[cur].isCorrect?'✅ 正确':'❌ 错误' }} · 得分 {{ rs[cur].score || 0 }}</div>
            <div v-if="rs[cur].correctAnswer" class="result-row">参考答案：{{ rs[cur].correctAnswer }}</div>
            <div v-if="rs[cur].analysis" class="result-row result-analysis">{{ rs[cur].analysis }}</div>
            <div v-if="rs[cur].aiFeedback" class="result-row result-ai">{{ rs[cur].aiFeedback }}</div>
          </div>
          <div class="action-row">
            <button class="btn btn--outline" @click="cur=Math.max(0,cur-1)" :disabled="cur===0">上一题</button>
            <span class="page-indicator">{{ cur+1 }}/{{ qCount }}</span>
            <button class="btn btn--outline" @click="cur=Math.min(qCount-1,cur+1)" :disabled="cur>=qCount-1">下一题</button>
            <button class="btn btn--outline" @click="overviewDetail=false">返回概览</button>
          </div>
        </div>
      </div>
    </main>
  </div>

  <!-- 答题模式 -->
  <div v-else class="page">
    <aside class="quiz-sidebar">
      <div class="quiz-sidebar__back" @click="router.push(subjectId ? `/subject/${subjectId}` : '/')">
        <span>← 返回题库</span>
      </div>
      <div class="quiz-sidebar__progress">
        <span class="quiz-sidebar__progress-label">答题进度</span>
        <span class="quiz-sidebar__progress-value">{{ answered }} / {{ qCount }}</span>
        <div class="progress-bar progress-bar--small">
          <div class="progress-bar__fill" :style="{ width: pct + '%' }"></div>
        </div>
      </div>
      <div class="quiz-sidebar__list">
        <div v-for="(item, i) in questionList" :key="i" class="quiz-sidebar__item" :class="`quiz-sidebar__item--${item.status}`" @click="cur=i">
          {{ i+1 }} {{ item.status === 'answered' ? '✓' : item.status === 'current' ? '▶' : '' }}
        </div>
      </div>
    </aside>

    <main class="main">
      <header class="main__toolbar">
        <h1 class="main__title">{{ pageTitle }}</h1>
        <div class="timer"><span class="timer__icon">⏱</span><span class="timer__value">{{ clock }}</span></div>
      </header>

      <div v-if="qCount === 0" class="main__content" style="align-items:center;justify-content:center">
        <span style="color:var(--color-text-secondary);font-size:var(--font-size-lg)">暂无题目，请选择其他科目</span>
        <button class="btn btn--primary" @click="router.push(subjectId ? `/subject/${subjectId}` : '/')" style="margin-top:16px">返回题库</button>
      </div>

      <div v-else class="main__content">
        <div class="tags-row">
          <span class="tag" :class="typeTagClass(currentQ)">{{ typeLabel(currentQ) }}</span>
          <span class="tag tag--gray">难度 {{ currentQ?.difficulty ?? 3 }}/5</span>
        </div>

        <div v-if="!isFillBlank(currentQ)" class="question-card">
          <p class="question-card__text">{{ cur+1 }}. {{ currentQ?.content }}</p>
        </div>

        <!-- 选择题 -->
        <div v-if="isChoice(currentQ)" class="options-list">
          <div v-for="opt in toOpts(currentQ?.options)" :key="opt.key" class="option" :class="{ 'option--selected': ans[cur]===opt.key }" @click="pick(opt.key)">
            <span class="option__key">{{ opt.key }}</span>
            <span class="option__text">{{ opt.text }}</span>
          </div>
        </div>

        <!-- 填空题（与 FillBlankPage 一致布局） -->
        <p v-else-if="isFillBlank(currentQ)" class="question-text">
          {{ cur+1 }}.
          <template v-for="(seg, si) in blankSegments" :key="'seg-'+cur+'-'+si">
            <span v-if="seg.text">{{ seg.text }}</span>
            <span v-if="seg.isBlank" class="blank-input-wrapper">
              <input
                v-model="blankAns[cur][seg.idx]"
                class="blank-input"
                :placeholder="'空'+(seg.idx+1)"
                @keydown.enter="onBlankEnter(seg.idx, blankSegments.filter(s=>s.isBlank).length)"
              />
            </span>
          </template>
        </p>

        <!-- 数学符号栏 -->
        <div v-if="isFillBlank(currentQ)" class="symbol-bar">
          <span class="symbol-bar__label">数符：</span>
          <span v-for="s in mathSymbols" :key="s" class="symbol-chip" @mousedown.prevent="insertSymbol(s)">{{ s }}</span>
        </div>

        <!-- 综合题 -->
        <div v-else class="answer-area">
          <div class="answer-area__toolbar">
            <span class="answer-area__label">作答区</span>
            <span class="word-count">{{ wordCount() }} 字</span>
          </div>
          <textarea v-model="ans[cur]" class="answer-textarea" placeholder="请在此输入你的答案..." rows="8"></textarea>
        </div>

        <div class="action-row">
          <button class="btn btn--outline" :disabled="cur===0" @click="goPrev">上一题</button>
          <span class="page-indicator">{{ cur+1 }} / {{ qCount }}</span>
          <button class="btn btn--outline" :disabled="cur>=qCount-1" @click="goNext">下一题</button>
          <button class="btn btn--primary" :disabled="!allDone || submitting" @click="doSubmit">
            {{ submitting ? '提交中...' : `提交 (${answered}/${qCount})` }}
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createSession, submitAnswersBatch, getNextBatch, completeSession as finishApi } from '../api/practice.js'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref('')
const session = ref(null)
const questions = ref([])
const ans = reactive({})
const blankAns = reactive({})
const rs = ref({})
const cur = ref(0)
const allSubmitted = ref(false)
const overviewDetail = ref(false)
const batchCorrect = ref(0)
const submitting = ref(false)
const hasMore = ref(true)
const start = ref(Date.now())
const sec = ref(0)
let timer = null
let autoNextTimer = null

const subjectId = computed(() => route.query.subject)
const mode = computed(() => 'mock_exam')
const qCount = computed(() => questions.value.length)

function isChoice(q) { return q?.questionType === 1 }
function isFillBlank(q) { return q?.questionType === 2 }
function isComprehensive(q) { return q?.questionType === 3 }

function typeLabel(q) { if (!q) return ''; if (isChoice(q)) return '选择题'; if (isFillBlank(q)) return '填空题'; return '综合题' }
function typeBadge(q) { if (!q) return ''; if (isChoice(q)) return '[选]'; if (isFillBlank(q)) return '[填]'; return '[综]' }
function typeTagClass(q) { if (!q) return ''; if (isChoice(q)) return 'tag--blue'; if (isFillBlank(q)) return 'tag--green'; return 'tag--yellow' }

function wordCount() { const v = ans[cur.value]; return v ? String(v).length : 0 }

const answered = computed(() => {
  return questions.value.reduce((c, q, i) => {
    if (isChoice(q)) return c + (ans[i] && String(ans[i]).trim() ? 1 : 0)
    if (isFillBlank(q)) {
      const bc = blankCount(i)
      if (!bc) return c
      const arr = blankAns[i]; if (!arr) return c
      return c + (Array.from({length: bc}).every((_, j) => arr[j] && String(arr[j]).trim()) ? 1 : 0)
    }
    return c + (ans[i] && String(ans[i]).trim() ? 1 : 0)
  }, 0)
})

const allDone = computed(() => answered.value >= qCount.value && qCount.value > 0)
const pct = computed(() => qCount.value ? Math.round(answered.value / qCount.value * 100) : 0)
const clock = computed(() => { const m = Math.floor(sec.value / 60); const s = sec.value % 60; return `${m}:${String(s).padStart(2, '0')}` })
const currentQ = computed(() => questions.value[cur.value] || null)

const pageTitle = computed(() => subjectId.value ? `模考 · ${subjectId.value}` : '模考')

const questionList = computed(() => questions.value.map((_, i) => {
  if (i === cur.value) return { status: 'current' }
  if (isChoice(questions.value[i])) {
    if (ans[i] && String(ans[i]).trim()) return { status: 'answered' }
  } else if (isFillBlank(questions.value[i])) {
    const bc = blankCount(i)
    if (bc && Array.from({length: bc}).every((_, j) => blankAns[i]?.[j] && String(blankAns[i][j]).trim())) return { status: 'answered' }
  } else {
    if (ans[i] && String(ans[i]).trim()) return { status: 'answered' }
  }
  return { status: 'pending' }
}))

const overviewList = computed(() => questions.value.map((_, i) => {
  if (rs.value[i]) return { status: rs.value[i].isCorrect ? 'done' : 'wrong' }
  return { status: 'pending' }
}))

function toOpts(raw) {
  if (!Array.isArray(raw)) return []
  return raw.map((o, i) => {
    const s = typeof o === 'string' ? o : o.text || ''
    const m = s.match(/^([A-J])[.、]\s*(.+)/)
    return m ? { key: m[1], text: m[2] } : { key: String.fromCharCode(65 + i), text: s }
  })
}

function splitByBlanks(content) {
  if (!content) return ['']
  if (typeof content === 'number') {
    const q = questions.value[content]
    content = q ? (typeof q.content === 'string' ? q.content : (q.content?.stem || '')) : ''
  } else if (typeof content === 'object') {
    content = typeof content.content === 'string' ? content.content : (content.content?.stem || '')
  }
  return (content || '').split(/_{2,}/g)
}

function blankCount(idx) {
  const parts = splitByBlanks(idx)
  return parts.length - 1
}

function pick(key) {
  ans[cur.value] = key
  if (cur.value < qCount.value - 1) { nextTick(() => goNext()) }
}

const mathSymbols = ['∑','∏','∫','√','∂','∇','≤','≥','≠','≈','∞','²','³','ⁿ','α','β','γ','δ','θ','λ','μ','π','σ','φ','ω','∈','∉','⊂','⊃','∪','∩','∅']

function onBlankEnter(blankIdx, total) {
  if (blankIdx >= total - 1 && cur.value < qCount.value - 1) { nextTick(() => goNext()) }
}

const blankSegments = computed(() => {
  const q = currentQ.value
  if (!q || !isFillBlank(q)) return []
  const content = typeof q.content === 'string' ? q.content : (q.content?.stem || '')
  const parts = splitByBlanks(content)
  const segs = []
  for (let i = 0; i < parts.length; i++) {
    if (parts[i]) segs.push({ text: parts[i], isBlank: false })
    if (i < parts.length - 1) segs.push({ isBlank: true, idx: i })
  }
  return segs
})

function goPrev() { if (cur.value > 0) cur.value-- }
function goNext() { if (cur.value < qCount.value - 1) cur.value++ }

async function loadQuestions() {
  loading.value = true; error.value = ''
  const key = `yantiku_progress_${subjectId.value}_${mode.value}`
  const raw = localStorage.getItem(key)
  let progress = null
  try { progress = raw ? JSON.parse(raw) : null } catch { }
  try {
    if (progress && progress.sessionId) {
      try {
        const qs = await getNextBatch(progress.sessionId, progress.offset || 0)
        if (qs && qs.length) {
          session.value = { id: progress.sessionId }; questions.value = qs
          hasMore.value = qs.length >= 20; initAnswers(); return
        }
      } catch { localStorage.removeItem(key) }
    }
    session.value = await createSession({ subjectId: subjectId.value, mode: mode.value, totalQuestions: 20 })
    questions.value = session.value?.questions || []
    hasMore.value = questions.value.length >= 20
    if (!questions.value.length) error.value = '该科目暂无题目'
    initAnswers()
  } catch (e) { error.value = '创建会话失败: ' + (e.message || '后端未启动') }
  finally { loading.value = false }
}

function initAnswers() {
  questions.value.forEach((q, i) => {
    ans[i] = ''
    if (isFillBlank(q)) {
      const bc = blankCount(i)
      blankAns[i] = Array.from({ length: bc }, () => '')
    }
  })
}

onMounted(() => { timer = setInterval(() => { sec.value = Math.floor((Date.now() - start.value) / 1000) }, 1000); loadQuestions() })
onUnmounted(() => clearInterval(timer))

async function doSubmit() {
  if (!allDone.value || submitting.value) return
  submitting.value = true
  try {
    const payload = questions.value.map((q, i) => {
      let userAnswer
      if (isChoice(q)) { userAnswer = ans[i] || '' }
      else if (isFillBlank(q)) {
        const bc = blankCount(i)
        const vals = {}; for (let j = 0; j < bc; j++) vals[j + 1] = blankAns[i]?.[j] || ''
        userAnswer = JSON.stringify(vals)
      } else { userAnswer = ans[i] || '' }
      return { questionId: q.id, userAnswer, timeSpent: Math.floor(sec.value / Math.max(1, qCount.value)) }
    })
    const res = await submitAnswersBatch(session.value.id, payload)
    let c = 0; res.forEach((r, i) => { rs.value[i] = r; if (r.isCorrect) c++ })
    batchCorrect.value = c; allSubmitted.value = true; cur.value = 0; overviewDetail.value = false
  } catch (e) { alert('提交失败: ' + (e.message || '系统繁忙')) }
  finally { submitting.value = false }
}

async function doRedo() {
  clearTimeout(autoNextTimer); allSubmitted.value = false; overviewDetail.value = false
  rs.value = {}; cur.value = 0; initAnswers()
}

function insertSymbol(sym) {
  const el = document.activeElement
  if (el && el.tagName === 'INPUT' && el.type === 'text') {
    const start = el.selectionStart; const end = el.selectionEnd
    const v = el.value
    el.value = v.slice(0, start) + sym + v.slice(end)
    el.selectionStart = el.selectionEnd = start + sym.length
    el.dispatchEvent(new Event('input', { bubbles: true }))
    el.focus()
  }
}

async function doFinish() {
  clearTimeout(autoNextTimer)
  if (hasMore.value) {
    try {
      const qs = await getNextBatch(session.value.id, questions.value.length)
      if (!qs || !qs.length) { error.value = '已无更多题目'; return }
      if (qs.length < 20) hasMore.value = false
      allSubmitted.value = false; overviewDetail.value = false; rs.value = {}; cur.value = 0
      questions.value = qs; initAnswers()
      const key = `yantiku_progress_${subjectId.value}_${mode.value}`
      localStorage.setItem(key, JSON.stringify({ sessionId: session.value.id, offset: questions.value.length }))
    } catch(e) { error.value = '加载失败: ' + (e.message || '系统繁忙') }
  } else {
    const key = `yantiku_progress_${subjectId.value}_${mode.value}`
    localStorage.removeItem(key)
    try { await finishApi(session.value.id) } catch (e) { /* ignore */ }
    router.push(subjectId.value ? `/subject/${subjectId.value}` : '/')
  }
}
</script>

<style scoped>
.page { display: flex; width: 100%; height: 100%; background: var(--color-bg-page); overflow: hidden; }

.quiz-sidebar { width: 256px; height: 100%; display: flex; flex-direction: column; background: var(--color-bg-sidebar); padding: 20px; gap: 16px; flex-shrink: 0; }
.quiz-sidebar__back { display: flex; align-items: center; width: 216px; height: 36px; padding: 0 12px; border: 1px solid var(--color-border); border-radius: 18px; gap: 8px; font-size: var(--font-size-base); color: var(--color-text-secondary); cursor: pointer; }
.quiz-sidebar__back:hover { background: rgba(0,0,0,0.03); }
.quiz-sidebar__progress { display: flex; flex-direction: column; gap: 8px; }
.quiz-sidebar__progress-label { font-size: var(--font-size-sm); font-weight: var(--font-weight-medium); color: var(--color-text-secondary); }
.quiz-sidebar__progress-value { font-size: var(--font-size-4xl); font-weight: var(--font-weight-bold); color: var(--color-primary); }
.progress-bar--small { height: 4px; background: var(--color-border); border-radius: 2px; }
.progress-bar--small .progress-bar__fill { height: 4px; background: var(--color-primary); border-radius: 2px; transition: width 0.3s; }
.quiz-sidebar__list { display: flex; flex-direction: column; gap: 4px; flex: 1; overflow-y: auto; }
.quiz-sidebar__item { display: flex; align-items: center; justify-content: center; width: 216px; height: 32px; border-radius: var(--radius-xl); font-size: var(--font-size-sm); font-weight: var(--font-weight-medium); cursor: pointer; transition: all 150ms ease; }
.quiz-sidebar__item--answered { background: var(--color-success); color: var(--color-white); }
.quiz-sidebar__item--wrong { background: var(--color-error); color: var(--color-white); }
.quiz-sidebar__item--done { background: var(--color-success); color: var(--color-white); }
.quiz-sidebar__item--current { background: var(--color-primary); color: var(--color-white); font-weight: var(--font-weight-semibold); }
.quiz-sidebar__item--pending { background: transparent; color: var(--color-text-muted); }
.quiz-sidebar__item--pending:hover { background: rgba(1,77,178,0.06); color: var(--color-primary); }

.main { flex: 1; display: flex; flex-direction: column; background: var(--color-bg-main); padding: 0 32px 0 32px; }
.main__toolbar { display: flex; align-items: center; justify-content: space-between; height: 48px; }
.main__title { font-size: var(--font-size-xl); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); }
.timer { display: flex; align-items: center; height: 32px; padding: 0 12px; background: var(--color-warning-light); border-radius: var(--radius-xl); gap: 6px; }
.timer__icon { font-size: var(--font-size-md); }
.timer__value { font-family: var(--font-mono); font-size: var(--font-size-md); font-weight: var(--font-weight-medium); color: var(--color-warning-dark); }
.main__content { flex: 1; display: flex; flex-direction: column; padding-top: 24px; gap: 24px; }

.tags-row { display: flex; gap: 8px; }
.tag { display: inline-flex; align-items: center; height: 24px; padding: 0 12px; border-radius: var(--radius-lg); font-size: 11px; font-weight: var(--font-weight-medium); }
.tag--blue { background: #dbeafe; color: #1e40af; }
.tag--green { background: #dcfce7; color: #166534; }
.tag--yellow { background: #fef9c3; color: #854d0e; }
.tag--gray { background: var(--color-tag-bg); color: var(--color-text-secondary); }

.question-card { padding: 20px 24px; background: var(--color-white); border: 1px solid var(--color-border); border-radius: var(--radius-xl); }
.question-card__text { font-size: 17px; font-weight: var(--font-weight-medium); color: var(--color-text-primary); line-height: 1.8; margin: 0; }

/* 选择题 */
.options-list { display: flex; flex-direction: column; gap: 8px; }
.option { display: flex; align-items: center; gap: 12px; height: 48px; padding: 0 16px; background: var(--color-white); border: 1px solid var(--color-border); border-radius: var(--radius-lg); cursor: pointer; transition: all 150ms ease; }
.option:hover { border-color: var(--color-primary); background: var(--color-primary-bg); }
.option--selected { border-color: var(--color-primary); background: var(--color-primary-bg); }
.option__key { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: var(--color-bg-page); font-size: 13px; font-weight: var(--font-weight-semibold); color: var(--color-text-secondary); flex-shrink: 0; }
.option--selected .option__key { background: var(--color-primary); color: var(--color-white); }
.option__text { font-size: var(--font-size-md); color: var(--color-text-primary); }

/* 填空 */
.blank-input { width: 80px; padding: 2px 8px; border: none; border-bottom: 2px solid var(--color-primary); font-size: 15px; font-family: inherit; text-align: center; color: var(--color-primary); outline: none; margin: 0 4px; background: transparent; }
.blank-input:focus { background: var(--color-primary-bg); }
.blank-input-wrapper { display: inline; }

/* Symbol bar */
.symbol-bar { display: flex; align-items: center; gap: 4px; padding: 8px 0; flex-wrap: wrap; }
.symbol-bar__label { font-size: 12px; color: var(--color-text-muted); margin-right: 4px; flex-shrink: 0; }
.symbol-chip { display: inline-flex; align-items: center; justify-content: center; min-width: 28px; height: 28px; padding: 0 6px; border: 1px solid var(--color-border); border-radius: 6px; font-size: 14px; color: var(--color-text-secondary); cursor: pointer; transition: all 120ms ease; background: var(--color-white); }
.symbol-chip:hover { border-color: var(--color-primary); color: var(--color-primary); background: var(--color-primary-bg); }

/* 综合 */
.answer-area { background: var(--color-white); border: 1px solid var(--color-border); border-radius: var(--radius-xl); overflow: hidden; }
.answer-area__toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-bottom: 1px solid var(--color-border); }
.answer-area__label { font-size: 14px; font-weight: var(--font-weight-medium); color: var(--color-text-primary); }
.word-count { font-size: 13px; color: var(--color-text-muted); }
.answer-textarea { width: 100%; min-height: 160px; padding: 16px; border: none; outline: none; font-size: 15px; font-family: var(--font-primary); line-height: 1.7; resize: vertical; box-sizing: border-box; color: var(--color-text-primary); }

/* 概览 */
.overview-grid { display: flex; flex-direction: column; gap: 6px; overflow-y: auto; }
.ov-card { display: flex; align-items: center; gap: 10px; padding: 10px 14px; background: var(--color-white); border: 1px solid var(--color-border); border-radius: var(--radius-lg); cursor: pointer; transition: all 150ms ease; }
.ov-card:hover { border-color: var(--color-primary); }
.ov-card--ok { border-color: #bbf7d0; background: var(--color-success-light); }
.ov-card--ng { border-color: #fecaca; background: var(--color-error-light); }
.ov-num { width: 24px; height: 24px; border-radius: 50%; background: var(--color-bg-page); display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: var(--font-weight-semibold); flex-shrink: 0; color: var(--color-text-secondary); }
.ov-card--ok .ov-num { background: var(--color-success); color: var(--color-white); }
.ov-card--ng .ov-num { background: var(--color-error); color: var(--color-white); }
.ov-stem { flex: 1; font-size: var(--font-size-base); color: var(--color-text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ov-res { font-size: 16px; }

/* 概览里的静态选项 */
.options-readonly { display: flex; flex-direction: column; gap: 6px; padding: 16px 20px; background: var(--color-white); border: 1px solid var(--color-border); border-radius: var(--radius-xl); }
.opt-line { font-size: 14px; color: var(--color-text-secondary); padding: 4px 0; }
.opt--pick { color: var(--color-primary); font-weight: var(--font-weight-semibold); }
.opt--correct { color: var(--color-success); font-weight: var(--font-weight-semibold); }

/* 详情 */
.q-detail { flex: 1; display: flex; flex-direction: column; gap: 20px; }
.question-text { font-size: 17px; font-weight: var(--font-weight-medium); color: var(--color-text-primary); line-height: 1.8; padding: 20px 24px; background: var(--color-white); border: 1px solid var(--color-border); border-radius: var(--radius-xl); }
.answer-card { background: var(--color-white); border: 1px solid var(--color-border); border-radius: var(--radius-xl); overflow: hidden; }
.answer-card__hd { padding: 10px 16px; background: var(--color-bg-page); font-size: 13px; font-weight: var(--font-weight-medium); color: var(--color-text-secondary); }
.answer-card__body { padding: 14px 16px; font-size: 15px; line-height: 1.7; color: var(--color-text-primary); white-space: pre-wrap; }

.result { padding: 14px 18px; border-radius: var(--radius-lg); font-size: 13px; line-height: 1.6; }
.result--ok { background: var(--color-success-light); color: #166534; }
.result--ng { background: var(--color-error-light); color: #991b1b; }
.result-head { font-weight: var(--font-weight-semibold); margin-bottom: 4px; font-size: 14px; }
.result-row { margin-top: 4px; }
.result-analysis { opacity: 0.85; }
.result-ai { font-style: italic; }

.action-row { display: flex; justify-content: center; align-items: center; gap: 12px; margin-top: auto; padding-bottom: 24px; }
.page-indicator { font-size: var(--font-size-base); color: var(--color-text-muted); min-width: 48px; text-align: center; }

.btn { display: inline-flex; align-items: center; justify-content: center; height: 44px; padding: 0 24px; border-radius: 22px; font-size: var(--font-size-md); font-weight: var(--font-weight-medium); cursor: pointer; border: none; transition: all 150ms ease; }
.btn--outline { background: var(--color-white); border: 1px solid var(--color-border); color: var(--color-text-secondary); }
.btn--outline:hover:not(:disabled) { border-color: var(--color-primary); color: var(--color-primary); }
.btn--outline:disabled { opacity: 0.4; cursor: not-allowed; }
.btn--primary { background: var(--color-primary); color: var(--color-white); font-weight: var(--font-weight-semibold); }
.btn--primary:hover:not(:disabled) { box-shadow: var(--shadow-md); }
.btn--primary:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
