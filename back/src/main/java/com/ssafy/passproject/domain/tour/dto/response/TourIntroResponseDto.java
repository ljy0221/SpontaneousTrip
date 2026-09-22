package com.ssafy.passproject.domain.tour.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourIntroResponseDto {
    // 관광지 기본 정보
    private String contentId;
    private String title;
    private String overview;
    private String addr1;
    private String addr2;
    private String firstImage;
    private String firstImage2;
    private String tel;
    private String homepage;

    // AI 생성 소개 문구
    private String aiIntroduction;
}