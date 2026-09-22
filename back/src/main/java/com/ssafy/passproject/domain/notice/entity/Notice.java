package com.ssafy.passproject.domain.notice.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notice {
	private Long noticeId;
	private String title;
	private String content;
	private String author;
	private int hit;
	private LocalDateTime writeDate;
	private NoticeCategory category;

	public enum NoticeCategory {
		NOTICE,		// 공지사항
		QNA,		// 질문글
		REQUEST,	// hotplace 추가 요청
	}
}
