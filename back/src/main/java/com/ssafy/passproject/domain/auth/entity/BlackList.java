package com.ssafy.passproject.domain.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlackList {

    private int id;
    private String email;
    private String accessToken;
    private String expiration;

    public BlackList(String username, String refreshToken, Date expiration) {
        this.email = username;
        this.accessToken = refreshToken;
        this.expiration = expiration.toString();
    }

    public static BlackList toSaveEntity(String accessToken, String email, String expiration) {
        return BlackList.builder()
                .email(email)
                .accessToken(accessToken)
                .expiration(expiration)
                .build();
    }
}
