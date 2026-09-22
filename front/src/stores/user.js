import { ref } from 'vue'
import { defineStore } from 'pinia'
import http from '@/utils/http-common'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
    const userInfo = ref(null)
    const isLoading = ref(false)

    // GET /user/me - 내 정보 조회
    const getUserInfo = async () => {
        isLoading.value = true
        try {
            const response = await http.get('/user/me')
            userInfo.value = response.data
            return response.data
        } catch (error) {
            console.error('Failed to fetch user info:', error)
            throw error
        } finally {
            isLoading.value = false
        }
    }

    // POST /user/me - 회원가입
    const register = async (email, password, nickname) => {
        isLoading.value = true
        try {
            const response = await http.post('/user/me', {
                email,
                password,
                nickname
            })
            return response.data
        } catch (error) {
            console.error('Registration failed:', error)
            throw error
        } finally {
            isLoading.value = false
        }
    }

    // PATCH /user/me - 내 정보 수정
    const updateUserInfo = async (data) => {
        isLoading.value = true
        try {
            // data can contain: { nickname?, currentPassword?, newPassword? }
            const response = await http.patch('/user/me', data)
            // Refresh user info after update
            await getUserInfo()
            return response.data
        } catch (error) {
            console.error('Failed to update user info:', error)
            throw error
        } finally {
            isLoading.value = false
        }
    }

    // DELETE /user/me - 회원탈퇴
    const deleteAccount = async (password) => {
        isLoading.value = true
        try {
            await http.delete('/user/me', {
                data: { password }
            })
            // Clear user info and redirect
            userInfo.value = null
            router.push('/')
        } catch (error) {
            console.error('Failed to delete account:', error)
            throw error
        } finally {
            isLoading.value = false
        }
    }

    // Clear user info (for logout)
    const clearUserInfo = () => {
        userInfo.value = null
    }

    return {
        userInfo,
        isLoading,
        getUserInfo,
        register,
        updateUserInfo,
        deleteAccount,
        clearUserInfo
    }
})
