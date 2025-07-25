package com.back.domain.preset.preset.service;

import com.back.domain.preset.preset.dto.PresetDto;
import com.back.domain.preset.preset.dto.PresetItemDto;
import com.back.domain.preset.preset.dto.PresetWriteReqDto;
import com.back.domain.preset.preset.entity.Preset;
import com.back.domain.preset.preset.entity.PresetItem;
import com.back.domain.preset.preset.repository.PresetRepository;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PresetService {
  private final PresetRepository presetRepository;

  public RsData<PresetDto> write(PresetWriteReqDto presetWriteReqDto) {

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
