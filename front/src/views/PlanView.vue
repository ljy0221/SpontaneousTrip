<script setup>
import { ref, onMounted } from 'vue'
import { usePlanStore } from '@/stores/plan'
import { useHotplaceStore } from '@/stores/hotplace'
import { useUserStore } from '@/stores/user'
import { useReviewStore } from '@/stores/review'
import { useRouter } from 'vue-router'
import Chatbot from '@/components/common/Chatbot.vue'
import PaginationControls from '@/components/common/PaginationControls.vue'

const planStore = usePlanStore()
const hotplaceStore = useHotplaceStore()
const userStore = useUserStore()
const reviewStore = useReviewStore()
const router = useRouter()

// Local Plan State
const localPlanItems = ref([])

// Search State
const timeInMinutes = ref(10)
const transportMode = ref('WALK')
const selectedCategory = ref('ALL')
const loading = ref(false)
const searchError = ref(null)
const locationError = ref(null)
const currentLocation = ref({
  lat: 36.355306,
  lon: 127.298111,
})

// 카테고리 목록 (하드코딩)
const categories = [
  { value: 'ALL', label: '전체', icon: '🌍' },
  { value: '관광지', label: '관광지', icon: '🏛️' },
  { value: '문화시설', label: '문화시설', icon: '🎭' },
  { value: '축제공연행사', label: '축제/공연', icon: '🎪' },
  { value: '여행코스', label: '여행코스', icon: '🗺️' },
  { value: '레포츠', label: '레포츠', icon: '⛷️' },
  { value: '숙박', label: '숙박', icon: '🏨' },
  { value: '쇼핑', label: '쇼핑', icon: '🛍️' }
]

// Pagination State
const currentPage = ref(0)
const pageSize = ref(12)

// UI State
const selectedPlace = ref(null)
const showDetailModal = ref(false)
const aiDescription = ref(null)
const loadingDescription = ref(false)

// Review State
const reviews = ref([])
const loadingReviews = ref(false)
const newReview = ref({
  rating: 5,
  content: '',
  timeSuitablityScore: 3
})
const editingReviewId = ref(null)
const editReview = ref({
  rating: 5,
  content: '',
  timeSuitablityScore: 3
})

onMounted(() => {
  getCurrentLocation()
})

const getCurrentLocation = async () => {
  if (!navigator.geolocation) {
    locationError.value = 'Geolocation is not supported by your browser'
    return
  }

  try {
    const position = await new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      })
    })

    currentLocation.value = {
      lat: position.coords.latitude,
      lon: position.coords.longitude,
    }
    locationError.value = null
  } catch (error) {
    console.error('위치를 찾을 수 없습니다. :', error)
    locationError.value = '위치를 찾을 수 없습니다.'
  }
}

const searchPlaces = async () => {
  loading.value = true
  searchError.value = null
  currentPage.value = 0 // Reset to first page on new search

  try {
    const category = selectedCategory.value !== 'ALL' ? selectedCategory.value : undefined
    await hotplaceStore.getNearbyHotplaces(
      currentLocation.value.lon, // mapX
      currentLocation.value.lat, // mapY
      timeInMinutes.value,
      transportMode.value,
      currentPage.value,
      pageSize.value,
      category
    )
    
    // 검색 성공했지만 결과가 없는 경우
    if (hotplaceStore.hotplaces.length === 0) {
      const categoryText = selectedCategory.value !== 'ALL' ? `'${selectedCategory.value}' 카테고리의 ` : ''
      searchError.value = `주변에서 ${categoryText}장소를 찾을 수 없습니다. 검색 조건을 변경해보세요.`
    }
  } catch (error) {
    console.error('Error searching hotplaces:', error)
    
    // 404 에러인 경우
    if (error.response?.status === 404) {
      const categoryText = selectedCategory.value !== 'ALL' ? `'${selectedCategory.value}' 카테고리의 ` : ''
      searchError.value = `주변에서 ${categoryText}장소를 찾을 수 없습니다.`
    } else {
      searchError.value = '장소 검색에 실패했습니다. 잠시 후 다시 시도해주세요.'
    }
  } finally {
    loading.value = false
  }
}

// 카테고리 변경 시 자동으로 검색 실행
const handleCategoryChange = async (category) => {
  selectedCategory.value = category
  // 이미 검색한 적이 있으면 자동으로 재검색
  if (hotplaceStore.hotplaces.length > 0 || hotplaceStore.pageInfo) {
    await searchPlaces()
  }
}

// Pagination Functions
const goToPage = async (page) => {
  if (page < 0 || !hotplaceStore.pageInfo) return
  if (page >= hotplaceStore.pageInfo.totalPages) return

  currentPage.value = page
  loading.value = true

  try {
    const category = selectedCategory.value !== 'ALL' ? selectedCategory.value : undefined
    await hotplaceStore.getNearbyHotplaces(
      currentLocation.value.lon,
      currentLocation.value.lat,
      timeInMinutes.value,
      transportMode.value,
      currentPage.value,
      pageSize.value,
      category
    )
  } catch (error) {
    console.error('Error loading page:', error)
    alert('페이지를 불러오는데 실패했습니다. 다시 시도해주세요.')
  } finally {
    loading.value = false
  }
}

