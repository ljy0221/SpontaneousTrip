package com.ssafy.passproject.domain.auth.controller;

import com.ssafy.passproject.domain.auth.dto.request.AuthLoginRequest;
import com.ssafy.passproject.domain.auth.dto.response.AuthLoginResponse;
import com.ssafy.passproject.domain.auth.dto.response.AuthLogoutResponse;
import com.ssafy.passproject.domain.auth.dto.response.RefreshTokenResponse;
import com.ssafy.passproject.domain.auth.service.AuthService;
import com.ssafy.passproject.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "사용자 인증 관련 엔드포인트 (로그인, 로그아웃, 토큰 갱신)")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final com.ssafy.passproject.domain.user.service.UserService userService;
    // private final ServletResponse servletResponse; // Unused

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다. AccessToken은 응답 body에, RefreshToken은 쿠키에 저장됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiJ9...\", \"refreshToken\": \"eyJhbGciOiJIUzI1NiJ9...\"}"))),
            @ApiResponse(responseCode = "401", description = "로그인 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"로그인 실패\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> login(@RequestBody AuthLoginRequest loginRequest,
            HttpServletResponse response) {
        try {
            AuthLoginResponse result = authService.login(loginRequest, response);
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthLoginResponse("로그인 실패"));
        }
    }

    @Operation(summary = "로그아웃", description = "RefreshToken을 무효화하고 BlackList에 추가하여 로그아웃합니다. lastLogin이 현재 시간으로 업데이트됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"로그아웃 성공\"}"))),
            @ApiResponse(responseCode = "400", description = "로그아웃 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"로그아웃 실패\"}")))
    })
    @PostMapping("/logout")
    public ResponseEntity<AuthLogoutResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 쿠키에서 RefreshToken 추출
            String refreshToken = CookieUtil.getRefreshTokenFromCookies(request.getCookies());

            // 로그아웃 처리
            AuthLogoutResponse result = authService.logout(refreshToken);

            // RefreshToken 쿠키 삭제
            response.addCookie(CookieUtil.deleteRefreshTokenCookie());

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("로그아웃 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthLogoutResponse("로그아웃 실패"));
        }
    }

    @Operation(summary = "AccessToken 재발급", description = "RefreshToken을 사용하여 새로운 AccessToken을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiJ9...\"}"))),
            @ApiResponse(responseCode = "401", description = "재발급 실패 (유효하지 않은 토큰)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"토큰 재발급 실패\"}")))
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(HttpServletRequest request) {
        try {
            // 쿠키에서 RefreshToken 추출
            String refreshToken = CookieUtil.getRefreshTokenFromCookies(request.getCookies());

            // AccessToken 재발급
            RefreshTokenResponse result = authService.refreshAccessToken(refreshToken);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("토큰 재발급 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @Operation(summary = "비밀번호 찾기", description = "이메일과 이름(닉네임)이 일치하는 사용자가 있으면 임시 비밀번호를 이메일로 발송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발송 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"임시 비밀번호가 이메일로 전송되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "발송 실패 (정보 불일치 등)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"정보가 일치하지 않습니다.\"}")))
    })
    @PostMapping("/find-password")
    public ResponseEntity<com.ssafy.passproject.domain.auth.dto.response.AuthLogoutResponse> findPassword(
            @RequestBody com.ssafy.passproject.domain.auth.dto.request.FindPasswordRequest request) {
        try {
            userService.findPassword(request.getEmail(), request.getName());
            return ResponseEntity.ok(
                    new com.ssafy.passproject.domain.auth.dto.response.AuthLogoutResponse("임시 비밀번호가 이메일로 전송되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new com.ssafy.passproject.domain.auth.dto.response.AuthLogoutResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.ssafy.passproject.domain.auth.dto.response.AuthLogoutResponse("메일 전송 중 오류가 났습니다."));
        }
    }
}
