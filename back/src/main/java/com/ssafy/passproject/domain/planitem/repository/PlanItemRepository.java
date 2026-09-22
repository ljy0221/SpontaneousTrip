package com.ssafy.passproject.domain.planitem.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.passproject.domain.planitem.entity.PlanItem;


@Mapper
public interface PlanItemRepository {
	public void insertPlanItem(PlanItem q) throws Exception;

	public PlanItem selectPlanItem(Long planItemId) throws Exception;

	public List<PlanItem> selectPlanItems() throws Exception;

	public void updatePlanItem(PlanItem q) throws Exception;

	public void deletePlanItem(Long planItemId) throws Exception;
	
	public List<PlanItem> selectPlanItemsByPlanId(Long planId) throws Exception;

	public void deletePlanItemsByPlanId(Long planId) throws Exception;

	public void updatePlanItemSequenceOrder(Long planItemId, int sequenceOrder) throws Exception;
}
