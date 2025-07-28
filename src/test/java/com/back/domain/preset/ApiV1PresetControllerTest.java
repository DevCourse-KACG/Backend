package com.back.domain.preset;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.enums.CheckListItemCategory;
import com.back.standard.util.Ut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class ApiV1PresetControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MemberRepository memberRepository;

  @Value("${custom.jwt.secretKey}")
  private String secretKey;

  @Value("${custom.accessToken.expirationSeconds}")
  private Integer expirationSeconds;

  private String jwtToken;
  private Member member;
  @BeforeEach
  void setUp() {

    member = Member.builder()
        .nickname("테스트 유저")
        .password("password")
        .build();

    memberRepository.save(member);

    // JWT 토큰 생성을 위한 Map
    Map<String, Object> claims = Map.of(
        "id", member.getId(),
        "nickname", member.getNickname());

    // JWT 토큰 생성
    jwtToken = Ut.jwt.toString(secretKey, expirationSeconds, claims);
  }

  void presetCreate() throws Exception {
    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "%s", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "My Custom Preset"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType("application/json")
            .content(requestBody)
    );
  }

  @Test
  @DisplayName("프리셋 생성 테스트")
  void t1() throws Exception {

    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "%s", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "My Custom Preset"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value(201))
        .andExpect(jsonPath("$.message").value("프리셋 생성 성공"))
        .andExpect(jsonPath("$.data.name").value("My Custom Preset"))
        .andExpect(jsonPath("$.data.presetItems[0].content").value("아이템 1"))
        .andExpect(jsonPath("$.data.presetItems[0].category").value(CheckListItemCategory.PREPARATION.name()))
        .andExpect(jsonPath("$.data.presetItems[1].content").value("아이템 2"))
        .andExpect(jsonPath("$.data.presetItems[1].category").value(CheckListItemCategory.ETC.name()))
        .andDo(print());

  }

  @Test
  @DisplayName("프리셋 생성 실패 - JWT 토큰 없음")
  void t2() throws Exception {
    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "%s", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "My Custom Preset"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("AccessToken을 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 생성 실패 - JWT 토큰 만료")
  void t3() throws Exception {
    // 만료된 JWT 토큰 생성
    String expiredJwtToken = Ut.jwt.toString(secretKey, -1, Map.of("id", member.getId(), "nickname", member.getNickname()));

    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "%s", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "My Custom Preset"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .header("Authorization", "Bearer " + expiredJwtToken)
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().is(499))
        .andExpect(jsonPath("$.code").value(499))
        .andExpect(jsonPath("$.message").value("AccessToken 만료"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 생성 실패 - 멤버를 찾을 수 없음")
  void t4() throws Exception {
    // 유효하지 않은 JWT 토큰 생성 (존재하지 않는 멤버 ID 사용)
    String invalidJwtToken = Ut.jwt.toString(secretKey, expirationSeconds, Map.of("id", 9999L, "nickname", "Invalid User"));

    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "%s", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "My Custom Preset"
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .header("Authorization", "Bearer " + invalidJwtToken)
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("멤버를 찾을 수 없습니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 생성 실패 - 잘못된 요청 형식")
  void t5() throws Exception {
    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "%s", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": ""
    }
    """, CheckListItemCategory.PREPARATION.name(), CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.message").value("name-NotBlank-프리셋 이름은 필수입니다"))
        .andDo(print());
  }

  @Test
  @DisplayName("프리셋 생성 실패 - 잘못된 카테고리")
  void t6() throws Exception {
    String requestBody = String.format("""
    {
      "presetItems": [
        { "content": "아이템 1", "category": "INVALID_CATEGORY", "sequence":1 },
        { "content": "아이템 2", "category": "%s", "sequence":2 }
      ],
      "name": "My Custom Preset"
    }
    """, CheckListItemCategory.ETC.name());

    mockMvc.perform(
        post("/api/v1/presets")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.message").value("요청 본문이 올바르지 않습니다."))
        .andDo(print());
  }

}
