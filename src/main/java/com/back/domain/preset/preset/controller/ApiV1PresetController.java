package com.back.domain.preset.preset.controller;

import com.back.domain.preset.preset.dto.PresetDto;
import com.back.domain.preset.preset.dto.PresetWriteReqDto;
import com.back.domain.preset.preset.entity.Preset;
import com.back.domain.preset.preset.entity.PresetItem;
import com.back.domain.preset.preset.service.PresetService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/presets")
@RequiredArgsConstructor
@Tag(name="ApiV1PresetController", description="프리셋 API V1 컨트롤러")
public class ApiV1PresetController {
  private final PresetService presetService;

  @PostMapping
  @Transactional
  @Operation(summary = "프리셋 생성")
  public ResponseEntity<RsData<PresetDto>> write(@Valid @RequestBody PresetWriteReqDto presetWriteReqDto) {
    RsData<PresetDto> presetDto = presetService.write(presetWriteReqDto);

    return ResponseEntity.status(presetDto.code()).body(presetDto);
  }

  @GetMapping("/{presetId}")
  @Operation(summary = "프리셋 조회")
  public ResponseEntity<RsData<PresetDto>> getPreset(@PathVariable Long presetId) {
    RsData<PresetDto> presetDto = presetService.getPreset(presetId);

    return ResponseEntity.status(presetDto.code()).body(presetDto);
  }

  @GetMapping
  @Operation(summary = "프리셋 목록 조회")
  public ResponseEntity<RsData<List<PresetDto>>> getPresetList() {
    RsData<List<PresetDto>> presetList = presetService.getPresetList();

    return ResponseEntity.status(presetList.code()).body(presetList);
  }

  @DeleteMapping("/{presetId}")
  @Transactional
  @Operation(summary = "프리셋 삭제")
  public ResponseEntity<RsData<Void>> deletePreset(@PathVariable Long presetId) {
    RsData<Void> deleteResult = presetService.deletePreset(presetId);

    return ResponseEntity.status(deleteResult.code()).body(deleteResult);
  }

  @PutMapping("/{presetId}")
  @Transactional
  @Operation(summary = "프리셋 수정")
  public ResponseEntity<RsData<PresetDto>> updatePreset(@PathVariable Long presetId, @Valid @RequestBody PresetWriteReqDto presetWriteReqDto) {
    RsData<PresetDto> updatedPreset = presetService.updatePreset(presetId, presetWriteReqDto);

    return ResponseEntity.status(updatedPreset.code()).body(updatedPreset);
  }
}
