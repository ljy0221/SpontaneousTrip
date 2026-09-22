package com.ssafy.passproject.domain.hotplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.ssafy.passproject.domain.hotplace.dto.response.HotplaceResponseDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDto {
    private String query;
    private Long place_id;
    private List<HotplaceResponseDto> candidates;
    @Builder.Default
    private int max_tokens = 500;
    @Builder.Default
    private double temperature = 0.7;
}
