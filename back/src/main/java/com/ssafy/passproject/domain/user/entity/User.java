package com.ssafy.passproject.domain.user.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private int userId;
    private String email;
    private String password;
    private String nickname;
    private String role;
    private LocalDateTime joinDate;
    private LocalDateTime lastLogin;

    public static User toRequestToken(String email, String role) {
        return User.builder()
                .email(email)
                .role(role)
                .build();
    }

    public static User toRequestToken(String email, String nickname, String role) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .role(role)
                .build();
    }
}