const goToNextPage = () => {
  if (hotplaceStore.pageInfo?.hasNext) {
    goToPage(currentPage.value + 1)
  }
}

const goToPrevPage = () => {
  if (hotplaceStore.pageInfo?.hasPrevious) {
    goToPage(currentPage.value - 1)
  }
}

// ✅ Detail 열 때 AI description까지 처리
const openDetail = async (place) => {
  selectedPlace.value = place
  showDetailModal.value = true
  aiDescription.value = null
  reviews.value = []
  editingReviewId.value = null

  // 🔐 사용자 정보 로드 (리뷰 권한 확인용)
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo()
      console.log('✅ User info loaded:', userStore.userInfo)
    } catch (error) {
      console.log('⚠️ Failed to load user info (not logged in?)', error)
    }
  }

  // 🤖 자동 AI 설명 생성
  await generateAiDescription()
  
  // 📝 리뷰 로드
  await loadReviews()
}

const closeDetail = () => {
  showDetailModal.value = false
  selectedPlace.value = null
  aiDescription.value = null
}

// ✅ AI description 생성 (스트리밍)
const generateAiDescription = async () => {
  if (!selectedPlace.value) return

  const placeId =
    selectedPlace.value.contentid || selectedPlace.value.contentId || selectedPlace.value.placeId

  if (!placeId) {
    console.warn('No valid place ID found')
    aiDescription.value = selectedPlace.value.overview || 'No description available'
    return
  }

  loadingDescription.value = true
  aiDescription.value = '' // 빈 문자열로 초기화

  try {
    console.log('🤖 Generating AI description for place:', placeId)

    const reader = await hotplaceStore.getSummary({
      place_id: placeId,
      max_length: 300,
    })

    // Read stream
    const decoder = new TextDecoder('utf-8')
    let chunkCount = 0

    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        console.log(`📦 Stream finished. Total chunks: ${chunkCount}`)
        break
      }

      const chunk = decoder.decode(value, { stream: true })
      chunkCount++
      aiDescription.value += chunk

      // 강제로 DOM 업데이트 (타이핑 효과)
      await new Promise(resolve => setTimeout(resolve, 10))
    }

    console.log('✅ AI description generated:', aiDescription.value)
    console.log('📏 Description length:', aiDescription.value.length)
  } catch (error) {
    console.warn('⚠️ Failed to generate AI description, using fallback:', error)
    aiDescription.value = selectedPlace.value.overview || 'No description available'
  } finally {
    loadingDescription.value = false
  }
}

// ✅ 재생성 버튼용
const regenerateDescription = async () => {
  await generateAiDescription()
}

// ✅ 리뷰 로드
const loadReviews = async () => {
  if (!selectedPlace.value) return

  const placeId =
    selectedPlace.value.contentid || selectedPlace.value.contentId || selectedPlace.value.placeId

  if (!placeId) {
    console.warn('No valid place ID found for reviews')
    return
  }

  loadingReviews.value = true
  try {
    await reviewStore.getReviewsByPlaceId(placeId)
    reviews.value = reviewStore.reviews
  } catch (error) {
    console.error('Failed to load reviews:', error)
    reviews.value = []
  } finally {
    loadingReviews.value = false
  }
}

// ✅ 리뷰 생성
const submitReview = async () => {
  if (!selectedPlace.value) return
  if (!newReview.value.content.trim()) {
    alert('리뷰 내용을 입력해주세요.')
    return
  }

  const placeId =
    selectedPlace.value.contentid || selectedPlace.value.contentId || selectedPlace.value.placeId

  try {
    // Ensure user is logged in
    if (!userStore.userInfo) {
      await userStore.getUserInfo()
    }

    await reviewStore.createReview(placeId, {
      userId: userStore.userInfo?.userId || userStore.userInfo?.id,
      rating: newReview.value.rating,
      content: newReview.value.content,
      timeSuitablityScore: newReview.value.timeSuitablityScore
    })

    // Reset form
    newReview.value = {
      rating: 5,
      content: '',
      timeSuitablityScore: 3
    }

    // Reload reviews
    reviews.value = reviewStore.reviews
    alert('리뷰가 등록되었습니다!')
  } catch (error) {
    console.error('Failed to submit review:', error)
    alert('리뷰 등록에 실패했습니다.')
  }
}

// ✅ 리뷰 수정 모드 시작
const startEditReview = (review) => {
  editingReviewId.value = review.reviewId
  editReview.value = {
    rating: review.rating,
    content: review.content,
    timeSuitablityScore: review.timeSuitablityScore
  }
}

// ✅ 리뷰 수정 취소
const cancelEditReview = () => {
  editingReviewId.value = null
  editReview.value = {
    rating: 5,
    content: '',
    timeSuitablityScore: 3
  }
}

