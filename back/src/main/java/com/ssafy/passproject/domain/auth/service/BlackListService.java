package com.ssafy.passproject.domain.auth.service;

public interface BlackListService {

    void addToBlackList(String accessToken, String email, String expiration);
    boolean isBlackListed(String accessToken);
    void deleteExpiredTokens();
}
