package com.back.domain.preset.preset.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.preset.preset.dto.PresetDto;
import com.back.domain.preset.preset.dto.PresetItemDto;
import com.back.domain.preset.preset.dto.PresetWriteReqDto;
import com.back.domain.preset.preset.entity.Preset;
import com.back.domain.preset.preset.entity.PresetItem;
import com.back.domain.preset.preset.repository.PresetRepository;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PresetService {
  private final PresetRepository presetRepository;
  private final MemberRepository memberRepository;
  private final Rq rq;

  @Value("${custom.jwt.secretKey}")
  private String secretKey;

  public RsData<PresetDto> write(PresetWriteReqDto presetWriteReqDto) {

    // JWT 토큰을 헤더에서 가져오기
    String jwtToken = rq.getHeader("Authorization", null);

    // JWT 토큰이 null인 경우 RsData 반환
    if (jwtToken == null) return RsData.of(404, "AccessToken을 찾을 수 없습니다");

    // JWT 토큰에서 "Bearer " 접두사를 제거
    String cleanToken = jwtToken.substring(7);

    // JWT 토큰이 유효하지 않은 경우 처리
    boolean jwtIsValid = Ut.jwt.isValid(secretKey, cleanToken);

    // JWT가 유효하지 않은 경우 RsData 반환
    if (!jwtIsValid) return RsData.of(499, "AccessToken 만료");

    // JWT 토큰에서 페이로드 추출
    Map<String, Object> jwtData = Ut.jwt.payload(secretKey, cleanToken);
    System.out.println("JWT Data: " + jwtData.get("id"));
    // JWT에서 멤버 ID 추출
    long memberId = ((Number) jwtData.get("id")).longValue();

    // 멤버 ID로 Member 엔티티 조회
    Optional<Member> OtnMember = memberRepository.findById(memberId);
    if (OtnMember.isEmpty()) return RsData.of(404, "멤버를 찾을 수 없습니다");
    Member member = OtnMember.get();

    // 전달 받은 presetWriteReqDto에서 Request받은 PresetItem를 PresetItem 엔티티로 변환 해서 리스트로 변환
    List<PresetItem> presetItems = presetWriteReqDto.presetItems().stream()
        .map(req -> PresetItem.builder()
            .content(req.content())
            .category(req.category())
            .sequence(req.sequence())
            .build())
        .toList();

    // 프리셋 빌더 생성
    Preset presetBuilder = Preset.builder()
        .owner(member)
        .name(presetWriteReqDto.name())
        .presetItems(presetItems)
        .build();

    // 프리셋 생성
    Preset preset = presetRepository.save(presetBuilder);

    // 프리셋 DTO로 변환
    PresetDto presetDto = new PresetDto(preset);

    return RsData.of(201, "프리셋 생성 성공", presetDto);
  }
}
