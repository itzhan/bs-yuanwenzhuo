<template>
  <n-space vertical :size="16">
    <n-card>
      <n-space>
        <n-input v-model:value="search.keyword" placeholder="搜索药品" clearable style="width: 200px" />
        <n-button type="primary" @click="fetchData">搜索</n-button>
        <n-button @click="resetSearch">重置</n-button>
        <n-button type="success" @click="openAdd">新增库存</n-button>
      </n-space>
    </n-card>
    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />
      <n-space justify="end" style="margin-top: 16px">
        <n-pagination v-model:page="pagination.page" :page-count="Math.ceil(pagination.total / pagination.size)" @update:page="fetchData" />
      </n-space>
    </n-card>
    <n-modal v-model:show="showModal" :title="isEdit ? '编辑库存' : '新增库存'" preset="card" style="width: 500px">
      <n-form ref="formRef" :model="form" :rules="invRules" label-placement="left" label-width="80" require-mark-placement="right-hanging">
        <n-form-item label="药品" path="drugId">
          <n-select
            v-model:value="form.drugId"
            :options="drugOptions"
            filterable
            placeholder="搜索药品名称"
            style="width: 100%"
          />
        </n-form-item>
        <n-form-item label="批次号" path="batchNumber"><n-input v-model:value="form.batchNumber" placeholder="请输入批次号" /></n-form-item>
        <n-form-item label="数量" path="quantity"><n-input-number v-model:value="form.quantity" :min="0" style="width: 100%" placeholder="请输入库存数量" /></n-form-item>
        <n-form-item label="预警阈值"><n-input-number v-model:value="form.warningThreshold" :min="0" style="width: 100%" placeholder="默认10（选填）" /></n-form-item>
        <n-form-item label="存放位置"><n-input v-model:value="form.location" placeholder="存放位置（选填）" /></n-form-item>
      </n-form>
      <template #action><n-space justify="end"><n-button @click="showModal = false">取消</n-button><n-button type="primary" @click="handleSubmit">确定</n-button></n-space></template>
    </n-modal>
    <n-modal v-model:show="showAdjust" title="库存调整" preset="card" style="width: 400px">
      <n-form :model="adjustForm" label-placement="left" label-width="80">
        <n-form-item label="调整数量"><n-input-number v-model:value="adjustForm.quantity" style="width: 100%" /></n-form-item>
        <n-form-item label="调整原因"><n-input v-model:value="adjustForm.reason" type="textarea" /></n-form-item>
      </n-form>
      <template #action><n-space justify="end"><n-button @click="showAdjust = false">取消</n-button><n-button type="primary" @click="handleAdjust">确定</n-button></n-space></template>
    </n-modal>
  </n-space>
</template>

<script setup lang="ts">
import { h, ref, reactive, onMounted, computed } from 'vue';
import { NButton, NTag, NSpace, NPopconfirm } from 'naive-ui';
import type { FormInst, FormRules } from 'naive-ui';
import { fetchInventoryList, createInventory, updateInventory, adjustStock, deleteInventory, fetchAllDrugs } from '@/service/api';

const loading = ref(false);
const list = ref<any[]>([]);
const pagination = reactive({ page: 1, size: 10, total: 0 });
const search = reactive({ keyword: '' });
const showModal = ref(false);
const isEdit = ref(false);
const editId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);
const form = reactive({ drugId: null as number | null, batchNumber: '', quantity: 0, warningThreshold: 10, location: '' });

const invRules: FormRules = {
  drugId: { required: true, type: 'number', message: '请选择药品', trigger: 'change' },
  batchNumber: { required: true, message: '请输入批次号', trigger: 'blur' },
  quantity: { required: true, type: 'number', message: '请输入数量', trigger: 'blur' }
};
const showAdjust = ref(false);
const adjustId = ref<number | null>(null);
const adjustForm = reactive({ quantity: 0, reason: '' });

const allDrugs = ref<any[]>([]);
const drugOptions = computed(() =>
  allDrugs.value.map(d => ({
    label: `${d.genericName}${d.specification ? ' (' + d.specification + ')' : ''}`,
    value: d.id
  }))
);

const columns = [
  { title: '药品名称', key: 'drugName', width: 120 },
  { title: '药品编码', key: 'drugCode', width: 110 },
  { title: '批次号', key: 'batchNumber', width: 100 },
  { title: '库存数量', key: 'quantity', width: 90, render: (row: any) => h('span', { style: row.quantity <= (row.warningThreshold || 10) ? 'color: red; font-weight: bold' : '' }, row.quantity) },
  { title: '预警阈值', key: 'warningThreshold', width: 90 },
  { title: '有效期至', key: 'expiryDate', width: 100 },
  { title: '存放位置', key: 'location', width: 100 },
  {
    title: '操作', key: 'action', width: 240,
    render: (row: any) => h(NSpace, null, () => [
      h(NButton, { size: 'small', onClick: () => openEdit(row) }, () => '编辑'),
      h(NButton, { size: 'small', type: 'warning', onClick: () => { adjustId.value = row.id; adjustForm.quantity = 0; adjustForm.reason = ''; showAdjust.value = true; } }, () => '调整'),
      h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'), default: () => '确定删除？' })
    ])
  }
];

async function fetchData() {
  loading.value = true;
  const { data, error } = await fetchInventoryList({ page: pagination.page, size: pagination.size, keyword: search.keyword || undefined });
  if (!error && data) { list.value = data.records || []; pagination.total = data.total || 0; }
  loading.value = false;
}

function resetSearch() { search.keyword = ''; pagination.page = 1; fetchData(); }
function openAdd() { isEdit.value = false; editId.value = null; Object.assign(form, { drugId: null, batchNumber: '', quantity: 0, warningThreshold: 10, location: '' }); showModal.value = true; }
function openEdit(row: any) { isEdit.value = true; editId.value = row.id; Object.assign(form, { drugId: row.drugId, batchNumber: row.batchNumber, quantity: row.quantity, warningThreshold: row.warningThreshold, location: row.location }); showModal.value = true; }

async function handleSubmit() {
  try { await formRef.value?.validate(); } catch { return; }
  if (isEdit.value && editId.value) {
    const { error } = await updateInventory(editId.value, form);
    if (!error) { showModal.value = false; window.$message?.success('更新成功'); fetchData(); }
  } else {
    const { error } = await createInventory(form);
    if (!error) { showModal.value = false; window.$message?.success('添加成功'); fetchData(); }
  }
}

async function handleAdjust() {
  if (adjustId.value) {
    const { error } = await adjustStock(adjustId.value, adjustForm);
    if (!error) { showAdjust.value = false; window.$message?.success('调整成功'); fetchData(); }
  }
}

async function handleDelete(id: number) {
  const { error } = await deleteInventory(id);
  if (!error) { window.$message?.success('删除成功'); fetchData(); }
}

onMounted(async () => {
  fetchData();
  const { data, error } = await fetchAllDrugs();
  if (!error && data) allDrugs.value = data;
});
</script>
