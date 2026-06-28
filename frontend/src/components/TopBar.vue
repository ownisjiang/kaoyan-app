<template>
  <header class="topbar">
    <button class="topbar__back" @click="goBack">
      <span>{{ backText }}</span>
    </button>
    <router-link to="/profile" class="topbar__user">
      <div class="topbar__avatar">{{ userInitial }}</div>
      <span class="topbar__username">{{ userNickname }}</span>
    </router-link>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyProfile } from '../api/user.js'
import { getLocalUser } from '../utils/auth.js'

const router = useRouter()
const profile = ref(null)

const props = defineProps({
  backTo: { type: String, default: '/' },
  backText: { type: String, default: '\u2190 返回首页' }
})

function goBack() {
  router.push(props.backTo)
}

const userInitial = computed(() => {
  if (profile.value?.nickname) return profile.value.nickname[0]
  const local = getLocalUser()
  return local?.nickname?.[0] || '?'
})

const userNickname = computed(() => {
  return profile.value?.nickname || getLocalUser()?.nickname || '用户'
})

onMounted(async () => {
  try {
    profile.value = await getMyProfile()
  } catch {
    profile.value = getLocalUser()
  }
})
</script>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  flex-shrink: 0;
}

.topbar__back {
  display: flex;
  align-items: center;
  height: 32px;
  padding: 0 14px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: var(--color-white);
  font-family: var(--font-primary);
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 150ms ease;
}

.topbar__back:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-bg);
}

.topbar__user {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  padding: 4px 12px 4px 4px;
  border-radius: 20px;
  transition: background 150ms ease;
}

.topbar__user:hover {
  background: rgba(1, 77, 178, 0.06);
}

.topbar__avatar {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary);
  color: var(--color-white);
  font-family: var(--font-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  border-radius: 50%;
  flex-shrink: 0;
}

.topbar__username {
  font-family: var(--font-primary);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}
</style>
