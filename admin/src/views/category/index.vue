<template>
  <n-card>
    <template #header><n-space><span>药品分类管理</span><n-button type="success" size="small" @click="openAdd">新增分类</n-button></n-space></template>
    <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />
    <n-modal v-model:show="showModal" :title="isEdit ? '编辑分类' : '新增分类'" preset="card" style="width: 400px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80" require-mark-placement="right-hanging">
        <n-form-item label="分类名称" path="name"><n-input v-model:value="form.name" placeholder="请输入分类名称" /></n-form-item>
        <n-form-item label="父分类"><n-select v-model:value="form.parentId" :options="parentOptions" clearable placeholder="无（顶级分类，选填）" /></n-form-item>
        <n-form-item label="排序"><n-input-number v-model:value="form.sort" :min="0" style="width: 100%" placeholder="排序序号（选填，默认0）" /></n-form-item>
      </n-form>
      <template #action><n-space justify="end"><n-button @click="showModal = false">取消</n-button><n-button type="primary" @click="handleSubmit">确定</n-button></n-space></template>
    </n-modal>
  </n-card>
</template>

<script setup lang="ts">
import { h, ref, reactive, computed, onMounted } from 'vue';
import { NButton, NSpace, NPopconfirm } from 'naive-ui';
import type { FormInst, FormRules } from 'naive-ui';
import { fetchCategoryList, createCategory, updateCategory, deleteCategory } from '@/service/api';

const loading = ref(false);
const list = ref<any[]>([]);
const showModal = ref(false);
const isEdit = ref(false);
const editId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);
const form = reactive({ name: '', parentId: 0 as number | null, sort: 0 });

const rules: FormRules = {
  name: { required: true, message: '请输入分类名称', trigger: 'blur' }
};

const parentOptions = computed(() => list.value.filter(c => c.parentId === 0).map(c => ({ label: c.name, value: c.id })));

const columns = [
  { title: '分类名称', key: 'name' },
  { title: '父分类', key: 'parentId', render: (row: any) => { const p = list.value.find(c => c.id === row.parentId); return p ? p.name : '顶级分类'; } },
  { title: '排序', key: 'sort', width: 80 },
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
  const { data, error } = await fetchCategoryList();
  if (!error && data) list.value = Array.isArray(data) ? data : [];
  loading.value = false;
}

function openAdd() { isEdit.value = false; editId.value = null; Object.assign(form, { name: '', parentId: 0, sort: 0 }); showModal.value = true; }
function openEdit(row: any) { isEdit.value = true; editId.value = row.id; Object.assign(form, { name: row.name, parentId: row.parentId, sort: row.sort }); showModal.value = true; }

async function handleSubmit() {
  try { await formRef.value?.validate(); } catch { return; }
  if (isEdit.value && editId.value) {
    const { error } = await updateCategory(editId.value, form);
    if (!error) { showModal.value = false; window.$message?.success('更新成功'); fetchData(); }
  } else {
    const { error } = await createCategory(form);
    if (!error) { showModal.value = false; window.$message?.success('添加成功'); fetchData(); }
  }
}

async function handleDelete(id: number) {
  const { error } = await deleteCategory(id);
  if (!error) { window.$message?.success('删除成功'); fetchData(); }
}

onMounted(fetchData);
</script>
