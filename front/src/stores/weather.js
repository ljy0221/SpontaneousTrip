import { ref } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'

// AI 서버용 axios 인스턴스 (포트 9000)
const aiHttp = axios.create({
  baseURL: import.meta.env.VITE_AI_API_URL || 'http://3.37.88.46:9000',
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
  },
  withCredentials: true,
})

export const useWeatherStore = defineStore('weather', () => {
  const weather = ref(null)
  const loading = ref(false)

  const getWeather = async (lat, lon) => {
    loading.value = true
    try {
      // AI 서버의 /api/weather 엔드포인트 호출
      const response = await aiHttp.post('/api/weather', { lat, lon })
      weather.value = response.data
    } catch (error) {
      console.error('Error fetching weather from AI server:', error)
      weather.value = null
    } finally {
      loading.value = false
    }
  }

  return { weather, getWeather, loading }
})
