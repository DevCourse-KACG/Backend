package com.back.domain.member.member;

import com.back.api.v1.domain.api.service.ApiKeyService;
import com.back.api.v1.domain.auth.service.AuthService;
import com.back.api.v1.domain.member.member.controller.ApiV1MemberController;
import com.back.api.v1.domain.member.member.entity.Member;
import com.back.api.v1.domain.member.member.entity.MemberInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import support.MemberFixture;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ApiV1MemberControllerTest {
    private MemberFixture memberFixture;

    @Autowired
    private ApiV1MemberController apiV1MemberController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("회원가입 - 정상 기입 / 객체 정상 생성")
    public void memberObjectCreationTest() {
        MemberInfo memberInfo = MemberInfo.builder()
                .email("qkqek6223@naver.com")
                .bio("안녕하세요 반갑습니다")
                .profileImageUrl("https://picsum.photos/seed/picsum/200/300")
                .build();

        Member member = Member.builder()
                .nickname("안수지")
                .password("password123")
                .memberInfo(memberInfo)
                .presets(null)
                .build();

        assertEquals("안수지", member.getNickname());
        assertEquals("password123", member.getPassword());
        assertNotNull(member.getMemberInfo());
        assertEquals("qkqek6223@naver.com", member.getMemberInfo().getEmail());
        assertEquals("안녕하세요 반갑습니다", member.getMemberInfo().getBio());
    }

    @Test
    @DisplayName("회원가입 - 정상 기입 / POST 정상 작동")
    public void memberPostTest() throws  Exception {
        String requestBody = """
                {
                    "email": "qkek6223@naver.com",
                    "password": "password123",
                    "nickname": "안수지",
                    "bio": "안녕하세요"
                }
                """;

        mockMvc.perform(post("/api/v1/members/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
    }

    @Test
    @DisplayName("회원가입 - 이메일 중복 기입 / POST 실패")
    public void memberPostTestException1() throws  Exception {
        String requestBody = """
                {
                    "email": "qkek6223@naver.com",
                    "password": "password123",
                    "nickname": "안수지",
                    "bio": "안녕하세요"
                }
                """;

        mockMvc.perform(post("/api/v1/members/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));

        String requestBody2 = """
                {
                    "email": "qkek6223@naver.com",
                    "password": "password123",
                    "nickname": "안수지1",
                    "bio": "안녕하세요"
                }
                """;

        mockMvc.perform(post("/api/v1/members/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody2))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("회원가입 - 닉네임 중복 기입 / POST 실패")
    public void memberPostTestException2() throws  Exception {
        String requestBody = """
                {
                    "email": "qkek6223@naver.com",
                    "password": "password123",
                    "nickname": "안수지",
                    "bio": "안녕하세요"
                }
                """;

        mockMvc.perform(post("/api/v1/members/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));

        String requestBody2 = """
                {
                    "email": "qkek622378@naver.com",
                    "password": "password123",
                    "nickname": "안수지",
                    "bio": "안녕하세요"
                }
                """;

        mockMvc.perform(post("/api/v1/members/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody2))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    @DisplayName("API key 발급 - 정상")
    public void generateApiKey_success() throws Exception {
        Long userId = 1L;

        String apiKey = apiKeyService.generateApiKey(userId);

        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("api_"));
    }

    @Test
    @DisplayName("AccessToken 발급 - 정상")
    public void generateAccessToken_success() throws Exception {
        String validApiKey = "api_123456abcdef";

        String accessToken = authService.generateAccessToken(validApiKey);

        assertNotNull(accessToken);
        assertTrue(accessToken.startsWith("Bearer "));
    }
}
