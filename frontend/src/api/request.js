import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
    baseURL: '/energy', // 使用相对路径，通过 Vite 代理访问后端
    timeout: 5000 // 请求超时时间
})

// 请求拦截器：添加JWT Token
service.interceptors.request.use(
    config => {
        // 动态获取 token，避免在模块加载时使用 store
        const token = localStorage.getItem('token')
        console.log('请求拦截器: token =', token ? `${token.substring(0, 10)}...` : '无token')

        if (token) {
            // Token添加到请求头
            config.headers['Authorization'] = `Bearer ${token}`
            console.log('请求拦截器: Authorization 头已设置')
        } else {
            console.log('请求拦截器: 未找到token')
        }
        
        return config
    },
    error => {
        console.error('请求拦截器错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器：统一处理响应结果
service.interceptors.response.use(
    response => {
        const res = response.data
        // 响应成功（code=200）
        if (res.code === 200) {
            return res
        } else {
            // 响应失败（非200状态码）
            // 不在这里处理错误，交给调用者处理
            return response // 返回原始响应，让组件自己处理错误
        }
    },
    error => {
        // 网络错误或后端500错误
        const status = error.response?.status

        // 处理401错误
        if (status === 401) {
            // 只显示错误消息，不自动跳转
            ElMessage.error('认证失败，请重新登录')
            // 不清除token和自动跳转，交由路由守卫处理
            return Promise.reject(error)
        } else if (status === 403) {
            ElMessage.error('权限不足')
        } else {
            // 网络错误或服务器错误
            ElMessage.error('系统错误，请联系管理员')
        }
        return Promise.reject(error)
    }
)

export default service