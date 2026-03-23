<template>
  <n-space vertical :size="16">
    <n-card>
      <n-space>
        <n-input v-model:value="search.keyword" placeholder="搜索单号" clearable style="width: 200px" />
        <n-select v-model:value="search.status" :options="statusOptions" placeholder="状态" clearable style="width: 120px" />
        <n-button type="primary" @click="fetchData">搜索</n-button>
        <n-button @click="resetSearch">重置</n-button>
        <n-button type="success" @click="showCreate = true">新增采购单</n-button>
      </n-space>
    </n-card>
    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />
      <n-space justify="end" style="margin-top: 16px">
        <n-pagination v-model:page="pagination.page" :page-count="Math.ceil(pagination.total / pagination.size)" @update:page="fetchData" />
      </n-space>
    </n-card>

    <!-- 新增采购单 Modal -->
    <n-modal v-model:show="showCreate" title="新增采购单" preset="card" style="width: 700px">
      <n-form ref="formRef" :model="createForm" :rules="poRules" label-placement="left" label-width="80" require-mark-placement="right-hanging">
        <n-form-item label="供应商" path="supplierId">
          <n-select
            v-model:value="createForm.supplierId"
            :options="supplierOptions"
            filterable
            placeholder="搜索供应商名称"
            style="width: 100%"
          />
        </n-form-item>
        <n-form-item label="备注"><n-input v-model:value="createForm.remark" placeholder="采购备注信息（选填）" /></n-form-item>
        <n-form-item label="采购明细">
          <n-space vertical style="width: 100%">
            <n-space v-for="(item, i) in createForm.items" :key="i" align="center">
              <n-select
                v-model:value="item.drugId"
                :options="drugOptions"
                filterable
                placeholder="搜索药品"
                style="width: 200px"
              />
              <n-input-number v-model:value="item.quantity" placeholder="数量" :min="1" style="width: 100px" />
              <n-input-number v-model:value="item.unitPrice" placeholder="单价" :min="0" :precision="2" style="width: 120px" />
              <n-button type="error" size="small" @click="createForm.items.splice(i, 1)">删除</n-button>
            </n-space>
            <n-button dashed @click="createForm.items.push({ drugId: null, quantity: 1, unitPrice: 0 })">+ 添加明细</n-button>
          </n-space>
        </n-form-item>
      </n-form>
      <template #action><n-space justify="end"><n-button @click="showCreate = false">取消</n-button><n-button type="primary" @click="handleCreate">提交</n-button></n-space></template>
    </n-modal>

    <!-- 采购单详情 Modal（用于审核/收货确认） -->
    <n-modal v-model:show="showDetail" :title="detailTitle" preset="card" style="width: 700px">
      <n-spin :show="detailLoading">
        <template v-if="detailData">
          <n-descriptions bordered :column="2" label-placement="left">
            <n-descriptions-item label="采购单号">{{ detailData.orderNo }}</n-descriptions-item>
            <n-descriptions-item label="状态">
              <n-tag :type="statusMap[detailData.status]?.type || 'default'" size="small">{{ statusMap[detailData.status]?.label || '未知' }}</n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="供应商">{{ detailData.supplierName }}</n-descriptions-item>
            <n-descriptions-item label="总金额">¥{{ detailData.totalAmount || 0 }}</n-descriptions-item>
            <n-descriptions-item label="创建人">{{ detailData.creatorName }}</n-descriptions-item>
            <n-descriptions-item label="创建时间">{{ detailData.createTime }}</n-descriptions-item>
            <n-descriptions-item v-if="detailData.remark" label="备注" :span="2">{{ detailData.remark }}</n-descriptions-item>
          </n-descriptions>
          <n-divider>采购明细</n-divider>
          <n-data-table
            :columns="detailItemColumns"
            :data="detailData.items || []"
            :bordered="true"
            size="small"
          />
        </template>
      </n-spin>
      <template #action>
        <n-space justify="end">
          <n-button @click="showDetail = false">关闭</n-button>
          <n-button v-if="detailAction === 'approve'" type="success" @click="confirmApprove">确认审核</n-button>
          <n-button v-if="detailAction === 'receive'" type="warning" @click="confirmReceive">确认收货</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-space>
</template>

<script setup lang="ts">
import { h, ref, reactive, onMounted, computed } from 'vue';
import { NButton, NTag, NSpace, NPopconfirm } from 'naive-ui';
import type { FormInst, FormRules } from 'naive-ui';
import { fetchPurchaseOrderList, fetchPurchaseOrderDetail, createPurchaseOrder, approvePurchaseOrder, receivePurchaseOrder, cancelPurchaseOrder, deletePurchaseOrder, fetchAllDrugs, fetchAllSuppliers } from '@/service/api';

const loading = ref(false);
const list = ref<any[]>([]);
const pagination = reactive({ page: 1, size: 10, total: 0 });
const search = reactive({ keyword: '', status: null as number | null });
const showCreate = ref(false);
const formRef = ref<FormInst | null>(null);
const createForm = reactive<any>({ supplierId: null, remark: '', items: [{ drugId: null, quantity: 1, unitPrice: 0 }] });

const poRules: FormRules = {
  supplierId: { required: true, type: 'number', message: '请选择供应商', trigger: 'change' }
};

