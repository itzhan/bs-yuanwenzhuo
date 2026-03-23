<template>
  <n-space vertical :size="16">
    <n-card>
      <n-space>
        <n-select v-model:value="search.type" :options="[{label:'入库',value:0},{label:'出库',value:1}]" placeholder="类型" clearable style="width: 120px" />
        <n-button type="primary" @click="fetchData">搜索</n-button>
        <n-button @click="resetSearch">重置</n-button>
        <n-button type="success" @click="openAdd">新增记录</n-button>
      </n-space>
    </n-card>
    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />
      <n-space justify="end" style="margin-top: 16px">
        <n-pagination v-model:page="pagination.page" :page-count="Math.ceil(pagination.total / pagination.size)" @update:page="fetchData" />
      </n-space>
    </n-card>
    <n-modal v-model:show="showModal" title="新增出入库记录" preset="card" style="width: 500px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100" require-mark-placement="right-hanging">
        <n-form-item label="药品" path="drugId">
          <n-select
            v-model:value="form.drugId"
            :options="drugOptions"
            filterable
            placeholder="搜索药品名称"
            style="width: 100%"
          />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="form.type" :options="[{label:'入库',value:0},{label:'出库',value:1}]" placeholder="选择出入库类型" />
        </n-form-item>
        <n-form-item label="数量" path="quantity">
          <n-input-number v-model:value="form.quantity" :min="1" style="width: 100%" placeholder="请输入变动数量" />
        </n-form-item>
        <n-form-item label="变动前数量" path="beforeQuantity">
          <n-input-number v-model:value="form.beforeQuantity" :min="0" style="width: 100%" placeholder="变动前库存数量" />
        </n-form-item>
        <n-form-item label="变动后数量" path="afterQuantity">
          <n-input-number v-model:value="form.afterQuantity" :min="0" style="width: 100%" placeholder="变动后库存数量" />
        </n-form-item>
        <n-form-item label="原因" path="reason">
          <n-input v-model:value="form.reason" type="textarea" placeholder="请输入变动原因" :rows="2" />
        </n-form-item>
      </n-form>
      <template #action><n-space justify="end"><n-button @click="showModal = false">取消</n-button><n-button type="primary" @click="handleCreate">确定</n-button></n-space></template>
    </n-modal>
  </n-space>
</template>

<script setup lang="ts">
import { h, ref, reactive, onMounted, computed } from 'vue';
import { NTag, NButton, NSpace, NPopconfirm } from 'naive-ui';
import type { FormInst, FormRules } from 'naive-ui';
import { fetchInventoryLogList, createInventoryLog, deleteInventoryLog, fetchAllDrugs } from '@/service/api';

const loading = ref(false);
const list = ref<any[]>([]);
const pagination = reactive({ page: 1, size: 10, total: 0 });
const search = reactive({ type: null as number | null });
const showModal = ref(false);
const formRef = ref<FormInst | null>(null);
const form = reactive({ drugId: null as number | null, type: null as number | null, quantity: 1, beforeQuantity: 0, afterQuantity: 0, reason: '' });

const rules: FormRules = {
  drugId: { required: true, type: 'number', message: '请选择药品', trigger: 'change' },
  type: { required: true, type: 'number', message: '请选择类型', trigger: 'change' },
  quantity: { required: true, type: 'number', message: '请输入数量', trigger: 'blur' },
  beforeQuantity: { required: true, type: 'number', message: '请输入变动前数量', trigger: 'blur' },
  afterQuantity: { required: true, type: 'number', message: '请输入变动后数量', trigger: 'blur' },
  reason: { required: true, message: '请输入变动原因', trigger: 'blur' }
};

const allDrugs = ref<any[]>([]);
const drugOptions = computed(() =>
  allDrugs.value.map(d => ({
    label: `${d.genericName}${d.specification ? ' (' + d.specification + ')' : ''}`,
    value: d.id
  }))
);

const columns = [
  { title: '药品名称', key: 'drugName', width: 120 },
  { title: '类型', key: 'type', width: 70, render: (row: any) => h(NTag, { type: row.type === '入库' ? 'success' : 'warning', size: 'small' }, () => row.type) },
  { title: '数量', key: 'quantity', width: 70 },
  { title: '变动前', key: 'beforeQuantity', width: 80, render: (row: any) => row.beforeQuantity ?? '-' },
  { title: '变动后', key: 'afterQuantity', width: 80, render: (row: any) => row.afterQuantity ?? '-' },
  { title: '原因', key: 'reason', ellipsis: { tooltip: true }, render: (row: any) => row.reason || row.remark || '-' },
  { title: '操作人', key: 'operatorName', width: 80 },
  { title: '时间', key: 'createTime', width: 160 },
  {
    title: '操作', key: 'action', width: 80,
    render: (row: any) => h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'), default: () => '确定删除？' })
  }
];

async function fetchData() {
  loading.value = true;
  const { data, error } = await fetchInventoryLogList({ page: pagination.page, size: pagination.size, type: search.type ?? undefined });
  if (!error && data) { list.value = data.records || []; pagination.total = data.total || 0; }
  loading.value = false;
}

function resetSearch() { search.type = null; pagination.page = 1; fetchData(); }

function openAdd() {
  Object.assign(form, { drugId: null, type: null, quantity: 1, beforeQuantity: 0, afterQuantity: 0, reason: '' });
  showModal.value = true;
}

async function handleCreate() {
  try { await formRef.value?.validate(); } catch { return; }
  const { error } = await createInventoryLog(form);
  if (!error) { showModal.value = false; window.$message?.success('创建成功'); fetchData(); }
}

async function handleDelete(id: number) {
  const { error } = await deleteInventoryLog(id);
  if (!error) { window.$message?.success('删除成功'); fetchData(); }
}

onMounted(async () => {
  fetchData();
  const { data, error } = await fetchAllDrugs();
  if (!error && data) allDrugs.value = data;
});
</script>
