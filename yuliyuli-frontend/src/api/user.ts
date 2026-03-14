import api from './index'
import type { ApiResponse } from './index'

interface LoginData {
  phone: string
  password: string
}

interface RegisterData {
  phone: string
  password: string
  username: string
}

interface UserInfo {
  id: number
  username: string
  avatar: string
  phone: string
  token: string
}

export const userApi = {
  // 登录
  login: (data: LoginData): Promise<ApiResponse<UserInfo>> => api.post('/api/user/login', data),
  
  // 注册
  register: (data: RegisterData, code: string): Promise<ApiResponse<{ userId: number }>> => api.post('/api/user/register', data, {
    params: { code }
  }),
  
  // 获取验证码
  getCode: (phone: string): Promise<ApiResponse<void>> => api.post('/api/user/getCode', { phone }),
  
  // 修改用户信息
  modifyInfo: (data: any): Promise<ApiResponse<void>> => api.post('/api/user/modifyInfo', data),
  
  // 获取用户信息
  getUserInfo: (): Promise<ApiResponse<UserInfo>> => api.get('/api/user/info')
}