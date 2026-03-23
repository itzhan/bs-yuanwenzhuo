<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
    </div>
    <div class="login-container">
      <div class="login-card fade-in-up">
        <div class="login-header">
          <div class="login-logo"><Pill :size="44" color="#1B6B4A" /></div>
          <h1>智慧药房</h1>
          <p>药品信息管理系统</p>
        </div>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" size="large">
          <n-form-item path="username" label="">
            <n-input v-model:value="formData.username" placeholder="请输入用户名">
              <template #prefix><n-icon><UserIcon :size="16" /></n-icon></template>
            </n-input>
          </n-form-item>
          <n-form-item path="password" label="">
            <n-input v-model:value="formData.password" type="password" show-password-on="click" placeholder="请输入密码" @keydown.enter="handleLogin">
              <template #prefix><n-icon><Lock :size="16" /></n-icon></template>
            </n-input>
          </n-form-item>
          <n-button type="primary" block size="large" :loading="loading" @click="handleLogin"
            style="margin-top:8px;border-radius:10px;height:48px;font-size:16px;">
            <template #icon><n-icon><LogIn :size="16" /></n-icon></template>
            登 录
          </n-button>
        </n-form>
        <div class="login-footer">
          <router-link to="/"><ArrowLeft :size="14" /> 返回首页</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { Pill, User as UserIcon, Lock, LogIn, ArrowLeft } from 'lucide-vue-next'
import { login } from '../api'

const router = useRouter()
const message = useMessage()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const formData = ref({ username: '', password: '' })
const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

async function handleLogin() {
  try { await formRef.value?.validate() } catch { return }
  loading.value = true
  try {
    const res: any = await login(formData.value)
    if (res.data?.token) {
      localStorage.setItem('token', res.data.token)
      message.success('登录成功')
      router.push('/')
    } else { message.error('登录失败') }
  } catch (e: any) { message.error(e.message || '登录失败')
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-page{min-height:100vh;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#f8faf9 0%,#eef5f0 50%,#fef7f0 100%);position:relative;overflow:hidden}
.login-bg{position:absolute;inset:0;pointer-events:none}
.bg-shape{position:absolute;border-radius:50%;opacity:.06}
.bg-shape-1{width:600px;height:600px;background:var(--primary);top:-200px;right:-150px}
.bg-shape-2{width:400px;height:400px;background:var(--accent);bottom:-150px;left:-100px}
.login-container{position:relative;z-index:1;width:100%;max-width:420px;padding:0 24px}
.login-card{background:var(--surface-card);border-radius:var(--radius-lg);padding:48px 36px 36px;box-shadow:var(--shadow-lg)}
.login-header{text-align:center;margin-bottom:36px}
.login-logo{display:flex;justify-content:center;margin-bottom:12px}
.login-header h1{font-size:24px;font-weight:700;color:var(--primary);margin-bottom:4px}
.login-header p{font-size:13px;color:var(--text-muted)}
.login-footer{text-align:center;margin-top:20px}
.login-footer a{display:inline-flex;align-items:center;gap:4px;font-size:13px;color:var(--text-muted)}
.login-footer a:hover{color:var(--primary)}
</style>
