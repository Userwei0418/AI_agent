import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { currentUser } from '@/api/userController.ts'
import ACCESS_ENUM from '@/access/accessEnum.ts'

/**
 * 存储登录用户信息的状态
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.UserVO>({
    userName: '未登录',
    roles: []
  })

  /**
   * 获取登录用户
   */
  async function fetchLoginUser() {
    const res = await currentUser()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  const isAdmin = computed(() => {
    return loginUser.value.roles?.includes(ACCESS_ENUM.ADMIN) ?? false
  })

  return { loginUser, isAdmin, setLoginUser, fetchLoginUser }
})
