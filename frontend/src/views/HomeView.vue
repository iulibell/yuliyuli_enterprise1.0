<template>
  <div class="home">
    <div class="container relative">   
      <!-- 视频网格 -->
      <div class="video-grid grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 relative z-10">
        <div 
          v-for="video in videoList" 
          :key="video.videoUrl"
          class="video-card cursor-pointer group"
          @click="goToVideo(video.videoUrl)"
        >
          <div class="relative">
            <img :src="video.coverUrl" alt="视频封面" class="video-cover w-full aspect-video object-cover rounded-md">
          </div>
          <div class="video-info mt-2">
            <h3 class="video-title text-sm font-medium line-clamp-2 group-hover:text-deeppink-500">
              {{ video.title }}
            </h3>
            <div class="video-meta mt-1 text-xs text-gray-500">
              <div class="flex items-center mb-1">
                <img :src="video.avatar" alt="用户头像" class="w-4 h-4 rounded-full mr-1">
                <span class="line-clamp-1">{{ video.username }}</span>
              </div>
              <div class="flex items-center">
                <span>{{ formatPlayCount(video.playCount) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination flex justify-center mt-8"
        style="margin-top: 470px;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          prev-text="上一页"
          next-text="下一页"
          total-text="共 {total} 条"
          jumper-text="前往 {page}"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { videoApi } from '../api/video'
import { searchApi } from '../api/search'

const router = useRouter()
const hotVideos = ref<any[]>([])
const videoList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getHotVideos = async () => {
  try {
    const res = await searchApi.getTopTenVideo()
    if (res.success) {
      hotVideos.value = res.data
    }
  } catch (error) {
    console.error('获取热门视频失败:', error)
  }
}

const getVideoList = async () => {
  try {
    const res = await videoApi.getVideoList(currentPage.value, pageSize.value)
    if (res.success) {
      videoList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取视频列表失败:', error)
  }
}

const goToVideo = (id: string) => {
  router.push(`/video/${id}`)
}

const formatPlayCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  getVideoList()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  getVideoList()
}

onMounted(() => {
  getHotVideos()
  getVideoList()
})
</script>

<style scoped>
/* Tailwind CSS 已替代原有样式 */
</style>