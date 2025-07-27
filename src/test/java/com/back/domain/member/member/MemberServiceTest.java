package com.back.domain.member.member;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberServiceTest {
    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원가입 - 닉네임 중복 시 예외 발생")
    public void registerWithDuplicateNicknameThrowsException() {
        MemberDto memberDto1 = new MemberDto("1", "pw1", "user", "안녕하세요");
        MemberDto memberDto2 = new MemberDto("2", "pw1", "user", "안녕하세요");

        memberService.register(memberDto1);

        assertThatThrownBy(() -> {
            memberService.register(memberDto2);
        }).isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("회원가입 - 이메일 중복 시 예외 발생")
    public void registerWithDuplicateEmailThrowsException() {
        MemberDto memberDto1 = new MemberDto("1", "pw1", "user1", "안녕하세요");
        MemberDto memberDto2 = new MemberDto("1", "pw1", "user2", "안녕하세요");

        memberService.register(memberDto1);

        assertThatThrownBy(() -> {
            memberService.register(memberDto2);
        }).isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("회원가입 - 비밀번호 해싱 성공")
    public void registerPasswordHashingAndMatching() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "pw1";

        memberService.register(new MemberDto("1", rawPassword, "user1", "<>"));
        Member savedMember = memberRepository.findByNickname("user1").get();

        String savedHashedPassword = savedMember.getPassword();

        assertNotEquals(rawPassword, savedHashedPassword);

        assertTrue(encoder.matches(rawPassword, savedHashedPassword));

        assertFalse(encoder.matches("wrongPassword", savedHashedPassword));
    }
}
