package com.back.domain.club.club.controller;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1ClubControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ClubService clubService;

    @Test
    @DisplayName("빈 그룹 생성 - 이미지 없는 경우")
    void createGroup() throws Exception {
        // given
        // when
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/clubs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "테스트 그룹",
                                            "bio": "테스트 그룹 설명",
                                            "category" : "TRAVEL",
                                            "mainSpot" : "서울",
                                            "maximumCapacity" : 10,
                                            "eventType" : "SHORT_TERM",
                                            "startDate" : "2023-10-01",
                                            "endDate" : "2023-10-31",
                                            "isPublic": true,
                                            "leaderId": 1,
                                            "clubMembers" : []
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubController.class))
                .andExpect(handler().methodName("createClub"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("클럽이 생성됐습니다."))
                .andExpect(jsonPath("$.data.clubId").isNumber())
                .andExpect(jsonPath("$.data.leaderId").value(1));

        // 추가 검증: 그룹이 실제로 생성되었는지 확인
        Club club = clubService.getLastCreatedClub();

        assertThat(club.getName()).isEqualTo("테스트 그룹");
        assertThat(club.getBio()).isEqualTo("테스트 그룹 설명");
        assertThat(club.getCategory()).isEqualTo(ClubCategory.TRAVEL);
        assertThat(club.getMainSpot()).isEqualTo("서울");
        assertThat(club.getMaximumCapacity()).isEqualTo(10);
        assertThat(club.getEventType()).isEqualTo(EventType.SHORT_TERM);
        assertThat(club.getStartDate().toLocalDate()).isEqualTo(LocalDate.of(2023, 10, 1));
        assertThat(club.getEndDate().toLocalDate()).isEqualTo(LocalDate.of(2023, 10, 31));
        assertThat(club.isPublic()).isTrue();
        assertThat(club.getLeaderId()).isEqualTo(1L);
        assertThat(club.isState()).isTrue(); // 활성화 상태가 true인지 확인
        assertThat(club.getClubMembers().isEmpty()).isTrue(); // 구성원이 비어있는지 확인
    }

}