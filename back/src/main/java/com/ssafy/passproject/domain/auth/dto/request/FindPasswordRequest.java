package com.ssafy.passproject.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
@Schema(description = "비밀번호 찾기 요청 DTO")
public class FindPasswordRequest {

  @Schema(description = "이메일", example = "user@example.com")
  private String email;

  @Schema(description = "이름(닉네임)", example = "닉네임")
  private String name;
}
