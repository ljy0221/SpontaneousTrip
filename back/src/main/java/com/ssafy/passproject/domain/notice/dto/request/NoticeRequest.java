package com.ssafy.passproject.domain.notice.dto.request;

import com.ssafy.passproject.domain.notice.entity.Notice;
import com.ssafy.passproject.domain.notice.entity.Notice.NoticeCategory;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class NoticeRequest {
    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    private String content;

    @NotBlank(message = "작성자는 필수입니다")
    private String author;

    @NotNull(message = "카테고리는 필수입니다")
    private NoticeCategory category;

    public Notice toEntity() {
        Notice notice = new Notice();
        notice.setTitle(this.title);
        notice.setContent(this.content);
        notice.setAuthor(this.author);
        notice.setCategory(this.category);
        return notice;
    }
}
