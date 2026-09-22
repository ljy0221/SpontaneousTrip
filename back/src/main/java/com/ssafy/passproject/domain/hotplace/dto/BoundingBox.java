package com.ssafy.passproject.domain.hotplace.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 바운딩 박스 좌표를 담는 DTO
 * MBR(Minimum Bounding Rectangle) 기반 공간 검색 최적화에 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoundingBox {
  private double minLat;
  private double maxLat;
  private double minLon;
  private double maxLon;
}