// ✅ 리뷰 수정 저장
const saveEditReview = async (reviewId) => {
  if (!selectedPlace.value) return
  if (!editReview.value.content.trim()) {
    alert('리뷰 내용을 입력해주세요.')
    return
  }

  const placeId =
    selectedPlace.value.contentid || selectedPlace.value.contentId || selectedPlace.value.placeId

  try {
    await reviewStore.updateReview(placeId, reviewId, {
      rating: editReview.value.rating,
      content: editReview.value.content,
      timeSuitablityScore: editReview.value.timeSuitablityScore
    })

    editingReviewId.value = null
    reviews.value = reviewStore.reviews
    alert('리뷰가 수정되었습니다!')
  } catch (error) {
    console.error('Failed to update review:', error)
    alert('리뷰 수정에 실패했습니다.')
  }
}

// ✅ 리뷰 삭제
const deleteReviewItem = async (reviewId) => {
  if (!confirm('이 리뷰를 삭제하시겠습니까?')) return
  if (!selectedPlace.value) return

  const placeId =
    selectedPlace.value.contentid || selectedPlace.value.contentId || selectedPlace.value.placeId

  try {
    // 사용자 정보 확인
    if (!userStore.userInfo) {
      await userStore.getUserInfo()
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    await reviewStore.deleteReview(placeId, reviewId, userId)
    reviews.value = reviewStore.reviews
    alert('리뷰가 삭제되었습니다!')
  } catch (error) {
    console.error('Failed to delete review:', error)
    alert('리뷰 삭제에 실패했습니다.')
  }
}

// ✅ 내 리뷰인지 확인
const isMyReview = (review) => {
  if (!userStore.userInfo) return false
  const myUserId = userStore.userInfo?.userId || userStore.userInfo?.id
  // 타입 불일치 문제 방지를 위해 문자열로 변환 후 비교
  return String(review.userId) === String(myUserId)
}

// ✅ 사용자 표시 이름 가져오기 (닉네임 우선)
const getUserDisplayName = (review) => {
  // 백엔드에서 닉네임을 포함하여 반환하므로 nickname 사용
  return review.nickname || review.userId
}

// ✅ Add to Plan - AI description도 함께 추가
const addToPlan = async () => {
  if (!selectedPlace.value) {
    alert('선택된 장소가 없습니다')
    return
  }

  const newItem = {
    placeId:
      selectedPlace.value.contentid ||
      selectedPlace.value.contentId ||
      selectedPlace.value.placeId ||
      selectedPlace.value.id,
    sequenceOrder: localPlanItems.value.length + 1,
    transportMode: transportMode.value,
    estimatedDuration: 60,
    title: selectedPlace.value.placeName || selectedPlace.value.title || selectedPlace.value.name,
    overview: selectedPlace.value.overview,
    description: aiDescription.value, // ✅ AI 설명
  }

  console.log('Adding to local plan with AI description:', newItem)
  localPlanItems.value.push(newItem)

  alert(
    `"${
      selectedPlace.value.placeName || selectedPlace.value.title || selectedPlace.value.name
    }" 장소가 일정에 추가되었습니다 (AI 설명 포함)!`,
  )
  closeDetail()
}

const deleteItem = (index) => {
  if (confirm('이 단계를 삭제하시겠습니까?')) {
    localPlanItems.value.splice(index, 1)
    localPlanItems.value.forEach((item, idx) => {
      item.sequenceOrder = idx + 1
    })
  }
}

const savePlan = async () => {
  if (localPlanItems.value.length === 0) {
    alert('일정이 비어있습니다!')
    return
  }

  // Ensure user is logged in
  if (!userStore.userInfo) {
    try {
        await userStore.getUserInfo()
    } catch (e) {
		alert('일정을 저장하려면 로그인해주세요.')
        return
    }
  }

  try {
    const planData = {
      // planId is auto-increment, so we don't send it or send null
      userId: userStore.userInfo?.userId || userStore.userInfo?.id,
      totalTimeInput: timeInMinutes.value,
      startLocation: `${currentLocation.value.lat}, ${currentLocation.value.lon}`, // Simple string representation
      status: 'ONGOING',
      planItems: localPlanItems.value.map((item, index) => ({
        placeId: item.placeId,
        sequenceOrder: index + 1,
        transportMode: item.transportMode || 'BUS',
        estimatedDuration: item.estimatedDuration || 60,
        // Note: title, overview, description are not saved in DB as per schema
      })),
    }

    await planStore.savePlan(planData)
    console.log('Plan saved:', planData)
    // alert('Plan saved successfully!')
    localPlanItems.value = []

    if (confirm('저장이 완료되었습니다. 일정을 확인하시겠습니까?')) {
        router.push({ name: 'mypage' })
    }
  } catch (error) {
    console.error('Failed to save plan:', error)
    alert('일정 저장에 실패했습니다. 다시 시도해주세요.')
  }
}
</script>

<template>
  <div class="container">
    <Chatbot
      :placeId="selectedPlace?.contentid || selectedPlace?.contentId || selectedPlace?.placeId || selectedPlace?.id"
      :candidates="hotplaceStore.hotplaces"
    />
    <h1>여행 계획</h1>

    <!-- Search Section -->
    <section class="search-section card">
      <h3>주변 장소 찾기</h3>


      <div class="location-status">
        <div v-if="locationError" class="location-error">⚠️ {{ locationError }}</div>
        <div v-else class="location-info">
          📍 현재 위치:
          {{ currentLocation.lat.toFixed(4) }},
          {{ currentLocation.lon.toFixed(4) }}
        </div>
        <button @click="getCurrentLocation" class="btn btn-secondary btn-sm">
          🔄 위치 새로고침
        </button>
      </div>

      <div class="search-controls">
        <div class="form-group">
          <label>사용 가능 시간 (분)</label>
          <input v-model.number="timeInMinutes" type="number" step="5" min="1" class="form-input" />
        </div>
        <div class="form-group">
          <label>이동 수단</label>
          <select v-model="transportMode" class="form-input">
            <option value="WALK">🚶 도보</option>
            <option value="TRANSIT">🚌 대중교통</option>
            <option value="CAR">🚗 자차</option>
          </select>
        </div>
        <div class="form-group search-btn-group">
          <label class="invisible-label">검색</label>
          <button @click="searchPlaces" class="btn btn-primary" :disabled="loading">
            {{ loading ? '검색 중...' : '검색' }}
          </button>
        </div>
      </div>

      <!-- 카테고리 필터 -->
      <div class="category-filter-section">
        <label class="filter-label">카테고리</label>
        <div class="category-filters">
          <button 
            v-for="cat in categories" 
            :key="cat.value"
            class="filter-btn" 
            :class="{ active: selectedCategory === cat.value }"
            @click="handleCategoryChange(cat.value)"
          >
            <span class="filter-icon">{{ cat.icon }}</span>
            <span class="filter-text">{{ cat.label }}</span>
          </button>
        </div>
      </div>
    </section>

    <!-- Gallery + Current Plan -->
    <div class="planning-layout">
      <!-- Gallery Section -->
      <section class="gallery-section">
        <h3>후보 목록</h3>

        <div v-if="loading" class="loading-state">⏳ 장소 로딩 중...</div>
        
        <!-- 검색 에러 메시지 -->
        <div v-else-if="searchError" class="error-state">
          <div class="error-icon">🔍</div>
          <p class="error-message">{{ searchError }}</p>
          <p class="error-hint">다른 카테고리를 선택하거나 검색 조건을 변경해보세요.</p>
        </div>
        
        <div v-else-if="hotplaceStore.hotplaces.length === 0" class="empty-state">
          장소를 검색해보세요...
        </div>
        <div v-else class="gallery-grid">
          <div
            v-for="place in hotplaceStore.hotplaces"
            :key="place.placeId"
            class="place-card card"
            @click="openDetail(place)"
          >
            <img
              v-if="place.image"
              :src="place.image"
              :alt="place.placeName"
              class="place-img"
            />
            <div v-else class="place-img-placeholder">이미지 없음</div>

            <h4>{{ place.placeName }}</h4>
            <p class="place-addr">{{ place.address }}</p>
          </div>

        </div>

		<!-- Pagination Controls -->
		<PaginationControls
		  v-if="hotplaceStore.pageInfo"
		  :current-page="currentPage"
		  :total-pages="hotplaceStore.pageInfo.totalPages"
		  :has-next="hotplaceStore.pageInfo.hasNext"
		  :has-previous="hotplaceStore.pageInfo.hasPrevious"
		  :is-loading="loading"
		  @update:page="goToPage"
		  @next="goToNextPage"
		  @previous="goToPrevPage"
		/>
      </section>

      <!-- Current Plan Section -->
      <section class="current-plan-section">
        <h3>현재 일정</h3>
        <div class="plan-list">
          <div v-if="localPlanItems.length === 0" class="empty-state">아직 일정에 항목이 없습니다.</div>
          <div v-for="(item, index) in localPlanItems" :key="index" class="plan-item card">
            <div class="item-info">
              <span class="step-badge">{{ item.sequenceOrder }}</span>
              <div class="item-details">
                <div class="item-title">
                  {{ item.title || `Place #${item.placeId}` }}
                </div>
                <div v-if="item.description" class="item-desc">✨ {{ item.description }}</div>
              </div>
            </div>
            <button @click="deleteItem(index)" class="btn-delete">×</button>
          </div>

          <button
            v-if="localPlanItems.length > 0"
            @click="savePlan"
            class="btn btn-primary btn-block mt-3"
          >
            일정 저장
          </button>
        </div>
      </section>
    </div>

    <!-- Detail Modal -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetail">
      <div class="modal-content card">
        <div class="modal-header">
          <h2>
            {{ selectedPlace.placeName || selectedPlace.title || selectedPlace.name }}
          </h2>
          <button @click="closeDetail" class="btn-close">×</button>
        </div>

        <div class="modal-body">
          <p class="place-address">📍 {{ selectedPlace.address || selectedPlace.addr1 }}</p>

          <img
            v-if="selectedPlace.image || selectedPlace.firstImage || selectedPlace.firstImage2"
            :src="selectedPlace.image || selectedPlace.firstImage || selectedPlace.firstImage2"
            :alt="selectedPlace.placeName || selectedPlace.title"
            class="detail-img"
          />

          <!-- AI Description -->
          <div class="description-section">
            <div class="description-header">
              <h4>✨ AI 설명</h4>
            </div>

            <div v-if="loadingDescription" class="loading-box">
              <div class="spinner"></div>
              <p>AI 설명 생성 중...</p>
            </div>

            <div v-else-if="aiDescription" class="description-box">
              <p>{{ aiDescription }}</p>
            </div>

            <div v-else class="description-box fallback">
              <p>{{ selectedPlace.overview || '설명을 사용할 수 없습니다.' }}</p>
            </div>
          </div>

          <!-- 원본 overview -->
          <details class="original-overview" v-if="selectedPlace.overview">
            <summary>📄 원본 설명</summary>
            <p class="full-desc">{{ selectedPlace.overview }}</p>
          </details>

          <!-- 리뷰 섹션 -->
          <div class="review-section">
            <h3>💬 리뷰</h3>

            <!-- 리뷰 작성 폼 -->
            <div class="review-form">
              <h4>리뷰 작성</h4>
              <div class="form-group">
                <label>평점</label>
                <div class="rating-input">
                  <span 
                    v-for="star in 5" 
                    :key="star"
                    @click="newReview.rating = star"
                    class="star"
                    :class="{ active: star <= newReview.rating }"
                  >
                    {{ star <= newReview.rating ? '⭐' : '☆' }}
                  </span>
                </div>
              </div>
              <div class="form-group">
                <label>내용</label>
                <textarea 
                  v-model="newReview.content" 
                  placeholder="리뷰를 작성해주세요..."
                  rows="3"
                  class="review-textarea"
                ></textarea>
              </div>
              <button @click="submitReview" class="btn btn-primary btn-sm">리뷰 등록</button>
            </div>

            <!-- 리뷰 목록 -->
            <div class="review-list">
              <div v-if="loadingReviews" class="loading-state">리뷰 로딩 중...</div>
              <div v-else-if="reviews.length === 0" class="empty-state">아직 리뷰가 없습니다.</div>
              <div v-else>
                <div 
                  v-for="review in reviews" 
                  :key="review.reviewId" 
                  class="review-item"
                >
                  <!-- 수정 모드가 아닐 때 -->
                  <div v-if="editingReviewId !== review.reviewId">
                    <div class="review-header">
                      <div class="review-user">
                        <span class="user-id">{{ getUserDisplayName(review) }}</span>
                        <span class="review-rating">
                          <span v-for="star in 5" :key="star">
                            {{ star <= review.rating ? '⭐' : '☆' }}
                          </span>
                        </span>
                      </div>
                    </div>
                    <p class="review-content">{{ review.content }}</p>
                    <div class="review-footer">
                      <span class="review-date">{{ new Date(review.createdAt).toLocaleDateString() }}</span>
                      <div class="review-actions" v-if="isMyReview(review)">
                        <button @click="startEditReview(review)" class="text-btn">수정</button>
                        <button @click="deleteReviewItem(review.reviewId)" class="text-btn delete">삭제</button>
                      </div>
                    </div>
                  </div>

                  <!-- 수정 모드일 때 -->
                  <div v-else class="review-edit-mode">
                    <div class="form-group">
                      <label>평점</label>
                      <div class="rating-input">
                        <span 
                          v-for="star in 5" 
                          :key="star"
                          @click="editReview.rating = star"
                          class="star"
                          :class="{ active: star <= editReview.rating }"
                        >
                          {{ star <= editReview.rating ? '⭐' : '☆' }}
                        </span>
                      </div>
                    </div>
                    <div class="form-group">
                      <textarea 
                        v-model="editReview.content" 
                        rows="3"
                        class="review-textarea"
                      ></textarea>
                    </div>
                    <div class="edit-actions">
                      <button @click="saveEditReview(review.reviewId)" class="btn btn-primary btn-sm">저장</button>
                      <button @click="cancelEditReview" class="btn btn-secondary btn-sm">취소</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="closeDetail" class="btn btn-secondary">닫기</button>
          <button @click="addToPlan" class="btn btn-primary">일정에 추가</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
// Mobile First Styles
h1 {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--color-text-main);
  margin-bottom: var(--spacing-lg);
  letter-spacing: -0.025em;
  
  @media (min-width: 768px) {
    font-size: var(--font-size-3xl);
    margin-bottom: var(--spacing-xl);
  }
}

