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
        <div
          v-for="(item, i) in overviewList"
          :key="i"
          class="quiz-sidebar__item"
          :class="`quiz-sidebar__item--${item.status}`"
          @click="cur=i; overviewDetail=true"
        >
          {{ i+1 }} {{ item.status === 'done' ? '✓' : item.status === 'wrong' ? '✗' : '⏭' }}
        </div>
      </div>
    </aside>

    <main class="main">
      <header class="main__toolbar">
        <h1 class="main__title">填空题 · 答题结果</h1>
        <div class="timer">
          <span class="timer__icon">⏱</span>
          <span class="timer__value">{{ clock }}</span>
        </div>
      </header>

      <div v-if="!overviewDetail" class="main__content">
        <div class="overview-grid">
          <div
            v-for="(q, i) in questions"
            :key="'ov-'+q.id"
            class="ov-card"
            :class="{ 'ov-card--ok': rs[i]?.isCorrect, 'ov-card--ng': rs[i] && !rs[i].isCorrect }"
            @click="cur=i; overviewDetail=true"
          >
            <span class="ov-num">{{ i+1 }}</span>
            <span class="ov-stem">{{ overviewText(i) }}</span>
            <span class="ov-res">{{ rs[i] ? (rs[i].isCorrect ? '✅' : '❌') : '⏭' }}</span>
          </div>
        </div>
        <div class="action-row">
          <button class="btn btn--outline" @click="doRedo" v-if="qCount>0">重新作答</button>
          <button class="btn btn--primary" @click="doFinish">{{ hasMore ? '完成 · 下一组' : '完成' }}</button>
        </div>
      </div>

      <!-- 展开单题详情 -->
      <div v-else class="main__content">
        <div class="q-detail">
          <div class="tags-row">
            <span class="tag tag--green">填空题</span>
            <span class="tag tag--gray">难度 {{ questions[cur]?.difficulty ?? 3 }}/5</span>
          </div>
          <p class="question-text">{{ cur+1 }}. {{ stemText(cur) }}</p>
          <div v-if="getBlanks(cur).length" class="fill-review-grid">
            <div v-for="b in getBlanks(cur)" :key="'rb-'+b.idx" class="fill-review-item">
              <span class="fill-review-label">空{{ b.idx+1 }}</span>
              <span class="fill-review-val" :class="{ 'fill-review-val--ok': blankCorrect(cur, b.idx), 'fill-review-val--ng': !blankCorrect(cur, b.idx) }">
                {{ ans[cur]?.['b'+b.idx] || '(未填)' }}
              </span>
            </div>
          </div>
          <div class="result" v-if="rs[cur]" :class="rs[cur].isCorrect?'result--ok':'result--ng'">
            <div class="result-head">{{ rs[cur].isCorrect?'✅ 正确':'❌ 错误' }}</div>
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
        <div
          v-for="(item, i) in questionList"
          :key="i"
          class="quiz-sidebar__item"
          :class="`quiz-sidebar__item--${item.status}`"
          @click="cur=i"
        >
          {{ i+1 }} {{ item.status === 'answered' ? '✓' : item.status === 'current' ? '▶' : '' }}
        </div>
      </div>
    </aside>

    <main class="main">
      <header class="main__toolbar">
        <h1 class="main__title">{{ pageTitle }}</h1>
        <div class="timer">
          <span class="timer__icon">⏱</span>
          <span class="timer__value">{{ clock }}</span>
        </div>
      </header>

      <div v-if="qCount === 0" class="main__content" style="align-items:center;justify-content:center">
        <span style="color:var(--color-text-secondary);font-size:var(--font-size-lg)">暂无题目，请选择其他科目</span>
        <button class="btn btn--primary" @click="router.push(subjectId ? `/subject/${subjectId}` : '/')" style="margin-top:16px">返回题库</button>
      </div>

      <div v-else class="main__content">
        <div class="tags-row">
          <span class="tag tag--green">填空题</span>
          <span class="tag tag--gray">难度 {{ currentQ?.difficulty ?? 3 }}/5</span>
        </div>

        <!-- 题目（含填空内嵌） -->
        <p class="question-text">
          {{ cur+1 }}.
          <template v-for="(seg, si) in stemSegments" :key="'seg-'+cur+'-'+si">
            <span v-if="seg.text">{{ seg.text }}</span>
            <span v-if="seg.isBlank" class="blank-input-wrapper">
              <input
                :ref="el => blankRefs[seg.idx] = el"
                v-model="ans[cur]['b'+seg.idx]"
                class="blank-input"
                :placeholder="'空'+(seg.idx+1)"
                @keydown.enter="onBlankEnter(seg.idx, stemSegments.filter(s=>s.isBlank).length)"
              />
            </span>
          </template>
        </p>

        <!-- 数学符号栏 -->
        <div class="symbol-bar">
          <span class="symbol-bar__label">数符：</span>
          <span v-for="s in mathSymbols" :key="s" class="symbol-chip" @mousedown.prevent="insertSymbol(s)">{{ s }}</span>
        </div>

        <!-- 操作按钮 -->
        <div class="action-row">
          <button class="btn btn--outline" :disabled="cur===0" @click="goPrev">上一题</button>
          <span class="page-indicator">{{ cur+1 }} / {{ qCount }}</span>
          <button class="btn btn--outline" :disabled="cur>=qCount-1" @click="goNext">下一题</button>
          <button
            class="btn btn--primary"
            :disabled="!allDone || submitting"
            @click="doSubmit"
          >
            {{ submitting ? 'AI批改中...' : `提交 (${answered}/${qCount})` }}
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
const blankRefs = {}

