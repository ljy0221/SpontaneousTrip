package com.ssafy.passproject.domain.hotplace.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.ssafy.passproject.domain.hotplace.entity.Hotplace;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HotplaceResponseDto {

    private Long placeId;
    private String placeName;
    private String category;
    private String address;
    private String overview;
    private String image;

    // 클라이언트 통신을 위한 분리된 double 값
    private double latitude;
    private double longitude;

    private boolean isPetFriendly;
    private double avgRating;
    private int count; // QuickPlan에서 사용된 횟수 (통계)

    // Entity를 DTO로 변환하는 생성자 (핵심 변환 로직)
    public HotplaceResponseDto(Hotplace entity) {
        this.placeId = entity.getPlaceId();
        this.placeName = entity.getPlaceName();
        this.category = entity.getCategory();
        this.address = entity.getAddress();
        this.overview = entity.getOverview();
        this.image = entity.getImage();

        // Point 객체에서 경도/위도 추출 (JTS Point의 표준 메서드 사용)
        if (entity.getPos() != null) {
            this.latitude = entity.getPos().getY(); // 위도
            this.longitude = entity.getPos().getX(); // 경도
        }

        this.isPetFriendly = entity.isPetFriendly();
        this.avgRating = entity.getAvgRating();
        this.count = entity.getCount();
    }
}