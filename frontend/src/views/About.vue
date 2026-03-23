<template>
  <div class="about-page">
    <section class="about-hero">
      <div class="container">
        <h1 class="fade-in-up">关于我们</h1>
        <p class="fade-in-up">药店药品信息管理系统 — 毕业设计作品</p>
      </div>
    </section>
    <section class="about-content">
      <div class="container">
        <div class="about-grid">
          <div class="about-card fade-in-up">
            <h2><ClipboardList :size="20" /> 项目简介</h2>
            <p>本系统是一个基于 Spring Boot + Vue 3 的药店药品信息管理系统，旨在为药店提供一套完整的药品信息管理解决方案。系统涵盖药品管理、库存管理、采购管理、销售管理、效期预警等核心业务模块。</p>
          </div>
          <div class="about-card fade-in-up">
            <h2><Server :size="20" /> 技术架构</h2>
            <div class="tech-list">
              <div class="tech-item"><span class="tech-label">后端框架</span><span class="tech-value">Spring Boot 3.2.5</span></div>
              <div class="tech-item"><span class="tech-label">ORM 框架</span><span class="tech-value">MyBatis-Plus 3.5.6</span></div>
              <div class="tech-item"><span class="tech-label">数据库</span><span class="tech-value">MySQL 8.0</span></div>
              <div class="tech-item"><span class="tech-label">认证方式</span><span class="tech-value">JWT Token</span></div>
              <div class="tech-item"><span class="tech-label">管理端前端</span><span class="tech-value">SoybeanAdmin + Naive UI</span></div>
              <div class="tech-item"><span class="tech-label">用户端前端</span><span class="tech-value">Vue 3 + Vite + Naive UI</span></div>
            </div>
          </div>
          <div class="about-card fade-in-up">
            <h2><LayoutGrid :size="20" /> 功能模块</h2>
            <div class="module-grid">
              <div class="module-item"><Pill :size="16" /> 药品信息管理</div>
              <div class="module-item"><FolderTree :size="16" /> 药品分类管理</div>
              <div class="module-item"><Factory :size="16" /> 供应商管理</div>
              <div class="module-item"><Package :size="16" /> 库存管理</div>
              <div class="module-item"><ShoppingCart :size="16" /> 采购订单管理</div>
              <div class="module-item"><BadgeDollarSign :size="16" /> 销售记录管理</div>
              <div class="module-item"><Clock :size="16" /> 效期预警</div>
              <div class="module-item"><BarChart3 :size="16" /> 数据统计</div>
              <div class="module-item"><Users :size="16" /> 用户权限管理</div>
              <div class="module-item"><FileText :size="16" /> 操作日志</div>
            </div>
          </div>
          <div class="about-card fade-in-up">
            <h2><UserRound :size="20" /> 作者信息</h2>
            <p>本系统为计算机科学与技术专业毕业设计作品。</p>
            <p style="margin-top:8px;color:var(--text-muted);font-size:13px">© 2026 药店药品信息管理系统 · 版权所有</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import {
  ClipboardList, Server, LayoutGrid, Pill, FolderTree, Factory, Package,
  ShoppingCart, BadgeDollarSign, Clock, BarChart3, Users, FileText, UserRound
} from 'lucide-vue-next'
</script>

<style scoped>
.about-hero{padding:64px 0 48px;background:linear-gradient(135deg,#F0FDF4 0%,#FFF7ED 100%);text-align:center}
.about-hero h1{font-size:32px;font-weight:700;margin-bottom:8px}
.about-hero p{color:var(--text-secondary);font-size:15px}
.about-content{padding:48px 0 64px}
.about-grid{display:flex;flex-direction:column;gap:24px}
.about-card{background:var(--surface-card);border-radius:var(--radius-lg);padding:32px;box-shadow:var(--shadow-sm)}
.about-card h2{display:flex;align-items:center;gap:8px;font-size:18px;font-weight:600;margin-bottom:16px}
.about-card p{font-size:14px;color:var(--text-secondary);line-height:1.8}
.tech-list{display:grid;grid-template-columns:repeat(2,1fr);gap:12px}
.tech-item{display:flex;justify-content:space-between;padding:12px 16px;background:#f8f9fa;border-radius:var(--radius-sm)}
.tech-label{font-size:13px;color:var(--text-muted)}
.tech-value{font-size:13px;font-weight:600;color:var(--primary)}
.module-grid{display:grid;grid-template-columns:repeat(5,1fr);gap:12px}
.module-item{display:flex;align-items:center;gap:6px;padding:16px 12px;background:#f8f9fa;border-radius:var(--radius-sm);font-size:13px;font-weight:500;transition:var(--transition)}
.module-item:hover{background:rgba(27,107,74,.08);color:var(--primary)}
@media(max-width:768px){.tech-list{grid-template-columns:1fr}.module-grid{grid-template-columns:repeat(2,1fr)}}
</style>
