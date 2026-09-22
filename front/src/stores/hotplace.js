import { ref } from 'vue'
import { defineStore } from 'pinia'
import http from '@/utils/http-common'
import axios from 'axios'

// AI 서버용 axios 인스턴스 (포트 9000)
const aiHttp = axios.create({
  baseURL: import.meta.env.VITE_AI_API_URL || 'http://3.37.88.46:9000',
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
  },
  withCredentials: true,
})

export const useHotplaceStore = defineStore('hotplace', () => {
  const hotplaces = ref([])
  const currentHotplace = ref(null)
  const reviews = ref([])
  const pageInfo = ref(null)

  const getHotplaces = async (category = null) => {
    try {
      const params = {}
      if (category) {
        params.category = category
      }

      const response = await http.get('/hotplace/', { params })
      hotplaces.value = response.data
    } catch (error) {
      console.error('Error fetching hotplaces:', error)
      throw error
    }
  }

  const getHotplace = async (id) => {
    try {
      const response = await http.get(`/hotplace/${id}`)
      currentHotplace.value = response.data
    } catch (error) {
      console.error('Error fetching hotplace:', error)
      throw error
    }
  }

  const getReviews = async (placeId) => {
    try {
      const response = await http.get(`/hotplace/${placeId}/review`)
      reviews.value = response.data
    } catch (error) {
      console.error('Error fetching reviews:', error)
      throw error
    }
  }

  const createReview = async (placeId, review) => {
    try {
      await http.post(`/hotplace/${placeId}/review`, review)
      await getReviews(placeId)
    } catch (error) {
      console.error('Error creating review:', error)
      throw error
    }
  }

  // 🔹 AI 챗 요청 (스트리밍) - Nginx 프록시를 통해 호출
  const chat = async (chatRequest) => {
    try {
      // 상대 경로로 호출 → Nginx가 AI 서버(9000)로 프록시
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json;charset=utf-8',
        },
        body: JSON.stringify(chatRequest),
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      // 스트림 리더 반환
      return response.body.getReader()
    } catch (error) {
      console.error('Error sending chat message:', error)
      throw error
    }
  }

  // 🔹 요약 요청 (스트리밍) - Nginx 프록시를 통해 호출
  const getSummary = async (summaryRequest) => {
    try {
      console.log('📤 Sending summary request:', summaryRequest)

      // 상대 경로로 호출 → Nginx가 AI 서버(9000)로 프록시
      const response = await fetch('/api/summary', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json;charset=utf-8',
        },
        body: JSON.stringify(summaryRequest),
      })

      console.log('📥 Response received:', {
        status: response.status,
        statusText: response.statusText,
        headers: Object.fromEntries(response.headers.entries()),
        bodyUsed: response.bodyUsed,
        body: response.body
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      if (!response.body) {
        throw new Error('Response body is null!')
      }

      // 스트림 리더 반환
      const reader = response.body.getReader()
      console.log('✅ Reader created successfully')
      return reader
    } catch (error) {
      console.error('❌ Error getting summary:', error)
      throw error
    }
  }

  // 🔹 주변 핫플 검색
  const getNearbyHotplaces = async (
    mapX,
    mapY,
    timeInMinutes,
    transportMode,
    page = 0,
    size = 12,
    category = null
  ) => {
    try {
      console.log('Fetching nearby hotplaces:', {
        mapX,
        mapY,
        timeInMinutes,
        transportMode,
        page,
        size,
        category
      })

      const params = { mapX, mapY, timeInMinutes, transportMode, page, size }
      if (category) {
        params.category = category
        console.log('✅ Category added to params:', category)
      } else {
        console.log('⚠️ No category (category is falsy):', category)
      }

      console.log('📤 Final params being sent:', params)

      const response = await http.get('/hotplace/search', { params })

      console.log('Nearby hotplaces response:', response.data)
      hotplaces.value = response.data.data ?? []
      pageInfo.value = response.data.pageInfo ?? null

      return response.data
    } catch (error) {
      console.error('Error fetching nearby hotplaces:', error)
      console.error('Error response:', error.response?.data)
      hotplaces.value = []
      pageInfo.value = null
      throw error
    }
  }

  // 🔹 인기 핫플 조회 (count 기준)
  const getPopularHotplaces = async (lat, lon, radiusMeters = 5000, limit = 10) => {
    try {
      console.log('Fetching popular hotplaces:', { lat, lon, radiusMeters, limit })

      const response = await http.get('/hotplace/popular', {
        params: { lat, lon, radiusMeters, limit },
      })

      console.log('Popular hotplaces response:', response.data)
      return response.data.data ?? []
    } catch (error) {
      console.error('Error fetching popular hotplaces:', error)
      console.error('Error response:', error.response?.data)
      return []
    }
  }

  return {
    hotplaces,
    currentHotplace,
    reviews,
    pageInfo,
    getHotplaces,
    getHotplace,
    getReviews,
    createReview,
    chat,
    getSummary,
    getNearbyHotplaces,
    getPopularHotplaces,
  }
})
