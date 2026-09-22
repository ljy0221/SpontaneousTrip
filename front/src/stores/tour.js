import { ref } from 'vue'
import { defineStore } from 'pinia'
import http from '@/utils/http-common'

export const useTourStore = defineStore('tour', () => {
  const places = ref([])

  const getPlaces = async (mapX, mapY, radius) => {
    try {
      console.log('Fetching tour places:', { mapX, mapY, radius })
      const response = await http.get('', {
        params: {
          mapX: String(mapX),
          mapY: String(mapY),
          radius: String(radius)
        }
      })
      console.log('Tour places response:', response.data)

      // Backend returns: { message, data, count }
      places.value = response.data.data || []
      return response.data
    } catch (error) {
      console.error('Error fetching tour places:', error)
      console.error('Error response:', error.response?.data)
      places.value = []
      throw error
    }
  }

  return { places, getPlaces }
})
