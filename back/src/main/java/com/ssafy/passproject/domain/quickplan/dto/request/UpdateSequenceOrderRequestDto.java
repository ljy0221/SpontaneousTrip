package com.ssafy.passproject.domain.quickplan.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSequenceOrderRequestDto {
	private List<PlanItemOrder> planItems;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PlanItemOrder {
		private Long planItemId;
		private int sequenceOrder;
	}
}
