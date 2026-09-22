package com.ssafy.passproject.global.config;

import com.ssafy.passproject.domain.auth.service.BlackListService;
import com.ssafy.passproject.global.security.JwtFilter;
import com.ssafy.passproject.global.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity // 메서드 레벨 보안 활성화
public class SecurityConfig {

	private final JwtUtil jwtUtil;
	private final BlackListService blackListService;

	// AuthenticationManager를 Bean으로 등록 (AuthService에서 사용)
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.addAllowedOriginPattern("*");
		configuration.addAllowedHeader("*");
		configuration.addAllowedMethod("*");
		configuration.setAllowCredentials(true);
		configuration.addExposedHeader("access");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		// csrf disable
		http.csrf((auth) -> auth.disable());

		http.cors((auth) -> auth.configurationSource(corsConfigurationSource()));

		// FormLogin 방식 disable -> formLogin은 세션 방식이기 때문에 jwt 방식에선 사용하지 않는다.
		http.formLogin((auth) -> auth.disable());

		// 경로 별 인가 작업
		http.authorizeHttpRequests((auth) -> auth
				.requestMatchers("/auth/**", "/user/me").permitAll() // 로그인, 회원가입은 인증 불필요
				.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**",
						"/webjars/**")
				.permitAll().requestMatchers("/tour/**", "/weather/**", "/hotplace/search", "/hotplace/popular").permitAll()
                .requestMatchers(HttpMethod.GET, "/notice/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/notice").permitAll()
				.anyRequest().authenticated()); // 나머지는 인증필요


		// JwtFilter 등록 - UsernamePasswordAuthenticationFilter 이전에 실행
		http.addFilterBefore(new JwtFilter(jwtUtil, blackListService), UsernamePasswordAuthenticationFilter.class);

		// 세션 설정
		http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}
}
