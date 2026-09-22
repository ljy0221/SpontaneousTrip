package com.ssafy.passproject.domain.auth.service.impl;

import com.ssafy.passproject.domain.auth.dto.response.RefreshResponse;
import com.ssafy.passproject.domain.auth.entity.Refresh;
import com.ssafy.passproject.domain.auth.repository.RefreshRepository;
import com.ssafy.passproject.domain.auth.service.RefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class RefreshServiceImpl implements RefreshService {

    private final RefreshRepository refreshRepository;

    @Override
    public void addRefreshToken(RefreshResponse response) {
        Date date = new Date(System.currentTimeMillis() + response.getExpiredMs());

        Refresh refresh = Refresh.toSaveEntity(response, date.toString());

        refreshRepository.insertRefresh(refresh);
    }

    @Override
    public boolean existsByRefreshToken(String refreshToken) {
        return refreshRepository.existsByRefresh(refreshToken);
    }

    @Override
    public void deleteByRefreshToken(String refreshToken) {
        refreshRepository.deleteByRefresh(refreshToken);
    }
}
