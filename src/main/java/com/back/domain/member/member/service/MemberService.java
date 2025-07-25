package com.back.domain.member.member.service;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    final MemberRepository memberRepository;

    public void register(MemberDto memberDto) {
        Member member = Member.builder()
                .memberInfo(null)
                .nickname(memberDto.nickname())
                .password(memberDto.password())
                .build();

        memberRepository.save(member);
    }
}
