import { ref } from 'vue'
import { defineStore } from 'pinia'
import http from '@/utils/http-common'
import { statusToFrontend } from '@/utils/statusMapper'

export const usePlanStore = defineStore('plan', () => {
  //const plans = ref([])
  const planItems = ref([])

  // Plan 객체의 status를 한글로 변환
  const convertPlanStatus = (plan) => {
    if (plan && plan.status) {
      plan.status = statusToFrontend(plan.status)
    }
    return plan
  }

  const getPlanItems = async () => {
    try {
      const response = await http.get('/planitem')
      console.log('Get plan items response:', response.data)
      planItems.value = response.data.items || []
      return response.data
    } catch (error) {
      console.error('Error fetching plan items:', error)
      console.error('Error response:', error.response?.data)
      throw error
    }
  }

  const createPlanItem = async (item) => {
    try {
      console.log('Creating plan item:', item)
      const response = await http.post('/planitem', item)
      console.log('Create plan item response:', response.data)

      // Refresh list after successful creation
      await getPlanItems()
      return response.data
    } catch (error) {
      console.error('Error creating plan item:', error)
      console.error('Error response:', error.response?.data)
      console.error('Request data:', item)
      throw error
    }
  }

  const deletePlanItem = async (id) => {
    try {
      console.log('Deleting plan item:', id)
      const response = await http.delete(`/planitem/${id}`)
      console.log('Delete plan item response:', response.data)

      // Refresh list after successful deletion
      await getPlanItems()
      return response.data
    } catch (error) {
      console.error('Error deleting plan item:', error)
      console.error('Error response:', error.response?.data)
      throw error
    }
  }

  const savePlan = async (plan) => {
    try {
      console.log('Saving plan:', plan)
      const response = await http.post('/quickplan', plan)
      console.log('Save plan response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error saving plan:', error)
      console.error('Error response:', error.response?.data)
      throw error
    }
  }

  const deletePlan = async (planId) => {
    try {
      console.log('Deleting plan:', planId)
      const response = await http.delete(`/quickplan/${planId}`)
      console.log('Delete plan response:', response.data)
      return response.data
    } catch (error) {
      console.error('Error deleting plan:', error)
      console.error('Error response:', error.response?.data)
      throw error
    }
  }

  const getUserPlans = async (userId) => {
    try {
      console.log('Fetching user plans for:', userId)
      const response = await http.get(`/quickplan/user/${userId}/with-items`)
      console.log('Get user plans response:', response.data)

      // 모든 plan의 status를 한글로 변환
      if (response.data.plans) {
        response.data.plans = response.data.plans.map(convertPlanStatus)
      }

      return response.data
    } catch (error) {
      console.error('Error fetching user plans:', error)
      console.error('Error response:', error.response?.data)
      throw error
    }
  }

  const getPlanById = async (planId) => {
    try {
      console.log('Fetching plan by ID:', planId)
      const response = await http.get(`/quickplan/${planId}/with-items`)
      console.log('Get plan by ID response:', response.data)

      // plan의 status를 한글로 변환
      if (response.data.plan) {
        response.data.plan = convertPlanStatus(response.data.plan)
      }

      return response.data
    } catch (error) {
      console.error('Error fetching plan by ID:', error)
      console.error('Error response:', error.response?.data)
      throw error
    }
  }

  const updatePlanItemsOrder = async (planId, items) => {
    try {
      console.log('Updating plan items order:', { planId, items })
      const response = await http.patch(`/quickplan/${planId}/items/order`, {
        planItems: items.map(item => ({
          planItemId: item.planItemId,
          sequenceOrder: item.sequenceOrder
        }))
      })
      console.log('Update plan items order response:', response.data)

      // Refresh plan data after update
      await getPlanById(planId)

      return response.data
    } catch (error) {
      console.error('Error updating plan items order:', error)
      console.error('Error response:', error.response?.data)
      throw error
    }
  }

  const updatePlanStatus = async (planId, newStatus) => {
    try {
      console.log('진행상태 변경 시작');
      const response = await http.patch(`/quickplan/${planId}/status`, {
        status: newStatus
      })
      console.log('진행상태 변경 완료\n', response.data);
      return response.data;
    } catch (error) {
      console.error('진행상태 변경 실패: ', error);
      throw error
    }
  }

  return {
    planItems,
    getPlanItems,
    createPlanItem,
    deletePlanItem,
    savePlan,
    deletePlan,
    getUserPlans,
    getPlanById,
    updatePlanItemsOrder,
    updatePlanStatus
  }
})
