package com.back.domain.club.clubLink.controller;

import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubLink.entity.ClubLink;
import com.back.domain.club.clubLink.repository.ClubLinkRepository;
import com.back.domain.club.clubLink.service.ClubLinkService;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1ClubLinkControllerTest {

    @Autowired
    private ClubLinkService clubLinkService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    @Autowired
    private ClubLinkRepository clubLinkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("초대 링크 - 링크 생성 성공")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    void createClubLink_Success() throws Exception {

        MvcResult result = mockMvc.perform(get("/api/v1/clubs/1/members/invitation-link"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.code").value(200))
                            .andExpect(jsonPath("$.message").value("클럽 초대 링크가 생성되었습니다."))
                            .andExpect(jsonPath("$.data.link").exists())
                            .andExpect(jsonPath("$.data.link").value(org.hamcrest.Matchers.containsString("https://supplies.com/clubs/invite?token=")))
                        .andReturn();

        // 응답 JSON 에서 link 추출
        String responseBody = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(responseBody);
        String link = root.path("data").path("link").asText();
        String inviteCodeFromResponse = link.substring(link.indexOf("token=") + 6);

        // DB 에서 실제 저장된 초대 코드 확인
        List<ClubLink> savedLinks = clubLinkRepository.findAll();
        assertFalse(savedLinks.isEmpty());
        assertEquals(inviteCodeFromResponse, savedLinks.get(0).getInviteCode());
    }

    @Test
    @DisplayName("초대 링크 생성 실패 - 존재하지 않는 클럽 ID")
    @WithUserDetails(value = "hgd222@test.com")
    void createClubLink_Fail_ClubNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/clubs/9999999/members/invitation-link"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("해당 id의 클럽을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("초대 링크 생성 실패 - 권한 없는 멤버")
    @WithUserDetails(value = "lyh3@test.com")
    void createClubLink_Fail_NoPermission() throws Exception {
        mockMvc.perform(get("/api/v1/clubs/1/members/invitation-link"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("호스트나 매니저만 초대 링크를 생성할 수 있습니다."));
    }
}
