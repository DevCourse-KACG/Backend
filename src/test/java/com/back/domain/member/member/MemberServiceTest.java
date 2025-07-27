package com.back.domain.member.member;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
public class MemberServiceTest {
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 - 닉네임 중복 시 예외 발생")
    public void registerWithDuplicateNicknameThrowsException() {
        MemberDto memberDto1 = new MemberDto("1", "pw1", "user", "안녕하세요");
        MemberDto memberDto2 = new MemberDto("2", "pw1", "user", "안녕하세요");

        memberService.register(memberDto1);

        assertThatThrownBy(() -> {
            memberService.register(memberDto2);
        }).isInstanceOf(IllegalArgumentException.class);
    }
}
