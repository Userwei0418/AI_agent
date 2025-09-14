import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import UserProfilePage from '@/pages/user/UserProfilePage.vue'
import AdminLayout from '@/pages/admin/AdminLayout.vue'
import type { RouteRecordRaw } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import ACCESS_ENUM from '@/access/accessEnum'
import checkAccess from '@/access/checkAccess'


const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: '主页',
    component: HomePage,
  },
  {
    path: '/user/login',
    name: '用户登录',
    component: UserLoginPage,
  },
  {
    path: '/user/register',
    name: '用户注册',
    component: UserRegisterPage,
  },
  {
    path: '/user/profile',
    name: '用户中心',
    component: UserProfilePage,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/AdminLayout',
    name: '后台管理',
    component: AdminLayout,
    meta: { access: ACCESS_ENUM.ADMIN, requiresAuth: true }
  },
  {
    path: '/admin/userManage',
    name: '用户管理',
    component: UserManagePage,
    meta: { access: ACCESS_ENUM.ADMIN, requiresAuth: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/Forbidden.vue')
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  const loginUser = loginUserStore.loginUser

  try {
    if (to.matched.some(record => record.meta.requiresAuth)) {
      if (!loginUser) {
        // 防止无限重定向
        if (to.path !== '/user/login') {
          next({ path: '/user/login', query: { redirect: to.fullPath } })
        } else {
          next()
        }
      } else {
        const needAccess = (to.meta?.access as string | undefined) ?? ACCESS_ENUM.NOT_LOGIN

        if (!checkAccess(loginUser, needAccess)) {
          next('/403')
        } else {
          next()
        }
      }
    } else {
      next()
    }
  } catch (error) {
    console.error('Error in beforeEach guard:', error)
    next('/403')
  }
})

export default router
