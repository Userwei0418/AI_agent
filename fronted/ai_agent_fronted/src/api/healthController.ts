// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /user/login */
export async function userLoginBySession(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/user/login', {
    method: 'GET',
    ...(options || {}),
  })
}
