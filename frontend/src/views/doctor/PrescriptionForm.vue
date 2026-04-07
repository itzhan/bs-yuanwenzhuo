<template>
  <div class="prescription-form-page">
    <section class="page-section">
      <div class="container">
        <div class="page-header fade-in-up">
          <div>
            <h1>{{ isEdit ? '修改处方' : '新建处方' }}</h1>
            <p>填写患者信息、诊断结果和药品明细</p>
          </div>
          <n-button @click="$router.back()">
            <template #icon><n-icon><ArrowLeft :size="16" /></n-icon></template>
            返回
          </n-button>
        </div>

        <n-card class="fade-in-up" :bordered="false">
          <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
            <h3 class="section-title">患者信息</h3>
            <n-grid :cols="4" :x-gap="16">
              <n-gi>
                <n-form-item path="patientName" label="姓名">
                  <n-input v-model:value="form.patientName" placeholder="患者姓名" />
                </n-form-item>
              </n-gi>
              <n-gi>
                <n-form-item label="性别">
                  <n-select v-model:value="form.patientGender" :options="genderOptions" placeholder="请选择" clearable />
                </n-form-item>
              </n-gi>
              <n-gi>
                <n-form-item label="年龄">
                  <n-input-number v-model:value="form.patientAge" :min="0" :max="150" placeholder="岁" style="width:100%" />
                </n-form-item>
              </n-gi>
              <n-gi>
                <n-form-item label="联系电话">
                  <n-input v-model:value="form.patientPhone" placeholder="手机号" />
                </n-form-item>
              </n-gi>
            </n-grid>

            <h3 class="section-title">诊断与备注</h3>
            <n-form-item label="诊断">
              <n-input v-model:value="form.diagnosis" type="textarea" :autosize="{ minRows: 2 }" placeholder="请输入诊断结果" />
            </n-form-item>
            <n-form-item label="备注">
              <n-input v-model:value="form.remark" type="textarea" :autosize="{ minRows: 2 }" placeholder="其他说明（可选）" />
            </n-form-item>

            <h3 class="section-title">
              药品明细
              <n-button size="small" type="primary" ghost @click="addItem" style="margin-left:12px">
                <template #icon><n-icon><Plus :size="14" /></n-icon></template>
                添加药品
              </n-button>
            </h3>
            <n-empty v-if="form.items.length === 0" description="请至少添加一条药品" />
            <div v-for="(item, idx) in form.items" :key="idx" class="drug-row">
              <n-grid :cols="24" :x-gap="12">
                <n-gi :span="8">
                  <n-form-item :label="idx === 0 ? '药品' : ''" :show-feedback="false">
                    <n-select
                      v-model:value="item.drugId"
                      filterable
                      remote
                      clearable
                      :options="drugOptions"
                      :loading="drugLoading"
                      placeholder="点击或输入药品名称搜索"
                      @search="onDrugSearch"
                      @focus="onDrugFocus"
                      @update:value="(v: any) => onSelectDrug(item, v)"
                    />
                  </n-form-item>
                </n-gi>
                <n-gi :span="3">
                  <n-form-item :label="idx === 0 ? '数量' : ''" :show-feedback="false">
                    <n-input-number v-model:value="item.quantity" :min="1" placeholder="数量" style="width:100%" />
                  </n-form-item>
                </n-gi>
                <n-gi :span="6">
                  <n-form-item :label="idx === 0 ? '用法用量' : ''" :show-feedback="false">
                    <n-input v-model:value="item.dosage" placeholder="如：每次1片 一日3次" />
                  </n-form-item>
                </n-gi>
                <n-gi :span="5">
                  <n-form-item :label="idx === 0 ? '备注' : ''" :show-feedback="false">
                    <n-input v-model:value="item.usageNote" placeholder="使用说明" />
                  </n-form-item>
                </n-gi>
                <n-gi :span="2">
                  <n-form-item :label="idx === 0 ? '操作' : ''" :show-feedback="false">
                    <n-button text type="error" @click="removeItem(idx)">
                      <template #icon><n-icon><Trash2 :size="16" /></n-icon></template>
                    </n-button>
                  </n-form-item>
                </n-gi>
              </n-grid>
            </div>

            <div class="form-actions">
              <n-button @click="$router.back()">取消</n-button>
              <n-button type="primary" :loading="submitting" @click="onSubmit">
                {{ isEdit ? '保存修改' : '开具处方' }}
              </n-button>
            </div>
          </n-form>
        </n-card>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { Plus, Trash2, ArrowLeft } from 'lucide-vue-next'
