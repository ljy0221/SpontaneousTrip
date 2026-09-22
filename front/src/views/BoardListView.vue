<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useBoardStore } from '@/stores/board'

const boardStore = useBoardStore()
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const selectedCategory = ref('ALL') // ALL, NOTICE, QNA, REQUEST

onMounted(() => {
  loadPosts()
})

// Watch for search query changes and reset to page 1
watch(searchQuery, () => {
  currentPage.value = 1
  loadPosts()
})

// Watch for category changes and reset to page 1
watch(selectedCategory, () => {
  currentPage.value = 1
  loadPosts()
})

const loadPosts = () => {
  const condition = {
    page: currentPage.value,
    size: pageSize.value,
    word: searchQuery.value || undefined,  // Backend expects 'word', not 'keyword'
    category: selectedCategory.value !== 'ALL' ? selectedCategory.value : undefined
  }
  boardStore.getPosts(condition)
}

const filteredPosts = computed(() => {
  return boardStore.posts
})

// Pagination computed properties
const totalPages = computed(() => boardStore.pageInfo.totalPages)
const hasPrevious = computed(() => boardStore.pageInfo.hasPrevious)
const hasNext = computed(() => boardStore.pageInfo.hasNext)

// Generate page numbers to display
const pageNumbers = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const pages = []
  
  // Show max 5 page numbers at a time
  let startPage = Math.max(1, current - 2)
  let endPage = Math.min(total, startPage + 4)
  
  // Adjust start if we're near the end
  if (endPage - startPage < 4) {
    startPage = Math.max(1, endPage - 4)
  }
  
  for (let i = startPage; i <= endPage; i++) {
    pages.push(i)
  }
  
  return pages
})

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadPosts()
  }
}

const previousPage = () => {
  if (hasPrevious.value) {
    goToPage(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (hasNext.value) {
    goToPage(currentPage.value + 1)
  }
}

const getCategoryLabel = (category) => {
  const labels = {
    'NOTICE': '공지사항',
    'QNA': '질문글',
    'REQUEST': 'Hotplace 요청'
  }
  return labels[category] || category
}
</script>

<template>
  <div class="container">
    <div class="header-actions">
      <h1>게시글 목록</h1>
      <router-link to="/board/write" class="btn btn-primary">게시글 작성</router-link>
    </div>

    <div class="filter-section">
      <div class="category-filters">
        <button 
          class="filter-btn" 
          :class="{ active: selectedCategory === 'ALL' }"
          @click="selectedCategory = 'ALL'"
        >
          전체
        </button>
        <button 
          class="filter-btn category-notice" 
          :class="{ active: selectedCategory === 'NOTICE' }"
          @click="selectedCategory = 'NOTICE'"
        >
          공지사항
        </button>
        <button 
          class="filter-btn category-qna" 
          :class="{ active: selectedCategory === 'QNA' }"
          @click="selectedCategory = 'QNA'"
        >
          질문글
        </button>
        <button 
          class="filter-btn category-request" 
          :class="{ active: selectedCategory === 'REQUEST' }"
          @click="selectedCategory = 'REQUEST'"
        >
          Hotplace 요청
        </button>
      </div>
    </div>

    <div class="search-bar">
      <input 
        v-model="searchQuery" 
        type="text" 
        class="form-input" 
        placeholder="제목으로 검색..." 
      />
    </div>

    <div class="table-container">
      <table class="board-table">
        <thead>
          <tr>
            <th class="th-no">아이디</th>
            <th class="th-category">카테고리</th>
            <th class="th-title">제목</th>
            <th class="th-author">작성자</th>
            <th class="th-date">날짜</th>
            <th class="th-views">조회수</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="post in filteredPosts" :key="post.id">
            <td>{{ post.id }}</td>
            <td>
              <span class="category-badge" :class="`category-${post.category?.toLowerCase()}`">
                {{ getCategoryLabel(post.category) }}
              </span>
            </td>
            <td class="td-title">
              <router-link :to="`/board/${post.id}`">{{ post.title }}</router-link>
            </td>
            <td>{{ post.author }}</td>
            <td>{{ post.date }}</td>
            <td>{{ post.views }}</td>
          </tr>
          <tr v-if="filteredPosts.length === 0">
            <td colspan="6" class="no-data">게시글이 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="totalPages > 0">
      <button 
        class="btn btn-secondary btn-sm" 
        :disabled="!hasPrevious"
        @click="previousPage"
      >
        &lt;
      </button>
      <button 
        v-for="page in pageNumbers" 
        :key="page"
        class="btn btn-sm"
        :class="page === currentPage ? 'btn-primary' : 'btn-secondary'"
        @click="goToPage(page)"
      >
        {{ page }}
      </button>
      <button 
        class="btn btn-secondary btn-sm"
        :disabled="!hasNext"
        @click="nextPage"
      >
        &gt;
      </button>
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
  
  @media (min-width: 768px) {
    font-size: var(--font-size-3xl);
  }
}

.header-actions {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid var(--color-border-light);
  
  @media (min-width: 768px) {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
  }
}

.filter-section {
	margin-bottom: 1.5rem;
}

.category-filters {
	display: flex;
	gap: 0.75rem;
	flex-wrap: wrap;
}

.filter-btn {
  padding: 0.625rem 1rem;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  background-color: var(--color-surface);
  color: var(--color-text-main);
  font-size: var(--font-size-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  min-height: 44px;
  
  @media (min-width: 768px) {
    padding: 0.5rem 1.25rem;
  }

  &:hover {
    border-color: var(--color-cyan);
    color: var(--color-cyan);
    transform: translateY(-1px);
  }

  &.active {
    background-color: var(--color-cyan);
    border-color: var(--color-cyan);
    color: white;
    box-shadow: var(--shadow-sm);
  }

  &.category-notice.active {
    background-color: #1976d2;
    border-color: #1976d2;
  }

  &.category-qna.active {
    background-color: #f57c00;
    border-color: #f57c00;
  }

  &.category-request.active {
    background-color: #7b1fa2;
    border-color: #7b1fa2;
  }
}

.search-bar {
  margin-bottom: 1.5rem;
  max-width: 100%;

  .form-input {
    padding-left: 2.5rem;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%236B7280'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z'%3E%3C/path%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: 0.75rem center;
    background-size: 1.25rem;
  }
  
  @media (min-width: 768px) {
    margin-bottom: 2rem;
    max-width: 400px;
  }
}

.table-container {
	background-color: var(--color-surface);
	border-radius: var(--radius-xl);
	box-shadow: var(--shadow-card);
	overflow: hidden;
	border: 1px solid var(--color-border-light);
}

.board-table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--font-size-sm);

  th,
  td {
    padding: 0.875rem 0.5rem;
    text-align: center;
    border-bottom: 1px solid var(--color-border-light);
  }

  th {
    background-color: var(--color-background-alt);
    font-weight: 700;
    font-size: 0.75rem;
    color: var(--color-text-muted);
    text-transform: uppercase;
    letter-spacing: 0.05em;
    white-space: nowrap;
  }
  
  @media (min-width: 768px) {
    font-size: var(--font-size-base);
    
    th,
    td {
      padding: 1.125rem 1.5rem;
    }
    
    th {
      font-size: var(--font-size-sm);
    }
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
    color: var(--color-text-main);
  }
}

