<template>
  <div class="upload">
    <div class="container">
      <div class="upload-form">
        <h2>上传视频</h2>
        <el-form :model="uploadForm" :rules="rules" ref="uploadFormRef">
          <el-form-item label="视频标题" prop="title">
            <el-input v-model="uploadForm.title" placeholder="请输入视频标题" />
          </el-form-item>
          <el-form-item label="视频分类" prop="type">
            <el-select v-model="uploadForm.type" placeholder="请选择视频分类">
              <el-option label="番剧" value="1" />
              <el-option label="综艺" value="2" />
              <el-option label="科技" value="3" />
              <el-option label="生活" value="4" />
              <el-option label="音乐" value="5" />
              <el-option label="舞蹈" value="6" />
              <el-option label="美食" value="7" />
              <el-option label="汽车" value="8" />
              <el-option label="体育" value="9" />
              <el-option label="游戏" value="10" />
            </el-select>
          </el-form-item>
          <el-form-item label="视频文件" prop="file">
            <el-upload
              class="upload-demo"
              action=""
              :auto-upload="false"
              :on-change="handleFileChange"
              :show-file-list="false"
              accept=".mp4,.flv,.avi,.mov"
            >
              <el-button type="primary" class="bg-blue-500 hover:bg-blue-600 text-white">选择视频文件</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持上传 mp4、flv、avi、mov 格式的视频文件，最大 3G
                </div>
              </template>
            </el-upload>
            <div v-if="selectedFile" class="selected-file">
              <span>{{ selectedFile.name }}</span>
              <el-button type="danger" size="small" @click="clearFile"
                style="margin-left: 15px;">删除</el-button>
            </div>
          </el-form-item>
          <el-form-item label="封面图片" prop="cover">
            <el-upload
              class="upload-cover"
              action=""
              :auto-upload="false"
              :on-change="handleCoverChange"
              :show-file-list="false"
              accept=".jpg,.jpeg,.png"
            >
              <el-button type="primary" class="bg-blue-500 hover:bg-blue-600 text-white">选择封面图片</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持上传 jpg、jpeg、png 格式的图片文件，建议尺寸 16:9
                </div>
              </template>
            </el-upload>
            <div v-if="selectedCover" class="selected-file">
              <span>{{ selectedCover.name }}</span>
              <el-button type="danger" size="small" @click="clearCover"
                style="margin-left: 15px;">删除</el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="upload-btn bg-blue-500 hover:bg-blue-600 text-white" @click="handleUpload" :loading="uploading">
              {{ uploading ? '上传中...' : '上传视频' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { videoApi } from '../api/video'

const router = useRouter()
const uploadFormRef = ref()
const selectedFile = ref<File | null>(null)
const selectedCover = ref<File | null>(null)
const uploading = ref(false)

const uploadForm = reactive({
  title: '',
  type: '',
  file: null,
  cover: null
})

const rules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度2-50位', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择视频分类', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请选择视频文件', trigger: 'blur' }
  ],
  cover: [
    { required: true, message: '请选择封面图片', trigger: 'blur' }
  ]
}

const handleFileChange = (file: any) => {
  if (file.size > 3 * 1024 * 1024 * 1024) {
    ElMessage.error('视频文件大小不能超过3G')
    return
  }
  selectedFile.value = file.raw
  uploadForm.file = file.raw
}

const handleCoverChange = (file: any) => {
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('封面图片大小不能超过10MB')
    return
  }
  selectedCover.value = file.raw
  uploadForm.cover = file.raw
}

const clearFile = () => {
  selectedFile.value = null
  uploadForm.file = null
}

const clearCover = () => {
  selectedCover.value = null
  uploadForm.cover = null
}

const handleUpload = async () => {
  if (uploadFormRef.value) {
    await uploadFormRef.value.validate(async (valid: boolean) => {
      if (valid && selectedFile.value) {
        uploading.value = true
        try {
          const formData = new FormData()
          formData.append('file', selectedFile.value)
          formData.append('video.title', uploadForm.title)
          formData.append('video.type', uploadForm.type)
          if (selectedCover.value) {
            formData.append('video.cover', selectedCover.value)
          }
          
          const res = await videoApi.uploadVideo(formData)
          if (res.success) {
            ElMessage.success('视频上传成功')
            router.push('/')
          } else {
            ElMessage.error(res.message)
          }
        } catch (error) {
          ElMessage.error('上传失败，请重试')
        } finally {
          uploading.value = false
        }
      }
    })
  }
}
</script>

<style scoped>
.upload {
  padding: 60px 0;
}

.upload-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.selected-file {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-file .el-button--danger {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

.selected-file .el-button--danger:hover {
  background-color: #f78989;
  border-color: #f78989;
}

.upload-btn {
  width: 100%;
}
</style>