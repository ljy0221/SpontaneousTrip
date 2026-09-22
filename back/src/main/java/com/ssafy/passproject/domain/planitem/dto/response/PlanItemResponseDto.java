package com.ssafy.passproject.domain.planitem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanItemResponseDto {
    private Long planItemId;
    private Long planId;
    private Long placeId;
    private int sequenceOrder;
    private String transportMode;
    private int estimatedDuration;
}