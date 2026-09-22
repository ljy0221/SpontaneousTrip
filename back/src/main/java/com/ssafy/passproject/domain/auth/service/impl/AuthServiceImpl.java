package com.ssafy.passproject.domain.auth.service.impl;

import com.ssafy.passproject.domain.auth.dto.request.AuthLoginRequest;
import com.ssafy.passproject.domain.auth.dto.response.AuthLoginResponse;
import com.ssafy.passproject.domain.auth.dto.response.AuthLogoutResponse;
import com.ssafy.passproject.domain.auth.dto.response.RefreshResponse;
import com.ssafy.passproject.domain.auth.dto.response.RefreshTokenResponse;
import com.ssafy.passproject.domain.auth.entity.BlackList;
import com.ssafy.passproject.domain.auth.entity.TokenPair;
import com.ssafy.passproject.domain.auth.repository.BlackListRepository;
import com.ssafy.passproject.domain.auth.repository.RefreshRepository;
import com.ssafy.passproject.domain.auth.service.AuthService;
import com.ssafy.passproject.domain.auth.service.BlackListService;
import com.ssafy.passproject.domain.auth.service.RefreshService;
import com.ssafy.passproject.domain.user.entity.User;
import com.ssafy.passproject.domain.user.repository.UserRepository;
import com.ssafy.passproject.global.security.JwtUtil;
import com.ssafy.passproject.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshService refreshService;
    private final BlackListService blackListService;
    private final RefreshRepository refreshRepository;
    private final BlackListRepository blackListRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AuthLoginResponse login(AuthLoginRequest loginRequest, HttpServletResponse response) {
        try {
            // 인증 토큰 생성
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword(),
                    null);

            // 인증 처리
            Authentication authentication = authenticationManager.authenticate(authToken);

            // 최초 로그인인 경우에만 lastLogin 업데이트
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            if (user.getLastLogin() == null) {
                userRepository.updateLastLogin(loginRequest.getEmail());
            }

            // 토큰 생성
            TokenPair tokenPair = createTokens(authentication);

            // 쿠키에 refresh Token 추가
            response.addCookie(CookieUtil.createRefreshTokenCookie(tokenPair.getRefreshToken()));

            log.info("로그인 성공 : {}", loginRequest.getEmail());
            // AccessToken을 응답 body에 포함
            return new AuthLoginResponse("로그인 성공", tokenPair.getAccessToken());
        } catch (AuthenticationException e) {
            log.error("로그인 실패 : {}", loginRequest.getEmail());
            throw e;
        }
    }

    @Override
    public TokenPair createTokens(Authentication authentication) {
        String username = authentication.getName();

        // role 가져오기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // 닉네임 가져오기
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String nickname = user.getNickname();

        // 토큰 생성
        String accessToken = jwtUtil.createJwt("access", username, nickname, role, 600000L); // 10분
        String refreshToken = jwtUtil.createJwt("refresh", username, nickname, role, 86400000L); // 24시간

        // refresh 토큰 저장
        RefreshResponse response = new RefreshResponse(username, refreshToken, 86400000L);
        refreshService.addRefreshToken(response);

        log.info("JWT 토큰 생성 완료: {}, {}", username, nickname);
        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public AuthLogoutResponse logout(String refreshToken) {
        // refresh 토큰 검증
        if (refreshToken.isEmpty() || refreshToken == null) {
            throw new IllegalArgumentException("RefreshToken이 없습니다.");
        }

        // 토큰 만료 확인
        if (jwtUtil.isExpired(refreshToken)) {
            throw new IllegalArgumentException("RefreshToken이 만료되었습니다.");
        }

        // 토큰 카테고리 확인
        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh")) {
            throw new IllegalArgumentException("RefreshToken이 아닙니다.");
        }

        // RefreshToken이 DB 저장되어 있는지 확인
        boolean check = refreshRepository.existsByRefresh(refreshToken);
        if (!check) {
            throw new IllegalArgumentException("RefreshToken이 유효하지 않습니다.");
        }

        // RefreshToken DB에서 제거
        refreshRepository.deleteByRefresh(refreshToken);

        // BlackList에 추가
        String username = jwtUtil.getUsername(refreshToken);
        Date expiration = jwtUtil.getExpiration(refreshToken);

        BlackList blackList = new BlackList(username, refreshToken, expiration);
        blackListRepository.insertBlackList(blackList);

        // lastLogin 업데이트 (로그아웃 시점 기록)
        userRepository.updateLastLogin(username);

        log.info("로그아웃 완료 : {}", username);
        return new AuthLogoutResponse("로그아웃 완료");
    }

    @Override
    public RefreshTokenResponse refreshAccessToken(String refreshToken) {
        // refresh 토큰 검증
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("RefreshToken이 없습니다.");
        }

        // 토큰 만료 확인
        if (jwtUtil.isExpired(refreshToken)) {
            throw new IllegalArgumentException("RefreshToken이 만료되었습니다.");
        }

        // 토큰 카테고리 확인
        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh")) {
            throw new IllegalArgumentException("RefreshToken이 아닙니다.");
        }

        // RefreshToken이 DB에 저장되어 있는지 확인
        boolean check = refreshRepository.existsByRefresh(refreshToken);
        if (!check) {
            throw new IllegalArgumentException("RefreshToken이 유효하지 않습니다.");
        }

        // 새로운 AccessToken 생성
        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);
        String nickname = jwtUtil.getNickname(refreshToken);
        String newAccessToken = jwtUtil.createJwt("access", username, nickname, role, 600000L); // 10분

        log.info("AccessToken 재발급 완료 : {}", username);
        return new RefreshTokenResponse(newAccessToken);
    }
}
