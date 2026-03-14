<template>
  <div class="author">
    <div class="container">
      <!-- 作者信息 -->
      <div class="author-info">
        <div class="author-avatar">
          <img :src="authorInfo?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="作者头像">
        </div>
        <div class="author-details">
          <h2 class="author-name">{{ authorInfo?.username }}</h2>
          <div class="author-stats">
            <span>视频: {{ authorInfo?.videoCount || 0 }}</span>
            <span>粉丝: {{ authorInfo?.followerCount || 0 }}</span>
            <span>关注: {{ authorInfo?.followingCount || 0 }}</span>
          </div>
          <el-button 
            v-if="!isFollowing" 
            type="primary" 
            @click="handleFollow"
          >
            关注
          </el-button>
          <el-button 
            v-else 
            type="default" 
            @click="handleFollow"
          >
            已关注
          </el-button>
        </div>
      </div>
      
      <!-- 作者视频列表 -->
      <div class="author-videos">
        <h3 class="section-title">作者视频</h3>
        <div class="video-grid">
          <div 
            v-for="video in authorVideos" 
            :key="video.videoUrl"
            class="video-card"
            @click="goToVideo(video.videoUrl)"
          >
            <img :src="video.coverUrl" alt="视频封面" class="video-cover">
            <div class="video-info">
              <h3 class="video-title">{{ video.title }}</h3>
              <div class="video-meta">
                <span>{{ formatPlayCount(video.playCount) }}</span>
                <span>{{ formatTime(video.uploadTime) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authorInfo = ref<any>(null)
const authorVideos = ref<any[]>([])
const isFollowing = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const userId = route.params.userId as string

const getAuthorInfo = async () => {
  try {
    // 这里需要调用后端接口获取作者信息
    // 假设后端有一个 /info/authorPage 接口
    const res = await fetch(`/api/info/authorPage?userId=${userId}`)
    const data = await res.json()
    if (data.success) {
      authorInfo.value = data.data
      isFollowing.value = data.data.isFollowing
      authorVideos.value = data.data.videos || []
      total.value = data.data.total || 0
    }
  } catch (error) {
    console.error('获取作者信息失败:', error)
  }
}

const handleFollow = async () => {
  try {
    // 这里需要调用后端关注接口
    const res = await fetch('/api/user/follow', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ userId })
    })
    const data = await res.json()
    if (data.success) {
      isFollowing.value = !isFollowing.value
      authorInfo.value.followerCount = isFollowing.value 
        ? (authorInfo.value.followerCount || 0) + 1 
        : Math.max(0, (authorInfo.value.followerCount || 0) - 1)
      ElMessage.success(isFollowing.value ? '关注成功' : '取消关注')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goToVideo = (videoUrl: string) => {
  router.push(`/video/${videoUrl}`)
}

const formatPlayCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  getAuthorInfo()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  getAuthorInfo()
}

onMounted(() => {
  getAuthorInfo()
})
</script>

<style scoped>
.author {
  padding: 40px 0;
}

.author-info {
  display: flex;
  gap: 30px;
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.author-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-details {
  flex: 1;
}

.author-name {
  font-size: 24px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #333;
}

.author-stats {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}

.section-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 20px;
  color: #333;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>