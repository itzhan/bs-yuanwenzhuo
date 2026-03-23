<template>
  <n-space vertical :size="16">
    <!-- 统计卡片 -->
    <n-grid :cols="4" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <n-gi span="4 m:1" v-for="item in statCards" :key="item.label">
        <n-card :bordered="false" class="stat-card" :style="{ background: item.gradient }">
          <div class="stat-card-inner">
            <div class="stat-card-info">
              <span class="stat-card-label">{{ item.label }}</span>
              <span class="stat-card-value">{{ item.value }}</span>
            </div>
            <div class="stat-card-icon">
              <SvgIcon :icon="item.icon" class="text-36px" />
            </div>
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 图表区域 -->
    <n-grid :cols="2" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <n-gi span="2 m:1">
        <n-card title="销售趋势（近6个月）" :bordered="false" class="chart-card">
          <div ref="lineChartRef" class="chart-container"></div>
        </n-card>
      </n-gi>
      <n-gi span="2 m:1">
        <n-card title="药品分类分布" :bordered="false" class="chart-card">
          <div ref="pieChartRef" class="chart-container"></div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 额外统计 + 快捷操作 -->
    <n-grid :cols="2" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <n-gi span="2 m:1">
        <n-card title="经营概览" :bordered="false">
          <n-grid :cols="2" :x-gap="16" :y-gap="16">
            <n-gi>
              <n-statistic label="累计销售额">
                <template #prefix><span style="color: #52c41a">¥</span></template>
                {{ formatMoney(data?.totalSalesAmount) }}
              </n-statistic>
            </n-gi>
            <n-gi>
              <n-statistic label="累计采购额">
                <template #prefix><span style="color: #1890ff">¥</span></template>
                {{ formatMoney(data?.totalPurchaseAmount) }}
              </n-statistic>
            </n-gi>
          </n-grid>
        </n-card>
      </n-gi>
      <n-gi span="2 m:1">
        <n-card title="快捷操作" :bordered="false">
          <n-space :size="12" :wrap="true">
            <n-button type="primary" @click="$router.push('/drug')">
              <template #icon><SvgIcon icon="mdi:pill" /></template>
              药品管理
            </n-button>
            <n-button type="info" @click="$router.push('/inventory')">
              <template #icon><SvgIcon icon="mdi:warehouse" /></template>
              库存管理
            </n-button>
            <n-button type="success" @click="$router.push('/sale')">
              <template #icon><SvgIcon icon="mdi:cart-outline" /></template>
              销售管理
            </n-button>
            <n-button type="warning" @click="$router.push('/expiry-alert')">
              <template #icon><SvgIcon icon="mdi:alert-circle-outline" /></template>
              效期预警
            </n-button>
            <n-button @click="$router.push('/purchase-order')">
              <template #icon><SvgIcon icon="mdi:truck-delivery-outline" /></template>
              采购管理
            </n-button>
            <n-button @click="$router.push('/supplier')">
              <template #icon><SvgIcon icon="mdi:store-outline" /></template>
              供应商管理
            </n-button>
          </n-space>
        </n-card>
      </n-gi>
    </n-grid>
  </n-space>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { fetchDashboardData } from '@/service/api';

const data = ref<any>(null);
const lineChartRef = ref<HTMLElement | null>(null);
const pieChartRef = ref<HTMLElement | null>(null);

let lineChart: echarts.ECharts | null = null;
let pieChart: echarts.ECharts | null = null;

const statCards = computed(() => [
  {
    label: '药品总数',
    value: data.value?.drugCount ?? 0,
    icon: 'mdi:pill',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    label: '供应商数',
    value: data.value?.supplierCount ?? 0,
    icon: 'mdi:store-outline',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    label: '库存预警',
    value: data.value?.lowStockCount ?? 0,
    icon: 'mdi:package-variant-closed-alert',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    label: '效期预警',
    value: data.value?.expiryAlertCount ?? 0,
    icon: 'mdi:alert-circle-outline',
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  }
]);

function formatMoney(val: any) {
  if (!val) return '0.00';
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function initLineChart() {
  if (!lineChartRef.value) return;
  lineChart = echarts.init(lineChartRef.value);
  const salesTrend = data.value?.salesTrend || [];
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '12%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: salesTrend.map((i: any) => i.month)
    },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#667eea' },
        lineStyle: { width: 3, color: '#667eea' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.35)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
          ])
        },
        data: salesTrend.map((i: any) => Number(i.amount))
      }
    ]
  });
}

function initPieChart() {
  if (!pieChartRef.value) return;
  pieChart = echarts.init(pieChartRef.value);
  const dist = data.value?.categoryDistribution || [];
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '2%', left: 'center', itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 12 } },
    color: ['#667eea', '#f5576c', '#4facfe', '#fa709a', '#43e97b', '#fee140', '#a18cd1', '#fbc2eb'],
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        labelLine: { show: false },
        data: dist.map((i: any) => ({ name: i.categoryName, value: i.count }))
      }
    ]
  });
}

function handleResize() {
  lineChart?.resize();
  pieChart?.resize();
}

async function loadData() {
  const { data: res, error } = await fetchDashboardData();
  if (!error) {
    data.value = res;
    await nextTick();
    initLineChart();
    initPieChart();
  }
}

onMounted(() => {
  loadData();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  lineChart?.dispose();
  pieChart?.dispose();
});
</script>

<style scoped>
.stat-card {
  border-radius: 12px;
  color: #fff;
  overflow: hidden;
}

.stat-card :deep(.n-card__content) {
  padding: 20px 24px !important;
}

.stat-card-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-card-label {
  font-size: 14px;
  opacity: 0.85;
}

.stat-card-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.stat-card-icon {
  opacity: 0.3;
}

.chart-card :deep(.n-card__content) {
  padding: 16px !important;
}

.chart-container {
  height: 340px;
  width: 100%;
}
</style>
