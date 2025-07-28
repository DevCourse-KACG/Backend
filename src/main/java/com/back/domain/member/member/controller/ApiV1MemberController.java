package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberRegisterResponse;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rsData.RsData;
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

    @PostMapping("/auth/register")
    public RsData<MemberRegisterResponse> register(@Valid @RequestBody MemberDto memberDto) {
        MemberRegisterResponse response = memberService.register(memberDto);

        return RsData.of(200, "회원가입 성공", response);
    };
}