package com.ssafy.passproject.domain.notice.repository;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

import com.ssafy.passproject.domain.notice.dto.request.SearchCondition;
import com.ssafy.passproject.domain.notice.entity.Notice;

@Mapper
public interface NoticeRepository {
    void insert(Notice notice);

    List<Notice> selectAll(SearchCondition condition);

    int getTotalCount(SearchCondition condition);

    Notice selectOne(Long id);

    void update(Notice notice);

    void delete(Long id);

    void updateHit(Long id);
}