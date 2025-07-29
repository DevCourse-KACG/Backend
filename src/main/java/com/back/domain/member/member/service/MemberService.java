package com.back.domain.member.member.service;

import com.back.domain.api.service.ApiKeyService;
import com.back.domain.auth.service.AuthService;
import com.back.domain.member.member.MemberType;
import com.back.domain.member.member.dto.MemberAuthResponse;
import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberLoginDto;
import com.back.domain.member.member.dto.MemberWithdrawMembershipResponse;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberInfoRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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

    //회원가입 메인 메소드
    public MemberAuthResponse register(MemberDto dto) {
        validateDuplicate(dto);
        String tag = createTag(dto);
        Member member = createAndSaveMember(dto, tag);
        String apiKey = apiKeyService.generateApiKey();
        createAndSaveMemberInfo(dto, member, apiKey);

        String accessToken = generateAccessToken(member);

        return new MemberAuthResponse(apiKey, accessToken);
    }

    //로그인 메인 메소드
    public MemberAuthResponse login(@Valid MemberLoginDto memberLoginDto) {
        Optional<MemberInfo> memberInfo = memberInfoRepository.findByEmail(memberLoginDto.email());
        Member member = validateUserLogin(memberInfo);
        validateRightPassword(memberLoginDto.password(), member);

        String apiKey = member.getMemberInfo().getApiKey();
        String accessToken = authService.generateAccessToken(member);

        return new MemberAuthResponse(apiKey, accessToken);
    }

    //회원 탈퇴 메인 메소드
    public MemberWithdrawMembershipResponse withdrawMembership(String nickname, String tag) {
        Member member = findByNicknameAndTag(nickname, tag);
        MemberInfo memberInfo = member.getMemberInfo();

        deleteMember(member);

        return new MemberWithdrawMembershipResponse(member.getNickname(), member.getTag());
    }

    private void validateDuplicate(MemberDto dto) {
        //이메일 중복 확인
        String email = dto.email().toLowerCase();
        if (memberInfoRepository.findByEmail(email).isPresent()) {
            throw new ServiceException(400, "이미 사용 중인 이메일입니다.");
        }
    }

    private String createTag(MemberDto dto) {
        //태그 생성
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
        //멤버 db 저장
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
        //멤버 인포 저장
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

    private Member validateUserLogin(Optional<MemberInfo> memberInfo) {
        //이메일 오류
        if (memberInfo.isEmpty()) {
            throw new ServiceException(400, "이메일과 비밀번호가 맞지 않습니다.");
        }

        return memberInfo.get().getMember();
    }

    private void validateRightPassword(String password, Member member) {
        //비밀번호 오류
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new ServiceException(400, "이메일과 비밀번호가 맞지 않습니다.");
        }
    }

    public Map<String, Object> payload(String accessToken) {
        //토큰 파싱
        return authService.payload(accessToken);
    }


    public Member findByEmail(String email) {
        Optional<MemberInfo> memberInfo = memberInfoRepository.findByEmail(email);

        if (memberInfo.isEmpty()) {
            throw new ServiceException(400, "사용자를 찾을 수 없습니다.");
        }

        return memberInfo.get().getMember();
    }

    private void deleteMember(Member member) {
        memberRepository.delete(member);
    }

    public Member findByNicknameAndTag(String nickname, String tag) {
        Optional<Member> optionalMember = memberRepository.findByNicknameAndTag(nickname, tag);

        if (optionalMember.isEmpty()) {
            throw new ServiceException(400, "해당 닉네임의 사용자를 찾을 수 없습니다.");
        }

        return optionalMember.get();
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public String generateAccessToken(Member member) {
        return authService.generateAccessToken(member);
    }

    public Member findByApiKey(String apiKey) {
        Optional<MemberInfo> optionalMemberInfo = memberInfoRepository.findByApiKey(apiKey);

        if (optionalMemberInfo.isEmpty()) {
            throw new ServiceException(400, "사용자를 찾을 수 없습니다.");
        }

        return optionalMemberInfo.get().getMember();
    }

    public boolean isValidApiKey(String apiKey) {

    }
}