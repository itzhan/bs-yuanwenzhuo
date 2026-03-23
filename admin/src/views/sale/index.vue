<template>
  <n-space vertical :size="16">
    <n-card>
      <n-space>
        <n-input v-model:value="search.keyword" placeholder="搜索" clearable style="width: 200px" />
        <n-button type="primary" @click="fetchData">搜索</n-button>
        <n-button @click="resetSearch">重置</n-button>
        <n-button type="success" @click="openCreate">新增销售</n-button>
      </n-space>
    </n-card>
    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />
      <n-space justify="end" style="margin-top: 16px">
        <n-pagination v-model:page="pagination.page" :page-count="Math.ceil(pagination.total / pagination.size)" @update:page="fetchData" />
      </n-space>
    </n-card>
    <n-modal v-model:show="showModal" title="新增销售" preset="card" style="width: 500px">
      <n-form ref="formRef" :model="form" :rules="saleRules" label-placement="left" label-width="80" require-mark-placement="right-hanging">
        <n-form-item label="药品" path="drugId">
          <n-select
            v-model:value="form.drugId"
            :options="drugOptions"
            filterable
            placeholder="搜索药品名称"
            style="width: 100%"
            @update:value="onDrugSelect"
          />
        </n-form-item>
        <n-form-item label="数量" path="quantity"><n-input-number v-model:value="form.quantity" :min="1" style="width: 100%" placeholder="请输入销售数量" /></n-form-item>
        <n-form-item label="单价" path="unitPrice"><n-input-number v-model:value="form.unitPrice" :min="0" :precision="2" style="width: 100%" placeholder="请输入单价" /></n-form-item>
        <n-form-item label="客户名称"><n-input v-model:value="form.customerName" placeholder="客户名称（选填）" /></n-form-item>
      </n-form>
      <template #action><n-space justify="end"><n-button @click="showModal = false">取消</n-button><n-button type="primary" @click="handleCreate">提交</n-button></n-space></template>
    </n-modal>
  </n-space>
</template>

<script setup lang="ts">
import { h, ref, reactive, onMounted, computed } from 'vue';
import { NButton, NPopconfirm } from 'naive-ui';
import type { FormInst, FormRules } from 'naive-ui';
import { fetchSaleList, createSale, deleteSale, fetchAllDrugs } from '@/service/api';

const loading = ref(false);
const list = ref<any[]>([]);
const pagination = reactive({ page: 1, size: 10, total: 0 });
const search = reactive({ keyword: '' });
const showModal = ref(false);
const formRef = ref<FormInst | null>(null);
const form = reactive({ drugId: null as number | null, quantity: 1, unitPrice: 0, customerName: '' });

const saleRules: FormRules = {
  drugId: { required: true, type: 'number', message: '请选择药品', trigger: 'change' },
  quantity: { required: true, type: 'number', message: '请输入数量', trigger: 'blur' },
  unitPrice: { required: true, type: 'number', message: '请输入单价', trigger: 'blur' }
};

const allDrugs = ref<any[]>([]);
const drugOptions = computed(() =>
  allDrugs.value.map(d => ({
    label: `${d.genericName}${d.specification ? ' (' + d.specification + ')' : ''}`,
    value: d.id
  }))
);

function onDrugSelect(drugId: number) {
  const drug = allDrugs.value.find(d => d.id === drugId);
  if (drug && drug.retailPrice) {
    form.unitPrice = drug.retailPrice;
  }
}

const columns = [
  { title: '销售单号', key: 'saleNo', width: 180 },
  { title: '药品名称', key: 'drugName', width: 120 },
  { title: '数量', key: 'quantity', width: 70 },
  { title: '单价', key: 'unitPrice', width: 80, render: (row: any) => `¥${row.unitPrice ?? 0}` },
  { title: '金额', key: 'totalPrice', width: 90, render: (row: any) => `¥${row.totalPrice ?? 0}` },
  { title: '客户', key: 'customerName', width: 100 },
  { title: '操作员', key: 'operatorName', width: 80 },
  { title: '时间', key: 'createTime', width: 160 },
  {
    title: '操作', key: 'action', width: 80,
    render: (row: any) => h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'), default: () => '确定删除？' })
  }
];

async function fetchData() {
  loading.value = true;
  const { data, error } = await fetchSaleList({ page: pagination.page, size: pagination.size, keyword: search.keyword || undefined });
  if (!error && data) { list.value = data.records || []; pagination.total = data.total || 0; }
  loading.value = false;
}

function resetSearch() { search.keyword = ''; pagination.page = 1; fetchData(); }
function openCreate() { Object.assign(form, { drugId: null, quantity: 1, unitPrice: 0, customerName: '' }); showModal.value = true; }

async function handleCreate() {
  try { await formRef.value?.validate(); } catch { return; }
  const { error } = await createSale(form);
  if (!error) { showModal.value = false; window.$message?.success('创建成功'); fetchData(); }
}

async function handleDelete(id: number) {
  const { error } = await deleteSale(id);
  if (!error) { window.$message?.success('删除成功'); fetchData(); }
}

onMounted(async () => {
  fetchData();
  const { data, error } = await fetchAllDrugs();
  if (!error && data) allDrugs.value = data;
});
</script>
