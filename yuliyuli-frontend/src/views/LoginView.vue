<template>
  <div class="login py-15">
    <div class="container">
      <div class="login-form max-w-md mx-auto bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-2xl font-semibold text-center mb-6 text-gray-800">登录</h2>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="loginForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" 
            style="margin-left: 14px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="w-full mb-2 bg-blue-500 hover:bg-blue-600 text-white" @click="handleLogin">登录</el-button>
            <el-button type="default" class="w-full bg-gray-100 hover:bg-gray-200 text-gray-700"
             @click="goToRegister"
             style="margin-left: 0px;">去注册</el-button>
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
import { userApi } from '../api/user'

const router = useRouter()
const loginFormRef = ref()
const loginForm = reactive({
  phone: '',
  password: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { len: 11, message: '手机号必须为11位', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (loginFormRef.value) {
    await loginFormRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          const res = await userApi.login(loginForm)
          if (res.success) {
            localStorage.setItem('token', res.data.token)
            ElMessage.success('登录成功')
            router.push('/')
          } else {
            ElMessage.error(res.message)
          }
        } catch (error) {
          ElMessage.error('登录失败，请重试')
        }
      }
    })
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login {
  padding: 60px 0;
}

.login-form {
  max-width: 400px;
  margin: 0 auto;
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.login-btn {
  width: 100%;
  margin-bottom: 10px;
}

.register-btn {
  width: 100%;
}
</style>