// Hide columns on mobile
.th-no {
  width: 60px;
  
  @media (min-width: 768px) {
    width: 80px;
  }
}

.th-category {
  width: 100px;
  
  @media (min-width: 768px) {
    width: 120px;
  }
}

.th-title {
  width: auto;
}

.th-author,
.th-views {
  display: none;
  
  @media (min-width: 768px) {
    display: table-cell;
  }
}

.th-author {
  @media (min-width: 768px) {
    width: 140px;
  }
}

.th-date {
  width: 80px;
  
  @media (min-width: 768px) {
    width: 140px;
  }
}

.th-views {
  @media (min-width: 768px) {
    width: 90px;
  }
}

.category-badge {
	display: inline-block;
	padding: 0.25rem 0.75rem;
	border-radius: 12px;
	font-size: 0.75rem;
	font-weight: 600;
	white-space: nowrap;
}

.category-notice {
	background-color: #e3f2fd;
	color: #1976d2;
}

.category-qna {
	background-color: #fff3e0;
	color: #f57c00;
}

.category-request {
	background-color: #f3e5f5;
	color: #7b1fa2;
}

.td-title {
	text-align: left;
	padding-left: 1.5rem;

	a {
		font-weight: 600;
		color: var(--color-text-main);
		transition: color 0.2s ease;

		&:hover {
			color: var(--color-cyan);
			text-decoration: none;
		}
	}
}

.no-data {
	padding: 3rem;
	color: var(--color-text-muted);
	text-align: center;
	font-size: var(--font-size-lg);
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 0.375rem;
  margin-top: 1.5rem;
  flex-wrap: wrap;
  
  @media (min-width: 768px) {
    gap: 0.5rem;
    margin-top: 2rem;
  }

  .btn {
    min-width: 2.25rem;
    height: 2.25rem;
    padding: 0;
    border-radius: var(--radius-md);
    
    @media (min-width: 768px) {
      min-width: 2.5rem;
      height: 2.5rem;
    }
  }

  .btn-primary {
    background-color: var(--color-cyan);
    border-color: var(--color-cyan);

    &:hover {
      background-color: var(--color-cyan-hover);
    }
  }

  .btn-secondary {
    background-color: var(--color-surface);
    border-color: var(--color-border);
    color: var(--color-text-main);

    &:hover:not(:disabled) {
      background-color: var(--color-cyan-light);
      border-color: var(--color-cyan);
      color: var(--color-cyan);
    }
  }
}
</style>
