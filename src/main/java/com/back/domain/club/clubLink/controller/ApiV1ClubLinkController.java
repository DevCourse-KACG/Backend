package com.back.domain.club.clubLink.controller;

import com.back.domain.club.clubLink.dtos.ClubLinkDtos;
import com.back.domain.club.clubLink.service.ClubLinkService;
import com.back.domain.member.member.entity.Member;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clubs")
@RequiredArgsConstructor
public class ApiV1ClubLinkController {
    private final Rq rq;
    private final ClubLinkService  clubLinkService;

    @GetMapping("/{clubId}/members/invitation-link")
    @Operation(summary = "클럽 초대 링크 생성")
    public RsData<ClubLinkDtos.CreateClubLinkResponse> createClubLink(@PathVariable Long clubId) {

        Member user = rq.getActor();
        ClubLinkDtos.CreateClubLinkResponse response = clubLinkService.createClubLink(user, clubId);

        return new RsData<>(200, "클럽 초대 링크가 생성되었습니다.", response);
    }
}
