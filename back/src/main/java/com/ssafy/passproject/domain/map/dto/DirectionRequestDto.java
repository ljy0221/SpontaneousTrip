package com.ssafy.passproject.domain.map.dto;

import lombok.Data;
import java.util.List;

@Data
public class DirectionRequestDto {
    private Location origin;
    private Location destination;
    private List<Location> waypoints;
    private String priority; // "RECOMMEND", "TIME", "DISTANCE"

    @Data
    public static class Location {
        private double x; // 경도 (longitude)
        private double y; // 위도 (latitude)
        private String name;
    }
}
