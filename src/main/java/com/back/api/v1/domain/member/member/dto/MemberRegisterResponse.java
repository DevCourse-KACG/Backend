package com.back.api.v1.domain.member.member.dto;

public record MemberRegisterResponse(
        String apikey,
        String accessToken
) {}
