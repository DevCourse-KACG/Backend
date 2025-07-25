package com.back.domain.member.member.service;

import com.back.domain.api.service.ApiKeyService;
import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.dto.MemberResisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    final MemberRepository memberRepository;
    final ApiKeyService apiKeyService;

    public MemberResisterResponse register(MemberDto memberDto) {
        Member member = Member.builder()
                .memberInfo(null)
                .nickname(memberDto.nickname())
                .password(memberDto.password())
                .build();

        memberRepository.save(member);

        String apikey = apiKeyService.generateApiKey(member.getId());
        String accessToken = "";

        return new MemberResisterResponse(apikey, accessToken);
    }
}
