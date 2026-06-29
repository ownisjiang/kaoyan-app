<template>
  <div class="page">
    <header class="header">
      <h1>题库管理</h1>
      <span class="subtitle">管理员专用 · 添加题目</span>
    </header>

    <div class="form-card">
      <div class="form-row">
        <label class="label">科目 <span class="required">*</span></label>
        <select v-model="form.subjectId" class="input">
          <option :value="null" disabled>请选择科目</option>
          <option v-for="s in subjects" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>

      <div class="form-row">
        <label class="label">题型 <span class="required">*</span></label>
        <div class="radio-group">
          <label v-for="t in types" :key="t.value" class="radio" :class="{ active: form.questionType === t.value }">
            <input type="radio" v-model="form.questionType" :value="t.value" />
            {{ t.label }}
          </label>
        </div>
      </div>

      <div class="form-row cols-2">
        <div>
          <label class="label">难度</label>
          <select v-model="form.difficulty" class="input">
            <option :value="null">不限</option>
            <option :value="1">1 - 简单</option>
            <option :value="2">2 - 中等</option>
            <option :value="3">3 - 困难</option>
            <option :value="4">4</option>
            <option :value="5">5</option>
          </select>
        </div>
        <div>
          <label class="label">真题年份</label>
          <select v-model="form.examYear" class="input">
            <option :value="null">不限</option>
            <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
          </select>
        </div>
      </div>

      <div class="form-row">
        <label class="label">来源</label>
        <input v-model="form.source" class="input" placeholder="如：2024年全国硕士研究生入学统一考试" />
      </div>

      <div class="form-row">
        <label class="label">题目内容 <span class="required">*</span></label>
        <textarea v-model="form.content" class="textarea" rows="4" placeholder="输入题目内容。填空题用 ___ 表示空位"></textarea>
      </div>

      <!-- 选择题选项 -->
      <div v-if="form.questionType === 1" class="form-row">
        <label class="label">选项 <span class="required">*</span></label>
        <div class="options-edit">
          <div v-for="(opt, i) in optionList" :key="i" class="opt-line">
            <span class="opt-key">{{ String.fromCharCode(65 + i) }}.</span>
            <input v-model="optionList[i]" class="input opt-input" :placeholder="'选项 ' + String.fromCharCode(65 + i)" />
            <button v-if="optionList.length > 2" class="btn-sm btn-danger" @click="optionList.splice(i,1)">×</button>
          </div>
          <button class="btn-sm" @click="optionList.push('')">+ 添加选项</button>
        </div>
      </div>

      <div class="form-row">
        <label class="label">参考答案 <span class="required">*</span></label>
        <input v-if="form.questionType === 1" v-model="form.answer" class="input" placeholder="如：A" />
        <textarea v-else v-model="form.answer" class="textarea" rows="3" placeholder='输入参考答案。填空题：JSON格式 {"1":"答案1","2":"答案2"}'></textarea>
      </div>

      <div class="form-row">
        <label class="label">解析</label>
        <textarea v-model="form.analysis" class="textarea" rows="3" placeholder="题目解析（可选）"></textarea>
      </div>

      <div class="form-row">
        <label class="label">标签</label>
        <input v-model="form.tags" class="input" placeholder="逗号分隔，如：二叉树,遍历" />
      </div>

      <div class="form-actions">
        <span v-if="msg" :class="msgType">{{ msg }}</span>
        <button class="btn btn--primary" :disabled="!valid || saving" @click="doSave">
          {{ saving ? '保存中...' : '添加题目' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getSubjects } from '../api/question.js'
import { createQuestion } from '../api/question.js'

const subjects = ref([])
const optionList = reactive(['', '', '', ''])
const saving = ref(false)
const msg = ref('')
const msgType = ref('')

const types = [
  { value: 1, label: '选择题' },
  { value: 2, label: '填空题' },
  { value: 3, label: '综合题' },
]

const years = Array.from({ length: 15 }, (_, i) => 2026 - i)

const form = reactive({
  subjectId: null,
  questionType: 1,
  difficulty: null,
  examYear: null,
  source: '',
  content: '',
  answer: '',
  analysis: '',
  tags: '',
})

const valid = computed(() => form.subjectId && form.questionType && form.content.trim() && form.answer.trim())

async function doSave() {
  if (!valid.value || saving.value) return
  saving.value = true; msg.value = ''
  try {
    const payload = { ...form }
    if (form.questionType === 1) {
      payload.options = JSON.stringify(optionList.filter(o => o.trim()).map((o, i) => `${String.fromCharCode(65 + i)}. ${o.trim()}`))
    }
    const id = await createQuestion(payload)
    msg.value = '✅ 题目添加成功！ID: ' + (id?.data ?? id)
    msgType.value = 'success'
    form.content = ''; form.answer = ''; form.analysis = ''; form.tags = ''; form.source = ''
    optionList.length = 0; optionList.push('', '', '', '')
  } catch (e) {
    msg.value = '❌ 添加失败: ' + (e.message || '请检查网络')
    msgType.value = 'error'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getSubjects()
    subjects.value = Array.isArray(res) ? res : (res?.data ?? [])
  } catch { /* ignore */ }
})
</script>

