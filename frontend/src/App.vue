<template>
  <div class="app min-h-screen flex flex-col">
    <header class="header bg-white shadow-sm sticky top-0 z-50">
      <!-- 顶部导航 -->
      <div class="container py-4">
        <div class="flex items-center justify-between">
          <div class="logo" style="margin-right: -20px;">
            <span style="margin-left: -40px;" class="text-2xl font-bold text-deeppink-500">yuliyuli</span>
            <router-link to="/" class="px-4 py-2 font-medium" :class="!route.query.category ? 'text-deeppink-500' : 'text-gray-600 hover:text-deeppink-500'"
            style="margin-left: 30px;">
            <el-icon class="mr-1"><House /></el-icon>首页</router-link>
          </div>
          <div class="search-bar">
            <input 
              v-model="searchKeyword" 
              type="text" 
              placeholder="搜索视频" 
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch"><el-icon class="mr-1"><Search /></el-icon>搜索</button>
          </div>
          <div class="user-actions flex items-center gap-5">
            <template v-if="isLoggedIn">
              <span class="text-sm text-gray-700">{{ userInfo?.username }}</span>
            </template>
            <template v-else>
              <router-link to="/login" class="px-4 py-2 text-deeppink-500 border border-deeppink-500 rounded-md hover:bg-deeppink-50">
                <el-icon class="mr-1"><Avatar /></el-icon>登录</router-link>
              <router-link to="/register" class="px-4 py-2 text-deeppink-500 border border-deeppink-500 rounded-md hover:bg-deeppink-50">
                <el-icon class="mr-1"><Coffee /></el-icon>注册</router-link>
            </template>
              <router-link to="/upload" class="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-md">
                <el-icon class="mr-1"><Upload /></el-icon>上传视频
              </router-link>
          </div>
        </div>
      </div>
      
      <!-- 分类导航 -->
      <div class="bg-white border-t border-gray-200">
        <div class="container">
          <div class="flex items-center overflow-x-auto whitespace-nowrap py-2">
            <router-link to="/?category=hot" class="px-4 py-2" :class="route.query.category === 'hot' ? 'text-deeppink-500 font-medium' : 'text-gray-600 hover:text-deeppink-500'">热门</router-link>
            <router-link to="/?category=anime" class="px-4 py-2" :class="route.query.category === 'anime' ? 'text-deeppink-500 font-medium' : 'text-gray-600 hover:text-deeppink-500'">番剧</router-link>
            <router-link to="/?category=variety" class="px-4 py-2" :class="route.query.category === 'variety' ? 'text-deeppink-500 font-medium' : 'text-gray-600 hover:text-deeppink-500'">综艺</router-link>
            <router-link to="/?category=game" class="px-4 py-2" :class="route.query.category === 'game' ? 'text-deeppink-500 font-medium' : 'text-gray-600 hover:text-deeppink-500'">游戏</router-link>
            <router-link to="/?category=tech" class="px-4 py-2" :class="route.query.category === 'tech' ? 'text-deeppink-500 font-medium' : 'text-gray-600 hover:text-deeppink-500'">科技</router-link>
            <router-link to="/?category=life" class="px-4 py-2" :class="route.query.category === 'life' ? 'text-deeppink-500 font-medium' : 'text-gray-600 hover:text-deeppink-500'">生活</router-link>
            <router-link to="/?category=music" class="px-4 py-2" :class="route.query.category === 'music' ? 'text-deeppink-500 font-medium' : 'text-gray-600 hover:text-deeppink-500'">音乐</router-link>
            <router-link to="/?category=dance" class="px-4 py-2" :class="route.query.category === 'dance' ? 'text-deeppink-500 font-medium' : 'text-gray-600 hover:text-deeppink-500'">舞蹈</router-link>
            <router-link to="/?category=food" class="px-4 py-2" :class="route.query.category === 'food' ? 'text-deeppink-500 font-medium' : 'text-gray-600 hover:text-deeppink-500'">美食</router-link>
            <router-link to="/?category=car" class="px-4 py-2" :class="route.query.category === 'car' ? 'text-deeppink-500 font-medium' : 'text-gray-600 hover:text-deeppink-500'">汽车</router-link>
            <router-link to="/?category=sports" class="px-4 py-2" :class="route.query.category === 'sports' ? 'text-deeppink-500 font-medium' : 'text-gray-600 hover:text-deeppink-500'">体育</router-link>
          </div>
        </div>
      </div>
    </header>
    
    <main class="main flex-1 py-5">
      <router-view />
    </main>
    
    <footer class="footer bg-gray-800 text-white py-2">
      <div class="container">
        <p class="text-center">© 2026 yuliyuli - 版权所有</p>        
        <p class="text-center">如有任何问题请联系我们:<span style="color: deepskyblue;">1913760871@qq.com</span></p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { userApi } from './api/user'
import { Search } from '@element-plus/icons-vue'
import { Upload } from '@element-plus/icons-vue'
import { Avatar } from '@element-plus/icons-vue'
import { Coffee } from '@element-plus/icons-vue'
import { House } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const searchKeyword = ref('')
const userInfo = ref<any>(null)

const isLoggedIn = computed(() => {
  return localStorage.getItem('token') !== null
})

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push(`/search?keyword=${encodeURIComponent(searchKeyword.value)}`)
  }
}

const getUserInfo = async () => {
  if (isLoggedIn.value) {
    try {
      const res = await userApi.getUserInfo()
      if (res.success) {
        userInfo.value = res.data
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
}

onMounted(() => {
  getUserInfo()
})
</script>

<style scoped>
/* Tailwind CSS 已替代原有样式 */
</style>