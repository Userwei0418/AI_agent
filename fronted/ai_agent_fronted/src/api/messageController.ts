// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /messages */
export async function getMessages(options?: { [key: string]: any }) {
  return request<API.BaseResponseListMessage>('/messages', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /messages/${param0} */
export async function deleteMessage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteMessageParams,
  options?: { [key: string]: any }
) {
  const { messageId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/messages/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /messages/${param0}/read */
export async function markAsRead(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.markAsReadParams,
  options?: { [key: string]: any }
) {
  const { messageId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/messages/${param0}/read`, {
    method: 'PUT',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /messages/send */
export async function sendNotification(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.sendNotificationParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/messages/send', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
