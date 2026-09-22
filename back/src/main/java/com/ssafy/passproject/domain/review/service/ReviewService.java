package com.ssafy.passproject.domain.review.service;

import java.util.List;

import com.ssafy.passproject.domain.hotplace.entity.Hotplace;
import com.ssafy.passproject.domain.review.entity.Review;

public interface ReviewService {
	public void insertReviewAndUpdateRating(Review review) throws Exception;

	public Review selectReviewDetail(Long reviewId) throws Exception;

	public List<Review> selectReviewsByPlaceId(Long placeId) throws Exception;

	public void updateReviewAndUpdateRating(Review review) throws Exception;

	public void deleteReviewAndUpdateRating(Long placeId, Long reviewId, String userId) throws Exception;
}
