package com.ssafy.passproject.domain.quickplan.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.ssafy.passproject.domain.planitem.entity.PlanItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuickPlan {
	private Long planId;
	private Long userId;
	private int totalTimeInput;
	private String startLocation;
	private LocalDateTime createdAt;
	private planStatus status;
    private int count;

	private List<PlanItem> planItems;

	public enum planStatus {
		TEMP,
		ONGOING,
		DONE,
	}
}
