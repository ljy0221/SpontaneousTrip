package com.ssafy.passproject.domain.planitem.service;

import java.util.List;

import com.ssafy.passproject.domain.planitem.entity.PlanItem;

public interface PlanItemService {
	public void insertPlanItem(PlanItem q) throws Exception;

	public PlanItem selectPlanItem(Long planItemId) throws Exception;

	public List<PlanItem> selectPlanItems() throws Exception;

	public void updatePlanItem(PlanItem q) throws Exception;

	public void deletePlanItem(Long planItemId) throws Exception;
}
