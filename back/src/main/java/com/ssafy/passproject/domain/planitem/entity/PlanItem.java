package com.ssafy.passproject.domain.planitem.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanItem {
	private Long planItemId;
	private Long planId;
	private Long placeId;
	private int sequenceOrder;
	private String transportMode;
	private int estimatedDuration;

	// Hotplace info (Joined)
	private String title;
	private String overview;
	private String description;
	private String firstImage1;
	private Double latitude;
	private Double longitude;
}