.search-section {
  margin-bottom: 1.5rem;
  box-shadow: var(--shadow-card);

  h3 {
    font-size: var(--font-size-lg);
    font-weight: 700;
    color: var(--color-text-main);
    margin-bottom: var(--spacing-md);
    
    @media (min-width: 768px) {
      font-size: var(--font-size-xl);
      margin-bottom: var(--spacing-lg);
    }
  }
  
  @media (min-width: 768px) {
    margin-bottom: 2rem;
  }
}

.location-status {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 0.875rem;
  background: linear-gradient(135deg, var(--color-cyan-light) 0%, #E0F9FC 100%);
  border-radius: var(--radius-lg);
  margin-bottom: 1rem;
  border: 1px solid rgba(34, 211, 238, 0.2);
  
  @media (min-width: 768px) {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
    padding: 1rem;
    margin-bottom: 1.5rem;
  }
}

.location-info {
  font-size: var(--font-size-sm);
  color: var(--color-text-main);
  font-weight: 600;
}

.location-error {
  font-size: var(--font-size-sm);
  color: var(--color-danger);
  font-weight: 600;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: var(--font-size-sm);
  font-weight: 600;
  width: 100%;
  
  @media (min-width: 768px) {
    width: auto;
  }
}

.search-controls {
  display: flex;
  flex-direction: column;
  gap: 0.875rem;

  .form-group {
    flex: 1;

    label {
      font-weight: 600;
      color: var(--color-text-main);
      display: block;
      margin-bottom: 0.5rem;
    }
  }

  .invisible-label {
    visibility: hidden;
  }

  .btn {
    white-space: nowrap;
    width: 100%;
    min-height: 48px;
    margin-top: auto; /* Just in case */
  }
  
  @media (min-width: 768px) {
    flex-direction: row;
    align-items: flex-start; /* Default alignment */
    gap: 1rem;
    
    .search-btn-group {
      flex: 0 0 auto; /* Don't stretch button column unnecessarily */
      min-width: 120px;
    }

    .btn {
      width: 100%;
    }
  }
}

// Category Filter Section
.category-filter-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--color-border-light);
  
  @media (min-width: 768px) {
    margin-top: 2rem;
    padding-top: 2rem;
  }
}

