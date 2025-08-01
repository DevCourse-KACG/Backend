package com.back.domain.club.clubLink.service;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubLink.dtos.ClubLinkDtos;
import com.back.domain.club.clubLink.entity.ClubLink;
import com.back.domain.club.clubLink.repository.ClubLinkRepository;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubLinkService {
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubLinkRepository clubLinkRepository;

    @Transactional
    public ClubLinkDtos.CreateClubLinkResponse createClubLink(Member user, Long clubId) {
        Club club = isClubExist(clubId);

        //권한 체크하여 Host, Manager이 아닐 시 에러
        validateClubManagerOrHost(club, user);

        LocalDateTime now = LocalDateTime.now();

        //기존 활성 링크가 있을 시 해당 링크 반환
        Optional<ClubLink> existingLink = clubLinkRepository.findByClubAndExpiresAtAfter(club, now);
        if (existingLink.isPresent()) {
            String existingCode = existingLink.get().getInviteCode();
            return new ClubLinkDtos.CreateClubLinkResponse(existingCode);
        }

        //UUID 기반 초대 코드 생성
        String inviteCode = UUID.randomUUID().toString();

        LocalDateTime expireAt = now.plusDays(7);

        //클럽 링크 객체 생성 및 db 저장
        ClubLink clubLink = ClubLink.builder()
                .inviteCode(inviteCode)
                .createdAt(now)
                .expiresAt(expireAt)
                .club(club)
                .build();

        clubLinkRepository.save(clubLink);

        String link = "https://supplies.com/clubs/invite?token=" + inviteCode;

        return new ClubLinkDtos.CreateClubLinkResponse(link);
    }

    public ClubLinkDtos.CreateClubLinkResponse getExistingClubLink(Member user, @Positive Long clubId) {
        Club club = isClubExist(clubId);

        //권한 체크하여 Host, Manager이 아닐 시 에러
        validateClubManagerOrHost(club, user);

        LocalDateTime now = LocalDateTime.now();

        ClubLink existingLink = clubLinkRepository.findByClubAndExpiresAtAfter(club, now)
                .orElseThrow(() -> new ServiceException(400, "활성화된 초대 링크를 찾을 수 없습니다."));

        return new ClubLinkDtos.CreateClubLinkResponse(existingLink.getInviteCode());
    }

    @Transactional
    public RsData<Object> applyToPrivateClubByToken(Member user, String token) {
        //토큰 유효성 확인
        ClubLink clubLink = clubLinkRepository.findByInviteCode(token)
                .orElseThrow(() -> new ServiceException(400, "초대 토큰이 유효하지 않습니다."));

        //토큰 만료 확인
        if (clubLink.isExpired()) {
            return new RsData<>(400, "초대 토큰이 만료되었습니다.", null);
        }

        Club club = clubLink.getClub();

        // 이미 가입한 경우 체크
        Optional<ClubMember> existingMemberOpt = clubMemberRepository.findByClubAndMember(club, user);

        if (existingMemberOpt.isPresent()) {
            ClubMember existingMember = existingMemberOpt.get();

            return switch (existingMember.getState()) {
                case JOINING -> new RsData<>(400, "이미 이 클럽에 가입되어 있습니다.", null);
                case APPLYING -> new RsData<>(400, "이미 이 클럽에 가입 신청 중입니다.", null);
                case INVITED -> new RsData<>(400, "이미 초대를 받은 상태입니다. 마이페이지에서 수락해주세요.", null);
                default -> new RsData<>(400, "해당 상태에서는 가입할 수 없습니다.", null);
            };
        }

        // 가입 처리
        ClubMember clubMember = ClubMember.builder()
                .member(user)
                .role(ClubMemberRole.PARTICIPANT)
                .state(ClubMemberState.APPLYING)
                .club(club)
                .build();

        clubMemberRepository.save(clubMember);

        return new RsData<>(200, "클럽에 성공적으로 가입되었습니다.", null);
    }

    //===============================기타 메서드================================

    public Club isClubExist(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(400, "해당 id의 클럽을 찾을 수 없습니다."));
    }

    public void validateClubManagerOrHost(Club club, Member user) {
        if (!clubMemberRepository.existsByClubAndMemberAndRoleIn(
                club,
                user,
                List.of(ClubMemberRole.MANAGER, ClubMemberRole.HOST))) {
            throw new ServiceException(400, "호스트나 매니저만 초대 링크를 관리할 수 있습니다.");
        }
    }
}