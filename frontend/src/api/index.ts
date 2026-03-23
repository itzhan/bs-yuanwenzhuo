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
