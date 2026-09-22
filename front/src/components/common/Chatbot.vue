<script setup>
import { ref, nextTick } from 'vue'
import { useHotplaceStore } from '@/stores/hotplace'
import DOMPurify from 'dompurify'

const props = defineProps({
  placeId: {
    type: [String, Number],
    default: null
  },
  candidates: {
    type: Array,
    default: () => []
  }
})

import MarkdownIt from 'markdown-it'
const md = new MarkdownIt({
  breaks: true, // Convert '\n' in paragraphs into <br>
  linkify: true // Autoconvert URL-like text to links
})

const renderMarkdown = (text) => {
  const html = md.render(text || '')
  return DOMPurify.sanitize(html)
}

const hotplaceStore = useHotplaceStore()
const isOpen = ref(false)
const messageInput = ref('')
const messages = ref([
  { id: 1, text: '안녕하세요! 어떤 여행을 하고싶은가요?', isUser: false },
])
const messagesContainer = ref(null)
const isLoading = ref(false)

const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    scrollToBottom()
  }
}

const sendMessage = async () => {
  const text = messageInput.value.trim()
  if (!text || isLoading.value) return

  // Add user message
  messages.value.push({
    id: Date.now(),
    text: text,
    isUser: true,
  })

  messageInput.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    const reader = await hotplaceStore.chat({
      query: text,
      place_id: props.placeId,
      candidates: props.candidates,
      max_tokens: 500,
      temperature: 0.7
    })

    // Create empty bot message
    const botMsgId = Date.now() + 1
    messages.value.push({
      id: botMsgId,
      text: '',
      isUser: false,
    })
    
    // Read stream
    const decoder = new TextDecoder("utf-8")
    let botMessageStruct = messages.value.find(m => m.id === botMsgId)

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      botMessageStruct.text += chunk
      await scrollToBottom()
    }

  } catch (error) {
    console.error('Chat error:', error)
    if (Date.now() - (messages.value[messages.value.length - 1]?.id || 0) > 1000) {
        messages.value.push({
        id: Date.now() + 1,
        text: '죄송합니다. 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
        isUser: false,
        })
    } else {
        // 만약 이미 봇 메시지 생성 중에 에러가 났다면 해당 메시지에 에러 표시
         let botMessageStruct = messages.value[messages.value.length - 1]
         if (botMessageStruct && !botMessageStruct.isUser) {
             botMessageStruct.text += '\n[오류가 발생하여 중단되었습니다]'
         }
    }
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}
</script>

<template>
  <div class="chatbot-container">
    <!-- Floating Button -->
    <button class="chatbot-toggle-btn" @click="toggleChat" :class="{ 'is-open': isOpen }">
      <span v-if="!isOpen">💬</span>
      <span v-else>✕</span>
    </button>

    <!-- Chat Window -->
    <transition name="slide-up">
      <div v-if="isOpen" class="chat-window card">
        <div class="chat-header">
          <h3>AI 가이드와 채팅</h3>
          <button @click="toggleChat" class="btn-close-header">✕</button>
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-bubble"
            :class="{ 'user-message': msg.isUser, 'bot-message': !msg.isUser }"
          >
            <!-- User message: Plain text -->
            <div v-if="msg.isUser">
               {{ msg.text }}
            </div>
            <!-- Bot message: Markdown -->
            <div v-else class="markdown-body" v-html="renderMarkdown(msg.text)"></div>
          </div>
        </div>

        <div class="chat-input-area">
          <input
            v-model="messageInput"
            @keyup.enter="sendMessage"
            type="text"
            placeholder="질문을 입력하세요..."
            class="chat-input"
            :disabled="isLoading"
          />
          <button @click="sendMessage" class="btn-send" :disabled="isLoading">
            <span v-if="isLoading">...</span>
            <span v-else>➤</span>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<style lang="scss" scoped>
