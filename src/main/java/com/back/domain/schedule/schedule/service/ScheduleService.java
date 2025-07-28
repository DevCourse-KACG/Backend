package com.back.domain.schedule.schedule.service;

import com.back.domain.checkList.checkList.repository.CheckListRepository;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.schedule.schedule.dto.ScheduleCreateReqBody;
import com.back.domain.schedule.schedule.dto.ScheduleUpdateReqBody;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.repository.ScheduleRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ClubRepository clubRepository;
    private final CheckListRepository checkListRepository;

    /**
     * 특정 모임의 일정 목록 조회
     * @param clubId
     * @return
     */
    @Transactional
    public List<Schedule> getGroupSchedules(Long clubId) {
        clubRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("%d번 모임은 존재하지 않습니다.".formatted(clubId)));

        // 모임의 일정 목록 조회
        return scheduleRepository.findByClubIdOrderByStartDate(clubId);
    }

    /**
     * 일정 조회
     * @param scheduleId
     * @return schedule
     */
    @Transactional(readOnly = true)
    public Schedule getScheduleById(Long scheduleId) {
        return scheduleRepository
                .findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("%d번 일정은 존재하지 않습니다.".formatted(scheduleId)));
    }
    
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

    @Transactional(readOnly = true)
    public int countClubSchedules(Long clubId) {
        return scheduleRepository.countByClubId(clubId);
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

        // 날짜 유효성 검증
        validateDate(reqBody.startDate(), reqBody.endDate());

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

    /**
     * 일정 수정
     * @param schedule
     * @param reqBody
     */
    @Transactional
    public void modifySchedule(Schedule schedule, ScheduleUpdateReqBody reqBody) {
        // 날짜 유효성 검증
        validateDate(reqBody.startDate(), reqBody.endDate());

        // 일정 수정
        schedule.modify(reqBody.title(), reqBody.content(), reqBody.startDate(), reqBody.endDate(), reqBody.spot());
    }

    private static void validateDate(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ServiceException(400, "시작일은 종료일보다 이전이어야 합니다.");
        }
    }

    /**
     * 일정 삭제
     * @param schedule
     */
    @Transactional
    public void deleteSchedule(Schedule schedule) {
        if (schedule.canDelete()) {
            // 일정 삭제 - 체크리스트 없을 시
            scheduleRepository.delete(schedule);
        } else {
            // 일정 비활성화 - 체크리스트 있을 시
            schedule.deactivate();

            // 체크리스트 비활성화
            schedule.getCheckList().deactivate();
        }
    }
}
