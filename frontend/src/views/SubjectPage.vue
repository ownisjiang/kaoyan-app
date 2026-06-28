<template>
  <div v-if="loading" class="page" style="display:flex;align-items:center;justify-content:center">
    <span style="color:var(--color-text-secondary);font-size:var(--font-size-lg)">加载中...</span>
  </div>
  <div v-else class="page">
    <main class="main">
      <TopBar />
      <header class="main__toolbar">
        <h1 class="main__title">{{ pageTitle }}</h1>
        <span class="main__stats">已做 {{ stats?.done ?? 0 }} 题 / 总掌握度 {{ overallMastery }}%</span>
      </header>

      <!-- Stat Cards -->
      <div class="stat-cards">
        <div class="stat-card">
          <span class="stat-card__value">{{ totalAvailable }}</span>
          <span class="stat-card__label">可用题目</span>
        </div>
        <div class="stat-card">
          <span class="stat-card__value">{{ overallMastery }}%</span>
          <span class="stat-card__label">总掌握度</span>
        </div>
        <div class="stat-card">
          <span class="stat-card__value">{{ stats?.toReview ?? 0 }}</span>
          <span class="stat-card__label">待复习</span>
        </div>
      </div>

      <div class="main__content">
        <!-- Knowledge Points -->
        <div class="section">
          <h2 class="section__title">📚 考点列表</h2>
          <div
            v-for="kp in knowledgePoints"
            :key="kp.id"
            class="kp-card"
            @click="startPractice('quick_quiz', kp.id)"
          >
            <div class="kp-card__header">
              <h3 class="kp-card__name">{{ kp.name }}</h3>
              <span v-if="kp.children?.length" class="kp-card__child-count">
                {{ kp.children.length }} 个子考点
              </span>
            </div>
            <!-- 子考点 -->
            <div v-if="kp.children?.length" class="kp-card__children">
              <span
                v-for="child in kp.children"
                :key="child.id"
                class="kp-tag"
                @click.stop="startPractice('quick_quiz', child.id)"
              >{{ child.name }}</span>
            </div>
          </div>
          <p v-if="knowledgePoints.length === 0" class="empty-hint">暂无考点数据</p>
        </div>

        <!-- Practice Modes -->
        <div class="section section--modes">
          <h2 class="section__title">📝 刷题模式</h2>
          <div class="mode-grid">
            <div
              v-for="mode in practiceModes"
              :key="mode.type"
              class="practice-card"
              :class="{ 'practice-card--active': mode.active }"
              @click="startPractice(mode.mode, null, mode)"
            >
              <div class="practice-card__icon" :class="`practice-card__icon--${mode.type}`"></div>
              <span class="practice-card__label" :class="{ 'practice-card__label--primary': mode.active }">
                {{ mode.label }}
              </span>
              <span class="practice-card__desc">{{ mode.desc }}</span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TopBar from '../components/TopBar.vue'
import directionConfig from '../data/direction.json'
import { getSubjectDetail, getKnowledgePointTree } from '../api/question.js'
import { getMyStats } from '../api/user.js'
import { getWrongBookStats } from '../api/wrongBook.js'
import { getSubjectMastery } from '../api/practice.js'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const subjectId = computed(() => route.params.id)

const subjectDetail = ref(null)
const knowledgePoints = ref([])
const stats = ref(null)
const practiceModes = ref(directionConfig.practiceModes)

const pageTitle = computed(() => {
  if (subjectDetail.value) {
    return subjectDetail.value.name || `科目 ${subjectId.value}`
  }
  return `科目 ${subjectId.value}`
})

const totalAvailable = computed(() => {
  // 从 subjectDetail.questionTypes 汇总
  const qts = subjectDetail.value?.questionTypes
  if (qts) return qts.reduce((sum, qt) => sum + (qt.count || 0), 0)
  return 0
})

const overallMastery = computed(() => {
  return subjectDetail.value?.mastery ?? 0
})

onMounted(async () => {
  try {
    const [subData, kpData, userStats, wbStats, masteryData] = await Promise.allSettled([
      getSubjectDetail(subjectId.value),
      getKnowledgePointTree(subjectId.value),
      getMyStats(),
      getWrongBookStats(),
      getSubjectMastery()
    ])

    if (subData.status === 'fulfilled') {
      subjectDetail.value = subData.value
    }

    // 合并掌握度
    if (masteryData.status === 'fulfilled' && Array.isArray(masteryData.value)) {
      const match = masteryData.value.find(m => String(m.subjectId) === String(subjectId.value))
      if (match && subjectDetail.value) {
        subjectDetail.value = { ...subjectDetail.value, mastery: match.mastery }
      }
    }

    if (kpData.status === 'fulfilled') {
      knowledgePoints.value = kpData.value || []
    } else {
      knowledgePoints.value = subjectDetail.value?.knowledgePoints || []
    }

    if (userStats.status === 'fulfilled') {
      stats.value = {
        done: userStats.value?.overallStats?.accuracy?.detail?.match(/\d+/)?.[0] || 0,
        toReview: 0
      }
    }

    if (wbStats.status === 'fulfilled') {
      if (stats.value) {
        stats.value.toReview = wbStats.value?.totalWrong ?? 0
      } else {
        stats.value = { done: 0, toReview: wbStats.value?.totalWrong ?? 0 }
      }
    }
  } catch (e) {
    console.error('科目详情加载失败:', e)
  }
  loading.value = false
})

