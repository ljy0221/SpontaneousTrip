package com.ssafy.passproject.domain.review.service;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.passproject.domain.review.entity.Review;
import com.ssafy.passproject.domain.review.repository.ReviewRepository;
import com.ssafy.passproject.domain.hotplace.repository.HotplaceRepository;

/**
 * 리뷰 관련 비즈니스 로직을 처리하는 서비스입니다.
 * Hotplace 평점 갱신 로직을 트랜잭션으로 통합하여 데이터 일관성을 유지합니다.
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    // 리뷰 DAO (MyBatis Repository)
    private final ReviewRepository reviewRepository;
    // 핫플레이스 DAO (평점 업데이트를 위해 주입)
    private final HotplaceRepository hotplaceRepository;

    // ** 1. 리뷰 등록 및 평점 업데이트 (POST 요청 처리) **
    @Transactional
    public void insertReviewAndUpdateRating(Review review) throws Exception {
        // 1. 리뷰 등록 (PK가 review 객체에 다시 할당됩니다)
        reviewRepository.insertReview(review);

        // 2. 해당 Hotplace의 평균 평점 업데이트
        hotplaceRepository.updateHotplaceAvgRating(review.getPlaceId());
    }

    // ** 2. 특정 핫플레이스의 리뷰 목록 조회 (GET /review) **
    public List<Review> selectReviewsByPlaceId(Long placeId) throws Exception {
        return reviewRepository.selectReviewsByPlaceId(placeId);
    }

    // ** 3. 특정 리뷰 상세 조회 (GET /review/{reviewId}) **
    public Review selectReviewDetail(Long reviewId) throws Exception {
        return reviewRepository.selectReviewDetail(reviewId);
    }

    // ** 4. 리뷰 수정 및 평점 업데이트 (PUT 요청 처리) **
    @Transactional
    public void updateReviewAndUpdateRating(Review review) throws Exception {
        // 1. 기존 리뷰 조회하여 작성자 확인
        Review existingReview = reviewRepository.selectReviewDetail(review.getReviewId());
        if (existingReview == null) {
            throw new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다.");
        }

        // 2. 작성자 본인인지 확인
        if (!existingReview.getUserId().equals(review.getUserId())) {
            throw new IllegalStateException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        // 3. 리뷰 데이터 수정
        reviewRepository.updateReview(review);

        // 4. Hotplace 평균 평점 업데이트
        hotplaceRepository.updateHotplaceAvgRating(review.getPlaceId());
    }

    // ** 5. 리뷰 삭제 및 평점 업데이트 (DELETE 요청 처리) **
    @Transactional
    public void deleteReviewAndUpdateRating(Long placeId, Long reviewId, String userId) throws Exception {
        // 1. 기존 리뷰 조회하여 작성자 확인
        Review existingReview = reviewRepository.selectReviewDetail(reviewId);
        if (existingReview == null) {
            throw new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다.");
        }

        // 2. 작성자 본인인지 확인
        if (!existingReview.getUserId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        // 3. 리뷰 삭제
        reviewRepository.deleteReview(reviewId, userId);

        // 4. 해당 Hotplace의 평균 평점 업데이트
        hotplaceRepository.updateHotplaceAvgRating(placeId);
    }
}