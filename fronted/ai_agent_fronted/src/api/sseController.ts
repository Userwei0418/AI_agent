// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /sse/connect */
export async function connect(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.connectParams,
  options?: { [key: string]: any }
) {
  return request<API.SseEmitter>('/sse/connect', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
