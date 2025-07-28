package com.back.domain.club.club.service;

import com.back.domain.club.club.dtos.ClubControllerDtos;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.member.member.entity.Member;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.enums.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    //private final MemberService memberService;

    /**
     * 마지막으로 생성된 클럽을 반환합니다.
     * @return 마지막으로 생성된 클럽
     */
    public Club getLastCreatedClub() {
        return clubRepository.findFirstByOrderByIdDesc()
                .orElseThrow(() -> new IllegalStateException("마지막으로 생성된 클럽이 없습니다."));
    }

    /**
     * 클럽을 생성합니다.
     * @param club 클럽 정보
     * @return 생성된 클럽
     */
    @Transactional
    public Club createClub(Club club) {
        return clubRepository.save(club);
    }

    @Transactional
    public Club createClub(ClubControllerDtos.CreateClubRequest reqBody) {
        // 클럽 생성
        Club club = Club.builder()
                .name(reqBody.name())
                .bio(reqBody.bio())
                .category(ClubCategory.fromString(reqBody.category().toUpperCase()))
                .mainSpot(reqBody.mainSpot())
                .maximumCapacity(reqBody.maximumCapacity())
                .eventType(EventType.fromString(reqBody.eventType().toUpperCase()))
                .startDate(LocalDate.parse(reqBody.startDate()))
                .endDate(LocalDate.parse(reqBody.endDate()))
                .isPublic(reqBody.isPublic())
                .leaderId(reqBody.leaderId())
                .build();

        // 클럽 멤버 설정
        Arrays.stream(reqBody.clubMembers()).forEach(memberInfo -> {
            // 멤버 ID로 Member 엔티티 조회
//            Member member = memberService.findById(memberInfo.id())
//                    .orElseThrow(() -> new NoSuchElementException("ID " + memberInfo.id() + "에 해당하는 멤버를 찾을 수 없습니다."));

            Member member = new Member(); // TODO : 임시로 Member 객체 생성, 실제로는 memberService.findById(memberInfo.id())를 사용해야 함

            // ClubMember 엔티티 생성
            ClubMember clubMember = ClubMember.builder()
                    .member(member)
                    .role(ClubMemberRole.fromString(memberInfo.role().toUpperCase())) // 문자열을 Enum으로 변환
                    .state(ClubMemberState.INVITED) // 초기 상태는 INVITED로 설정
                    .build();

            // 연관관계 편의 메서드를 사용하여 Club에 ClubMember 추가
            club.addClubMember(clubMember);
        });

        return createClub(club);
    }
}
