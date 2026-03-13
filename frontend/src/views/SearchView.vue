<template>
  <div class="search">
    <div class="container">
      <div class="search-bar">
        <input 
          v-model="searchKeyword" 
          type="text" 
          placeholder="搜索视频" 
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
      
      <div class="search-results">
        <h2 class="results-title">搜索结果: {{ searchKeyword }}</h2>
        
        <div class="video-grid">
          <div 
            v-for="video in searchResults" 
            :key="video.videoUrl"
            class="video-card"
            @click="goToVideo(video.videoUrl)"
          >
            <img :src="video.coverUrl" alt="视频封面" class="video-cover">
            <div class="video-info">
              <h3 class="video-title">{{ video.title }}</h3>
              <div class="video-meta">
                <span>{{ video.username }}</span>
                <span>{{ formatPlayCount(video.playCount) }}</span>
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
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { videoApi } from '../api/video'
import { searchApi } from '../api/search'

const router = useRouter()
const route = useRoute()
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const handleSearch = async () => {
  if (searchKeyword.value.trim()) {
    currentPage.value = 1
    await searchVideos()
  }
}

const searchVideos = async () => {
  try {
    // 优先使用 ES 搜索
    const res = await searchApi.searchVideoES(searchKeyword.value, currentPage.value, pageSize.value)
    if (res.success) {
      searchResults.value = res.data.records
      total.value = res.data.total
    } else {
      // 回退到普通搜索
      const fallbackRes = await videoApi.searchVideo(searchKeyword.value, currentPage.value, pageSize.value)
      if (fallbackRes.success) {
        searchResults.value = fallbackRes.data.records
        total.value = fallbackRes.data.total
      }
    }
  } catch (error) {
    console.error('搜索失败:', error)
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

const handleSizeChange = (size: number) => {
  pageSize.value = size
  searchVideos()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  searchVideos()
}

// 从路由参数中获取搜索关键词
watch(
  () => route.query.keyword,
  (newKeyword) => {
    if (newKeyword) {
      searchKeyword.value = newKeyword as string
      handleSearch()
    }
  },
  { immediate: true }
)

onMounted(() => {
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword as string
    handleSearch()
  }
})
</script>

<style scoped>
.search {
  padding: 40px 0;
}

.search-bar {
  max-width: 600px;
  margin: 0 auto 40px;
}

.results-title {
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