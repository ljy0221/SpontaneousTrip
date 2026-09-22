package com.ssafy.passproject.global.util;

import jakarta.servlet.http.Cookie;

/**
 * 쿠키 생성 및 관리를 위한 유틸리티 클래스
 */
public class CookieUtil {

    private CookieUtil() {
        // 유틸리티 클래스는 인스턴스화 방지
        throw new IllegalStateException("Utility class");
    }

    /**
     * 쿠키 생성
     *
     * @param key 쿠키 이름
     * @param value 쿠키 값
     * @param maxAge 쿠키 유효 시간 (초)
     * @return 생성된 쿠키
     */
    public static Cookie createCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        // cookie.setSecure(true); // HTTPS 사용 시 활성화
        return cookie;
    }

    /**
     * 기본 유효시간(24시간)으로 쿠키 생성
     *
     * @param key 쿠키 이름
     * @param value 쿠키 값
     * @return 생성된 쿠키
     */
    public static Cookie createCookie(String key, String value) {
        return createCookie(key, value, 24 * 60 * 60); // 24시간
    }

    /**
     * 쿠키 삭제 (MaxAge를 0으로 설정)
     *
     * @param key 삭제할 쿠키 이름
     * @return 삭제용 쿠키
     */
    public static Cookie deleteCookie(String key) {
        return createCookie(key, null, 0);
    }

    /**
     * Refresh 토큰 쿠키 생성 (24시간 유효)
     *
     * @param refreshToken Refresh 토큰 값
     * @return Refresh 토큰 쿠키
     */
    public static Cookie createRefreshTokenCookie(String refreshToken) {
        return createCookie("refresh", refreshToken, 24 * 60 * 60);
    }

    /**
     * Refresh 토큰 쿠키 삭제
     *
     * @return 삭제용 쿠키
     */
    public static Cookie deleteRefreshTokenCookie() {
        return deleteCookie("refresh");
    }

    /**
     * 쿠키 배열에서 Refresh 토큰 추출
     *
     * @param cookies 쿠키 배열
     * @return Refresh 토큰 값, 없으면 null
     */
    public static String getRefreshTokenFromCookies(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
        
        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        
        return null;
    }
}
