<template>
  <div class="drug-detail-page">
    <div class="container">
      <n-spin :show="loading">
        <div v-if="drug" class="detail-content fade-in-up">
          <!-- Breadcrumb -->
          <n-breadcrumb separator=">">
            <n-breadcrumb-item @click="$router.push('/')">首页</n-breadcrumb-item>
            <n-breadcrumb-item @click="$router.push('/drugs')">药品查询</n-breadcrumb-item>
            <n-breadcrumb-item>{{ drug.genericName }}</n-breadcrumb-item>
          </n-breadcrumb>

          <!-- Main Info Card -->
          <div class="detail-main">
            <div class="detail-img">
              <img v-if="drug.image" :src="drug.image" :alt="drug.genericName" class="detail-photo" />
              <Pill v-else :size="64" class="detail-placeholder" />
            </div>
            <div class="detail-info">
              <div class="detail-tags">
                <n-tag v-if="drug.dosageForm" type="info" round size="small">{{ drug.dosageForm }}</n-tag>
                <n-tag v-if="drug.categoryName" type="success" round size="small">{{ drug.categoryName }}</n-tag>
                <n-tag v-if="drug.status === 1" type="primary" round size="small">
                  <template #icon><n-icon><CheckCircle :size="12" /></n-icon></template>
                  在售
                </n-tag>
                <n-tag v-else type="error" round size="small">已下架</n-tag>
              </div>
              <h1 class="detail-title">{{ drug.genericName }}</h1>
              <p class="detail-trade" v-if="drug.tradeName">商品名：{{ drug.tradeName }}</p>

              <div class="price-area">
                <span class="price-label">零售价</span>
                <span class="price-value">¥{{ drug.retailPrice?.toFixed(2) || '—' }}</span>
                <span class="price-unit">/ {{ drug.unit || '盒' }}</span>
              </div>

              <n-divider />

              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label"><Hash :size="12" /> 药品编码</span>
                  <span class="info-value">{{ drug.drugCode }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label"><Ruler :size="12" /> 规格</span>
                  <span class="info-value">{{ drug.specification || '—' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label"><Pill :size="12" /> 剂型</span>
                  <span class="info-value">{{ drug.dosageForm || '—' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label"><PackageIcon :size="12" /> 单位</span>
                  <span class="info-value">{{ drug.unit || '—' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label"><FactoryIcon :size="12" /> 生产厂家</span>
                  <span class="info-value">{{ drug.manufacturer || '—' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label"><FileCheck :size="12" /> 批准文号</span>
                  <span class="info-value">{{ drug.approvalNumber || '—' }}</span>
                </div>
                <div class="info-item" v-if="drug.barcode">
                  <span class="info-label"><Barcode :size="12" /> 条形码</span>
                  <span class="info-value">{{ drug.barcode }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Description -->
          <div class="detail-desc" v-if="drug.description">
            <h2><FileText :size="18" /> 药品说明</h2>
            <div class="desc-content">{{ drug.description }}</div>
          </div>

          <!-- Back -->
          <div class="detail-actions">
            <n-button @click="$router.back()" size="large">
              <template #icon><n-icon><ArrowLeft :size="16" /></n-icon></template>
              返回列表
            </n-button>
          </div>
        </div>
      </n-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  Pill, CheckCircle, Hash, Ruler, Factory as FactoryIcon,
  Package as PackageIcon, FileCheck, Barcode, FileText, ArrowLeft
} from 'lucide-vue-next'
import { getPublicDrugDetail } from '../api'

const route = useRoute()
const drug = ref<any>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    const res: any = await getPublicDrugDetail(id)
    drug.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.drug-detail-page { padding: 32px 0 64px; }

.detail-main {
  display: flex;
  gap: 40px;
  margin-top: 24px;
  background: var(--surface-card);
  border-radius: var(--radius-lg);
  padding: 36px;
  box-shadow: var(--shadow-md);
}
.detail-img {
  width: 320px;
  min-height: 280px;
  background: linear-gradient(135deg, #F0FDF4, #ECFDF5);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}
.detail-photo { width: 100%; height: 100%; object-fit: cover; }
.detail-placeholder { color: var(--text-muted); opacity: 0.35; }

.detail-info { flex: 1; }
.detail-tags { display: flex; gap: 8px; margin-bottom: 12px; }
.detail-title { font-size: 26px; font-weight: 700; margin-bottom: 4px; }
.detail-trade { font-size: 14px; color: var(--text-secondary); margin-bottom: 16px; }

.price-area { display: flex; align-items: baseline; gap: 8px; margin-bottom: 8px; }
.price-label { font-size: 13px; color: var(--text-muted); }
.price-value { font-size: 32px; font-weight: 800; color: var(--accent); }
.price-unit { font-size: 14px; color: var(--text-muted); }

.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; color: var(--text-muted); font-weight: 500;
}
.info-value { font-size: 14px; color: var(--text-primary); }

.detail-desc {
  margin-top: 28px;
  background: var(--surface-card);
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}
.detail-desc h2 {
  display: flex; align-items: center; gap: 8px;
  font-size: 18px; font-weight: 600; margin-bottom: 16px;
}
.desc-content {
  font-size: 14px; color: var(--text-secondary);
  line-height: 1.8; white-space: pre-wrap;
}
.detail-actions { margin-top: 28px; }

@media (max-width: 768px) {
  .detail-main { flex-direction: column; }
  .detail-img { width: 100%; min-height: 200px; }
  .info-grid { grid-template-columns: 1fr; }
}
</style>
