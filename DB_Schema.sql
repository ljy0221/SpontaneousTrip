-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema finalproject
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema finalproject
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `finalproject` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `finalproject` ;

-- -----------------------------------------------------
-- Table `finalproject`.`sidos`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`sidos` (
  `no` INT NOT NULL AUTO_INCREMENT COMMENT '시도번호',
  `sido_code` INT NOT NULL COMMENT '시도코드',
  `sido_name` VARCHAR(20) NULL DEFAULT NULL COMMENT '시도이름',
  PRIMARY KEY (`no`),
  UNIQUE INDEX `sido_code_UNIQUE` (`sido_code` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 35
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = '시도정보테이블';


-- -----------------------------------------------------
-- Table `finalproject`.`guguns`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`guguns` (
  `no` INT NOT NULL AUTO_INCREMENT COMMENT '구군번호',
  `sido_code` INT NOT NULL COMMENT '시도코드',
  `gugun_code` INT NOT NULL COMMENT '구군코드',
  `gugun_name` VARCHAR(20) NULL DEFAULT NULL COMMENT '구군이름',
  PRIMARY KEY (`no`),
  INDEX `guguns_sido_to_sidos_cdoe_fk_idx` (`sido_code` ASC) VISIBLE,
  INDEX `gugun_code_idx` (`gugun_code` ASC) VISIBLE,
  CONSTRAINT `guguns_sido_to_sidos_cdoe_fk`
    FOREIGN KEY (`sido_code`)
    REFERENCES `finalproject`.`sidos` (`sido_code`))
ENGINE = InnoDB
AUTO_INCREMENT = 469
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = '구군정보테이블';


-- -----------------------------------------------------
-- Table `finalproject`.`contenttypes`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`contenttypes` (
  `content_type_id` INT NOT NULL COMMENT '콘텐츠타입번호',
  `content_type_name` VARCHAR(45) NULL DEFAULT NULL COMMENT '콘텐츠타입이름',
  PRIMARY KEY (`content_type_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = '콘텐츠타입정보테이블';


-- -----------------------------------------------------
-- Table `finalproject`.`attractions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`attractions` (
  `no` INT NOT NULL AUTO_INCREMENT COMMENT '명소코드',
  `content_id` INT NULL DEFAULT NULL COMMENT '콘텐츠번호',
  `title` VARCHAR(500) NULL DEFAULT NULL COMMENT '명소이름',
  `content_type_id` INT NULL DEFAULT NULL COMMENT '콘텐츠타입',
  `area_code` INT NULL DEFAULT NULL COMMENT '시도코드',
  `si_gun_gu_code` INT NULL DEFAULT NULL COMMENT '구군코드',
  `first_image1` VARCHAR(100) NULL DEFAULT NULL COMMENT '이미지경로1',
  `first_image2` VARCHAR(100) NULL DEFAULT NULL COMMENT '이미지경로2',
  `map_level` INT NULL DEFAULT NULL COMMENT '줌레벨',
  `latitude` DECIMAL(20,17) NULL DEFAULT NULL COMMENT '위도',
  `longitude` DECIMAL(20,17) NULL DEFAULT NULL COMMENT '경도',
  `tel` VARCHAR(20) NULL DEFAULT NULL COMMENT '전화번호',
  `addr1` VARCHAR(100) NULL DEFAULT NULL COMMENT '주소1',
  `addr2` VARCHAR(100) NULL DEFAULT NULL COMMENT '주소2',
  `homepage` VARCHAR(1000) NULL DEFAULT NULL COMMENT '홈페이지',
  `overview` VARCHAR(10000) NULL DEFAULT NULL COMMENT '설명',
  PRIMARY KEY (`no`),
  INDEX `attractions_typeid_to_types_typeid_fk_idx` (`content_type_id` ASC) VISIBLE,
  INDEX `attractions_sido_to_sidos_code_fk_idx` (`area_code` ASC) VISIBLE,
  INDEX `attractions_sigungu_to_guguns_gugun_fk_idx` (`si_gun_gu_code` ASC) VISIBLE,
  CONSTRAINT `attractions_area_to_sidos_code_fk`
    FOREIGN KEY (`area_code`)
    REFERENCES `finalproject`.`sidos` (`sido_code`),
  CONSTRAINT `attractions_sigungu_to_guguns_gugun_fk`
    FOREIGN KEY (`si_gun_gu_code`)
    REFERENCES `finalproject`.`guguns` (`gugun_code`),
  CONSTRAINT `attractions_typeid_to_types_typeid_fk`
    FOREIGN KEY (`content_type_id`)
    REFERENCES `finalproject`.`contenttypes` (`content_type_id`))
ENGINE = InnoDB
AUTO_INCREMENT = 107559
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = '명소정보테이블';


-- -----------------------------------------------------
-- Table `finalproject`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`users` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 고유 ID (PK)',
  `email` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(50) NULL DEFAULT '닉네임',
  `role` VARCHAR(20) NULL DEFAULT 'ROLE_USER' COMMENT '사용자 권한',
  `join_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login` DATETIME NULL DEFAULT NULL,
  `preference` VARCHAR(20) NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE INDEX `email` (`email` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 758
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `finalproject`.`blacklist`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`blacklist` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '블랙리스트 ID (PK)',
  `email` VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
  `access_token` VARCHAR(500) NOT NULL COMMENT '차단된 액세스 토큰',
  `expiration` VARCHAR(100) NOT NULL COMMENT '토큰 만료 시각 (문자열)',
  PRIMARY KEY (`id`),
  INDEX `FK_BLACKLIST_USERS` (`email` ASC) VISIBLE,
  CONSTRAINT `FK_BLACKLIST_USERS`
    FOREIGN KEY (`email`)
    REFERENCES `finalproject`.`users` (`email`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 657
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `finalproject`.`hotplace`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`hotplace` (
  `place_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '핫플레이스 ID (PK)',
  `place_name` VARCHAR(200) NOT NULL,
  `category` VARCHAR(50) NOT NULL,
  `address` VARCHAR(255) NULL DEFAULT '주소',
  `image` VARCHAR(100) NULL DEFAULT NULL,
  `pos` POINT NULL DEFAULT NULL,
  `api_source` VARCHAR(50) NULL DEFAULT '데이터 출처',
  `is_pet_friendly` TINYINT(1) NOT NULL DEFAULT '1',
  `avg_rating` DECIMAL(3,2) NULL DEFAULT '0.00',
  `overview` TEXT NULL DEFAULT NULL COMMENT '명소 설명',
  `count` INT NOT NULL DEFAULT '0',
  PRIMARY KEY (`place_id`),
  INDEX `idx_hotplace_name` (`place_name` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 196606
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `finalproject`.`hotplace_backup`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`hotplace_backup` (
  `place_id` BIGINT NOT NULL DEFAULT '0' COMMENT '핫플레이스 ID (PK)',
  `place_name` VARCHAR(200) NOT NULL,
  `category` VARCHAR(50) NOT NULL,
  `address` VARCHAR(255) NULL DEFAULT '주소',
  `pos` POINT NULL DEFAULT NULL,
  `api_source` VARCHAR(50) NULL DEFAULT '데이터 출처',
  `is_pet_friendly` TINYINT(1) NOT NULL DEFAULT '1',
  `avg_rating` DECIMAL(3,2) NULL DEFAULT '0.00',
  `overview` TEXT NULL DEFAULT NULL COMMENT '명소 설명')
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `finalproject`.`news_scrap`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`news_scrap` (
  `news_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '스크랩 ID (PK)',
  `place_id` BIGINT NOT NULL COMMENT '핫플레이스 ID (FK)',
  `title` VARCHAR(255) NOT NULL,
  `url` VARCHAR(500) NOT NULL,
  `summary` TEXT NULL DEFAULT NULL,
  `scraped_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`news_id`),
  INDEX `FK_NEWSCRAP_HOTPLACE` (`place_id` ASC) VISIBLE,
  CONSTRAINT `FK_NEWSCRAP_HOTPLACE`
    FOREIGN KEY (`place_id`)
    REFERENCES `finalproject`.`hotplace` (`place_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `finalproject`.`notice`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`notice` (
  `notice_id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `author` VARCHAR(50) NOT NULL,
  `hit` INT NULL DEFAULT '0',
  `write_Date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `category` VARCHAR(20) NULL DEFAULT NULL,
  PRIMARY KEY (`notice_id`))
ENGINE = InnoDB
AUTO_INCREMENT = 28
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `finalproject`.`quick_plan`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`quick_plan` (
  `plan_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '사용자 ID (FK)',
  `total_time_input` INT NOT NULL,
  `start_location` VARCHAR(255) NULL DEFAULT '출발지',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(20) NULL DEFAULT 'ONGOING',
  PRIMARY KEY (`plan_id`),
  INDEX `FK_PLAN_USERS` (`user_id` ASC) VISIBLE,
  CONSTRAINT `FK_PLAN_USERS`
    FOREIGN KEY (`user_id`)
    REFERENCES `finalproject`.`users` (`user_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 453
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `finalproject`.`plan_item`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`plan_item` (
  `plan_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '계획 항목 ID (PK)',
  `plan_id` BIGINT NOT NULL COMMENT '퀵 플랜 ID (FK)',
  `place_id` BIGINT NOT NULL COMMENT '핫플레이스 ID (FK)',
  `sequence_order` INT NOT NULL,
  `transport_mode` VARCHAR(20) NULL DEFAULT '이동 수단',
  `estimated_duration` INT NULL DEFAULT '60',
  PRIMARY KEY (`plan_item_id`),
  INDEX `FK_PLANITEM_PLAN` (`plan_id` ASC) VISIBLE,
  INDEX `FK_PLANITEM_HOTPLACE` (`place_id` ASC) VISIBLE,
  CONSTRAINT `FK_PLANITEM_HOTPLACE`
    FOREIGN KEY (`place_id`)
    REFERENCES `finalproject`.`hotplace` (`place_id`)
    ON DELETE CASCADE,
  CONSTRAINT `FK_PLANITEM_PLAN`
    FOREIGN KEY (`plan_id`)
    REFERENCES `finalproject`.`quick_plan` (`plan_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 74
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `finalproject`.`refresh`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`refresh` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '리프레시 토큰 ID (PK)',
  `email` VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
  `refresh_token` VARCHAR(500) NOT NULL COMMENT '리프레시 토큰',
  `expiration` VARCHAR(100) NOT NULL COMMENT '토큰 만료 시각 (문자열)',
  PRIMARY KEY (`id`),
  INDEX `FK_REFRESH_USERS` (`email` ASC) VISIBLE,
  CONSTRAINT `FK_REFRESH_USERS`
    FOREIGN KEY (`email`)
    REFERENCES `finalproject`.`users` (`email`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 936
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `finalproject`.`review`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `finalproject`.`review` (
  `review_id` BIGINT NOT NULL AUTO_INCREMENT,
  `place_id` BIGINT NOT NULL COMMENT '핫플레이스 ID (FK)',
  `user_id` BIGINT NOT NULL COMMENT '사용자 ID (FK)',
  `rating` TINYINT NOT NULL,
  `content` TEXT NULL DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `time_suitability_score` TINYINT NULL DEFAULT NULL,
  PRIMARY KEY (`review_id`),
  INDEX `FK_REVIEW_HOTPLACE` (`place_id` ASC) VISIBLE,
  INDEX `FK_REVIEW_USERS` (`user_id` ASC) VISIBLE,
  CONSTRAINT `FK_REVIEW_HOTPLACE`
    FOREIGN KEY (`place_id`)
    REFERENCES `finalproject`.`hotplace` (`place_id`)
    ON DELETE CASCADE,
  CONSTRAINT `FK_REVIEW_USERS`
    FOREIGN KEY (`user_id`)
    REFERENCES `finalproject`.`users` (`user_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 9
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
