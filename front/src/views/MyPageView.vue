<script setup>
import { ref, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { usePlanStore } from '@/stores/plan'
import { useRouter, useRoute } from 'vue-router'
import ProfileTab from '@/components/mypage/ProfileTab.vue'
import PlansTab from '@/components/mypage/PlansTab.vue'

const authStore = useAuthStore()
const userStore = useUserStore()
const planStore = usePlanStore()
const router = useRouter()
const route = useRoute()

const user = ref(null)
const plans = ref([])
const loading = ref(false)

// Tab state (기본값: 'plans')
const activeTab = ref('plans')

// URL 쿼리 파라미터 동기화
watch(activeTab, (newTab) => {
  router.replace({ query: { tab: newTab } })
})

onMounted(async () => {
  // URL 쿼리 파라미터에서 탭 읽기
  const tab = route.query.tab
  if (tab === 'plans' || tab === 'profile') {
    activeTab.value = tab
  }

  await fetchUserInfo()
  await fetchUserPlans()
})

const fetchUserInfo = async () => {
  try {
    const data = await userStore.getUserInfo()
    user.value = data
  } catch (error) {
    console.error('Failed to fetch user info:', error)
    alert('사용자 정보를 불러오는데 실패했습니다. 다시 로그인해주세요.')
    router.push('/login')
  }
}

const fetchUserPlans = async () => {
  if (!user.value) return

  loading.value = true
  try {
    const userId = user.value.userId || user.value.id
    if (!userId) {
      console.error('User ID is missing')
      return
    }
    const response = await planStore.getUserPlans(userId)
    plans.value = response.plans || []
    console.log('Loaded plans:', plans.value)
  } catch (error) {
    console.error('Failed to fetch user plans:', error)
    plans.value = []
  } finally {
    loading.value = false
  }
}

const deletePlan = async (planId) => {
  // PlansTab에서 emit으로 받을 수도 있지만, 현재는 PlansTab 내부에서 처리
  await fetchUserPlans()
}

const viewPlanDetail = (planId) => {
  router.push(`/plan/${planId}`)
}
</script>

<template>
  <div class="container">
    <h1>마이페이지</h1>

    <!-- 탭 버튼 (상단) -->
    <div class="tabs">
      <button
        :class="['tab-btn', { active: activeTab === 'profile' }]"
        @click="activeTab = 'profile'"
      >
        내 정보
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'plans' }]"
        @click="activeTab = 'plans'"
      >
        내 여행 일정
      </button>
    </div>

    <!-- 탭 컨텐츠 -->
    <div class="tab-content">
      <ProfileTab
        v-if="activeTab === 'profile'"
        :user="user"
        :auth-store="authStore"
        :user-store="userStore"
        @refresh-user="fetchUserInfo"
      />

      <PlansTab
        v-if="activeTab === 'plans'"
        :plans="plans"
        :loading="loading"
        @delete-plan="deletePlan"
        @view-plan="viewPlanDetail"
      />
    </div>

    <div v-if="!user" class="loading">
      로딩 중...
    </div>
  </div>
</template>

<style lang="scss" scoped>
h1 {
	font-size: var(--font-size-3xl);
	font-weight: 700;
	color: var(--color-text-main);
	letter-spacing: -0.025em;
	margin-bottom: var(--spacing-xl);
	text-align: center;
}

.loading {
	text-align: center;
	padding: 3rem;
	color: var(--color-text-muted);
	font-size: var(--font-size-lg);
}

/* Tab Styles (from AdminView) */
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
	animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
	from {
		opacity: 0;
		transform: translateY(10px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

@media (max-width: 768px) {
	.tabs {
		flex-direction: column;
		border-radius: var(--radius-lg);
		border-bottom: none;
	}

	.tab-btn {
		border-bottom: none;
		border-left: 3px solid transparent;
		text-align: left;

		&.active {
			border-bottom-color: transparent;
			border-left-color: var(--color-cyan);
		}
	}
}
</style>
