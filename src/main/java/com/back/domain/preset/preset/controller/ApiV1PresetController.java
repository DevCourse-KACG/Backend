package com.back.domain.preset.preset.controller;

import com.back.domain.preset.preset.dto.PresetDto;
import com.back.domain.preset.preset.dto.PresetWriteReqDto;
import com.back.domain.preset.preset.service.PresetService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/presets")
@RequiredArgsConstructor
@Tag(name="PresetController", description="프리셋 API")
public class PresetController {

  private final PresetService presetService;

  public ResponseEntity<RsData<PresetDto>> write(@Valid @RequestBody PresetWriteReqDto presetWriteReqDto) {

    RsData<PresetDto> presetDto = presetService.write(presetReqDto);

    return ResponseEntity.status(201).body(presetDto);
  }
}
