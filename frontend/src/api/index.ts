import request from './request'

/** 公开药品列表 */
export function getPublicDrugs(params: {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number | null
}) {
  return request.get('/public/drugs', { params })
}

/** 公开药品详情 */
export function getPublicDrugDetail(id: number) {
  return request.get(`/public/drugs/${id}`)
}

/** 公开分类树 */
export function getPublicCategories() {
  return request.get('/public/categories')
}

/** 公开统计数据 */
export function getPublicStats() {
  return request.get('/public/stats')
}

/** 登录 */
export function login(data: { username: string; password: string }) {
  return request.post('/auth/login', data)
}

/** 获取当前用户信息 */
export function getUserInfo() {
  return request.get('/auth/info')
}

/* ========== 处方管理（医师） ========== */

/** 处方列表（默认只看自己的） */
export function listPrescriptions(params: {
  page?: number
  size?: number
  keyword?: string
  status?: number | null
  mineOnly?: boolean
}) {
  return request.get('/prescriptions', { params })
}

/** 处方详情 */
export function getPrescription(id: number | string) {
  return request.get(`/prescriptions/${id}`)
}

/** 开具处方 */
export function createPrescription(data: any) {
  return request.post('/prescriptions', data)
}

/** 修改处方 */
export function updatePrescription(id: number | string, data: any) {
  return request.put(`/prescriptions/${id}`, data)
}

/** 作废处方 */
export function voidPrescription(id: number | string) {
  return request.put(`/prescriptions/${id}/void`)
}

/** 查询患者用药记录 */
export function getPatientMedication(params: { patientName?: string; patientPhone?: string }) {
  return request.get('/prescriptions/patient', { params })
}

/** 搜索药品（复用 admin 侧药品接口） */
export function searchDrugs(params: { page?: number; size?: number; keyword?: string }) {
  return request.get('/drugs', { params })
}
