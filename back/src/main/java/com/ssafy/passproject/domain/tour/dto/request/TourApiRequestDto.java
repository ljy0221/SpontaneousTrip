package com.ssafy.passproject.domain.tour.dto.request;

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
public class TourApiRequestDto {
    private String mapX; // 경도 (필수)
    private String mapY; // 위도 (필수)
    private String radius; // 거리 (미터 단위, 기본값: 1000)
    private String contentTypeId; // 관광타입 (12:관광지, 14:문화시설, 15:축제공연행사, 25:여행코스, 28:레포츠, 32:숙박, 38:쇼핑, 39:음식점)
    private String areaCode; // 지역 코드

    // API 기본 파라미터
    @Builder.Default
    private String numOfRows = "10"; // 한 페이지 결과 수
    @Builder.Default
    private String pageNo = "1"; // 페이지 번호
    @Builder.Default
    private String arrange = "E"; // 정렬 (A:제목순, B:조회순, C:수정일순, D:생성일순, E:거리순)
    @Builder.Default
    private String mobileOS = "ETC";
    @Builder.Default
    private String mobileApp = "AppTest";
}