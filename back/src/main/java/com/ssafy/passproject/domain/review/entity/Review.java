package com.ssafy.passproject.domain.review.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
	private Long reviewId; // 리뷰 고유 ID (PK)
	private Long placeId; // 이 리뷰가 달린 핫플레이스 ID (FK)
	private String userId; // 리뷰 작성자 ID (FK)
	private String nickname; // 리뷰 작성자 닉네임 (JOIN으로 가져옴)

	private double rating;
	private String content;
	private LocalDateTime createdAt;
	private int timeSuitablityScore;
}
