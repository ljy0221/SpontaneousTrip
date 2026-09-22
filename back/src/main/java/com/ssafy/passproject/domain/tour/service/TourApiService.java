package com.ssafy.passproject.domain.tour.service;

import java.util.List;

import com.ssafy.passproject.domain.tour.dto.request.TourApiRequestDto;
import com.ssafy.passproject.domain.tour.dto.request.TourDetailRequestDto;
import com.ssafy.passproject.domain.tour.dto.response.TourDetailResponseDto;
import com.ssafy.passproject.domain.tour.entity.TourPlace;

public interface TourApiService {
    // contentId 리스트만 반환
    List<String> getContentIds(TourApiRequestDto requestDto) throws Exception;
    
    // 전체 정보 반환 (필요시)
    List<TourPlace> getTourPlaces(TourApiRequestDto requestDto) throws Exception;
    
    // 공통정보 조회 (detailCommon2)
    TourDetailResponseDto getTourDetail(TourDetailRequestDto requestDto) throws Exception;
}