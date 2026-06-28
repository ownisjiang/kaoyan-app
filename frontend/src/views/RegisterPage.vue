<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-card__header">
        <h1 class="auth-card__logo">研题库</h1>
        <p class="auth-card__subtitle">创建你的冲刺刷题账号</p>
      </div>

      <form class="auth-form" @submit.prevent="handleRegister">
        <div class="form-field">
          <input
            v-model="phone"
            type="tel"
            class="form-input"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </div>

        <div class="form-field">
          <input
            v-model="nickname"
            type="text"
            class="form-input"
            placeholder="请输入昵称"
          />
        </div>

        <div class="form-field form-field--sms">
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

        <div class="form-field">
          <input
            v-model="password"
            type="password"
            class="form-input"
            placeholder="请设置密码（至少6位）"
          />
        </div>

        <div class="form-field">
          <input
            v-model="confirmPassword"
            type="password"
            class="form-input"
            placeholder="请确认密码"
          />
        </div>

        <p v-if="errorMsg" class="form-error">{{ errorMsg }}</p>

        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>

      <div class="auth-card__footer">
        <span>已有账号？</span>
        <router-link to="/login" class="auth-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { register, sendSmsCode } from '../api/auth.js'

const router = useRouter()

const phone = ref('')
const nickname = ref('')
const smsCode = ref('')
const password = ref('')
const confirmPassword = ref('')
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
    await sendSmsCode(phone.value, 'register')
    startCountdown()
  } catch (e) {
    errorMsg.value = e.message || '验证码发送失败'
  }
}

async function handleRegister() {
  if (!/^1\d{10}$/.test(phone.value)) {
    errorMsg.value = '请输入正确的手机号'
    return
  }
  if (!nickname.value.trim()) {
    errorMsg.value = '请输入昵称'
    return
  }
  if (!smsCode.value) {
    errorMsg.value = '请输入验证码'
    return
  }
  if (password.value.length < 6) {
    errorMsg.value = '密码至少6位'
    return
  }
  if (password.value !== confirmPassword.value) {
    errorMsg.value = '两次密码不一致'
    return
  }

  errorMsg.value = ''
  loading.value = true

  try {
    await register({
      phone: phone.value,
      nickname: nickname.value,
      smsCode: smsCode.value,
      password: password.value
    })
    router.replace('/')
  } catch (e) {
    errorMsg.value = e.message || '注册失败，请重试'
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
  width: 400px; padding: 36px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-lg);
}

.auth-card__header { text-align: center; margin-bottom: 24px; }
.auth-card__logo { font-size: var(--font-size-4xl); font-weight: var(--font-weight-bold); color: var(--color-primary); }
.auth-card__subtitle { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-top: 4px; }

.auth-form { width: 100%; display: flex; flex-direction: column; gap: 14px; }

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

.auth-card__footer { margin-top: 20px; font-size: var(--font-size-sm); color: var(--color-text-secondary); }
.auth-link { color: var(--color-primary); text-decoration: none; font-weight: var(--font-weight-medium); }
.auth-link:hover { text-decoration: underline; }
</style>
