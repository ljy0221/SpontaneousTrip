package com.ssafy.passproject.domain.hotplace.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedHotplaceResponse {
    private List<HotplaceResponseDto> data;
    private PageInfo pageInfo;

    public static PagedHotplaceResponse of(List<HotplaceResponseDto> data, int page, int size, long totalElements) {
        PageInfo pageInfo = PageInfo.of(page, size, totalElements);
        return new PagedHotplaceResponse(data, pageInfo);
    }
}
