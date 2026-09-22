package com.ssafy.passproject.domain.notice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.passproject.domain.notice.dto.request.NoticeRequest;
import com.ssafy.passproject.domain.notice.dto.request.SearchCondition;
import com.ssafy.passproject.domain.notice.dto.response.NoticeResponse;
import com.ssafy.passproject.domain.notice.dto.response.PagedNoticeResponse;
import com.ssafy.passproject.domain.notice.entity.Notice;
import com.ssafy.passproject.domain.notice.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository repo;

    @Transactional
    public void write(NoticeRequest request) {
        repo.insert(request.toEntity());
    }

    public PagedNoticeResponse getList(SearchCondition condition) {
        List<Notice> notices = repo.selectAll(condition);
        List<NoticeResponse> responses = notices.stream()
            .map(NoticeResponse::new)
            .collect(Collectors.toList());
        int totalCount = repo.getTotalCount(condition);

        return PagedNoticeResponse.of(responses, condition.getPage(), condition.getSize(), totalCount);
    }

    @Transactional
    public NoticeResponse getDetail(Long id) {
        repo.updateHit(id);
        Notice notice = repo.selectOne(id);
        return notice != null ? new NoticeResponse(notice) : null;
    }

    @Transactional
    public void modify(Long id, NoticeRequest request) {
        Notice notice = request.toEntity();
        notice.setNoticeId(id);
        repo.update(notice);
    }

    @Transactional
    public void remove(Long id) {
        repo.delete(id);
    }

    
}
