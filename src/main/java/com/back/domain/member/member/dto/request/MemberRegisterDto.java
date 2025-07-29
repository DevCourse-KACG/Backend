package com.back.domain.member.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberRegisterDto(
        @Schema(description = "회원 이메일", example = "test@example.com")
        @NotBlank @Email String email,

        @Schema(description = "회원 비밀번호", example = "example123")
        @NotBlank String password,

        @Schema(description = "회원 닉네임", example = "testUser1")
        @NotBlank String nickname,

        @Schema(description = "회원 자기소개", example = "안녕하세요. 반갑습니다.")
        String bio
) {}