.filter-label {
  display: block;
  font-weight: 700;
  color: var(--color-text-main);
  margin-bottom: 0.75rem;
  font-size: var(--font-size-base);
}

.category-filters {
  display: flex;
  gap: 0.5rem;
  
  // Mobile: 가로 스크롤
  overflow-x: auto;
  overflow-y: hidden;
  flex-wrap: nowrap;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none; // Firefox
  
  &::-webkit-scrollbar {
    display: none; // Chrome, Safari
  }
  
  // Tablet+: 여러 줄 wrap
  @media (min-width: 768px) {
    flex-wrap: wrap;
    gap: 0.75rem;
    overflow-x: visible;
  }
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.625rem 0.875rem;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  background-color: var(--color-surface);
  color: var(--color-text-main);
  font-size: var(--font-size-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  min-height: 44px;
  flex-shrink: 0; // 모바일에서 버튼 크기 유지
  
  @media (min-width: 768px) {
    padding: 0.5rem 1rem;
    gap: 0.5rem;
  }

  &:hover {
    border-color: var(--color-cyan);
    color: var(--color-cyan);
    transform: translateY(-1px);
  }

  &.active {
    background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
    border-color: var(--color-cyan);
    color: white;
    box-shadow: var(--shadow-md);
  }
}

.filter-icon {
  font-size: 1.125rem;
  
  @media (min-width: 768px) {
    font-size: 1.25rem;
  }
}

.filter-text {
  white-space: nowrap;
}

// Mobile first layout
.planning-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
  
  @media (min-width: 1024px) {
    grid-template-columns: 2fr 1fr;
    gap: 2rem;
  }
}

