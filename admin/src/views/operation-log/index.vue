<template>
  <n-space vertical :size="16">
    <n-card>
      <n-space>
        <n-input v-model:value="search.username" placeholder="用户名" clearable style="width: 150px" />
        <n-input v-model:value="search.module" placeholder="操作模块" clearable style="width: 150px" />
        <n-button type="primary" @click="fetchData">搜索</n-button>
        <n-button @click="resetSearch">重置</n-button>
      </n-space>
    </n-card>
    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />
      <n-space justify="end" style="margin-top: 16px">
        <n-pagination v-model:page="pagination.page" :page-count="Math.ceil(pagination.total / pagination.size)" @update:page="fetchData" />
      </n-space>
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { fetchOperationLogList } from '@/service/api';

const loading = ref(false);
const list = ref<any[]>([]);
const pagination = reactive({ page: 1, size: 10, total: 0 });
const search = reactive({ username: '', module: '' });

const columns = [
  { title: '用户名', key: 'username', width: 100 },
  { title: '操作模块', key: 'module', width: 100 },
  { title: '操作类型', key: 'action', width: 100 },
  { title: '描述', key: 'description' },
  { title: 'IP', key: 'ip', width: 120 },
  { title: '时间', key: 'createTime', width: 160 }
];

async function fetchData() {
  loading.value = true;
  const { data, error } = await fetchOperationLogList({ page: pagination.page, size: pagination.size, username: search.username || undefined, module: search.module || undefined });
  if (!error && data) { list.value = data.records || []; pagination.total = data.total || 0; }
  loading.value = false;
}

function resetSearch() { search.username = ''; search.module = ''; pagination.page = 1; fetchData(); }

onMounted(fetchData);
</script>
