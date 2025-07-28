package com.back.global.initData;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberInfoRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


/**
 * 테스트 환경의 초기 데이터 설정
 */
@Configuration
@Profile("test")
@RequiredArgsConstructor
public class TestInitData {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ScheduleRepository scheduleRepository;

    @Autowired
    @Lazy
    private TestInitData self;

    @Bean
    ApplicationRunner testInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
            self.work3();

            self.initDataForSchedule();
            self.scheduleInitData();
        };
    }

    @Transactional
    public void work1() {
        // 여기에 데이터 삽입 로직 작성
    }

    @Transactional
    public void work2() {
    }

    @Transactional
    public void work3() {
    }

    /**
     * 스케줄 관련 초기 데이터 설정
     * - 회원, 모임 초기 데이터 생길 시 삭제 예정
     */
    @Transactional
    public void initDataForSchedule() {
        // 회원
        Member member = Member.builder()
                .nickname("member1")
                .password("password1")
                .build();
        memberRepository.save(member);

        /*
        MemberInfo memberInfo = MemberInfo.builder()
                .email("member1@email.com")
                .bio("bio1")
                .member(member)
                .build();
        memberInfoRepository.save(memberInfo);
        member.setMemberInfo(memberInfo); // 양방향 설정 필요
        */

        // 장기 공개 모임 - 모집 중
//        Club club1 = Club.builder()
//                .name("산책 모임")
//                .category("산책")
//                .mainSpot("서울")
//                .maximumCapacity(25)
//                .recruitingStatus(true)
//                .eventType("장기")
//                .startDate(LocalDateTime.parse("2025-07-05T10:00:00"))
//                .endDate(LocalDateTime.parse("2025-08-30T15:00:00"))
//                .images("img2")
//                .isPublic(true)
//                .leaderId(member.getId())
//                .stats(true).build();
//        clubRepository.save(club1);
//
//        ClubMember clubMember1 = ClubMember.builder()
//                .member(member)
//                .role(ClubMemberRole.HOST)
//                .club(club1)
//                .build();
//        clubMemberRepository.save(clubMember1);
//
//        // 장기 비공개 모임 - 모집 마감
//        Club club2 = Club.builder()
//                .name("친구 모임")
//                .category("친목")
//                .mainSpot("강원도")
//                .maximumCapacity(4)
//                .recruitingStatus(false)
//                .eventType("장기")
//                .startDate(LocalDateTime.parse("2025-05-01T00:00:00"))
//                .endDate(LocalDateTime.parse("2026-12-31T23:59:59"))
//                .images("img1")
//                .isPublic(false)
//                .leaderId(member.getId())
//                .stats(true).build();
//        clubRepository.save(club2);
//
//        ClubMember clubMember2 = ClubMember.builder()
//                .member(member)
//                .role(ClubMemberRole.HOST)
//                .club(club2)
//                .build();
//        clubMemberRepository.save(clubMember2);
    }

    /**
     * 일정 초기 데이터 설정
     */
    @Transactional
    public void scheduleInitData() {
        // 모임 1의 일정 초기 데이터
        Club club1 = clubRepository.findById(1L).get();

        for (int i = 1; i <= 4; i++) {
            Schedule schedule = Schedule.builder()
                    .title("제 %s회 걷기 일정".formatted(i))
                    .content("서울에서 함께 산책합니다")
                    .startDate(LocalDateTime.parse("2025-07-05T10:00:00").plusDays(i * 7))
                    .endDate(LocalDateTime.parse("2025-07-05T15:00:00").plusDays(i * 7))
                    .spot("서울시 서초동")
                    .club(club1)
                    .build();
            scheduleRepository.save(schedule);

            System.out.println("모임 %d 일정 : ".formatted(schedule.getId()) + schedule.getStartDate() + " ~ " + schedule.getEndDate());
        }

        // 모임 2의 일정 초기 데이터
        Club club2 = clubRepository.findById(2L).get();

        Schedule schedule2 = Schedule.builder()
                .title("맛집 탐방")
                .content("시장 맛집 탐방")
                .startDate(LocalDateTime.parse("2025-05-07T18:00:00"))
                .endDate(LocalDateTime.parse("2025-05-07T21:30:00"))
                .spot("단양시장")
                .club(club2)
                .build();
        scheduleRepository.save(schedule2);

        Schedule schedule3 = Schedule.builder()
                .title("강릉 여행")
                .content("1박 2일 강릉 여행")
                .startDate(LocalDateTime.parse("2025-07-23T08:10:00"))
                .endDate(LocalDateTime.parse("2025-07-24T15:00:00"))
                .spot("강릉")
                .club(club2)
                .build();
        scheduleRepository.save(schedule3);
    }
}