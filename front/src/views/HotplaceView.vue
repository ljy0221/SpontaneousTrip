<script setup>
import { onMounted, ref } from 'vue'
import { useHotplaceStore } from '@/stores/hotplace'

const hotplaceStore = useHotplaceStore()
const error = ref(null)
const selectedPlace = ref(null)
const showModal = ref(false)
const aiDescription = ref(null)
const chatMessages = ref([])
const chatInput = ref('')
const loadingDescription = ref(false)
const loadingChat = ref(false)

onMounted(async () => {
  try {
    await hotplaceStore.getHotplaces()
  } catch (e) {
    error.value = '인기 장소를 불러오는데 실패했습니다. 접근 권한이 없거나 서버 오류입니다.'
    console.error(e)
  }
})

// ✅ 개선: Detail 열 때 자동으로 AI description 생성
const openDetail = async (place) => {
  selectedPlace.value = place
  showModal.value = true
  aiDescription.value = null  // 초기화
  chatMessages.value = []
  chatInput.value = ''

  // 🤖 자동으로 AI description 생성 (비동기 실행)
  generateAiDescription()
  
  // TODO: 리뷰 불러오기 함수가 있다면 여기서 병렬로 실행하세요.
  // fetchReviews()
}

const closeDetail = () => {
  showModal.value = false
  selectedPlace.value = null
  aiDescription.value = null
}

// ✅ 새로운 함수: AI description 자동 생성 (스트리밍)
const generateAiDescription = async () => {
  if (!selectedPlace.value) return

  loadingDescription.value = true
  aiDescription.value = '' // 빈 문자열로 초기화

  try {
    console.log('🤖 Generating AI description for:', selectedPlace.value.placeName)

    const reader = await hotplaceStore.getSummary({
      place_id: selectedPlace.value.placeId,
      max_length: 300
    })

    // Read stream
    const decoder = new TextDecoder("utf-8")

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      aiDescription.value += chunk
    }

    console.log('✅ AI description generated:', aiDescription.value)

  } catch (e) {
    console.error('⚠️ Failed to generate AI description:', e)
    // Fallback: overview 사용
    aiDescription.value = selectedPlace.value.overview || '설명을 사용할 수 없습니다.'
  } finally {
    loadingDescription.value = false
  }
}

const sendMessage = async () => {
  if (!chatInput.value.trim() || !selectedPlace.value) return

  const userMessage = chatInput.value
  chatMessages.value.push({ role: 'user', content: userMessage })
  chatInput.value = ''
  loadingChat.value = true

  // Create empty assistant message
  const assistantMsgIndex = chatMessages.value.length
  chatMessages.value.push({ role: 'assistant', content: '' })

  try {
    const reader = await hotplaceStore.chat({
      query: userMessage,
      place_id: selectedPlace.value.placeId
    })

    // Read stream
    const decoder = new TextDecoder("utf-8")

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      chatMessages.value[assistantMsgIndex].content += chunk
    }

  } catch (e) {
    console.error(e)
    chatMessages.value[assistantMsgIndex].content = '오류: 응답을 받는데 실패했습니다.'
  } finally {
    loadingChat.value = false
  }
}
</script>

<template>
  <div class="container">
    <h1>인기 장소</h1>

    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else class="hotplace-grid">
      <div v-for="place in hotplaceStore.hotplaces" :key="place.placeId" class="card hotplace-card">
        <h3>{{ place.placeName }}</h3>
        <p class="type">{{ place.category }}</p>
        <p class="desc">{{ place.overview ? place.overview.substring(0, 100) + '...' : '설명 없음' }}</p>
        <div class="meta">
          <span>⭐ {{ place.avgRating }}</span>
          <span>리뷰: {{ place.reviewCount || 0 }}</span>
        </div>
        <button @click="openDetail(place)" class="btn btn-secondary btn-block">상세 보기</button>
      </div>
    </div>

    <!-- ✅ 개선된 Detail Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeDetail">
      <div class="modal-content card">
        <div class="modal-header">
          <h2>{{ selectedPlace.placeName }}</h2>
          <button @click="closeDetail" class="btn-close">×</button>
        </div>

        <div class="modal-body">
          <p><strong>카테고리:</strong> {{ selectedPlace.category }}</p>
          <p><strong>주소:</strong> {{ selectedPlace.address }}</p>

          <!-- ✅ AI Description 섹션 (자동 생성) -->
          <div class="description-section">
            <div class="description-header">
              <h4>✨ AI 설명</h4>
            </div>

            <!-- 로딩 중 -->
            <div v-if="loadingDescription" class="loading-box">
              <div class="spinner"></div>
              <p>AI 설명 생성 중...</p>
            </div>

            <!-- AI Description 표시 -->
            <div v-else-if="aiDescription" class="description-box">
              <p>{{ aiDescription }}</p>
            </div>

            <!-- Fallback -->
            <div v-else class="description-box fallback">
              <p>{{ selectedPlace.overview || '설명을 사용할 수 없습니다.' }}</p>
						</div>
          </div>

          <!-- ✅ 원본 Overview는 접을 수 있게 (선택사항) -->
          <details class="original-overview">
            <summary>📄 원본 설명</summary>
            <p class="full-desc">{{ selectedPlace.overview || '설명 없음.' }}</p>
          </details>

          <!-- Chat Section -->
          <div class="chat-section">
            <h3>💬 AI 가이드와 채팅</h3>
            <div class="chat-window">
              <div v-if="chatMessages.length === 0" class="empty-chat">
                이 장소에 대해 무엇이든 물어보세요!
              </div>
              <div v-for="(msg, index) in chatMessages" :key="index" :class="['chat-message', msg.role]">
                <div class="message-content">{{ msg.content }}</div>
              </div>
            </div>
            <div class="chat-input-area">
              <input
                v-model="chatInput"
                @keyup.enter="sendMessage"
                placeholder="질문을 입력하세요..."
                class="form-input"
                :disabled="loadingChat"
              />
              <button @click="sendMessage" class="btn btn-primary" :disabled="loadingChat">전송</button>
            </div>
          </div>
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
  letter-spacing: -0.025em;
  margin-bottom: 1.5rem;
  text-align: center;
  
  @media (min-width: 768px) {
    font-size: var(--font-size-3xl);
    margin-bottom: 2rem;
  }
}

