package com.back.domain.checkList.checkList.service;

import com.back.domain.checkList.checkList.dto.CheckListDto;
import com.back.domain.checkList.checkList.dto.CheckListWriteReqDto;
import com.back.domain.checkList.checkList.entity.CheckList;
import com.back.domain.checkList.checkList.entity.CheckListItem;
import com.back.domain.checkList.checkList.repository.CheckListRepository;
import com.back.domain.checkList.itemAssign.entity.ItemAssign;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.repository.ScheduleRepository;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckListService {
  private final CheckListRepository checkListRepository;
  private final ScheduleRepository scheduleRepository;
  private final MemberRepository memberRepository;
  private final Rq rq;

  @Value("${custom.jwt.secretKey}")
  private String secretKey;

  @Transactional
  public RsData<CheckListDto> write(CheckListWriteReqDto checkListWriteReqDto) {
    RsData<Map<String, Object>> jwtRsData = getJwtData();

    // JWT 데이터가 유효하지 않은 경우 RsData 반환
    if (jwtRsData.code() != 200) {
      return RsData.of(jwtRsData.code(), jwtRsData.message());
    }
    // JWT에서 멤버 ID 추출
    Map<String, Object> jwtData = jwtRsData.data();
    long memberId = ((Number) jwtData.get("id")).longValue();
    Optional<Member> otnMember = memberRepository.findById(memberId);
    if (otnMember.isEmpty()) return RsData.of(404, "멤버를 찾을 수 없습니다");
    Member member = otnMember.get();

    // 전달 받은 checkListWriteReqDto에서 scheduleId로 Schedule 엔티티 조회
    Optional<Schedule> otnSchedule = scheduleRepository.findById(checkListWriteReqDto.scheduleId());
    if (otnSchedule.isEmpty()) return RsData.of(404, "일정을 찾을 수 없습니다");
    Schedule schedule = otnSchedule.get();

    // Schedule에 CheckList가 이미 존재하는 경우 RsData 반환
    if (schedule.getCheckList() != null) return RsData.of(403, "이미 체크리스트가 존재합니다");

    // Schedule 엔티티에서 클럽 조회 멤버 조회
    Optional<ClubMember> otnClubMember = schedule.getClub().getClubMembers().stream()
        .filter(clubMember ->
            clubMember.getMember().getId().equals(member.getId())).findFirst();

    // 클럽 멤버가 아닌 경우 RsData 반환
    if (otnClubMember.isEmpty() || !otnClubMember.get().getState().equals(ClubMemberState.JOINING)) return RsData.of(403, "클럽 멤버가 아닙니다");

    if (otnClubMember.get().getRole().equals(ClubMemberRole.PARTICIPANT)) return RsData.of(403, "호스트 또는 관리자만 체크리스트를 생성할 수 있습니다");
    List<CheckListItem> checkListItems;
    // 전달 받은 checkListWriteReqDto에서 Request받은 CheckListItem를 CheckListItem 엔티티로 변환 해서 리스트로 변환
    try {
      checkListItems = checkListWriteReqDto.checkListItems().stream()
          .map(req -> CheckListItem.builder()
              .content(req.content())
              .category(req.category())
              .sequence(req.sequence())
              .itemAssigns(Optional.ofNullable(req.itemAssigns())
                  .orElse(Collections.emptyList())
                  .stream()
                  .map(itemAssignReq -> {
                    // 클럽 멤버 조회 (Optional 안전 처리)
                    ClubMember clubMember = schedule.getClub().getClubMembers().stream().filter(
                            cm -> cm.getId().equals(itemAssignReq.clubMemberId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("클럽 멤버를 찾을 수 없습니다"));

                    return ItemAssign.builder()
                        .clubMember(clubMember)
                        .build();
                  })
                  .collect(Collectors.toList()))
              .build())
          .collect(Collectors.toList());
    }catch (IllegalArgumentException e) {
      return RsData.of(403, e.getMessage());
    }
    // CheckList 엔티티 생성
    CheckList checkListBuilder = CheckList.builder()
        .schedule(schedule)
        .isActive(checkListWriteReqDto.isActive())
        .checkListItems(checkListItems)
        .build();

    // CheckList 엔티티 저장
    CheckList checkList = checkListRepository.save(checkListBuilder);
    schedule.updateCheckList(checkList); // Schedule과 CheckList 연관 설정
    scheduleRepository.save(schedule); // Schedule 업데이트
    // CheckListDto로 변환
    CheckListDto checkListDto = new CheckListDto(checkList);

    return RsData.of(201, "체크리스트 생성 성공", checkListDto);
  }





























  RsData<Map<String, Object>> getJwtData() {
    // JWT 토큰을 헤더에서 가져오기
    String jwtToken = rq.getHeader("Authorization", null);

    // JWT 토큰이 null인 경우 RsData 반환
    if (jwtToken == null) return RsData.of(404, "AccessToken을 찾을 수 없습니다");
    // JWT 토큰이 "Bearer "로 시작하지 않는 경우 RsData 반환
    if (!jwtToken.startsWith("Bearer ")) return RsData.of(404, "AccessToken이 잘못되었습니다");

    // JWT 토큰에서 "Bearer " 접두사를 제거
    String cleanToken = jwtToken.substring(7);

    // JWT 토큰이 유효하지 않은 경우 처리
    boolean jwtIsValid = Ut.jwt.isValid(secretKey, cleanToken);

    // JWT가 유효하지 않은 경우 RsData 반환
    if (!jwtIsValid) return RsData.of(499, "AccessToken 만료");

    // JWT 토큰에서 페이로드 추출
    Map<String, Object> jwtData = Ut.jwt.payload(secretKey, cleanToken);
    return RsData.of(200, "토큰 검증 성공", jwtData);

  }
}
