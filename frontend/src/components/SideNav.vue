<template>
  <nav v-if="!ready" class="side-nav" style="justify-content:center;align-items:center">
    <span style="color:var(--color-text-muted)">加载中...</span>
  </nav>
  <nav v-else class="side-nav">
    <div class="side-nav__brand">
      <span class="side-nav__logo">{{ sidebar.brand.name }}</span>
      <span class="side-nav__subtitle">{{ sidebar.brand.subtitle }}</span>
    </div>

    <div class="side-nav__divider"></div>

    <router-link
      v-for="item in sidebar.navItems"
      :key="item.key"
      :to="item.to"
      class="side-nav__item"
      :class="{ 'side-nav__item--active': activeNav === item.key }"
    >
      <span>{{ item.label }}</span>
    </router-link>

    <div class="side-nav__spacer"></div>

    <router-link v-if="isAdminUser" to="/admin" class="side-nav__item" :class="{ 'side-nav__item--active': activeNav === 'admin' }">
      <span>⚙️ 题库管理</span>
    </router-link>

    <router-link :to="sidebar.userRoute" class="side-nav__user">
      <div class="side-nav__avatar">{{ userInitial }}</div>
      <span class="side-nav__username">{{ userNickname }}</span>
    </router-link>

    <button class="side-nav__logout" @click="doLogout">退出登录</button>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import sidebarData from '../data/sidebar.json'
import { getMyProfile } from '../api/user.js'
import { getLocalUser, clearAuth, isAdmin } from '../utils/auth.js'

const router = useRouter()
const sidebar = ref(sidebarData)
const profile = ref(null)
const ready = ref(false)

defineProps({
  activeNav: String
})

const userInitial = computed(() => {
  if (profile.value?.nickname) return profile.value.nickname[0]
  const local = getLocalUser()
  return local?.nickname?.[0] || '?'
})

const userNickname = computed(() => {
  return profile.value?.nickname || getLocalUser()?.nickname || '用户'
})

const isAdminUser = computed(() => isAdmin())

onMounted(async () => {
  try {
    profile.value = await getMyProfile()
  } catch {
    profile.value = getLocalUser()
  }
  ready.value = true
})

function doLogout() {
  clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.side-nav {
  width: 256px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-sidebar);
  padding: 20px;
  gap: 8px;
  flex-shrink: 0;
}

.side-nav__brand {
  display: flex;
  flex-direction: column;
  padding: 0;
}

.side-nav__logo {
  font-family: var(--font-primary);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  line-height: 1.2;
}

.side-nav__subtitle {
  font-family: var(--font-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-regular);
  color: var(--color-text-secondary);
  margin-top: 2px;
}

.side-nav__divider {
  width: 216px;
  height: 1px;
  background: var(--color-border);
  margin: 4px 0;
}

.side-nav__item {
  display: flex;
  align-items: center;
  width: 216px;
  height: 40px;
  padding: 0 16px;
  border-radius: var(--radius-3xl);
  gap: var(--space-3);
  font-family: var(--font-primary);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: all 150ms ease;
}

.side-nav__item:hover {
  background: rgba(1, 77, 178, 0.06);
  color: var(--color-primary);
}

.side-nav__item--active {
  background: var(--color-primary);
  color: var(--color-white);
}

.side-nav__item--active:hover {
  background: var(--color-primary);
  color: var(--color-white);
}

.side-nav__spacer {
  flex: 1;
}

.side-nav__user {
  display: flex;
  align-items: center;
  width: 216px;
  height: 48px;
  padding: 8px;
  border-radius: var(--radius-lg);
  gap: var(--space-3);
  text-decoration: none;
  transition: background 150ms ease;
}

.side-nav__user:hover {
  background: rgba(255, 255, 255, 0.5);
}

.side-nav__avatar {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary);
  color: var(--color-white);
  font-family: var(--font-primary);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  border-radius: 50%;
  flex-shrink: 0;
}

.side-nav__username {
  font-family: var(--font-primary);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.side-nav__logout {
  display: flex; align-items: center; width: 216px; height: 36px; padding: 0 16px;
  border: 1px solid var(--color-border); border-radius: var(--radius-xl);
  background: transparent; color: var(--color-text-muted); cursor: pointer;
  font-size: var(--font-size-sm); font-family: var(--font-primary);
  transition: all 150ms ease; margin-top: 4px;
}
.side-nav__logout:hover { border-color: var(--color-error); color: var(--color-error); background: var(--color-error-light); }
</style>
