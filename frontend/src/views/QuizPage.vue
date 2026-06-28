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
        <h1 class="main__title">选择题 · 答题结果</h1>
        <div class="timer">
          <span class="timer__icon">⏱</span>
          <span class="timer__value">{{ clock }}</span>
        </div>
      </header>

      <!-- 概览卡片列表 -->
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
            <span class="ov-stem">{{ (q.content||'').slice(0,40) }}{{ (q.content||'').length>40?'...':'' }}</span>
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
            <span class="tag tag--blue">选择题</span>
            <span class="tag tag--gray">难度 {{ questions[cur]?.difficulty ?? 3 }}/5</span>
          </div>
          <p class="question-text">{{ cur+1 }}. {{ questions[cur]?.content }}</p>
          <div class="opts">
            <div
              v-for="o in toOpts(questions[cur]?.options)"
              :key="o.key"
              class="opt"
              :class="{
                'opt--picked': ans[cur]===o.key,
                'opt--correct': rs[cur]?.isCorrect && rs[cur]?.correctAnswer===o.key,
                'opt--wrong': rs[cur] && ans[cur]===o.key && !rs[cur]?.isCorrect
              }"
            >
              <span class="opt-letter">{{ o.key }}</span>
              <span class="opt-text">{{ o.text }}</span>
            </div>
          </div>
          <div class="result" v-if="rs[cur]" :class="rs[cur].isCorrect?'result--ok':'result--ng'">
            <div class="result-head">{{ rs[cur].isCorrect?'✅ 正确':'❌ 错误' }}</div>
            <div v-if="rs[cur].correctAnswer" class="result-row">正确答案：{{ rs[cur].correctAnswer }}</div>
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
          <div
            class="progress-bar__fill"
            :style="{ width: pct + '%' }"
          ></div>
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
        <!-- Tags -->
        <div class="tags-row">
          <span class="tag tag--blue">选择题</span>
          <span class="tag tag--gray">难度 {{ currentQ?.difficulty ?? 3 }}/5</span>
          <span class="tag tag--gray" v-if="currentQ?.source">{{ currentQ.source }}</span>
        </div>

        <!-- 题目 -->
        <p class="question-text">{{ cur+1 }}. {{ currentQ?.content }}</p>

        <!-- 选项 -->
        <div class="opts" v-if="currentQ?.questionType === 1">
          <div
            v-for="o in currentOpts"
            :key="o.key"
            class="opt"
            :class="{ 'opt--picked': ans[cur]===o.key }"
            @click="pick(o.key)"
          >
            <span class="opt-letter">{{ o.key }}</span>
            <span class="opt-text">{{ o.text }}</span>
          </div>
        </div>

        <!-- 非选择题输入 -->
        <textarea
          v-else
          v-model="ans[cur]"
          placeholder="在此输入答案..."
          class="text-ans"
          rows="4"
        ></textarea>

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
            {{ submitting ? '提交中...' : `提交 (${answered}/${qCount})` }}
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createSession, submitAnswersBatch, getNextBatch, completeSession as finishApi } from '../api/practice.js'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref('')
const session = ref(null)
const questions = ref([])
const ans = ref({})
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
const mode = computed(() => route.query.mode || 'quick_quiz')
const qCount = computed(() => questions.value.length)

