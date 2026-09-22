package com.ssafy.passproject.domain.hotplace.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.locationtech.jts.geom.Point;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Hotplace {

	private Long placeId;

	private String placeName;
	private String category;
	private String address;
	private String overview;
	private String image; // 이미지 URL
	private Point pos;
	private String apiSource;
	private boolean isPetFriendly;
	private double avgRating;
	private int count; // QuickPlan에서 사용된 횟수 (통계용)

	// 생성자 (ID 미포함 - 등록용)
	public Hotplace(Long placeId, String placeName, String category, String address, String overview, Point pos,
			String apiSource, boolean isPetFriendly, double avgRating) {
		this(placeId, placeName, category, address, overview, null, pos, apiSource, isPetFriendly, avgRating, 0);
	}

	// ID만 받는 생성자 (삭제/조회용)
	public Hotplace(Long placeId) {
		this.placeId = placeId;
	}
}