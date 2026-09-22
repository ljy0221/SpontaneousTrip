package com.ssafy.passproject.domain.hotplace.dto.request;

import org.locationtech.jts.geom.Point;

import com.ssafy.passproject.domain.hotplace.entity.Hotplace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HotplaceRequestDto {

    @NotBlank(message = "장소 이름은 필수 입력 항목입니다.")
    private String placeName;

    @NotBlank(message = "카테고리는 필수 입력 항목입니다.")
    private String category;

    @NotBlank(message = "주소는 필수 입력 항목입니다.")
    private String address;

    private String overview;
    private String description;

    // 클라이언트로부터는 분리된 double 값을 받습니다.
    @NotNull(message = "위도(latitude)는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도(longitude)는 필수입니다.")
    private Double longitude;

    private boolean isPetFriendly;

    @DecimalMin(value = "0.0", message = "평점은 0.0 이상이어야 합니다.")
    private double avgRating;

    // 💡 DTO -> Entity 변환 메서드 예시 (Service Layer에서 사용)
    public Hotplace toEntity(Point location, String source) {
        return new Hotplace(
                null, this.placeName, this.category, this.address, this.overview,
                null, location, source, this.isPetFriendly, this.avgRating, 0);
    }
}