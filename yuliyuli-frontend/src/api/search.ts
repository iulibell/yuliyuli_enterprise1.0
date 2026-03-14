import api from './index'
import type { ApiResponse } from './index'

interface Video {
  id: number
  title: string
  videoUrl: string
  coverUrl: string
  username: string
  playCount: number
  likeCount: number
  collectCount: number
  commentCount: number
  uploadTime: string
}

interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const searchApi = {
  // 获取热门视频
  getTopTenVideo: (): Promise<ApiResponse<Video[]>> => api.get('/api/search/topTenVideo'),
  
  // ES 搜索视频
  searchVideoES: (keyword: string, page: number, size: number): Promise<ApiResponse<PageResult<Video>>> => api.get('/api/search/video', {
    params: { keyword, page, size }
  })
}