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
    RsData<Map<String, Object>> jwtRsData = getJwtData();

    // JWT 데이터가 유효하지 않은 경우 RsData 반환
    if (jwtRsData.code() != 200) {
      return RsData.of(jwtRsData.code(), jwtRsData.message());
    }
    // JWT에서 멤버 ID 추출
    Map<String, Object> jwtData = jwtRsData.data();
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

  public RsData<PresetDto> getPreset(Long presetId) {
    RsData<Map<String, Object>> jwtRsData = getJwtData();

    // JWT 데이터가 유효하지 않은 경우 RsData 반환
    if (jwtRsData.code() != 200) {
      return RsData.of(jwtRsData.code(), jwtRsData.message());
    }
    // JWT에서 멤버 ID 추출
    Map<String, Object> jwtData = jwtRsData.data();
    long memberId = ((Number) jwtData.get("id")).longValue();

    // 프리셋 ID로 프리셋 조회
    Optional<Preset> otnPreset = presetRepository.findById(presetId);

    // 프리셋이 존재하지 않는 경우 RsData 반환
    if (otnPreset.isEmpty()) return RsData.of(404, "프리셋을 찾을 수 없습니다");
    Preset preset = otnPreset.get();

    // 프리셋의 소유자와 JWT에서 추출한 멤버 ID가 일치하지 않는 경우 RsData 반환
    if (!preset.getOwner().getId().equals(memberId)) return RsData.of(403, "권한 없는 프리셋");

    // 프리셋이 존재하는 경우 프리셋 DTO로 변환
    PresetDto presetDto = new PresetDto(preset);

    return RsData.of(200, "프리셋 조회 성공", presetDto);
  }

  public RsData<List<PresetDto>> getPresetList() {
    RsData<Map<String, Object>> jwtRsData = getJwtData();

    // JWT 데이터가 유효하지 않은 경우 RsData 반환
    if (jwtRsData.code() != 200) {
      return RsData.of(jwtRsData.code(), jwtRsData.message());
    }
    // JWT에서 멤버 ID 추출
    Map<String, Object> jwtData = jwtRsData.data();
    long memberId = ((Number) jwtData.get("id")).longValue();
    // 멤버 ID로 Member 엔티티 조회
    Optional<Member> otnMember = memberRepository.findById(memberId);
    // 멤버가 존재하지 않는 경우 RsData 반환
    if (otnMember.isEmpty()) return RsData.of(404, "멤버를 찾을 수 없습니다");
    Member member = otnMember.get();
    // 멤버의 프리셋 목록 조회
    List<Preset> presets = presetRepository.findByOwner(member);

    // 프리셋 목록을 PresetDTO로 변환
    List<PresetDto> presetDtos = presets.stream()
        .map(PresetDto::new)
        .toList();

    return RsData.of(200, "프리셋 목록 조회 성공", presetDtos);
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

  public RsData<Void> deletePreset(Long presetId) {
    RsData<Map<String, Object>> jwtRsData = getJwtData();

    // JWT 데이터가 유효하지 않은 경우 RsData 반환
    if (jwtRsData.code() != 200) {
      return RsData.of(jwtRsData.code(), jwtRsData.message());
    }
    // JWT에서 멤버 ID 추출
    Map<String, Object> jwtData = jwtRsData.data();
    long memberId = ((Number) jwtData.get("id")).longValue();

    // 프리셋 ID로 프리셋 조회
    Optional<Preset> otnPreset = presetRepository.findById(presetId);

    // 프리셋이 존재하지 않는 경우 RsData 반환
    if (otnPreset.isEmpty()) return RsData.of(404, "프리셋을 찾을 수 없습니다");
    Preset preset = otnPreset.get();

    // 프리셋의 소유자와 JWT에서 추출한 멤버 ID가 일치하지 않는 경우 RsData 반환
    if (!preset.getOwner().getId().equals(memberId)) return RsData.of(403, "권한 없는 프리셋");

    // 프리셋 삭제
    presetRepository.delete(preset);

    return RsData.of(200, "프리셋 삭제 성공");
  }
}
