package com.teja.featureflagservice.dto;

import com.teja.featureflagservice.entity.FeatureFlag;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FeatureResponse {

    private Long id;
    private String featureName;
    private String environment;
    private Boolean enabled;
    private LocalDateTime createdAt;

    public static FeatureResponse fromEntity(FeatureFlag featureFlag) {
        return FeatureResponse.builder()
                .id(featureFlag.getId())
                .featureName(featureFlag.getFeatureName())
                .environment(featureFlag.getEnvironment())
                .enabled(featureFlag.getEnabled())
                .createdAt(featureFlag.getCreatedAt())
                .build();
    }
}