<style scoped>
.page { max-width: 720px; margin: 0 auto; padding: 32px 24px 64px; }
.header { margin-bottom: 28px; }
.header h1 { font-size: 24px; font-weight: 700; color: var(--color-text-primary); margin: 0 0 4px; }
.subtitle { font-size: 14px; color: var(--color-text-muted); }

.form-card { background: var(--color-white); border: 1px solid var(--color-border); border-radius: 14px; padding: 28px; display: flex; flex-direction: column; gap: 20px; }

.form-row { display: flex; flex-direction: column; gap: 6px; }
.form-row.cols-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.label { font-size: 14px; font-weight: 600; color: var(--color-text-primary); }
.required { color: var(--color-error); }

.input, .textarea { width: 100%; padding: 10px 14px; border: 1px solid var(--color-border); border-radius: 8px; font-size: 14px; font-family: inherit; color: var(--color-text-primary); background: var(--color-bg-page); outline: none; box-sizing: border-box; }
.input:focus, .textarea:focus { border-color: var(--color-primary); }

.radio-group { display: flex; gap: 8px; }
.radio { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border: 1px solid var(--color-border); border-radius: 8px; cursor: pointer; font-size: 14px; color: var(--color-text-secondary); }
.radio.active { border-color: var(--color-primary); color: var(--color-primary); background: var(--color-primary-bg); }
.radio input { display: none; }

.options-edit { display: flex; flex-direction: column; gap: 8px; }
.opt-line { display: flex; align-items: center; gap: 8px; }
.opt-key { width: 24px; font-weight: 600; font-size: 14px; color: var(--color-text-secondary); text-align: right; }
.opt-input { flex: 1; }

.btn-sm { display: inline-flex; align-items: center; height: 32px; padding: 0 12px; border: 1px solid var(--color-border); border-radius: 6px; font-size: 13px; color: var(--color-text-secondary); background: var(--color-white); cursor: pointer; }
.btn-sm:hover { border-color: var(--color-primary); color: var(--color-primary); }
.btn-danger { color: var(--color-error); border-color: var(--color-error-light); }
.btn-danger:hover { background: var(--color-error-light); }

.form-actions { display: flex; align-items: center; justify-content: flex-end; gap: 12px; padding-top: 8px; }
.success { color: var(--color-success); font-size: 14px; }
.error { color: var(--color-error); font-size: 14px; }

.btn { display: inline-flex; align-items: center; justify-content: center; height: 44px; padding: 0 24px; border-radius: 22px; font-size: 15px; font-weight: 600; cursor: pointer; border: none; }
.btn--primary { background: var(--color-primary); color: #fff; }
.btn--primary:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