// Grid: 1 column mobile, 2 tablet, 3 desktop
.hotplace-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
  margin-top: 1.5rem;
  
  @media (min-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 2rem;
    margin-top: 2rem;
  }
  
  @media (min-width: 1024px) {
    grid-template-columns: repeat(3, 1fr);
    margin-top: 2.5rem;
  }
}

.hotplace-card {
	display: flex;
	flex-direction: column;
	height: 100%;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

	&:hover {
		transform: translateY(-8px);
		box-shadow: var(--shadow-card-hover);
	}

	h3 {
		font-size: var(--font-size-xl);
		font-weight: 700;
		color: var(--color-text-main);
		margin-bottom: 0.75rem;
		line-height: 1.3;
	}
}

.type {
	display: inline-block;
	padding: 0.25rem 0.75rem;
	background: linear-gradient(135deg, var(--color-cyan-light) 0%, #E0F9FC 100%);
	color: var(--color-cyan);
	font-weight: 600;
	font-size: var(--font-size-sm);
	border-radius: var(--radius-full);
	margin-bottom: 1rem;
}

.desc {
	color: var(--color-text-muted);
	flex-grow: 1;
	margin-bottom: 1.5rem;
	line-height: 1.6;
	font-size: var(--font-size-sm);
}

.meta {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 1.5rem;
	padding-top: 1rem;
	border-top: 1px solid var(--color-border-light);
	font-weight: 600;
	font-size: var(--font-size-sm);
	color: var(--color-text-main);

	span {
		display: flex;
		align-items: center;
		gap: 0.25rem;
	}
}

.btn-block {
	width: 100%;
	padding: 0.875rem;
	font-weight: 600;
	background-color: var(--color-cyan);
	color: white;
	border-radius: var(--radius-lg);
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

	&:hover {
		background-color: var(--color-cyan-hover);
		transform: translateY(-2px);
		box-shadow: var(--shadow-md);
	}
}

.error-message {
	color: var(--color-danger);
	padding: 2.5rem;
	text-align: center;
	background-color: var(--color-danger-bg);
	border: 2px solid var(--color-danger-border);
	border-radius: var(--radius-xl);
	margin-top: 2rem;
	font-weight: 600;
	font-size: var(--font-size-lg);
}

.modal-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.6);
	backdrop-filter: blur(4px);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
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
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  padding: 1.5rem;
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  @media (min-width: 768px) {
    width: 90%;
    padding: 2rem;
    border-radius: var(--radius-2xl);
  }
}

@keyframes slideUp {
	from {
		opacity: 0;
		transform: translateY(20px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--color-border-light);

  h2 {
    font-size: var(--font-size-xl);
    font-weight: 700;
    color: var(--color-text-main);
    margin: 0;
    
    @media (min-width: 768px) {
      font-size: var(--font-size-2xl);
    }
  }
  
  @media (min-width: 768px) {
    margin-bottom: 1.5rem;
    padding-bottom: 1.5rem;
  }
}

.btn-close {
	background: none;
	border: none;
	font-size: 2rem;
	color: var(--color-text-muted);
	cursor: pointer;
	width: 40px;
	height: 40px;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: var(--radius-full);
	transition: all 0.2s ease;
	flex-shrink: 0;

	&:hover {
		background-color: var(--color-background-alt);
		color: var(--color-text-main);
	}
}

.modal-body {
	p {
		margin-bottom: 1rem;
		line-height: 1.6;

		strong {
			color: var(--color-cyan);
			font-weight: 600;
		}
	}
}

.description-section {
  margin: 1.5rem 0;
  padding: 1.5rem;
  background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
  border-radius: var(--radius-lg);
  color: white;
  box-shadow: var(--shadow-lg);
  
  @media (min-width: 768px) {
    padding: 2rem;
    border-radius: var(--radius-xl);
  }
}

.description-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;

  h4 {
    margin: 0;
    font-size: var(--font-size-lg);
    font-weight: 700;
    
    @media (min-width: 768px) {
      font-size: var(--font-size-xl);
    }
  }
  
  @media (min-width: 768px) {
    margin-bottom: 1.5rem;
  }
}

