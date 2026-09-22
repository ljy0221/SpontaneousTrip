package com.ssafy.passproject.domain.user.dto.request;

import lombok.Data;

@Data
public class userRegistRequest {
    private String email;
    private String password;
    private String nickname;
}