const mathSymbols = ['∑','∏','∫','√','∂','∇','≤','≥','≠','≈','∞','²','³','ⁿ','α','β','γ','δ','θ','λ','μ','π','σ','φ','ω','∈','∉','⊂','⊃','∪','∩','∅']

const subjectId = computed(() => route.query.subject)
const mode = computed(() => route.query.mode || 'fill_blank')
const qCount = computed(() => questions.value.length)

const answered = computed(() => {
  return questions.value.reduce((c, _, i) => {
    const blanks = getBlanks(i)
    if (!blanks.length) return c
    const allFilled = blanks.every(b => ans[i]?.['b' + b.idx] && String(ans[i]['b' + b.idx]).trim())
    return c + (allFilled ? 1 : 0)
  }, 0)
})

const allDone = computed(() => answered.value >= qCount.value && qCount.value > 0)
const pct = computed(() => qCount.value ? Math.round(answered.value / qCount.value * 100) : 0)
const clock = computed(() => {
  const m = Math.floor(sec.value / 60)
  const s = sec.value % 60
  return `${m}:${String(s).padStart(2, '0')}`
})

const currentQ = computed(() => questions.value[cur.value] || null)

const pageTitle = computed(() => {
  if (!subjectId.value) return '填空题精练'
  return `填空题 · ${subjectId.value}`
})

function splitByBlanks(str) {
  if (!str) return ['']
  return str.split(/_{2,}/)
}

function getBlanks(qIdx) {
  if (qIdx < 0 || qIdx >= questions.value.length) return []
  const q = questions.value[qIdx]
  const content = typeof q?.content === 'string' ? q.content : (q?.content?.stem || '')
  const parts = splitByBlanks(content)
  const blanks = []
  for (let i = 0; i < parts.length - 1; i++) {
    blanks.push({ idx: i, before: parts[i], after: parts[i + 1] || '' })
  }
  return blanks
}

const stemSegments = computed(() => {
  const q = currentQ.value
  if (!q) return []
  const content = typeof q.content === 'string' ? q.content : (q.content?.stem || '')
  const parts = splitByBlanks(content)
  const segs = []
  for (let i = 0; i < parts.length; i++) {
    if (parts[i]) segs.push({ text: parts[i], isBlank: false })
    if (i < parts.length - 1) segs.push({ isBlank: true, idx: i })
  }
  return segs
})

function stemText(qIdx) {
  const q = questions.value[qIdx]
  if (!q) return ''
  const content = typeof q.content === 'string' ? q.content : (q.content?.stem || '')
  return content.replace(/_{2,}/g, '___').slice(0, 60) + (content.length > 60 ? '...' : '')
}

function overviewText(i) {
  const blanks = getBlanks(i)
  const filled = blanks.map(b => ans[i]?.['b' + b.idx] || '?').join(', ')
  const stem = stemText(i)
  return `[${filled}] ${stem}`
}

function blankCorrect(qIdx, blankIdx) {
  // 如果整体正确则所有空都算对
  if (rs.value[qIdx]?.isCorrect) return true
  return false
}

