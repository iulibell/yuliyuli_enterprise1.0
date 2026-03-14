import axios from 'axios'

// 定义业务响应类型
interface ApiResponse<T = any> {
  success: boolean
  message?: string
  data: T
}

const api = axios.create({
  baseURL: '',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  (config: any) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error: any) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response: any) => {
    const data = response.data
    // 转换后端响应结构为前端期望的结构
    if (data.code !== undefined) {
      return {
        success: data.code === 200,
        message: data.msg || data.message,
        data: data.data
      }
    }
    return data
  },
  (error: any) => {
    return Promise.reject(error)
  }
)

export default api
export type { ApiResponse }
