package com.back.domain.schedule.schedule.checker;

import com.back.domain.club.club.checker.ClubAuthorizationChecker;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("scheduleAuthorizationChecker")
@RequiredArgsConstructor
public class ScheduleAuthorizationChecker {
    private final ScheduleService scheduleService;
    private final ClubAuthorizationChecker clubChecker;

    @Transactional(readOnly = true)
    public boolean isActiveClubHost(Long scheduleId, Long memberId) {
        Schedule schedule = scheduleService.getActiveScheduleById(scheduleId);
        Long clubId = schedule.getClub().getId();

        return clubChecker.isActiveClubHost(clubId, memberId);
    }

    @Transactional(readOnly = true)
    public boolean isActiveClubManagerOrHost(Long scheduleId, Long memberId) {
        Schedule schedule = scheduleService.getActiveScheduleById(scheduleId);
        Long clubId = schedule.getClub().getId();

        return clubChecker.isActiveClubManagerOrHost(clubId, memberId);
    }

    @Transactional(readOnly = true)
    public boolean isClubMember(Long scheduleId, Long memberId) {
        Schedule schedule = scheduleService.getActiveScheduleById(scheduleId);
        Long clubId = schedule.getClub().getId();

        return clubChecker.isClubMember(clubId, memberId);
    }
}
