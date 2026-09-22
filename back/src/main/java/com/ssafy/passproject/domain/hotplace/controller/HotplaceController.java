package com.ssafy.passproject.domain.hotplace.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ssafy.passproject.domain.hotplace.dto.request.HotplaceRequestDto;
import com.ssafy.passproject.domain.hotplace.dto.response.HotplaceResponseDto;
import com.ssafy.passproject.domain.hotplace.dto.response.PagedHotplaceResponse;
import com.ssafy.passproject.domain.hotplace.entity.Hotplace;
import com.ssafy.passproject.domain.hotplace.service.HotplaceService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Hotplace API", description = "GIS 및 AI 기반 핫플레이스 관리 및 추천 엔드포인트")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/hotplace")
@RestController
public class HotplaceController {

	private final HotplaceService service;
	private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

	// // -------------------------------------------------------------
	// // 1. 🌟 핵심 기능: Spontaneous Trip Search (GET /hotplace/spontaneous)
	// // -------------------------------------------------------------
	// @Operation(summary = "AI 기반 즉흥 여행지 추천", description = "현재 위치와 남은 시간을 기반으로 AI가
	// 큐레이션한 Hotplace 목록을 거리순으로 반환합니다.")
	// @ApiResponses({
	// @ApiResponse(responseCode = "200", description = "추천 성공", content =
	// @Content(mediaType = "application/json", examples = @ExampleObject(value =
	// "{\"message\": \"AI 큐레이션 결과입니다.\", \"list\": [...]}"))),
	// @ApiResponse(responseCode = "400", description = "잘못된 요청 (시간 예산 부족 등)",
	// content = @Content(mediaType = "application/json", examples =
	// @ExampleObject(value = "{\"message\": \"시간 예산(Time Budget)은 0보다 커야
	// 합니다.\"}"))),
	// @ApiResponse(responseCode = "404", description = "추천 결과 없음", content =
	// @Content(mediaType = "application/json", examples = @ExampleObject(value =
	// "{\"message\": \"주변에서 적절한 즉흥 여행지를 찾지 못했습니다.\"}"))),
	// @ApiResponse(responseCode = "500", description = "서버 오류", content =
	// @Content(mediaType = "application/json", examples = @ExampleObject(value =
	// "{\"message\": \"검색 처리 중 서버 오류가 발생했습니다.\"}")))
	// })
	// @GetMapping("/spontaneous")
	// public ResponseEntity<Map<String, Object>> findSpontaneousPlaces(
	// @Parameter(description = "현재 사용자 위도 (Latitude)", required = true)
	// @RequestParam("lat") double currentLat,

	// @Parameter(description = "현재 사용자 경도 (Longitude)", required = true)
	// @RequestParam("lon") double currentLon,

	// @Parameter(description = "남은 시간 예산 (분 단위)", required = true)
	// @RequestParam("timeBudget") int timeBudget) {
	// log.info("Spontaneous Search Request - Lat: {}, Lon: {}, Time: {} min",
	// currentLat, currentLon, timeBudget);

	// if (timeBudget <= 0) {
	// return new ResponseEntity<>(
	// Map.of("message", "시간 예산(Time Budget)은 0보다 커야 합니다."),
	// HttpStatus.BAD_REQUEST);
	// }

	// try {
	// List<HotplaceResponseDto> list = service.findSpontaneousPlaces(
	// currentLat,
	// currentLon,
	// timeBudget);

	// if (list.isEmpty()) {
	// return new ResponseEntity<>(
	// Map.of("message", "주변에서 적절한 즉흥 여행지를 찾지 못했습니다."),
	// HttpStatus.NOT_FOUND);
	// }

	// Map<String, Object> response = Map.of(
	// "message", "AI 큐레이션 결과입니다.",
	// "list", list);
	// return new ResponseEntity<>(response, HttpStatus.OK);

	// } catch (Exception e) {
	// log.error("Spontaneous Search Failed", e);
	// return new ResponseEntity<>(
	// Map.of("message", "검색 처리 중 서버 오류가 발생했습니다."),
	// HttpStatus.INTERNAL_SERVER_ERROR);
	// }
	// }

