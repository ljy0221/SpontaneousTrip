package com.ssafy.passproject.domain.hotplace.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ssafy.passproject.domain.hotplace.dto.request.ChatRequestDto;
import com.ssafy.passproject.domain.hotplace.dto.request.SummaryRequestDto;
import com.ssafy.passproject.domain.hotplace.dto.response.ChatResponseDto;
import com.ssafy.passproject.domain.hotplace.dto.response.HotplaceResponseDto;
import com.ssafy.passproject.domain.hotplace.dto.response.PagedHotplaceResponse;
import com.ssafy.passproject.domain.hotplace.dto.response.SummaryResponseDto;
import com.ssafy.passproject.domain.hotplace.entity.Hotplace;

public interface HotplaceService {
	public void insertHotplace(Hotplace hotplace) throws Exception;

	public Hotplace selectHotplaceDetail(Hotplace hotplace) throws Exception;

	public List<Hotplace> selectHotplaceList() throws Exception;

	public List<Hotplace> findByCategory(String category) throws Exception;

	public void updateHotplace(Hotplace hotplace) throws Exception;

	public void deleteHotplace(Hotplace hotplace) throws Exception;

	public Hotplace selectHotplaceDetailWithReviews(Long placeId) throws Exception;

	public void updateHotplaceAvgRating(Long placeId) throws Exception;

	public List<HotplaceResponseDto> findSpontaneousPlaces(double currentLat, double currentLon, int timeBudget)
			throws Exception;

	public PagedHotplaceResponse getNearbyHotplaces(double mapX, double mapY, int timeInMinutes,
			String transportMode, String category, int page, int size) throws Exception;

	public void fetchAndSaveTourData(String areaCode) throws Exception;

	public ChatResponseDto chat(ChatRequestDto request) throws Exception;

	public SummaryResponseDto generateSummary(SummaryRequestDto request) throws Exception;

	public CompletableFuture<SummaryResponseDto> generateSummaryAsync(SummaryRequestDto request);

	// 위치 기반 인기 Hotplace 조회 (count 기준 내림차순)
	public List<HotplaceResponseDto> getPopularHotplaces(double currentLat, double currentLon, int radiusMeters,
			int limit) throws Exception;
}
