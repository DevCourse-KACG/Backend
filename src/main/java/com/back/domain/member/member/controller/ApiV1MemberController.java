package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberAuthResponse;
import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberLoginDto;
import com.back.domain.member.member.dto.MemberWithdrawMembershipResponse;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

        Cookie accessTokenCookie = createAccessTokenCookie(memberAuthResponse.accessToken());

        response.addCookie(accessTokenCookie);

        return RsData.of(200, "회원가입 성공", memberAuthResponse);
    };

    @Operation(summary = "로그인 API", description = "이메일과 비밀번호를 받아 로그인을 처리합니다.")
    @PostMapping("/auth/login")
    public RsData<MemberAuthResponse> login(@Valid @RequestBody MemberLoginDto memberLoginDto, HttpServletResponse response) {
        MemberAuthResponse memberAuthResponse = memberService.login(memberLoginDto);

        Cookie accessTokenCookie = createAccessTokenCookie(memberAuthResponse.accessToken());

        response.addCookie(accessTokenCookie);

        return RsData.of(200, "로그인 성공", memberAuthResponse);
    }

    @Operation(summary = "로그아웃 API", description = "로그아웃 처리 API입니다.")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/auth/logout")
    public RsData<MemberAuthResponse> logout(HttpServletResponse response) {
        Cookie expiredCookie = deleteCookie();

        response.addCookie(expiredCookie);

        return RsData.of(200, "로그아웃 성공");
    }

    @Operation(summary = "회원탈퇴 API", description = "회원탈퇴 처리 API입니다.")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public RsData<MemberWithdrawMembershipResponse> withdrawMembership(HttpServletResponse response,
                                                                       @AuthenticationPrincipal SecurityUser user) {
        MemberWithdrawMembershipResponse responseDto =
                memberService.withdrawMembership(user.getNickname(), user.getTag());

        response.addCookie(deleteCookie());

        return RsData.of(200,
                "회원탈퇴 성공",
                responseDto);
    }

    private Cookie createAccessTokenCookie(String accessToken) {
        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    private Cookie deleteCookie() {
        Cookie expiredCookie = new Cookie("accessToken", "");
        expiredCookie.setHttpOnly(true);
        expiredCookie.setSecure(true);
        expiredCookie.setPath("/");
        expiredCookie.setMaxAge(0);

        return expiredCookie;
    }
}

//@GetMapping("/{scheduleId}")
//    @Operation(summary = "일정 조회")
//    public RsData<ScheduleDto> getSchedule(
//            @PathVariable Long scheduleId
//    ) {
//        Schedule schedule = scheduleService.getScheduleById(scheduleId);
//        return RsData.of(
//                200,
//                "%s번 일정이 조회되었습니다.".formatted(scheduleId),
//                new ScheduleDto(schedule)
//        );
//    }