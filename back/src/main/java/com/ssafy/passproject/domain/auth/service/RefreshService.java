package com.ssafy.passproject.domain.auth.service;

import com.ssafy.passproject.domain.auth.dto.response.RefreshResponse;

public interface RefreshService {
    void addRefreshToken(RefreshResponse response);
    boolean existsByRefreshToken(String refreshToken);
    void deleteByRefreshToken(String refreshToken);
}
