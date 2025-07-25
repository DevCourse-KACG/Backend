package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("api/v1/members")
public class MemberController {
    final MemberService memberService;

    @PostMapping("/auth/register")
    public RsData<Map<String, String>> register(@RequestBody MemberDto memberDto) {
        memberService.register(memberDto);

        Map<String, String> result = Map.of(
            "apiKey", "",
            "accessToken", ""
        );

        return RsData.of(201, "회원가입 성공", result);
    };
}