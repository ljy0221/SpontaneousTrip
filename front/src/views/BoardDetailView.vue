<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBoardStore } from '@/stores/board'

const route = useRoute()
const router = useRouter()
const boardStore = useBoardStore()

const post = ref(null)

onMounted(async () => {
  const id = route.params.id
  await boardStore.getPostById(id)
  post.value = boardStore.currentPost
  
  if (!post.value) {
    alert('게시글을 찾을 수 없습니다!')
    router.push('/board')
  }
})

const goBack = () => {
  router.push('/board')
}

const deletePost = async () => {
  if (confirm('이 게시글을 삭제하시겠습니까?')) {
    try {
      await boardStore.deletePost(post.value.id)
      router.push('/board')
    } catch (error) {
      alert('게시글 삭제에 실패했습니다.')
    }
  }
}
</script>

<template>
  <div class="container" v-if="post">
    <div class="post-header">
      <h1 class="post-title">{{ post.title }}</h1>
      <div class="post-meta">
        <span>작성자: <strong>{{ post.author }}</strong></span>
        <span class="divider">|</span>
        <span>{{ post.date }}</span>
        <span class="divider">|</span>
        <span>조회수: {{ post.views }}</span>
      </div>
    </div>

    <div class="card post-content">
      {{ post.content }}
    </div>

    <div class="actions">
      <button @click="goBack" class="btn btn-secondary">목록</button>
      <div class="right-actions">
        <button @click="router.push(`/board/update/${post.id}`)" class="btn btn-secondary">편집</button>
        <button @click="deletePost" class="btn btn-danger">삭제</button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.post-header {
	margin-bottom: 2.5rem;
	padding-bottom: 2rem;
	border-bottom: 2px solid var(--color-border-light);
}

.post-title {
	font-size: var(--font-size-3xl);
	font-weight: 700;
	color: var(--color-text-main);
	letter-spacing: -0.025em;
	margin-bottom: 1rem;
	line-height: 1.3;
}

.post-meta {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	color: var(--color-text-muted);
	font-size: var(--font-size-sm);
	font-weight: 500;

	strong {
		color: var(--color-cyan);
		font-weight: 600;
	}
}

.divider {
	margin: 0 0.5rem;
	color: var(--color-border);
}

.post-content {
	min-height: 300px;
	margin-bottom: 2.5rem;
	line-height: 1.8;
	padding: 2rem;
	background-color: var(--color-surface);
	border-radius: var(--radius-xl);
	box-shadow: var(--shadow-card);
	border: 1px solid var(--color-border-light);
	white-space: pre-wrap;
	word-wrap: break-word;
	font-size: var(--font-size-base);
	color: var(--color-text-main);
}

.actions {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 1rem;
	padding-top: 1.5rem;
	border-top: 1px solid var(--color-border-light);

	.btn {
		padding: 0.75rem 1.5rem;
		font-weight: 600;
		border-radius: var(--radius-lg);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

		&:hover {
			transform: translateY(-2px);
			box-shadow: var(--shadow-md);
		}
	}

	.btn-secondary {
		background-color: var(--color-surface);
		color: var(--color-text-main);
		border: 2px solid var(--color-border);

		&:hover {
			background-color: var(--color-cyan-light);
			border-color: var(--color-cyan);
			color: var(--color-cyan);
		}
	}

	.btn-danger {
		background-color: var(--color-danger);
		color: white;

		&:hover {
			background-color: #DC2626;
		}
	}
}

.right-actions {
	display: flex;
	gap: 0.75rem;
}

@media (max-width: 768px) {
	.post-title {
		font-size: var(--font-size-2xl);
	}

	.post-content {
		padding: 1.5rem;
	}

	.actions {
		flex-direction: column;

		.btn {
			width: 100%;
		}

		.right-actions {
			width: 100%;
			flex-direction: column;
		}
	}
}
</style>
