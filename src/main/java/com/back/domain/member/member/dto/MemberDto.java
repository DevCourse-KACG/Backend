package com.back.domain.member.member.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberDto(
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String nickname,
        String bio
) {}
