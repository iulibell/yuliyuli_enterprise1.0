<template>
  <div class="video-detail">
    <div class="container">
      <div class="video-player-section">
        <video 
          ref="videoRef" 
          :src="videoInfo?.videoUrl" 
          controls 
          class="video-player"
          @play="handlePlay"
        ></video>
        <div class="video-info">
          <h1 class="video-title">{{ videoInfo?.title }}</h1>
          <div class="video-meta">
            <span class="author" @click="goToAuthor(videoInfo?.userId)">{{ videoInfo?.username }}</span>
            <span class="play-count">{{ formatPlayCount(videoInfo?.playCount || 0) }}</span>
            <span class="publish-time">{{ formatTime(videoInfo?.uploadTime) }}</span>
          </div>
          <div class="video-actions">
            <div class="action-btn" @click="handleLike" :class="{ active: isLiked }">
              <i class="el-icon-star-off"></i>
              <span>{{ videoInfo?.likeCount || 0 }}</span>
            </div>
            <div class="action-btn" @click="handleCollect" :class="{ active: isCollected }">
              <i class="el-icon-collection-tag"></i>
              <span>{{ videoInfo?.collectCount || 0 }}</span>
            </div>
            <div class="action-btn" @click="showCommentInput = !showCommentInput">
              <i class="el-icon-chat-line-round"></i>
              <span>{{ videoInfo?.commentCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 评论区 -->
      <div class="comment-section">
        <h3 class="comment-title">评论 ({{ videoInfo?.commentCount || 0 }})</h3>
        
        <!-- 发表评论 -->
        <div v-if="showCommentInput" class="comment-input-section">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
          ></el-input>
          <div class="comment-actions">
            <el-button type="primary" @click="handleComment">发表评论</el-button>
          </div>
        </div>
        
        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar">
              <img :src="comment.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="用户头像">
            </div>
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-author">{{ comment.username }}</span>
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { videoApi } from '../api/video'

const route = useRoute()
const router = useRouter()
const videoInfo = ref<any>(null)
const comments = ref<any[]>([])
const isLiked = ref(false)
const isCollected = ref(false)
const showCommentInput = ref(false)
const commentContent = ref('')

const videoUrl = computed(() => route.params.videoUrl as string)

const getVideoInfo = async () => {
  try {
    const res = await videoApi.clickVideo(videoUrl.value)
    if (res.success) {
      videoInfo.value = res.data
      isLiked.value = res.data.isLiked
      isCollected.value = res.data.isCollected
      comments.value = res.data.comments || []
    }
  } catch (error) {
    console.error('获取视频信息失败:', error)
  }
}

const handlePlay = () => {
  // 视频播放时的处理
}

const handleLike = async () => {
  try {
    const res = await videoApi.likeVideo(videoUrl.value)
    if (res.success) {
      isLiked.value = !isLiked.value
      videoInfo.value.likeCount = isLiked.value 
        ? (videoInfo.value.likeCount || 0) + 1 
        : Math.max(0, (videoInfo.value.likeCount || 0) - 1)
      ElMessage.success(isLiked.value ? '点赞成功' : '取消点赞')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleCollect = async () => {
  try {
    const res = await videoApi.collectVideo(videoUrl.value)
    if (res.success) {
      isCollected.value = !isCollected.value
      videoInfo.value.collectCount = isCollected.value 
        ? (videoInfo.value.collectCount || 0) + 1 
        : Math.max(0, (videoInfo.value.collectCount || 0) - 1)
      ElMessage.success(isCollected.value ? '收藏成功' : '取消收藏')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  try {
    const res = await videoApi.commentVideo({
      videoUrl: videoUrl.value,
      content: commentContent.value
    })
    if (res.success) {
      ElMessage.success('评论成功')
      commentContent.value = ''
      showCommentInput.value = false
      // 重新获取评论列表
      getVideoInfo()
    }
  } catch (error) {
    ElMessage.error('评论失败')
  }
}

const goToAuthor = (userId: number) => {
  router.push(`/author/${userId}`)
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
  return date.toLocaleString()
}

onMounted(() => {
  getVideoInfo()
})
</script>

<style scoped>
.video-detail {
  padding: 20px 0;
}

.video-player-section {
  margin-bottom: 30px;
}

.video-player {
  width: 100%;
  max-height: 600px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.video-title {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #333;
}

.video-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
}

.author {
  color: #409eff;
  cursor: pointer;
}

.video-actions {
  display: flex;
  gap: 40px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: color 0.3s;
}

.action-btn:hover {
  color: #409eff;
}

.action-btn.active {
  color: #409eff;
}

.action-btn i {
  font-size: 18px;
}

.comment-section {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.comment-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 20px;
  color: #333;
}

.comment-input-section {
  margin-bottom: 30px;
}

.comment-actions {
  margin-top: 10px;
  text-align: right;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-author {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
}
</style>