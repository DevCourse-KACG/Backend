package com.back.domain.member.member.dto;

public record MemberAuthResponse(
        String apikey,
        String accessToken
) {}
