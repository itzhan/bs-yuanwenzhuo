<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';
import { loginModuleRecord } from '@/constants/app';
import { useAppStore } from '@/store/modules/app';
import { useThemeStore } from '@/store/modules/theme';
import { $t } from '@/locales';
import PwdLogin from './modules/pwd-login.vue';
import CodeLogin from './modules/code-login.vue';
import Register from './modules/register.vue';
import ResetPwd from './modules/reset-pwd.vue';
import BindWechat from './modules/bind-wechat.vue';

interface Props {
  /** The login module */
  module?: UnionKey.LoginModule;
}

const props = defineProps<Props>();

const appStore = useAppStore();
const themeStore = useThemeStore();

interface LoginModule {
  label: App.I18n.I18nKey;
  component: Component;
}

const moduleMap: Record<UnionKey.LoginModule, LoginModule> = {
  'pwd-login': { label: loginModuleRecord['pwd-login'], component: PwdLogin },
  'code-login': { label: loginModuleRecord['code-login'], component: CodeLogin },
  register: { label: loginModuleRecord.register, component: Register },
  'reset-pwd': { label: loginModuleRecord['reset-pwd'], component: ResetPwd },
  'bind-wechat': { label: loginModuleRecord['bind-wechat'], component: BindWechat }
};

const activeModule = computed(() => moduleMap[props.module || 'pwd-login']);
</script>

<template>
  <div class="login-page">
    <!-- Left branding area -->
    <div class="login-brand">
      <div class="brand-bg">
        <div class="brand-shape shape-1"></div>
        <div class="brand-shape shape-2"></div>
        <div class="brand-shape shape-3"></div>
      </div>
      <div class="brand-content">
        <div class="brand-icon">
          <svg width="72" height="72" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4.5 12.5V20h15V12.5M12 2C6.5 2 2 5.5 2 9.5c0 3 2 5.5 5 6.5v-5h2v5.5h6v-5.5h2v5C19 15 22 12.5 22 9.5 22 5.5 17.5 2 12 2z" fill="rgba(255,255,255,0.95)"/>
            <path d="M10 9h4v1h-4zM11 8h2v3h-2z" fill="rgba(255,255,255,0.95)"/>
          </svg>
        </div>
        <h1 class="brand-title">智慧药房</h1>
        <h2 class="brand-subtitle">管理系统</h2>
        <p class="brand-desc">高效管理药品信息、库存与销售<br/>助力药房数字化转型</p>
        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>药品全生命周期管理</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>智能库存预警系统</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>采购销售一体化</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Right login form area -->
    <div class="login-form-area">
      <div class="login-form-card">
        <div class="form-header">
          <SystemLogo class="size-48px" />
          <div>
            <h3 class="form-title">欢迎回来</h3>
            <p class="form-subtitle">登录到智慧药房管理系统</p>
          </div>
        </div>
        <div class="form-body">
          <Transition :name="themeStore.page.animateMode" mode="out-in" appear>
            <component :is="activeModule.component" />
          </Transition>
        </div>
        <div class="form-footer">
          <p>© 2025-2026 智慧药房管理系统</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

/* ── Left brand panel ── */
.login-brand {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1B6B4A 0%, #0d4a30 40%, #174a35 100%);
  overflow: hidden;
  padding: 60px;
}
.brand-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.brand-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.06);
}
.shape-1 {
  width: 500px; height: 500px;
  top: -150px; right: -100px;
  animation: float 20s ease-in-out infinite;
}
.shape-2 {
  width: 300px; height: 300px;
  bottom: -80px; left: -50px;
  background: rgba(255,255,255,0.04);
  animation: float 15s ease-in-out infinite reverse;
}
.shape-3 {
  width: 180px; height: 180px;
  top: 50%; left: 60%;
  background: rgba(255,255,255,0.03);
  animation: float 18s ease-in-out infinite 3s;
}
@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(5deg); }
}

.brand-content {
  position: relative;
  z-index: 1;
  color: white;
  max-width: 420px;
}
.brand-icon {
  width: 96px;
  height: 96px;
  background: rgba(255,255,255,0.12);
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 36px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.15);
}
.brand-title {
  font-size: 44px;
  font-weight: 800;
  letter-spacing: 2px;
  margin-bottom: 4px;
  line-height: 1.2;
}
.brand-subtitle {
  font-size: 28px;
  font-weight: 300;
  opacity: 0.85;
  margin-bottom: 20px;
  letter-spacing: 6px;
}
.brand-desc {
  font-size: 15px;
  line-height: 1.8;
  opacity: 0.7;
  margin-bottom: 40px;
}
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  opacity: 0.8;
}
.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255,255,255,0.6);
  flex-shrink: 0;
}

/* ── Right form panel ── */
.login-form-area {
  flex: 0 0 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #f8faf9;
}
.login-form-card {
  width: 100%;
  max-width: 400px;
}
.form-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 40px;
}
.form-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 2px;
}
.form-subtitle {
  font-size: 13px;
  color: #888;
}
.form-body {
  margin-bottom: 40px;
}
.form-footer {
  text-align: center;
}
.form-footer p {
  font-size: 12px;
  color: #bbb;
}

/* ── Responsive ── */
@media (max-width: 960px) {
  .login-brand {
    display: none;
  }
  .login-form-area {
    flex: 1;
  }
}
</style>
