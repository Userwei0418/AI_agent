declare namespace API {
  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponseListMessage = {
    code?: number
    data?: Message[]
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponseListPermission = {
    code?: number
    data?: Permission[]
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponseListRole = {
    code?: number
    data?: Role[]
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponseListRolePermission = {
    code?: number
    data?: RolePermission[]
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponseListUserRole = {
    code?: number
    data?: UserRole[]
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponsePermission = {
    code?: number
    data?: Permission
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponseRole = {
    code?: number
    data?: Role
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponseRolePermission = {
    code?: number
    data?: RolePermission
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponseUserRole = {
    code?: number
    data?: UserRole
    message?: string
    description?: string
    timestamp?: number
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
    description?: string
    timestamp?: number
  }

  type connectParams = {
    userId: number
  }

  type deleteMessageParams = {
    messageId: number
  }

  type deletePermissionParams = {
    permissionId: number
  }

  type deleteRoleParams = {
    roleId: number
  }

  type deleteRolePermissionParams = {
    roleId: number
    permissionId: number
  }

  type deleteUserParams = {
    userId: number
  }

  type deleteUserRoleParams = {
    userId: number
    roleId: number
  }

  type markAsReadParams = {
    messageId: number
  }

  type Message = {
    id?: number
    userId?: number
    title?: string
    content?: string
    type?: number
    isRead?: number
    createTime?: string
  }

  type OrderItem = {
    column?: string
    asc?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageUserVO
    searchCount?: PageUserVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type Permission = {
    id?: number
    name?: string
    code?: string
  }

  type resetUserPasswordParams = {
    userId: number
  }

  type Role = {
    id?: number
    name?: '普通用户' | '会员' | '管理员'
  }

  type RolePermission = {
    roleId?: number
    permissionId?: number
  }

  type searchUsersParams = {
    queryRequest: UserQueryRequest
  }

  type sendNotificationParams = {
    userId?: number
    title: string
    content: string
  }

  type SseEmitter = {
    timeout?: number
  }

  type updatePermissionParams = {
    permissionId: number
  }

  type updateRoleParams = {
    roleId: number
  }

  type updateUserStatusParams = {
    userId: number
    status: string
  }

  type UserLoginRequest = {
    userAccount: string
    userPassword: string
  }

  type UserPasswordUpdateRequest = {
    oldPassword: string
    newPassword: string
  }

  type UserQueryRequest = {
    page?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    keyword?: string
  }

  type UserRegisterRequest = {
    userAccount: string
    userPassword: string
    checkPassword: string
    inviteCode?: string
    passwordMatching?: boolean
  }

  type UserRole = {
    userId?: number
    roleId?: number
  }

  type UserRoleRequest = {
    userId?: number
    roleName?: string
  }

  type UserUpdateRequest = {
    id: number
    userName?: string
    userGender?: string
    userAvatar?: string
    userPhone?: string
    userEmail?: string
    userProfile?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userGender?: string
    userAvatar?: string
    userPhone?: string
    userEmail?: string
    userProfile?: string
    shareCode?: string
    inviteUserNumber?: number
    createTime?: string
    updateTime?: string
    roles?: string[]
    permissions?: string[]
    userStatus?: string
  }
}