	// @Operation(summary = "주변 핫플레이스 검색", description = "지정된 좌표와 반경 내의 핫플레이스를
	// 검색합니다.")
	// @GetMapping("/search")
	// public ResponseEntity<Map<String, Object>> searchHotplaces(
	// @Parameter(description = "경도 (Map X)", required = true) @RequestParam("mapX")
	// double mapX,
	// @Parameter(description = "위도 (Map Y)", required = true) @RequestParam("mapY")
	// double mapY,
	// @Parameter(description = "반경 (m)", required = true) @RequestParam("radius")
	// int radius) {
	// try {
	// List<HotplaceResponseDto> list = service.findSpontaneousPlaces(mapX, mapY,
	// radius / 60);
	// Map<String, Object> response = Map.of("message", "성공적으로 검색되었습니다.", "data",
	// list);
	// return new ResponseEntity<>(response, HttpStatus.OK);
	// } catch (Exception e) {
	// log.error("Hotplace Search Failed", e);
	// return new ResponseEntity<>(Map.of("message", "검색 실패: " + e.getMessage()),
	// HttpStatus.INTERNAL_SERVER_ERROR);
	// }
	// }

	// -------------------------------------------------------------
	// 2. CRUD 기능 (DTO + Swagger 적용)
	// -------------------------------------------------------------

