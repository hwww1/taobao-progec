import axios from 'axios'

// 根据环境变量设置API地址
// 开发环境使用 /api（通过vite代理）
// 生产环境使用环境变量 VITE_API_BASE_URL
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const request = axios.create({
  baseURL: baseURL,
  timeout: 10000,
  withCredentials: true
})

request.interceptors.response.use(
  response => {
    const data = response.data
    // 如果后端已经返回了 success 字段（如上传接口），直接返回
    if (data && typeof data === 'object' && 'success' in data) {
      return data
    }
    // 否则统一包装：对象或字符串都放在 data
    return { success: true, data: data }
  },
  error => {
    // 简化错误处理
    if (error.response && error.response.data) {
      const errorData = error.response.data
      if (typeof errorData === 'object' && 'success' in errorData) {
        return errorData
      }
      return {
        success: false,
        message: errorData.message || errorData || '操作失败'
      }
    }
    return {
      success: false,
      message: '网络错误，请重试'
    }
  }
)

export default request

