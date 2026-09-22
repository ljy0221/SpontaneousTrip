package com.ssafy.passproject.domain.hotplace.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
    private String answer;
    private Long place_id;
    private String model;
    private LocalDateTime timestamp;
}
