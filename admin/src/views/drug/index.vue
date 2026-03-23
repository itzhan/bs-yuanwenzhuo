<template>
  <n-space vertical :size="16">
    <n-card>
      <n-space>
        <n-input v-model:value="search.keyword" placeholder="搜索药品名称" clearable style="width: 200px" />
        <n-select v-model:value="search.status" :options="statusOptions" placeholder="状态" clearable style="width: 120px" />
        <n-button type="primary" @click="fetchData">搜索</n-button>
        <n-button @click="resetSearch">重置</n-button>
        <n-button type="success" @click="openAdd">新增药品</n-button>
      </n-space>
    </n-card>
    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />
      <n-space justify="end" style="margin-top: 16px">
        <n-pagination v-model:page="pagination.page" :page-count="Math.ceil(pagination.total / pagination.size)" @update:page="fetchData" />
      </n-space>
    </n-card>
    <n-modal v-model:show="showModal" :title="isEdit ? '编辑药品' : '新增药品'" preset="card" style="width: 640px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80" require-mark-placement="right-hanging">
        <n-divider title-placement="left" style="margin-top: 0; font-size: 14px">基本信息（必填）</n-divider>
        <n-grid :cols="2" :x-gap="16">
          <n-gi><n-form-item label="通用名" path="genericName"><n-input v-model:value="form.genericName" placeholder="请输入药品通用名" /></n-form-item></n-gi>
          <n-gi><n-form-item label="药品分类" path="categoryId"><n-select v-model:value="form.categoryId" :options="categoryOptions" filterable clearable placeholder="选择分类" /></n-form-item></n-gi>
          <n-gi><n-form-item label="生产厂家" path="manufacturer"><n-input v-model:value="form.manufacturer" placeholder="请输入生产厂家" /></n-form-item></n-gi>
          <n-gi><n-form-item label="零售价" path="retailPrice"><n-input-number v-model:value="form.retailPrice" :precision="2" :min="0" placeholder="零售价" style="width: 100%" /></n-form-item></n-gi>
          <n-gi :span="2">
            <n-form-item label="规格 / 单位" required>
              <n-space :size="8" style="width: 100%">
                <n-form-item path="specification" :show-label="false" style="flex: 1; margin-bottom: 0">
                  <n-input v-model:value="form.specification" placeholder="请输入规格，如 0.25g×24粒、100ml、10mg×30片" style="width: 100%" />
                </n-form-item>
                <span style="line-height: 34px; color: #999">/</span>
                <n-form-item path="unit" :show-label="false" style="width: 100px; margin-bottom: 0">
                  <n-select v-model:value="form.unit" :options="unitOptions" filterable placeholder="单位" />
                </n-form-item>
              </n-space>
            </n-form-item>
          </n-gi>
        </n-grid>

        <n-divider title-placement="left" style="font-size: 14px">其他信息（选填）</n-divider>
        <n-grid :cols="2" :x-gap="16">
          <n-gi><n-form-item label="商品名"><n-input v-model:value="form.tradeName" placeholder="药品商品名（选填）" /></n-form-item></n-gi>
          <n-gi><n-form-item label="剂型"><n-input v-model:value="form.dosageForm" placeholder="如：片剂、胶囊（选填）" /></n-form-item></n-gi>
          <n-gi><n-form-item label="批准文号"><n-input v-model:value="form.approvalNumber" placeholder="国药准字号（选填）" /></n-form-item></n-gi>
          <n-gi><n-form-item label="条形码"><n-input v-model:value="form.barcode" placeholder="药品条形码（选填）" /></n-form-item></n-gi>
          <n-gi><n-form-item label="采购价"><n-input-number v-model:value="form.purchasePrice" :precision="2" :min="0" placeholder="采购价（选填）" style="width: 100%" /></n-form-item></n-gi>
          <n-gi><n-form-item label="状态"><n-select v-model:value="form.status" :options="statusOptions" /></n-form-item></n-gi>
          <n-gi :span="2"><n-form-item label="药品说明"><n-input v-model:value="form.description" type="textarea" placeholder="药品功效、用法用量等说明信息（选填）" :rows="3" /></n-form-item></n-gi>
        </n-grid>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-space>
</template>

<script setup lang="ts">
import { h, ref, reactive, onMounted, computed } from 'vue';
import { NButton, NTag, NSpace, NPopconfirm } from 'naive-ui';
import type { FormInst, FormRules } from 'naive-ui';
import { fetchDrugList, createDrug, updateDrug, deleteDrug, fetchCategoryList } from '@/service/api';

