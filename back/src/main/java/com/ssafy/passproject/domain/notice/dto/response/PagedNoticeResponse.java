package com.ssafy.passproject.domain.notice.dto.response;

import java.util.List;

import com.ssafy.passproject.domain.hotplace.dto.response.PageInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedNoticeResponse {
    private List<NoticeResponse> data;
    private PageInfo pageInfo;

    public static PagedNoticeResponse of(List<NoticeResponse> data, int page, int size, long totalElements) {
        // Note: page is 1-based, but PageInfo.of expects 0-based
        PageInfo pageInfo = PageInfo.of(page - 1, size, totalElements);
        return new PagedNoticeResponse(data, pageInfo);
    }
}
