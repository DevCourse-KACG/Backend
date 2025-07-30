package com.back.domain.member.member.service;

import com.back.domain.api.service.ApiKeyService;
import com.back.domain.auth.service.AuthService;
import com.back.domain.member.member.MemberType;
import com.back.domain.member.member.dto.request.MemberLoginDto;
import com.back.domain.member.member.dto.request.MemberRegisterDto;
import com.back.domain.member.member.dto.request.UpdateMemberInfoDto;
import com.back.domain.member.member.dto.response.MemberAuthResponse;
import com.back.domain.member.member.dto.response.MemberDetailInfoResponse;
import com.back.domain.member.member.dto.response.MemberPasswordResponse;
import com.back.domain.member.member.dto.response.MemberWithdrawMembershipResponse;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberInfoRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.aws.S3Service;
import com.back.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final S3Service s3Service;

    //회원가입 메인 메소드
    public MemberAuthResponse register(MemberRegisterDto dto) {
        validateDuplicate(dto);
        String tag = createTag(dto.nickname());
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

    //유저 정보 반환 메소드
    public MemberDetailInfoResponse getUserInfo(Long id) {
        Member member = findById(id)
                .orElseThrow(() -> new ServiceException(400, "해당 id의 유저가 없습니다."));
        MemberInfo memberInfo = member.getMemberInfo();

        String nickname = member.getNickname();
        String tag = member.getTag();
        String email = memberInfo.getEmail();
        String bio = memberInfo.getBio();
        String profileImage = memberInfo.getProfileImageUrl();


        return new MemberDetailInfoResponse(nickname, email, bio, profileImage, tag);
    }

    //유저 정보 수정 메소드
    public MemberDetailInfoResponse updateInfo(Long id, UpdateMemberInfoDto dto, MultipartFile image) throws IOException {
        Member member = findById(id).orElseThrow(() ->
                new ServiceException(400, "해당 id의 유저가 없습니다."));
        MemberInfo memberInfo = member.getMemberInfo();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = member.getPassword();
        if (dto.password() != null && !dto.password().isBlank()) {
            password = encoder.encode(dto.password());
        }

        String nickname = (dto.nickname() != null) ? dto.nickname() : member.getNickname();
        String tag = (dto.nickname() != null) ? createTag(dto.nickname()) : member.getTag();
        String bio = (dto.bio() != null) ? dto.bio() : memberInfo.getBio();

        //프로필 이미지 없는 버전의 멤버, 멤버인포 생성
        member.updateInfo(nickname, tag, password);
        memberInfo.updateBio(bio);


        //S3 이미지 업로드
        if (image != null && !image.isEmpty()){
            String imageUrl = s3Service.upload(image, "member/" + memberInfo.getId() + "/profile");
            memberInfo.updateImageUrl(imageUrl);
        }

        member.setMemberInfo(memberInfo);
        memberRepository.save(member);
        memberInfoRepository.save(memberInfo);

        return new MemberDetailInfoResponse(member.getNickname(),
                memberInfo.getEmail(),
                memberInfo.getBio(),
                memberInfo.getProfileImageUrl(),
                member.getTag());
    }

    private void validateDuplicate(MemberRegisterDto dto) {
        //이메일 중복 확인
        String email = dto.email().toLowerCase();
        if (memberInfoRepository.findByEmail(email).isPresent()) {
            throw new ServiceException(400, "이미 사용 중인 이메일입니다.");
        }
    }

    private String createTag(String nickname) {
        //태그 생성
        String tag;
            do {
                tag = UUID.randomUUID().toString().substring(0, 6);
            } while (memberRepository.existsByNicknameAndTag(nickname, tag));

            return tag;
    }

    private Member createAndSaveMember(MemberRegisterDto dto, String tag) {
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

    private MemberInfo createAndSaveMemberInfo(MemberRegisterDto dto, Member member, String apiKey) {
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

    public MemberPasswordResponse checkPasswordValidity(Long memberId, String password) {
        Member member = findById(memberId)
                .orElseThrow(() -> new ServiceException(400, "해당 id로 유저를 찾을 수 없습니다."));

        try {
            validateRightPassword(password, member);
            return new MemberPasswordResponse(true);
        } catch (ServiceException e) {
            return new MemberPasswordResponse(false);

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
}