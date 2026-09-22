package com.ssafy.passproject.domain.review.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.passproject.domain.review.entity.Review;

@Mapper
public interface ReviewRepository {

	public void insertReview(Review review) throws Exception;

	public Review selectReviewDetail(Long reviewId) throws Exception;

	public List<Review> selectReviewsByPlaceId(Long placeId) throws Exception;

	public void updateReview(Review review) throws Exception;

	public void deleteReview(@Param("reviewId") Long reviewId, @Param("userId") String userId) throws Exception;
}