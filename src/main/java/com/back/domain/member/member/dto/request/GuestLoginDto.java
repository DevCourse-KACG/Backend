package com.back.domain.member.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GuestLoginDto(
        @NotBlank String nickname,
        @NotBlank String password,
        @NotNull Long clubId
) {
}
