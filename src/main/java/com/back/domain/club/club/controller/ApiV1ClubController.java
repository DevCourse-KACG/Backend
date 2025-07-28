package com.back.domain.club.club.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clubs")
@RequiredArgsConstructor
@Tag(name = "ClubController", description = "클럽 관련 API")
public class ApiV1ClubController {

}
