import { createRouter, createWebHistory } from 'vue-router'
import { getUserInfo } from '../api'

const routes = [
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'drugs', name: 'DrugList', component: () => import('../views/DrugList.vue') },
      { path: 'drugs/:id', name: 'DrugDetail', component: () => import('../views/DrugDetail.vue') },
      { path: 'about', name: 'About', component: () => import('../views/About.vue') },
      {
        path: 'prescription',
        name: 'PrescriptionList',
        component: () => import('../views/doctor/PrescriptionList.vue'),
        meta: { requiresDoctor: true }
      },
      {
        path: 'prescription/create',
        name: 'PrescriptionCreate',
        component: () => import('../views/doctor/PrescriptionForm.vue'),
        meta: { requiresDoctor: true }
      },
      {
        path: 'prescription/:id/edit',
        name: 'PrescriptionEdit',
        component: () => import('../views/doctor/PrescriptionForm.vue'),
        meta: { requiresDoctor: true }
      },
      {
        path: 'records',
        name: 'MedicalRecord',
        component: () => import('../views/doctor/MedicalRecord.vue'),
        meta: { requiresDoctor: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 简单缓存当前用户 role，避免每次路由都请求 /auth/info
let cachedRole: number | null = null
export function clearCachedRole() {
  cachedRole = null
}

router.beforeEach(async (to) => {
  if (!to.meta?.requiresDoctor) return true
  const token = localStorage.getItem('token')
  if (!token) {
    return { path: '/login' }
  }
  if (cachedRole === null) {
    try {
      const res: any = await getUserInfo()
      cachedRole = res.data?.role ?? null
    } catch {
      localStorage.removeItem('token')
      return { path: '/login' }
    }
  }
  if (cachedRole !== 1) {
    return { path: '/' }
  }
  return true
})

export default router
