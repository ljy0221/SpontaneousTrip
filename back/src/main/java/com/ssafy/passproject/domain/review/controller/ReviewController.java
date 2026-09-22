package com.ssafy.passproject.domain.review.controller;

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

import com.ssafy.passproject.domain.review.entity.Review;
import com.ssafy.passproject.domain.review.service.ReviewService;

// Swagger/OpenAPI 3.0 Imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "리뷰 관리", description = "핫플레이스에 대한 리뷰(평점) 등록, 조회, 수정, 삭제 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/hotplace/{placeId}/review")
@RestController
public class ReviewController {

	private final ReviewService reviewService;

	// 1. 리뷰 등록 (POST /hotplace/{placeId}/review)
	@Operation(summary = "리뷰 등록 및 평점 갱신", description = "특정 핫플레이스에 새로운 리뷰를 등록하고 해당 핫플레이스의 평균 평점을 갱신합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "리뷰 등록 성공 및 평점 갱신", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"리뷰가 성공적으로 등록되었고 평점이 갱신되었습니다.\", \"reviewId\": 1}"))),
			@ApiResponse(responseCode = "500", description = "리뷰 등록 중 서버 오류 발생", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"리뷰 등록 중 오류 발생: ...\"}")))
	})
	@PostMapping
	public ResponseEntity<Map<String, Object>> insertReview(
			@Parameter(description = "핫플레이스 ID", required = true) @PathVariable Long placeId,
			@RequestBody Review review) {

		try {
			review.setPlaceId(placeId);
			reviewService.insertReviewAndUpdateRating(review);

			Map<String, Object> response = Map.of("message", "리뷰가 성공적으로 등록되었고 평점이 갱신되었습니다.", "reviewId",
					review.getReviewId());

			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("리뷰 등록 오류: placeId={}", placeId, e);
			Map<String, Object> response = Map.of("message", "리뷰 등록 중 오류 발생: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 2. 핫플레이스별 리뷰 목록 조회 (GET /hotplace/{placeId}/review)
	@Operation(summary = "핫플레이스별 리뷰 목록 조회", description = "특정 핫플레이스에 달린 모든 리뷰 목록을 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"리뷰가 성공적으로 조회되었습니다.\", \"reviewsByPlaceId\": []}"))),
			@ApiResponse(responseCode = "500", description = "리뷰 목록 조회 중 서버 오류 발생", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"리뷰 조회 중 오류 발생: ...\"}")))
	})
	@GetMapping
	public ResponseEntity<Map<String, Object>> selectReviewsByPlaceId(
			@Parameter(description = "핫플레이스 ID", required = true) @PathVariable Long placeId) {

		try {

			List<Review> reviewsByPlaceId = reviewService.selectReviewsByPlaceId(placeId);

			Map<String, Object> response = Map.of("message", "리뷰가 성공적으로 조회되었습니다.", "reviewsByPlaceId",
					reviewsByPlaceId);

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("리뷰 목록 조회 오류: placeId={}", placeId, e);
			Map<String, Object> response = Map.of("message", "리뷰 조회 중 오류 발생: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 3. 특정 리뷰 상세 조회 (GET /hotplace/{placeId}/review/{reviewId})
	@Operation(summary = "특정 리뷰 상세 조회", description = "특정 핫플레이스의 특정 리뷰 ID에 해당하는 상세 정보를 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "리뷰 상세 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"리뷰를 찾았습니다.\", \"review\": {}}"))),
			@ApiResponse(responseCode = "404", description = "해당 ID의 리뷰를 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"해당 ID의 리뷰를 찾을 수 없습니다.\"}"))),
			@ApiResponse(responseCode = "500", description = "리뷰 상세 조회 중 서버 오류 발생", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"리뷰 조회 중 오류 발생: ...\"}")))
	})
	@GetMapping("/{reviewId}")
	public ResponseEntity<Map<String, Object>> selectReviewDetail(
			@Parameter(description = "핫플레이스 ID", required = true) @PathVariable Long placeId, // RESTful 일관성 유지를 위한 Path
			@Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId) {

		try {
			Review selectReviewDetail = reviewService.selectReviewDetail(reviewId);

			if (selectReviewDetail == null) {
				Map<String, Object> response = Map.of("message", "해당 ID의 리뷰를 찾을 수 없습니다.");
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}

			Map<String, Object> response = Map.of("message", "리뷰를 찾았습니다.", "review",
					selectReviewDetail);

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("리뷰 상세 조회 오류: reviewId={}", reviewId, e);
			Map<String, Object> response = Map.of("message", "리뷰 조회 중 오류 발생: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 4. 리뷰 수정 (PUT /hotplace/{placeId}/review/{reviewId})
	@Operation(summary = "리뷰 수정 및 평점 갱신", description = "특정 리뷰를 수정하고 해당 핫플레이스의 평균 평점을 갱신합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "리뷰 수정 성공 및 평점 갱신", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"리뷰가 성공적으로 수정되었고 평점이 갱신되었습니다.\", \"reviewId\": 1}"))),
			@ApiResponse(responseCode = "500", description = "리뷰 수정 중 서버 오류 발생", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"리뷰 수정 중 오류 발생: ...\"}")))
	})
	@PutMapping("/{reviewId}")
	public ResponseEntity<Map<String, Object>> updateReview(
			@Parameter(description = "핫플레이스 ID", required = true) @PathVariable Long placeId,
			@Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId,
			@RequestBody Review review) {

		try {
			review.setPlaceId(placeId);
			review.setReviewId(reviewId);

			reviewService.updateReviewAndUpdateRating(review);

			Map<String, Object> response = Map.of("message", "리뷰가 성공적으로 수정되었고 평점이 갱신되었습니다.", "reviewId",
					reviewId);

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (IllegalStateException e) {
			// 권한 없음
			log.warn("리뷰 수정 권한 없음: placeId={}, reviewId={}, error={}", placeId, reviewId, e.getMessage());
			Map<String, Object> response = Map.of("message", e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
		} catch (IllegalArgumentException e) {
			// 리뷰를 찾을 수 없음
			log.warn("리뷰를 찾을 수 없음: placeId={}, reviewId={}, error={}", placeId, reviewId, e.getMessage());
			Map<String, Object> response = Map.of("message", e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("리뷰 수정 중 오류 발생: placeId={}, reviewId={}", placeId, reviewId, e);
			Map<String, Object> response = Map.of("message", "리뷰 수정 중 오류 발생: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 5. 리뷰 삭제 (DELETE /hotplace/{placeId}/review/{reviewId})
	@Operation(summary = "리뷰 삭제 및 평점 갱신", description = "특정 리뷰를 삭제하고 해당 핫플레이스의 평균 평점을 갱신합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "리뷰 삭제 성공 및 평점 갱신", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"리뷰가 성공적으로 삭제되었고 평점이 갱신되었습니다.\"}"))),
			@ApiResponse(responseCode = "500", description = "리뷰 삭제 중 서버 오류 발생", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"리뷰 삭제 중 오류 발생: ...\"}")))
	})
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Map<String, Object>> deleteReview(
			@Parameter(description = "핫플레이스 ID", required = true) @PathVariable Long placeId,
			@Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId,
			@RequestBody Map<String, String> requestBody) {

		try {
			String userId = requestBody.get("userId");
			if (userId == null || userId.isEmpty()) {
				Map<String, Object> response = Map.of("message", "사용자 ID가 필요합니다.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			reviewService.deleteReviewAndUpdateRating(placeId, reviewId, userId);

			Map<String, Object> response = Map.of("message", "리뷰가 성공적으로 삭제되었고 평점이 갱신되었습니다.");

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (IllegalStateException e) {
			// 권한 없음
			log.warn("리뷰 삭제 권한 없음: placeId={}, reviewId={}, error={}", placeId, reviewId, e.getMessage());
			Map<String, Object> response = Map.of("message", e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
		} catch (IllegalArgumentException e) {
			// 리뷰를 찾을 수 없음
			log.warn("리뷰를 찾을 수 없음: placeId={}, reviewId={}, error={}", placeId, reviewId, e.getMessage());
			Map<String, Object> response = Map.of("message", e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("리뷰 삭제 중 오류 발생: placeId={}, reviewId={}", placeId, reviewId, e);
			Map<String, Object> response = Map.of("message", "리뷰 삭제 중 오류 발생: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}