<template>
  <n-space vertical :size="16">
    <n-card>
      <n-space>
        <n-input v-model:value="search.keyword" placeholder="搜索用户" clearable style="width: 200px" />
        <n-button type="primary" @click="fetchData">搜索</n-button>
        <n-button @click="resetSearch">重置</n-button>
        <n-button type="success" @click="openAdd">新增用户</n-button>
      </n-space>
    </n-card>
    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />
      <n-space justify="end" style="margin-top: 16px">
        <n-pagination v-model:page="pagination.page" :page-count="Math.ceil(pagination.total / pagination.size)" @update:page="fetchData" />
      </n-space>
    </n-card>
    <n-modal v-model:show="showModal" :title="isEdit ? '编辑用户' : '新增用户'" preset="card" style="width: 500px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80" require-mark-placement="right-hanging">
        <n-form-item label="用户名" path="username"><n-input v-model:value="form.username" :disabled="isEdit" placeholder="请输入用户名" /></n-form-item>
        <n-form-item v-if="!isEdit" label="密码" path="password"><n-input v-model:value="form.password" type="password" placeholder="请输入密码" /></n-form-item>
        <n-form-item label="姓名" path="realName"><n-input v-model:value="form.realName" placeholder="请输入真实姓名" /></n-form-item>
        <n-form-item label="角色" path="role"><n-select v-model:value="form.role" :options="roleOptions" placeholder="选择角色" /></n-form-item>
        <n-form-item label="电话"><n-input v-model:value="form.phone" placeholder="手机号码（选填）" /></n-form-item>
        <n-form-item label="邮箱"><n-input v-model:value="form.email" placeholder="邮箱地址（选填）" /></n-form-item>
        <n-form-item label="状态"><n-select v-model:value="form.status" :options="[{label:'启用',value:1},{label:'禁用',value:0}]" /></n-form-item>
      </n-form>
      <template #action><n-space justify="end"><n-button @click="showModal = false">取消</n-button><n-button type="primary" @click="handleSubmit">确定</n-button></n-space></template>
    </n-modal>
  </n-space>
</template>

<script setup lang="ts">
import { h, ref, reactive, onMounted } from 'vue';
import { NButton, NTag, NSpace, NPopconfirm } from 'naive-ui';
import type { FormInst, FormRules } from 'naive-ui';
import { fetchUserList, createUser, updateUser, deleteUser } from '@/service/api';

const loading = ref(false);
const list = ref<any[]>([]);
const pagination = reactive({ page: 1, size: 10, total: 0 });
const search = reactive({ keyword: '' });
const showModal = ref(false);
const isEdit = ref(false);
const editId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);
const form = reactive({ username: '', password: '', realName: '', phone: '', email: '', role: 0, status: 1 });

const rules: FormRules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
  realName: { required: true, message: '请输入真实姓名', trigger: 'blur' },
  role: { required: true, type: 'number', message: '请选择角色', trigger: 'change' }
};

const roleOptions = [
  { label: '管理员', value: 0 }, { label: '药剂师', value: 1 }, { label: '仓库管理员', value: 2 }
];
const roleMap: Record<number, { label: string; type: any }> = {
  0: { label: '管理员', type: 'info' }, 1: { label: '药剂师', type: 'success' }, 2: { label: '仓库管理员', type: 'warning' }
};

const columns = [
  { title: '用户名', key: 'username', width: 100 },
  { title: '姓名', key: 'realName', width: 80 },
  { title: '电话', key: 'phone', width: 120 },
  { title: '邮箱', key: 'email', width: 150 },
  { title: '角色', key: 'role', width: 100, render: (row: any) => { const r = roleMap[row.role]; return h(NTag, { type: r?.type || 'default', size: 'small' }, () => r?.label || '未知'); } },
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
  const { data, error } = await fetchUserList({ page: pagination.page, size: pagination.size, keyword: search.keyword || undefined });
  if (!error && data) { list.value = data.records || []; pagination.total = data.total || 0; }
  loading.value = false;
}

function resetSearch() { search.keyword = ''; pagination.page = 1; fetchData(); }
function openAdd() { isEdit.value = false; editId.value = null; Object.assign(form, { username: '', password: '123456', realName: '', phone: '', email: '', role: 0, status: 1 }); showModal.value = true; }
function openEdit(row: any) { isEdit.value = true; editId.value = row.id; Object.assign(form, { ...row, password: '' }); showModal.value = true; }

async function handleSubmit() {
  try { await formRef.value?.validate(); } catch { return; }
  if (isEdit.value && editId.value) {
    const { error } = await updateUser(editId.value, form);
    if (!error) { showModal.value = false; window.$message?.success('更新成功'); fetchData(); }
  } else {
    const { error } = await createUser(form);
    if (!error) { showModal.value = false; window.$message?.success('添加成功'); fetchData(); }
  }
}

async function handleDelete(id: number) {
  const { error } = await deleteUser(id);
  if (!error) { window.$message?.success('删除成功'); fetchData(); }
}

onMounted(fetchData);
</script>
