package com.back.domain.schedule.schedule.service;

import com.back.domain.checkList.checkList.repository.CheckListRepository;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.schedule.schedule.dto.ScheduleCreateReqBody;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ClubRepository clubRepository;
    private final CheckListRepository checkListRepository;

    /**
     * 특정 모임의 최신 일정 조회
     * @param clubId
     * @return schedule
     */
    @Transactional(readOnly = true)
    public Schedule getLatestClubSchedule(Long clubId) {
        return scheduleRepository
                .findFirstByClubIdOrderByIdDesc(clubId)
                .orElseThrow(() -> new NoSuchElementException("%d번 모임의 일정은 존재하지 않습니다.".formatted(clubId)));
    }

    /**
     * 일정 생성
     * @param reqBody (ScheduleCreateReqBody)
     * @return schedule
     */
    @Transactional
    public Schedule createSchedule(ScheduleCreateReqBody reqBody) {
        // 변경 필요 (임시 조회용)
        // 조회 + 모임 (종료일 지난 모임, 삭제된 모임 등) 검증 로직 호출
        // 또는 clubRepository.findByIdAndStatsTrueAndEndDateAfter
        Club club = clubRepository
                .findById(reqBody.clubId())
                .orElseThrow(() -> new NoSuchElementException("%d번 모임은 존재하지 않습니다.".formatted(reqBody.clubId())));

        // 일정 생성
        Schedule schedule = Schedule.builder()
                .title(reqBody.title())
                .content(reqBody.content())
                .startDate(reqBody.startDate())
                .endDate(reqBody.endDate())
                .spot(reqBody.spot())
                .club(club)
                .build();

        return scheduleRepository.save(schedule);
    }
}
