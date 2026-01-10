<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="logo">智慧校园能耗监测平台</div>
      <nav>
        <router-link to="/" :class="{ active: $route.path === '/' }">
          <HomeFilled /> 数据大屏
        </router-link>
        <router-link to="/building" :class="{ active: $route.path === '/building' }" v-if="userRole === 'ADMIN'">
          <OfficeBuilding /> 建筑管理
        </router-link>
        <router-link to="/device" :class="{ active: $route.path === '/device' }" v-if="userRole === 'ADMIN'">
          <Coin /> 设备管理
        </router-link>
        <router-link to="/energy" :class="{ active: $route.path === '/energy' }">
          <Document /> 能耗数据
        </router-link>
        <router-link to="/alarm" :class="{ active: $route.path === '/alarm' }">
          <Warning /> 告警记录
        </router-link>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <main class="content">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="user-info">
          <span>{{ username }}</span>
          <button @click="handleLogout">
            <Close /> 退出登录
          </button>
        </div>
      </header>

      <!-- 路由视图（渲染子页面） -->
      <router-view />
    </main>
  </div>
</template>

<script setup>
import {HomeFilled, OfficeBuilding,Coin, Document, Warning, Close, } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/index'
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'

const userStore = useUserStore()
const router = useRouter()

// 响应式数据
const username = ref('')
const userRole = ref('')

// 初始化用户信息
onMounted(() => {
  // 从 localStorage 获取用户信息
  username.value = localStorage.getItem('username') || '用户'
  userRole.value = localStorage.getItem('userRole') || 'NORMAL'
})

// 退出登录处理
const handleLogout = () => {
  // 清除 store 中的数据
  userStore.clearToken()
  // 清除本地存储
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userRole')
  // 跳转到登录页
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 200px;
  background-color: #2c3e50;
  color: white;
  padding: 1rem 0;
}

.logo {
  text-align: center;
  padding: 1rem;
  font-size: 1.2rem;
  font-weight: bold;
  border-bottom: 1px solid #34495e;
}

nav {
  padding-top: 1rem;
}

nav a {
  display: flex;
  align-items: center;
  padding: 0.8rem 1.5rem;
  color: #ecf0f1;
  text-decoration: none;
  gap: 0.5rem;
}

nav a.active {
  background-color: #3498db;
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: flex-end;
  padding: 1rem 2rem;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info button {
  background: none;
  border: none;
  color: #e74c3c;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.3rem;
}
</style>