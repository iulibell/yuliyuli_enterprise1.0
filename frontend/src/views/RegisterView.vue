<template>
  <div class="register py-15">
    <div class="container">
      <div class="register-form max-w-md mx-auto bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-2xl font-semibold text-center mb-6 text-gray-800">注册</h2>
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="验证码" prop="code">
            <el-row :gutter="10">
              <el-col :span="16">
                <el-input v-model="registerForm.code" placeholder="请输入验证码" 
                style="margin-right: -40px;"/>
              </el-col>
              <el-col :span="8">
                <el-button 
                  type="primary" 
                  :disabled="countdown > 0"
                  @click="sendCode"
                  class="bg-blue-500 hover:bg-blue-600 text-white"
                >
                  {{ countdown > 0 ? `${countdown}s后重新发送` : '获取验证码' }}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" 
            style="margin-left: 15px;"/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="w-full mb-2 bg-blue-500 hover:bg-blue-600 text-white" @click="handleRegister">注册</el-button>
            <el-button type="default" class="w-full" @click="goToLogin"
            style="margin-left: 0px;">去登录</el-button>
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
const registerFormRef = ref()
const registerForm = reactive({
  phone: '',
  code: '',
  username: '',
  password: ''
})
const countdown = ref(0)
let timer: any = null

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度2-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const sendCode = async () => {
  if (!registerForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  
  try {
    const res = await userApi.getCode(registerForm.phone)
    if (res.success) {
      ElMessage.success('验证码发送成功')
      startCountdown()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('发送验证码失败')
  }
}

const startCountdown = () => {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleRegister = async () => {
  if (registerFormRef.value) {
    await registerFormRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          const res = await userApi.register(registerForm)
          if (res.success) {
            ElMessage.success('注册成功')
            router.push('/login')
          } else {
            ElMessage.error(res.message)
          }
        } catch (error) {
          ElMessage.error('注册失败，请重试')
        }
      }
    })
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register {
  padding: 60px 0;
}

.register-form {
  max-width: 400px;
  margin: 0 auto;
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.register-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.register-btn {
  width: 100%;
  margin-bottom: 10px;
}

.login-btn {
  width: 100%;
}
</style>