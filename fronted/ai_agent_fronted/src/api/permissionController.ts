// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /permissions */
export async function getAllPermissions(options?: { [key: string]: any }) {
  return request<API.BaseResponseListPermission>('/permissions', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /permissions */
export async function createPermission(body: API.Permission, options?: { [key: string]: any }) {
  return request<API.BaseResponsePermission>('/permissions', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /permissions/${param0} */
export async function updatePermission(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updatePermissionParams,
  body: API.Permission,
  options?: { [key: string]: any }
) {
  const { permissionId: param0, ...queryParams } = params
  return request<API.BaseResponsePermission>(`/permissions/${param0}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /permissions/${param0} */
export async function deletePermission(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deletePermissionParams,
  options?: { [key: string]: any }
) {
  const { permissionId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/permissions/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}
