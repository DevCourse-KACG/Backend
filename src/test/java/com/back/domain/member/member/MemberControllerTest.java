package com.back.domain.member.member;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;
import support.MemberFixture;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RecordApplicationEvents
public class MemberControllerTest {
    MemberFixture memberFixture;
    MemberRepository memberRepository;

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
    @DisplayName("회원가입 - 닉네임 중복 시 예외 발생")
    public void registerWithDuplicateNicknameThrowsException() {
        String duplicateNickname = "테스트유저0";
        given(memberRepository.existsByNickname(duplicateNickname)).willReturn(true);

        Member member = Member.builder()
                .nickname(duplicateNickname)
                .password("password123")
                .memberInfo(MemberFixture.createMemberInfo())
                .presets(null)
                .build();

        assertThrows(
                DuplicateNicknameException.class,
                () -> memberService.register(member)
        );
    }
}
