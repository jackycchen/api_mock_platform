import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/layout/Layout.vue'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '控制台', requiresAuth: true }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/project/ProjectList.vue'),
        meta: { title: '项目管理', requiresAuth: true }
      },
      {
        path: 'projects/:id/apis',
        name: 'ProjectApis',
        component: () => import('@/views/api/ApiList.vue'),
        meta: { title: '接口管理', requiresAuth: true }
      },
      {
        path: 'projects/:id/settings',
        name: 'ProjectSettings',
        component: () => import('@/views/project/ProjectSettings.vue'),
        meta: { title: '项目设置', requiresAuth: true }
      },
      {
        path: 'projects/:projectId/documentation',
        name: 'ProjectDocumentation',
        component: () => import('@/views/documentation/DocumentationPage.vue'),
        meta: { title: 'API文档', requiresAuth: true }
      },
      {
        path: 'projects/:id/statistics',
        name: 'AccessStatistics',
        component: () => import('@/views/statistics/AccessStatistics.vue'),
        meta: { title: '访问统计', requiresAuth: true }
      },
      {
        path: 'mock',
        name: 'Mock',
        component: () => import('@/views/mock/MockList.vue'),
        meta: { title: 'Mock管理', requiresAuth: true }
      },
      {
        path: 'data',
        name: 'Data',
        component: () => import('@/views/data/DataManagement.vue'),
        meta: { title: '数据管理', requiresAuth: true }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('@/views/monitor/Monitor.vue'),
        meta: { title: '监控统计', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn

  // 检查是否需要登录
  if (to.meta.requiresAuth && !isLoggedIn) {
    // 需要登录但未登录，跳转到登录页
    next({ name: 'Login' })
  } else if ((to.name === 'Login' || to.name === 'Register') && isLoggedIn) {
    // 已登录但访问登录或注册页，跳转到首页
    next({ name: 'Dashboard' })
  } else {
    // 其他情况正常跳转
    next()
  }
})

export default router