	/**
	 * Hotplace 등록 (POST /hotplace/)
	 */
	@Operation(summary = "Hotplace 등록", description = "새로운 Hotplace 정보를 등록합니다. (사용자 등록 혹은 관리자 등록)")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "등록 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"성공적으로 삽입되었습니다.\", \"placeId\": 1}"))),
			@ApiResponse(responseCode = "406", description = "등록 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"삽입 실패: ...\"}")))
	})
	@PostMapping("/")
	public ResponseEntity<Map<String, Object>> hotplaceInsert(
			@RequestBody @Valid HotplaceRequestDto dto) {
		try {
			Point location = geometryFactory.createPoint(
					new Coordinate(dto.getLongitude(), dto.getLatitude()));
			Hotplace hotplace = dto.toEntity(location, "USER_REG");
			service.insertHotplace(hotplace);

			Map<String, Object> response = Map.of(
					"message", "성공적으로 삽입되었습니다.",
					"placeId", hotplace.getPlaceId());
			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Hotplace Insert Failed", e);
			return new ResponseEntity<>(
					Map.of("message", "삽입 실패: " + e.getMessage()),
					HttpStatus.NOT_ACCEPTABLE);
		}
	}

	/**
	 * Hotplace 목록 조회 (GET /hotplace/)
	 */
	@Operation(summary = "Hotplace 전체 목록 조회", description = "등록된 모든 Hotplace의 기본 정보를 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(
					// --- 이 부분이 핵심적으로 수정되었습니다 ---
					value = """
							{
							  "message": "성공적으로 목록이 조회되었습니다.",
							  "lists": [
							    {
							      "placeId": 1,
							      "name": "장소 이름",
							      "category": "카테고리",
							      "address": "주소",
							      "latitude": 37.5666,
							      "longitude": 126.9780,
							      "description": "장소 설명",
							      "image": "이미지 URL"
							    },
							    {
							      "placeId": 2,
							      "name": "장소 이름",
							      "category": "카테고리",
							      "address": "주소",
							      "latitude": 37.5666,
							      "longitude": 126.9780,
							      "description": "장소 설명",
							      "image": "이미지 URL"
							    }
							  ]
							}
							"""))),
			@ApiResponse(responseCode = "404", description = "조회 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"조회 실패: ...\"}")))

	})

	@GetMapping("/")
	public ResponseEntity<Map<String, Object>> hotplaceList(
			@Parameter(description = "카테고리 필터 (선택사항)", required = false) @RequestParam(required = false) String category) {
		log.info("========== Hotplace List Request ==========");
		log.info("Received category parameter: {}", category);

		try {
			List<Hotplace> hotplaces;

			// 카테고리가 제공되면 카테고리별로 필터링, 아니면 전체 조회
			if (category != null && !category.isEmpty()) {
				hotplaces = service.findByCategory(category);
			} else {
				hotplaces = service.selectHotplaceList();
			}

			List<HotplaceResponseDto> lists = hotplaces.stream()
					.map(HotplaceResponseDto::new)
					.collect(Collectors.toList());

			Map<String, Object> response = Map.of(
					"message", "성공적으로 조회되었습니다.",
					"lists", lists);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Hotplace List Failed", e);
			return new ResponseEntity<>(
					Map.of("message", "조회 실패: " + e.getMessage()),
					HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Hotplace 수정 (PUT /hotplace/{id})
	 */
	@Operation(summary = "Hotplace 정보 수정", description = "지정된 ID의 Hotplace 정보를 업데이트합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"성공적으로 갱신되었습니다.\", \"placeId\": 1}"))),
			@ApiResponse(responseCode = "404", description = "수정 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"갱신 실패: ...\"}")))
	})
	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> hotplaceUpdate(
			@Parameter(description = "수정할 Hotplace ID", required = true) @PathVariable Long id,
			@RequestBody @Valid HotplaceRequestDto dto) {
		try {
			Point location = geometryFactory.createPoint(
					new Coordinate(dto.getLongitude(), dto.getLatitude()));
			Hotplace hotplace = dto.toEntity(location, "USER_REG");
			// 필요하면 여기서 ID 세팅:
			// hotplace.setPlaceId(id);

			service.updateHotplace(hotplace);

			Map<String, Object> response = Map.of(
					"message", "성공적으로 갱신되었습니다.",
					"placeId", id);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Hotplace Update Failed", e);
			return new ResponseEntity<>(
					Map.of("message", "갱신 실패: " + e.getMessage()),
					HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Hotplace 상세 조회 (GET /hotplace/{id})
	 */
	@Operation(summary = "Hotplace 상세 조회", description = "지정된 ID의 Hotplace 상세 정보와 위치를 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(
					// **줄 바꿈(\n)을 사용하여 포맷을 유지하고, Hotplace 내부의 실제 구조를 정의해야 합니다.**
					value = "{\n" +
							"  \"message\": \"성공적으로 조회되었습니다.\",\n" +
							"  \"Hotplace\": {\n" +
							"    \"id\": 101,\n" +
							"    \"name\": \"강남역\",\n" +
							"    \"score\": 4.8\n" +
							"  }\n" +
							"}"))),
			@ApiResponse(responseCode = "404", description = "조회 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"상세 조회 실패: ...\"}")))
	})
	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> hotplaceDetail(
			@Parameter(description = "조회할 Hotplace ID", required = true) @PathVariable Long id) {
		try {
			Hotplace hotplace = service.selectHotplaceDetail(new Hotplace(id));
			HotplaceResponseDto dto = new HotplaceResponseDto(hotplace);

			Map<String, Object> response = Map.of(
					"message", "성공적으로 조회되었습니다.",
					"Hotplace", dto);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Hotplace Detail Failed", e);
			return new ResponseEntity<>(
					Map.of("message", "상세 조회 실패: " + e.getMessage()),
					HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Hotplace 삭제 (DELETE /hotplace/{id})
	 */
	@Operation(summary = "Hotplace 삭제", description = "지정된 ID의 Hotplace를 삭제합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"성공적으로 삭제되었습니다.\", \"placeId\": 1}"))),
			@ApiResponse(responseCode = "404", description = "삭제 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"삭제 실패: ...\"}")))
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> hotplaceDelete(
			@Parameter(description = "삭제할 Hotplace ID", required = true) @PathVariable Long id) {
		try {
			service.deleteHotplace(new Hotplace(id));
			Map<String, Object> response = Map.of(
					"message", "성공적으로 삭제되었습니다.",
					"placeId", id);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Hotplace Delete Failed", e);
			return new ResponseEntity<>(
					Map.of("message", "삭제 실패: " + e.getMessage()),
					HttpStatus.NOT_FOUND);
		}
	}

	// // -------------------------------------------------------------
	// // Chatbot & Summary
	// // -------------------------------------------------------------
	// @Operation(summary = "Chatbot 대화", description = "RAG 챗봇과 대화합니다.")
	// @ApiResponses({
	// @ApiResponse(responseCode = "200", description = "대화 성공", content =
	// @Content(mediaType = "application/json", examples = @ExampleObject(value =
	// "{\"response\": \"챗봇 응답...\"}"))),
	// @ApiResponse(responseCode = "500", description = "서버 오류", content =
	// @Content(mediaType = "application/json", examples = @ExampleObject(value =
	// "{\"message\": \"채팅 실패: ...\"}")))
	// })
	// @PostMapping("/chat")
	// public ResponseEntity<?> chat(
	// @RequestBody com.ssafy.passproject.domain.hotplace.dto.request.ChatRequestDto
	// request) {
	// try {
	// com.ssafy.passproject.domain.hotplace.dto.response.ChatResponseDto response =
	// service.chat(request);
	// return new ResponseEntity<>(response, HttpStatus.OK);
	// } catch (Exception e) {
	// log.error("Chat Failed", e);
	// return new ResponseEntity<>(
	// Map.of("message", "채팅 실패: " + e.getMessage()),
	// HttpStatus.INTERNAL_SERVER_ERROR);
	// }
	// }

	// @Operation(summary = "Hotplace 요약 생성", description = "Hotplace에 대한 요약을
	// 생성합니다.")
	// @ApiResponses({
	// @ApiResponse(responseCode = "200", description = "요약 성공", content =
	// @Content(mediaType = "application/json", examples = @ExampleObject(value =
	// "{\"summary\": \"요약 내용...\"}"))),
	// @ApiResponse(responseCode = "500", description = "서버 오류", content =
	// @Content(mediaType = "application/json", examples = @ExampleObject(value =
	// "{\"message\": \"요약 생성 실패: ...\"}")))
	// })
	// @PostMapping("/summary")
	// public ResponseEntity<?> generateSummary(
	// @RequestBody
	// com.ssafy.passproject.domain.hotplace.dto.request.SummaryRequestDto request)
	// {
	// try {
	// com.ssafy.passproject.domain.hotplace.dto.response.SummaryResponseDto
	// response = service
	// .generateSummary(request);
	// return new ResponseEntity<>(response, HttpStatus.OK);
	// } catch (Exception e) {
	// log.error("Summary Generation Failed", e);
	// return new ResponseEntity<>(
	// Map.of("message", "요약 생성 실패: " + e.getMessage()),
	// HttpStatus.INTERNAL_SERVER_ERROR);
	// }
	// }

	// -------------------------------------------------------------
	// Tour API 연동
	// -------------------------------------------------------------
	@Operation(summary = "Tour API 데이터 가져오기", description = "Tour API에서 데이터를 가져와 DB에 저장합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "가져오기 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"데이터 가져오기 성공\"}"))),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"데이터 가져오기 실패: ...\"}")))
	})
	@PostMapping("/fetch")
	public ResponseEntity<?> fetchTourData(@RequestParam String areaCode) {
		try {
			service.fetchAndSaveTourData(areaCode);
			return new ResponseEntity<>(
					Map.of("message", "데이터 가져오기 성공"),
					HttpStatus.OK);
		} catch (Exception e) {
			log.error("Fetch Failed", e);
			return new ResponseEntity<>(
					Map.of("message", "데이터 가져오기 실패: " + e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// -------------------------------------------------------------
	// Time and Transport-Based Search (GET /hotplace/search)
	// -------------------------------------------------------------
	@Operation(summary = "시간 및 교통수단 기반 Hotplace 검색", description = "현재 위치, 남은 시간, 교통수단을 기반으로 도달 가능한 Hotplace 목록을 페이징하여 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "검색 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"검색이 성공적으로 완료되었습니다.\", \"data\": []}"))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"시간(timeInMinutes)은 0보다 커야 합니다.\"}"))),
			@ApiResponse(responseCode = "404", description = "검색 결과 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"주변에서 Hotplace를 찾지 못했습니다.\"}"))),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"검색 처리 중 서버 오류가 발생했습니다.\"}")))
	})
	@GetMapping("/search")
	public ResponseEntity<Map<String, Object>> searchNearbyHotplaces(
			@Parameter(description = "경도 (Longitude)", required = true) @RequestParam("mapX") double mapX,

			@Parameter(description = "위도 (Latitude)", required = true) @RequestParam("mapY") double mapY,

			@Parameter(description = "남은 시간 (분 단위)", required = false) @RequestParam(value = "timeInMinutes", defaultValue = "10") int timeInMinutes,

			@Parameter(description = "교통수단 (CAR, TRANSIT, WALK)", required = false) @RequestParam(value = "transportMode", defaultValue = "WALK") String transportMode,

			@Parameter(description = "카테고리 필터 (선택사항)", required = false) @RequestParam(required = false) String category,

			@Parameter(description = "페이지 번호 (0부터 시작)", required = false) @RequestParam(value = "page", defaultValue = "0") int page,

			@Parameter(description = "페이지 크기", required = false) @RequestParam(value = "size", defaultValue = "10") int size) {

		if (timeInMinutes <= 0) {
			return new ResponseEntity<>(
					Map.of("message", "시간(timeInMinutes)은 0보다 커야 합니다."),
					HttpStatus.BAD_REQUEST);
		}

		if (page < 0 || size <= 0) {
			return new ResponseEntity<>(
					Map.of("message", "페이지 번호는 0 이상, 페이지 크기는 1 이상이어야 합니다."),
					HttpStatus.BAD_REQUEST);
		}

		try {
			PagedHotplaceResponse pagedResponse = service.getNearbyHotplaces(mapX, mapY, timeInMinutes, transportMode,
					category, page, size);

			if (pagedResponse.getData().isEmpty()) {
				return new ResponseEntity<>(
						Map.of("message", "주변에서 Hotplace를 찾지 못했습니다."),
						HttpStatus.NOT_FOUND);
			}

			Map<String, Object> response = Map.of(
					"message", "검색이 성공적으로 완료되었습니다.",
					"data", pagedResponse.getData(),
					"pageInfo", pagedResponse.getPageInfo(),
					"timeInMinutes", timeInMinutes,
					"transportMode", transportMode);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Hotplace Search Failed", e);
			return new ResponseEntity<>(
					Map.of("message", "검색 처리 중 서버 오류가 발생했습니다."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// -------------------------------------------------------------
	// 위치 기반 인기 hotplace 조회 top 10개
	// -------------------------------------------------------------
	@Operation(summary = "위치 기반 인기 Hotplace 조회", description = "현재 위치 기준으로 반경 내에서 count가 높은 인기 Hotplace를 내림차순으로 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인기 Hotplace 조회 성공\", \"data\": []}"))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"반경(radiusMeters)은 0보다 커야 합니다.\"}"))),
			@ApiResponse(responseCode = "404", description = "결과 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"주변에서 인기 Hotplace를 찾지 못했습니다.\"}"))),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"조회 중 서버 오류가 발생했습니다.\"}")))
	})
	@GetMapping("/popular")
	public ResponseEntity<Map<String, Object>> getPopularHotplaces(
			@Parameter(description = "현재 위도 (Latitude)", required = true) @RequestParam("lat") double lat,
			@Parameter(description = "현재 경도 (Longitude)", required = true) @RequestParam("lon") double lon,
			@Parameter(description = "검색 반경 (미터 단위)", required = false) @RequestParam(value = "radiusMeters", defaultValue = "5000") int radiusMeters,
			@Parameter(description = "조회할 개수", required = false) @RequestParam(value = "limit", defaultValue = "10") int limit) {

		log.info("Popular Hotplaces Request - lat: {}, lon: {}, radius: {} m, limit: {}", lat, lon, radiusMeters,
				limit);

		if (radiusMeters <= 0) {
			return new ResponseEntity<>(
					Map.of("message", "반경(radiusMeters)은 0보다 커야 합니다."),
					HttpStatus.BAD_REQUEST);
		}

		if (limit <= 0 || limit > 100) {
			return new ResponseEntity<>(
					Map.of("message", "조회 개수(limit)는 1 이상 100 이하여야 합니다."),
					HttpStatus.BAD_REQUEST);
		}

		try {
			List<HotplaceResponseDto> popularHotplaces = service.getPopularHotplaces(lat, lon, radiusMeters, limit);

			if (popularHotplaces.isEmpty()) {
				return new ResponseEntity<>(
						Map.of("message", "주변에서 인기 Hotplace를 찾지 못했습니다."),
						HttpStatus.NOT_FOUND);
			}

			Map<String, Object> response = Map.of(
					"message", "인기 Hotplace 조회 성공",
					"data", popularHotplaces,
					"count", popularHotplaces.size());
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Popular Hotplaces Query Failed", e);
			return new ResponseEntity<>(
					Map.of("message", "조회 중 서버 오류가 발생했습니다."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
