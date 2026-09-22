<script setup>
import { ref, onMounted } from 'vue'
import { useHotplaceStore } from '@/stores/hotplace'
import { useReviewStore } from '@/stores/review'
import { useRouter } from 'vue-router'

const hotplaceStore = useHotplaceStore()
const reviewStore = useReviewStore()
const router = useRouter()

const popularPlaces = ref([])
const loading = ref(false)
const currentLocation = ref({
  lat: 36.355306,
  lon: 127.298111,
})

// Modal state
const selectedPlace = ref(null)
const showDetailModal = ref(false)
const aiDescription = ref(null)
const loadingDescription = ref(false)

// Review state
const reviews = ref([])
const loadingReviews = ref(false)

onMounted(async () => {
  await getCurrentLocation()
  await loadPopularPlaces()
})

const getCurrentLocation = async () => {
  if (!navigator.geolocation) {
    console.warn('Geolocation is not supported')
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
  } catch (error) {
    console.warn('위치를 찾을 수 없습니다. 기본 위치를 사용합니다.', error)
  }
}

const loadPopularPlaces = async () => {
  loading.value = true
  try {
    popularPlaces.value = await hotplaceStore.getPopularHotplaces(
      currentLocation.value.lat,
      currentLocation.value.lon,
      5000, // 5km 반경
      10 // 상위 10개
    )
  } catch (error) {
    console.error('인기 장소를 불러오는데 실패했습니다:', error)
  } finally {
    loading.value = false
  }
}

// Open detail modal
const openDetail = async (place) => {
  selectedPlace.value = place
  showDetailModal.value = true
  aiDescription.value = null
  reviews.value = []

  // AI 설명 자동 생성
  await generateAiDescription()

  // 리뷰 로드
  await loadReviews()
}

const closeDetail = () => {
  showDetailModal.value = false
  selectedPlace.value = null
  aiDescription.value = null
}

// AI description 생성 (스트리밍)
const generateAiDescription = async () => {
  if (!selectedPlace.value) return

  const placeId = selectedPlace.value.placeId

  if (!placeId) {
    console.warn('No valid place ID found')
    aiDescription.value = selectedPlace.value.overview || 'No description available'
    return
  }

  loadingDescription.value = true
  aiDescription.value = ''

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

      // 타이핑 효과
      await new Promise(resolve => setTimeout(resolve, 10))
    }

    console.log('✅ AI description generated')
  } catch (error) {
    console.warn('⚠️ Failed to generate AI description, using fallback:', error)
    aiDescription.value = selectedPlace.value.overview || 'No description available'
  } finally {
    loadingDescription.value = false
  }
}

// 재생성 버튼
const regenerateDescription = async () => {
  await generateAiDescription()
}

