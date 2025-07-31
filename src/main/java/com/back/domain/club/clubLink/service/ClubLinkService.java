package com.back.domain.club.clubLink.service;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubLink.dtos.ClubLinkDtos;
import com.back.domain.club.clubLink.entity.ClubLink;
import com.back.domain.club.clubLink.repository.ClubLinkRepository;
import com.back.domain.member.member.entity.Member;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubLinkService {
    private final ClubRepository clubRepository;
    private final ClubLinkRepository clubLinkRepository;

    @Transactional
    public ClubLinkDtos.CreateClubLinkResponse createClubLink(Member user, Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(400, "해당 id의 클럽을 찾을 수 없습니다."));

        //UUID 기반 초대 코드 생성
        String inviteCode = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();
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
