package com.ssafy.passproject.domain.tour.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TourDetailResponseDto {
    private String contentId;          // 콘텐츠 ID
    private String contentTypeId;      // 콘텐츠 타입 ID
    private String title;              // 제목
    private String createdTime;        // 등록일
    private String modifiedTime;       // 수정일
    private String tel;                // 전화번호
    private String telName;            // 전화번호명
    private String homepage;           // 홈페이지 주소
    private String firstImage;         // 대표이미지(원본)
    private String firstImage2;        // 대표이미지(썸네일)
    private String cpyrhtDivCd;        // 저작권유형
    private String areaCode;           // 지역코드
    private String sigunguCode;        // 시군구코드
    private String cat1;               // 대분류
    private String cat2;               // 중분류
    private String cat3;               // 소분류
    private String addr1;              // 주소
    private String addr2;              // 상세주소
    private String zipcode;            // 우편번호
    private String mapX;               // GPS X좌표 (경도)
    private String mapY;               // GPS Y좌표 (위도)
    private String mlevel;             // Map Level
    private String overview;           // 개요
    private String bookTour;           // 교과서 속 여행지 여부
}