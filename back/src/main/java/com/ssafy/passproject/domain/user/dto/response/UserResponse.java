package com.ssafy.passproject.domain.user.dto.response;

import com.ssafy.passproject.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private int userId;
    private String email;
    private String nickname;
    private String role;
    private LocalDateTime joinDate;
    private LocalDateTime lastLogin;

    public UserResponse(int userId, String email, String nickname) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
    }

    public static UserResponse entityToDto(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .joinDate(user.getJoinDate())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
