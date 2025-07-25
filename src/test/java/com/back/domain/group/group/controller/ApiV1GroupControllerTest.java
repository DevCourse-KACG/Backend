package com.back.domain.group.group.controller;

import com.back.domain.group.group.entity.Group;
import com.back.domain.group.group.service.GroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1GroupControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private GroupService groupService;

    @Test
    @DisplayName("빈 그룹 생성 - 이미지 없는 경우")
    void createGroup() throws Exception {
        // given
        // when
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/groups")
                                .content("""
                                        {
                                            "name": "테스트 그룹",
                                            "bio": "테스트 그룹 설명",
                                            "category" : "여행",
                                            "mainSpot" : "서울",
                                            "maximumCapacity" : 10,
                                            "eventType" : "단기",
                                            "startDate" : "2023-10-01",
                                            "endDate" : "2023-10-31",
                                            "isPublic": true,
                                            "leaderId": 1,
                                            "groupMembers" : []
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1GroupController.class))
                .andExpect(handler().methodName("createGroup"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("그룹이 생성됐습니다."))
                .andExpect(jsonPath("$.data.group_id").isNumber())
                .andExpect(jsonPath("$.data.leader_id").value(1));

        // 추가 검증: 그룹이 실제로 생성되었는지 확인
        Group group = groupService.findById(1L);

        assertThat(group.getName()).isEqualTo("테스트 그룹");
        assertThat(group.getBio()).isEqualTo("테스트 그룹 설명");
        assertThat(group.getCategory()).isEqualTo("여행");
        assertThat(group.getMainSpot()).isEqualTo("서울");
        assertThat(group.getMaximumCapacity()).isEqualTo(10);
        assertThat(group.getEventType()).isEqualTo("단기");
        assertThat(group.getStartDate()).isEqualTo("2023-10-01");
        assertThat(group.getEndDate()).isEqualTo("2023-10-31");
        assertThat(group.isPublic()).isTrue();
        assertThat(group.getLeaderId()).isEqualTo(1L);
        assertThat(group.isStats()).isTrue(); // 활성화 상태가 true인지 확인
        assertThat(group.getGroupMembers().isEmpty()).isTrue(); // 구성원이 비어있는지 확인
    }

}