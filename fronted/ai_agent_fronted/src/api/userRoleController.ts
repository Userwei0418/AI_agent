// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /user-roles */
export async function getAllUserRoles(options?: { [key: string]: any }) {
  return request<API.BaseResponseListUserRole>('/user-roles', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user-roles */
export async function createUserRole(body: API.UserRole, options?: { [key: string]: any }) {
  return request<API.BaseResponseUserRole>('/user-roles', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /user-roles/${param0}/${param1} */
export async function deleteUserRole(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteUserRoleParams,
  options?: { [key: string]: any }
) {
  const { userId: param0, roleId: param1, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/user-roles/${param0}/${param1}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}
