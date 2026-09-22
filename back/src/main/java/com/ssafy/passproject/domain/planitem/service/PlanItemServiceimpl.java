package com.ssafy.passproject.domain.planitem.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ssafy.passproject.domain.hotplace.entity.Hotplace;
import com.ssafy.passproject.domain.hotplace.service.HotplaceService;
import com.ssafy.passproject.domain.planitem.entity.PlanItem;
import com.ssafy.passproject.domain.planitem.repository.PlanItemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanItemServiceimpl implements PlanItemService {

	private final PlanItemRepository planItemRepository;
	private final HotplaceService hotplaceService;
	private final com.ssafy.passproject.domain.tour.service.TourApiService tourApiService;

	@Override
	public void insertPlanItem(PlanItem i) throws Exception {
		planItemRepository.insertPlanItem(i);
	}

	@Override
	public PlanItem selectPlanItem(Long planItemId) throws Exception {
		PlanItem item = planItemRepository.selectPlanItem(planItemId);
		enrichWithDescriptionAsync(item).join(); // 단건은 기다림
		return item;
	}

	@Override
	public List<PlanItem> selectPlanItems() throws Exception {
		List<PlanItem> items = planItemRepository.selectPlanItems();

		List<CompletableFuture<Void>> futures = items.stream()
				.map(this::enrichWithDescriptionAsync)
				.collect(Collectors.toList());

		// 모든 비동기 작업이 완료될 때까지 기다림 (병렬 처리)
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		return items;
	}

	@Override
	public void updatePlanItem(PlanItem i) throws Exception {
		planItemRepository.updatePlanItem(i);
	}

	@Override
	public void deletePlanItem(Long planItemId) throws Exception {
		planItemRepository.deletePlanItem(planItemId);
	}

	private CompletableFuture<Void> enrichWithDescriptionAsync(PlanItem item) {
		return CompletableFuture.runAsync(() -> {
			try {
				// 1. Fetch current Hotplace state to avoid race conditions and partial updates
				Hotplace hotplace = hotplaceService.selectHotplaceDetail(new Hotplace(item.getPlaceId()));
				if (hotplace == null) {
					log.warn("Hotplace not found for placeId {}", item.getPlaceId());
					return;
				}

				// 2. Check and Fetch Overview (from TourAPI)
				if (hotplace.getOverview() == null || hotplace.getOverview().isEmpty()) {
					try {
						log.info("Overview missing for placeId {}, fetching from TourAPI...", item.getPlaceId());
						com.ssafy.passproject.domain.tour.dto.request.TourDetailRequestDto request = com.ssafy.passproject.domain.tour.dto.request.TourDetailRequestDto
								.builder()
								.contentId(String.valueOf(item.getPlaceId()))
								.build();

						com.ssafy.passproject.domain.tour.dto.response.TourDetailResponseDto detail = tourApiService
								.getTourDetail(request);

						if (detail != null && detail.getOverview() != null) {
							hotplace.setOverview(detail.getOverview());
							item.setOverview(detail.getOverview());

							// IMMEDIATE UPDATE: Save overview so AI service can see it
							hotplaceService.updateHotplace(hotplace);
							log.info("Fetched and saved overview for placeId {}", item.getPlaceId());
						}
					} catch (Exception e) {
						log.warn("Failed to fetch overview from TourAPI for placeId {}", item.getPlaceId(), e);
					}
				} else {
					item.setOverview(hotplace.getOverview());
				}

				// AI Description 로직 제거 (description 필드가 없어졌으므로)
				// 프론트에서 실시간으로 AI 설명을 생성하므로 백엔드에 저장할 필요 없음

			} catch (Exception e) {
				log.error("Error during enrichment for planItem {}", item.getPlanItemId(), e);
			}
		});
	}
}
