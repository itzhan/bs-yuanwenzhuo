<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1"></div>
        <div class="hero-shape hero-shape-2"></div>
        <div class="hero-shape hero-shape-3"></div>
      </div>
      <div class="container hero-content">
        <div class="hero-text fade-in-up">
          <div class="hero-badge"><Hospital :size="14" /> 专业药品查询平台</div>
          <h1>智慧药房<br/><span class="hero-highlight">药品信息管理系统</span></h1>
          <p class="hero-desc">提供全面的药品信息查询服务，涵盖药品分类、规格、厂家、价格等详细信息，助力科学用药。</p>
          <div class="hero-actions">
            <router-link to="/drugs">
              <n-button type="primary" size="large" round>
                <template #icon><n-icon><SearchIcon :size="16" /></n-icon></template>
                开始查询药品
              </n-button>
            </router-link>
            <router-link to="/about">
              <n-button size="large" ghost round>了解更多</n-button>
            </router-link>
          </div>
        </div>
        <div class="hero-visual fade-in">
          <div class="hero-card hero-card-1">
            <div class="hc-icon"><Pill :size="28" color="#1B6B4A" /></div>
            <div class="hc-info">
              <span class="hc-label">药品总数</span>
              <span class="hc-value">{{ stats.drugCount || 0 }}</span>
            </div>
          </div>
          <div class="hero-card hero-card-2">
            <div class="hc-icon"><FolderTree :size="28" color="#E07A5F" /></div>
            <div class="hc-info">
              <span class="hc-label">药品分类</span>
              <span class="hc-value">{{ stats.categoryCount || 0 }}</span>
            </div>
          </div>
          <div class="hero-card hero-card-3">
            <div class="hc-icon"><Factory :size="28" color="#3B82F6" /></div>
            <div class="hc-info">
              <span class="hc-label">合作供应商</span>
              <span class="hc-value">{{ stats.supplierCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Category Section -->
    <section class="section categories-section">
      <div class="container">
        <div class="section-header fade-in-up">
          <h2>药品分类</h2>
          <p>按分类快速查找所需药品</p>
        </div>
        <div class="category-grid">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="category-card fade-in-up"
            @click="goCategory(cat.id)"
          >
            <div class="cat-icon">
              <component :is="getCategoryIcon(cat.name)" :size="32" />
            </div>
            <h3>{{ cat.name }}</h3>
            <p>{{ cat.children?.length || 0 }} 个子分类</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Hot Drugs Section -->
    <section class="section drugs-section">
      <div class="container">
        <div class="section-header fade-in-up">
          <h2>热门药品</h2>
          <router-link to="/drugs" class="see-all">
            查看全部 <ArrowRight :size="14" />
          </router-link>
        </div>
        <div class="drug-grid">
          <div
            v-for="drug in hotDrugs"
            :key="drug.id"
            class="drug-card fade-in-up"
            @click="$router.push(`/drugs/${drug.id}`)"
          >
            <div class="drug-img">
              <img v-if="drug.image" :src="drug.image" :alt="drug.genericName" class="drug-photo" />
              <Pill v-else :size="40" class="drug-placeholder-icon" />
              <n-tag v-if="drug.dosageForm" size="small" round :bordered="false" type="info" class="drug-tag">{{ drug.dosageForm }}</n-tag>
            </div>
            <div class="drug-body">
              <h4 class="drug-name">{{ drug.genericName }}</h4>
              <p class="drug-spec" v-if="drug.specification">{{ drug.specification }}</p>
              <p class="drug-mfr">{{ drug.manufacturer }}</p>
              <div class="drug-footer">
                <span class="drug-price">¥{{ drug.retailPrice?.toFixed(2) || '0.00' }}</span>
                <span class="drug-unit">/ {{ drug.unit || '盒' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="section features-section">
      <div class="container">
        <div class="section-header fade-in-up">
          <h2>平台优势</h2>
          <p>为您提供专业可靠的药品信息服务</p>
        </div>
        <div class="feature-grid">
          <div class="feature-card fade-in-up">
            <div class="feat-icon"><SearchIcon :size="36" color="#1B6B4A" /></div>
            <h3>精准搜索</h3>
            <p>支持通用名、商品名、药品编码等多维度搜索，快速定位所需药品。</p>
          </div>
          <div class="feature-card fade-in-up">
            <div class="feat-icon"><ClipboardList :size="36" color="#E07A5F" /></div>
            <h3>详细信息</h3>
            <p>涵盖药品规格、剂型、批准文号、生产厂家等全面信息。</p>
          </div>
          <div class="feature-card fade-in-up">
            <div class="feat-icon"><Tags :size="36" color="#3B82F6" /></div>
            <h3>分类浏览</h3>
            <p>科学的药品分类体系，支持树形分类导航，方便按类查找。</p>
          </div>
          <div class="feature-card fade-in-up">
            <div class="feat-icon"><BadgeDollarSign :size="36" color="#F59E0B" /></div>
            <h3>价格透明</h3>
            <p>公示药品零售价格，为患者合理用药提供参考依据。</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, type Component as VueComponent } from 'vue'
import { useRouter } from 'vue-router'
import {
  Pill, Hospital, FolderTree, Factory, ArrowRight,
  Search as SearchIcon, ClipboardList, Tags, BadgeDollarSign,
  Leaf, Tablets, FlaskConical, Stethoscope, ShieldCheck, Syringe,
  Heart, HeartPulse, Baby, Package
} from 'lucide-vue-next'
import { getPublicDrugs, getPublicCategories, getPublicStats } from '../api'

const router = useRouter()
const stats = ref<any>({})
const categories = ref<any[]>([])
const hotDrugs = ref<any[]>([])

const iconMap: Record<string, VueComponent> = {
  '中药': Leaf, '西药': Tablets, '中成药': FlaskConical, '保健品': ShieldCheck,
  '处方药': Stethoscope, '非处方药': Package, '抗生素': Syringe, '感冒': Pill,
  '消化': HeartPulse, '心血管': Heart, '维生素': FlaskConical, '外用药': ShieldCheck,
  '注射剂': Syringe, '儿科': Baby, '妇科': Heart, '医疗器械': Stethoscope
}

function getCategoryIcon(name: string | undefined): VueComponent {
  if (!name) return Package
  for (const [k, v] of Object.entries(iconMap)) {
    if (name.includes(k)) return v
  }
  return Package
}

function goCategory(id: number) {
  router.push({ path: '/drugs', query: { categoryId: String(id) } })
}

onMounted(async () => {
  try {
    const [statsRes, catRes, drugsRes]: any[] = await Promise.all([
      getPublicStats(),
      getPublicCategories(),
      getPublicDrugs({ page: 1, size: 8 })
    ])
    stats.value = statsRes.data || {}
    categories.value = (catRes.data || []).slice(0, 8)
    hotDrugs.value = drugsRes.data?.records || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
/* ── Hero ── */
.hero {
  position: relative;
  overflow: hidden;
  padding: 80px 0 60px;
  background: linear-gradient(135deg, #F0FDF4 0%, #ECFDF5 40%, #FFF7ED 100%);
}
.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.hero-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  background: var(--primary);
}
.hero-shape-1 { width: 400px; height: 400px; top: -100px; right: -80px; }
.hero-shape-2 { width: 200px; height: 200px; bottom: -50px; left: 10%; background: var(--accent); }
.hero-shape-3 { width: 120px; height: 120px; top: 40%; left: 60%; }

.hero-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 60px;
}
.hero-text { flex: 1; }
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: rgba(27, 107, 74, 0.1);
  color: var(--primary);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 20px;
}
.hero h1 {
  font-size: 42px;
  font-weight: 800;
  line-height: 1.2;
  color: var(--text-primary);
  margin-bottom: 16px;
}
.hero-highlight {
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-desc {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 32px;
  max-width: 480px;
}
.hero-actions { display: flex; gap: 12px; }

.hero-visual {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 260px;
}
.hero-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: var(--surface-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  transition: var(--transition);
}
.hero-card:hover {
  transform: translateX(-4px);
  box-shadow: var(--shadow-lg);
}
.hc-icon { display: flex; align-items: center; }
.hc-label { font-size: 12px; color: var(--text-muted); }
.hc-value { font-size: 28px; font-weight: 700; color: var(--text-primary); }
.hc-info { display: flex; flex-direction: column; }

/* ── Sections ── */
.section { padding: 80px 0; }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 48px;
}
.section-header h2 { font-size: 28px; font-weight: 700; }
.section-header p { color: var(--text-secondary); font-size: 15px; }
.see-all {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--primary);
}

/* ── Categories ── */
.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.category-card {
  padding: 28px 20px;
  background: var(--surface-card);
  border-radius: var(--radius-md);
  text-align: center;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  transition: var(--transition);
}
.category-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary);
}
.cat-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
  color: var(--primary);
}
.category-card h3 { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.category-card p { font-size: 12px; color: var(--text-muted); }

/* ── Drugs Grid ── */
.drug-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.drug-card {
  background: var(--surface-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  transition: var(--transition);
}
.drug-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
.drug-img {
  height: 160px;
  background: linear-gradient(135deg, #F0FDF4, #ECFDF5);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.drug-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.drug-placeholder-icon {
  color: var(--text-muted);
  opacity: 0.4;
}
.drug-tag { position: absolute; top: 10px; right: 10px; }
.drug-body { padding: 16px; }
.drug-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.drug-spec { font-size: 13px; color: var(--text-secondary); margin-bottom: 2px; }
.drug-mfr {
  font-size: 12px;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 12px;
}
.drug-footer { display: flex; align-items: baseline; gap: 2px; }
.drug-price { font-size: 20px; font-weight: 700; color: var(--accent); }
.drug-unit { font-size: 12px; color: var(--text-muted); }

/* ── Features ── */
.features-section {
  background: linear-gradient(180deg, #F8FAF9 0%, var(--surface-bg) 100%);
}
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}
.feature-card {
  padding: 32px 24px;
  background: var(--surface-card);
  border-radius: var(--radius-md);
  text-align: center;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  transition: var(--transition);
}
.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}
.feat-icon { display: flex; justify-content: center; margin-bottom: 16px; }
.feature-card h3 { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.feature-card p { font-size: 13px; color: var(--text-secondary); line-height: 1.6; }

/* ── Responsive ── */
@media (max-width: 768px) {
  .hero-content { flex-direction: column; text-align: center; }
  .hero h1 { font-size: 28px; }
  .hero-desc { margin: 0 auto 24px; }
  .hero-actions { justify-content: center; }
  .hero-visual { flex-direction: row; flex-wrap: wrap; justify-content: center; }
  .category-grid, .drug-grid, .feature-grid { grid-template-columns: repeat(2, 1fr); }
  .section-header { flex-direction: column; text-align: center; gap: 8px; }
}
</style>