// 리뷰 로드 (읽기 전용)
const loadReviews = async () => {
  if (!selectedPlace.value) return

  const placeId = selectedPlace.value.placeId

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

// 사용자 표시 이름 가져오기 (닉네임 우선)
const getUserDisplayName = (review) => {
  // 백엔드에서 닉네임을 포함하여 반환하므로 nickname 사용
  return review.nickname || review.userId
}
</script>


<template>
  <div>
    <div class="container">
      <div class="hero">
        <h1>여행 플래너에 오신 것을 환영합니다</h1>
        <p>여행을 계획하고 인기 장소를 찾아보세요!</p>
      </div>

      <div class="actions">
        <router-link to="/plan" class="btn btn-primary">여행 계획</router-link>
        <router-link to="/hotplace" class="btn btn-secondary">인기 장소</router-link>
      </div>
    </div>

    <!-- 주변에서 핫한 장소 섹션 -->
    <section class="popular-section">
      <div class="popular-container">
        <h2> 주변에서 핫한 장소</h2>

        <div v-if="loading" class="loading-state">⏳ 인기 장소 로딩 중...</div>

        <div v-else-if="popularPlaces.length === 0" class="empty-state">
          주변에 인기 장소가 없습니다.
        </div>

        <div v-else class="gallery-grid">
          <div
            v-for="place in popularPlaces"
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

            <div class="card-content">
              <h4>{{ place.placeName }}</h4>
              <p class="place-addr">{{ place.address }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Detail Modal -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetail">
      <div class="modal-content card">
        <div class="modal-header">
          <h2>{{ selectedPlace.placeName }}</h2>
          <button @click="closeDetail" class="btn-close">×</button>
        </div>

        <div class="modal-body">
          <p class="place-address">📍 {{ selectedPlace.address }}</p>

          <img
            v-if="selectedPlace.image"
            :src="selectedPlace.image"
            :alt="selectedPlace.placeName"
            class="detail-img"
          />

          <!-- AI Description -->
          <div class="description-section">
            <div class="description-header">
              <h4>✨ AI 설명</h4>
              <button
                @click="regenerateDescription"
                class="btn btn-sm btn-secondary"
                :disabled="loadingDescription"
                title="설명 재생성"
              >
                🔄
              </button>
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

          <!-- 리뷰 섹션 (읽기 전용) -->
          <div class="review-section">
            <h3>💬 리뷰</h3>

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
                  <span class="review-date">{{ new Date(review.createdAt).toLocaleDateString() }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="closeDetail" class="btn btn-secondary">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
// Mobile First Styles
.container {
	min-height: 70vh;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	background-color: var(--color-surface);
	box-shadow: var(--shadow-card);
	border-radius: var(--radius-2xl);
	padding: 3rem;
	position: relative;
	overflow: hidden;
	max-width: 1400px;
	margin: 0 auto;
	width: 100%;
	background-image: url('/background.jpg');
	background-size: cover;
	background-position: center;
	background-repeat: no-repeat;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.4);
    pointer-events: none;
  }

  // Tablet and up
  @media (min-width: 768px) {
    border-radius: var(--radius-2xl);
    padding: 3rem 2rem;
  }

  // Desktop and up
  @media (min-width: 1024px) {
    padding: 3rem;
  }
}

.hero {
  text-align: center;
  margin-bottom: 2rem;
  position: relative;
  z-index: 1;

  h1 {
    font-size: clamp(2rem, 8vw, 4rem);
    font-weight: 700;
    color: var(--color-text-main);
    margin-bottom: 1rem;
    letter-spacing: -0.025em;
    background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    line-height: 1.2;
  }

  p {
    font-size: var(--font-size-base);
    color: #332b2b;
    font-weight: 750;
    margin-top: 0.5rem;

    @media (min-width: 768px) {
      font-size: var(--font-size-lg);
    }

    @media (min-width: 1024px) {
      font-size: var(--font-size-xl);
    }
  }

  @media (min-width: 768px) {
    margin-bottom: 3rem;
  }
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
  max-width: 320px;
  position: relative;
  z-index: 1;

  .btn {
    padding: 1rem 2rem;
    font-size: var(--font-size-base);
    font-weight: 600;
    border-radius: var(--radius-xl);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: var(--shadow-md);
    text-decoration: none;
    width: 100%;

    &:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-card-hover);
    }

    @media (min-width: 768px) {
      padding: 1rem 2.5rem;
      font-size: var(--font-size-lg);
    }
  }

  .btn-primary {
    background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
    color: white;
    border: none;

    &:hover {
      background: linear-gradient(135deg, var(--color-cyan-hover) 0%, var(--color-teal-hover) 100%);
    }
  }

  .btn-secondary {
    background-color: var(--color-surface);
    color: var(--color-cyan);
    border: 2px solid var(--color-cyan);

    &:hover {
      background-color: var(--color-cyan-light);
    }
  }

  // Tablet and up - horizontal layout
  @media (min-width: 768px) {
    flex-direction: row;
    justify-content: center;
    gap: 1.5rem;
    max-width: none;

    .btn {
      width: auto;
    }
  }
}

.weather-card {
  background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
  color: white;
  padding: 2rem;
  border-radius: var(--radius-lg);
  text-align: center;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-xl);
  position: relative;
  z-index: 1;

  h2 {
    margin-bottom: 1rem;
    font-size: var(--font-size-xl);

    @media (min-width: 768px) {
      font-size: var(--font-size-2xl);
    }
  }

  @media (min-width: 768px) {
    padding: 2.5rem;
    border-radius: var(--radius-xl);
    margin-bottom: 3rem;
  }
}

.weather-info {
  margin-top: 1.5rem;
}

.temp {
  font-size: 3rem;
  font-weight: 700;
  margin: 1rem 0;

  @media (min-width: 768px) {
    font-size: 4rem;
  }
}

.desc {
  font-size: var(--font-size-lg);
  text-transform: capitalize;
  opacity: 0.95;

  @media (min-width: 768px) {
    font-size: var(--font-size-xl);
  }
}
// 인기 장소 섹션 (container 밖)
.popular-section {
	width: 100%;
	padding: 3rem 0;
	background: linear-gradient(180deg, transparent 0%, var(--color-background-alt) 100%);
	margin-top: 2rem;
}

.popular-container {
	max-width: 1400px;
	margin: 0 auto;
	padding: 0 0rem;

	h2 {
		font-size: var(--font-size-2xl);
		font-weight: 700;
		color: var(--color-text-main);
		margin-bottom: 2rem;
		text-align: left;
	}
}

