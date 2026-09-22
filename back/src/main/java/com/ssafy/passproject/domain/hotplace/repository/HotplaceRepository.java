package com.ssafy.passproject.domain.hotplace.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.passproject.domain.hotplace.entity.Hotplace;

@Mapper
public interface HotplaceRepository {
	public void insertHotplace(Hotplace hotplace) throws Exception;;

	public Hotplace selectHotplaceDetail(Hotplace hotplace) throws Exception;

	public List<Hotplace> selectHotplaceList() throws Exception;

	public List<Hotplace> findByCategory(@Param("category") String category) throws Exception;

	public void updateHotplace(Hotplace hotplace) throws Exception;

	public void deleteHotplace(Hotplace hotplace) throws Exception;

	public Hotplace selectHotplaceDetailWithReviews(Long placeId) throws Exception;

	public void updateHotplaceAvgRating(Long placeId) throws Exception;

	// 페이징 없는 버전 (기존 호환성 유지)
	public List<Hotplace> findNearByHotplaces(
			@Param("currentLat") double currentLat,
			@Param("currentLon") double currentLon,
			@Param("radiusMeters") int radiusMeters) throws Exception;

	// 페이징 지원 버전 (MBR 최적화)
	public List<Hotplace> findNearByHotplaces(
			@Param("currentLat") double currentLat,
			@Param("currentLon") double currentLon,
			@Param("radiusMeters") int radiusMeters,
			@Param("minLat") double minLat,
			@Param("maxLat") double maxLat,
			@Param("minLon") double minLon,
			@Param("maxLon") double maxLon,
			@Param("offset") int offset,
			@Param("limit") int limit) throws Exception;

	public int countNearByHotplaces(
			@Param("currentLat") double currentLat,
			@Param("currentLon") double currentLon,
			@Param("radiusMeters") int radiusMeters,
			@Param("minLat") double minLat,
			@Param("maxLat") double maxLat,
			@Param("minLon") double minLon,
			@Param("maxLon") double maxLon) throws Exception;

	// 카테고리 필터 포함 버전 (MBR 최적화)
	public List<Hotplace> findNearByHotplacesWithCategory(
			@Param("currentLat") double currentLat,
			@Param("currentLon") double currentLon,
			@Param("radiusMeters") int radiusMeters,
			@Param("category") String category,
			@Param("minLat") double minLat,
			@Param("maxLat") double maxLat,
			@Param("minLon") double minLon,
			@Param("maxLon") double maxLon,
			@Param("offset") int offset,
			@Param("limit") int limit) throws Exception;

	public int countNearByHotplacesWithCategory(
			@Param("currentLat") double currentLat,
			@Param("currentLon") double currentLon,
			@Param("radiusMeters") int radiusMeters,
			@Param("category") String category,
			@Param("minLat") double minLat,
			@Param("maxLat") double maxLat,
			@Param("minLon") double minLon,
			@Param("maxLon") double maxLon) throws Exception;

	// 중복 체크용 메서드
	public Hotplace findByNameAndPosition(
			@Param("placeName") String placeName,
			@Param("pos") org.locationtech.jts.geom.Point pos) throws Exception;

	// Hotplace count 증가 (QuickPlan 생성 시 사용)
	public void incrementHotplaceCount(@Param("placeId") Long placeId) throws Exception;

	// 위치 기반 인기 Hotplace 조회 (count 내림차순, 상위 N개)
	public List<Hotplace> findPopularHotplacesByLocation(
			@Param("currentLat") double currentLat,
			@Param("currentLon") double currentLon,
			@Param("radiusMeters") int radiusMeters,
			@Param("limit") int limit) throws Exception;
}
