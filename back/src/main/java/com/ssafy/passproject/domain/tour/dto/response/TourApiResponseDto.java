package com.ssafy.passproject.domain.tour.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TourApiResponseDto {
    private List<String> contentIds;
    private int totalCount;
}