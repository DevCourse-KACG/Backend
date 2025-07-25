package com.back.domain.member.member;

import com.back.domain.member.member.controller.MemberController;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import support.MemberFixture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class MemberControllerTest {
    MemberFixture memberFixture;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberController memberController;

    @Autowired
    private MockMvc mockMvc;

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

//    @Test
//    @DisplayName("회원가입 - 닉네임 중복 시 예외 발생")
//    public void registerWithDuplicateNicknameThrowsException() {
//        String duplicateNickname = "테스트유저0";
//        given(memberRepository.existsByNickname(duplicateNickname)).willReturn(true);
//
//        Member member = Member.builder()
//                .nickname(duplicateNickname)
//                .password("password123")
//                .memberInfo(MemberFixture.createMemberInfo())
//                .presets(null)
//                .build();
//
//
//        assertThrows(
//                DuplicateNicknameException.class,
//                () -> memberRepository.save(member)
//        );
//    }

    @Test
    @DisplayName("회원가입 - 정상 기입 / POST 정상 작동")
    public void memberPostTest() throws  Exception {
        //controller에게 post를 보내면 정상적으로 처리되었다는 메세지가 반환되어야함
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
}
