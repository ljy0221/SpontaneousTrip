package com.ssafy.passproject.domain.planitem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.passproject.domain.planitem.entity.PlanItem;
import com.ssafy.passproject.domain.planitem.service.PlanItemService;
import com.ssafy.passproject.domain.quickplan.service.QuickPlanService;

@Tag(name = "PlanItem", description = "여행 세부 일정 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/planitem")
@RestController
public class PlanItemController {

	private final PlanItemService planItemService;
	private final QuickPlanService quickPlanService;

	@Operation(summary = "세부 일정 생성", description = "새로운 여행 세부 일정을 생성합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "일정 생성 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"planItem이 성공적으로 삽입되었습니다.\", \"item\": {\"id\": 1, \"title\": \"일정 제목\"}}"))),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"planItem 삽입이 실패되었습니다.\"}")))
	})
	@PostMapping()
	public ResponseEntity<Map<String, Object>> insertPlanItem(
			@Parameter(description = "생성할 세부 일정 정보", required = true) @RequestBody PlanItem i) {
		try {
			planItemService.insertPlanItem(i);
			Map<String, Object> response = Map.of("message", "planItem이 성공적으로 삽입되었습니다.", "item", i);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, Object> response = Map.of("message", "planItem 삽입이 실패되었습니다.");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "세부 일정 단건 조회", description = "특정 ID의 세부 일정을 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"planItem이 성공적으로 조회되었습니다.\", \"item\": {\"id\": 1, \"title\": \"일정 제목\"}}"))),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"planItem 조회가 실패되었습니다.\"}")))
	})
	@GetMapping("/{planItemId}")
	public ResponseEntity<Map<String, Object>> selectPlanItem(
			@Parameter(description = "조회할 세부 일정 ID", required = true, example = "1") @PathVariable Long planItemId) {
		try {
			PlanItem selectPlanItem = planItemService.selectPlanItem(planItemId);
			Map<String, Object> response = Map.of("message", "planItem이 성공적으로 조회되었습니다.", "item", selectPlanItem);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, Object> response = Map.of("message", "planItem 조회가 실패되었습니다.");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "전체 세부 일정 조회", description = "모든 세부 일정을 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"planItem이 성공적으로 조회되었습니다.\", \"items\": [{\"id\": 1, \"title\": \"일정 제목\"}]}"))),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"planItem 조회가 실패되었습니다.\"}")))
	})
	@GetMapping()
	public ResponseEntity<Map<String, Object>> selectPlanItems() {
		try {
			List<PlanItem> items = planItemService.selectPlanItems();
			Map<String, Object> response = Map.of("message", "planItem이 성공적으로 조회되었습니다.", "items", items);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, Object> response = Map.of("message", "planItem 조회가 실패되었습니다.");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "세부 일정 수정", description = "기존 세부 일정 정보를 수정합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"planItem이 성공적으로 수정되었습니다.\", \"item\": {\"id\": 1, \"title\": \"수정된 일정 제목\"}}"))),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"planItem 수정이 실패되었습니다.\"}")))
	})
	@PutMapping()
	public ResponseEntity<Map<String, Object>> updatePlanItem(
			@Parameter(description = "수정할 세부 일정 정보", required = true) @RequestBody PlanItem i) {
		try {
			planItemService.updatePlanItem(i);
			Map<String, Object> response = Map.of("message", "planItem이 성공적으로 수정되었습니다.", "item", i);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, Object> response = Map.of("message", "planItem 수정이 실패되었습니다.");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "세부 일정 삭제", description = "특정 ID의 세부 일정을 삭제합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"planItem이 성공적으로 삭제되었습니다.\", \"item\": 1}"))),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"planItem 삭제가 실패되었습니다.\"}")))
	})
	@DeleteMapping("/{planItemId}")
	public ResponseEntity<Map<String, Object>> deletePlanItem(
			@Parameter(description = "삭제할 세부 일정 ID", required = true, example = "1") @PathVariable Long planItemId) {
		try {
			planItemService.deletePlanItem(planItemId);
			Map<String, Object> response = Map.of("message", "planItem이 성공적으로 삭제되었습니다.", "item", planItemId);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, Object> response = Map.of("message", "planItem 삭제가 실패되었습니다.");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}