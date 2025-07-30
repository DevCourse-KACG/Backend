package com.back.domain.member.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateMemberInfoDto(
        @NotBlank String nickname,
        String password,
        @NotBlank String bio
) {
}
