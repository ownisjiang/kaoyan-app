<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-card__header">
        <h1 class="auth-card__logo">研题库</h1>
        <p class="auth-card__subtitle">冲刺刷题平台</p>
      </div>

      <div class="auth-tabs">
        <button
          class="auth-tabs__btn"
          :class="{ 'auth-tabs__btn--active': mode === 'password' }"
          @click="mode = 'password'"
        >密码登录</button>
        <button
          class="auth-tabs__btn"
          :class="{ 'auth-tabs__btn--active': mode === 'sms' }"
          @click="mode = 'sms'"
        >验证码登录</button>
      </div>

      <form class="auth-form" @submit.prevent="handleLogin">
        <div class="form-field">
          <input
            v-model="phone"
            type="tel"
            class="form-input"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </div>

        <div v-if="mode === 'password'" class="form-field">
          <input
            v-model="password"
            type="password"
            class="form-input"
            placeholder="请输入密码"
          />
        </div>

        <div v-else class="form-field form-field--sms">
          <input
            v-model="smsCode"
            type="text"
            class="form-input"
            placeholder="请输入验证码"
            maxlength="6"
          />
          <button
            type="button"
            class="sms-btn"
            :disabled="countdown > 0"
            @click="handleSendSms"
          >
            <span v-if="countdown > 0">{{ countdown }}s</span>
            <span v-else>获取验证码</span>
          </button>
        </div>

        <p v-if="errorMsg" class="form-error">{{ errorMsg }}</p>

        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <div class="auth-card__footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="auth-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login, sendSmsCode } from '../api/auth.js'

const router = useRouter()
const route = useRoute()

const mode = ref('password')
const phone = ref('')
const password = ref('')
const smsCode = ref('')
const errorMsg = ref('')
const loading = ref(false)
const countdown = ref(0)
let timer = null

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

async function handleSendSms() {
  if (!/^1\d{10}$/.test(phone.value)) {
    errorMsg.value = '请输入正确的手机号'
    return
  }
  errorMsg.value = ''
  try {
    await sendSmsCode(phone.value, 'login')
    startCountdown()
  } catch (e) {
    errorMsg.value = e.message || '验证码发送失败'
  }
}

async function handleLogin() {
  if (!/^1\d{10}$/.test(phone.value)) {
    errorMsg.value = '请输入正确的手机号'
    return
  }
  if (mode.value === 'password' && !password.value) {
    errorMsg.value = '请输入密码'
    return
  }
  if (mode.value === 'sms' && !smsCode.value) {
    errorMsg.value = '请输入验证码'
    return
  }

  errorMsg.value = ''
  loading.value = true

  try {
    const params = mode.value === 'password'
      ? { phone: phone.value, password: password.value }
      : { phone: phone.value, smsCode: smsCode.value }
    await login(params)
    const redirect = route.query.redirect || '/'
    router.replace(redirect)
  } catch (e) {
    errorMsg.value = e.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex; align-items: center; justify-content: center;
  width: 100%; height: 100%;
  background: var(--color-bg-page);
}

.auth-card {
  display: flex; flex-direction: column; align-items: center;
  width: 400px; padding: 40px 36px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-lg);
}

.auth-card__header { text-align: center; margin-bottom: 28px; }
.auth-card__logo { font-size: var(--font-size-4xl); font-weight: var(--font-weight-bold); color: var(--color-primary); }
.auth-card__subtitle { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-top: 4px; }

.auth-tabs { display: flex; gap: 8px; margin-bottom: 24px; }
.auth-tabs__btn {
  padding: 6px 16px; border: none; background: transparent;
  font-size: var(--font-size-md); font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary); cursor: pointer;
  border-bottom: 2px solid transparent; transition: all 150ms ease;
}
.auth-tabs__btn--active { color: var(--color-primary); border-bottom-color: var(--color-primary); }

.auth-form { width: 100%; display: flex; flex-direction: column; gap: 16px; }

.form-field { display: flex; flex-direction: column; }
.form-field--sms { flex-direction: row; gap: 8px; }
.form-field--sms .form-input { flex: 1; }

.form-input {
  height: 44px; padding: 0 16px;
  background: var(--color-bg-page);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  font-size: var(--font-size-md); color: var(--color-text-primary);
  outline: none; transition: border-color 150ms ease;
}
.form-input:focus { border-color: var(--color-primary); }
.form-input::placeholder { color: var(--color-text-muted); }

.sms-btn {
  flex-shrink: 0; width: 120px; height: 44px;
  background: var(--color-primary-bg); border: 1px solid var(--color-primary-light);
  border-radius: var(--radius-lg);
  font-size: var(--font-size-sm); font-weight: var(--font-weight-medium);
  color: var(--color-primary); cursor: pointer; transition: all 150ms ease;
}
.sms-btn:hover:not(:disabled) { background: var(--color-primary); color: var(--color-white); }
.sms-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.form-error { font-size: var(--font-size-sm); color: var(--color-error); }

.submit-btn {
  height: 44px; margin-top: 8px;
  background: var(--color-primary); color: var(--color-white);
  border: none; border-radius: 22px;
  font-size: var(--font-size-md); font-weight: var(--font-weight-semibold);
  cursor: pointer; transition: box-shadow 150ms ease;
}
.submit-btn:hover:not(:disabled) { box-shadow: var(--shadow-md); }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.auth-card__footer { margin-top: 24px; font-size: var(--font-size-sm); color: var(--color-text-secondary); }
.auth-link { color: var(--color-primary); text-decoration: none; font-weight: var(--font-weight-medium); }
.auth-link:hover { text-decoration: underline; }
</style>
