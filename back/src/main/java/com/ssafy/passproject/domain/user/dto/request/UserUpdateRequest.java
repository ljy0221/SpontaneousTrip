package com.ssafy.passproject.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String nickname; // 변경할 닉네임 (선택)
    private String currentPassword; // 현재 비밀번호 (비밀번호 변경 시 필수)
    private String newPassword; // 새 비밀번호 (선택)
}