.gallery-section,
.current-plan-section {
  h3 {
    font-size: var(--font-size-lg);
    font-weight: 700;
    color: var(--color-text-main);
    margin-bottom: var(--spacing-md);
    
    @media (min-width: 768px) {
      font-size: var(--font-size-xl);
      margin-bottom: var(--spacing-lg);
    }
  }
}

.loading-state,
.empty-state,
.error-state {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-muted);
  font-size: var(--font-size-base);
  background-color: var(--color-background-alt);
  border-radius: var(--radius-lg);
  border: 2px dashed var(--color-border);
  
  @media (min-width: 768px) {
    padding: 3rem;
    font-size: var(--font-size-lg);
  }
}

.error-state {
  border-color: var(--color-danger-border);
  background-color: var(--color-danger-bg);
  
  .error-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
    
    @media (min-width: 768px) {
      font-size: 4rem;
    }
  }
  
  .error-message {
    color: var(--color-text-main);
    font-weight: 600;
    font-size: var(--font-size-base);
    margin-bottom: 0.75rem;
    
    @media (min-width: 768px) {
      font-size: var(--font-size-lg);
      margin-bottom: 1rem;
    }
  }
  
  .error-hint {
    color: var(--color-text-muted);
    font-size: var(--font-size-sm);
    margin: 0;
    
    @media (min-width: 768px) {
      font-size: var(--font-size-base);
    }
  }
}

// Gallery Grid: 2 columns mobile, 3 tablet, 4 desktop
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  
  @media (min-width: 768px) {
    grid-template-columns: repeat(3, 1fr);
    gap: 1.25rem;
  }
  
  @media (min-width: 1024px) {
    grid-template-columns: repeat(4, 1fr);
  }
}

