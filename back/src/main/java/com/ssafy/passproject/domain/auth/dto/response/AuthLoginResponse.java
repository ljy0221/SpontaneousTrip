package com.ssafy.passproject.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginResponse {
    private String message;
    private String accessToken; // AccessToken 포함

    // message만 받는 생성자 (실패 응답용)
    public AuthLoginResponse(String message) {
        this.message = message;
        this.accessToken = null;
    }
}
