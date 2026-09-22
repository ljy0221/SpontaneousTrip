package com.ssafy.passproject.domain.quickplan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.ssafy.passproject.domain.quickplan.dto.request.UpdateSequenceOrderRequestDto;
import com.ssafy.passproject.domain.quickplan.dto.request.UpdateStatusRequest;
import com.ssafy.passproject.domain.quickplan.entity.QuickPlan;
import com.ssafy.passproject.domain.quickplan.entity.QuickPlan.planStatus;
import com.ssafy.passproject.domain.quickplan.service.QuickPlanService;

@Tag(name = "QuickPlan", description = "즉흥 여행 계획 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/quickplan")
@RestController
public class QuickPlanController {

    private final QuickPlanService quickPlanService;

    @Operation(summary = "여행 계획 생성", description = "새로운 즉흥 여행 계획을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "계획 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"계획이 성공적으로 생성되었습니다.\", \"plan\": {\"planId\": 1, \"title\": \"즉흥 여행\"}}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"계획 생성 중 오류가 발생했습니다.\"}"
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> insertQuickPlan(
            @Parameter(description = "생성할 여행 계획 정보", required = true)
            @RequestBody QuickPlan plan) {
        try {
            quickPlanService.insertQuickPlan(plan);
            Map<String, Object> response = Map.of(
                    "message", "계획이 성공적으로 생성되었습니다.",
                    "plan", plan
            );

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("insertQuickPlan error", e);
            Map<String, Object> response = Map.of(
                    "message", "계획 생성 중 오류가 발생했습니다."
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "여행 계획 단건 조회", description = "특정 ID의 여행 계획을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"계획이 성공적으로 조회되었습니다.\", \"plan\": {\"planId\": 1, \"title\": \"즉흥 여행\"}}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"계획 조회 중 오류가 발생했습니다.\"}"
                            )
                    )
            )
    })
    @GetMapping("/{planId}")
    public ResponseEntity<Map<String, Object>> selectQuickPlan(
            @Parameter(description = "조회할 계획 ID", required = true, example = "1")
            @PathVariable Long planId) {
        try {
            QuickPlan plan = quickPlanService.selectQuickPlan(planId);
            Map<String, Object> response = Map.of(
                    "message", "계획이 성공적으로 조회되었습니다.",
                    "plan", plan
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("selectQuickPlan error", e);
            Map<String, Object> response = Map.of(
                    "message", "계획 조회 중 오류가 발생했습니다."
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@Operation(summary = "여행 계획 상세 조회 (PlanItem 포함)", description = "특정 ID의 여행 계획과 연관된 세부 일정을 함께 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"계획이 성공적으로 조회되었습니다.\", \"plan\": {\"id\": 1, \"title\": \"즉흥 여행\", \"items\": []}}"))),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"계획 조회 중 오류가 발생했습니다.\"}")))
	})
	@GetMapping("/{planId}/with-items")
	public ResponseEntity<Map<String, Object>> selectQuickPlanWithItems(
			@Parameter(description = "조회할 계획 ID", required = true, example = "1") @PathVariable Long planId) {
		try {
			QuickPlan plan = quickPlanService.selectQuickPlanWithItems(planId);
			Map<String, Object> response = Map.of("message", "계획이 성공적으로 조회되었습니다.", "plan", plan);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("selectQuickPlanWithItems error", e);
            Map<String, Object> response = Map.of(
                    "message", "계획 조회 중 오류가 발생했습니다."
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@Operation(summary = "사용자별 여행 계획 목록 조회", description = "특정 사용자의 모든 여행 계획을 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"계획이 성공적으로 조회되었습니다.\", \"plans\": []}"))),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"계획 조회 중 오류가 발생했습니다.\"}")))
	})
	@GetMapping("/user/{userId}")
	public ResponseEntity<Map<String, Object>> selectQuickPlans(
			@Parameter(description = "조회할 사용자 ID", required = true, example = "user123") @PathVariable Long userId) {
		try {
			List<QuickPlan> plans = quickPlanService.selectQuickPlans(userId);
			Map<String, Object> response = Map.of("message", "계획이 성공적으로 조회되었습니다.", "plans", plans);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("selectQuickPlans error", e);
            Map<String, Object> response = Map.of(
                    "message", "계획 조회 중 오류가 발생했습니다."
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@Operation(summary = "사용자별 여행 계획 목록 조회 (PlanItem 포함)", description = "특정 사용자의 모든 여행 계획과 세부 일정을 함께 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"계획이 성공적으로 조회되었습니다.\", \"plans\": []}"))),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"계획 조회 중 오류가 발생했습니다.\"}")))
	})
	@GetMapping("/user/{userId}/with-items")
	public ResponseEntity<Map<String, Object>> selectQuickPlansWithItems(
			@Parameter(description = "조회할 사용자 ID", required = true, example = "user123") @PathVariable Long userId) {
		try {
			List<QuickPlan> plans = quickPlanService.selectQuickPlansWithItems(userId);
			Map<String, Object> response = Map.of("message", "계획이 성공적으로 조회되었습니다.", "plans", plans);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("selectQuickPlansWithItems error", e);
            Map<String, Object> response = Map.of(
                    "message", "계획 조회 중 오류가 발생했습니다."
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "여행 계획 수정", description = "기존 여행 계획 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"계획이 성공적으로 수정되었습니다.\", \"plan\": {\"planId\": 1, \"title\": \"수정된 여행\"}}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"계획 수정 중 오류가 발생했습니다.\"}"
                            )
                    )
            )
    })
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateQuickPlan(
            @Parameter(description = "수정할 여행 계획 정보", required = true)
            @RequestBody QuickPlan plan) {
        try {
            quickPlanService.updateQuickPlan(plan);
            Map<String, Object> response = Map.of(
                    "message", "계획이 성공적으로 수정되었습니다.",
                    "plan", plan
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("updateQuickPlan error", e);
            Map<String, Object> response = Map.of(
                    "message", "계획 수정 중 오류가 발생했습니다."
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "여행 계획 아이템 순서 변경", description = "특정 여행 계획의 PlanItem 순서를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "순서 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"순서가 성공적으로 변경되었습니다.\", \"planId\": 1}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"잘못된 PlanItem ID입니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"순서 변경 중 오류가 발생했습니다.\"}"
                            )
                    )
            )
    })
    @PatchMapping("/{planId}/items/order")
    public ResponseEntity<Map<String, Object>> updateQuickPlanOrder(
            @Parameter(description = "순서를 변경할 계획 ID", required = true, example = "1")
            @PathVariable Long planId,
            @Parameter(description = "PlanItem ID와 순서 매핑 정보", required = true)
            @RequestBody UpdateSequenceOrderRequestDto requestDto) {
        try {
            // DTO를 Map으로 변환
            Map<Long, Integer> planItemOrders = new HashMap<>();
            if (requestDto.getPlanItems() != null) {
                for (UpdateSequenceOrderRequestDto.PlanItemOrder item : requestDto.getPlanItems()) {
                    planItemOrders.put(item.getPlanItemId(), item.getSequenceOrder());
                }
            }

            quickPlanService.updatePlanItemsOrder(planId, planItemOrders);
            Map<String, Object> response = Map.of(
                    "message", "순서가 성공적으로 변경되었습니다.",
                    "planId", planId
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("updateQuickPlanOrder validation error", e);
            Map<String, Object> response = Map.of(
                    "message", e.getMessage()
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("updateQuickPlanOrder error", e);
            Map<String, Object> response = Map.of(
                    "message", "순서 변경 중 오류가 발생했습니다."
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "여행 계획 삭제", description = "특정 ID의 여행 계획을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"계획이 성공적으로 삭제되었습니다.\", \"planId\": 1}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"계획 삭제 중 오류가 발생했습니다.\"}"
                            )
                    )
            )
    })
    @DeleteMapping("/{planId}")
    public ResponseEntity<Map<String, Object>> deleteQuickPlan(
            @Parameter(description = "삭제할 계획 ID", required = true, example = "1")
            @PathVariable Long planId) {
        try {
            quickPlanService.deleteQuickPlan(planId);
            Map<String, Object> response = Map.of(
                    "message", "계획이 성공적으로 삭제되었습니다.",
                    "planId", planId
            );

            // 보통 204는 바디를 안 주긴 하는데, 지금 구조 맞추려고 그대로 둠
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("deleteQuickPlan error", e);
            Map<String, Object> response = Map.of(
                    "message", "계획 삭제 중 오류가 발생했습니다."
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "상태별 여행 계획 조회", description = "특정 사용자의 여행 계획을 상태(TEMP/ONGOING/DONE)별로 필터링하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"계획이 성공적으로 조회되었습니다.\", \"plans\": []}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"계획 조회 중 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<Map<String, Object>> selectQuickPlansByStatus(
            @Parameter(description = "조회할 사용자 ID", required = true, example = "1") @PathVariable Long userId,
            @Parameter(description = "계획 상태 (TEMP/ONGOING/DONE)", required = true, example = "ONGOING") @RequestParam planStatus status) {
        try {
            List<QuickPlan> plans = quickPlanService.selectQuickPlansByStatus(userId, status);
            Map<String, Object> response = Map.of("message", "계획이 성공적으로 조회되었습니다.", "plans", plans);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("selectQuickPlansByStatus error", e);
            Map<String, Object> response = Map.of(
                    "message", "계획 조회 중 오류가 발생했습니다."
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "여행 계획 상태 변경", description = "특정 여행 계획의 상태만 변경합니다. ONGOING ↔ DONE 간 전환만 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경 성공",
                content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\": true, \"planId\": 123, \"status\": \"완료\"}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 상태 전이",
                content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\": false, \"message\": \"잘못된 상태 전이입니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "계획을 찾을 수 없음",
                content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\": false, \"message\": \"존재하지 않는 계획입니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\": false, \"message\": \"상태 변경 중 오류가 발생했습니다.\"}")))
    })
    @PatchMapping("/{planId}/status")
    public ResponseEntity<Map<String, Object>> updateQuickPlanStatus(
            @Parameter(description = "계획 ID", required = true, example = "1")
            @PathVariable Long planId,
            @Parameter(description = "변경할 상태", required = true)
            @Valid @RequestBody UpdateStatusRequest request) {
        try {
            quickPlanService.updateQuickPlanStatus(planId, request.getStatus());

            Map<String, Object> response = Map.of(
                    "success", true,
                    "planId", planId,
                    "status", request.getStatus()
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // 계획을 찾을 수 없는 경우 (404)
            log.error("Plan not found: planId={}", planId, e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", e.getMessage()
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            // 잘못된 상태 전이 (400)
            log.error("Invalid status transition: planId={}, status={}", planId, request.getStatus(), e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", e.getMessage()
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // 기타 서버 오류 (500)
            log.error("updateQuickPlanStatus error: planId={}", planId, e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "상태 변경 중 오류가 발생했습니다."
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}