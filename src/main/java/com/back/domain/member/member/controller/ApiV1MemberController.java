package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberRegisterResponse;
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
    public RsData<MemberRegisterResponse> register(@Valid @RequestBody MemberDto memberDto) {
        MemberRegisterResponse response = memberService.register(memberDto);

        return RsData.of(200, "회원가입 성공", response);
    };
}