package com.back.domain.member.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GuestRegisterDto(
        @NotBlank String nickname,
        @NotBlank String password,
        @NotBlank Long clubId
) {
}