function startPractice(mode, knowledgePointId, modeConfig) {
  const routeMap = {
    choice: '/quiz',
    'fill-blank': '/fill-blank',
    comprehensive: '/comprehensive',
    mock: '/mock-exam'
  }
  const routePath = modeConfig?.route || routeMap[modeConfig?.type] || '/quiz'
  router.push({
    path: routePath,
    query: {
      subject: subjectId.value,
      knowledgePointId: knowledgePointId,
      mode: mode
    }
  })
}
</script>

<style scoped>
.page { display: flex; width: 100%; height: 100%; background: var(--color-bg-page); overflow: hidden; }

.main { flex: 1; display: flex; flex-direction: column; background: var(--color-bg-main); padding: 16px 32px 0 32px; }

.main__toolbar { display: flex; align-items: center; justify-content: space-between; height: 48px; }
.main__title { font-size: var(--font-size-2xl); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); }
.main__stats { font-size: var(--font-size-sm); color: var(--color-text-secondary); }

/* Stat Cards */
.stat-cards { display: flex; gap: 16px; margin-top: 0; }
.stat-card { display: flex; flex-direction: column; gap: 4px; width: 160px; padding: 0; }
.stat-card__value { font-family: var(--font-primary); font-size: var(--font-size-5xl); font-weight: var(--font-weight-bold); color: var(--color-primary); }
.stat-card__label { font-family: var(--font-primary); font-size: var(--font-size-base); color: var(--color-text-secondary); }

.main__content { flex: 1; display: flex; flex-direction: column; padding-top: 20px; gap: 20px; overflow-y: auto; }

/* Knowledge Points */
.section { display: flex; flex-direction: column; gap: 12px; }
.section--modes { margin-top: 0; }
.section__title { font-size: var(--font-size-xl); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); }

.kp-card { display: flex; flex-direction: column; gap: 10px; background: var(--color-white); border: 1px solid var(--color-border);
  border-radius: var(--radius-xl); padding: 18px 24px; cursor: pointer; transition: box-shadow 150ms ease; }
.kp-card:hover { box-shadow: var(--shadow-md); }

.kp-card__header { display: flex; align-items: center; justify-content: space-between; }
.kp-card__name { font-size: var(--font-size-lg); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); }
.kp-card__child-count { font-size: var(--font-size-sm); color: var(--color-text-muted); }

.kp-card__children { display: flex; flex-wrap: wrap; gap: 8px; }
.kp-tag { display: inline-flex; align-items: center; height: 28px; padding: 0 12px;
  background: var(--color-primary-bg); color: var(--color-primary); border-radius: 14px;
  font-size: var(--font-size-xs); font-weight: var(--font-weight-medium); cursor: pointer; }
.kp-tag:hover { background: var(--color-primary); color: var(--color-white); }

/* Practice Modes */
.mode-grid { display: flex; gap: 16px; }
.practice-card { display: flex; flex-direction: column; align-items: center; justify-content: center;
  width: 180px; height: 140px; background: var(--color-white); border: 1px solid var(--color-border);
  border-radius: var(--radius-xl); gap: 8px; cursor: pointer; transition: box-shadow 150ms ease; }
.practice-card:hover { box-shadow: var(--shadow-md); }
.practice-card--active { border-color: var(--color-primary); background: var(--color-primary-bg); }

.practice-card__icon { width: 40px; height: 40px; border-radius: 12px; }
.practice-card__icon--choice { background: linear-gradient(135deg, var(--color-primary), #6ab0ff); }
.practice-card__icon--fill-blank { background: linear-gradient(135deg, var(--color-success), #7ed68a); }
.practice-card__icon--comprehensive { background: linear-gradient(135deg, #8b5cf6, #c4b5fd); }
.practice-card__icon--mock { background: linear-gradient(135deg, var(--color-warning), #ffc107); }

.practice-card__label { font-size: var(--font-size-md); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); }
.practice-card__label--primary { color: var(--color-primary); }
.practice-card__desc { font-size: var(--font-size-sm); color: var(--color-text-secondary); text-align: center; }

.empty-hint { font-size: var(--font-size-md); color: var(--color-text-muted); }
</style>
