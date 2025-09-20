import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * 格式化时间
 * @param time 时间字符串
 * @param format 格式化字符串，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的时间字符串，如果时间为空则返回空字符串
 */
// export const formatTime = (time: string | undefined, format = 'YYYY-MM-DD HH:mm:ss'): string => {
//   if (!time) return ''
//   return dayjs(time).format(format)
// }

/**
 * 格式化时间
 * @param time 时间，可以是字符串、数组或 undefined
 * @param format 格式化字符串，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的时间字符串，如果时间为空则返回空字符串
 */
export const formatTime = (time: string | number[] | undefined, format = 'YYYY-MM-DDTHH:mm:ss'): string => {
  if (!time) return ''

  // 如果是数组格式的时间，转换为 Date 对象
  if (Array.isArray(time)) {
    const [year, month, day, hour, minute, second] = time
    return dayjs(new Date(year, month - 1, day, hour, minute, second)).format(format)  // 生成 ISO 格式
  }

  return dayjs(time).format(format)  // 默认格式化为 ISO 格式
}


/**
 * 格式化时间为相对时间
 * @param time 时间字符串
 * @returns 相对时间字符串，如 "2小时前"
 */
export const formatRelativeTime = (time: string | undefined): string => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

/**
 * 格式化时间为日期
 * @param time 时间字符串
 * @returns 日期字符串，如 "2024-01-01"
 */
export const formatDate = (time: string | undefined): string => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD')
}
