package com.back.domain.member.member.service;

import com.back.domain.api.service.ApiKeyService;
import com.back.domain.auth.service.AuthService;
import com.back.domain.member.member.MemberType;
import com.back.domain.member.member.dto.MemberAuthResponse;
import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberLoginDto;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberInfoRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.config.jwt.JwtProperties;
import com.back.global.exception.ServiceException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final ApiKeyService apiKeyService;
    private final AuthService authService;
    private final JwtProperties jwtProperties;

    public MemberAuthResponse register(MemberDto dto) {
        validateDuplicate(dto);
        String tag = createTag(dto);
        Member member = createAndSaveMember(dto, tag);
        String apiKey = apiKeyService.generateApiKey();
        MemberInfo memberInfo = createAndSaveMemberInfo(dto, member, apiKey);

        String accessToken = authService.generateAccessToken(apiKey);

        return new MemberAuthResponse(apiKey, accessToken);
    }


    private void validateDuplicate(MemberDto dto) {
        String email = dto.email().toLowerCase();
        if (memberInfoRepository.findByEmail(email).isPresent()) {
            throw new ServiceException(400, "이미 사용 중인 이메일입니다.");
        }
    }

    private String createTag(MemberDto dto) {
        if (memberRepository.findByNickname(dto.nickname()).isPresent()) {
            String tag;
            do {
                tag = UUID.randomUUID().toString().substring(0, 6);
            } while (memberRepository.existsByNicknameAndTag(dto.nickname(), tag));
            return tag;
        }
        return null;

    }

    private Member createAndSaveMember(MemberDto dto, String tag) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(dto.password());

        Member member = Member.builder()
                .nickname(dto.nickname())
                .password(hashedPassword)
                .tag(tag)
                .memberType(MemberType.MEMBER)
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

    public MemberAuthResponse login(@Valid MemberLoginDto memberLoginDto) {
        Optional<MemberInfo> memberInfo = memberInfoRepository.findByEmail(memberLoginDto.email());
        Member member = validateUserLogin(memberInfo);
        validateRightPassword(memberLoginDto.password(), member);

        String apiKey = member.getMemberInfo().getApiKey();
        String accessToken = authService.generateAccessToken(apiKey);

        return new MemberAuthResponse(apiKey, accessToken);
    }

    private Member validateUserLogin(Optional<MemberInfo> memberInfo) {
        if (memberInfo.isEmpty()) {
            throw new ServiceException(400, "이메일과 비밀번호가 맞지 않습니다.");
        }

        return memberInfo.get().getMember();
    }

    private void validateRightPassword(String password, Member member) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new ServiceException(400, "이메일과 비밀번호가 맞지 않습니다.");
        }
    }

    public Map<String, Object> payload(String accessToken) {
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.getJwt().getSecretKey().getBytes());
        System.out.println(secretKey);
        return Jwts.parser()
                .verifyWith(secretKey)
            .build()
                .parseSignedClaims(accessToken).getPayload();
    }

//    public Map<String, Object> payload(String accessToken) {
//        try {
//            return Jwts.parser()
//                    .setSigningKey(jwtProperties.getJwt().getSecretKey().getBytes())
//                    .parseClaimsJws(accessToken)
//                    .getBody();
//        } catch (Exception e) {
//            // 토큰이 유효하지 않을 경우 null 반환하거나 예외 처리
//            return null;
//        }
//    }


    public Member findByEmail(String email) {
        Optional<MemberInfo> memberInfo = memberInfoRepository.findByEmail(email);

        return memberInfo.get().getMember();
    }
}
