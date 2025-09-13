// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /roles */
export async function getAllRoles(options?: { [key: string]: any }) {
  return request<API.BaseResponseListRole>('/roles', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /roles */
export async function createRole(body: API.Role, options?: { [key: string]: any }) {
  return request<API.BaseResponseRole>('/roles', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /roles/${param0} */
export async function updateRole(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateRoleParams,
  body: API.Role,
  options?: { [key: string]: any }
) {
  const { roleId: param0, ...queryParams } = params
  return request<API.BaseResponseRole>(`/roles/${param0}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /roles/${param0} */
export async function deleteRole(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteRoleParams,
  options?: { [key: string]: any }
) {
  const { roleId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/roles/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}