.btn-sm {
	padding: 0.5rem 1rem;
	font-size: var(--font-size-sm);
	background: rgba(255, 255, 255, 0.2);
	border: 1px solid rgba(255, 255, 255, 0.4);
	color: white;
	border-radius: var(--radius-lg);
	cursor: pointer;
	transition: all 0.3s ease;
	font-weight: 600;

	&:hover:not(:disabled) {
		background: rgba(255, 255, 255, 0.3);
		transform: scale(1.05);
	}

	&:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}
}

.loading-box {
  background: rgba(255, 255, 255, 0.15);
  border: 2px dashed rgba(255, 255, 255, 0.4);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  text-align: center;
  
  @media (min-width: 768px) {
    padding: 2.5rem;
  }
}

.spinner {
	width: 50px;
	height: 50px;
	margin: 0 auto 1.5rem;
	border: 4px solid rgba(255, 255, 255, 0.3);
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
	border-radius: var(--radius-lg);
	line-height: 1.7;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

	&.fallback {
		background: rgba(255, 255, 255, 0.8);
		border: 2px dashed rgba(0, 0, 0, 0.2);
	}

	p {
		margin: 0;
		font-size: var(--font-size-base);
	}
}

.original-overview {
	margin: 1.5rem 0;
	padding: 1.5rem;
	background-color: var(--color-background-alt);
	border-radius: var(--radius-lg);
	border: 1px solid var(--color-border-light);

	summary {
		cursor: pointer;
		font-weight: 600;
		color: var(--color-text-muted);
		padding: 0.5rem;
		transition: color 0.2s ease;

		&:hover {
			color: var(--color-cyan);
		}
	}

	.full-desc {
		padding: 1rem;
		margin-top: 1rem;
		color: var(--color-text-main);
		line-height: 1.7;
		border-top: 1px solid var(--color-border-light);
	}
}

.chat-section {
	margin-top: 2rem;
	border-top: 2px solid var(--color-border-light);
	padding-top: 2rem;

	h3 {
		font-size: var(--font-size-xl);
		font-weight: 700;
		color: var(--color-text-main);
		margin-bottom: 1rem;
	}
}

.chat-window {
	height: 300px;
	overflow-y: auto;
	border: 2px solid var(--color-border);
	border-radius: var(--radius-lg);
	padding: 1.5rem;
	margin-bottom: 1rem;
	background-color: var(--color-background-alt);
}

.empty-chat {
	text-align: center;
	color: var(--color-text-muted);
	margin-top: 3rem;
	font-size: var(--font-size-lg);
	font-weight: 500;
}

.chat-message {
	margin-bottom: 1rem;
	display: flex;

	&.user {
		justify-content: flex-end;

		.message-content {
			background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
			color: white;
			border-bottom-right-radius: 0.25rem;
		}
	}

	&.assistant {
		justify-content: flex-start;

		.message-content {
			background-color: var(--color-surface);
			color: var(--color-text-main);
			border: 1px solid var(--color-border);
			border-bottom-left-radius: 0.25rem;
		}
	}
}

.message-content {
	padding: 0.75rem 1.25rem;
	border-radius: var(--radius-xl);
	max-width: 80%;
	line-height: 1.5;
	box-shadow: var(--shadow-sm);
}

.chat-input-area {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;

  .form-input {
    flex-grow: 1;
    padding: 0.875rem 1rem;
    border: 2px solid var(--color-border);
    border-radius: var(--radius-lg);
    font-size: var(--font-size-base);
    transition: all 0.2s ease;

    &:focus {
      outline: none;
      border-color: var(--color-cyan);
      box-shadow: var(--shadow-focus);
    }

    &:disabled {
      background-color: var(--color-background-alt);
      cursor: not-allowed;
    }
  }

  .btn {
    padding: 0.875rem 1.5rem;
    font-weight: 600;
    background-color: var(--color-cyan);
    color: white;
    border-radius: var(--radius-lg);
    width: 100%;
    min-height: 48px;

    &:hover:not(:disabled) {
      background-color: var(--color-cyan-hover);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
  
  @media (min-width: 768px) {
    flex-direction: row;
    
    .btn {
      width: auto;
    }
  }
}

.chat-window {
  height: 250px;
  
  @media (min-width: 768px) {
    height: 300px;
  }
}
</style>
