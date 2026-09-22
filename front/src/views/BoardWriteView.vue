<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useBoardStore } from '@/stores/board'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const boardStore = useBoardStore()
const userStore = useUserStore()

const title = ref('')
const content = ref('')
const category = ref('QNA') // Default to QNA for regular users

// 관리자 권한 체크
const isAdmin = computed(() => {
  const role = userStore.userInfo?.role
  console.log('===== Frontend Admin Check =====')
  console.log('userInfo:', userStore.userInfo)
  console.log('role:', role)
  console.log('Is Admin:', role === 'ROLE_ADMIN')
  console.log('================================')
  return role === 'ROLE_ADMIN'
})

onMounted(async () => {
  // Ensure user is logged in
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo()
      console.log('User info loaded:', userStore.userInfo)
    } catch (error) {
      alert('로그인이 필요합니다.')
      router.push('/login')
      return
    }
  }
  
  console.log('Current userInfo:', userStore.userInfo)
  console.log('Is Admin:', isAdmin.value)
  
  // 관리자인 경우에만 NOTICE를 기본값으로 설정
  if (isAdmin.value) {
    category.value = 'NOTICE'
  }
})

const handleSubmit = async () => {
  if (!title.value || !content.value) {
    alert('모든 필드를 입력해주세요.')
    return
  }

  // Check if user is logged in
  if (!userStore.userInfo) {
    alert('로그인이 필요합니다.')
    router.push('/login')
    return
  }
  
  // 관리자가 아닌데 NOTICE를 작성하려고 하면 막기
  if (category.value === 'NOTICE' && !isAdmin.value) {
    alert('공지사항은 관리자만 작성할 수 있습니다.')
    return
  }

  try {
    await boardStore.createPost({
      title: title.value,
      content: content.value,
      author: userStore.userInfo.nickname || userStore.userInfo.id,
      category: category.value,
    })
    router.push('/board')
  } catch (error) {
    alert('게시글 작성에 실패했습니다.')
  }
}

const cancel = () => {
  router.back()
}
</script>

<template>
  <div class="container">
    <h1>게시글 작성</h1>
    <form @submit.prevent="handleSubmit" class="card">
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
        <label class="form-label">카테고리</label>
        <select v-model="category" class="form-input" required>
          <option v-if="isAdmin" value="NOTICE">공지사항</option>
          <option value="QNA">질문글</option>
          <option value="REQUEST">Hotplace 추가 요청</option>
        </select>
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
        <button type="submit" class="btn btn-primary">등록</button>
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
