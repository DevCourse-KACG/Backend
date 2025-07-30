package com.back.domain.club.clubMember.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ClubMemberDtos {



    /**
     * 클럽 멤버 정보를 등록하기 위한 DTO 클래스
     */
    public static record ClubMemberRegisterInfo(
            @NotBlank
            String email,
            @NotBlank
            String role
    ) {}

    public static record ClubMemberRegisterRequest(
            @NotEmpty
            List<ClubMemberRegisterInfo> members
    ){}
}
