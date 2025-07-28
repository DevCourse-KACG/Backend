package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberAuthResponse;
import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberLoginDto;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/members")
public class ApiV1MemberController {
    final MemberService memberService;

    @Operation(summary = "회원가입 API", description = "이메일, 비밀번호 등을 받아 회원가입을 처리합니다.")
    @PostMapping("/auth/register")
    public RsData<MemberAuthResponse> register(@Valid @RequestBody MemberDto memberDto) {
        MemberAuthResponse response = memberService.register(memberDto);

        return RsData.of(200, "회원가입 성공", response);
    };

    @Operation(summary = "로그인 API", description = "이메일과 비밀번호를 받아 로그인을 처리합니다.")
    @PostMapping("/auth/login")
    public RsData<MemberAuthResponse> login(@Valid @RequestBody MemberLoginDto memberLoginDto) {
        MemberAuthResponse response = memberService.login(memberLoginDto);

        return RsData.of(200, "로그인 성공", response);
    }
}