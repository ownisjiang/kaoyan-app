<template>
  <div v-if="loading" class="page-loading">加载中...</div>
  <div v-else class="page">
    <SideNav />
    <main class="main">
      <!-- 顶部: 头像 + 基础信息 -->
      <div class="hero">
        <div class="avatar">{{ initial }}</div>
        <div class="hero-info">
          <h1 class="hero-name">{{ profile.nickname || profile.name || '同学' }}</h1>
          <span class="hero-email">{{ profile.email }}</span>
          <span class="hero-stats">{{ stats.totalQuestions }} 道总答题 · {{ stats.totalCorrect }} 正确</span>
        </div>
        <div class="hero-streak">
          <span class="streak-num">{{ stats.streakDays }}</span>
          <span class="streak-lab">连续天数</span>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stat-grid">
        <div class="stat-card">
          <span class="stat-val">{{ stats.todayQuestions }}</span>
          <span class="stat-lab">今日答题</span>
        </div>
        <div class="stat-card">
          <span class="stat-val accent">{{ accPct }}%</span>
          <span class="stat-lab">正确率</span>
        </div>
        <div class="stat-card">
          <span class="stat-val">{{ fmtSec(stats.totalDurationSec) }}</span>
          <span class="stat-lab">总学习时长</span>
        </div>
        <div class="stat-card">
          <span class="stat-val">{{ wrongCount }}</span>
          <span class="stat-lab">待复习错题</span>
        </div>
      </div>

      <!-- 成就 -->
      <div class="section" v-if="achievements.length">
        <h2 class="section-title">🏆 成就徽章</h2>
        <div class="ach-grid">
          <div v-for="a in achievements" :key="a.code" class="ach-item" :class="{ unlocked: a.unlocked }">
            <span class="ach-icon">{{ a.iconUrl }}</span>
            <div class="ach-info">
              <span class="ach-name">{{ a.name }}</span>
              <span class="ach-desc">{{ a.description }}</span>
              <div class="ach-bar">
                <div class="ach-fill" :style="{ width: a.progress + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="section">
        <h2 class="section-title">⚡ 快捷操作</h2>
        <div class="quick-acts">
          <button class="act-btn" @click="router.push('/wrong-book')">📋 错题本 ({{ wrongCount }})</button>
          <button class="act-btn" @click="router.push('/report')">📊 诊断报告</button>
          <button class="act-btn" @click="router.push('/')">📝 开始刷题</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import SideNav from '../components/SideNav.vue'
import { getMyProfile, getMyStats, getMyAchievements } from '../api/user.js'
import { getWrongBookStats } from '../api/wrongBook.js'

const router = useRouter()
const loading = ref(true)
const profile = ref({})
const stats = ref({ totalQuestions: 0, totalCorrect: 0, totalDurationSec: 0, streakDays: 0, todayQuestions: 0, todayCorrect: 0 })
const achievements = ref([])
const wrongCount = ref(0)

const initial = computed(() => {
  const n = profile.value.nickname || profile.value.name || '?'
  return n[0] || '?'
})

const accPct = computed(() => {
  if (!stats.value.totalQuestions) return 0
  return Math.round(stats.value.totalCorrect / stats.value.totalQuestions * 100)
})

function fmtSec(s) {
  if (!s) return '0分钟'
  const h = Math.floor(s/3600), m = Math.floor((s%3600)/60)
  return h>0 ? `${h}小时${m}分钟` : `${m}分钟`
}

onMounted(async () => {
  try {
    const [p, s, a, w] = await Promise.all([
      getMyProfile(), getMyStats(), getMyAchievements(), getWrongBookStats()
    ])
    if (p) profile.value = p
    if (s) stats.value = s
    if (a) achievements.value = Array.isArray(a) ? a : a.data || []
    if (w) wrongCount.value = w.totalWrong || 0
  } catch (e) { /* ignore */ }
  loading.value = false
})
</script>

<style scoped>
.page { display: flex; width: 100%; height: 100vh; background: #f5f7fa; }
.main { flex: 1; overflow-y: auto; padding: 32px 40px; }

/* Hero */
.hero { display: flex; align-items: center; gap: 18px; background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 24px 28px; margin-bottom: 20px; }
.avatar { width: 60px; height: 60px; border-radius: 50%; background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 700; flex-shrink: 0; }
.hero-info { flex: 1; }
.hero-name { font-size: 20px; font-weight: 700; color: #1e293b; margin: 0 0 4px 0; }
.hero-email { font-size: 13px; color: #94a3b8; display: block; }
.hero-stats { font-size: 12px; color: #64748b; margin-top: 4px; display: block; }
.hero-streak { display: flex; flex-direction: column; align-items: center; border-left: 1px solid #e2e8f0; padding-left: 24px; }
.streak-num { font-size: 28px; font-weight: 800; color: #f59e0b; }
.streak-lab { font-size: 11px; color: #94a3b8; margin-top: 2px; }

/* Stats */
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 20px; }
.stat-card { display: flex; flex-direction: column; align-items: center; background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px 12px; }
.stat-val { font-size: 24px; font-weight: 700; color: #1e293b; }
.stat-val.accent { color: #6366f1; }
.stat-lab { font-size: 12px; color: #94a3b8; margin-top: 4px; }
@media (max-width: 700px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } }

/* Section */
.section { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px 24px; margin-bottom: 16px; }
.section-title { font-size: 15px; font-weight: 600; color: #1e293b; margin: 0 0 14px 0; }

/* Achievements */
.ach-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 10px; }
.ach-item { display: flex; align-items: center; gap: 12px; padding: 12px 14px; border: 1px solid #e2e8f0; border-radius: 10px; opacity: 0.45; }
.ach-item.unlocked { opacity: 1; border-color: #bbf7d0; background: #f0fdf4; }
.ach-icon { font-size: 28px; flex-shrink: 0; }
.ach-info { flex: 1; min-width: 0; }
.ach-name { font-size: 14px; font-weight: 600; color: #1e293b; display: block; }
.ach-desc { font-size: 11px; color: #94a3b8; display: block; margin-bottom: 6px; }
.ach-bar { height: 4px; background: #e2e8f0; border-radius: 2px; overflow: hidden; }
.ach-fill { height: 100%; background: #6366f1; border-radius: 2px; transition: width 0.4s; }

/* Quick actions */
.quick-acts { display: flex; gap: 10px; }
.act-btn { padding: 10px 20px; border: 1px solid #e2e8f0; border-radius: 10px; background: #fff; color: #475569; font-size: 14px; cursor: pointer; }
.act-btn:hover { background: #f8fafc; }
.act-btn.primary { background: #6366f1; color: #fff; border-color: #6366f1; }

.page-loading { display: flex; align-items: center; justify-content: center; height: 100vh; color: #64748b; font-size: 16px; }
</style>
