package com.back.domain.preset;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.enums.CheckListItemCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

  private Member member;
  @BeforeEach
  void setUp() {

    member = Member.builder()
        .nickname("테스트 유저")
        .password("password")
        .build();

    memberRepository.save(member);
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

    System.out.println(requestBody);

    mockMvc.perform(
        post("/api/v1/presets")
            .contentType("application/json")
            .content(requestBody)
    )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value(201))
        .andExpect(jsonPath("$.msg").value("프리셋 생성 성공"))
        .andExpect(jsonPath("$.data.name").value("My Custom Preset"))
        .andExpect(jsonPath("$.data.presetItems[0].content").value("아이템 1"))
        .andExpect(jsonPath("$.data.presetItems[0].category").value(CheckListItemCategory.PREPARATION.name()))
        .andExpect(jsonPath("$.data.presetItems[1].content").value("아이템 2"))
        .andExpect(jsonPath("$.data.presetItems[1].category").value(CheckListItemCategory.ETC.name()))
        .andDo(print());

  }
}
