<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useBoardStore } from '@/stores/board'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const boardStore = useBoardStore()
const authStore = useAuthStore()

const title = ref('')
const content = ref('')
const category = ref('QNA')
const id = route.params.id

onMounted(async () => {
  await boardStore.getPostById(id)
  const post = boardStore.currentPost
  if (post) {
    title.value = post.title
    content.value = post.content
    category.value = post.category || 'QNA'
  } else {
    alert('게시글을 찾을 수 없습니다!')
    router.push('/board')
  }
})

const handleSubmit = async () => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다.')
    router.push('/login')
    return
  }

  if (!title.value || !content.value) {
    alert('모든 필드를 입력해주세요.')
    return
  }

  try {
    await boardStore.updatePost(id, {
      title: title.value,
      content: content.value,
      author: authStore.userId,
      category: category.value
    })
    router.push(`/board/${id}`)
  } catch (error) {
    alert('게시글 수정에 실패했습니다.')
  }
}

const cancel = () => {
  router.back()
}
</script>

<template>
  <div class="container">
    <h1>게시글 수정</h1>
    <form @submit.prevent="handleSubmit" class="card">
      <div class="form-group">
        <label class="form-label">카테고리</label>
        <select v-model="category" class="form-input" required>
          <option value="NOTICE">공지사항</option>
          <option value="QNA">질문글</option>
          <option value="REQUEST">Hotplace 요청</option>
        </select>
      </div>
      <div class="form-group">
        <label class="form-label">제목</label>
        <input 
          v-model="title" 
          type="text" 
          class="form-input" 
          placeholder="제목을 입력하세요" 
          required
        />
      </div>
      <div class="form-group">
        <label class="form-label">내용</label>
        <textarea 
          v-model="content" 
          class="form-input" 
          rows="10" 
          placeholder="내용을 입력하세요" 
          required
        ></textarea>
      </div>
      <div class="actions">
        <button type="button" @click="cancel" class="btn btn-secondary">취소</button>
        <button type="submit" class="btn btn-primary">수정</button>
      </div>
    </form>
  </div>
</template>

<style lang="scss" scoped>
h1 {
	font-size: var(--font-size-3xl);
	font-weight: 700;
	color: var(--color-text-main);
	letter-spacing: -0.025em;
	margin-bottom: 2rem;
	text-align: center;
}

.card {
	max-width: 800px;
	margin: 0 auto;
	padding: 2.5rem;
	background-color: var(--color-surface);
	border-radius: var(--radius-xl);
	box-shadow: var(--shadow-card);
	border: 1px solid var(--color-border-light);
}

.form-group {
	margin-bottom: 1.5rem;
}

.form-label {
	display: block;
	margin-bottom: 0.5rem;
	font-weight: 600;
	color: var(--color-text-main);
	font-size: var(--font-size-sm);
	text-transform: uppercase;
	letter-spacing: 0.05em;
}

.form-input {
	width: 100%;
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

	&::placeholder {
		color: var(--color-text-muted);
	}
}

textarea.form-input {
	resize: vertical;
	min-height: 200px;
	line-height: 1.6;
	font-family: inherit;
}

.actions {
	display: flex;
	justify-content: flex-end;
	gap: 1rem;
	margin-top: 2.5rem;
	padding-top: 2rem;
	border-top: 1px solid var(--color-border-light);

	.btn {
		padding: 0.875rem 2rem;
		font-weight: 600;
		border-radius: var(--radius-lg);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

		&:hover {
			transform: translateY(-2px);
			box-shadow: var(--shadow-md);
		}
	}

	.btn-primary {
		background-color: var(--color-cyan);
		color: white;

		&:hover {
			background-color: var(--color-cyan-hover);
		}
	}

	.btn-secondary {
		background-color: var(--color-surface);
		color: var(--color-text-main);
		border: 2px solid var(--color-border);

		&:hover {
			background-color: var(--color-background);
			border-color: var(--color-cyan);
			color: var(--color-cyan);
		}
	}
}

@media (max-width: 768px) {
	.card {
		padding: 1.5rem;
	}

	.actions {
		flex-direction: column-reverse;

		.btn {
			width: 100%;
		}
	}
}
</style>
