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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubMemberService {
    private final ClubMemberRepository clubMemberRepository;
    private final ClubService clubService;
    private final MemberService memberService;

    public void addMemberToClub(Long clubId, ClubMemberDtos.ClubMemberRegisterRequest reqBody) {
        Club club = clubService.getClubById(clubId).orElseThrow(() -> new IllegalArgumentException("클럽이 존재하지 않습니다."));

        reqBody.members().forEach(memberInfo -> {
            // 멤버 정보가 존재하는지 확인
            Member member = memberService.getMemberByEmail(memberInfo.email())
                    .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 멤버가 존재하지 않습니다: " + memberInfo.email()));

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
