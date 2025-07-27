package com.back.domain.member.member.service;

import com.back.domain.api.service.ApiKeyService;
import com.back.domain.auth.service.AuthService;
import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberResisterResponse;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    final MemberRepository memberRepository;
    final ApiKeyService apiKeyService;
    final AuthService authService;

    public MemberResisterResponse register(MemberDto memberDto) {
        if (memberRepository.findByNickname(memberDto.nickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        Member member = Member.builder()
                .memberInfo(null)
                .nickname(memberDto.nickname())
                .password(memberDto.password())
                .build();

        memberRepository.save(member);

        String apikey = apiKeyService.generateApiKey(member.getId());
        String accessToken = authService.generateAccessToken(apikey);

        return new MemberResisterResponse(apikey, accessToken);
    }
}
