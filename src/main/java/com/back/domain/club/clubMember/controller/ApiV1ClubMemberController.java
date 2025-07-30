package com.back.domain.club.clubMember.controller;

import com.back.domain.club.clubMember.dtos.ClubMemberDtos;
import com.back.domain.club.clubMember.service.ClubMemberService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}/members")
@RequiredArgsConstructor
@Tag(name = "ClubMemberController", description = "클럽 멤버 관련 API")
public class ApiV1ClubMemberController {
    private final ClubMemberService clubMemberService;

    @PostMapping
    @Operation(summary = "클럽에 멤버 추가")
    public RsData<Void> addMembersToClub(
            @PathVariable Long clubId,
            @RequestBody @Valid ClubMemberDtos.ClubMemberRegisterRequest reqBody
    ) {
        clubMemberService.addMembersToClub(clubId, reqBody);

        return RsData.of(201, "클럽에 멤버가 추가됐습니다.", null);
    }
}
