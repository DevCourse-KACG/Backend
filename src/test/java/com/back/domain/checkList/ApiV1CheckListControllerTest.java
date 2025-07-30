package com.back.domain.checkList;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.repository.ScheduleRepository;
import com.back.global.enums.*;
import com.back.standard.util.Ut;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class ApiV1CheckListControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private ClubRepository clubRepository;

  @Autowired
  private ScheduleRepository scheduleRepository;

  @Autowired
  private ClubMemberRepository clubMemberRepository;

  @Value("${custom.jwt.secretKey}")
  private String secretKey;

  @Value("${custom.accessToken.expirationSeconds}")
  private Integer expirationSeconds;

  private String jwtToken;

  private Club club;
  private Member member;
  private ClubMember clubMember;
  private Schedule schedule;

  @BeforeEach
  void setUp() {
    Member memberBuilder = Member.builder()
        .nickname("테스트 유저")
        .password("password")
        .build();

    member = memberRepository.save(memberBuilder);

    // JWT 토큰 생성을 위한 Map
    Map<String, Object> claims = Map.of(
        "id", member.getId(),
        "nickname", member.getNickname());

    // JWT 토큰 생성
    jwtToken = Ut.jwt.toString(secretKey, expirationSeconds, claims);

    clubMember = ClubMember.builder()
        .member(member)
        .role(ClubMemberRole.MANAGER)
        .state(ClubMemberState.JOINING)
        .build();

    // 클럽 생성
    Club clubBuilder = Club.builder()
        .name("테스트 클럽")
        .bio("테스트 클럽 설명")
        .category(ClubCategory.CULTURE)
        .mainSpot("테스트 장소")
        .maximumCapacity(10)
        .recruitingStatus(true)
        .eventType(EventType.LONG_TERM)
        .startDate(LocalDate.parse("2025-07-05"))
        .endDate(LocalDate.parse("2025-08-30"))
        .isPublic(false)
        .leaderId(member.getId())
        .state(true)
        .build();

    clubBuilder.addClubMember(clubMember);


    club = clubRepository.save(clubBuilder);

    Schedule scheduleBuilder = Schedule.builder()
        .club(club)
        .title("테스트 일정")
        .content("테스트 일정 내용")
        .startDate(LocalDateTime.parse("2025-08-15T10:00:00"))
        .endDate(LocalDateTime.parse("2025-08-16T10:00:00"))
        .spot("테스트 장소")
        .build();

    schedule = scheduleRepository.save(scheduleBuilder);
  }

  Long checkListCreate() throws Exception {
    String requestBody = """
          {
            "scheduleId": %d,
            "checkListItems": [
              {
                "content": "체크리스트 아이템 1",
                "category": "%s",
                "sequence": 1,
                "itemAssigns": [
                  {
                    "clubMemberId": %d
                  }
                ]
              },
              {
                "content": "체크리스트 아이템 2",
                "category": "%s",
                "sequence": 2,
                "itemAssigns": []
              }
            ]
          }
        """.formatted(schedule.getId(), CheckListItemCategory.PREPARATION.name(), clubMember.getId(), CheckListItemCategory.ETC.name());

    System.out.println("Request Body: " + requestBody);
    MvcResult result = mockMvc.perform(
            post("/api/v1/checklists")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(requestBody))
    .andReturn();
    String responseContent = result.getResponse().getContentAsString();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(responseContent);
    Long checkListId = jsonNode.get("data").get("id").asLong();
    return checkListId;
  }

  @Test
  @DisplayName("체크리스트 생성")
  void t1() throws Exception {
    String requestBody = """
          {
            "scheduleId": %d,
            "checkListItems": [
              {
                "content": "체크리스트 아이템 1",
                "category": "%s",
                "sequence": 1,
                "itemAssigns": [
                  {
                    "clubMemberId": %d
                  }
                ]
              },
              {
                "content": "체크리스트 아이템 2",
                "category": "%s",
                "sequence": 2,
                "itemAssigns": []
              }
            ]
          }
        """.formatted(schedule.getId(), CheckListItemCategory.PREPARATION.name(), clubMember.getId(), CheckListItemCategory.ETC.name());

    System.out.println("Request Body: " + requestBody);
    mockMvc.perform(
            post("/api/v1/checklists")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value(201))
        .andExpect(jsonPath("$.message").value("체크리스트 생성 성공"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 생성 실패 - 일정이 존재하지 않는 경우")
  void t2() throws Exception {
    String requestBody = """
          {
            "scheduleId": 9999,
            "checkListItems": [
              {
                "content": "체크리스트 아이템 1",
                "category": "%s",
                "sequence": 1,
                "itemAssigns": [
                  {
                    "clubMemberId": %d
                  }
                ]
              }
            ]
          }
        """.formatted(CheckListItemCategory.PREPARATION.name(), clubMember.getId());

    mockMvc.perform(
            post("/api/v1/checklists")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("일정을 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 생성 실패 - 클럽 멤버가 아닌 경우")
  void t3() throws Exception {
    // 다른 멤버를 생성하고 클럽에 추가하지 않음
    Member anotherMember = Member.builder()
        .nickname("다른 유저")
        .password("password")
        .build();
    memberRepository.save(anotherMember);

    String requestBody = """
          {
            "scheduleId": %d,
            "checkListItems": [
              {
                "content": "체크리스트 아이템 1",
                "category": "%s",
                "sequence": 1,
                "itemAssigns": [
                  {
                    "clubMemberId": %d
                  }
                ]
              }
            ]
          }
        """.formatted(schedule.getId(), CheckListItemCategory.PREPARATION.name(), anotherMember.getId());

    mockMvc.perform(
            post("/api/v1/checklists")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403))
        .andExpect(jsonPath("$.message").value("클럽 멤버를 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 생성 실패 - 호스트 또는 관리자만 체크리스트를 생성할 수 있는 경우")
  void t4() throws Exception {
    // 새로운 클럽 멤버 생성
    Member anotherMember = Member.builder()
        .nickname("다른 유저")
        .password("password")
        .build();
    memberRepository.save(anotherMember);

    // anotherMember JWT 토큰 생성
    Map<String, Object> anotherClaims = Map.of(
        "id", anotherMember.getId(),
        "nickname", anotherMember.getNickname());
    String anotherJwtToken = Ut.jwt.toString(secretKey, expirationSeconds, anotherClaims);

    ClubMember anotherClubMember = ClubMember.builder()
        .member(anotherMember)
        .role(ClubMemberRole.PARTICIPANT) // 호스트 또는 관리자가 아닌 경우
        .state(ClubMemberState.JOINING)
        .build();

    club.addClubMember(anotherClubMember);
    clubRepository.save(club);
    String requestBody = """
          {
            "scheduleId": %d,
            "checkListItems": [
              {
                "content": "체크리스트 아이템 1",
                "category": "%s",
                "sequence": 1,
                "itemAssigns": [
                  {
                    "clubMemberId": %d
                  }
                ]
              }
            ]
          }
        """.formatted(schedule.getId(), CheckListItemCategory.PREPARATION.name(), member.getId());
    mockMvc.perform(
            post("/api/v1/checklists")
                .header("Authorization", "Bearer " + anotherJwtToken)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403))
        .andExpect(jsonPath("$.message").value("호스트 또는 관리자만 체크리스트를 생성할 수 있습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 생성 실패 - JWT가 유효하지 않은 경우")
  void t5() throws Exception {
    String requestBody = """
          {
            "scheduleId": %d,
            "checkListItems": [
              {
                "content": "체크리스트 아이템 1",
                "category": "%s",
                "sequence": 1,
                "itemAssigns": [
                  {
                    "clubMemberId": %d
                  }
                ]
              }
            ]
          }
        """.formatted(schedule.getId(), CheckListItemCategory.PREPARATION.name(), clubMember.getId());

    mockMvc.perform(
            post("/api/v1/checklists")
                .header("Authorization", "Bearer invalid_token")
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().is(499))
        .andExpect(jsonPath("$.code").value(499))
        .andExpect(jsonPath("$.message").value("AccessToken 만료"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 생성 실패 - JWT가 없는 경우")
  void t6() throws Exception {
    String requestBody = """
          {
            "scheduleId": %d,
            "checkListItems": [
              {
                "content": "체크리스트 아이템 1",
                "category": "%s",
                "sequence": 1,
                "itemAssigns": [
                  {
                    "clubMemberId": %d
                  }
                ]
              }
            ]
          }
        """.formatted(schedule.getId(), CheckListItemCategory.PREPARATION.name(), clubMember.getId());

    mockMvc.perform(
            post("/api/v1/checklists")
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("AccessToken을 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 생성 실패 - JWT 토큰 만료")
  void t7() throws Exception {
    // 만료된 JWT 토큰 생성
    Map<String, Object> expiredClaims = Map.of(
        "id", member.getId(),
        "nickname", member.getNickname());
    String expiredJwtToken = Ut.jwt.toString(secretKey, -1, expiredClaims); // 만료 시간을 -1로 설정

    String requestBody = """
          {
            "scheduleId": %d,
            "checkListItems": [
              {
                "content": "체크리스트 아이템 1",
                "category": "%s",
                "sequence": 1,
                "itemAssigns": [
                  {
                    "clubMemberId": %d
                  }
                ]
              }
            ]
          }
        """.formatted(schedule.getId(), CheckListItemCategory.PREPARATION.name(), clubMember.getId());

    mockMvc.perform(
            post("/api/v1/checklists")
                .header("Authorization", "Bearer " + expiredJwtToken)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().is(499))
        .andExpect(jsonPath("$.code").value(499))
        .andExpect(jsonPath("$.message").value("AccessToken 만료"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 생성 실패 - 일정에 체크리스트가 이미 존재하는 경우")
  void t8() throws Exception {
    // 먼저 체크리스트를 생성
    Long checkListId = checkListCreate();

    // 동일한 일정에 다시 체크리스트를 생성하려고 시도
    String requestBody = """
          {
            "scheduleId": %d,
            "checkListItems": [
              {
                "content": "체크리스트 아이템 1",
                "category": "%s",
                "sequence": 1,
                "itemAssigns": [
                  {
                    "clubMemberId": %d
                  }
                ]
              }
            ]
          }
        """.formatted(schedule.getId(), CheckListItemCategory.PREPARATION.name(), clubMember.getId());

    mockMvc.perform(
            post("/api/v1/checklists")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(409))
        .andExpect(jsonPath("$.message").value("이미 체크리스트가 존재합니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 조회")
  void t9() throws Exception {
    // 먼저 체크리스트를 생성
    Long checkListId = checkListCreate();

    mockMvc.perform(
            get("/api/v1/checklists/" + checkListId)
                .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("체크리스트 조회 성공"))
        .andExpect(jsonPath("$.data.id").value(checkListId))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 조회 실패 - 체크리스트가 존재하지 않는 경우")
  void t10() throws Exception {
    Long nonExistentCheckListId = 9999L; // 존재하지 않는 체크리스트 ID

    mockMvc.perform(
            get("/api/v1/checklists/" + nonExistentCheckListId)
                .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("체크리스트를 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 조회 실패 - JWT가 유효하지 않은 경우")
  void t11() throws Exception {
    Long checkListId = checkListCreate();

    mockMvc.perform(
            get("/api/v1/checklists/" + checkListId)
                .header("Authorization", "Bearer invalid_token"))
        .andExpect(status().is(499))
        .andExpect(jsonPath("$.code").value(499))
        .andExpect(jsonPath("$.message").value("AccessToken 만료"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 조회 실패 - JWT가 없는 경우")
  void t12() throws Exception {
    Long checkListId = checkListCreate();

    mockMvc.perform(
            get("/api/v1/checklists/" + checkListId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("AccessToken을 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 조회 실패 - JWT 토큰 만료")
  void t13() throws Exception {
    // 만료된 JWT 토큰 생성
    Map<String, Object> expiredClaims = Map.of(
        "id", member.getId(),
        "nickname", member.getNickname());
    String expiredJwtToken = Ut.jwt.toString(secretKey, -1, expiredClaims); // 만료 시간을 -1로 설정

    Long checkListId = checkListCreate();

    mockMvc.perform(
            get("/api/v1/checklists/" + checkListId)
                .header("Authorization", "Bearer " + expiredJwtToken))
        .andExpect(status().is(499))
        .andExpect(jsonPath("$.code").value(499))
        .andExpect(jsonPath("$.message").value("AccessToken 만료"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 조회 실패 - 클럽 멤버가 아닌 경우")
  void t14() throws Exception {
    // 다른 멤버를 생성하고 클럽에 추가하지 않음
    Member anotherMember = Member.builder()
        .nickname("다른 유저")
        .password("password")
        .build();
    memberRepository.save(anotherMember);

    // 다른 멤버의 JWT 토큰 생성
    Map<String, Object> anotherClaims = Map.of(
        "id", anotherMember.getId(),
        "nickname", anotherMember.getNickname());
    String anotherJwtToken = Ut.jwt.toString(secretKey, expirationSeconds, anotherClaims);

    Long checkListId = checkListCreate();

    mockMvc.perform(
            get("/api/v1/checklists/" + checkListId)
                .header("Authorization", "Bearer " + anotherJwtToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403))
        .andExpect(jsonPath("$.message").value("클럽 멤버가 아닙니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 수정")
  void t15() throws Exception {
    // 먼저 체크리스트를 생성
    Long checkListId = checkListCreate();
    // 다른 클럽 멤버 생성
    Member anotherMember = Member.builder()
        .nickname("다른 유저")
        .password("password")
        .build();
    memberRepository.save(anotherMember);
    // 다른 클럽 멤버를 클럽에 추가
    ClubMember anotherClubMember = ClubMember.builder()
        .member(anotherMember)
        .role(ClubMemberRole.PARTICIPANT)
        .state(ClubMemberState.JOINING)
        .build();
    club.addClubMember(anotherClubMember);
    clubMemberRepository.save(anotherClubMember);

    String requestBody = """
          {
            "checkListItems": [
              {
                "id": 1,
                "content": "수정된 체크리스트 아이템 1",
                "category": "%s",
                "isChecked": true,
                "sequence": 1,
                "itemAssigns": [
                  {
                    "clubMemberId": %d,
                    "isChecked": true
                  }
                ]
              },
              {
                "id": 2,
                "content": "수정된 체크리스트 아이템 2",
                "category": "%s",
                "sequence": 2,
                "itemAssigns": []
              }
            ]
          }
        """.formatted(CheckListItemCategory.PREPARATION.name(), anotherClubMember.getId(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
            put("/api/v1/checklists/" + checkListId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("체크리스트 수정 성공"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 수정 실패 - 체크리스트가 존재하지 않는 경우")
  void t16() throws Exception {
    Long nonExistentCheckListId = 9999L; // 존재하지 않는 체크리스트 ID

    String requestBody = """
          {
            "checkListItems": [
              {
                "id": 1,
                "content": "수정된 체크리스트 아이템 1",
                "category": "%s",
                "isChecked": true,
                "sequence": 1,
                "itemAssigns": []
              }
            ]
          }
        """.formatted(CheckListItemCategory.PREPARATION.name());

    mockMvc.perform(
            put("/api/v1/checklists/" + nonExistentCheckListId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("체크리스트를 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 수정 실패 - 클럽 멤버가 아닌 경우")
  void t17() throws Exception {
    // 다른 멤버를 생성하고 클럽에 추가하지 않음
    Member anotherMember = Member.builder()
        .nickname("다른 유저")
        .password("password")
        .build();
    memberRepository.save(anotherMember);

    // 먼저 체크리스트를 생성
    Long checkListId = checkListCreate();

    String requestBody = """
          {
            "checkListItems": [
              {
                "id": 1,
                "content": "수정된 체크리스트 아이템 1",
                "category": "%s",
                "isChecked": true,
                "sequence": 1,
                "itemAssigns": [
                  {
                    "clubMemberId": %d
                  }
                ]
              }
            ]
          }
        """.formatted(CheckListItemCategory.PREPARATION.name(), anotherMember.getId());

    mockMvc.perform(
            put("/api/v1/checklists/" + checkListId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403))
        .andExpect(jsonPath("$.message").value("클럽 멤버를 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 수정 실패 - 호스트 또는 관리자만 체크리스트를 수정할 수 있는 경우")
  void t18() throws Exception {
    // 새로운 클럽 멤버 생성
    Member anotherMember = Member.builder()
        .nickname("다른 유저")
        .password("password")
        .build();
    memberRepository.save(anotherMember);

    // anotherMember JWT 토큰 생성
    Map<String, Object> anotherClaims = Map.of(
        "id", anotherMember.getId(),
        "nickname", anotherMember.getNickname());
    String anotherJwtToken = Ut.jwt.toString(secretKey, expirationSeconds, anotherClaims);

    ClubMember anotherClubMember = ClubMember.builder()
        .member(anotherMember)
        .role(ClubMemberRole.PARTICIPANT) // 호스트 또는 관리자가 아닌 경우
        .state(ClubMemberState.JOINING)
        .build();

    club.addClubMember(anotherClubMember);
    clubRepository.save(club);

    // 먼저 체크리스트를 생성
    Long checkListId = checkListCreate();

    String requestBody = """
          {
            "checkListItems": [
              {
                "id": 1,
                "content": "수정된 체크리스트 아이템 1",
                "category": "%s",
                "isChecked": true,
                "sequence": 1,
                "itemAssigns": []
              }
            ]
          }
        """.formatted(CheckListItemCategory.PREPARATION.name());

    mockMvc.perform(
            put("/api/v1/checklists/" + checkListId)
                .header("Authorization", "Bearer " + anotherJwtToken)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403))
        .andExpect(jsonPath("$.message").value("호스트 또는 관리자만 체크리스트를 수정할 수 있습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 수정 실패 - JWT가 유효하지 않은 경우")
  void t19() throws Exception {
    // 먼저 체크리스트를 생성
    Long checkListId = checkListCreate();

    String requestBody = """
          {
            "checkListItems": [
              {
                "id": 1,
                "content": "수정된 체크리스트 아이템 1",
                "category": "%s",
                "isChecked": true,
                "sequence": 1,
                "itemAssigns": []
              }
            ]
          }
        """.formatted(CheckListItemCategory.PREPARATION.name());

    mockMvc.perform(
            put("/api/v1/checklists/" + checkListId)
                .header("Authorization", "Bearer invalid_token")
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().is(499))
        .andExpect(jsonPath("$.code").value(499))
        .andExpect(jsonPath("$.message").value("AccessToken 만료"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 수정 실패 - JWT가 없는 경우")
  void t20() throws Exception {
    // 먼저 체크리스트를 생성
    Long checkListId = checkListCreate();

    String requestBody = """
          {
            "checkListItems": [
              {
                "id": 1,
                "content": "수정된 체크리스트 아이템 1",
                "category": "%s",
                "isChecked": true,
                "sequence": 1,
                "itemAssigns": []
              }
            ]
          }
        """.formatted(CheckListItemCategory.PREPARATION.name());

    mockMvc.perform(
            put("/api/v1/checklists/" + checkListId)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("AccessToken을 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("체크리스트 수정 실패 - JWT 토큰 만료")
  void t21() throws Exception {
    // 먼저 체크리스트를 생성
    Long checkListId = checkListCreate();

    // 만료된 JWT 토큰 생성
    Map<String, Object> expiredClaims = Map.of(
        "id", member.getId(),
        "nickname", member.getNickname());
    String expiredJwtToken = Ut.jwt.toString(secretKey, -1, expiredClaims); // 만료 시간을 -1로 설정

    String requestBody = """
          {
            "checkListItems": [
              {
                "id": 1,
                "content": "수정된 체크리스트 아이템 1",
                "category": "%s",
                "isChecked": true,
                "sequence": 1,
                "itemAssigns": []
              }
            ]
          }
        """.formatted(CheckListItemCategory.PREPARATION.name());

    mockMvc.perform(
            put("/api/v1/checklists/" + checkListId)
                .header("Authorization", "Bearer " + expiredJwtToken)
                .contentType("application/json")
                .content(requestBody))
        .andExpect(status().is(499))
        .andExpect(jsonPath("$.code").value(499))
        .andExpect(jsonPath("$.message").value("AccessToken 만료"))
        .andDo(print());
  }

}
