package com.back.domain.member.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberDto(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String nickname,
        String bio
) {}
