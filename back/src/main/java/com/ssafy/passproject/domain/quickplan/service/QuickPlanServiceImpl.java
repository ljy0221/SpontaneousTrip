package com.ssafy.passproject.domain.quickplan.service;

import java.util.List;

import com.ssafy.passproject.domain.planitem.entity.PlanItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.passproject.domain.hotplace.repository.HotplaceRepository;
import com.ssafy.passproject.domain.planitem.repository.PlanItemRepository;
import com.ssafy.passproject.domain.quickplan.entity.QuickPlan;
import com.ssafy.passproject.domain.quickplan.entity.QuickPlan.planStatus;
import com.ssafy.passproject.domain.quickplan.repository.QuickPlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuickPlanServiceImpl implements QuickPlanService {

	private final QuickPlanRepository quickPlanRepository;
	private final PlanItemRepository planItemRepository;
	private final HotplaceRepository hotplaceRepository;

	@Override
	@Transactional
	public void insertQuickPlan(QuickPlan q) throws Exception {
		quickPlanRepository.insertQuickPlan(q);

		// PlanItem 저장 로직 추가
		if (q.getPlanItems() != null && !q.getPlanItems().isEmpty()) {
			for (PlanItem item : q.getPlanItems()) {
				item.setPlanId(q.getPlanId()); // 생성된 planId 설정
				planItemRepository.insertPlanItem(item);

				// Hotplace count 증가 (통계 수집)
				if (item.getPlaceId() != null) {
					hotplaceRepository.incrementHotplaceCount(item.getPlaceId());
				}
			}
		}
	}

	@Override
	public QuickPlan selectQuickPlan(Long planId) throws Exception {
		return quickPlanRepository.selectQuickPlan(planId);
	}

	@Override
	public QuickPlan selectQuickPlanWithItems(Long planId) throws Exception {
		return quickPlanRepository.selectQuickPlanWithItems(planId);
	}

	@Override
	public List<QuickPlan> selectQuickPlans(Long userId) throws Exception {
		return quickPlanRepository.selectQuickPlans(userId);
	}

	@Override
	public List<QuickPlan> selectQuickPlansByStatus(Long userId, planStatus status) throws Exception {
		return quickPlanRepository.selectQuickPlansByStatus(userId, status);
	}

	@Override
	public List<QuickPlan> selectQuickPlansWithItems(Long userId) throws Exception {
		return quickPlanRepository.selectQuickPlansWithItems(userId);
	}

	@Override
	@Transactional
	public void updateQuickPlan(QuickPlan q) throws Exception {
		quickPlanRepository.updateQuickPlan(q);
	}

	@Override
	@Transactional
	public void updateQuickPlanStatus(Long planId, planStatus newStatus) throws Exception {
		// 1. 현재 계획 조회
		QuickPlan currentPlan = quickPlanRepository.selectQuickPlan(planId);

		if (currentPlan == null) {
			throw new IllegalArgumentException("존재하지 않는 계획입니다. planId: " + planId);
		}

		planStatus currentStatus = currentPlan.getStatus();

		// 2. 상태 전이 검증: ONGOING ↔ DONE만 허용
		if (!isValidStatusTransition(currentStatus, newStatus)) {
			throw new IllegalStateException(
					String.format("잘못된 상태 전이입니다. %s에서 %s로 변경할 수 없습니다.",
							currentStatus, newStatus));
		}

		// 3. 상태 업데이트
		quickPlanRepository.updateQuickPlanStatus(planId, newStatus);
	}

	private boolean isValidStatusTransition(planStatus from,
			com.ssafy.passproject.domain.quickplan.entity.QuickPlan.planStatus to) {
		// ONGOING ↔ DONE만 허용
		if (from == com.ssafy.passproject.domain.quickplan.entity.QuickPlan.planStatus.ONGOING
				&& to == com.ssafy.passproject.domain.quickplan.entity.QuickPlan.planStatus.DONE)
			return true;
		if (from == com.ssafy.passproject.domain.quickplan.entity.QuickPlan.planStatus.DONE
				&& to == com.ssafy.passproject.domain.quickplan.entity.QuickPlan.planStatus.ONGOING)
			return true;

		// 같은 상태로의 전이는 허용 (idempotent)
		if (from == to)
			return true;

		// 그 외 모든 전이는 금지
		return false;
	}

	@Override
	@Transactional
	public void deleteQuickPlan(Long quickPlanId) throws Exception {
		// PlanItem 먼저 삭제 (외래키 제약조건)
		planItemRepository.deletePlanItemsByPlanId(quickPlanId);
		// QuickPlan 삭제
		quickPlanRepository.deleteQuickPlan(quickPlanId);
	}

	@Override
	@Transactional
	public void updatePlanItemsOrder(Long planId, java.util.Map<Long, Integer> planItemOrders) throws Exception {
		if (planItemOrders == null || planItemOrders.isEmpty()) {
			throw new IllegalArgumentException("planItemOrders cannot be null or empty");
		}

		// planId에 속한 planItem들인지 검증
		List<com.ssafy.passproject.domain.planitem.entity.PlanItem> existingItems = planItemRepository
				.selectPlanItemsByPlanId(planId);

		// 각 planItemId와 sequenceOrder로 업데이트
		for (java.util.Map.Entry<Long, Integer> entry : planItemOrders.entrySet()) {
			Long planItemId = entry.getKey();
			Integer sequenceOrder = entry.getValue();

			// 해당 planItemId가 planId에 속하는지 검증
			boolean isValid = existingItems.stream()
					.anyMatch(item -> item.getPlanItemId().equals(planItemId));

			if (!isValid) {
				throw new IllegalArgumentException(
						"PlanItem ID " + planItemId + " does not belong to Plan ID " + planId);
			}

			planItemRepository.updatePlanItemSequenceOrder(planItemId, sequenceOrder);
		}
	}
}