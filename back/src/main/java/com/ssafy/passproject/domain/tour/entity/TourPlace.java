package com.ssafy.passproject.domain.tour.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TourPlace {
    private String contentId;
    private String contentTypeId;
    private String title;
    private String addr1;
    private String addr2;
    private String mapX;  // 경도
    private String mapY;  // 위도
    private String firstImage;
    private String firstImage2;
    private String tel;
    private Double dist;  // 거리
}