package com.ssafy.passproject.domain.auth.entity;

import com.ssafy.passproject.domain.auth.dto.response.RefreshResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refresh {

    private Long id;
    private String email;
    private String refreshToken;
    private String expiration;

    public static Refresh toSaveEntity(RefreshResponse response, String expiration){
        return Refresh.builder()
                .email(response.getEmail())
                .refreshToken(response.getRefresh())
                .expiration(expiration)
                .build();
    }
}