// 答题中侧边栏
const questionList = computed(() => {
  return questions.value.map((_, i) => {
    if (i === cur.value) return { status: 'current' }
    const blanks = getBlanks(i)
    if (blanks.length && blanks.every(b => ans[i]?.['b' + b.idx] && String(ans[i]['b' + b.idx]).trim())) {
      return { status: 'answered' }
    }
    return { status: 'pending' }
  })
})

// 提交后概览侧边栏
const overviewList = computed(() => {
  return questions.value.map((_, i) => {
    if (rs.value[i]) {
      return { status: rs.value[i].isCorrect ? 'done' : 'wrong' }
    }
    return { status: 'pending' }
  })
})

function onBlankEnter(thisIdx, totalBlanks) {
  // 如果当前是最后一个空 → 自动跳到下一题
  if (thisIdx >= totalBlanks - 1 && cur.value < qCount.value - 1) {
    nextTick(() => goNext())
  }
}

function goPrev() {
  if (cur.value > 0) { cur.value--; focusFirstBlank() }
}

function goNext() {
  if (cur.value < qCount.value - 1) { cur.value++; focusFirstBlank() }
}

function focusFirstBlank() {
  nextTick(() => {
    const el = blankRefs[0]
    if (el) el.focus()
  })
}

async function loadQuestions() {
  loading.value = true
  error.value = ''
  const key = `yantiku_progress_${subjectId.value}_${mode.value}`
  const raw = localStorage.getItem(key)
  let progress = null
  try { progress = raw ? JSON.parse(raw) : null } catch { }
  try {
    if (progress && progress.sessionId) {
      try {
        const qs = await getNextBatch(progress.sessionId, progress.offset || 0)
        if (qs && qs.length) {
          session.value = { id: progress.sessionId }
          questions.value = qs
          hasMore.value = qs.length >= 10
          questions.value.forEach((_, i) => { ans[i] = {} })
          return
        }
      } catch { localStorage.removeItem(key) }
    }
    session.value = await createSession({
      subjectId: subjectId.value,
      mode: mode.value,
      totalQuestions: 10
    })
    questions.value = session.value?.questions || []
    hasMore.value = questions.value.length >= 10
    if (!questions.value.length) {
      error.value = '该科目暂无填空题'
    }
    questions.value.forEach((_, i) => { ans[i] = {} })
  } catch (e) {
    error.value = '创建会话失败: ' + (e.message || '后端未启动')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  timer = setInterval(() => { sec.value = Math.floor((Date.now() - start.value) / 1000) }, 1000)
  loadQuestions()
})

onUnmounted(() => clearInterval(timer))

async function doSubmit() {
  if (!allDone.value || submitting.value) return
  submitting.value = true
  try {
    const payload = questions.value.map((q, i) => {
      const blanks = getBlanks(i)
      const answers = {}
      blanks.forEach(b => { answers[b.idx + 1] = ans[i]?.['b' + b.idx] || '' })
      return {
        questionId: q.id,
        userAnswer: JSON.stringify(answers),
        timeSpent: Math.floor(sec.value / Math.max(1, qCount.value))
      }
    })
    const res = await submitAnswersBatch(session.value.id, payload)
    let c = 0
    res.forEach((r, i) => { rs.value[i] = r; if (r.isCorrect) c++ })
    batchCorrect.value = c
    allSubmitted.value = true
    cur.value = 0
    overviewDetail.value = false
  } catch (e) {
    alert('提交失败: ' + (e.message || '系统繁忙'))
  } finally {
    submitting.value = false
  }
}

async function doRedo() {
  clearTimeout(autoNextTimer)
  allSubmitted.value = false
  overviewDetail.value = false
  rs.value = {}
  questions.value.forEach((_, i) => { ans[i] = {} })
  cur.value = 0
}

