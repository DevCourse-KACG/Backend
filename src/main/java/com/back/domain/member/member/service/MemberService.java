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

    public MemberRegisterResponse register(MemberDto dto) {
        validateDuplicate(dto);
        Member member = createAndSaveMember(dto);
        MemberInfo memberInfo = createAndSaveMemberInfo(dto, member);
        connectMemberAndInfo(member, memberInfo);

        String apiKey = apiKeyService.generateApiKey(member.getId());
        String accessToken = authService.generateAccessToken(apiKey);

        return new MemberRegisterResponse(apiKey, accessToken);
    }


    private void validateDuplicate(MemberDto dto) {
        if (memberRepository.findByNickname(dto.nickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        if (memberInfoRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private Member createAndSaveMember(MemberDto dto) {
        String hashedPassword = new BCryptPasswordEncoder().encode(dto.password());

        Member member = Member.builder()
                .nickname(dto.nickname())
                .password(hashedPassword)
                .build();

        return memberRepository.save(member);
    }

    private MemberInfo createAndSaveMemberInfo(MemberDto dto, Member member) {
        MemberInfo info = MemberInfo.builder()
                .email(dto.email())
                .bio(dto.bio())
                .profileImageUrl("")
                .member(member)
                .build();

        return memberInfoRepository.save(info);
    }

    private void connectMemberAndInfo(Member member, MemberInfo info) {
        // 양방향 관계 세팅
    }

}
