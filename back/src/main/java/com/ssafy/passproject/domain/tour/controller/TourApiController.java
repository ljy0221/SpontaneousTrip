package com.ssafy.passproject.domain.tour.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.passproject.domain.tour.dto.request.TourApiRequestDto;
import com.ssafy.passproject.domain.tour.dto.request.TourDetailRequestDto;
import com.ssafy.passproject.domain.tour.dto.response.TourApiResponseDto;
import com.ssafy.passproject.domain.tour.dto.response.TourDetailResponseDto;
import com.ssafy.passproject.domain.tour.entity.TourPlace;
import com.ssafy.passproject.domain.tour.service.TourApiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Tour API", description = "한국관광공사 API 연동")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/tour")
@RestController
public class TourApiController {

        private final TourApiService tourApiService;

        @Operation(summary = "주변 관광지 ContentId 조회", description = "좌표와 거리를 기반으로 주변 관광지의 ContentId 목록을 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"관광지 정보가 성공적으로 조회되었습니다.\", \"data\": {\"contentIds\": [\"123\", \"456\"], \"totalCount\": 2}}"))),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"잘못된 요청입니다.\"}"))),
                        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"관광지 조회 중 오류가 발생했습니다.\"}")))
        })
        @GetMapping("/content-ids")
        public ResponseEntity<Map<String, Object>> getContentIds(
                        @Parameter(description = "경도 (예: 126.981106)", required = true) @RequestParam String mapX,

                        @Parameter(description = "위도 (예: 37.568477)", required = true) @RequestParam String mapY,

                        @Parameter(description = "반경 거리 (미터, 기본값: 1000)", required = false) @RequestParam(required = false, defaultValue = "1000") String radius,

                        @Parameter(description = "관광타입 (12:관광지, 14:문화시설, 15:축제, 25:여행코스, 28:레포츠, 32:숙박, 38:쇼핑, 39:음식점)", required = false) @RequestParam(required = false) String contentTypeId,

                        @Parameter(description = "한 페이지 결과 수 (기본값: 50)", required = false) @RequestParam(required = false, defaultValue = "50") String numOfRows,

                        @Parameter(description = "페이지 번호 (기본값: 1)", required = false) @RequestParam(required = false, defaultValue = "1") String pageNo) {

                try {
                        TourApiRequestDto requestDto = TourApiRequestDto.builder()
                                        .mapX(mapX)
                                        .mapY(mapY)
                                        .radius(radius)
                                        .contentTypeId(contentTypeId)
                                        .numOfRows(numOfRows)
                                        .pageNo(pageNo)
                                        .arrange("E") // 거리순 정렬
                                        .build();

                        List<String> contentIds = tourApiService.getContentIds(requestDto);

                        TourApiResponseDto responseDto = new TourApiResponseDto(contentIds, contentIds.size());

                        Map<String, Object> response = Map.of(
                                        "message", "관광지 정보가 성공적으로 조회되었습니다.",
                                        "data", responseDto);

                        return new ResponseEntity<>(response, HttpStatus.OK);

                } catch (Exception e) {
                        log.error("관광지 조회 중 오류 발생", e);
                        Map<String, Object> response = Map.of(
                                        "message", "관광지 조회 중 오류가 발생했습니다.",
                                        "error", e.getMessage());
                        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        @Operation(summary = "주변 관광지 상세 정보 조회", description = "좌표와 거리를 기반으로 주변 관광지의 상세 정보를 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"관광지 정보가 성공적으로 조회되었습니다.\", \"data\": [{\"title\": \"관광지명\", \"addr1\": \"주소\"}], \"count\": 1}"))),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"잘못된 요청입니다.\"}"))),
                        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"관광지 조회 중 오류가 발생했습니다.\"}")))
        })
        @GetMapping("/places")
        public ResponseEntity<Map<String, Object>> getTourPlaces(
                        @Parameter(description = "경도 (예: 126.981106)", required = true) @RequestParam String mapX,

                        @Parameter(description = "위도 (예: 37.568477)", required = true) @RequestParam String mapY,

                        @Parameter(description = "반경 거리 (미터, 기본값: 1000)", required = false) @RequestParam(required = false, defaultValue = "1000") String radius,

                        @Parameter(description = "관광타입", required = false) @RequestParam(required = false) String contentTypeId,

                        @Parameter(description = "한 페이지 결과 수 (기본값: 10)", required = false) @RequestParam(required = false, defaultValue = "10") String numOfRows,

                        @Parameter(description = "페이지 번호 (기본값: 1)", required = false) @RequestParam(required = false, defaultValue = "1") String pageNo) {

                try {
                        TourApiRequestDto requestDto = TourApiRequestDto.builder()
                                        .mapX(mapX)
                                        .mapY(mapY)
                                        .radius(radius)
                                        .contentTypeId(contentTypeId)
                                        .numOfRows(numOfRows)
                                        .pageNo(pageNo)
                                        .arrange("E")
                                        .build();

                        List<TourPlace> tourPlaces = tourApiService.getTourPlaces(requestDto);

                        Map<String, Object> response = Map.of(
                                        "message", "관광지 정보가 성공적으로 조회되었습니다.",
                                        "data", tourPlaces,
                                        "count", tourPlaces.size());

                        return new ResponseEntity<>(response, HttpStatus.OK);

                } catch (Exception e) {
                        log.error("관광지 조회 중 오류 발생", e);
                        Map<String, Object> response = Map.of(
                                        "message", "관광지 조회 중 오류가 발생했습니다.",
                                        "error", e.getMessage());
                        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        @Operation(summary = "관광지 공통정보 상세 조회", description = "contentId를 통해 관광지의 상세 공통정보를 조회합니다. (제목, 주소, 좌표, 개요 등)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"관광지 상세정보가 성공적으로 조회되었습니다.\", \"data\": {\"title\": \"관광지명\", \"overview\": \"개요\"}}"))),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"잘못된 요청입니다.\"}"))),
                        @ApiResponse(responseCode = "404", description = "관광지를 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"해당 contentId의 관광지를 찾을 수 없습니다.\"}"))),
                        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"관광지 상세정보 조회 중 오류가 발생했습니다.\"}")))
        })
        @GetMapping("/detail")
        public ResponseEntity<Map<String, Object>> getTourDetail(
                        @Parameter(description = "콘텐츠 ID (예: 126508)", required = true) @RequestParam String contentId) {

                try {
                        TourDetailRequestDto requestDto = TourDetailRequestDto.builder()
                                        .contentId(contentId)
                                        .build();

                        TourDetailResponseDto tourDetail = tourApiService.getTourDetail(requestDto);

                        Map<String, Object> response = Map.of(
                                        "message", "관광지 상세정보가 성공적으로 조회되었습니다.",
                                        "data", tourDetail);

                        return new ResponseEntity<>(response, HttpStatus.OK);

                } catch (Exception e) {
                        log.error("관광지 상세정보 조회 중 오류 발생", e);

                        // 조회 결과가 없는 경우
                        if (e.getMessage().contains("조회 결과가 없습니다")) {
                                Map<String, Object> response = Map.of(
                                                "message", "해당 contentId의 관광지를 찾을 수 없습니다.",
                                                "error", e.getMessage());
                                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                        }

                        Map<String, Object> response = Map.of(
                                        "message", "관광지 상세정보 조회 중 오류가 발생했습니다.",
                                        "error", e.getMessage());
                        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }
}