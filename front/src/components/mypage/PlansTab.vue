<script setup>
import { useRouter } from 'vue-router'
import { usePlanStore } from '@/stores/plan'
import { statusToBackend } from '@/utils/statusMapper'
import { formatDate } from '@/utils/dateFormatter'

const router = useRouter()
const planStore = usePlanStore()

const props = defineProps({
  plans: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['delete-plan', 'view-plan', 'refresh-plans'])

const deletePlan = async (planId) => {
  if (!confirm('이 일정을 삭제하시겠습니까?')) {
    return
  }

  try {
    console.log('Requesting delete for plan:', planId)
    await planStore.deletePlan(planId)
    alert('여행 일정이 성공적으로 삭제되었습니다.')
    emit('delete-plan', planId)
  } catch (error) {
    console.error('Failed to delete plan:', error)
    alert('오류: 일정을 삭제하지 못했습니다. 다시 시도해주세요.')
  }
}

const viewPlanDetail = (planId) => {
  router.push(`/plan/${planId}`)
}

const changeToggle = async (plan) => {
  // 1. 이전 상태 저장 (롤백용)
  const previousStatus = plan.status;

  // 2. UI 먼저 업데이트 (낙관적) - 한글 상태로 토글
  if(plan.status === "진행중") {
    plan.status = "완료";
  } else {
    plan.status = "진행중"
  }

  // 3. API 호출 - 한글을 영어로 변환하여 전달
  try {
    const backendStatus = statusToBackend(plan.status);
    await planStore.updatePlanStatus(plan.planId, backendStatus);
    // 성공: 그대로 유지 (이미 UI 업데이트됨)
  } catch (error) {
    // 4. 실패: 롤백 + 에러 메시지
    plan.status = previousStatus;
    alert('상태 변경에 실패했습니다. 다시 시도해주세요.');
    console.error('Status toggle failed:', error);
  }
}
</script>

<template>
  <div class="plans-section">
    <div v-if="loading" class="loading">일정을 불러오는 중...</div>

    <div v-else-if="plans.length === 0" class="empty-state card">
      <p>저장된 여행 일정이 없습니다.</p>
      <button @click="router.push('/plan')" class="btn btn-primary">새 일정 만들기</button>
    </div>

    <div v-else class="plans-grid">
      <div
        v-for="plan in plans"
        :key="plan.planId"
        class="plan-card card"
        @click="viewPlanDetail(plan.planId)"
        style="cursor: pointer;"
      >
        <div class="plan-header">
          <h3>여행 일정 #{{ plan.planId }}</h3>
          <span
            class="plan-status"
            :class="plan.status"
            @click.stop="changeToggle(plan)"
            style="cursor: pointer;"
          >
            {{ plan.status }}
          </span>
        </div>

        <div class="plan-info">
          <div class="info-item">
            <span class="label">📍 출발지:</span>
            <span class="value">{{ plan.startLocation || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">⏱️ 소요 시간:</span>
            <span class="value">{{ plan.totalTimeInput }}분</span>
          </div>
          <div class="info-item">
            <span class="label">📅 생성일:</span>
            <span class="value">{{ formatDate(plan.createdAt) }}</span>
          </div>
          <div class="info-item">
            <span class="label">📌 장소 수:</span>
            <span class="value">{{ plan.planItems?.length || 0 }}개</span>
          </div>
        </div>

        <div
          v-if="plan.planItems && plan.planItems.length > 0"
          class="plan-items"
        >
          <h4>일정 상세</h4>
          <div
            v-for="(item, index) in plan.planItems"
            :key="item.planItemId"
            class="plan-item"
          >
            <span class="item-number">{{ index + 1 }}</span>
            <div class="item-details">
              <div class="item-title">{{ item.title || `장소 #${item.placeId}` }}</div>
              <div class="item-meta">
                <span>🚌 {{ item.transportMode }}</span>
                <span>⏱️ {{ item.estimatedDuration }}분</span>
              </div>
            </div>
          </div>
        </div>

        <div class="plan-actions" @click.stop>
          <button @click="deletePlan(plan.planId)" class="btn btn-danger btn-sm">삭제</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.loading {
	text-align: center;
	padding: 3rem;
	color: var(--color-text-muted);
	font-size: var(--font-size-lg);
}

/* Plans Section */
.plans-section {
	max-width: 1200px;
	margin: 0 auto;
}

.plans-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
	gap: 2rem;
	margin-top: 2rem;
}

.plan-card {
	background-color: var(--color-surface);
	border: 2px solid var(--color-border-light);
	border-radius: var(--radius-xl);
	padding: 2rem;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	position: relative;
	overflow: hidden;

	&::before {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 4px;
		background: linear-gradient(90deg, var(--color-cyan) 0%, var(--color-teal) 100%);
		transform: scaleX(0);
		transform-origin: left;
		transition: transform 0.3s ease;
	}

	&:hover {
		transform: translateY(-8px);
		box-shadow: var(--shadow-card-hover);
		border-color: var(--color-cyan);

		&::before {
			transform: scaleX(1);
		}
	}
}

.plan-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 1.5rem;
	padding-bottom: 1rem;
	border-bottom: 2px solid var(--color-border-light);

	h3 {
		font-size: var(--font-size-xl);
		font-weight: 700;
		color: var(--color-text-main);
		margin: 0;
		letter-spacing: -0.025em;
	}
}

.plan-status {
	padding: 0.4rem 0.875rem;
	border-radius: var(--radius-full);
	font-size: var(--font-size-xs);
	font-weight: 700;
	text-transform: uppercase;
	letter-spacing: 0.05em;
	background: linear-gradient(135deg, var(--color-cyan-light) 0%, #E0F9FC 100%);
	color: var(--color-cyan);
	transition: all 0.3s ease;

	&:hover {
		background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
		color: white;
		transform: scale(1.05);
	}
}

.plan-info {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 1rem;
	margin-bottom: 1.5rem;
}

.info-item {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;

	.label {
		font-size: var(--font-size-xs);
		font-weight: 600;
		color: var(--color-text-muted);
		text-transform: uppercase;
		letter-spacing: 0.05em;
	}

	.value {
		font-size: var(--font-size-base);
		font-weight: 600;
		color: var(--color-text-main);
	}
}

.plan-items {
	margin-top: 1.5rem;
	padding-top: 1.5rem;
	border-top: 2px solid var(--color-border-light);

	h4 {
		font-size: var(--font-size-base);
		font-weight: 700;
		color: var(--color-text-main);
		margin: 0 0 1rem 0;
		text-transform: uppercase;
		letter-spacing: 0.05em;
	}
}

.plan-item {
	display: flex;
	gap: 1rem;
	padding: 0.875rem;
	margin-bottom: 0.75rem;
	background-color: var(--color-background-alt);
	border-radius: var(--radius-lg);
	border: 1px solid var(--color-border-light);
	transition: all 0.2s ease;

	&:hover {
		background-color: var(--color-surface);
		border-color: var(--color-cyan);
		transform: translateX(4px);
	}

	&:last-child {
		margin-bottom: 0;
	}
}

.item-number {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 32px;
	height: 32px;
	flex-shrink: 0;
	background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
	color: white;
	border-radius: 50%;
	font-weight: 700;
	font-size: var(--font-size-sm);
}

.item-details {
	flex: 1;
}

.item-title {
	font-weight: 600;
	color: var(--color-text-main);
	margin-bottom: 0.5rem;
	font-size: var(--font-size-sm);
}

.item-meta {
	display: flex;
	gap: 1rem;
	font-size: var(--font-size-xs);
	color: var(--color-text-muted);
	font-weight: 500;

	span {
		display: flex;
		align-items: center;
		gap: 0.25rem;
	}
}

.plan-actions {
	margin-top: 1.5rem;
	padding-top: 1.5rem;
	border-top: 1px solid var(--color-border-light);
	display: flex;
	justify-content: flex-end;

	.btn-sm {
		padding: 0.5rem 1.25rem;
		font-size: var(--font-size-sm);
		font-weight: 600;
		border-radius: var(--radius-lg);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

		&:hover {
			transform: translateY(-2px);
			box-shadow: var(--shadow-md);
		}
	}
}

/* Empty State */
.empty-state {
	text-align: center;
	padding: 4rem 2rem;
	background-color: var(--color-background-alt);
	border-radius: var(--radius-xl);
	border: 2px dashed var(--color-border);

	p {
		color: var(--color-text-muted);
		font-size: var(--font-size-lg);
		margin: 0 0 2rem 0;
		font-weight: 500;
	}

	.btn {
		padding: 1rem 2.5rem;
		font-weight: 700;
		background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
		color: white;
		border: none;
		border-radius: var(--radius-xl);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
		box-shadow: var(--shadow-md);
		font-size: var(--font-size-base);

		&:hover {
			transform: translateY(-3px);
			box-shadow: var(--shadow-card-hover);
		}
	}
}

@media (max-width: 768px) {
	.plans-grid {
		grid-template-columns: 1fr;
		gap: 1.5rem;
	}

	.plan-card {
		padding: 1.5rem;
	}

	.plan-info {
		grid-template-columns: 1fr;
		gap: 0.75rem;
	}

	.plan-header {
		flex-direction: column;
		align-items: flex-start;
		gap: 0.75rem;
	}

	.plan-item {
		flex-direction: column;
		align-items: flex-start;
	}

	.item-number {
		align-self: flex-start;
	}
}
</style>
