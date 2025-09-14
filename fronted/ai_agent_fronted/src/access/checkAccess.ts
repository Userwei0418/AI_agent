import ACCESS_ENUM from '@/access/accessEnum'

const checkAccess = (loginUser: any, needAccess = ACCESS_ENUM.NOT_LOGIN) => {
  const loginUserRoles = loginUser?.roles || []

  if (loginUserRoles.includes(ACCESS_ENUM.ADMIN)) {
    return true
  }

  switch (needAccess) {
    case ACCESS_ENUM.NOT_LOGIN:
      return true
    case ACCESS_ENUM.USER:
      return loginUserRoles.length > 0
    case ACCESS_ENUM.VIP:
      return loginUserRoles.includes(ACCESS_ENUM.VIP)
    case ACCESS_ENUM.ADMIN:
      return loginUserRoles.includes(ACCESS_ENUM.ADMIN)
    default:
      return false
  }
}

export default checkAccess
