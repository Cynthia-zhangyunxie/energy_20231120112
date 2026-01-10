import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref('')
    const userInfo = ref({})

    const setToken = (newToken) => {
        token.value = newToken
        localStorage.setItem('token', newToken)
    }

    const clearToken = () => {
        token.value = ''
        localStorage.removeItem('token')
    }

    return {
        token,
        userInfo,
        setToken,
        clearToken
    }
})