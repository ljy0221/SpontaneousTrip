package com.ssafy.passproject.domain.hotplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryRequestDto {
    private Long place_id;
    @Builder.Default
    private int max_length = 200;
}
