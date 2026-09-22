<script setup>
import { ref } from 'vue'

const activeTab = ref('users')

const users = ref([
  { id: 1, username: 'user1', email: 'user1@example.com', role: 'User' },
  { id: 2, username: 'user2', email: 'user2@example.com', role: 'User' },
  { id: 3, username: 'admin', email: 'admin@example.com', role: 'Admin' },
])

const boards = ref([
  { id: 1, title: 'Welcome', author: 'admin', date: '2024-05-01' },
  { id: 2, title: 'Vue 3', author: 'user1', date: '2024-05-02' },
])
</script>

<template>
  <div class="container">
    <h1>관리자 대시보드</h1>
    
    <div class="tabs">
      <button 
        :class="['tab-btn', { active: activeTab === 'users' }]"
        @click="activeTab = 'users'"
      >
        사용자 관리
      </button>
      <button 
        :class="['tab-btn', { active: activeTab === 'boards' }]"
        @click="activeTab = 'boards'"
      >
        게시판 관리
      </button>
    </div>

    <div v-if="activeTab === 'users'" class="tab-content">
      <h2>사용자 목록</h2>
      <table class="admin-table">
        <thead>
          <tr>
            <th>아이디</th>
            <th>사용자명</th>
            <th>이메일</th>
            <th>역할</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.role }}</td>
            <td>
              <button class="btn btn-secondary btn-sm">편집</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="activeTab === 'boards'" class="tab-content">
      <h2>게시글 목록</h2>
      <table class="admin-table">
        <thead>
          <tr>
            <th>아이디</th>
            <th>제목</th>
            <th>작성자</th>
            <th>날짜</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="board in boards" :key="board.id">
            <td>{{ board.id }}</td>
            <td>{{ board.title }}</td>
            <td>{{ board.author }}</td>
            <td>{{ board.date }}</td>
            <td>
              <button class="btn btn-danger btn-sm">삭제</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style lang="scss" scoped>
h1 {
	font-size: var(--font-size-3xl);
	font-weight: 700;
	color: var(--color-text-main);
	letter-spacing: -0.025em;
	margin-bottom: 2.5rem;
	text-align: center;
}

.tabs {
	display: flex;
	gap: 0;
	margin-bottom: 3rem;
	border-bottom: 2px solid var(--color-border-light);
	background-color: var(--color-surface);
	border-radius: var(--radius-xl) var(--radius-xl) 0 0;
	overflow: hidden;
	box-shadow: var(--shadow-sm);
}

.tab-btn {
	flex: 1;
	padding: 1.25rem 2rem;
	background: none;
	border: none;
	font-size: var(--font-size-base);
	font-weight: 600;
	color: var(--color-text-muted);
	cursor: pointer;
	position: relative;
	transition: all 0.3s ease;
	border-bottom: 3px solid transparent;

	&:hover:not(.active) {
		background-color: var(--color-background-alt);
		color: var(--color-text-main);
	}

	&.active {
		color: var(--color-cyan);
		background-color: var(--color-cyan-light);
		border-bottom-color: var(--color-cyan);
	}
}

.tab-content {
	h2 {
		font-size: var(--font-size-2xl);
		font-weight: 700;
		color: var(--color-text-main);
		margin-bottom: 2rem;
		padding-bottom: 1rem;
		border-bottom: 2px solid var(--color-border-light);
	}
}

.admin-table {
	width: 100%;
	border-collapse: collapse;
	margin-top: 1.5rem;
	background-color: var(--color-surface);
	border-radius: var(--radius-xl);
	overflow: hidden;
	box-shadow: var(--shadow-card);

	th,
	td {
		padding: 1.25rem 1.5rem;
		text-align: left;
		border-bottom: 1px solid var(--color-border-light);
	}

	th {
		background-color: var(--color-background-alt);
		font-weight: 700;
		font-size: var(--font-size-sm);
		color: var(--color-text-muted);
		text-transform: uppercase;
		letter-spacing: 0.05em;
	}

	tbody tr {
		transition: background-color 0.2s ease;

		&:hover {
			background-color: var(--color-background-alt);
		}

		&:last-child td {
			border-bottom: none;
		}
	}

	td {
		font-size: var(--font-size-base);
		color: var(--color-text-main);
	}
}

.btn-sm {
	padding: 0.5rem 1rem;
	font-size: var(--font-size-sm);
	font-weight: 600;
	border-radius: var(--radius-md);
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

	&.btn-secondary {
		background-color: var(--color-surface);
		color: var(--color-cyan);
		border: 2px solid var(--color-cyan);

		&:hover {
			background-color: var(--color-cyan-light);
			transform: translateY(-2px);
			box-shadow: var(--shadow-sm);
		}
	}

	&.btn-danger {
		background-color: var(--color-danger);
		color: white;

		&:hover {
			background-color: #DC2626;
			transform: translateY(-2px);
			box-shadow: var(--shadow-sm);
		}
	}
}

@media (max-width: 768px) {
	.tabs {
		flex-direction: column;
	}

	.tab-btn {
		border-bottom: none;
		border-left: 3px solid transparent;

		&.active {
			border-left-color: var(--color-cyan);
		}
	}

	.admin-table {
		font-size: var(--font-size-sm);

		th,
		td {
			padding: 0.875rem 1rem;
		}
	}
}
</style>