const loading = ref(false);
const list = ref<any[]>([]);
const pagination = reactive({ page: 1, size: 10, total: 0 });
const search = reactive({ keyword: '', status: null as number | null });
const showModal = ref(false);
const isEdit = ref(false);
const editId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);
const form = reactive<any>({ genericName: '', tradeName: '', dosageForm: '', specification: '', manufacturer: '', approvalNumber: '', barcode: '', unit: '', purchasePrice: null, retailPrice: null, description: '', categoryId: null as number | null, status: 1 });

const rules: FormRules = {
  genericName: { required: true, message: '请输入药品通用名', trigger: 'blur' },
  categoryId: { required: true, type: 'number', message: '请选择药品分类', trigger: 'change' },
  specification: { required: true, message: '请输入药品规格', trigger: 'blur' },
  manufacturer: { required: true, message: '请输入生产厂家', trigger: 'blur' },
  unit: { required: true, message: '请选择药品单位', trigger: 'change' },
  retailPrice: { required: true, type: 'number', message: '请输入零售价', trigger: 'blur' }
};

const allCategories = ref<any[]>([]);
const categoryOptions = computed(() =>
  allCategories.value.map(c => ({
    label: c.name,
    value: c.id
  }))
);

const statusOptions = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 }
];

const unitOptions = [
  '盒', '瓶', '支', '袋', '片', '粒',
  '板', '管', '贴', '包', '只', '个',
  '对', '套', 'ml', 'g', 'kg'
].map(u => ({ label: u, value: u }));



const columns = [
  { title: '药品编码', key: 'drugCode', width: 110 },
  { title: '通用名', key: 'genericName', width: 120 },
  { title: '商品名', key: 'tradeName', width: 120 },
  { title: '药品分类', key: 'categoryName', width: 100, render: (row: any) => row.categoryName || '-' },
  { title: '规格', key: 'specification', width: 100 },
  { title: '采购价', key: 'purchasePrice', width: 80, render: (row: any) => row.purchasePrice ? `¥${row.purchasePrice}` : '-' },
  { title: '零售价', key: 'retailPrice', width: 80, render: (row: any) => row.retailPrice ? `¥${row.retailPrice}` : '-' },
  { title: '状态', key: 'status', width: 70, render: (row: any) => h(NTag, { type: row.status === 1 ? 'success' : 'warning', size: 'small' }, () => row.status === 1 ? '上架' : '下架') },
  {
    title: '操作', key: 'action', width: 150,
    render: (row: any) => h(NSpace, null, () => [
      h(NButton, { size: 'small', onClick: () => openEdit(row) }, () => '编辑'),
      h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'), default: () => '确定删除？' })
    ])
  }
];

async function fetchData() {
  loading.value = true;
  const { data, error } = await fetchDrugList({ page: pagination.page, size: pagination.size, keyword: search.keyword || undefined, status: search.status ?? undefined });
  if (!error && data) { list.value = data.records || []; pagination.total = data.total || 0; }
  loading.value = false;
}

function resetSearch() { search.keyword = ''; search.status = null; pagination.page = 1; fetchData(); }

function openAdd() {
  isEdit.value = false; editId.value = null;
  Object.assign(form, { genericName: '', tradeName: '', dosageForm: '', specification: '', manufacturer: '', approvalNumber: '', barcode: '', unit: '', purchasePrice: null, retailPrice: null, description: '', categoryId: null, status: 1 });
  showModal.value = true;
}

function openEdit(row: any) {
  isEdit.value = true; editId.value = row.id;
  Object.assign(form, { ...row });
  showModal.value = true;
}

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  if (isEdit.value && editId.value) {
    const { error } = await updateDrug(editId.value, form);
    if (!error) { showModal.value = false; window.$message?.success('更新成功'); fetchData(); }
  } else {
    const { error } = await createDrug(form);
    if (!error) { showModal.value = false; window.$message?.success('添加成功'); fetchData(); }
  }
}

async function handleDelete(id: number) {
  const { error } = await deleteDrug(id);
  if (!error) { window.$message?.success('删除成功'); fetchData(); }
}

onMounted(async () => {
  fetchData();
  const { data, error } = await fetchCategoryList();
  if (!error && data) allCategories.value = Array.isArray(data) ? data : [];
});
</script>