.place-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 1rem;
  box-shadow: var(--shadow-sm);
  background-color: var(--color-surface);
  
  @media (min-width: 768px) {
    padding: 1.25rem;
  }

  &:hover {
    transform: translateY(-8px);
    box-shadow: var(--shadow-card-hover);
    border-color: var(--color-cyan);

    .place-img-placeholder {
      background: linear-gradient(135deg, var(--color-cyan-light) 0%, #E0F9FC 100%);
    }
  }

  h4 {
    font-size: var(--font-size-sm);
    font-weight: 700;
    color: var(--color-text-main);
    margin: 0.75rem 0 0.5rem;
    line-height: 1.4;
    
    @media (min-width: 768px) {
      font-size: var(--font-size-base);
    }
  }
}

.place-img-placeholder {
  height: 120px;
  background-color: var(--color-background-alt);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  font-weight: 600;
  font-size: var(--font-size-sm);
  border-radius: var(--radius-lg);
  transition: background 0.3s ease;
  
  @media (min-width: 768px) {
    height: 140px;
  }
}

.place-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: var(--radius-lg);
  transition: transform 0.3s ease;
  
  @media (min-width: 768px) {
    height: 140px;
  }
}

.place-addr {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  line-height: 1.4;
  
  @media (min-width: 768px) {
    font-size: var(--font-size-sm);
  }
}

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.plan-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.875rem;
  background-color: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all 0.2s ease;
  border: 1px solid var(--color-border-light);
  
  @media (min-width: 768px) {
    padding: 1rem;
  }

  &:hover {
    box-shadow: var(--shadow-md);
    border-color: var(--color-cyan-light);
  }
}

.item-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
  
  @media (min-width: 768px) {
    gap: 0.75rem;
  }
}

.item-details {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  flex: 1;
}

.step-badge {
  background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
  color: white;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);
  
  @media (min-width: 768px) {
    width: 32px;
    height: 32px;
    font-size: var(--font-size-sm);
  }
}

.item-title {
  font-weight: 700;
  font-size: var(--font-size-sm);
  color: var(--color-text-main);
  line-height: 1.4;
  
  @media (min-width: 768px) {
    font-size: var(--font-size-base);
  }
}

.item-desc {
  font-size: 0.75rem;
  color: var(--color-cyan);
  font-style: italic;
  line-height: 1.5;
  font-weight: 500;
  
  @media (min-width: 768px) {
    font-size: var(--font-size-sm);
  }
}

.btn-delete {
  background: none;
  border: none;
  color: var(--color-danger);
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.25rem;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  transition: all 0.2s ease;
  min-width: 44px;
  min-height: 44px;

  &:hover {
    background-color: var(--color-danger-bg);
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
  padding: 1rem;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-xl);
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  @media (min-width: 768px) {
    width: 90%;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--color-border-light);
  
  @media (min-width: 768px) {
    margin-bottom: 1.5rem;
  }

  h2 {
    font-size: var(--font-size-xl);
    font-weight: 700;
    color: var(--color-text-main);
    line-height: 1.3;
    margin: 0;
    flex: 1;
    
    @media (min-width: 768px) {
      font-size: var(--font-size-2xl);
    }
  }
}

.btn-close {
  background: none;
  border: none;
  font-size: 2rem;
  cursor: pointer;
  color: var(--color-text-muted);
  width: 2.5rem;
  height: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover {
    background-color: var(--color-background);
    color: var(--color-text-main);
  }
}

.modal-body {
  margin-bottom: 1.5rem;
}

.place-address {
  color: var(--color-text-muted);
  font-weight: 600;
  font-size: var(--font-size-sm);
  margin-bottom: 1rem;
  padding: 0.75rem;
  background-color: var(--color-background-alt);
  border-radius: var(--radius-md);
  border-left: 3px solid var(--color-cyan);
  
  @media (min-width: 768px) {
    font-size: var(--font-size-base);
    margin-bottom: 1.5rem;
  }
}

.detail-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: var(--radius-lg);
  margin-bottom: 1rem;
  box-shadow: var(--shadow-md);
  
  @media (min-width: 768px) {
    height: 300px;
    margin-bottom: 1.5rem;
  }
}

.description-section {
  margin: 1.5rem 0;
  padding: 1.25rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: var(--radius-md);
  color: white;
  
  @media (min-width: 768px) {
    padding: 1.5rem;
  }
}

.description-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  
  h4 {
    margin: 0;
    font-size: 1rem;
    
    @media (min-width: 768px) {
      font-size: 1.1rem;
    }
  }
}

.btn-secondary.btn-sm {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.3);
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.loading-box {
  background: rgba(255, 255, 255, 0.1);
  border: 1px dashed rgba(255, 255, 255, 0.3);
  border-radius: var(--radius-sm);
  padding: 1.5rem;
  text-align: center;
  
  @media (min-width: 768px) {
    padding: 2rem;
  }
}

.spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto 1rem;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.description-box {
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  padding: 1.25rem;
  border-radius: var(--radius-sm);
  line-height: 1.6;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  
  @media (min-width: 768px) {
    padding: 1.5rem;
  }
  
  &.fallback {
    background: rgba(255, 255, 255, 0.8);
    border: 1px dashed rgba(0, 0, 0, 0.2);
  }
  
  p {
    margin: 0;
  }
}