// 详情弹窗
const showDetail = ref(false);
const detailLoading = ref(false);
const detailData = ref<any>(null);
const detailAction = ref<'approve' | 'receive' | 'view'>('view');
const detailTitle = computed(() => {
  if (detailAction.value === 'approve') return '审核采购单';
  if (detailAction.value === 'receive') return '确认收货';
  return '采购单详情';
});

const allDrugs = ref<any[]>([]);
const allSuppliers = ref<any[]>([]);

const drugOptions = computed(() =>
  allDrugs.value.map(d => ({
    label: `${d.genericName}${d.specification ? ' (' + d.specification + ')' : ''}`,
    value: d.id
  }))
);

const supplierOptions = computed(() =>
  allSuppliers.value.map(s => ({
    label: s.name,
    value: s.id
  }))
);

const statusOptions = [
  { label: '待审核', value: 0 }, { label: '已审核', value: 1 },
  { label: '已入库', value: 2 }, { label: '已取消', value: 3 }
];
const statusMap: Record<number, { label: string; type: any }> = {
  0: { label: '待审核', type: 'info' }, 1: { label: '已审核', type: 'success' },
  2: { label: '已入库', type: 'default' }, 3: { label: '已取消', type: 'error' }
};

const detailItemColumns = [
  { title: '药品名称', key: 'drugName', width: 150 },
  { title: '数量', key: 'quantity', width: 80 },
  { title: '单价', key: 'unitPrice', width: 100, render: (row: any) => `¥${row.unitPrice || 0}` },
  { title: '小计', key: 'subtotal', width: 100, render: (row: any) => `¥${((row.quantity || 0) * (row.unitPrice || 0)).toFixed(2)}` }
];

const columns = [
  { title: '采购单号', key: 'orderNo', width: 180 },
  { title: '供应商', key: 'supplierName', width: 120 },
  { title: '总金额', key: 'totalAmount', width: 100, render: (row: any) => `¥${row.totalAmount || 0}` },
  { title: '状态', key: 'status', width: 80, render: (row: any) => { const s = statusMap[row.status]; return h(NTag, { type: s?.type || 'default', size: 'small' }, () => s?.label || '未知'); } },
  { title: '创建人', key: 'creatorName', width: 80 },
  { title: '创建时间', key: 'createTime', width: 160 },
  {
    title: '操作', key: 'action', width: 280,
    render: (row: any) => h(NSpace, null, () => {
      const btns: any[] = [];
      btns.push(h(NButton, { size: 'small', onClick: () => openDetail(row.id, 'view') }, () => '详情'));
      if (row.status === 0) {
        btns.push(h(NButton, { size: 'small', type: 'success', onClick: () => openDetail(row.id, 'approve') }, () => '审核'));
        btns.push(h(NPopconfirm, { onPositiveClick: () => handleCancel(row.id) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '取消'), default: () => '确定取消？' }));
      }
      if (row.status === 1) {
        btns.push(h(NButton, { size: 'small', type: 'warning', onClick: () => openDetail(row.id, 'receive') }, () => '收货'));
      }
      btns.push(h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'), default: () => '确定删除？' }));
      return btns;
    })
  }
];

async function fetchData() {
  loading.value = true;
  const { data, error } = await fetchPurchaseOrderList({ page: pagination.page, size: pagination.size, keyword: search.keyword || undefined, status: search.status ?? undefined });
  if (!error && data) { list.value = data.records || []; pagination.total = data.total || 0; }
  loading.value = false;
}

function resetSearch() { search.keyword = ''; search.status = null; pagination.page = 1; fetchData(); }

async function openDetail(id: number, action: 'approve' | 'receive' | 'view') {
  detailAction.value = action;
  detailData.value = null;
  showDetail.value = true;
  detailLoading.value = true;
  const { data, error } = await fetchPurchaseOrderDetail(id);
  if (!error && data) { detailData.value = data; }
  detailLoading.value = false;
}

async function handleCreate() {
  try { await formRef.value?.validate(); } catch { return; }
  const { error } = await createPurchaseOrder(createForm);
  if (!error) { showCreate.value = false; window.$message?.success('创建成功'); fetchData(); }
}

async function confirmApprove() {
  if (!detailData.value) return;
  const { error } = await approvePurchaseOrder(detailData.value.id);
  if (!error) { showDetail.value = false; window.$message?.success('审核成功'); fetchData(); }
}

async function confirmReceive() {
  if (!detailData.value) return;
  const { error } = await receivePurchaseOrder(detailData.value.id);
  if (!error) { showDetail.value = false; window.$message?.success('收货成功'); fetchData(); }
}

async function handleCancel(id: number) { const { error } = await cancelPurchaseOrder(id); if (!error) { window.$message?.success('取消成功'); fetchData(); } }
async function handleDelete(id: number) { const { error } = await deletePurchaseOrder(id); if (!error) { window.$message?.success('删除成功'); fetchData(); } }

onMounted(async () => {
  fetchData();
  const [drugsRes, suppliersRes] = await Promise.all([fetchAllDrugs(), fetchAllSuppliers()]);
  if (!drugsRes.error && drugsRes.data) allDrugs.value = drugsRes.data;
  if (!suppliersRes.error && suppliersRes.data) allSuppliers.value = suppliersRes.data;
});
</script>
