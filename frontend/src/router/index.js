import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Login from '@/views/Login.vue'
import MainLayout from '@/layouts/MainLayout.vue'

// 导入页面组件
import Dashboard from '@/views/Dashboard.vue'
import BuildingManage from '@/views/BuildingManage.vue'
import DeviceManage from '@/views/DeviceManage.vue'
import EnergyData from '@/views/EnergyData.vue'
import AlarmRecord from '@/views/AlarmRecord.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: { requiresAuth: false } // 无需登录
    },
    {
        path: '/',
        component: MainLayout,
        meta: { requiresAuth: true }, // 需要登录
        children: [
            {
                path: '',
                name: 'Dashboard',
                component: Dashboard,
                meta: { title: '数据大屏' }
            },
            {
                path: '/building',
                name: 'BuildingManage',
                component: BuildingManage,
                meta: { title: '建筑管理', roles: ['ADMIN'] } // 仅管理员可访问
            },
            {
                path: '/device',
                name: 'DeviceManage',
                component: DeviceManage,
                meta: { title: '设备管理', roles: ['ADMIN'] } // 仅管理员可访问
            },
            {
                path: '/energy',
                name: 'EnergyData',
                component: EnergyData,
                meta: { title: '能耗数据', roles: ['ADMIN', 'NORMAL'] } // 所有登录用户可访问
            },
            {
                path: '/alarm',
                name: 'AlarmRecord',
                component: AlarmRecord,
                meta: { title: '告警记录', roles: ['ADMIN', 'NORMAL'] } // 所有登录用户可访问
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

// 路由守卫：校验登录状态和角色权限
router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = to.meta.title || '智慧校园能耗监测平台'

    // 无需登录的页面直接放行
    if (!to.meta.requiresAuth) {
        next()
        return
    }

    // 检查是否登录（Token是否存在）
    const token = localStorage.getItem('token')
    if (!token) {
        next('/login') // 未登录，跳转到登录页
        return
    }

    // TODO: 检查角色权限
    // 如果需要角色权限验证，可以从 token 中解析或从 localStorage 获取
    // const userRole = localStorage.getItem('userRole')

    next() // 权限通过，放行
})

export default router