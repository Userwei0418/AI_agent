import type { Directive } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import checkAccess from '@/access/checkAccess'

const permissionDirective: Directive = {
  mounted(el, binding) {
    const loginUserStore = useLoginUserStore()
    const loginUser = loginUserStore.loginUser

    const unsubscribe = loginUserStore.$subscribe(() => {
      updatePermission()
    })

    function updatePermission() {
      if (!loginUser) {
        el.style.display = 'none'
        return
      }

      const requiredAccess = binding.value // 期望的权限枚举值（如 ACCESS_ENUM.ADMIN）

      if (!checkAccess(loginUser, requiredAccess)) {
        el.style.display = 'none'
      } else {
        el.style.display = ''
      }
    }

    // 初始检查
    updatePermission()

    // 清理监听
    el.addEventListener('hook:beforeUnmount', () => {
      unsubscribe()
    })
  }
}

export default permissionDirective
