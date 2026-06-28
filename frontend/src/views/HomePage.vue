<template>
  <div v-if="loading" class="page" style="display:flex;align-items:center;justify-content:center">
    <span style="color:var(--color-text-secondary);font-size:var(--font-size-lg)">加载中...</span>
  </div>
  <div v-else class="page">
    <SideNav active-nav="home" />
    <main class="main">
      <h1 class="main__title">{{ home.title }}</h1>

      <!-- Search Bar -->
      <div class="search-bar">
        <input
          v-model="searchKeyword"
          type="text"
          class="search-bar__input"
          :placeholder="home.searchPlaceholder"
          @keyup.enter="handleSearch"
        />
      </div>

      <p class="main__subtitle">{{ home.subtitle }}</p>
      <p class="main__desc-text">{{ home.description }}</p>

      <!-- Subject Cards -->
      <div class="subject-grid">
        <div
          v-for="s in filteredSubjects"
          :key="s.id"
          class="subject-card"
          @click="goToSubject(s.id)"
        >
          <span class="subject-card__code">{{ s.code || s.id }}</span>
          <span class="subject-card__name">{{ s.name }}</span>
          <div class="subject-card__meta">
            <span v-if="s.questionTypes" class="subject-card__count">
              {{ totalQuestions(s) }} 题
            </span>
            <span v-if="s.mastery != null" class="subject-card__mastery">
              掌握度 {{ s.mastery }}%
            </span>
          </div>
        </div>
      </div>

      <p v-if="!loading && filteredSubjects.length === 0" class="empty-hint">
        暂无科目数据，请确认后端服务已启动
      </p>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import SideNav from '../components/SideNav.vue'
import homeData from '../data/home.json'
import { getSubjects } from '../api/question.js'
import { getSubjectMastery } from '../api/practice.js'

const router = useRouter()
const home = ref(homeData)
const subjects = ref([])
const loading = ref(true)
const searchKeyword = ref('')

const filteredSubjects = computed(() => {
  if (!searchKeyword.value.trim()) return subjects.value
  const kw = searchKeyword.value.trim().toLowerCase()
  return subjects.value.filter(s =>
    (s.name || '').toLowerCase().includes(kw) ||
    (s.code || '').toLowerCase().includes(kw)
  )
})

function totalQuestions(s) {
  if (!s.questionTypes) return 0
  return s.questionTypes.reduce((sum, qt) => sum + (qt.count || 0), 0)
}

function goToSubject(id) {
  router.push({ name: 'subject', params: { id } })
}

function handleSearch() {
  // 搜索已在 filteredSubjects 中实时生效，Enter 不需要额外操作
}

onMounted(async () => {
  try {
    const [subjectsData, masteryData] = await Promise.allSettled([
      getSubjects(),
      getSubjectMastery()
    ])

    // 解析 mastery 数据: { subjectId → mastery }
    const masteryMap = {}
    if (masteryData.status === 'fulfilled' && Array.isArray(masteryData.value)) {
      masteryData.value.forEach(m => { masteryMap[m.subjectId] = m.mastery })
    }

    if (subjectsData.status === 'fulfilled') {
      const data = subjectsData.value || []
      subjects.value = data.map(s => ({
        id: s.id,
        code: s.code,
        name: s.name,
        mastery: masteryMap[s.id] != null ? masteryMap[s.id] : (s.mastery || 0),
        questionTypes: s.questionTypes
      }))
    } else {
      subjects.value = []
    }
  } catch (e) {
    subjects.value = []
  }
  loading.value = false
})
</script>

<style scoped>
.page { display: flex; width: 100%; height: 100%; background: var(--color-bg-page); overflow: hidden; }

.main { flex: 1; display: flex; flex-direction: column; background: var(--color-bg-main); padding: 24px 32px 32px 32px; }

.main__title { font-size: var(--font-size-3xl); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); margin-bottom: 14px; }

.search-bar { margin-bottom: 12px; }
.search-bar__input { width: 640px; max-width: 100%; height: 44px; padding: 0 16px; background: var(--color-white);
  border: 1px solid var(--color-border); border-radius: var(--radius-lg); font-size: var(--font-size-md);
  color: var(--color-text-primary); outline: none; }
.search-bar__input::placeholder { color: var(--color-text-muted); }
.search-bar__input:focus { border-color: var(--color-primary); }

.main__subtitle { margin-bottom: 4px; font-size: var(--font-size-lg); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); }

.main__desc-text { margin-bottom: 16px; font-size: var(--font-size-md); color: var(--color-text-secondary); }

.subject-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

.subject-card { display: flex; flex-direction: column; gap: 4px; height: 116px;
  padding: 16px; background: var(--color-white); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); cursor: pointer; transition: all 150ms ease; }
.subject-card:hover { border-color: var(--color-primary); background: var(--color-primary-bg); }

.subject-card__code { font-size: var(--font-size-md); font-weight: var(--font-weight-semibold); color: var(--color-primary); }
.subject-card__name { font-size: var(--font-size-lg); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); }
.subject-card__meta { display: flex; gap: 12px; margin-top: auto; }
.subject-card__count { font-size: var(--font-size-sm); color: var(--color-text-secondary); }
.subject-card__mastery { font-size: var(--font-size-sm); color: var(--color-success); }

.empty-hint { margin-top: 24px; font-size: var(--font-size-md); color: var(--color-text-muted); }
</style>
