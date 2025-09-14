import router from '@/router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import ACCESS_ENUM from './accessEnum'
import checkAccess from './checkAccess'

router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser

  // 如果之前没登陆过，自动登录
  if (!loginUser || !loginUser.roles) {
    await loginUserStore.fetchLoginUser();
    loginUser = loginUserStore.loginUser;
  }

  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN

  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    if (!loginUser || !loginUser.roles || loginUser.roles.length === 0) {
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }

    if (!checkAccess(loginUser, needAccess)) {
      next('/noAuth')
      return
    }
  }

  next()
})
