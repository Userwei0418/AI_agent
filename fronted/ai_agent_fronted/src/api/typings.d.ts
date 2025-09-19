declare namespace API {
  type AppCreateRequest = {
    initPrompt: string
    codeGenType?: string
  }

  type AppDeployRequest = {
    appId: number
  }

  type AppQueryRequest = {
    page?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    codeGenType?: string
    userId?: number
    keyword?: string
  }

  type AppUpdateRequest = {
    id: number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    createTime?: string
    updateTime?: string
    user?: UserVO
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
    description?: string
    timestamp?: number
  }

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

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
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

  type BaseResponseString = {
    code?: number
    data?: string
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

  type chatToGenCodeParams = {
    appId: number
    message: string
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

  type DeleteRequest = {
    id: number
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

  type getAllAppsParams = {
    queryRequest: AppQueryRequest
  }

  type getAppByIdParams = {
    appId: number
  }

  type getFeaturedAppsParams = {
    queryRequest: AppQueryRequest
  }

  type getMyAppsParams = {
    queryRequest: AppQueryRequest
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

  type PageAppVO = {
    records?: AppVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageAppVO
    searchCount?: PageAppVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
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

  type ServerSentEventString = true

  type serveStaticResourceParams = {
    deployKey: string
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
