<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">智慧校园能耗监测与管理平台</h2>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-tip">
        <span>管理员账号：admin / admin123</span>
        <span>普通用户账号：user / 123456</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'
import { ElMessage } from 'element-plus'
import axios from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)

// 登录表单数据
const loginForm = ref({
  username: '',
  password: ''
})

// 表单校验规则
const loginRules = ref({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

// 登录按钮点击事件
const handleLogin = () => {
  loginFormRef.value.validate((isValid) => {
    if (isValid) {
      // 调用登录API
      axios.post('/auth/login', loginForm.value)
          .then(response => {
            console.log('登录响应:', response)

            // response 已经是经过拦截器处理后的数据
            if (response.code === 200) {
              // 保存token到store和localStorage
              const token = response.data

              // 打印token信息用于调试
              console.log('原始token:', token)
              console.log('token类型:', typeof token)
              console.log('token长度:', token ? token.length : 'undefined')

              // 存储token
              userStore.setToken(token)

              // 保存用户信息到localStorage（从用户名获取）
              localStorage.setItem('username', loginForm.value.username)
              // 根据用户名设置默认角色
              const role = loginForm.value.username === 'admin' ? 'ADMIN' : 'NORMAL'
              localStorage.setItem('userRole', role)

              ElMessage.success('登录成功')
              router.push('/') // 跳转到首页
            } else {
              ElMessage.error(response.msg || '登录失败')
            }
          })
          .catch(error => {
            console.error('登录错误:', error)
            ElMessage.error('登录失败：' + (error.response?.data?.msg || '网络错误'))
          })
    }
  })
}
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 400px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 20px;
  color: #1989fa;
}

.login-btn {
  width: 100%;
}

.login-tip {
  margin-top: 15px;
  font-size: 12px;
  color: #666;
  display: flex;
  justify-content: space-between;
}
</style>