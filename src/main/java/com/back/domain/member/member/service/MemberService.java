package com.back.domain.member.member.service;

import com.back.domain.api.service.ApiKeyService;
import com.back.domain.auth.service.AuthService;
import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberRegisterResponse;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberInfoRepository;
import com.back.domain.member.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final ApiKeyService apiKeyService;
    private final AuthService authService;

    public MemberRegisterResponse register(MemberDto memberDto) {
        if (memberRepository.findByNickname(memberDto.nickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        if (memberInfoRepository.findByEmail(memberDto.email()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(memberDto.password());

        Member member = Member.builder()
                .memberInfo(null)
                .nickname(memberDto.nickname())
                .password(hashedPassword)
                .build();

        memberRepository.save(member);

        MemberInfo memberInfo = MemberInfo.builder()
                .email(memberDto.email())
                .bio(memberDto.bio())
                .profileImageUrl("")
                .member(member)
                .build();

        memberInfoRepository.save(memberInfo);

        //Todo: 양방향 관계 세팅

        String apikey = apiKeyService.generateApiKey(member.getId());
        String accessToken = authService.generateAccessToken(apikey);

        return new MemberRegisterResponse(apikey, accessToken);
    }
}
