package com.ssafy.passproject.domain.quickplan.service;

import java.util.List;

import com.ssafy.passproject.domain.quickplan.entity.QuickPlan;
import com.ssafy.passproject.domain.quickplan.entity.QuickPlan.planStatus;

public interface QuickPlanService {
	public void insertQuickPlan(QuickPlan q) throws Exception;

	public QuickPlan selectQuickPlan(Long planId) throws Exception;

	public List<QuickPlan> selectQuickPlans(Long userId) throws Exception;

	public List<QuickPlan> selectQuickPlansByStatus(Long userId, planStatus status) throws Exception;

	public void updateQuickPlan(QuickPlan q) throws Exception;

	public void updateQuickPlanStatus(Long planId, planStatus newStatus) throws Exception;

	public void deleteQuickPlan(Long quickPlanId) throws Exception;

	public QuickPlan selectQuickPlanWithItems(Long planId) throws Exception;

	public List<QuickPlan> selectQuickPlansWithItems(Long userId) throws Exception;

	public void updatePlanItemsOrder(Long planId, java.util.Map<Long, Integer> planItemOrders) throws Exception;
}
