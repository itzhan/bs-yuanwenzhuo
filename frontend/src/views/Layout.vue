<template>
  <div class="layout">
    <!-- Header -->
    <header class="header">
      <div class="header-inner container">
        <router-link to="/" class="logo">
          <div class="logo-icon"><Pill :size="26" :stroke-width="2.2" /></div>
          <div class="logo-text">
            <span class="logo-title">智慧药房</span>
            <span class="logo-subtitle">药品信息查询平台</span>
          </div>
        </router-link>
        <nav class="nav">
          <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">
            <Home :size="16" /> 首页
          </router-link>
          <router-link to="/drugs" class="nav-link" active-class="active">
            <Search :size="16" /> 药品查询
          </router-link>
          <router-link to="/about" class="nav-link" active-class="active">
            <Info :size="16" /> 关于我们
          </router-link>
        </nav>
        <div class="header-actions">
          <template v-if="isLoggedIn">
            <n-dropdown :options="userMenuOptions" @select="handleUserMenu">
              <n-button quaternary>
                <template #icon><n-icon><UserRound :size="18" /></n-icon></template>
                {{ userInfo?.realName || userInfo?.username || '用户' }}
              </n-button>
            </n-dropdown>
          </template>
          <template v-else>
            <router-link to="/login">
              <n-button type="primary" ghost size="medium">
                <template #icon><n-icon><LogIn :size="16" /></n-icon></template>
                登录
              </n-button>
            </router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- Main -->
    <main class="main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- Footer -->
    <footer class="footer">
      <div class="container footer-inner">
        <div class="footer-brand">
          <span class="footer-logo"><Pill :size="20" /> 智慧药房</span>
          <p class="footer-desc">基于 Spring Boot 的药店药品信息管理系统</p>
        </div>
        <div class="footer-links">
          <router-link to="/">首页</router-link>
          <router-link to="/drugs">药品查询</router-link>
          <router-link to="/about">关于我们</router-link>
        </div>
        <div class="footer-copy">
          <p>© 2026 药店药品信息管理系统 · 毕业设计</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { Pill, Home, Search, Info, LogIn, UserRound, LogOut } from 'lucide-vue-next'
import { getUserInfo } from '../api'

const router = useRouter()
const userInfo = ref<any>(null)
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const userMenuOptions = [
  { label: '退出登录', key: 'logout', icon: () => h(NIcon, null, { default: () => h(LogOut, { size: 16 }) }) }
]

function handleUserMenu(key: string) {
  if (key === 'logout') {
    localStorage.removeItem('token')
    userInfo.value = null
    router.push('/')
    window.location.reload()
  }
}

onMounted(async () => {
  if (isLoggedIn.value) {
    try {
      const res: any = await getUserInfo()
      userInfo.value = res.data
    } catch {
      localStorage.removeItem('token')
    }
  }
})
</script>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* ── Header ── */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
}
.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--text-primary);
}
.logo-icon {
  color: var(--primary);
  display: flex;
  align-items: center;
}
.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.logo-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary);
}
.logo-subtitle {
  font-size: 11px;
  color: var(--text-muted);
  letter-spacing: 0.5px;
}
.nav {
  display: flex;
  gap: 8px;
}
.nav-link {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: var(--transition);
}
.nav-link:hover {
  color: var(--primary);
  background: rgba(27, 107, 74, 0.06);
}
.nav-link.active {
  color: var(--primary);
  background: rgba(27, 107, 74, 0.1);
}

/* ── Main ── */
.main {
  flex: 1;
}

/* ── Footer ── */
.footer {
  background: var(--text-primary);
  color: rgba(255, 255, 255, 0.7);
  padding: 48px 0 32px;
  margin-top: 80px;
}
.footer-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  text-align: center;
}
.footer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
}
.footer-desc {
  font-size: 13px;
  margin-top: 4px;
}
.footer-links {
  display: flex;
  gap: 24px;
}
.footer-links a {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}
.footer-links a:hover {
  color: #fff;
}
.footer-copy {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}
</style>
