package com.ssafy.passproject.domain.notice.dto.request;

import com.ssafy.passproject.domain.notice.entity.Notice.NoticeCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchCondition {
    private String key = "none";
    private String word;
    private String orderBy = "none";
    private String orderByDir = "asc";
    private int page = 1;
    private int size = 10;
    private NoticeCategory category;  // 카테고리 필터

    public int getOffset() {
        return (page - 1) * size;
    }
}