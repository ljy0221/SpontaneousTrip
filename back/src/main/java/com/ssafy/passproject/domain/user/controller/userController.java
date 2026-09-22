package com.ssafy.passproject.domain.user.controller;

import com.ssafy.passproject.domain.auth.dto.security.CustomUserDetails;
import com.ssafy.passproject.domain.user.dto.request.UserDeleteRequest;
import com.ssafy.passproject.domain.user.dto.request.UserUpdateRequest;
import com.ssafy.passproject.domain.user.dto.request.userRegistRequest;
import com.ssafy.passproject.domain.user.dto.response.UserResponse;
import com.ssafy.passproject.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "사용자 정보 관리 엔드포인트 (회원가입, 조회, 수정, 탈퇴)")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class userController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다. lastLogin 필드를 통해 이전 로그인 시간을 확인할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"userId\": \"user1\", \"email\": \"user1@test.com\", \"nickname\": \"닉네임\"}"))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증되지 않은 사용자입니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"내 정보 조회 실패\"}")))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails.getUsername();
        log.info("마이페이지 요청 : {}", email);
        UserResponse response = userService.findByEmail(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 이메일, 비밀번호, 닉네임이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "가입 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"userId\": \"user1\", \"email\": \"user1@test.com\", \"nickname\": \"닉네임\"}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"잘못된 요청입니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"회원가입 실패\"}")))
    })
    @PostMapping("/me")
    public ResponseEntity<UserResponse> regist(@RequestBody userRegistRequest request) {
        log.info("회원가입 요청: {}", request.getEmail());
        UserResponse response = userService.regist(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다. 닉네임과 비밀번호를 변경할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"userId\": \"user1\", \"email\": \"user1@test.com\", \"nickname\": \"변경된닉네임\"}"))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증되지 않은 사용자입니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"회원정보 수정 실패\"}")))
    })
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserUpdateRequest request) {
        String email = userDetails.getUsername();
        log.info("회원정보 수정 요청: {}", email);
        UserResponse response = userService.updateUser(email, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다. 비밀번호 확인이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"회원탈퇴 성공\"}"))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증되지 않은 사용자입니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"회원탈퇴 실패\"}")))
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserDeleteRequest request) {
        String email = userDetails.getUsername();
        log.info("회원탈퇴 요청: {}", email);
        userService.deleteUser(email, request);
        return ResponseEntity.noContent().build();
    }
}
