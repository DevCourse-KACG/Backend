package com.back.domain.member.member.dto;

public record MemberDto(
        String email,
        String password,
        String nickname,
        String bio
) {}
