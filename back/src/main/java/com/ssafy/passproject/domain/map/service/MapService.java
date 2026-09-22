package com.ssafy.passproject.domain.map.service;

import com.ssafy.passproject.domain.map.dto.DirectionRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MapService {

    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;

    private static final String KAKAO_DIRECTION_URL = "https://apis-navi.kakaomobility.com/v1/waypoints/directions";

    public String getDirection(DirectionRequestDto requestDto) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();

        // Origin
        Map<String, Object> originMap = new HashMap<>();
        originMap.put("x", requestDto.getOrigin().getX());
        originMap.put("y", requestDto.getOrigin().getY());
        if (requestDto.getOrigin().getName() != null) {
            originMap.put("name", requestDto.getOrigin().getName());
        }
        body.put("origin", originMap);

        // Destination
        Map<String, Object> destMap = new HashMap<>();
        destMap.put("x", requestDto.getDestination().getX());
        destMap.put("y", requestDto.getDestination().getY());
        if (requestDto.getDestination().getName() != null) {
            destMap.put("name", requestDto.getDestination().getName());
        }
        body.put("destination", destMap);

        // Waypoints
        if (requestDto.getWaypoints() != null && !requestDto.getWaypoints().isEmpty()) {
            body.put("waypoints", requestDto.getWaypoints().stream().map(wp -> {
                Map<String, Object> wpMap = new HashMap<>();
                wpMap.put("x", wp.getX());
                wpMap.put("y", wp.getY());
                if (wp.getName() != null) {
                    wpMap.put("name", wp.getName());
                }
                return wpMap;
            }).toList());
        }

        // Priority
        body.put("priority", requestDto.getPriority() != null ? requestDto.getPriority() : "RECOMMEND");

        // Other options (optional)
        body.put("car_fuel", "GASOLINE");
        body.put("car_hipass", false);
        body.put("alternatives", false);
        body.put("road_details", false);

        log.info("Calling Kakao Direction API with body: {}", body);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_DIRECTION_URL,
                    HttpMethod.POST,
                    entity,
                    String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call Kakao Mobility API", e);
            throw new RuntimeException("Failed to get direction from Kakao API");
        }
    }
}
