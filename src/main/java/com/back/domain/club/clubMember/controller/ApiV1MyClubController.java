package com.back.domain.club.clubMember.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 클럽 관련 API를 제공하는 컨트롤러
 * 유저 본인 입장에서 클럽 멤버 정보를 관리하는 기능을 포함한다
 */
@RestController
@RequestMapping("api/v1/my-clubs")
@RequiredArgsConstructor
@Tag(name = "MyClubController", description = "유저 본인 클럽 관련 API")
public class ApiV1MyClubController {

}