.original-overview {
  margin: 1rem 0;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: var(--radius-md);
  border: 1px solid #e9ecef;
  
  summary {
    cursor: pointer;
    font-weight: 500;
    color: #666;
    padding: 0.5rem;
    
    &:hover {
      color: var(--color-primary);
    }
  }
  
  .full-desc {
    padding: 1rem;
    margin-top: 0.5rem;
    color: #666;
    line-height: 1.6;
  }
}

.modal-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-top: 1rem;
  border-top: 2px solid var(--color-border-light);
  
  @media (min-width: 768px) {
    flex-direction: row;
    justify-content: flex-end;
    gap: 1rem;
    padding-top: 1.5rem;
  }

  .btn {
    padding: 0.75rem 1.5rem;
    width: 100%;
    
    @media (min-width: 768px) {
      width: auto;
    }
  }
}

.mt-3 {
  margin-top: 1.5rem;
}

// Review Section Styles
.review-section {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 2px solid var(--color-border-light);

  h3 {
    font-size: var(--font-size-lg);
    font-weight: 700;
    color: var(--color-text-main);
    margin-bottom: 1.5rem;
    
    @media (min-width: 768px) {
      font-size: var(--font-size-xl);
    }
  }
}

.review-form {
  background-color: var(--color-background-alt);
  padding: 1.25rem;
  border-radius: var(--radius-lg);
  margin-bottom: 1.5rem;
  border: 1px solid var(--color-border-light);

  h4 {
    font-size: var(--font-size-base);
    font-weight: 600;
    color: var(--color-text-main);
    margin-bottom: 1rem;
  }

  .form-group {
    margin-bottom: 1rem;

    label {
      display: block;
      font-weight: 600;
      color: var(--color-text-main);
      margin-bottom: 0.5rem;
      font-size: var(--font-size-sm);
    }
  }

  @media (min-width: 768px) {
    padding: 1.5rem;
  }
}

.rating-input {
  display: flex;
  gap: 0.25rem;

  .star {
    font-size: 1.5rem;
    cursor: pointer;
    transition: transform 0.2s ease;

    &:hover {
      transform: scale(1.2);
    }

    &.active {
      filter: brightness(1.2);
    }
  }
}

.review-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-family: inherit;
  font-size: var(--font-size-sm);
  resize: vertical;
  transition: border-color 0.2s ease;

  &:focus {
    outline: none;
    border-color: var(--color-cyan);
  }

  @media (min-width: 768px) {
    font-size: var(--font-size-base);
  }
}

.review-list {
  margin-top: 1.5rem;

  .loading-state,
  .empty-state {
    text-align: center;
    padding: 2rem;
    color: var(--color-text-muted);
    font-size: var(--font-size-sm);
    background-color: var(--color-background-alt);
    border-radius: var(--radius-lg);
    border: 2px dashed var(--color-border);
  }
}

.review-item {
  background-color: var(--color-surface);
  padding: 1rem;
  border-radius: var(--radius-lg);
  margin-bottom: 1rem;
  border: 1px solid var(--color-border-light);
  transition: all 0.2s ease;

  &:hover {
    box-shadow: var(--shadow-sm);
    border-color: var(--color-cyan-light);
  }

  @media (min-width: 768px) {
    padding: 1.25rem;
  }
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.review-user {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;

  @media (min-width: 768px) {
    flex-direction: row;
    align-items: center;
    gap: 0.75rem;
  }
}

.user-id {
  font-weight: 600;
  color: var(--color-text-main);
  font-size: var(--font-size-sm);

  @media (min-width: 768px) {
    font-size: var(--font-size-base);
  }
}

.review-rating {
  font-size: 1rem;
  
  @media (min-width: 768px) {
    font-size: 1.125rem;
  }
}

.review-actions {
  display: flex;
  gap: 0.75rem;
}

.text-btn {
  background: none;
  border: none;
  color: var(--color-cyan);
  font-size: var(--font-size-sm);
  font-weight: 600;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  transition: all 0.2s ease;

  &:hover {
    color: var(--color-cyan-hover);
    text-decoration: underline;
  }

  &.delete {
    color: var(--color-danger);

    &:hover {
      color: #c53030;
    }
  }
}

.review-content {
  color: var(--color-text-main);
  line-height: 1.6;
  margin-bottom: 0.5rem;
  font-size: var(--font-size-sm);

  @media (min-width: 768px) {
    font-size: var(--font-size-base);
  }
}

.review-date {
  font-size: 0.75rem;
  color: var(--color-text-muted);

  @media (min-width: 768px) {
    font-size: var(--font-size-xs);
  }
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.5rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.review-edit-mode {
  .form-group {
    margin-bottom: 1rem;

    label {
      display: block;
      font-weight: 600;
      color: var(--color-text-main);
      margin-bottom: 0.5rem;
      font-size: var(--font-size-sm);
    }
  }
}

.edit-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

</style>
