import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'drugs', name: 'DrugList', component: () => import('../views/DrugList.vue') },
      { path: 'drugs/:id', name: 'DrugDetail', component: () => import('../views/DrugDetail.vue') },
      { path: 'about', name: 'About', component: () => import('../views/About.vue') }
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

export default router
