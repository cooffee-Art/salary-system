import { createRouter, createWebHistory } from 'vue-router'

import Login from '../views/Login.vue'
import Layout from '../layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/employees',
    children: [
      {
        path: 'employees',
        name: 'employees',
        component: () => import('../views/EmployeeList.vue'),
        meta: { title: '员工管理' },
      },
      {
        path: 'salaries',
        name: 'salaries',
        component: () => import('../views/SalaryList.vue'),
        meta: { title: '薪资管理' },
      },
      {
        path: 'salary-report',
        name: 'salary-report',
        component: () => import('../views/SalaryReport.vue'),
        meta: { title: '部门报表' },
      },
    ],
  },
  // 兜底：未知路径回登录页（已登录会被守卫再转到 /employees）
  { path: '/:pathMatch(.*)*', redirect: '/login' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 全局前置守卫：
 * - 未登录访问任何非 /login 页面 → 重定向 /login
 * - 已登录访问 /login → 重定向 /employees
 */
router.beforeEach((to) => {
  const token = localStorage.getItem('token')

  if (to.path === '/login') {
    return token ? { path: '/employees' } : true
  }

  return token ? true : { path: '/login' }
})

export default router
