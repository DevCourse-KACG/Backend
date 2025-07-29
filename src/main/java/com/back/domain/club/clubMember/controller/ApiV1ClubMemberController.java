package com.back.domain.club.clubMember.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}/members")
@RequiredArgsConstructor
@Tag(name = "ClubMemberController", description = "클럽 멤버 관련 API")
public class ApiV1ClubMemberController {
}
