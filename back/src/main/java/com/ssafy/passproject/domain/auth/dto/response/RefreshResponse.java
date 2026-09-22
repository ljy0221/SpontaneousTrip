package com.ssafy.passproject.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshResponse {
    private String email;
    private String refresh;
    private Long expiredMs;
}
