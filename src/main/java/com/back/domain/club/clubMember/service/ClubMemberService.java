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

import java.util.List;

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

        // 중복 체크
        List<String> existingMemberEmails = clubMemberRepository.findAllByClubId(clubId)
                .stream()
                .map(clubMember -> clubMember.getMember().getEmail())
                .toList();

        List<ClubMemberDtos.ClubMemberRegisterInfo> newMembers = reqBody.members().stream()
                .filter(memberInfo -> !existingMemberEmails.contains(memberInfo.email()))
                .toList();

        newMembers.forEach(memberInfo -> {
            // 멤버 정보가 존재하는지 확인
            Member member = memberService.findByEmail(memberInfo.email());

            // 클럽 멤버 생성
            ClubMember clubMember = ClubMember.builder()
                    .member(member)
                    .role(ClubMemberRole.fromString(memberInfo.role().toUpperCase()))
                    .state(ClubMemberState.INVITED) // 기본 상태는 초대됨
                    .build();

            // 클럽과 멤버 연결
            club.addClubMember(clubMember);

            // 클럽 멤버 저장
            clubMemberRepository.save(clubMember);
        });

    }
}
