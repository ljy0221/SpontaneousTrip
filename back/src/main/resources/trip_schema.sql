DROP DATABASE IF EXISTS finalproject;
CREATE DATABASE finalproject;
USE finalproject;

-- ************************************************************
-- 1. 지역 코드 테이블 (sidos, guguns)
-- ************************************************************

-- SIDO 테이블: 시/도 코드 및 이름
CREATE TABLE `sidos` (
	`sido_code`	INT	NOT NULL	COMMENT '시도 코드 (PK)',
	`sido_name`	VARCHAR(20)	NULL	DEFAULT NULL	COMMENT '시도 이름',
    PRIMARY KEY (`sido_code`)
);

-- GUGUNS 테이블: 시/군/구 코드 및 이름
CREATE TABLE `guguns` (
	`sido_code`	INT	NOT NULL	COMMENT '시도 코드 (FK)',
	`gugun_code`	INT	NOT NULL	COMMENT '구군 코드',
	`gugun_name`	VARCHAR(20)	NULL	DEFAULT NULL	COMMENT '구군 이름',
    PRIMARY KEY (`sido_code`, `gugun_code`)
);

-- ************************************************************
-- 2. 콘텐츠 타입 테이블
-- ************************************************************

CREATE TABLE `contenttypes` (
	`content_type_id`	INT	NOT NULL	COMMENT '콘텐츠 타입 ID (PK)',
	`content_type_name`	VARCHAR(45)	NULL	DEFAULT NULL	COMMENT '콘텐츠 타입 이름',
    PRIMARY KEY (`content_type_id`)
);

-- ************************************************************
-- 3. 사용자 테이블 (user_id는 VARCHAR(50)로 통일)
-- ************************************************************

CREATE TABLE `users` (
     `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 고유 ID (PK)',
     `email` VARCHAR(100) NOT NULL UNIQUE,
     `password` VARCHAR(255) NOT NULL,
     `nickname` VARCHAR(50) NULL DEFAULT '닉네임',
     `join_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
     `last_login` DATETIME NULL,
     PRIMARY KEY (`user_id`)
);

-- ************************************************************
-- 4. 명소 테이블 (attractions) - 기본 정보
-- ************************************************************

CREATE TABLE `attractions` (
	`attraction_id`	BIGINT	NOT NULL	COMMENT '명소 고유 ID (PK)',
	`content_id`	BIGINT	NULL	DEFAULT NULL	COMMENT 'API 콘텐츠 ID',
	`title`	VARCHAR(500)	NULL	DEFAULT NULL,
	`content_type_id`	INT	NULL	DEFAULT NULL,
	`area_code`	INT	NULL	DEFAULT NULL	COMMENT '시도 코드 (FK)',
	`si_gun_gu_code`	INT	NULL	DEFAULT NULL	COMMENT '구군 코드 (FK)',
	`first_image_1`	VARCHAR(100)	NULL	DEFAULT NULL	COMMENT '이미지 경로 1',
	`first_image_2`	VARCHAR(100)	NULL	DEFAULT NULL	COMMENT '이미지 경로 2',
	`map_level`	INT	NULL	DEFAULT NULL	COMMENT '지도 줌 레벨',
	`latitude`	DECIMAL(20, 17)	NULL	DEFAULT NULL,
	`longitude`	DECIMAL(20, 17)	NULL	DEFAULT NULL,
	`tel`	VARCHAR(20)	NULL	DEFAULT NULL,
	`address_1`	VARCHAR(100)	NULL	DEFAULT NULL,
	`address_2`	VARCHAR(100)	NULL	DEFAULT NULL,
    PRIMARY KEY (`attraction_id`)
);

-- ************************************************************
-- 5. 핫플레이스 테이블 (hotplace) - BIGINT로 ID 통일
-- ************************************************************

