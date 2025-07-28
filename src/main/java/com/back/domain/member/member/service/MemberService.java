package com.back.domain.member.member.service;

import com.back.domain.api.service.ApiKeyService;
import com.back.domain.auth.service.AuthService;
import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberRegisterResponse;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberInfoRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
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
        String apiKey = apiKeyService.generateApiKey();
        MemberInfo memberInfo = createAndSaveMemberInfo(dto, member, apiKey);

        String accessToken = authService.generateAccessToken(apiKey);

        return new MemberRegisterResponse(apiKey, accessToken);
    }


    private void validateDuplicate(MemberDto dto) {
        if (memberInfoRepository.findByEmail(dto.email()).isPresent()) {
            throw new ServiceException(400, "이미 사용 중인 이메일입니다.");
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

    private MemberInfo createAndSaveMemberInfo(MemberDto dto, Member member, String apiKey) {
        MemberInfo info = MemberInfo.builder()
                .email(dto.email())
                .bio(dto.bio())
                .profileImageUrl("")
                .member(member)
                .apiKey(apiKey)
                .build();

        MemberInfo savedInfo = memberInfoRepository.save(info);

        member.setMemberInfo(savedInfo);

        return savedInfo;

    }
}
