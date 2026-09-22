package com.ssafy.passproject.domain.quickplan.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.ssafy.passproject.domain.planitem.dto.response.PlanItemResponseDto;
import com.ssafy.passproject.domain.quickplan.entity.QuickPlan.planStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuickPlanResponseDto {
    private Long planId;
    private Long userId;
    private int totalTimeInput;
    private String startLocation;
    private LocalDateTime createdAt;
    private planStatus status;
    private List<PlanItemResponseDto> planItems;
}