async function doNext() {
  clearTimeout(autoNextTimer)
  allSubmitted.value = false
  overviewDetail.value = false
  rs.value = {}
  cur.value = 0
  try {
    const qs = await getNextBatch(session.value.id, questions.value.length)
    if (qs.length < 10) hasMore.value = false
    questions.value = qs
    questions.value.forEach((_, i) => { ans[i] = {} })
    if (!qs.length) error.value = '已无更多题目'
  } catch(e) { error.value = '加载失败' }
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
      if (qs.length < 10) hasMore.value = false
      allSubmitted.value = false
      overviewDetail.value = false
      rs.value = {}
      cur.value = 0
      questions.value = qs
      questions.value.forEach((_, i) => { ans[i] = {} })
      const key = `yantiku_progress_${subjectId.value}_${mode.value}`
      localStorage.setItem(key, JSON.stringify({ sessionId: session.value.id, offset: questions.value.length }))
    } catch(e) { error.value = '加载失败: ' + (e.message || '系统繁忙') }
  } else {
    const key = `yantiku_progress_${subjectId.value}_${mode.value}`
    localStorage.removeItem(key)
    try { await finishApi(session.value.id) } catch (e) { /* ignore */ }
    alert(`🎉 会话完成！
本次答对 ${batchCorrect.value}/${qCount.value} 题
错题已自动加入错题本
诊断报告已生成
可在个人中心查看详情`)
    router.push(subjectId.value ? `/subject/${subjectId.value}` : '/')
  }
}
</script>

<style scoped>
.page { display: flex; width: 100%; height: 100%; background: var(--color-bg-page); overflow: hidden; }

/* ===== Sidebar ===== */
.quiz-sidebar {
  width: 256px; height: 100%; display: flex; flex-direction: column;
  background: var(--color-bg-sidebar); padding: 20px; gap: 16px; flex-shrink: 0;
}
.quiz-sidebar__back {
  display: flex; align-items: center; width: 216px; height: 36px; padding: 0 12px;
  border: 1px solid var(--color-border); border-radius: 18px; gap: 8px;
  font-size: var(--font-size-base); color: var(--color-text-secondary); cursor: pointer;
  transition: background 150ms ease;
}
.quiz-sidebar__back:hover { background: rgba(0,0,0,0.03); }
.quiz-sidebar__progress { display: flex; flex-direction: column; gap: 8px; }
.quiz-sidebar__progress-label { font-size: var(--font-size-sm); font-weight: var(--font-weight-medium); color: var(--color-text-secondary); }
.quiz-sidebar__progress-value { font-size: var(--font-size-4xl); font-weight: var(--font-weight-bold); color: var(--color-primary); }
.progress-bar--small { height: 4px; background: var(--color-border); border-radius: 2px; }
.progress-bar--small .progress-bar__fill { height: 4px; background: var(--color-primary); border-radius: 2px; transition: width 0.3s; }
.quiz-sidebar__list { display: flex; flex-direction: column; gap: 4px; flex: 1; overflow-y: auto; }
.quiz-sidebar__item {
  display: flex; align-items: center; justify-content: center; width: 216px; height: 32px;
  border-radius: var(--radius-xl); font-size: var(--font-size-sm); font-weight: var(--font-weight-medium);
  cursor: pointer; transition: all 150ms ease;
}
.quiz-sidebar__item--answered { background: var(--color-success); color: var(--color-white); }
.quiz-sidebar__item--wrong { background: var(--color-error); color: var(--color-white); }
.quiz-sidebar__item--done { background: var(--color-success); color: var(--color-white); }
.quiz-sidebar__item--current { background: var(--color-primary); color: var(--color-white); font-weight: var(--font-weight-semibold); }
.quiz-sidebar__item--pending { background: transparent; color: var(--color-text-muted); }
.quiz-sidebar__item--pending:hover { background: rgba(1,77,178,0.06); color: var(--color-primary); }

/* ===== Main ===== */
.main { flex: 1; display: flex; flex-direction: column; background: var(--color-bg-main); padding: 0 32px 0 32px; }
.main__toolbar { display: flex; align-items: center; justify-content: space-between; height: 48px; }
.main__title { font-size: var(--font-size-xl); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); }
.timer { display: flex; align-items: center; height: 32px; padding: 0 12px;
  background: var(--color-warning-light); border-radius: var(--radius-xl); gap: 6px; }
.timer__icon { font-size: var(--font-size-md); }
.timer__value { font-family: var(--font-mono); font-size: var(--font-size-md); font-weight: var(--font-weight-medium); color: var(--color-warning-dark); }
.main__content { flex: 1; display: flex; flex-direction: column; padding-top: 24px; gap: 24px; }

/* ===== Tags ===== */
.tags-row { display: flex; gap: 8px; }
.tag { display: inline-flex; align-items: center; height: 24px; padding: 0 12px;
  border-radius: var(--radius-lg); font-size: 11px; font-weight: var(--font-weight-medium); }
