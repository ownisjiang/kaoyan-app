<template>
  <div v-if="loading" class="page-loading">加载中...</div>
  <div v-else class="page">
    <SideNav active-nav="report" />
    <main class="main">
      <header class="header">
        <h1 class="title">诊断报告</h1>
        <div class="header-right">
          <span class="date" v-if="reportDate">{{ reportDate }}</span>
          <span class="rating" :class="'rating--'+levelClass">{{ abilityRating }}</span>
        </div>
      </header>

      <!-- 无报告 -->
      <div v-if="!report" class="empty">
        <span class="empty-icon">📊</span>
        <p class="empty-text">暂无诊断报告</p>
        <p class="empty-hint">完成一次刷题会话后，系统会自动生成诊断报告</p>
        <button class="btn primary" @click="router.push('/')">去刷题</button>
      </div>

      <template v-else>
        <!-- 综合得分卡片 -->
        <div class="score-hero">
          <div class="score-circle">
            <svg viewBox="0 0 120 120" width="120" height="120">
              <circle cx="60" cy="60" r="52" fill="none" stroke="#e2e8f0" stroke-width="8"/>
              <circle cx="60" cy="60" r="52" fill="none" :stroke="scoreColor" stroke-width="8"
                      :stroke-dasharray="2 * Math.PI * 52"
                      :stroke-dashoffset="2 * Math.PI * 52 * (1 - overallScore / 100)"
                      stroke-linecap="round" transform="rotate(-90 60 60)" style="transition: stroke-dashoffset 1s;"/>
            </svg>
            <div class="score-inner">
              <span class="score-num">{{ overallScore }}</span>
              <span class="score-sub">综合得分</span>
            </div>
          </div>
        </div>

        <!-- 雷达图区域 -->
        <div class="section" v-if="radarLabels.length">
          <h2 class="section-title">📡 能力雷达图</h2>
          <div class="radar-chart">
            <div class="radar-bars">
              <div v-for="(item, i) in radarLabels" :key="i" class="radar-row">
                <span class="radar-name">{{ item.label }}</span>
                <div class="radar-track">
                  <div class="radar-fill" :style="{ width: item.value + '%', background: radarColor(item.value) }"></div>
                </div>
                <span class="radar-val">{{ item.value }}%</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 双栏: 薄弱 + 优势 -->
        <div class="cols">
          <div class="section" v-if="weakPoints.length">
            <h2 class="section-title">📉 薄弱知识点</h2>
            <div class="kp-list">
              <div v-for="(w, i) in weakPoints" :key="'w'+i" class="kp-item weak">
                <span class="kp-rank">{{ i+1 }}</span>
                <span class="kp-name">{{ w.knowledgePointName }}</span>
                <span class="kp-pct">{{ w.accuracy }}%</span>
                <span class="kp-detail">{{ w.correct }}/{{ w.total }}</span>
              </div>
            </div>
          </div>
          <div class="section" v-if="strengths.length">
            <h2 class="section-title">📈 优势知识点</h2>
            <div class="kp-list">
              <div v-for="(s, i) in strengths" :key="'s'+i" class="kp-item strong">
                <span class="kp-rank">{{ i+1 }}</span>
                <span class="kp-name">{{ s.knowledgePointName }}</span>
                <span class="kp-pct">{{ s.accuracy }}%</span>
                <span class="kp-detail">{{ s.correct }}/{{ s.total }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 时间分析 -->
        <div class="section" v-if="timeItems.length">
          <h2 class="section-title">⏱ 答题用时分析</h2>
          <div class="time-grid">
            <div class="time-item">
              <span class="time-val">{{ fmtSec(totalTime) }}</span>
              <span class="time-lab">总用时</span>
            </div>
            <div class="time-item">
              <span class="time-val">{{ fmtSec(avgTime) }}</span>
              <span class="time-lab">平均每题</span>
            </div>
            <div v-for="(t, i) in timeItems" :key="'t'+i" class="time-item">
              <span class="time-val">{{ fmtSec(t.avgTimeSeconds) }}</span>
              <span class="time-lab">{{ t.questionTypeName }}</span>
            </div>
          </div>
        </div>

        <!-- 改进建议 -->
        <div class="section" v-if="suggestions.length">
          <h2 class="section-title">💡 改进建议</h2>
          <div class="sug-list">
            <div v-for="(s, i) in suggestions" :key="i" class="sug-item" :class="'prio--'+s.priority">
              <span class="sug-tag">{{ s.target || '整体' }}</span>
              <span class="sug-desc">{{ s.description }}</span>
            </div>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import SideNav from '../components/SideNav.vue'
import { getReportDetail } from '../api/diagnostic.js'

const router = useRouter()
const loading = ref(true)
const report = ref(null)

const reportDate = computed(() => {
  const d = report.value?.generatedAt
  if (!d) return ''
  const dt = new Date(d)
  return `${dt.getFullYear()}年${dt.getMonth()+1}月${dt.getDate()}日 ${String(dt.getHours()).padStart(2,'0')}:${String(dt.getMinutes()).padStart(2,'0')}`
})

const overallScore = computed(() => Math.round(report.value?.overallScore || 0))
const abilityRating = computed(() => report.value?.abilityRating || '?')
const levelClass = computed(() => {
  const r = abilityRating.value; if (r==='S'||r==='A') return 'high'; if (r==='B') return 'mid'; return 'low'
})
const scoreColor = computed(() => {
  const s = overallScore.value; if (s>=90) return '#22c55e'; if (s>=75) return '#6366f1'; if (s>=60) return '#f59e0b'; return '#ef4444'
})

const radarLabels = computed(() => {
  const rd = report.value?.radarData
  if (!rd) return []
  return rd.axes || []
})

const weakPoints = computed(() => (report.value?.weakPoints || []).slice(0, 5))
const strengths = computed(() => (report.value?.strengths || []).slice(0, 5))
const suggestions = computed(() => {
  const sg = report.value?.suggestions
  if (!sg) return []
  return Array.isArray(sg) ? sg : Object.values(sg)
})

const totalTime = computed(() => {
  const ta = report.value?.timeAnalysis
  return ta?.totalTimeSeconds || 0
})
const avgTime = computed(() => {
  const ta = report.value?.timeAnalysis
  return ta?.avgTimePerQuestion || 0
})
const timeItems = computed(() => {
  const ta = report.value?.timeAnalysis
  const by = ta?.byQuestionType
  if (!by) return []
  return Array.isArray(by) ? by : Object.values(by)
})

function fmtSec(s) {
  if (!s) return '0s'
  const m = Math.floor(s/60); const sec = s%60
  return m>0 ? `${m}分${sec}秒` : `${sec}秒`
}

function radarColor(v) {
  if (v>=80) return '#22c55e'; if (v>=60) return '#6366f1'; if (v>=40) return '#f59e0b'; return '#ef4444'
}

onMounted(async () => {
  try {
    // 直接获取最新报告
    const res = await getReportDetail('latest')
    report.value = res
  } catch (e) {
    report.value = null
  }
  loading.value = false
})
</script>

<style scoped>
.page { display: flex; width: 100%; height: 100vh; background: #f5f7fa; }
.main { flex: 1; overflow-y: auto; padding: 32px 40px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.title { font-size: 22px; font-weight: 700; color: #1e293b; margin: 0; }
.header-right { display: flex; align-items: center; gap: 12px; }
.date { font-size: 13px; color: #94a3b8; }
.rating { display: inline-flex; align-items: center; height: 30px; padding: 0 14px; border-radius: 15px; font-size: 14px; font-weight: 700; color: #fff; }
.rating--high { background: #22c55e; }
.rating--mid { background: #6366f1; }
.rating--low { background: #f59e0b; }

.empty { display: flex; flex-direction: column; align-items: center; padding: 80px 0; }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-text { font-size: 18px; color: #64748b; }
.empty-hint { font-size: 14px; color: #94a3b8; margin: 8px 0 24px; }

/* 综合得分 */
.score-hero { display: flex; justify-content: center; margin-bottom: 28px; }
.score-circle { position: relative; width: 120px; height: 120px; }
.score-inner { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.score-num { font-size: 32px; font-weight: 800; color: #1e293b; }
.score-sub { font-size: 11px; color: #94a3b8; margin-top: 2px; }

/* Section */
.section { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px 24px; margin-bottom: 16px; }
.section-title { font-size: 15px; font-weight: 600; color: #1e293b; margin: 0 0 14px 0; }

/* 雷达图 */
.radar-bars { display: flex; flex-direction: column; gap: 10px; }
.radar-row { display: flex; align-items: center; gap: 10px; }
.radar-name { width: 80px; font-size: 13px; color: #475569; text-align: right; flex-shrink: 0; }
.radar-track { flex: 1; height: 8px; background: #e2e8f0; border-radius: 4px; overflow: hidden; }
.radar-fill { height: 100%; border-radius: 4px; transition: width 0.6s; }
.radar-val { width: 40px; font-size: 12px; font-weight: 600; color: #64748b; text-align: right; }

/* 双栏 */
.cols { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
@media (max-width: 800px) { .cols { grid-template-columns: 1fr; } }

/* 知识点列表 */
.kp-item { display: flex; align-items: center; gap: 8px; padding: 8px 0; border-bottom: 1px solid #f1f5f9; }
.kp-item:last-child { border-bottom: none; }
.kp-rank { width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; flex-shrink: 0; }
.kp-item.weak .kp-rank { background: #fef2f2; color: #ef4444; }
.kp-item.strong .kp-rank { background: #f0fdf4; color: #22c55e; }
.kp-name { flex: 1; font-size: 13px; color: #334155; }
.kp-pct { font-size: 13px; font-weight: 600; }
.kp-item.weak .kp-pct { color: #ef4444; }
.kp-item.strong .kp-pct { color: #22c55e; }
.kp-detail { font-size: 11px; color: #94a3b8; }

/* 时间分析 */
.time-grid { display: flex; gap: 20px; }
.time-item { display: flex; flex-direction: column; gap: 4px; }
.time-val { font-size: 20px; font-weight: 700; color: #1e293b; }
.time-lab { font-size: 11px; color: #94a3b8; }

/* 建议 */
.sug-item { display: flex; align-items: flex-start; gap: 10px; padding: 8px 0; border-bottom: 1px solid #f1f5f9; }
.sug-item:last-child { border-bottom: none; }
.sug-tag { display: inline-flex; align-items: center; height: 22px; padding: 0 10px; border-radius: 10px; font-size: 11px; font-weight: 500; white-space: nowrap; flex-shrink: 0; }
.prio--high .sug-tag { background: #fef2f2; color: #ef4444; }
.prio--medium .sug-tag { background: #fef9c3; color: #92400e; }
.prio--low .sug-tag { background: #f1f5f9; color: #64748b; }
.sug-desc { font-size: 13px; color: #475569; line-height: 1.5; }

/* 通用 */
.btn { padding: 8px 20px; border: 1px solid #e2e8f0; border-radius: 8px; background: #fff; color: #475569; font-size: 14px; cursor: pointer; }
.btn.primary { background: #6366f1; color: #fff; border-color: #6366f1; }
.page-loading { display: flex; align-items: center; justify-content: center; height: 100vh; color: #64748b; font-size: 16px; }
</style>