.chatbot-container {
	position: fixed;
	bottom: 2rem;
	right: 2rem;
	z-index: 9999;
	font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.chatbot-toggle-btn {
	width: 70px;
	height: 70px;
	border-radius: 50%;
	background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
	color: white;
	border: none;
	font-size: 1.75rem;
	cursor: pointer;
	box-shadow: var(--shadow-xl);
	transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
	display: flex;
	align-items: center;
	justify-content: center;

	&:hover {
		transform: scale(1.15);
		box-shadow: 0 8px 25px rgba(34, 211, 238, 0.4);
	}

	&.is-open {
		transform: rotate(90deg);
		background: linear-gradient(135deg, #333 0%, #555 100%);
	}
}

.chat-window {
	position: absolute;
	bottom: 90px;
	right: 0;
	width: 500px;
	height: 80vh;
	max-height: 800px;
	background: var(--color-surface);
	border-radius: var(--radius-2xl);
	box-shadow: var(--shadow-xl);
	display: flex;
	flex-direction: column;
	overflow: hidden;
	border: 1px solid var(--color-border-light);
	transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.chat-header {
	background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
	color: white;
	padding: 1.25rem 1.5rem;
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

	h3 {
		margin: 0;
		font-size: var(--font-size-lg);
		font-weight: 700;
		letter-spacing: -0.025em;
	}
}

.btn-close-header {
	background: rgba(255, 255, 255, 0.2);
	border: none;
	color: white;
	font-size: 1.5rem;
	cursor: pointer;
	width: 36px;
	height: 36px;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.2s ease;

	&:hover {
		background: rgba(255, 255, 255, 0.3);
		transform: rotate(90deg);
	}
}

.chat-messages {
	flex: 1;
	padding: 1.5rem;
	overflow-y: auto;
	display: flex;
	flex-direction: column;
	gap: 1rem;
	background-color: var(--color-background-alt);

	/* Custom scrollbar */
	&::-webkit-scrollbar {
		width: 6px;
	}

	&::-webkit-scrollbar-track {
		background: transparent;
	}

	&::-webkit-scrollbar-thumb {
		background: var(--color-border);
		border-radius: 10px;

		&:hover {
			background: var(--color-text-muted);
		}
	}
}

.message-bubble {
	max-width: 85%;
	padding: 1rem 1.25rem;
	border-radius: var(--radius-xl);
	font-size: var(--font-size-sm);
	line-height: 1.5;
	word-wrap: break-word;
	animation: messageSlide 0.3s ease;
}

@keyframes messageSlide {
	from {
		opacity: 0;
		transform: translateY(10px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.bot-message {
	align-self: flex-start;
	background: white;
	color: var(--color-text-main);
	border-bottom-left-radius: 0.25rem;
	box-shadow: var(--shadow-sm);
	border: 1px solid var(--color-border-light);
}

.user-message {
	align-self: flex-end;
	background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
	color: white;
	border-bottom-right-radius: 0.25rem;
	box-shadow: 0 4px 12px rgba(34, 211, 238, 0.3);
}

.chat-input-area {
	padding: 1.25rem;
	background: white;
	border-top: 1px solid var(--color-border-light);
	display: flex;
	gap: 0.75rem;
}

.chat-input {
	flex: 1;
	padding: 0.875rem 1.25rem;
	border: 2px solid var(--color-border);
	border-radius: var(--radius-full);
	outline: none;
	font-size: var(--font-size-sm);
	transition: all 0.2s ease;
	background-color: var(--color-background-alt);

	&:focus {
		border-color: var(--color-cyan);
		box-shadow: var(--shadow-focus);
		background-color: white;
	}

	&:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}
}

.btn-send {
	background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
	color: white;
	border: none;
	width: 48px;
	height: 48px;
	border-radius: 50%;
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 1.25rem;
	transition: all 0.3s ease;
	box-shadow: var(--shadow-sm);

	&:hover:not(:disabled) {
		transform: scale(1.1);
		box-shadow: var(--shadow-md);
	}

	&:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}
}

.slide-up-enter-active,
.slide-up-leave-active {
	transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.slide-up-enter-from,
.slide-up-leave-to {
	opacity: 0;
	transform: translateY(20px) scale(0.9);
}

@media (max-width: 480px) {
	.chatbot-container {
		bottom: 1rem;
		right: 1rem;
	}

	.chatbot-toggle-btn {
		width: 60px;
		height: 60px;
	}

	.chat-window {
		width: calc(100vw - 2rem);
		max-width: 380px;
		height: 500px;
	}
}

/* Markdown Styles */
:deep(.markdown-body) {
  font-size: 0.95rem;
  line-height: 1.6;

  p {
    margin-bottom: 0.5rem;
    &:last-child {
      margin-bottom: 0;
    }
  }

  ul, ol {
    margin-left: 1.25rem;
    margin-bottom: 0.5rem;
  }

  strong {
    font-weight: 700;
    color: var(--color-teal);
  }

  a {
    color: var(--color-cyan);
    text-decoration: underline;
  }
}
</style>
