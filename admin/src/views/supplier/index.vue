<template>
  <n-space vertical :size="16">
    <n-card>
      <n-space>
        <n-input v-model:value="search.keyword" placeholder="搜索供应商" clearable style="width: 200px" />
        <n-button type="primary" @click="fetchData">搜索</n-button>
        <n-button @click="resetSearch">重置</n-button>
        <n-button type="success" @click="openAdd">新增供应商</n-button>
      </n-space>
    </n-card>
    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />
      <n-space justify="end" style="margin-top: 16px">
        <n-pagination v-model:page="pagination.page" :page-count="Math.ceil(pagination.total / pagination.size)" @update:page="fetchData" />
      </n-space>
    </n-card>
    <n-modal v-model:show="showModal" :title="isEdit ? '编辑供应商' : '新增供应商'" preset="card" style="width: 500px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80" require-mark-placement="right-hanging">
        <n-form-item label="名称" path="name"><n-input v-model:value="form.name" placeholder="请输入供应商名称" /></n-form-item>
        <n-form-item label="联系人" path="contactPerson"><n-input v-model:value="form.contactPerson" placeholder="请输入联系人姓名" /></n-form-item>
        <n-form-item label="电话" path="phone"><n-input v-model:value="form.phone" placeholder="请输入联系电话" /></n-form-item>
        <n-form-item label="邮箱"><n-input v-model:value="form.email" placeholder="邮箱地址（选填）" /></n-form-item>
        <n-form-item label="地址"><n-input v-model:value="form.address" type="textarea" placeholder="供应商地址（选填）" /></n-form-item>
      </n-form>
      <template #action><n-space justify="end"><n-button @click="showModal = false">取消</n-button><n-button type="primary" @click="handleSubmit">确定</n-button></n-space></template>
    </n-modal>
  </n-space>
</template>

<script setup lang="ts">
import { h, ref, reactive, onMounted } from 'vue';
import { NButton, NTag, NSpace, NPopconfirm } from 'naive-ui';
import type { FormInst, FormRules } from 'naive-ui';
import { fetchSupplierList, createSupplier, updateSupplier, deleteSupplier } from '@/service/api';

const loading = ref(false);
const list = ref<any[]>([]);
const pagination = reactive({ page: 1, size: 10, total: 0 });
const search = reactive({ keyword: '' });
const showModal = ref(false);
const isEdit = ref(false);
const editId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);
const form = reactive({ name: '', contactPerson: '', phone: '', email: '', address: '', status: 1 });

const rules: FormRules = {
  name: { required: true, message: '请输入供应商名称', trigger: 'blur' },
  contactPerson: { required: true, message: '请输入联系人', trigger: 'blur' },
  phone: { required: true, message: '请输入联系电话', trigger: 'blur' }
};

const columns = [
  { title: '供应商名称', key: 'name' },
  { title: '联系人', key: 'contactPerson', width: 100 },
  { title: '电话', key: 'phone', width: 120 },
  { title: '邮箱', key: 'email', width: 150 },
  { title: '状态', key: 'status', width: 70, render: (row: any) => h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, () => row.status === 1 ? '启用' : '禁用') },
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
  const { data, error } = await fetchSupplierList({ page: pagination.page, size: pagination.size, keyword: search.keyword || undefined });
  if (!error && data) { list.value = data.records || []; pagination.total = data.total || 0; }
  loading.value = false;
}

function resetSearch() { search.keyword = ''; pagination.page = 1; fetchData(); }
function openAdd() { isEdit.value = false; editId.value = null; Object.assign(form, { name: '', contactPerson: '', phone: '', email: '', address: '', status: 1 }); showModal.value = true; }
function openEdit(row: any) { isEdit.value = true; editId.value = row.id; Object.assign(form, { ...row }); showModal.value = true; }

async function handleSubmit() {
  try { await formRef.value?.validate(); } catch { return; }
  if (isEdit.value && editId.value) {
    const { error } = await updateSupplier(editId.value, form);
    if (!error) { showModal.value = false; window.$message?.success('更新成功'); fetchData(); }
  } else {
    const { error } = await createSupplier(form);
    if (!error) { showModal.value = false; window.$message?.success('添加成功'); fetchData(); }
  }
}

async function handleDelete(id: number) {
  const { error } = await deleteSupplier(id);
  if (!error) { window.$message?.success('删除成功'); fetchData(); }
}

onMounted(fetchData);
</script>
