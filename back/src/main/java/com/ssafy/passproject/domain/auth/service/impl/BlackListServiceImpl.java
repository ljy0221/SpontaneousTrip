package com.ssafy.passproject.domain.auth.service.impl;

import com.ssafy.passproject.domain.auth.entity.BlackList;
import com.ssafy.passproject.domain.auth.repository.BlackListRepository;
import com.ssafy.passproject.domain.auth.service.BlackListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlackListServiceImpl implements BlackListService {

    private final BlackListRepository blackListRepository;

    @Override
    @Transactional
    public void addToBlackList(String accessToken, String email, String expiration) {
        BlackList blackList = BlackList.toSaveEntity(accessToken, email, expiration);

        blackListRepository.insertBlackList(blackList);

        log.info("userEmail : {}의 토큰이 BlackList에 추가되었습니다.", email);
    }

    @Override
    public boolean isBlackListed(String accessToken) {
        return blackListRepository.existsByToken(accessToken);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        blackListRepository.deleteExpiredTokens();
        log.info("만료된 토큰이 BlackList에서 삭제되었습니다.");
    }
}
