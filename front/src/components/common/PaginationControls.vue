<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentPage: {
    type: Number,
    required: true,
    validator: (value) => value >= 0
  },
  totalPages: {
    type: Number,
    required: true,
    validator: (value) => value > 0
  },
  hasNext: {
    type: Boolean,
    default: false
  },
  hasPrevious: {
    type: Boolean,
    default: false
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  maxVisiblePages: {
    type: Number,
    default: 5,
    validator: (value) => value > 0 && value % 2 === 1
  }
})

const emit = defineEmits(['update:page', 'next', 'previous'])

const visiblePages = computed(() => {
  const total = props.totalPages
  const current = props.currentPage
  const maxVisible = props.maxVisiblePages

  let start = Math.max(0, current - Math.floor(maxVisible / 2))
  let end = Math.min(total, start + maxVisible)

  if (end - start < maxVisible) {
    start = Math.max(0, end - maxVisible)
  }

  const pages = []
  for (let i = start; i < end; i++) {
    pages.push(i)
  }
  return pages
})
</script>

<template>
  <div class="pagination-controls">
    <div class="pagination-buttons">
      <!-- Previous Button -->
      <button
        @click="emit('previous')"
        class="btn btn-nav btn-sm"
        :disabled="!hasPrevious || isLoading"
        aria-label="이전 페이지"
      >
        &lt;
      </button>

      <!-- Page Number Buttons -->
      <button
        v-for="page in visiblePages"
        :key="page"
        @click="emit('update:page', page)"
        class="btn btn-sm"
        :class="page === currentPage ? 'btn-primary' : 'btn-outline'"
        :disabled="isLoading"
        :aria-label="`${page + 1} 페이지로 이동`"
        :aria-current="page === currentPage ? 'page' : undefined"
      >
        {{ page + 1 }}
      </button>

      <!-- Next Button -->
      <button
        @click="emit('next')"
        class="btn btn-nav btn-sm"
        :disabled="!hasNext || isLoading"
        aria-label="다음 페이지"
      >
        &gt;
      </button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.pagination-buttons {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn {
  min-width: 2.5rem;
  height: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text-main);

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: var(--shadow-sm);
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.btn-nav {
  border-width: 2px;
  font-weight: 700;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
  color: white;
  border-color: var(--color-cyan);

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, var(--color-cyan-hover) 0%, var(--color-teal) 100%);
  }
}

.btn-outline {
  background: var(--color-surface);
  color: var(--color-text-main);
  border-color: var(--color-border);

  &:hover:not(:disabled) {
    background: var(--color-cyan-light);
    border-color: var(--color-cyan);
    color: var(--color-cyan);
  }
}

@media (max-width: 768px) {
  .pagination-controls {
    margin-top: 1.5rem;
  }

  .btn {
    min-width: 2rem;
    height: 2rem;
    font-size: var(--font-size-xs);
  }
}
</style>
