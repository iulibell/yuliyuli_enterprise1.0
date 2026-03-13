import api from './index'
import type { ApiResponse } from './index'

interface LoginData {
  phone: string
  password: string
}

interface RegisterData {
  phone: string
  code: string
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
  login: (data: LoginData): Promise<ApiResponse<UserInfo>> => api.post('/user/login', data),
  
  // 注册
  register: (data: RegisterData): Promise<ApiResponse<{ userId: number }>> => api.post('/user/register', data),
  
  // 获取验证码
  getCode: (phone: string): Promise<ApiResponse<void>> => api.post('/user/getCode', { phone }),
  
  // 修改用户信息
  modifyInfo: (data: any): Promise<ApiResponse<void>> => api.post('/user/modifyInfo', data),
  
  // 获取用户信息
  getUserInfo: (): Promise<ApiResponse<UserInfo>> => api.get('/user/info')
}