import {
  createPrescription, updatePrescription, getPrescription, searchDrugs
} from '../../api'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const isEdit = computed(() => !!route.params.id)
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)

const form = ref<any>({
  patientName: '',
  patientGender: null,
  patientAge: null,
  patientPhone: '',
  diagnosis: '',
  remark: '',
  items: [] as any[]
})

const rules = {
  patientName: { required: true, message: '请输入患者姓名', trigger: 'blur' }
}

const genderOptions = [
  { label: '男', value: 1 },
  { label: '女', value: 0 }
]

const drugOptions = ref<any[]>([])
const drugLoading = ref(false)
let searchTimer: any = null

async function fetchDrugs(query: string) {
  drugLoading.value = true
  try {
    const res: any = await searchDrugs({ page: 1, size: 30, keyword: query || undefined })
    const incoming = (res.data?.records || []).map((d: any) => ({
      label: `${d.genericName}${d.specification ? ' · ' + d.specification : ''}`,
      value: d.id,
      raw: d
    }))
    // 合并：保留已选项，避免回显丢失
    const existing = drugOptions.value.filter(o => !incoming.some((i: any) => i.value === o.value))
    drugOptions.value = [...incoming, ...existing]
  } catch (e: any) {
    message.error(e.message || '搜索药品失败')
  } finally {
    drugLoading.value = false
  }
}

function onDrugSearch(query: string) {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => fetchDrugs(query), 300)
}

function onDrugFocus() {
  // 首次 focus 或下拉为空时，自动拉一批
  if (drugOptions.value.length === 0) {
    fetchDrugs('')
  }
}

function onSelectDrug(item: any, drugId: any) {
  const opt = drugOptions.value.find(o => o.value === drugId)
  if (opt?.raw) {
    item._drugName = opt.raw.genericName
  }
}

function addItem() {
  form.value.items.push({ drugId: null, quantity: 1, dosage: '', usageNote: '' })
}

function removeItem(idx: number | string) {
  form.value.items.splice(Number(idx), 1)
}

async function onSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  if (!form.value.items.length) {
    message.warning('请至少添加一条药品')
    return
  }
  for (const it of form.value.items) {
    if (!it.drugId) {
      message.warning('请为每条记录选择药品')
      return
    }
    if (!it.quantity || it.quantity <= 0) {
      message.warning('请填写有效数量')
      return
    }
  }

  submitting.value = true
  try {
    const payload = {
      patientName: form.value.patientName,
      patientGender: form.value.patientGender,
      patientAge: form.value.patientAge,
      patientPhone: form.value.patientPhone,
      diagnosis: form.value.diagnosis,
      remark: form.value.remark,
      items: form.value.items.map((it: any) => ({
        drugId: it.drugId,
        quantity: it.quantity,
        dosage: it.dosage,
        usageNote: it.usageNote
      }))
    }
    if (isEdit.value) {
      await updatePrescription(route.params.id as string, payload)
      message.success('修改成功')
    } else {
      await createPrescription(payload)
      message.success('开具成功')
    }
    router.push('/prescription')
  } catch (e: any) {
    message.error(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

async function loadDetail(id: string) {
  try {
    const res: any = await getPrescription(id)
    const d = res.data
    form.value = {
      patientName: d.patientName,
      patientGender: d.patientGender,
      patientAge: d.patientAge,
      patientPhone: d.patientPhone,
      diagnosis: d.diagnosis,
      remark: d.remark,
      items: (d.items || []).map((it: any) => ({
        drugId: it.drugId,
        quantity: it.quantity,
        dosage: it.dosage,
        usageNote: it.usageNote
      }))
    }
    // 预置下拉选项，回显用
    drugOptions.value = (d.items || []).map((it: any) => ({
      label: `${it.drugName}${it.specification ? ' · ' + it.specification : ''}`,
      value: it.drugId,
      raw: { id: it.drugId, genericName: it.drugName, specification: it.specification }
    }))
  } catch (e: any) {
    message.error(e.message || '加载失败')
  }
}

onMounted(() => {
  if (isEdit.value) {
    loadDetail(route.params.id as string)
  } else {
    addItem()
  }
  // 预加载一批药品，避免首次点开下拉为空
  fetchDrugs('')
})
</script>

<style scoped>
.page-section { padding: 40px 0 60px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-header h1 { font-size: 28px; margin: 0; color: var(--text-primary); }
.page-header p { color: var(--text-muted); margin: 6px 0 0; font-size: 14px; }
.section-title {
  font-size: 16px;
  margin: 24px 0 12px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
}
.drug-row {
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--border-color);
  margin-bottom: 8px;
}
.drug-row:last-child { border-bottom: none; }
.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
