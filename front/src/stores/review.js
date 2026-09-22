import { ref } from 'vue'
import { defineStore } from 'pinia'
import http from '@/utils/http-common'

export const useReviewStore = defineStore('review', () => {
    const reviews = ref([])

    // 특정 장소의 모든 리뷰 조회
    const getReviewsByPlaceId = async (placeId) => {
        try {
            const response = await http.get(`/hotplace/${placeId}/review`)
            reviews.value = response.data.reviewsByPlaceId || []
            return reviews.value
        } catch (error) {
            console.error('Error fetching reviews:', error)
            reviews.value = []
            throw error
        }
    }

    // 리뷰 생성
    const createReview = async (placeId, reviewData) => {
        try {
            const response = await http.post(`/hotplace/${placeId}/review`, reviewData)
            // 생성 후 리뷰 목록 다시 조회
            await getReviewsByPlaceId(placeId)
            return response.data
        } catch (error) {
            console.error('Error creating review:', error)
            throw error
        }
    }

    // 리뷰 수정
    const updateReview = async (placeId, reviewId, reviewData) => {
        try {
            const response = await http.put(`/hotplace/${placeId}/review/${reviewId}`, reviewData)
            // 수정 후 리뷰 목록 다시 조회
            await getReviewsByPlaceId(placeId)
            return response.data
        } catch (error) {
            console.error('Error updating review:', error)
            throw error
        }
    }

    // 리뷰 삭제
    const deleteReview = async (placeId, reviewId, userId) => {
        try {
            const response = await http.delete(`/hotplace/${placeId}/review/${reviewId}`, {
                data: { userId }
            })
            // 삭제 후 리뷰 목록 다시 조회
            await getReviewsByPlaceId(placeId)
            return response.data
        } catch (error) {
            console.error('Error deleting review:', error)
            throw error
        }
    }

    return {
        reviews,
        getReviewsByPlaceId,
        createReview,
        updateReview,
        deleteReview,
    }
})
