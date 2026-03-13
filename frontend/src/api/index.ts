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
    return response.data
  },
  (error: any) => {
    if (error.response && error.response.status === 401) {
      // 未登录，跳转到登录页
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
export type { ApiResponse }