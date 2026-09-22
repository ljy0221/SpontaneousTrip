package com.ssafy.passproject.domain.auth.service;

import com.ssafy.passproject.domain.auth.dto.request.AuthLoginRequest;
import com.ssafy.passproject.domain.auth.dto.response.AuthLoginResponse;
import com.ssafy.passproject.domain.auth.dto.response.AuthLogoutResponse;
import com.ssafy.passproject.domain.auth.dto.response.RefreshTokenResponse;
import com.ssafy.passproject.domain.auth.entity.TokenPair;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthLoginResponse login(AuthLoginRequest loginRequest, HttpServletResponse response);

    TokenPair createTokens(Authentication authentication);

    AuthLogoutResponse logout(String refreshToken);

    RefreshTokenResponse refreshAccessToken(String refreshToken);
}
