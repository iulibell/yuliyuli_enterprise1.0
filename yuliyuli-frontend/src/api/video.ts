import api from './index'
import type { ApiResponse } from './index'

interface CommentData {
  videoUrl: string
  content: string
}

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
  isLiked: boolean
  isCollected: boolean
  comments?: Array<{
    id: number
    content: string
    username: string
    avatar: string
    createTime: string
  }>
}

interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const videoApi = {
  // 上传视频
  uploadVideo: (data: FormData): Promise<ApiResponse<void>> => api.post('/api/video/delivery', data, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),
  
  // 视频点赞
  likeVideo: (videoUrl: string): Promise<ApiResponse<void>> => api.post('/api/video/like', { videoUrl }),
  
  // 视频收藏
  collectVideo: (videoUrl: string): Promise<ApiResponse<void>> => api.post('/api/video/collect', { videoUrl }),
  
  // 发表评论
  commentVideo: (data: CommentData): Promise<ApiResponse<void>> => api.post('/api/video/comment', data),
  
  // 获取视频列表
  getVideoList: (page: number, size: number): Promise<ApiResponse<PageResult<Video>>> => api.get('/api/video/videoList', {
    params: { page, size }
  }),
  
  // 搜索视频
  searchVideo: (keyword: string, page: number, size: number): Promise<ApiResponse<PageResult<Video>>> => api.get('/api/video/clickSearch', {
    params: { keyword, page, size }
  }),
  
  // 点击视频
  clickVideo: (videoUrl: string): Promise<ApiResponse<Video>> => api.get(`/api/video/clickVideo/${videoUrl}`),
  
  // 删除视频
  deleteVideo: (videoUrl: string): Promise<ApiResponse<void>> => api.post('/api/info/videoDelete', { videoUrl })
}