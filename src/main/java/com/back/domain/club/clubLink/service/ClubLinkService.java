package com.back.domain.club.clubLink.service;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubLink.dtos.ClubLinkDtos;
import com.back.domain.club.clubLink.entity.ClubLink;
import com.back.domain.club.clubLink.repository.ClubLinkRepository;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.global.enums.ClubMemberRole;
import com.back.global.exception.ServiceException;
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
        //권한 체크하여 Host, Manager이 아닐 시 에러
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(400, "해당 id의 클럽을 찾을 수 없습니다."));

        if (!clubMemberRepository.existsByClubAndMemberAndRoleIn(
                club,
                user,
                List.of(ClubMemberRole.MANAGER, ClubMemberRole.HOST))) {
            throw new ServiceException(400, "호스트나 매니저만 초대 링크를 생성할 수 있습니다.");
        }

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
}
