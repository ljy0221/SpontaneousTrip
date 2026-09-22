import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import http from '@/utils/http-common'
import router from '@/router'

// Helper function to decode JWT token
function decodeJWT(token) {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('Failed to decode JWT:', error)
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const userId = ref(null)
  const nickname = ref(null)
  const isAuthenticated = computed(() => !!userId.value)

  // Initialize from storage
  const storedUser = localStorage.getItem('userId') || sessionStorage.getItem('userId')
  const storedNickname = localStorage.getItem('nickname') || sessionStorage.getItem('nickname')

  if (storedUser) {
    userId.value = storedUser
  }
  if (storedNickname) {
    nickname.value = storedNickname
  }

  async function login(email, password, remember = false) {
    try {
      const response = await http.post('/auth/login', { email, password })
      const { accessToken } = response.data

      // Decode JWT to extract nickname
      const payload = decodeJWT(accessToken)
      const userNickname = payload?.nickname || null

      userId.value = email
      nickname.value = userNickname

      if (remember) {
        localStorage.setItem('userId', email)
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('nickname', userNickname)
      } else {
        sessionStorage.setItem('userId', email)
        sessionStorage.setItem('accessToken', accessToken)
        sessionStorage.setItem('nickname', userNickname)
      }
      return true
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  async function logout() {
    try {
      await http.post('/auth/logout')
    } catch (error) {
      console.error('Logout failed:', error)
    } finally {
      userId.value = null
      nickname.value = null
      localStorage.removeItem('userId')
      localStorage.removeItem('accessToken')
      localStorage.removeItem('nickname')
      sessionStorage.removeItem('userId')
      sessionStorage.removeItem('accessToken')
      sessionStorage.removeItem('nickname')
      router.push('/')
    }
  }

  async function refreshAccessToken() {
    try {
      const response = await http.post('/auth/refresh')
      const { accessToken } = response.data

      // Update token in storage (check which storage was used)
      if (localStorage.getItem('userId')) {
        localStorage.setItem('accessToken', accessToken)
      } else {
        sessionStorage.setItem('accessToken', accessToken)
      }

      return accessToken
    } catch (error) {
      console.error('Token refresh failed:', error)
      // If refresh fails, logout user
      await logout()
      throw error
    }
  }

  return { userId, nickname, isAuthenticated, login, logout, refreshAccessToken }
})
