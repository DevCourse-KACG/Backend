package com.back.domain.club.club.controller;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.global.aws.S3Service;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1ClubControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ClubService clubService;

    @MockitoBean
    private S3Service s3Service; // S3Service는 MockBean으로 주입하여 실제 S3와의 통신을 피합니다.

    @Test
    @DisplayName("빈 그룹 생성 - 이미지 없는 경우")
    void createGroup() throws Exception {
        // given
        String jsonData = """
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
            """;

        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonData.getBytes(StandardCharsets.UTF_8)
        );

        // when
        ResultActions resultActions = mvc
                .perform(
                        multipart("/api/v1/clubs").file(dataPart)
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
        assertThat(club.getStartDate()).isEqualTo(LocalDate.of(2023, 10, 1));
        assertThat(club.getEndDate()).isEqualTo(LocalDate.of(2023, 10, 31));
        assertThat(club.isPublic()).isTrue();
        assertThat(club.getLeaderId()).isEqualTo(1L);
        assertThat(club.isState()).isTrue(); // 활성화 상태가 true인지 확인
        assertThat(club.getClubMembers().isEmpty()).isTrue(); // 구성원이 비어있는지 확인
    }



    @Test
    @DisplayName("그룹 생성 - 이미지가 있는 경우")
    void createClubWithImage() throws Exception {
        // given
        // ⭐️ S3 업로더의 행동 정의: 어떤 파일이든 업로드 요청이 오면, 지정된 가짜 URL을 반환한다.
        String fakeImageUrl = "https://my-s3-bucket.s3.ap-northeast-2.amazonaws.com/club/1/profile/fake-image.jpg";
        given(s3Service.upload(any(MultipartFile.class), any(String.class))).willReturn(fakeImageUrl);

        // 1. 가짜 이미지 파일(MockMultipartFile) 생성
        MockMultipartFile imagePart = new MockMultipartFile(
                "image", // @RequestPart("image") 이름과 일치
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        // 2. JSON 데이터 파트 생성 (위와 동일)
        String jsonData = """
            {
                "name": "이미지 있는 그룹",
                "bio": "테스트 그룹 설명",
                "category" : "HOBBY",
                "mainSpot" : "부산",
                "maximumCapacity" : 5,
                "eventType" : "LONG_TERM",
                "startDate" : "2025-08-01",
                "endDate" : "2026-07-31",
                "isPublic": false,
                "leaderId": 2,
                "clubMembers" : []
            }
            """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json", jsonData.getBytes(StandardCharsets.UTF_8));


        // when
        // 3. MockMvc로 multipart 요청 생성 (JSON 파트와 이미지 파트 모두 포함)
        ResultActions resultActions = mvc.perform(
                        multipart("/api/v1/clubs")
                                .file(dataPart)
                                .file(imagePart) // 'image' 파트 추가
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.clubId").isNumber());

        // 추가 검증
        Club club = clubService.getLastCreatedClub();
        assertThat(club.getName()).isEqualTo("이미지 있는 그룹");
        assertThat(club.getImageUrl()).isEqualTo(fakeImageUrl); // ⭐️ 이미지 URL이 가짜 URL과 일치하는지 확인
    }



}