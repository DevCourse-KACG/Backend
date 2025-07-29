package com.back.domain.member.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CustomMemberDto(
        @Schema(description = "회원 닉네임", example = "testUser1")
        @NotBlank String nickname,

        @Schema(description = "회원 태그", example = "2455")
        @NotBlank String tag
) {
}
