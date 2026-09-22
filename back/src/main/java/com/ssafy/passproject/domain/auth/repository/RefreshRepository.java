package com.ssafy.passproject.domain.auth.repository;

import com.ssafy.passproject.domain.auth.entity.Refresh;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface RefreshRepository {
    /**
     * Refresh 토큰 존재 여부 확인
     */
    boolean existsByRefresh(String refresh);

    /**
     * Refresh 토큰으로 조회
     */
    Optional<Refresh> findByRefresh(String refresh);

    /**
     * Refresh 토큰 저장
     */
    void insertRefresh(Refresh refresh);

    /**
     * Refresh 토큰 삭제
     */
    void deleteByRefresh(String refresh);
}
