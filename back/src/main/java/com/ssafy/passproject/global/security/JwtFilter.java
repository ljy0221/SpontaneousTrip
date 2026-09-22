package com.ssafy.passproject.global.security;

import com.ssafy.passproject.domain.auth.dto.security.CustomUserDetails;
import com.ssafy.passproject.domain.auth.service.BlackListService;
import com.ssafy.passproject.domain.user.entity.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * jwt 검증 필터
 */
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final BlackListService blackListService;

    // 모든 HTTP 요청에서 JWT 토큰을 검사하고, 유효하면 인증 정보를 SecurityContext에 등록하는 필터
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 헤더에서 access키에 담긴 토큰을 꺼낸다.
        String accessToken = request.getHeader("access");

        // 토큰이 없다면 현재 필터 종료
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 만료 여부 확인, 만료 시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (JwtException e) {
            // response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            // response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // BlackList에 있는 토큰인지 확인
        if (blackListService.isBlackListed(accessToken)) {

            // response body
            PrintWriter writer = response.getWriter();
            writer.print("블랙리스트에 등록된 토큰");

            // response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // username, nickname, role 값을 등록
        String username = jwtUtil.getUsername(accessToken);
        String nickname = jwtUtil.getNickname(accessToken);
        String role = jwtUtil.getRole(accessToken);

        User user = User.toRequestToken(username, nickname, role);

        // UserDetail에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // Security 인증 Token generate
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());

        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
