package com.back.domain.member.member;

import com.back.domain.api.service.ApiKeyService;
import com.back.domain.auth.service.AuthService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.member.member.support.MemberFixture;
import com.back.global.security.SecurityUser;
import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ApiV1MemberControllerTest {
    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberService memberService;

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
    @DisplayName("API key 발급 - 정상")
    public void generateApiKey_success() throws Exception {
        String apiKey = apiKeyService.generateApiKey();

        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("api_"));
    }

    @Test
    @DisplayName("AccessToken 발급 - 정상")
    public void generateAccessToken_success() throws Exception {
        Member member = memberFixture.createMember(1);

        String accessToken = authService.generateAccessToken(member);

        assertNotNull(accessToken);
    }

    @Test
    @DisplayName("로그인 - 정상 기입")
    public void loginSuccess() throws Exception {
        memberFixture.createMember(1);

        String requestBody = """
        {
            "email": "test1@example.com",
            "password": "password123"
        }
        """;

        mockMvc.perform(post("/api/v1/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.apikey").isNotEmpty())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("로그인 - 없는 이메일 기입")
    public void loginNonexistentEmail() throws Exception {
        memberFixture.createMember(1);

        String requestBody = """
        {
            "email": "wrong@example.com",
            "password": "password123"
        }
        """;

        mockMvc.perform(post("/api/v1/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일과 비밀번호가 맞지 않습니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").doesNotExist());

    }

    @Test
    @DisplayName("로그인 - 맞지 않는 비밀번호 기입")
    public void loginWrongPassword() throws Exception {
        memberFixture.createMember(1);

        String requestBody = """
        {
            "email": "test1@example.com",
            "password": "WrongPassword"
        }
        """;

        mockMvc.perform(post("/api/v1/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일과 비밀번호가 맞지 않습니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").doesNotExist());

    }

    @Test
    @DisplayName("로그아웃 - 정상 처리")
    public void logout() throws Exception {
        memberFixture.createMember(1);

        Cookie accessTokenCookie = loginAndGetAccessTokenCookie("test1@example.com", "password123");

        mockMvc.perform(delete("/api/v1/members/auth/logout")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("accessToken", 0)); // 쿠키 만료 확인
    }

    @Test
    @DisplayName("회원탈퇴 - 정상 처리")
    public void withdrawMembership() throws Exception {
        Member member = memberFixture.createMember(1);

        Cookie accessTokenCookie = loginAndGetAccessTokenCookie("test1@example.com", "password123");

        mockMvc.perform(delete("/api/v1/members/me")
                .with(user(new SecurityUser(
                        member.getId(),
                        member.getNickname(),
                        member.getTag(),
                        member.getPassword(),
                        Collections.emptyList()
                )))
                .cookie(accessTokenCookie)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.tag").value(member.getTag()))
                .andExpect(cookie().maxAge("accessToken", 0)); // 쿠키 만료 확인

        Optional<Member> deletedMember = memberRepository.findById(member.getId());
        assertThat(deletedMember).isEmpty();
    }

    @Test
    @DisplayName("access token 재발급 - 정상")
    public void reGenerateAccessToken_success() throws Exception {
        //회원 생성
        Member member = memberFixture.createMember(1);

        //로그인하여 액세스 토큰 쿠키 받기
        loginAndGetAccessTokenCookie("test1@example.com", "password123");

        //apiKey 멤버에서 가져오기
        String apiKey = member.getMemberInfo().getApiKey();

        //액세스 토큰 재발급 요청 바디
        String requestBody = String.format("""
        {
            "refreshToken": "%s"
        }
        """, apiKey);

        //재발급 api 호출 및 검증
        mockMvc.perform(post("/api/v1/members/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Access Token 재발급 성공"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.apikey").value(apiKey))
                .andExpect(cookie().exists("accessToken"))
                .andDo(print());
    }

    @Test
    @DisplayName("내 정보 조회 - 정상")
    public void getMyInfo_success() throws Exception {
        Member member = memberFixture.createMember(1);
        MemberInfo memberInfo = member.getMemberInfo();

        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getPassword(),
                Collections.emptyList()
        );

        mockMvc.perform(get("/api/v1/members/me")
                        .with(user(securityUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("유저 정보 반환 성공"))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.email").value(memberInfo.getEmail()))
                .andExpect(jsonPath("$.data.profileImage").value(memberInfo.getProfileImageUrl()))
                .andExpect(jsonPath("$.data.bio").value(memberInfo.getBio()));
    }

    @Test
    @DisplayName("비밀번호 유효성 검사 - 정상")
    public void checkPasswordValidity_success() throws Exception {
        Member member = memberFixture.createMember(1);
        MemberInfo memberInfo = member.getMemberInfo();
        String rawPassword = "password123"; //평문 비밀번호

        String requestBody = String.format("""
        {
            "password": "%s"
        }
        """, rawPassword);

        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getPassword(),
                Collections.emptyList()
        );

        mockMvc.perform(post("/api/v1/members/auth/verify-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .cookie(loginAndGetAccessTokenCookie(memberInfo.getEmail(), rawPassword))
                    .with(user(securityUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("비밀번호 유효성 반환 성공"))
                .andExpect(jsonPath("$.data.verified").value(true))    ;

    }

    @Test
    @DisplayName("비밀번호 유효성 검사 - 잘못된 비밀번호")
    public void checkPasswordValidity_wrongPassword() throws Exception {
        Member member = memberFixture.createMember(1);
        MemberInfo memberInfo = member.getMemberInfo();
        String wrongPassword = "wrongPassword!"; // 틀린 비밀번호

        String requestBody = String.format("""
    {
        "password": "%s"
    }
    """, wrongPassword);

        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getPassword(),
                Collections.emptyList()
        );

        mockMvc.perform(post("/api/v1/members/auth/verify-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(loginAndGetAccessTokenCookie(memberInfo.getEmail(), "password123")) // 실제 맞는 비밀번호로 로그인
                        .with(user(securityUser)))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.verified").value(false));
    }

    @Test
    @DisplayName("비밀번호 유효성 검사 - 인증되지 않은 사용자")
    public void checkPasswordValidity_unauthorized() throws Exception {
        String requestBody = """
    {
        "password": "password123"
    }
    """;

        mockMvc.perform(post("/api/v1/members/auth/verify-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized()); // 인증 없으면 401
    }

    @Test
    @DisplayName("비밀번호 유효성 검사 - 빈 비밀번호")
    public void checkPasswordValidity_blankPassword() throws Exception {
        Member member = memberFixture.createMember(1);
        MemberInfo memberInfo = member.getMemberInfo();

        String requestBody = """
    {
        "password": ""
    }
    """;

        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getNickname(),
                member.getTag(),
                member.getPassword(),
                Collections.emptyList()
        );

        mockMvc.perform(post("/api/v1/members/auth/verify-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(loginAndGetAccessTokenCookie(memberInfo.getEmail(), "password123"))
                        .with(user(securityUser)))
                .andExpect(status().isBadRequest())  // @NotBlank 검증 실패 예상
                .andExpect(jsonPath("$.code").value(400)); // 상세 메시지는 DTO의 @NotBlank 메시지에 따라 다름
    }


    private Cookie loginAndGetAccessTokenCookie(String email, String password) throws Exception {
        String loginRequestBody = String.format("""
        {
            "email": "%s",
            "password": "%s"
        }
        """, email, password);

        return  mockMvc.perform(post("/api/v1/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andDo(print())
                .andReturn()
                .getResponse()
                .getCookie("accessToken");
    }
}
