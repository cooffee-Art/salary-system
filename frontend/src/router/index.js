import { createRouter, createWebHistory } from 'vue-router'

import Login from '../views/Login.vue'
import Home from '../views/Home.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    name: 'home',
    component: Home,
    meta: { title: '登录成功' },
  },
  // 兜底：未知路径回到登录页
  { path: '/:pathMatch(.*)*', redirect: '/login' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 全局前置守卫。
 *
 * 需求里写的是「/ 重定向到 /login」，但如果把 `/` 静态重定向到 /login，
 * 登录成功后 router.push('/') 会又被打回登录页，形成死循环。
 * 因此这里用守卫实现同样的效果：
 *   - 未登录访问 `/`  →  重定向到 /login
 *   - 已登录访问 `/`  →  正常进入 Home（登录成功页）
 *   - 已登录再访问 /login → 直接回首页
 */
router.beforeEach((to) => {
  const token = localStorage.getItem('token')

  if (to.meta.public) {
    return to.path === '/login' && token ? { path: '/' } : true
  }

  return token ? true : { path: '/login' }
})

export default router
