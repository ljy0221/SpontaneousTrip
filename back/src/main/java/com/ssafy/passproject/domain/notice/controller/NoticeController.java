package com.ssafy.passproject.domain.notice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.ssafy.passproject.domain.notice.dto.request.NoticeRequest;
import com.ssafy.passproject.domain.notice.dto.request.SearchCondition;
import com.ssafy.passproject.domain.notice.dto.response.NoticeResponse;
import com.ssafy.passproject.domain.notice.dto.response.PagedNoticeResponse;
import com.ssafy.passproject.domain.notice.entity.Notice.NoticeCategory;
import com.ssafy.passproject.domain.notice.service.NoticeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "Notice API", description = "공지사항 관리 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notice")
@RestController
public class NoticeController {
    private final NoticeService service;

    @Operation(summary = "공지사항 목록 조회", description = "검색 조건(제목, 내용 등)에 따라 공지사항 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"목록 조회 성공\", \"data\": [], \"pageInfo\": {\"currentPage\": 0, \"pageSize\": 10, \"totalElements\": 0, \"totalPages\": 0, \"hasNext\": false, \"hasPrevious\": false}}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 페이지 파라미터", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"페이지 번호는 1 이상, 페이지 크기는 0보다 커야 합니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"목록 조회 실패\"}")))
    })
    @GetMapping
    public ResponseEntity<?> list(@ModelAttribute SearchCondition condition) {
        try {
            // Validate pagination parameters
            if (condition.getPage() < 1 || condition.getSize() <= 0) {
                return new ResponseEntity<>(
                    Map.of("message", "페이지 번호는 1 이상, 페이지 크기는 0보다 커야 합니다."),
                    HttpStatus.BAD_REQUEST
                );
            }

            PagedNoticeResponse pagedResponse = service.getList(condition);
            return new ResponseEntity<>(Map.of(
                "message", "목록 조회 성공",
                "data", pagedResponse.getData(),
                "pageInfo", pagedResponse.getPageInfo()
            ), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Notice List Error", e);
            return new ResponseEntity<>(Map.of("message", "목록 조회 실패"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "공지사항 상세 조회", description = "ID를 기반으로 공지사항의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"상세 조회 성공\", \"notice\": {\"id\": 1, \"title\": \"제목\", \"content\": \"내용\"}}"))),
            @ApiResponse(responseCode = "404", description = "해당 게시글 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"해당 게시글이 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"상세 조회 실패\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        try {
            NoticeResponse notice = service.getDetail(id);
            if (notice == null) {
                return new ResponseEntity<>(Map.of("message", "해당 게시글이 없습니다."), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(Map.of("message", "상세 조회 성공", "notice", notice), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Notice Detail Error", e);
            return new ResponseEntity<>(Map.of("message", "상세 조회 실패"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 등록합니다. NOTICE 카테고리는 ROLE_ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"작성 성공\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (NOTICE 카테고리)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"공지사항은 관리자만 작성할 수 있습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"작성 실패\"}")))
    })
    @PostMapping
    public ResponseEntity<?> write(@Valid @RequestBody NoticeRequest notice) {
        try {
            // NOTICE 카테고리인 경우에만 관리자 권한 체크
            if (notice.getCategory() == NoticeCategory.NOTICE) {
                if (!isAdmin()) {
                    return new ResponseEntity<>(Map.of("message", "공지사항은 관리자만 작성할 수 있습니다."), HttpStatus.FORBIDDEN);
                }
            }
            
            service.write(notice);
            return new ResponseEntity<>(Map.of("message", "작성 성공"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Notice Write Error", e);
            return new ResponseEntity<>(Map.of("message", "작성 실패"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다. NOTICE 카테고리는 ROLE_ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"수정 성공\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (NOTICE 카테고리)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"공지사항은 관리자만 수정할 수 있습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"수정 실패\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> modify(@PathVariable Long id, @Valid @RequestBody NoticeRequest notice) {
        try {
            // NOTICE 카테고리인 경우에만 관리자 권한 체크
            if (notice.getCategory() == NoticeCategory.NOTICE) {
                if (!isAdmin()) {
                    return new ResponseEntity<>(Map.of("message", "공지사항은 관리자만 수정할 수 있습니다."), HttpStatus.FORBIDDEN);
                }
            }
            
            service.modify(id, notice);
            return new ResponseEntity<>(Map.of("message", "수정 성공"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Notice Modify Error", e);
            return new ResponseEntity<>(Map.of("message", "수정 실패"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "게시글 삭제", description = "ID를 기반으로 게시글을 삭제합니다. NOTICE 카테고리는 ROLE_ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"삭제 성공\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (NOTICE 카테고리)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"공지사항은 관리자만 삭제할 수 있습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"삭제 실패\"}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            // 삭제하려는 게시글의 카테고리를 먼저 조회
            NoticeResponse notice = service.getDetail(id);
            if (notice == null) {
                return new ResponseEntity<>(Map.of("message", "해당 게시글이 없습니다."), HttpStatus.NOT_FOUND);
            }
            
            // NOTICE 카테고리인 경우에만 관리자 권한 체크
            if (notice.getCategory() == NoticeCategory.NOTICE) {
                if (!isAdmin()) {
                    return new ResponseEntity<>(Map.of("message", "공지사항은 관리자만 삭제할 수 있습니다."), HttpStatus.FORBIDDEN);
                }
            }
            
            service.remove(id);
            return new ResponseEntity<>(Map.of("message", "삭제 성공"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Notice Delete Error", e);
            return new ResponseEntity<>(Map.of("message", "삭제 실패"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 현재 로그인한 사용자가 관리자인지 확인
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Authentication is null or not authenticated");
            return false;
        }
        
        // 디버깅: 권한 정보 로그 출력
        log.info("===== Admin Check Debug =====");
        log.info("User: {}", authentication.getName());
        log.info("Authorities: {}", authentication.getAuthorities());
        
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> {
                    String authority = auth.getAuthority();
                    log.info("Checking authority: [{}]", authority);
                    return authority.equals("ROLE_ADMIN");
                });
        
        log.info("Is Admin: {}", hasAdminRole);
        log.info("=============================");
        return hasAdminRole;
    }
}