<template>
  <n-space vertical :size="16">
    <n-card>
      <n-space>
        <n-select v-model:value="search.alertLevel" :options="levelOptions" placeholder="预警等级" clearable style="width: 140px" />
        <n-select v-model:value="search.status" :options="[{label:'待处理',value:0},{label:'已处理',value:1}]" placeholder="状态" clearable style="width: 120px" />
        <n-button type="primary" @click="fetchData">搜索</n-button>
        <n-button @click="resetSearch">重置</n-button>
        <n-button type="warning" @click="handleGenerate">生成预警</n-button>
      </n-space>
    </n-card>
    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />
      <n-space justify="end" style="margin-top: 16px">
        <n-pagination v-model:page="pagination.page" :page-count="Math.ceil(pagination.total / pagination.size)" @update:page="fetchData" />
      </n-space>
    </n-card>
    <n-modal v-model:show="showHandle" title="处理预警" preset="card" style="width: 400px">
      <n-form :model="handleForm" label-placement="left" label-width="80">
        <n-form-item label="处理备注"><n-input v-model:value="handleForm.handlerNote" type="textarea" /></n-form-item>
      </n-form>
      <template #action><n-space justify="end"><n-button @click="showHandle = false">取消</n-button><n-button type="primary" @click="submitHandle">确定</n-button></n-space></template>
    </n-modal>
  </n-space>
</template>

<script setup lang="ts">
import { h, ref, reactive, onMounted } from 'vue';
import { NButton, NTag, NSpace, NPopconfirm } from 'naive-ui';
import { fetchExpiryAlertList, handleExpiryAlert, generateExpiryAlerts, deleteExpiryAlert } from '@/service/api';

const loading = ref(false);
const list = ref<any[]>([]);
const pagination = reactive({ page: 1, size: 10, total: 0 });
const search = reactive({ alertLevel: null as number | null, status: null as number | null });
const showHandle = ref(false);
const handleId = ref<number | null>(null);
const handleForm = reactive({ handlerNote: '' });

const levelOptions = [
  { label: '黄色预警（6个月内）', value: 0 },
  { label: '橙色预警（3个月内）', value: 1 },
  { label: '红色预警（1个月内）', value: 2 }
];
const levelMap: Record<number, { label: string; type: any }> = {
  0: { label: '黄色', type: 'warning' }, 1: { label: '橙色', type: 'warning' }, 2: { label: '红色', type: 'error' }
};

const columns = [
  { title: '药品名称', key: 'drugName', width: 120 },
  { title: '规格', key: 'specification', width: 100 },
  { title: '批次号', key: 'batchNumber', width: 100 },
  { title: '有效期至', key: 'expiryDate', width: 100 },
  { title: '预警等级', key: 'alertLevel', width: 90, render: (row: any) => { const l = levelMap[row.alertLevel]; return h(NTag, { type: l?.type || 'default', size: 'small' }, () => l?.label || '未知'); } },
  { title: '状态', key: 'status', width: 80, render: (row: any) => h(NTag, { type: row.status === 0 ? 'info' : 'success', size: 'small' }, () => row.status === 0 ? '待处理' : '已处理') },
  { title: '处理备注', key: 'handlerNote', ellipsis: { tooltip: true } },
  {
    title: '操作', key: 'action', width: 150,
    render: (row: any) => h(NSpace, null, () => {
      const btns: any[] = [];
      if (row.status === 0) {
        btns.push(h(NButton, { size: 'small', type: 'primary', onClick: () => { handleId.value = row.id; handleForm.handlerNote = ''; showHandle.value = true; } }, () => '处理'));
      }
      btns.push(h(NPopconfirm, { onPositiveClick: () => handleDeleteAlert(row.id) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'), default: () => '确定删除？' }));
      return btns;
    })
  }
];

async function fetchData() {
  loading.value = true;
  const { data, error } = await fetchExpiryAlertList({ page: pagination.page, size: pagination.size, alertLevel: search.alertLevel ?? undefined, status: search.status ?? undefined });
  if (!error && data) { list.value = data.records || []; pagination.total = data.total || 0; }
  loading.value = false;
}

function resetSearch() { search.alertLevel = null; search.status = null; pagination.page = 1; fetchData(); }

async function handleGenerate() {
  const { error } = await generateExpiryAlerts();
  if (!error) { window.$message?.success('预警生成成功'); fetchData(); }
}

async function submitHandle() {
  if (handleId.value) {
    const { error } = await handleExpiryAlert(handleId.value, handleForm);
    if (!error) { showHandle.value = false; window.$message?.success('处理成功'); fetchData(); }
  }
}

async function handleDeleteAlert(id: number) {
  const { error } = await deleteExpiryAlert(id);
  if (!error) { window.$message?.success('删除成功'); fetchData(); }
}

onMounted(fetchData);
</script>
