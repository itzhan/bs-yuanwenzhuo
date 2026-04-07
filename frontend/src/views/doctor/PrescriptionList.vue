<template>
  <div class="prescription-page">
    <section class="page-section">
      <div class="container">
        <div class="page-header fade-in-up">
          <div>
            <h1>处方管理</h1>
            <p>查看、修改或作废您开具的处方</p>
          </div>
          <n-button type="primary" size="large" @click="$router.push('/prescription/create')">
            <template #icon><n-icon><Plus :size="16" /></n-icon></template>
            新建处方
          </n-button>
        </div>

        <div class="toolbar fade-in-up">
          <n-input
            v-model:value="keyword"
            placeholder="搜索处方编号、患者姓名、电话、诊断..."
            clearable
            style="max-width: 360px"
            @keydown.enter="loadData(1)"
          >
            <template #prefix><n-icon><Search :size="16" /></n-icon></template>
          </n-input>
          <n-select
            v-model:value="status"
            :options="statusOptions"
            placeholder="全部状态"
            clearable
            style="width: 140px"
            @update:value="loadData(1)"
          />
          <n-button @click="loadData(1)">查询</n-button>
        </div>

        <n-card class="fade-in-up" :bordered="false">
          <n-data-table
            :columns="columns"
            :data="list"
            :loading="loading"
            :pagination="pagination"
            :row-key="(row: any) => row.id"
            remote
            @update:page="loadData"
          />
        </n-card>
      </div>
    </section>

    <!-- 详情抽屉 -->
    <n-drawer v-model:show="detailVisible" :width="560" placement="right">
      <n-drawer-content title="处方详情" closable>
        <template v-if="detail">
          <n-descriptions label-placement="left" :column="2" bordered size="small">
            <n-descriptions-item label="处方编号">{{ detail.prescriptionNo }}</n-descriptions-item>
            <n-descriptions-item label="状态">
              <n-tag :type="detail.status === 1 ? 'success' : 'error'" size="small">
                {{ detail.status === 1 ? '有效' : '作废' }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="患者">{{ detail.patientName }}</n-descriptions-item>
            <n-descriptions-item label="性别">{{ detail.patientGender === 1 ? '男' : detail.patientGender === 0 ? '女' : '-' }}</n-descriptions-item>
            <n-descriptions-item label="年龄">{{ detail.patientAge ?? '-' }}</n-descriptions-item>
            <n-descriptions-item label="电话">{{ detail.patientPhone || '-' }}</n-descriptions-item>
            <n-descriptions-item label="诊断" :span="2">{{ detail.diagnosis || '-' }}</n-descriptions-item>
            <n-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</n-descriptions-item>
            <n-descriptions-item label="开具时间" :span="2">{{ detail.createTime }}</n-descriptions-item>
          </n-descriptions>
          <h3 style="margin-top:16px">药品明细</h3>
          <n-data-table
            :columns="itemColumns"
            :data="detail.items || []"
            :bordered="false"
            size="small"
          />
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton, NIcon, NTag, NPopconfirm, NSpace, useMessage
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { Plus, Search } from 'lucide-vue-next'
import { listPrescriptions, getPrescription, voidPrescription } from '../../api'

const router = useRouter()
const message = useMessage()

const keyword = ref('')
const status = ref<number | null>(null)
const statusOptions = [
  { label: '有效', value: 1 },
  { label: '作废', value: 0 }
]

const list = ref<any[]>([])
const loading = ref(false)
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: false
})

const detailVisible = ref(false)
const detail = ref<any>(null)

async function loadData(page = pagination.value.page) {
  loading.value = true
  try {
    const res: any = await listPrescriptions({
      page,
      size: pagination.value.pageSize,
      keyword: keyword.value || undefined,
      status: status.value,
      mineOnly: true
    })
    list.value = res.data.records || []
    pagination.value.itemCount = res.data.total || 0
    pagination.value.page = page
  } catch (e: any) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function showDetail(id: number) {
  try {
    const res: any = await getPrescription(id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e: any) {
    message.error(e.message || '获取详情失败')
  }
}

async function doVoid(id: number) {
  try {
    await voidPrescription(id)
    message.success('已作废')
    loadData()
  } catch (e: any) {
    message.error(e.message || '作废失败')
  }
}

const columns: DataTableColumns = [
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
  { title: '开具时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 240,
    fixed: 'right',
    render: (row: any) =>
      h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => showDetail(row.id) }, { default: () => '详情' }),
          h(NButton, {
            size: 'small',
            quaternary: true,
            type: 'info',
            disabled: row.status !== 1,
            onClick: () => router.push(`/prescription/${row.id}/edit`)
          }, { default: () => '修改' }),
          h(NPopconfirm, {
            onPositiveClick: () => doVoid(row.id)
          }, {
            trigger: () => h(NButton, {
              size: 'small',
              quaternary: true,
              type: 'error',
              disabled: row.status !== 1
            }, { default: () => '作废' }),
            default: () => '确认作废该处方？'
          })
        ]
      })
  }
]

const itemColumns: DataTableColumns = [
  { title: '药品', key: 'drugName' },
  { title: '规格', key: 'specification' },
  { title: '数量', key: 'quantity', width: 70 },
  { title: '单位', key: 'unit', width: 60 },
  { title: '用法用量', key: 'dosage' }
]

onMounted(() => loadData(1))
</script>

<style scoped>
.page-section { padding: 40px 0 60px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-header h1 { font-size: 28px; margin: 0; color: var(--text-primary); }
.page-header p { color: var(--text-muted); margin: 6px 0 0; font-size: 14px; }
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
</style>
