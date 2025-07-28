package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberAuthResponse;
import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberLoginDto;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/members")
public class ApiV1MemberController {
    final MemberService memberService;

    @Operation(summary = "회원가입 API", description = "이메일, 비밀번호 등을 받아 회원가입을 처리합니다.")
    @PostMapping("/auth/register")
    public RsData<MemberAuthResponse> register(@Valid @RequestBody MemberDto memberDto, HttpServletResponse response) {
        MemberAuthResponse memberAuthResponse = memberService.register(memberDto);

        Cookie accessTokenCookie = new Cookie("accessToken", memberAuthResponse.accessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60 * 24);

        response.addCookie(accessTokenCookie);

        return RsData.of(200, "회원가입 성공", memberAuthResponse);
    };

    @Operation(summary = "로그인 API", description = "이메일과 비밀번호를 받아 로그인을 처리합니다.")
    @PostMapping("/auth/login")
    public RsData<MemberAuthResponse> login(@Valid @RequestBody MemberLoginDto memberLoginDto, HttpServletResponse response) {
        MemberAuthResponse memberAuthResponse = memberService.login(memberLoginDto);

        Cookie accessTokenCookie = new Cookie("accessToken", memberAuthResponse.accessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60 * 24);

        response.addCookie(accessTokenCookie);

        return RsData.of(200, "로그인 성공", memberAuthResponse);
    }

    @Operation(summary = "로그아웃 API", description = "로그아웃 처리 API입니다.")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/auth/logout")
    public RsData<MemberAuthResponse> logout(HttpServletResponse response) {
        Cookie deleteCookie = new Cookie("accessToken", "");
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(true);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);

        response.addCookie(deleteCookie);

        return RsData.of(200, "로그아웃 성공");
    }
}