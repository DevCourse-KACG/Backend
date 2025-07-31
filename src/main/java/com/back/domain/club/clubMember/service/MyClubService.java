package com.back.domain.club.clubMember.service;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.global.enums.ClubMemberState;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyClubService {
    private final ClubService clubService;
    private final ClubMemberRepository clubMemberRepository;
    private final Rq rq;

    /**
     * 클럽 초대를 수락하거나 거절하는 메서드
     * @param clubId 클럽 ID
     * @param accept 초대 수락 여부 (true면 수락, false면 거절)
     * @return 클럽 정보
     */
    @Transactional
    public Club handleClubInvitation(Long clubId, boolean accept) {
        // 멤버 가져오기
        Member user = rq.getActor();
        // 클럽 ID로 클럽 가져오기
        Club club = clubService.getClubById(clubId)
                .orElseThrow(() -> new ServiceException(404, "클럽이 존재하지 않습니다."));
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, user)
                .orElseThrow(() -> new ServiceException(400, "클럽 초대 상태가 아닙니다."));

        // 클럽 멤버 상태 확인
        if (clubMember.getState() == ClubMemberState.JOINING) // 가입 중인 경우
            throw new ServiceException(400, "이미 가입 상태입니다.");
        else if (clubMember.getState() != ClubMemberState.INVITED) // 초대 상태가 아닌 경우 (가입 신청, 탈퇴)
            throw new ServiceException(400, "클럽 초대 상태가 아닙니다.");

        // 클럽 멤버 상태 업데이트
        if(accept) {
            clubMember.updateState(ClubMemberState.JOINING); // 초대 수락
        } else {
            club.getClubMembers().remove(clubMember); // 클럽에서 멤버 제거
            clubMemberRepository.delete(clubMember); // 초대 거절
        }

        return club; // 클럽 반환
    }
}
