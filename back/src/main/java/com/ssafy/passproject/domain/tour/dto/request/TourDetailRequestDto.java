package com.ssafy.passproject.domain.tour.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDetailRequestDto {
    // 필수 파라미터
    private String contentId;          // 콘텐츠 ID
    
    @Builder.Default
    private String numOfRows = "10";   // 한 페이지 결과 수 (필수)
    
    @Builder.Default
    private String pageNo = "1";       // 페이지 번호 (필수)
}