CREATE TABLE `hotplace` (
	`place_id`	BIGINT	NOT NULL auto_increment	COMMENT '핫플레이스 ID (PK, Long 타입)',
	`place_name`	VARCHAR(200)	NOT NULL,
	`category`	VARCHAR(50)	NOT NULL,
CREATE TABLE `review` (
	`review_id`	BIGINT	NOT NULL auto_increment,
	`place_id`	BIGINT	NOT NULL	COMMENT '핫플레이스 ID (FK)',
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자 ID (FK, String DTO와 통일)',
	`rating`	TINYINT	NOT NULL,
	`content`	TEXT	NULL,
	`created_at`	DATETIME	NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`time_suitability_score`	TINYINT	NULL,
    PRIMARY KEY (`review_id`)
);

-- ************************************************************
-- 7. 공지사항 (notice) - BIGINT로 ID 통일
-- ************************************************************

CREATE TABLE `notice` (
	`notice_id` BIGINT NOT NULL auto_increment,
    `title` varchar(100) NOT NULL,
    `content` TEXT NOT NULL,
    `author` varchar(50) NOT NULL,
    `hit` int DEFAULT 0,
    `write_Date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`notice_id`)
);

-- ************************************************************
-- 7. 퀵 플랜 테이블 (quick_plan) - BIGINT로 ID 통일
-- ************************************************************

CREATE TABLE `quick_plan` (
	`plan_id`	BIGINT	NOT NULL auto_increment,
	`user_id`	BIGINT	NOT NULL	COMMENT '사용자 ID (FK)',
	`total_time_input`	INT	NOT NULL,
	`start_location`	VARCHAR(255)	NULL	DEFAULT '출발지',
	`created_at`	DATETIME	NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`status`	VARCHAR(20)	NOT NULL	DEFAULT 'TEMP' COMMENT '상태(TEMP/COMPLETE)',
    PRIMARY KEY (`plan_id`)
);

-- ************************************************************
-- 8. 플랜 아이템 테이블 (plan_item) - BIGINT로 ID 통일
-- ************************************************************

CREATE TABLE `plan_item` (
	`plan_item_id`	BIGINT	NOT NULL auto_increment	COMMENT '계획 항목 ID (PK, Long 타입)',
	`plan_id`	BIGINT	NOT NULL	COMMENT '퀵 플랜 ID (FK, Long 타입)',
	`place_id`	BIGINT	NOT NULL	COMMENT '핫플레이스 ID (FK, Long 타입)',
	`sequence_order`	INT	NOT NULL,
	`transport_mode`	VARCHAR(20)	NULL	DEFAULT '이동 수단',
	`estimated_duration`	INT	NULL	DEFAULT 60,
    PRIMARY KEY (`plan_item_id`)
);

-- ************************************************************
-- 9. 뉴스 스크랩 테이블 (news_scrap) - BIGINT로 ID 통일
-- ************************************************************

CREATE TABLE `news_scrap` (
	`news_id`	BIGINT	NOT NULL auto_increment	COMMENT '스크랩 ID (PK, Long 타입)',
	`place_id`	BIGINT	NOT NULL	COMMENT '핫플레이스 ID (FK, Long 타입)',
	`title`	VARCHAR(255)	NOT NULL,
	`url`	VARCHAR(500)	NOT NULL,
	`summary`	TEXT	NULL,
	`scraped_at`	DATETIME	NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`news_id`)
);

-- 10. 블랙리스트 테이블 (blacklist)
CREATE TABLE `blacklist` (
     `id`            INT             NOT NULL AUTO_INCREMENT COMMENT '블랙리스트 ID (PK)',
     `email`         VARCHAR(100)    NOT NULL COMMENT '사용자 이메일',
     `access_token`  VARCHAR(500)    NOT NULL COMMENT '차단된 액세스 토큰',
     `expiration`    VARCHAR(100)    NOT NULL COMMENT '토큰 만료 시각 (문자열)',
     PRIMARY KEY (`id`)
);

-- 11. 리프레시 토큰 테이블 (refresh)
CREATE TABLE `refresh` (
       `id`            INT             NOT NULL AUTO_INCREMENT COMMENT '리프레시 토큰 ID (PK)',
       `email`         VARCHAR(100)    NOT NULL COMMENT '사용자 이메일',
       `refresh_token` VARCHAR(500)    NOT NULL COMMENT '리프레시 토큰',
       `expiration`    VARCHAR(100)    NOT NULL COMMENT '토큰 만료 시각 (문자열)',
       PRIMARY KEY (`id`)
);


-- ************************************************************
-- 외래키(FOREIGN KEY) 제약조건 추가
-- ************************************************************

-- GUGUNS FK (sido_code)
ALTER TABLE `guguns` ADD CONSTRAINT `FK_GUGUNS_SIDOS` FOREIGN KEY (`sido_code`)
REFERENCES `sidos` (`sido_code`) ON DELETE CASCADE;

-- ATTRACTIONS FKs 
ALTER TABLE `attractions` ADD CONSTRAINT `FK_ATTRACTIONS_CONTENTTYPES` FOREIGN KEY (`content_type_id`)
REFERENCES `contenttypes` (`content_type_id`) ON DELETE SET NULL;

ALTER TABLE `attractions` ADD CONSTRAINT `FK_ATTRACTIONS_SIDO` FOREIGN KEY (`area_code`)
REFERENCES `sidos` (`sido_code`) ON DELETE SET NULL;

-- area_code와 si_gun_gu_code 복합키 FK
ALTER TABLE `attractions` ADD CONSTRAINT `FK_ATTRACTIONS_GUGUN` FOREIGN KEY (`area_code`, `si_gun_gu_code`)
REFERENCES `guguns` (`sido_code`, `gugun_code`) ON DELETE SET NULL;


-- REVIEW FKs 
ALTER TABLE `review` ADD CONSTRAINT `FK_REVIEW_HOTPLACE` FOREIGN KEY (`place_id`)
REFERENCES `hotplace` (`place_id`) ON DELETE CASCADE;

ALTER TABLE `review` ADD CONSTRAINT `FK_REVIEW_USERS` FOREIGN KEY (`user_id`)
REFERENCES `users` (`user_id`) ON DELETE CASCADE; 

-- QUICK_PLAN FK
ALTER TABLE `quick_plan` ADD CONSTRAINT `FK_PLAN_USERS` FOREIGN KEY (`user_id`)
REFERENCES `users` (`user_id`) ON DELETE CASCADE;

-- PLAN_ITEM FKs 
ALTER TABLE `plan_item` ADD CONSTRAINT `FK_PLANITEM_PLAN` FOREIGN KEY (`plan_id`)
REFERENCES `quick_plan` (`plan_id`) ON DELETE CASCADE;

ALTER TABLE `plan_item` ADD CONSTRAINT `FK_PLANITEM_HOTPLACE` FOREIGN KEY (`place_id`)
REFERENCES `hotplace` (`place_id`) ON DELETE CASCADE;

-- NEWS_SCRAP FK
ALTER TABLE `news_scrap` ADD CONSTRAINT `FK_NEWSCRAP_HOTPLACE` FOREIGN KEY (`place_id`)
REFERENCES `hotplace` (`place_id`) ON DELETE CASCADE;

ALTER TABLE `blacklist` ADD CONSTRAINT `FK_BLACKLIST_USERS` FOREIGN KEY (`email`)
REFERENCES `users` (`email`) ON DELETE CASCADE;

ALTER TABLE `refresh` ADD CONSTRAINT `FK_REFRESH_USERS` FOREIGN KEY (`email`)
REFERENCES `users` (`email`) ON DELETE CASCADE;