const answered = computed(() => {
  return questions.value.reduce((c, _, i) => {
    const v = ans.value[i]
    return c + (v && String(v).trim() ? 1 : 0)
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

const currentOpts = computed(() => toOpts(currentQ.value?.options))

const pageTitle = computed(() => {
  if (!subjectId.value) return '选择题'
  return `选择题 · ${subjectId.value}`
})

// 答题中侧边栏状态
const questionList = computed(() => {
  return questions.value.map((_, i) => {
    if (i === cur.value) return { status: 'current' }
    if (ans.value[i] && String(ans.value[i]).trim()) return { status: 'answered' }
    return { status: 'pending' }
  })
})

// 提交后概览侧边栏状态
const overviewList = computed(() => {
  return questions.value.map((_, i) => {
    if (rs.value[i]) {
      return { status: rs.value[i].isCorrect ? 'done' : 'wrong' }
    }
    return { status: 'pending' }
  })
})

function toOpts(raw) {
  if (!Array.isArray(raw)) return []
  return raw.map((o, i) => {
    const s = typeof o === 'string' ? o : o.text || ''
    const m = s.match(/^([A-J])[.、]\s*(.+)/)
    return m ? { key: m[1], text: m[2] } : { key: String.fromCharCode(65 + i), text: s }
  })
}

// 选择选项 → 记录答案 → 自动跳下一题
function pick(key) {
  ans.value[cur.value] = key
  // 自动跳转到下一题（最后一题除外）
  if (cur.value < qCount.value - 1) {
    goNext()
  }
}

function goPrev() {
  if (cur.value > 0) cur.value--
}

function goNext() {
  if (cur.value < qCount.value - 1) cur.value++
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
      error.value = '该科目暂无题目'
    }
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
    const payload = questions.value.map((q, i) => ({
      questionId: q.id,
      userAnswer: ans.value[i] || '',
      timeSpent: Math.floor(sec.value / qCount.value)
    }))
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
  ans.value = {}
  cur.value = 0
}

async function doNext() {
  clearTimeout(autoNextTimer)
  allSubmitted.value = false
  overviewDetail.value = false
  rs.value = {}
  ans.value = {}
  cur.value = 0
  try {
    const qs = await getNextBatch(session.value.id, questions.value.length)
    if (qs.length < 10) hasMore.value = false
    questions.value = qs
    if (!qs.length) error.value = '已无更多题目'
  } catch(e) { error.value = '加载失败' }
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
      ans.value = {}
      cur.value = 0
      questions.value = qs
      const key = `yantiku_progress_${subjectId.value}_${mode.value}`
      localStorage.setItem(key, JSON.stringify({ sessionId: session.value.id, offset: questions.value.length }))
    } catch(e) { error.value = '加载失败: ' + (e.message || '系统繁忙') }
  } else {
    // 没有更多题目：正常完成，清除进度
    const key = `yantiku_progress_${subjectId.value}_${mode.value}`
    localStorage.removeItem(key)
    try { await finishApi(session.value.id) } catch (e) { /* ignore */ }
    alert(`🎉 会话完成！
本次答对 ${batchCorrect.value}/${qCount.value} 题
错题已自动加入错题本
诊断报告已生成
成就已检查
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
.tag--blue { background: var(--color-primary-light); color: var(--color-primary); }
.tag--gray { background: var(--color-tag-bg); color: var(--color-text-secondary); }

/* ===== Question ===== */
.question-text {
  font-size: 17px; font-weight: var(--font-weight-medium); color: var(--color-text-primary);
  line-height: 1.8; padding: 20px 24px; background: var(--color-white);
  border: 1px solid var(--color-border); border-radius: var(--radius-xl);
}

/* ===== Options ===== */
.opts { display: flex; flex-direction: column; gap: 10px; }
.opt {
  display: flex; align-items: flex-start; gap: 12px; padding: 14px 16px;
  background: var(--color-white); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); cursor: pointer; transition: all 150ms ease;
}
.opt:hover { border-color: var(--color-primary); background: var(--color-primary-bg); }
.opt--picked { border-color: var(--color-primary); background: var(--color-primary-light); }
.opt--correct { border-color: var(--color-success); background: var(--color-success-light); }
.opt--wrong { border-color: var(--color-error); background: var(--color-error-light); }
.opt-letter {
  width: 28px; height: 28px; border-radius: 50%; background: var(--color-bg-page);
  color: var(--color-text-secondary); display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: var(--font-weight-semibold); flex-shrink: 0;
  transition: all 150ms ease;
}
.opt--picked .opt-letter { background: var(--color-primary); color: var(--color-white); }
.opt--correct .opt-letter { background: var(--color-success); color: var(--color-white); }
.opt--wrong .opt-letter { background: var(--color-error); color: var(--color-white); }
.opt-text { font-size: 15px; line-height: 1.6; color: var(--color-text-primary); padding-top: 3px; }

/* ===== Textarea ===== */
.text-ans {
  width: 100%; padding: 14px 16px; border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); font-size: var(--font-size-md); font-family: var(--font-primary);
  resize: vertical; outline: none; box-sizing: border-box; background: var(--color-white);
}
.text-ans:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-bg); }

/* ===== Result ===== */
.result { margin-top: 0; padding: 14px 18px; border-radius: var(--radius-lg); font-size: 13px; line-height: 1.6; }
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

/* ===== Detail view (after submit) ===== */
.q-detail { flex: 1; display: flex; flex-direction: column; gap: 20px; }
</style>