.tag--green { background: #dcfce7; color: #166534; }
.tag--gray { background: var(--color-tag-bg); color: var(--color-text-secondary); }

/* ===== Question ===== */
.question-text {
  font-size: 17px; font-weight: var(--font-weight-medium); color: var(--color-text-primary);
  line-height: 2.2; padding: 20px 24px; background: var(--color-white);
  border: 1px solid var(--color-border); border-radius: var(--radius-xl);
}

/* ===== Blank inputs ===== */
.blank-input-wrapper { display: inline-flex; align-items: center; vertical-align: middle; }
.blank-input {
  width: 120px; height: 32px; padding: 0 8px; border: none;
  border-bottom: 2px solid var(--color-primary); outline: none;
  font-size: 16px; font-family: var(--font-primary); text-align: center;
  background: transparent; color: var(--color-text-primary);
  transition: border-color 150ms ease; margin: 0 4px;
}
.blank-input:focus { border-bottom-color: var(--color-primary-dark); }
.blank-input::placeholder { color: var(--color-text-muted); font-size: 14px; }

/* ===== Symbol bar ===== */
.symbol-bar { display: flex; align-items: center; gap: 4px; padding: 8px 0; flex-wrap: wrap; }
.symbol-bar__label { font-size: 12px; color: var(--color-text-muted); margin-right: 4px; flex-shrink: 0; }
.symbol-chip { display: inline-flex; align-items: center; justify-content: center; min-width: 28px; height: 28px; padding: 0 6px; border: 1px solid var(--color-border); border-radius: 6px; font-size: 14px; color: var(--color-text-secondary); cursor: pointer; transition: all 120ms ease; background: var(--color-white); }
.symbol-chip:hover { border-color: var(--color-primary); color: var(--color-primary); background: var(--color-primary-bg); }

/* ===== Fill review ===== */
.fill-review-grid { display: flex; flex-wrap: wrap; gap: 10px; }
.fill-review-item { display: flex; align-items: center; gap: 8px; padding: 6px 12px;
  background: var(--color-white); border: 1px solid var(--color-border); border-radius: var(--radius-md); }
.fill-review-label { font-size: 12px; color: var(--color-text-muted); font-weight: var(--font-weight-medium); }
.fill-review-val { font-size: 14px; font-weight: var(--font-weight-semibold); }
.fill-review-val--ok { color: #166534; }
.fill-review-val--ng { color: #991b1b; }

/* ===== Result ===== */
.result { padding: 14px 18px; border-radius: var(--radius-lg); font-size: 13px; line-height: 1.6; }
.result--ok { background: var(--color-success-light); color: #166534; }
.result--ng { background: var(--color-error-light); color: #991b1b; }
.result-head { font-weight: var(--font-weight-semibold); margin-bottom: 4px; font-size: 14px; }
.result-row { margin-top: 4px; }
.result-analysis { opacity: 0.85; }
.result-ai { font-style: italic; }

/* ===== Actions ===== */
.action-row { display: flex; justify-content: center; align-items: center; gap: 12px; margin-top: auto; padding-bottom: 24px; }
.page-indicator { font-size: var(--font-size-base); color: var(--color-text-muted); min-width: 48px; text-align: center; }

.btn { display: inline-flex; align-items: center; justify-content: center; height: 44px; padding: 0 24px;
  border-radius: 22px; font-size: var(--font-size-md); font-weight: var(--font-weight-medium); cursor: pointer;
  border: none; transition: all 150ms ease; }
.btn--outline { background: var(--color-white); border: 1px solid var(--color-border); color: var(--color-text-secondary); }
.btn--outline:hover:not(:disabled) { border-color: var(--color-primary); color: var(--color-primary); }
.btn--outline:disabled { opacity: 0.4; cursor: not-allowed; }
.btn--primary { background: var(--color-primary); color: var(--color-white); font-weight: var(--font-weight-semibold); }
.btn--primary:hover:not(:disabled) { box-shadow: var(--shadow-md); }
.btn--primary:disabled { opacity: 0.5; cursor: not-allowed; }

/* ===== Overview ===== */
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

/* ===== Detail view ===== */
.q-detail { flex: 1; display: flex; flex-direction: column; gap: 20px; }
</style>
