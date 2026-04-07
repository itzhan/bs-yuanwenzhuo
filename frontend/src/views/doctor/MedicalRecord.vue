<template>
  <div class="record-page">
    <section class="page-section">
      <div class="container">
        <div class="page-header fade-in-up">
          <div>
            <h1>记录管理</h1>
            <p>查询处方记录与患者用药历史</p>
          </div>
        </div>

        <n-card class="fade-in-up" :bordered="false">
          <n-tabs type="line" size="large" animated default-value="mine">
            <!-- Tab 1: 我开具的处方 -->
            <n-tab-pane name="mine" tab="我开具的处方">
              <div class="toolbar">
                <n-input
                  v-model:value="mineKeyword"
                  placeholder="搜索处方编号、患者、诊断..."
                  clearable
                  style="max-width: 360px"
                  @keydown.enter="loadMine(1)"
                >
                  <template #prefix><n-icon><Search :size="16" /></n-icon></template>
                </n-input>
                <n-button @click="loadMine(1)">查询</n-button>
              </div>
              <n-data-table
                :columns="mineColumns"
                :data="mineList"
                :loading="mineLoading"
                :pagination="minePagination"
                :row-key="(row: any) => row.id"
                remote
                @update:page="loadMine"
              />
            </n-tab-pane>

            <!-- Tab 2: 患者用药记录 -->
            <n-tab-pane name="patient" tab="患者用药记录">
              <div class="toolbar">
                <n-input
                  v-model:value="queryName"
                  placeholder="患者姓名"
                  clearable
                  style="max-width: 200px"
                />
                <n-input
                  v-model:value="queryPhone"
                  placeholder="联系电话"
                  clearable
                  style="max-width: 200px"
                  @keydown.enter="loadPatient"
                />
                <n-button type="primary" @click="loadPatient">
                  <template #icon><n-icon><Search :size="16" /></n-icon></template>
                  查询
                </n-button>
              </div>

              <n-empty v-if="!patientLoaded" description="请输入患者姓名或电话" />
              <n-empty v-else-if="patientList.length === 0" description="未查到该患者的用药记录" />
              <div v-else class="patient-records">
                <n-card
                  v-for="p in patientList"
                  :key="p.id"
                  :title="`处方 ${p.prescriptionNo}`"
                  size="small"
                  class="record-card"
                >
                  <template #header-extra>
                    <n-tag :type="p.status === 1 ? 'success' : 'error'" size="small">
                      {{ p.status === 1 ? '有效' : '作废' }}
                    </n-tag>
                  </template>
                  <div class="record-meta">
                    <span><b>开方医师：</b>{{ p.doctorName || '-' }}</span>
                    <span><b>开具时间：</b>{{ p.createTime }}</span>
                    <span><b>诊断：</b>{{ p.diagnosis || '-' }}</span>
                  </div>
                  <n-data-table
                    size="small"
                    :columns="itemColumns"
                    :data="p.items || []"
                    :bordered="false"
                  />
                </n-card>
              </div>
            </n-tab-pane>
          </n-tabs>
        </n-card>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import {
  NTag, useMessage
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { Search } from 'lucide-vue-next'
import { listPrescriptions, getPatientMedication } from '../../api'

const message = useMessage()

// ---- Tab 1: 我的处方 ----
const mineKeyword = ref('')
const mineList = ref<any[]>([])
const mineLoading = ref(false)
const minePagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: false
})

const mineColumns: DataTableColumns = [
  { title: '处方编号', key: 'prescriptionNo', width: 200 },
  { title: '患者', key: 'patientName', width: 100 },
  { title: '电话', key: 'patientPhone', width: 130 },
  { title: '诊断', key: 'diagnosis', ellipsis: { tooltip: true } },
  { title: '药品数', key: 'itemCount', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: any) =>
      h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' },
        { default: () => (row.status === 1 ? '有效' : '作废') })
  },
  { title: '开具时间', key: 'createTime', width: 170 }
]

async function loadMine(page = minePagination.value.page) {
  mineLoading.value = true
  try {
    const res: any = await listPrescriptions({
      page,
      size: minePagination.value.pageSize,
      keyword: mineKeyword.value || undefined,
      mineOnly: true
    })
    mineList.value = res.data.records || []
    minePagination.value.itemCount = res.data.total || 0
    minePagination.value.page = page
  } catch (e: any) {
    message.error(e.message || '加载失败')
  } finally {
    mineLoading.value = false
  }
}

// ---- Tab 2: 患者用药 ----
const queryName = ref('')
const queryPhone = ref('')
const patientLoaded = ref(false)
const patientList = ref<any[]>([])

const itemColumns: DataTableColumns = [
  { title: '药品', key: 'drugName' },
  { title: '规格', key: 'specification' },
  { title: '数量', key: 'quantity', width: 70 },
  { title: '单位', key: 'unit', width: 60 },
  { title: '用法用量', key: 'dosage' }
]

async function loadPatient() {
  if (!queryName.value && !queryPhone.value) {
    message.warning('请至少输入患者姓名或电话')
    return
  }
  try {
    const res: any = await getPatientMedication({
      patientName: queryName.value || undefined,
      patientPhone: queryPhone.value || undefined
    })
    patientList.value = res.data || []
    patientLoaded.value = true
  } catch (e: any) {
    message.error(e.message || '查询失败')
  }
}

onMounted(() => loadMine(1))
</script>

<style scoped>
.page-section { padding: 40px 0 60px; }
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 28px; margin: 0; color: var(--text-primary); }
.page-header p { color: var(--text-muted); margin: 6px 0 0; font-size: 14px; }
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.patient-records {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.record-card { background: #fafbfc; }
.record-meta {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}
</style>
