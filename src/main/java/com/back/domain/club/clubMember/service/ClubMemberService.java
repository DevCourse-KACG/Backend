package com.back.domain.club.clubMember.service;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.dtos.ClubMemberDtos;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.service.MemberService;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.exception.ServiceException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClubMemberService {
    private final ClubMemberRepository clubMemberRepository;
    private final ClubService clubService;
    private final MemberService memberService;

    /**
     * 클럽 멤버의 역할을 확인합니다.
     * @param clubId 클럽 ID
     * @param memberId 멤버 ID
     * @param roles 요청된 역할 배열
     * @return 요청된 역할 중 하나라도 일치하면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public boolean checkMemberRole(Long clubId, Long memberId, ClubMemberRole[] roles) {
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));
        Member member = memberService.findById(memberId)
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new ServiceException(404, "클럽 멤버가 존재하지 않습니다."));

        // 요청된 역할이 클럽 멤버의 역할 중 하나인지 확인
        for (ClubMemberRole role : roles) {
            if (clubMember.getRole() == role) {
                return true; // 역할이 일치하면 true 반환
            }
        }
        return false; // 일치하는 역할이 없으면 false 반환
    }

    /**
     * 클럽에 멤버를 추가합니다.
     * @param clubId 클럽 ID
     * @param member 추가할 멤버
     * @param role 클럽 멤버 역할
     */
    @Transactional
    public ClubMember addMemberToClub(Long clubId, Member member, ClubMemberRole role) {
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));

        ClubMember clubMember = ClubMember.builder()
                .member(member)
                .role(role) // 기본 역할은 MEMBER
                .state(ClubMemberState.INVITED) // 기본 상태는 INVITED
                .build();

        club.addClubMember(clubMember);

        return clubMemberRepository.save(clubMember);
    }

    /**
     * 클럽에 멤버를 추가합니다. 요청된 이메일을 기반으로 중복된 멤버는 제외하고 추가합니다.
     * @param clubId 클럽 ID
     * @param reqBody 클럽 멤버 등록 요청 DTO
     */
    @Transactional
    public void addMembersToClub(Long clubId, ClubMemberDtos.ClubMemberRegisterRequest reqBody) {
        Club club = clubService.getClubById(clubId).orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));

        // TODO : 유저 권한 체크

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
                    Member member = memberService.findMemberByEmail(memberInfo.email());

                    ClubMember clubMember = ClubMember.builder()
                            .member(member)
                            .role(ClubMemberRole.fromString(memberInfo.role().toUpperCase()))
                            .state(ClubMemberState.INVITED)
                            .build();

                    club.addClubMember(clubMember);
                    clubMemberRepository.save(clubMember);
                });

    }

    /**
     * 클럽에서 멤버를 탈퇴시킵니다.
     * @param clubId 클럽 ID
     * @param memberId 탈퇴할 멤버 ID
     */
    @Transactional
    public void withdrawMemberFromClub(Long clubId, Long memberId) {
        // TODO : 유저 권한 체크 (본인, HOST)

        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));
        Member member = memberService.findMemberById(memberId)
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));

        // 클럽에서 멤버 탈퇴
        clubMember.updateState(ClubMemberState.WITHDRAWN);
        clubMemberRepository.save(clubMember);
    }

    /**
     * 클럽 멤버의 역할을 변경합니다.
     * @param clubId 클럽 ID
     * @param memberId 멤버 ID
     * @param role 변경할 역할
     */
    @Transactional
    public void changeMemberRole(Long clubId, Long memberId, @NotBlank String role) {
        // TODO : 유저 권한 체크

        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));
        Member member = memberService.findMemberById(memberId)
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new ServiceException(404, "멤버가 존재하지 않습니다."));

        // 역할 변경
        clubMember.updateRole(ClubMemberRole.fromString(role.toUpperCase()));
        clubMemberRepository.save(clubMember);
    }

    /**
     * 클럽의 멤버 목록을 조회합니다.
     * @param clubId 클럽 ID
     * @param state 상태 필터링 (선택적)
     * @return 클럽 멤버 목록 DTO
     */
    @Transactional(readOnly = true)
    public ClubMemberDtos.ClubMemberResponse getClubMembers(Long clubId, String state) {
        // 클럽 확인
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));

        // 클럽멤버 목록 반환
        List<ClubMember> clubMembers;
        if(state != null){
            clubMembers = clubMemberRepository.findByClubAndState(club, ClubMemberState.fromString(state));
        }
        else {
            clubMembers = clubMemberRepository.findByClub(club);
        }

        // 클럽 멤버 정보를 DTO로 변환
        List<ClubMemberDtos.ClubMemberInfo> memberInfos = clubMembers.stream()
                .map(clubMember -> {
                    Member m = clubMember.getMember();

                    return new ClubMemberDtos.ClubMemberInfo(
                            clubMember.getId(),
                            m.getId(),
                            m.getNickname(),
                            m.getTag(),
                            clubMember.getRole(),
                            Optional.ofNullable(m.getMemberInfo())
                                    .map(MemberInfo::getEmail)
                                    .orElse(""),
                            m.getMemberType(),
                            Optional.ofNullable(m.getMemberInfo())
                                    .map(MemberInfo::getProfileImageUrl)
                                    .orElse(""),
                            clubMember.getState()
                    );
                }).toList();

        return new ClubMemberDtos.ClubMemberResponse(memberInfos);

    }
}
