package com.ssafy.passproject.domain.hotplace.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ssafy.passproject.domain.hotplace.dto.request.ChatRequestDto;
import com.ssafy.passproject.domain.hotplace.dto.request.SummaryRequestDto;
import com.ssafy.passproject.domain.hotplace.dto.response.ChatResponseDto;
import com.ssafy.passproject.domain.hotplace.dto.response.HotplaceResponseDto;
import com.ssafy.passproject.domain.hotplace.dto.response.PagedHotplaceResponse;
import com.ssafy.passproject.domain.hotplace.dto.response.SummaryResponseDto;
import com.ssafy.passproject.domain.hotplace.entity.Hotplace;
import com.ssafy.passproject.domain.hotplace.repository.HotplaceRepository;
import com.ssafy.passproject.domain.tour.dto.request.TourApiRequestDto;
import com.ssafy.passproject.domain.tour.dto.request.TourDetailRequestDto;
import com.ssafy.passproject.domain.tour.dto.response.TourDetailResponseDto;
import com.ssafy.passproject.domain.tour.entity.TourPlace;
import com.ssafy.passproject.domain.tour.service.TourApiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class HotplaceServiceImpl implements HotplaceService {

    private final HotplaceRepository repo;
    private final TourApiService tourApiService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final WebClient webClient = WebClient.builder().baseUrl("http://localhost:9000").build();

    // 교통수단별 평균 속도 (미터/분)
    private static final int SPEED_CAR = 1000; // 시속 60km
    private static final int SPEED_TRANSIT = 500; // 시속 30km
    private static final int SPEED_WALK = 83; // 시속 5km

    /**
     * 중심점과 반경으로 바운딩 박스 계산
     * MBR 기반 공간 검색 최적화를 위한 메서드
     * 
     * @param lat          중심점 위도
     * @param lon          중심점 경도
     * @param radiusMeters 반경 (미터)
     * @return BoundingBox 객체
     */
    private com.ssafy.passproject.domain.hotplace.dto.BoundingBox calculateBoundingBox(
            double lat, double lon, int radiusMeters) {
        // 위도 1도 ≈ 111,320m (지구 둘레 40,075km / 360도)
        double latDelta = radiusMeters / 111320.0;

        // 경도 1도의 거리는 위도에 따라 변함: 111,320m * cos(위도)
        double lonDelta = radiusMeters / (111320.0 * Math.cos(Math.toRadians(lat)));

        return new com.ssafy.passproject.domain.hotplace.dto.BoundingBox(
                lat - latDelta, // minLat
                lat + latDelta, // maxLat
                lon - lonDelta, // minLon
                lon + lonDelta // maxLon
        );
    }

    @Override
    public void insertHotplace(Hotplace hotplace) throws Exception {
        // 중복 체크: 같은 이름과 좌표를 가진 hotplace가 이미 있는지 확인
        Hotplace existing = repo.findByNameAndPosition(
                hotplace.getPlaceName(),
                hotplace.getPos());

        if (existing != null) {
            log.warn("Duplicate hotplace detected: '{}' at ({}, {}). Skipping insert. Existing place_id: {}",
                    hotplace.getPlaceName(),
                    hotplace.getPos().getY(),
                    hotplace.getPos().getX(),
                    existing.getPlaceId());
            return; // 중복이면 삽입하지 않음
        }

        repo.insertHotplace(hotplace);
        log.info("Successfully inserted new hotplace: '{}' with place_id: {}",
                hotplace.getPlaceName(),
                hotplace.getPlaceId());
    }

    @Override
    public List<Hotplace> selectHotplaceList() throws Exception {
        return repo.selectHotplaceList();
    }

    @Override
    public List<Hotplace> findByCategory(String category) throws Exception {
        return repo.findByCategory(category);
    }

    @Override
    public void updateHotplace(Hotplace hotplace) throws Exception {
        repo.updateHotplace(hotplace);
    }

    @Override
    public Hotplace selectHotplaceDetail(Hotplace hotplace) throws Exception {
        return repo.selectHotplaceDetail(hotplace);
    }

    @Override
    public void deleteHotplace(Hotplace hotplace) throws Exception {
        repo.deleteHotplace(hotplace);
    }

    @Override
    public List<HotplaceResponseDto> findSpontaneousPlaces(double currentLat, double currentLon, int timeBudget)
            throws Exception {
        int radiusMeters = timeBudget * 60;
        List<Hotplace> places = repo.findNearByHotplaces(currentLat, currentLon, radiusMeters);
        return places.stream().map(HotplaceResponseDto::new).collect(Collectors.toList());
    }

    @Override
    public PagedHotplaceResponse getNearbyHotplaces(double mapX, double mapY, int timeInMinutes,
            String transportMode, String category, int page, int size) throws Exception {
        // 교통수단에 따른 속도 선택
        int speedMetersPerMinute;
        switch (transportMode.toUpperCase()) {
            case "CAR":
                speedMetersPerMinute = SPEED_CAR;
                break;
            case "TRANSIT":
                speedMetersPerMinute = SPEED_TRANSIT;
                break;
            case "WALK":
                speedMetersPerMinute = SPEED_WALK;
                break;
            default:
                log.warn("Unknown transport mode: {}, defaulting to WALK", transportMode);
                speedMetersPerMinute = SPEED_WALK;
        }

        // 시간을 거리로 변환 (직선거리 기준)
        int radiusMeters = timeInMinutes * speedMetersPerMinute;

        // MBR 최적화: 바운딩 박스 계산
        com.ssafy.passproject.domain.hotplace.dto.BoundingBox bbox = calculateBoundingBox(mapY, mapX, radiusMeters);

        // 페이징 계산
        int offset = page * size;

        // 카테고리 필터 여부에 따라 다른 메서드 호출
        int totalElements;
        List<Hotplace> nearbyHotplaces;

        if (category != null && !category.isEmpty()) {
            // 전체 개수 조회 (카테고리 포함, MBR 최적화)
            totalElements = repo.countNearByHotplacesWithCategory(
                    mapY, mapX, radiusMeters, category,
                    bbox.getMinLat(), bbox.getMaxLat(), bbox.getMinLon(), bbox.getMaxLon());
            // 페이징된 데이터 조회 (카테고리 포함, MBR 최적화)
            nearbyHotplaces = repo.findNearByHotplacesWithCategory(
                    mapY, mapX, radiusMeters, category,
                    bbox.getMinLat(), bbox.getMaxLat(), bbox.getMinLon(), bbox.getMaxLon(),
                    offset, size);
        } else {
            // 전체 개수 조회 (MBR 최적화)
            totalElements = repo.countNearByHotplaces(
                    mapY, mapX, radiusMeters,
                    bbox.getMinLat(), bbox.getMaxLat(), bbox.getMinLon(), bbox.getMaxLon());
            // 페이징된 데이터 조회 (MBR 최적화)
            nearbyHotplaces = repo.findNearByHotplaces(
                    mapY, mapX, radiusMeters,
                    bbox.getMinLat(), bbox.getMaxLat(), bbox.getMinLon(), bbox.getMaxLon(),
                    offset, size);
        }

        // Convert entities to DTOs
        List<HotplaceResponseDto> result = new ArrayList<>();
        for (Hotplace hotplace : nearbyHotplaces) {
            result.add(new HotplaceResponseDto(hotplace));
        }

        return PagedHotplaceResponse.of(result, page, size, totalElements);
    }

    @Override
    public ChatResponseDto chat(ChatRequestDto request) {
        return webClient.post().uri("/api/chat").bodyValue(request).retrieve().bodyToMono(ChatResponseDto.class)
                .block();
    }

    @Override
    public SummaryResponseDto generateSummary(SummaryRequestDto request) {
        return webClient.post().uri("/api/summary").bodyValue(request).retrieve().bodyToMono(SummaryResponseDto.class)
                .block();
    }

    @Override
    public CompletableFuture<SummaryResponseDto> generateSummaryAsync(SummaryRequestDto request) {
        return webClient.post().uri("/api/summary").bodyValue(request).retrieve().bodyToMono(SummaryResponseDto.class)
                .toFuture();
    }

    @Override
    public void fetchAndSaveTourData(String areaCode) throws Exception {
        TourApiRequestDto requestDto = TourApiRequestDto
                .builder()
                .numOfRows("20")
                .pageNo("1")
                .mobileOS("ETC")
                .mobileApp("AppTest")
                .arrange("A")
                .contentTypeId("12")
                .areaCode(areaCode)
                .build();

        List<TourPlace> tourPlaces = tourApiService.getTourPlaces(requestDto);

        for (TourPlace tourPlace : tourPlaces) {
            TourDetailRequestDto detailRequest = TourDetailRequestDto
                    .builder()
                    .contentId(tourPlace.getContentId())
                    .build();

            TourDetailResponseDto detail = tourApiService
                    .getTourDetail(detailRequest);

            Point location = null;
            try {
                double lat = Double.parseDouble(detail.getMapY());
                double lon = Double.parseDouble(detail.getMapX());
                location = geometryFactory.createPoint(new Coordinate(lon, lat));
            } catch (Exception e) {
                log.warn("Invalid coordinates for contentId: {}", tourPlace.getContentId());
                continue;
            }

            Hotplace hotplace = new Hotplace(
                    null,
                    detail.getTitle(),
                    "관광지",
                    detail.getAddr1(),
                    detail.getOverview(),
                    null,
                    location,
                    "TOUR_API",
                    true,
                    0.0,
                    0);

            repo.insertHotplace(hotplace);

            // AI summary 생성 로직 제거 (description 필드가 없어졌으므로)
            // 프론트에서 실시간으로 AI 설명을 생성하므로 백엔드에 저장할 필요 없음
        }
    }

    @Override
    public Hotplace selectHotplaceDetailWithReviews(Long placeId) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'selectHotplaceDetailWithReviews'");
    }

    @Override
    public void updateHotplaceAvgRating(Long placeId) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'updateHotplaceAvgRating'");
    }

    @Override
    public List<HotplaceResponseDto> getPopularHotplaces(double currentLat, double currentLon, int radiusMeters,
            int limit) throws Exception {
        List<Hotplace> hotplaces = repo.findPopularHotplacesByLocation(currentLat, currentLon, radiusMeters, limit);
        return hotplaces.stream()
                .map(HotplaceResponseDto::new)
                .collect(Collectors.toList());
    }
}
