package com.ssafy.passproject.domain.quickplan.dto.request;

import com.ssafy.passproject.domain.quickplan.entity.QuickPlan.planStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuickPlanRequestDto {
    private int totalTimeInput;
    private String startLocation;

    @NotNull(message = "상태는 필수입니다")
    private planStatus status;
}