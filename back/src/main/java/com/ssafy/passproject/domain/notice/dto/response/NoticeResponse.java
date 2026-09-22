package com.ssafy.passproject.domain.notice.dto.response;

import com.ssafy.passproject.domain.notice.entity.Notice;
import com.ssafy.passproject.domain.notice.entity.Notice.NoticeCategory;
import lombok.Data;
import java.time.format.DateTimeFormatter;

@Data
public class NoticeResponse {
    private Long id;
    private String title;
    private String content;
    private String author;
    private int views;
    private String date;
    private NoticeCategory category;

    public NoticeResponse(Notice notice) {
        this.id = notice.getNoticeId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.author = notice.getAuthor();
        this.views = notice.getHit();
        this.category = notice.getCategory();
        if (notice.getWriteDate() != null) {
            this.date = notice.getWriteDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}
