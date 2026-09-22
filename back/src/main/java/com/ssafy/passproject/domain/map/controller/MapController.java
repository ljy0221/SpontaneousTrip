package com.ssafy.passproject.domain.map.controller;

import com.ssafy.passproject.domain.map.dto.DirectionRequestDto;
import com.ssafy.passproject.domain.map.service.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
@Slf4j
public class MapController {

    private final MapService mapService;

    @PostMapping("/direction")
    public ResponseEntity<String> getDirection(@RequestBody DirectionRequestDto requestDto) {
        log.info("Received Direction Request - Origin: {}, Destination: {}", requestDto.getOrigin(),
                requestDto.getDestination());
        log.info("Full DTO: {}", requestDto);
        String result = mapService.getDirection(requestDto);
        return ResponseEntity.ok(result);
    }
}
