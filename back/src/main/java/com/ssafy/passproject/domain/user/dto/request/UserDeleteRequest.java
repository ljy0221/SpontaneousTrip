package com.ssafy.passproject.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDeleteRequest {
    private String password; // 탈퇴 확인용 비밀번호
}
