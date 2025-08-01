package com.back.domain.preset.preset.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.preset.preset.dto.PresetDto;
import com.back.domain.preset.preset.dto.PresetWriteReqDto;
import com.back.domain.preset.preset.entity.Preset;
import com.back.domain.preset.preset.entity.PresetItem;
import com.back.domain.preset.preset.repository.PresetRepository;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresetService {
  private final PresetRepository presetRepository;
  private final Rq rq;

  @Transactional
  public RsData<PresetDto> write(PresetWriteReqDto presetWriteReqDto) {
    Member member = Optional.ofNullable(rq.getActor()).orElseThrow(() -> new ServiceException(404, "멤버를 찾을 수 없습니다"));

    // 전달 받은 presetWriteReqDto에서 Request받은 PresetItem를 PresetItem 엔티티로 변환 해서 리스트로 변환
    List<PresetItem> presetItems = presetWriteReqDto.presetItems().stream()
        .map(req -> PresetItem.builder()
            .content(req.content())
            .category(req.category())
            .sequence(req.sequence())
            .build())
        .collect(Collectors.toList());

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
    Member member = Optional.ofNullable(rq.getActor()).orElseThrow(() -> new ServiceException(404, "멤버를 찾을 수 없습니다"));

    // 프리셋 ID로 프리셋 조회
    Optional<Preset> otnPreset = presetRepository.findById(presetId);

    // 프리셋이 존재하지 않는 경우 RsData 반환
    if (otnPreset.isEmpty()) return RsData.of(404, "프리셋을 찾을 수 없습니다");
    Preset preset = otnPreset.get();

    // 프리셋의 소유자와 JWT에서 추출한 멤버 ID가 일치하지 않는 경우 RsData 반환
    if (!preset.getOwner().getId().equals(member.getId())) return RsData.of(403, "권한 없는 프리셋");

    // 프리셋이 존재하는 경우 프리셋 DTO로 변환
    PresetDto presetDto = new PresetDto(preset);

    return RsData.of(200, "프리셋 조회 성공", presetDto);
  }

  public RsData<List<PresetDto>> getPresetList() {
    Member member = Optional.ofNullable(rq.getActor()).orElseThrow(() -> new ServiceException(404, "멤버를 찾을 수 없습니다"));
    List<Preset> presets = presetRepository.findByOwner(member);

    // 프리셋 목록을 PresetDTO로 변환
    List<PresetDto> presetDtos = presets.stream()
        .map(PresetDto::new)
        .toList();

    return RsData.of(200, "프리셋 목록 조회 성공", presetDtos);
  }

  @Transactional
  public RsData<Void> deletePreset(Long presetId) {
    Member member = Optional.ofNullable(rq.getActor()).orElseThrow(() -> new ServiceException(404, "멤버를 찾을 수 없습니다"));

    // 프리셋 ID로 프리셋 조회
    Optional<Preset> otnPreset = presetRepository.findById(presetId);

    // 프리셋이 존재하지 않는 경우 RsData 반환
    if (otnPreset.isEmpty()) return RsData.of(404, "프리셋을 찾을 수 없습니다");
    Preset preset = otnPreset.get();

    // 프리셋의 소유자와 JWT에서 추출한 멤버 ID가 일치하지 않는 경우 RsData 반환
    if (!preset.getOwner().getId().equals(member.getId())) return RsData.of(403, "권한 없는 프리셋");

    // 프리셋 삭제
    presetRepository.delete(preset);

    return RsData.of(200, "프리셋 삭제 성공");
  }

  @Transactional
  public RsData<PresetDto> updatePreset(Long presetId, PresetWriteReqDto presetWriteReqDto) {
    Member member = Optional.ofNullable(rq.getActor()).orElseThrow(() -> new ServiceException(404, "멤버를 찾을 수 없습니다"));

    // 프리셋 ID로 프리셋 조회
    Optional<Preset> otnPreset = presetRepository.findById(presetId);

    // 프리셋이 존재하지 않는 경우 RsData 반환
    if (otnPreset.isEmpty()) return RsData.of(404, "프리셋을 찾을 수 없습니다");
    Preset preset = otnPreset.get();
    // 프리셋의 소유자와 JWT에서 추출한 멤버 ID가 일치하지 않는 경우 RsData 반환
    if (!preset.getOwner().getId().equals(member.getId())) return RsData.of(403, "권한 없는 프리셋");

    // 전달 받은 presetWriteReqDto에서 Request받은 PresetItem를 PresetItem 엔티티로 변환 해서 리스트로 변환
    List<PresetItem> presetItems = presetWriteReqDto.presetItems().stream().map(
        req -> PresetItem.builder()
            .content(req.content())
            .category(req.category())
            .sequence(req.sequence())
            .preset(preset)
            .build()
    ).collect(Collectors.toList());

    // 프리셋 수정
    preset.updateName(presetWriteReqDto.name());
    preset.updatePresetItems(presetItems);

    // 프리셋 저장
    Preset modifyPreset = presetRepository.save(preset);

    // 수정된 프리셋을 DTO로 변환
    PresetDto updatedPresetDto = new PresetDto(modifyPreset);

    return RsData.of(200, "프리셋 수정 성공", updatedPresetDto);
  }

}
