package com.back.domain.member.member.dto.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberLoginDto (
        @NotBlank @Email String email,
        @NotBlank String password
) {
}