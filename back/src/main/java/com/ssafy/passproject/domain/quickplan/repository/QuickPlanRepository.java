package com.ssafy.passproject.domain.quickplan.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.passproject.domain.quickplan.entity.QuickPlan;
import com.ssafy.passproject.domain.quickplan.entity.QuickPlan.planStatus;

@Mapper
public interface QuickPlanRepository {
	public void insertQuickPlan(QuickPlan q) throws Exception;

	public QuickPlan selectQuickPlan(Long planId) throws Exception;

	public List<QuickPlan> selectQuickPlans(Long userId) throws Exception;

	public List<QuickPlan> selectQuickPlansByStatus(@Param("userId") Long userId, @Param("status") planStatus status) throws Exception;

	public void updateQuickPlan(QuickPlan q) throws Exception;

	public void updateQuickPlanStatus(@Param("planId") Long planId, @Param("status") planStatus status) throws Exception;

	public void deleteQuickPlan(Long quickPlanId) throws Exception;

	public QuickPlan selectQuickPlanWithItems(Long planId) throws Exception;

	public List<QuickPlan> selectQuickPlansWithItems(Long userId) throws Exception;
}
