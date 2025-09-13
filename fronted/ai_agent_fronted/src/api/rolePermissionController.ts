// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /role-permissions */
export async function getAllRolePermissions(options?: { [key: string]: any }) {
  return request<API.BaseResponseListRolePermission>('/role-permissions', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /role-permissions */
export async function createRolePermission(
  body: API.RolePermission,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseRolePermission>('/role-permissions', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /role-permissions/${param0}/${param1} */
export async function deleteRolePermission(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteRolePermissionParams,
  options?: { [key: string]: any }
) {
  const { roleId: param0, permissionId: param1, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/role-permissions/${param0}/${param1}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}
