package com.back.domain.club.clubMember.service;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.dtos.ClubMemberDtos;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClubMemberService {
    private final ClubMemberRepository clubMemberRepository;
    private final ClubService clubService;
    private final MemberService memberService;

    public void addMemberToClub(Long clubId, Member member, ClubMemberRole role) {
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));

        ClubMember clubMember = ClubMember.builder()
                .member(member)
                .role(role) // 기본 역할은 MEMBER
                .state(ClubMemberState.INVITED) // 기본 상태는 JOINED
                .build();

        club.addClubMember(clubMember);

        clubMemberRepository.save(clubMember);
    }

    public void addMembersToClub(Long clubId, ClubMemberDtos.ClubMemberRegisterRequest reqBody) {
        Club club = clubService.getClubById(clubId).orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));

        // 요청된 이메일 추출
        List<String> requestEmails = reqBody.members().stream()
                .map(ClubMemberDtos.ClubMemberRegisterInfo::email)
                .toList();

        // 이미 가입된 멤버 이메일만 조회 (IN 쿼리)
        Set<String> existingEmails = new HashSet<>(
                clubMemberRepository.findExistingEmails(clubId, requestEmails)
        );

        // 중복 제외한 새로운 멤버만 추가
        reqBody.members().stream()
                .distinct() // 중복 제거
                .filter(memberInfo -> !existingEmails.contains(memberInfo.email()))
                .forEach(memberInfo -> {
                    Member member = memberService.findByEmail(memberInfo.email());

                    ClubMember clubMember = ClubMember.builder()
                            .member(member)
                            .role(ClubMemberRole.fromString(memberInfo.role().toUpperCase()))
                            .state(ClubMemberState.INVITED)
                            .build();

                    club.addClubMember(clubMember);
                    clubMemberRepository.save(clubMember);
                });

    }
}
