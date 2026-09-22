package com.ssafy.passproject.domain.tour.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.passproject.domain.tour.dto.request.TourApiRequestDto;
import com.ssafy.passproject.domain.tour.dto.request.TourDetailRequestDto;
import com.ssafy.passproject.domain.tour.dto.response.TourDetailResponseDto;
import com.ssafy.passproject.domain.tour.entity.TourPlace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourApiServiceImpl implements TourApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${tour.api.service-key}")
    private String serviceKey;

    @Override
    public List<String> getContentIds(TourApiRequestDto requestDto) throws Exception {
        List<TourPlace> tourPlaces = getTourPlaces(requestDto);

        return tourPlaces.stream()
                .map(TourPlace::getContentId)
                .collect(Collectors.toList());
    }

    @Override
    public List<TourPlace> getTourPlaces(TourApiRequestDto requestDto) throws Exception {
        try {
            log.info("========================================");
            log.info("요청 파라미터:");
            log.info("mapX: {}", requestDto.getMapX());
            log.info("mapY: {}", requestDto.getMapY());
            log.info("radius: {}", requestDto.getRadius());
            log.info("contentTypeId: {}", requestDto.getContentTypeId());
            log.info("========================================");

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString("https://apis.data.go.kr/B551011/KorService2/locationBasedList2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "AppTest")
                    .queryParam("_type", "json")
                    .queryParam("mapX", requestDto.getMapX())
                    .queryParam("mapY", requestDto.getMapY())
                    .queryParam("radius", requestDto.getRadius() != null ? requestDto.getRadius() : "1000")
                    .queryParam("numOfRows", requestDto.getNumOfRows() != null ? requestDto.getNumOfRows() : "50")
                    .queryParam("pageNo", requestDto.getPageNo() != null ? requestDto.getPageNo() : "1");

            if (requestDto.getArrange() != null && !requestDto.getArrange().isEmpty()) {
                builder.queryParam("arrange", requestDto.getArrange());
            }

            if (requestDto.getContentTypeId() != null && !requestDto.getContentTypeId().isEmpty()) {
                builder.queryParam("contentTypeId", requestDto.getContentTypeId());
            }

            URI uri = builder.build(true).toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Accept", "*/*");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("========================================");
            log.info("Tour API 호출");
            log.info("Full URI: {}", uri.toString());
            log.info("========================================");

            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    String.class);

            log.info("✅ 응답 상태: {}", response.getStatusCode());
            log.info("응답 본문: {}", response.getBody());

            return parseTourApiResponse(response.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("========================================");
            log.error("❌ API 호출 실패!");
            log.error("상태코드: {}", e.getStatusCode());
            log.error("응답 본문: {}", e.getResponseBodyAsString());
            log.error("========================================");
            throw new Exception("관광 정보 조회 실패: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("처리 중 오류", e);
            throw new Exception("관광 정보 조회 중 오류: " + e.getMessage(), e);
        }
    }

    @Override
    public TourDetailResponseDto getTourDetail(TourDetailRequestDto requestDto) throws Exception {
        try {
            log.info("========================================");
            log.info("공통정보 조회 요청");
            log.info("contentId: {}", requestDto.getContentId());
            log.info("numOfRows: {}", requestDto.getNumOfRows());
            log.info("pageNo: {}", requestDto.getPageNo());
            log.info("========================================");

            // 필수 파라미터 모두 포함
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString("https://apis.data.go.kr/B551011/KorService2/detailCommon2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "AppTest")
                    .queryParam("_type", "json")
                    .queryParam("contentId", requestDto.getContentId())
                    .queryParam("numOfRows", requestDto.getNumOfRows())
                    .queryParam("pageNo", requestDto.getPageNo());

            URI uri = builder.build(true).toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Accept", "*/*");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("========================================");
            log.info("DetailCommon API 호출");
            log.info("Full URI: {}", uri.toString());
            log.info("========================================");

            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    String.class);

            log.info("✅ 응답 상태: {}", response.getStatusCode());
            log.info("응답 본문: {}", response.getBody());

            return parseTourDetailResponse(response.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("========================================");
            log.error("❌ API 호출 실패!");
            log.error("상태코드: {}", e.getStatusCode());
            log.error("응답 본문: {}", e.getResponseBodyAsString());
            log.error("========================================");
            throw new Exception("공통정보 조회 실패: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("처리 중 오류", e);
            throw new Exception("공통정보 조회 중 오류: " + e.getMessage(), e);
        }
    }

    private List<TourPlace> parseTourApiResponse(String response) throws Exception {
        List<TourPlace> tourPlaces = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode header = root.path("response").path("header");
            String resultCode = header.path("resultCode").asText();
            String resultMsg = header.path("resultMsg").asText();

            if (!"0000".equals(resultCode)) {
                throw new Exception("API 오류: " + resultMsg);
            }

            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    tourPlaces.add(parseTourPlace(item));
                }
            } else if (items.isObject()) {
                tourPlaces.add(parseTourPlace(items));
            }

            log.info("파싱 완료 - {}개", tourPlaces.size());

        } catch (Exception e) {
            log.error("파싱 실패", e);
            throw new Exception("파싱 오류: " + e.getMessage(), e);
        }

        return tourPlaces;
    }

    private TourDetailResponseDto parseTourDetailResponse(String response) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode header = root.path("response").path("header");
            String resultCode = header.path("resultCode").asText();
            String resultMsg = header.path("resultMsg").asText();

            log.info("API response code: {}, msg: {}", resultCode, resultMsg);

            if (!"0000".equals(resultCode)) {
                throw new Exception("API error: " + resultMsg + " (code: " + resultCode + ")");
            }

            JsonNode items = root.path("response").path("body").path("items").path("item");

            // 단일 객체 또는 배열의 첫 번째 요소 가져오기
            JsonNode item;
            if (items.isArray() && items.size() > 0) {
                item = items.get(0);
            } else if (items.isObject()) {
                item = items;
            } else {
                throw new Exception("Tour detail not found");
            }
            log.info(item.get("overview").asText());
            log.info("Tour detail parsed successfully");

            return parseTourDetail(item);

        } catch (Exception e) {
            log.error("Tour detail parsing failed", e);
            throw new Exception("Tour detail parsing failed: " + e.getMessage(), e);
        }
    }

    private TourPlace parseTourPlace(JsonNode item) {
        TourPlace tourPlace = new TourPlace();
        tourPlace.setContentId(item.path("contentid").asText());
        tourPlace.setContentTypeId(item.path("contenttypeid").asText());
        tourPlace.setTitle(item.path("title").asText());
        tourPlace.setAddr1(item.path("addr1").asText());
        tourPlace.setAddr2(item.path("addr2").asText());
        tourPlace.setMapX(item.path("mapx").asText());
        tourPlace.setMapY(item.path("mapy").asText());
        tourPlace.setFirstImage(item.path("firstimage").asText());
        tourPlace.setFirstImage2(item.path("firstimage2").asText());
        tourPlace.setTel(item.path("tel").asText());

        String distStr = item.path("dist").asText();
        if (!distStr.isEmpty()) {
            try {
                tourPlace.setDist(Double.parseDouble(distStr));
            } catch (NumberFormatException e) {
                log.warn("거리 파싱 실패: {}", distStr);
            }
        }

        return tourPlace;
    }

    private TourDetailResponseDto parseTourDetail(JsonNode item) {
        TourDetailResponseDto detailDto = new TourDetailResponseDto();

        detailDto.setContentId(item.path("contentid").asText());
        detailDto.setContentTypeId(item.path("contenttypeid").asText());
        detailDto.setTitle(item.path("title").asText());
        detailDto.setCreatedTime(item.path("createdtime").asText());
        detailDto.setModifiedTime(item.path("modifiedtime").asText());
        detailDto.setTel(item.path("tel").asText());
        detailDto.setTelName(item.path("telname").asText());
        detailDto.setHomepage(item.path("homepage").asText());
        detailDto.setFirstImage(item.path("firstimage").asText());
        detailDto.setFirstImage2(item.path("firstimage2").asText());
        detailDto.setCpyrhtDivCd(item.path("cpyrhtDivCd").asText());
        detailDto.setAreaCode(item.path("areacode").asText());
        detailDto.setSigunguCode(item.path("sigungucode").asText());
        detailDto.setCat1(item.path("cat1").asText());
        detailDto.setCat2(item.path("cat2").asText());
        detailDto.setCat3(item.path("cat3").asText());
        detailDto.setAddr1(item.path("addr1").asText());
        detailDto.setAddr2(item.path("addr2").asText());
        detailDto.setZipcode(item.path("zipcode").asText());
        detailDto.setMapX(item.path("mapx").asText());
        detailDto.setMapY(item.path("mapy").asText());
        detailDto.setMlevel(item.path("mlevel").asText());
        detailDto.setOverview(item.path("overview").asText());
        detailDto.setBookTour(item.path("booktour").asText());

        return detailDto;
    }
}