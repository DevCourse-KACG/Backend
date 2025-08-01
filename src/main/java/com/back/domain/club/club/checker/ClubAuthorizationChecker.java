package com.back.domain.club.club.checker;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.enums.ClubMemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Objects;

@Component("clubAuthorizationChecker")
@RequiredArgsConstructor
public class ClubAuthorizationChecker {
    private final MemberService memberService;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubService clubService;

    /**
     * 모임이 존재하는지 확인
     * @param clubId 모임 ID
     * @return 모임 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean isClubExists(Long clubId) {
        return clubRepository.existsById(clubId);
    }

    /**
     * 모임 호스트 권한이 있는지 확인
     * @param clubId 모임 ID
     * @param memberId 로그인 유저 ID
     * @return 모임 호스트 권한 여부
     */
    @Transactional(readOnly = true)
    public boolean isClubHost(Long clubId, Long memberId) {
        Club club = getClub(clubId);

        return Objects.equals(club.getLeaderId(), memberId);
    }

    /**
     * 모임 호스트 권한이 있는지 확인 (활성화된 모임에 대해서만)
     * @param clubId 모임 ID
     * @param memberId 로그인 유저 ID
     * @return 모임 호스트 권한 여부
     */
    @Transactional(readOnly = true)
    public boolean isActiveClubHost(Long clubId, Long memberId) {
        Club club = getValidAndActiveClub(clubId);

        return Objects.equals(club.getLeaderId(), memberId);
    }

    /**
     * 모임 매니저의 역할 확인 (활성화된 모임에 대해서만)
     * @param clubId 모임 ID
     * @param memberId 로그인 유저 ID
     * @return 모임 매니저 권한 여부
     */
    @Transactional(readOnly = true)
    public boolean isActiveClubManager(Long clubId, Long memberId) {
        Club club = getValidAndActiveClub(clubId);
        Member member = getMember(memberId);
        ClubMember clubMember = getClubMember(club, member);

        return clubMember.getRole() == ClubMemberRole.MANAGER;
    }

    /**
     * 모임 호스트 또는 매니저 권한 확인 (활성화된 모임에 대해서만)
     * @param clubId 모임 ID
     * @param memberId 로그인 유저 ID
     * @return 모임 호스트 또는 매니저 권한 여부
     */
    @Transactional(readOnly = true)
    public boolean isActiveClubManagerOrHost(Long clubId, Long memberId) {
        Club club = getValidAndActiveClub(clubId);
        Member member = getMember(memberId);
        ClubMember clubMember = getClubMember(club, member);

        return clubMember.getRole() == ClubMemberRole.MANAGER
                || Objects.equals(club.getLeaderId(), memberId);
    }

    /**
     * 모임 참여자 여부 확인
     * @param clubId 모임 ID
     * @param memberId 로그인 유저 ID
     * @return 모임 참여자 여부
     */
    @Transactional(readOnly = true)
    public boolean isClubMember(Long clubId, Long memberId) {
        Club club = getClub(clubId);
        Member member = getMember(memberId);
        return clubMemberRepository.existsByClubAndMember(club, member);
    }

    /**
     * 로그인 유저가 요청한 멤버 ID와 일치하는지 확인
     * @param targetMemberId 멤버 ID
     * @param currentUserId 로그인 유저 ID
     * @return 로그인 유저 - 요청한 멤버 ID 일치 여부
     */
    @Transactional(readOnly = true)
    public boolean isSelf(Long targetMemberId, Long currentUserId) {
        return Objects.equals(targetMemberId, currentUserId);
    }


    // 핼퍼 메서드 ----------

    /**
     * 모임 ID로 모임 조회
     * @param clubId 모임 ID
     * @return 모임 엔티티
     */
    private Club getClub(Long clubId) {
        return clubService.getClubById(clubId)
                .orElseThrow(() -> new NoSuchElementException("모임이 존재하지 않습니다."));
    }

    /**
     * 모임 ID로 활성화된 모임 조회
     * @param clubId 모임 ID
     * @return 활성화된 모임 엔티티
     */
    private Club getValidAndActiveClub(Long clubId) {
        return clubService
                .getValidAndActiveClub(clubId)
                .orElseThrow(() -> new NoSuchElementException("모임이 존재하지 않거나 활성화되지 않았습니다."));
    }

    /**
     * 멤버 ID로 멤버 조회
     * @param memberId 멤버 ID
     * @return 멤버 엔티티
     */
    private Member getMember(Long memberId) {
        return memberService.findMemberById(memberId)
                .orElseThrow(() -> new NoSuchElementException("멤버가 존재하지 않습니다."));
    }

    /**
     * 클럽과 멤버로 클럽 멤버 조회
     * @param club 모임 엔티티
     * @param member 멤버 엔티티
     * @return 클럽 멤버 엔티티
     */
    private ClubMember getClubMember(Club club, Member member) {
        return clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));
    }
}
