package com.back.domain.schedule.schedule.controller;

import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.service.ScheduleService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.ParameterObjectNamingStrategyCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ApiV1ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private ParameterObjectNamingStrategyCustomizer parameterObjectNamingStrategyCustomizer;

    @Test
    @DisplayName("일정 목록 조회 - 날짜 파라미터 없는 경우 현재 달 기준")
    void trl1() throws Exception {
        Long clubId = 1L;

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/schedules/clubs/" + clubId))
                .andDo(print());

        List<Schedule> schedules = scheduleService.getClubSchedules(clubId, null, null);

        resultActions
                .andExpect(handler().handlerType(ApiV1ScheduleController.class))
                .andExpect(handler().methodName("getClubSchedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s번 모임의 일정 목록이 조회되었습니다.".formatted(clubId)))
                .andExpect(jsonPath("$.data.length()").value(schedules.size()));

        for (int i = 0; i < schedules.size(); i++) {
            Schedule schedule = schedules.get(i);

            resultActions
                    .andExpect(jsonPath("$.data[%d].id".formatted(i)).value(schedule.getId()))
                    .andExpect(jsonPath("$.data[%d].title".formatted(i)).value(schedule.getTitle()))
                    .andExpect(jsonPath("$.data[%d].content".formatted(i)).value(schedule.getContent()))
                    .andExpect(jsonPath("$.data[%d].startDate".formatted(i)).value(Matchers.startsWith(schedule.getStartDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$.data[%d].endDate".formatted(i)).value(Matchers.startsWith(schedule.getEndDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$.data[%d].spot".formatted(i)).value(schedule.getSpot()));
        }
    }

    @Test
    @DisplayName("일정 목록 조회 - 날짜 파라미터 있는 경우")
    void trl2() throws Exception {
        Long clubId = 1L;
        String startDateStr = "2025-08-01";
        String endDateStr = "2025-08-31";

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/schedules/clubs/" + clubId)
                        .param("startDate", startDateStr)
                        .param("endDate", endDateStr))
                .andDo(print());

        List<Schedule> schedules = scheduleService.getClubSchedules(clubId, LocalDate.parse(startDateStr), LocalDate.parse(endDateStr));

        resultActions
                .andExpect(handler().handlerType(ApiV1ScheduleController.class))
                .andExpect(handler().methodName("getClubSchedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s번 모임의 일정 목록이 조회되었습니다.".formatted(clubId)))
                .andExpect(jsonPath("$.data.length()").value(schedules.size()));

        for (int i = 0; i < schedules.size(); i++) {
            Schedule schedule = schedules.get(i);

            resultActions
                    .andExpect(jsonPath("$.data[%d].id".formatted(i)).value(schedule.getId()))
                    .andExpect(jsonPath("$.data[%d].title".formatted(i)).value(schedule.getTitle()))
                    .andExpect(jsonPath("$.data[%d].content".formatted(i)).value(schedule.getContent()))
                    .andExpect(jsonPath("$.data[%d].startDate".formatted(i)).value(Matchers.startsWith(schedule.getStartDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$.data[%d].endDate".formatted(i)).value(Matchers.startsWith(schedule.getEndDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$.data[%d].spot".formatted(i)).value(schedule.getSpot()));
        }
    }

    @Test
    @DisplayName("일정 목록 조회 - 시작일 파라미터만 있는 경우")
    void trl3() throws Exception {
        Long clubId = 1L;
        String startDateStr = "2025-07-15";
        String endDateStr = "2025-07-31";

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/schedules/clubs/" + clubId)
                        .param("startDate", startDateStr))
                .andDo(print());

        List<Schedule> schedules = scheduleService.getClubSchedules(clubId, LocalDate.parse(startDateStr), LocalDate.parse(endDateStr));

        resultActions
                .andExpect(handler().handlerType(ApiV1ScheduleController.class))
                .andExpect(handler().methodName("getClubSchedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s번 모임의 일정 목록이 조회되었습니다.".formatted(clubId)))
                .andExpect(jsonPath("$.data.length()").value(schedules.size()));

        for (int i = 0; i < schedules.size(); i++) {
            Schedule schedule = schedules.get(i);

            resultActions
                    .andExpect(jsonPath("$.data[%d].id".formatted(i)).value(schedule.getId()))
                    .andExpect(jsonPath("$.data[%d].title".formatted(i)).value(schedule.getTitle()))
                    .andExpect(jsonPath("$.data[%d].content".formatted(i)).value(schedule.getContent()))
                    .andExpect(jsonPath("$.data[%d].startDate".formatted(i)).value(Matchers.startsWith(schedule.getStartDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$.data[%d].endDate".formatted(i)).value(Matchers.startsWith(schedule.getEndDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$.data[%d].spot".formatted(i)).value(schedule.getSpot()));
        }
    }

    @Test
    @DisplayName("일정 조회")
    void tr1() throws Exception {
        Long scheduleId = 1L;

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/schedules/" + scheduleId))
                .andDo(print());

        Schedule schedule = scheduleService.getScheduleById(scheduleId);

        resultActions
                .andExpect(handler().handlerType(ApiV1ScheduleController.class))
                .andExpect(handler().methodName("getSchedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s번 일정이 조회되었습니다.".formatted(scheduleId)))
                .andExpect(jsonPath("$.data.id").value(schedule.getId()))
                .andExpect(jsonPath("$.data.title").value(schedule.getTitle()))
                .andExpect(jsonPath("$.data.content").value(schedule.getContent()))
                .andExpect(jsonPath("$.data.startDate").value(Matchers.startsWith(schedule.getStartDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.endDate").value(Matchers.startsWith(schedule.getEndDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.spot").value(schedule.getSpot()));
    }

    @Test
    @DisplayName("일정 생성")
    void tc1() throws Exception {
        Long clubId = 1L;

        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "clubId" : 1,
                                    "title" : "제 4회 걷기 일정",
                                    "content" : "공원에서 함께 걷습니다",
                                    "spot" : "서울시 서초동",
                                    "startDate" : "2025-08-02T10:00:00",
                                    "endDate" : "2025-08-02T15:00:00"
                                }
                                """)
                )
                .andDo(print());

        Schedule schedule = scheduleService.getLatestClubSchedule(clubId);

        resultActions
                .andExpect(handler().handlerType(ApiV1ScheduleController.class))
                .andExpect(handler().methodName("createSchedule"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("%d번 일정이 생성되었습니다.".formatted(schedule.getId())))
                .andExpect(jsonPath("$.data.id").value(schedule.getId()))
                .andExpect(jsonPath("$.data.title").value(schedule.getTitle()))
                .andExpect(jsonPath("$.data.content").value(schedule.getContent()))
                .andExpect(jsonPath("$.data.startDate").value(Matchers.startsWith(schedule.getStartDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.endDate").value(Matchers.startsWith(schedule.getEndDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.spot").value(schedule.getSpot()));
    }

    @Test
    @DisplayName("일정 수정")
    void tu1() throws Exception {
        Long scheduleId = 6L;

        ResultActions resultActions = mockMvc
                .perform(put("/api/v1/schedules/" + scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title" : "속초 여행",
                                "content" : "1박 2일 속초 여행",
                                "spot" : "속초",
                                "startDate" : "2025-07-22T10:00:00",
                                "endDate" : "2025-07-24T15:00:00"
                            }
                            """)
                )
                .andDo(print());

        Schedule schedule = scheduleService.getScheduleById(scheduleId);

        resultActions
                .andExpect(handler().handlerType(ApiV1ScheduleController.class))
                .andExpect(handler().methodName("modifySchedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%d번 일정이 수정되었습니다.".formatted(schedule.getId())))
                .andExpect(jsonPath("$.data.id").value(schedule.getId()))
                .andExpect(jsonPath("$.data.title").value(schedule.getTitle()))
                .andExpect(jsonPath("$.data.content").value(schedule.getContent()))
                .andExpect(jsonPath("$.data.startDate").value(Matchers.startsWith(schedule.getStartDate().toString())))
                .andExpect(jsonPath("$.data.endDate").value(Matchers.startsWith(schedule.getEndDate().toString())))
                .andExpect(jsonPath("$.data.spot").value(schedule.getSpot()));
    }

    @Test
    @DisplayName("일정 수정 - 시작일 종료일 보다 늦은 경우 예외 발생")
    void tu2() throws Exception {
        Long scheduleId = 6L;

        ResultActions resultActions = mockMvc
                .perform(put("/api/v1/schedules/" + scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title" : "속초 여행",
                                "content" : "1박 2일 속초 여행",
                                "spot" : "속초",
                                "startDate" : "2025-07-22T15:00:00",
                                "endDate" : "2025-07-22T10:00:00"
                            }
                            """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1ScheduleController.class))
                .andExpect(handler().methodName("modifySchedule"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("시작일은 종료일보다 이전이어야 합니다."));
    }

    @Test
    @DisplayName("일정 삭제 - 체크리스트가 없는 경우")
    void td1() throws Exception {
        Long scheduleId = 4L;
        Schedule schedule = scheduleService.getScheduleById(scheduleId);

        Long clubId = schedule.getClub().getId();
        long preCnt = scheduleService.countClubSchedules(clubId);

        ResultActions resultActions = mockMvc
                .perform(delete("/api/v1/schedules/" + scheduleId))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1ScheduleController.class))
                .andExpect(handler().methodName("deleteSchedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%d번 일정이 삭제되었습니다.".formatted(scheduleId)));

        long afterCnt = scheduleService.countClubSchedules(clubId);
        assertThat(afterCnt).isEqualTo(preCnt - 1);
    }

    @Test
    @DisplayName("일정 삭제(비활성화) - 체크리스트가 있는 경우")
    void td2() throws Exception {
        Long scheduleId = 5L;
        Schedule schedule = scheduleService.getScheduleById(scheduleId);

        Long clubId = schedule.getClub().getId();
        long preCnt = scheduleService.countClubSchedules(clubId);

        ResultActions resultActions = mockMvc
                .perform(delete("/api/v1/schedules/" + scheduleId))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1ScheduleController.class))
                .andExpect(handler().methodName("deleteSchedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%d번 일정이 삭제되었습니다.".formatted(scheduleId)));

        // 일정이 삭제가 아닌 비활성화 되었는지 확인
        long afterCnt = scheduleService.countClubSchedules(clubId);
        assertThat(afterCnt).isEqualTo(preCnt);

        Schedule updatedSchedule = scheduleService.getScheduleById(scheduleId);
        assertThat(updatedSchedule.isActive()).isFalse();

        // 체크리스트가 비활성화 되었는지 확인
        if( updatedSchedule.getCheckList() != null) {
            assertThat(updatedSchedule.getCheckList().isActive()).isFalse();
        }
    }
}