.loading-state,
.empty-state {
	text-align: center;
	padding: 3rem;
	color: var(--color-text-muted);
	font-size: var(--font-size-lg);
	background-color: var(--color-background-alt);
	border-radius: var(--radius-lg);
	border: 2px dashed var(--color-border);
}

.gallery-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
	gap: 1.25rem;
}

.place-card {
	cursor: pointer;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	padding: 0;
	box-shadow: var(--shadow-sm);
	background-color: var(--color-surface);
	overflow: hidden;

	&:hover {
		transform: translateY(-8px);
		box-shadow: var(--shadow-card-hover);
		border-color: var(--color-cyan);

		.place-img-placeholder {
			background: linear-gradient(135deg, var(--color-cyan-light) 0%, #E0F9FC 100%);
		}
	}
}

.place-img-placeholder {
	height: 140px;
	background-color: var(--color-background-alt);
	display: flex;
	align-items: center;
	justify-content: center;
	color: var(--color-text-muted);
	font-weight: 600;
	font-size: var(--font-size-sm);
	transition: background 0.3s ease;
}

.place-img {
	width: 100%;
	height: 140px;
	object-fit: cover;
	transition: transform 0.3s ease;
}

.card-content {
	padding: 1.25rem;

	h4 {
		font-size: var(--font-size-base);
		font-weight: 700;
		color: var(--color-text-main);
		margin: 0 0 0.5rem;
		line-height: 1.4;
	}
}

.place-addr {
	font-size: var(--font-size-sm);
	color: var(--color-text-muted);
	line-height: 1.4;
	margin-bottom: 0.75rem;
}

.place-stats {
	display: flex;
	gap: 0.5rem;
	flex-wrap: wrap;
}

.stat-badge {
	display: inline-flex;
	align-items: center;
	gap: 0.25rem;
	padding: 0.25rem 0.75rem;
	background: linear-gradient(135deg, var(--color-cyan-light) 0%, #E0F9FC 100%);
	color: var(--color-cyan);
	border-radius: var(--radius-full);
	font-size: var(--font-size-xs);
	font-weight: 600;
	border: 1px solid rgba(34, 211, 238, 0.2);
}

@media (max-width: 768px) {
	.container {
		padding: 2rem 1rem;
	}

	.hero {
		h1 {
			font-size: 2.5rem;
		}

		p {
			font-size: var(--font-size-base);
		}
	}

	.actions {
		flex-direction: column;
		width: 100%;
		max-width: 320px;

		.btn {
			width: 100%;
		}
	}

	.gallery-grid {
		grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
	}
}

// Modal styles
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
	width: 90%;
	max-width: 700px;
	max-height: 90vh;
	overflow-y: auto;
	box-shadow: var(--shadow-xl);
	animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
	margin-bottom: 1.5rem;
	padding-bottom: 1rem;
	border-bottom: 2px solid var(--color-border-light);

	h2 {
		font-size: var(--font-size-2xl);
		font-weight: 700;
		color: var(--color-text-main);
		line-height: 1.3;
		margin: 0;
		flex: 1;
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
	font-size: var(--font-size-base);
	margin-bottom: 1.5rem;
	padding: 0.75rem;
	background-color: var(--color-background-alt);
	border-radius: var(--radius-md);
	border-left: 3px solid var(--color-cyan);
}

.detail-img {
	width: 100%;
	height: 300px;
	object-fit: cover;
	border-radius: var(--radius-lg);
	margin-bottom: 1.5rem;
	box-shadow: var(--shadow-md);
}

.description-section {
	margin: 1.5rem 0;
	padding: 1.5rem;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: var(--radius-md);
	color: white;
}

.description-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 1rem;

	h4 {
		margin: 0;
		font-size: 1.1rem;
	}
}

.btn-secondary.btn-sm {
	background: rgba(255, 255, 255, 0.2);
	border: 1px solid rgba(255, 255, 255, 0.3);
	color: white;
	border-radius: var(--radius-sm);
	cursor: pointer;
	transition: all 0.2s;
	padding: 0.5rem 1rem;
	font-size: var(--font-size-sm);

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
	padding: 2rem;
	text-align: center;
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
	padding: 1.5rem;
	border-radius: var(--radius-sm);
	line-height: 1.6;
	box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);

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
			color: var(--color-cyan);
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
	justify-content: flex-end;
	gap: 1rem;
	padding-top: 1.5rem;
	border-top: 2px solid var(--color-border-light);

	.btn {
		padding: 0.75rem 1.5rem;
	}
}

// Review Section Styles (Read-only)
.review-section {
	margin-top: 2rem;
	padding-top: 2rem;
	border-top: 2px solid var(--color-border-light);

	h3 {
		font-size: var(--font-size-lg);
		font-weight: 700;
		color: var(--color-text-main);
		margin-bottom: 1.5rem;
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

</style>
