package com.back.domain.member.member.controller;

import com.back.domain.api.dto.TokenRefreshRequest;
import com.back.domain.member.member.dto.request.*;
import com.back.domain.member.member.dto.response.*;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import io.jsonwebtoken.io.IOException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/members")
public class ApiV1MemberController {
    final MemberService memberService;

    @Operation(summary = "회원가입 API", description = "이메일, 비밀번호 등을 받아 회원가입을 처리합니다.")
    @PostMapping("/auth/register")
    public RsData<MemberAuthResponse> register(@Valid @RequestBody MemberRegisterDto memberRegisterDto, HttpServletResponse response) {
        MemberAuthResponse memberAuthResponse = memberService.register(memberRegisterDto);

        Cookie accessTokenCookie = createAccessTokenCookie(memberAuthResponse.accessToken(), false);

        response.addCookie(accessTokenCookie);

        return RsData.of(200, "회원가입 성공", memberAuthResponse);
    };

    @Operation(summary = "로그인 API", description = "이메일과 비밀번호를 받아 로그인을 처리합니다.")
    @PostMapping("/auth/login")
    public RsData<MemberAuthResponse> login(@Valid @RequestBody MemberLoginDto memberLoginDto, HttpServletResponse response) {
        MemberAuthResponse memberAuthResponse = memberService.login(memberLoginDto);

        Cookie accessTokenCookie = createAccessTokenCookie(memberAuthResponse.accessToken(), false);

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

    @Operation(summary = "회원탈퇴 API", description = "회원탈퇴 처리 API 입니다.")
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

    @Operation(summary = "비밀번호 유효성 검사 API", description = "비밀번호의 유효성을 인증하는 API 입니다.")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/auth/verify-password")
    public RsData<MemberPasswordResponse> checkPasswordValidity(@AuthenticationPrincipal SecurityUser user,
                                                                @Valid @RequestBody PasswordCheckRequestDto dto) {

        if (user == null) {
            throw new ServiceException(401, "인증이 필요합니다.");
        }

        MemberPasswordResponse response = memberService.checkPasswordValidity(user.getId(), dto.password());

        return RsData.of(200,
                "비밀번호 유효성 반환 성공",
                response);
    }

    @Operation(summary = "access token 재발급 API", description = "리프레시 토큰으로 access token을 재발급하는 API 입니다.")
    @PostMapping("/auth/refresh")
    public RsData<MemberAuthResponse> apiTokenReissue(@RequestBody TokenRefreshRequest requestBody,
                                                      HttpServletResponse response) {

        String ApiKey = requestBody.refreshToken();

        if (ApiKey == null || ApiKey.isBlank()) {
            return RsData.of(401, "Refresh Token이 존재하지 않습니다.");
        }

        // 사용자 정보 추출
        Member member = memberService.findByApiKey(ApiKey);

        // 새로운 access token 생성
        String newAccessToken = memberService.generateAccessToken(member);

        // access token 쿠키에 담아서 응답
        Cookie accessTokenCookie = createAccessTokenCookie(newAccessToken, false);
        response.addCookie(accessTokenCookie);

        return RsData.of(200, "Access Token 재발급 성공",
                new MemberAuthResponse(ApiKey, newAccessToken));
    }

    @Operation(summary = "내 정보 반환 API", description = "현재 로그인한 유저 정보를 반환하는 API 입니다.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public RsData<MemberDetailInfoResponse> getMyInfo(HttpServletResponse response,
                                                      @AuthenticationPrincipal SecurityUser user) {

        MemberDetailInfoResponse memberDetailInfoResponse =
                memberService.getUserInfo(user.getId());

        return RsData.of(200,
                "유저 정보 반환 성공",
                memberDetailInfoResponse);
    }

    @Operation(summary = "내 정보 수정 API", description = "현재 로그인한 유저 정보를 수정하는 API 입니다.")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public RsData<MemberDetailInfoResponse> updateInfo(@AuthenticationPrincipal SecurityUser user,
                                                       @Valid @RequestPart(value = "data") UpdateMemberInfoDto dto,
                                                       @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) throws IOException {
        MemberDetailInfoResponse memberDetailInfoResponse =
                memberService.updateInfo(user.getId(), dto, profileImage);

        return RsData.of(200,
                "유저 정보 수정 성공",
                memberDetailInfoResponse);
    }

    @Operation(summary = "비회원 모임 등록 API", description = "비회원 모임 등록 API 입니다.")
    @PostMapping("/auth/guest-register")
    public RsData<GuestResponse> registerGuest(HttpServletResponse response,
                                               @Valid @RequestBody GuestRegisterDto dto) {
        GuestResponse guestResponse =
                memberService.registerGuest(dto);

        Cookie accessTokenCookie = createAccessTokenCookie(guestResponse.accessToken(), true);

        response.addCookie(accessTokenCookie);

        return RsData.of(200,
                "비회원 모임 가입 성공",
                guestResponse);
    }

    @Operation(summary = "비회원 임시 로그인 API", description = "비회원 임시 로그인 API 입니다.")
    @PutMapping("/auth/guest-login")
    public RsData<GuestResponse> guestLogin(HttpServletResponse response,
                                                @Valid @RequestBody GuestLoginDto guestLoginDto) {
        GuestResponse guestAuthResponse = memberService.GuestLogin(guestLoginDto);

        Cookie accessTokenCookie = createAccessTokenCookie(guestAuthResponse.accessToken(), true);

        response.addCookie(accessTokenCookie);

        return RsData.of(200, "비회원 로그인 성공", guestAuthResponse);
    }

    private Cookie createAccessTokenCookie(String accessToken, boolean isGuest) {
        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");

        cookie.setMaxAge(isGuest ? 60 * 60 * 24 * 30 : 60 * 60 * 24);
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

//    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
//        if (request.getCookies() == null) return null;
//
//        for (Cookie cookie : request.getCookies()) {
//            if ("refreshToken".equals(cookie.getName())) {
//                return cookie.getValue();
//            }
//        }
//        return null;
//    }
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