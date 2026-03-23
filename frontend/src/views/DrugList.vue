<template>
  <div class="drug-list-page">
    <!-- Search Bar -->
    <section class="search-section">
      <div class="container">
        <div class="search-header fade-in-up">
          <h1>药品查询</h1>
          <p>输入药品名称、编码进行搜索，或按分类筛选</p>
        </div>
        <div class="search-bar fade-in-up">
          <n-input
            v-model:value="keyword"
            placeholder="搜索药品名称、商品名、药品编码..."
            size="large"
            clearable
            @keydown.enter="doSearch"
          >
            <template #prefix>
              <n-icon><SearchIcon :size="18" /></n-icon>
            </template>
          </n-input>
          <n-button type="primary" size="large" @click="doSearch">
            <template #icon><n-icon><SearchIcon :size="16" /></n-icon></template>
            搜索
          </n-button>
        </div>
        <div class="filter-bar fade-in-up">
          <span class="filter-label"><Filter :size="14" /> 分类筛选：</span>
          <n-tag
            :checked="!selectedCategory"
            checkable
            round
            @update:checked="selectedCategory = null; doSearch()"
          >
            全部
          </n-tag>
          <n-tag
            v-for="cat in allCategories"
            :key="cat.id"
            :checked="selectedCategory === cat.id"
            checkable
            round
            @update:checked="(v: boolean) => { selectedCategory = v ? cat.id : null; doSearch() }"
          >
            {{ cat.name }}
          </n-tag>
        </div>
      </div>
    </section>

    <!-- Results -->
    <section class="results-section">
      <div class="container">
        <div class="results-info">
          <span>共找到 <strong>{{ total }}</strong> 种药品</span>
        </div>

        <n-spin :show="loading">
          <div class="drug-grid" v-if="drugs.length > 0">
            <div
              v-for="drug in drugs"
              :key="drug.id"
              class="drug-card fade-in-up"
              @click="$router.push(`/drugs/${drug.id}`)"
            >
              <div class="drug-img">
                <img v-if="drug.image" :src="drug.image" :alt="drug.genericName" class="drug-photo" />
                <Pill v-else :size="36" class="drug-placeholder-icon" />
                <n-tag v-if="drug.dosageForm" size="small" round :bordered="false" type="info" class="drug-form-tag">{{ drug.dosageForm }}</n-tag>
              </div>
              <div class="drug-body">
                <h3 class="drug-name">{{ drug.genericName }}</h3>
                <p class="drug-trade" v-if="drug.tradeName">{{ drug.tradeName }}</p>
                <div class="drug-meta">
                  <span v-if="drug.specification">{{ drug.specification }}</span>
                  <span v-if="drug.categoryName" class="drug-cat">{{ drug.categoryName }}</span>
                </div>
                <p class="drug-mfr" v-if="drug.manufacturer">{{ drug.manufacturer }}</p>
                <div class="drug-bottom">
                  <span class="drug-price">¥{{ drug.retailPrice?.toFixed(2) || '—' }}</span>
                  <span class="drug-unit">/ {{ drug.unit || '盒' }}</span>
                </div>
              </div>
            </div>
          </div>
          <n-empty v-else-if="!loading" description="未找到相关药品" style="padding:80px 0" />
        </n-spin>

        <div class="pagination-wrap" v-if="total > pageSize">
          <n-pagination
            v-model:page="currentPage"
            :page-count="Math.ceil(total / pageSize)"
            :page-slot="7"
            @update:page="handlePageChange"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search as SearchIcon, Filter, Pill } from 'lucide-vue-next'
import { getPublicDrugs, getPublicCategories } from '../api'

const route = useRoute()

const keyword = ref('')
const selectedCategory = ref<number | null>(null)
const drugs = ref<any[]>([])
const allCategories = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 12
const loading = ref(false)

async function fetchDrugs() {
  loading.value = true
  try {
    const res: any = await getPublicDrugs({
      page: currentPage.value,
      size: pageSize,
      keyword: keyword.value || undefined,
      categoryId: selectedCategory.value
    })
    drugs.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function doSearch() {
  currentPage.value = 1
  fetchDrugs()
}

function handlePageChange(p: number) {
  currentPage.value = p
  fetchDrugs()
}

onMounted(async () => {
  if (route.query.categoryId) {
    selectedCategory.value = Number(route.query.categoryId)
  }
  if (route.query.keyword) {
    keyword.value = String(route.query.keyword)
  }

  const catRes: any = await getPublicCategories()
  allCategories.value = catRes.data || []

  fetchDrugs()
})

watch(() => route.query.categoryId, (val) => {
  if (val) {
    selectedCategory.value = Number(val)
    doSearch()
  }
})
</script>

<style scoped>
.search-section {
  padding: 48px 0 32px;
  background: linear-gradient(135deg, #F0FDF4 0%, #FFF7ED 100%);
}
.search-header { text-align: center; margin-bottom: 24px; }
.search-header h1 { font-size: 28px; font-weight: 700; margin-bottom: 4px; }
.search-header p { color: var(--text-secondary); font-size: 14px; }
.search-bar {
  display: flex;
  gap: 12px;
  max-width: 640px;
  margin: 0 auto 20px;
}
.search-bar .n-input { flex: 1; }

.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}
.filter-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.results-section { padding: 32px 0 64px; }
.results-info { margin-bottom: 24px; font-size: 14px; color: var(--text-secondary); }
.results-info strong { color: var(--primary); }

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
.drug-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-lg); }
.drug-img {
  height: 140px;
  background: linear-gradient(135deg, #F0FDF4, #ECFDF5);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.drug-photo { width: 100%; height: 100%; object-fit: cover; }
.drug-placeholder-icon { color: var(--text-muted); opacity: 0.4; }
.drug-form-tag { position: absolute; top: 8px; right: 8px; }
.drug-body { padding: 16px; }
.drug-name {
  font-size: 15px; font-weight: 600; margin-bottom: 4px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.drug-trade { font-size: 12px; color: var(--text-muted); margin-bottom: 6px; }
.drug-meta { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 6px; }
.drug-meta span {
  font-size: 12px; padding: 2px 8px; background: #f3f4f6;
  border-radius: 4px; color: var(--text-secondary);
}
.drug-cat { background: rgba(27, 107, 74, 0.08) !important; color: var(--primary) !important; }
.drug-mfr {
  font-size: 12px; color: var(--text-muted); margin-bottom: 12px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.drug-bottom { display: flex; align-items: baseline; gap: 2px; }
.drug-price { font-size: 20px; font-weight: 700; color: var(--accent); }
.drug-unit { font-size: 12px; color: var(--text-muted); }

.pagination-wrap { display: flex; justify-content: center; margin-top: 40px; }

@media (max-width: 768px) { .drug-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
