package com.ssafy.passproject.domain.auth.dto.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AuthLoginRequest {
    private String email;
    private String password;
}
