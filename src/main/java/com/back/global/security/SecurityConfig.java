package com.back.global.security;

import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    //private final CustomAuthenticationFilter customAuthenticationFilter;
    private final Optional<MockAuthFilterForSpecificApi> mockAuthFilterForSpecificApi;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/favicon.ico").permitAll() // 파비콘 접근 허용 (검색 엔진 최적화)
                        .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 접근 허용
                        .requestMatchers("/api/v1/members/join", "/api/v1/members/login").permitAll() // 회원가입, 로그인 허용
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (API 서버에서는 일반적으로 비활성화)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                //.addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(
                        headers -> headers
                                .frameOptions(
                                        HeadersConfigurer.FrameOptionsConfig::sameOrigin
                                )
                )
                .exceptionHandling(
                        exceptionHandling -> exceptionHandling
                                .authenticationEntryPoint(
                                        (request, response, authException) -> {
                                            response.setContentType("application/json;charset=UTF-8");

                                            response.setStatus(401);
                                            response.getWriter().write(
                                                    Ut.json.toString(
                                                            RsData.of(
                                                                    401,
                                                                    "로그인 후 이용해주세요."
                                                            )

                                                    )
                                            );
                                        }
                                )
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) -> {
                                            response.setContentType("application/json;charset=UTF-8");

                                            response.setStatus(403);
                                            response.getWriter().write(
                                                    Ut.json.toString(
                                                            RsData.of(
                                                                    403,
                                                                    "권한이 없습니다."
                                                            )
                                                    )
                                            );
                                        }
                                )
                );

        // Profile test 일때 Mock 인증 필터를 특정 API에만 적용
        mockAuthFilterForSpecificApi.ifPresent(filter ->
                http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        );

        return http.build();

    }

}
