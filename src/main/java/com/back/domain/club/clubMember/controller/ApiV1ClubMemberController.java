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

    @DeleteMapping("/{memberId}")
    @Operation(summary = "클럽에서 멤버 탈퇴")
    public RsData<Void> withdrawMemberFromClub(
            @PathVariable Long clubId,
            @PathVariable Long memberId
    ) {
        clubMemberService.withdrawMemberFromClub(clubId, memberId);

        return RsData.of(200, "클럽에서 멤버가 탈퇴됐습니다.", null);
    }

    @PutMapping("/{memberId}/role")
    @Operation(summary = "클럽 멤버 권한 변경")
    public RsData<Void> changeMemberRole(
            @PathVariable Long clubId,
            @PathVariable Long memberId,
            @RequestBody @Valid ClubMemberDtos.ClubMemberRoleChangeRequest reqBody
    ) {
        clubMemberService.changeMemberRole(clubId, memberId, reqBody.role());

        return RsData.of(200, "멤버의 권한이 변경됐습니다.", null);
    }

    @GetMapping
    @Operation(summary = "클럽 멤버 목록 조회")
    public RsData<ClubMemberDtos.ClubMemberResponse> getClubMembers(
            @PathVariable Long clubId,
            @RequestParam(required = false) String state // Optional: 상태 필터링
    ) {
        ClubMemberDtos.ClubMemberResponse clubMemberResponse = clubMemberService.getClubMembers(clubId, state);

        return RsData.of(200, "클럽 멤버 목록이 조회됐습니다.", clubMemberResponse);
    }
}
