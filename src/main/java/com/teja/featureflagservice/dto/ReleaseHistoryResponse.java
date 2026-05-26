package com.teja.featureflagservice.dto;

import com.teja.featureflagservice.entity.ReleaseHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReleaseHistoryResponse {

    private Long id;
    private String featureName;
    private Boolean oldStatus;
    private Boolean newStatus;
    private String environment;
    private LocalDateTime timestamp;

    public static ReleaseHistoryResponse fromEntity(ReleaseHistory releaseHistory) {
        return ReleaseHistoryResponse.builder()
                .id(releaseHistory.getId())
                .featureName(releaseHistory.getFeatureName())
                .oldStatus(releaseHistory.getOldStatus())
                .newStatus(releaseHistory.getNewStatus())
                .environment(releaseHistory.getEnvironment())
                .timestamp(releaseHistory.getTimestamp())
                .build();
    }
}
