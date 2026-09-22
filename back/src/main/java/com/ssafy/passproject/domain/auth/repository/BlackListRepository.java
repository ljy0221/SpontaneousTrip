package com.ssafy.passproject.domain.auth.repository;

import com.ssafy.passproject.domain.auth.entity.BlackList;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface BlackListRepository {
    /**
     * BlackList에 토큰 추가
     */
    void insertBlackList(BlackList blackList);

    /**
     * 토큰이 BlackList에 존재하는지 확인
     */
    boolean existsByToken(String token);

    /**
     * 토큰으로 BlackList 조회
     */
    Optional<BlackList> findByToken(String token);

    /**
     * 만료된 토큰 삭제 (배치 작업용)
     */
    void deleteExpiredTokens();
}
