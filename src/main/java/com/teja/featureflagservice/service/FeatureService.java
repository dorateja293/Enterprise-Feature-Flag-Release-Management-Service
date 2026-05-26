package com.teja.featureflagservice.service;

import com.teja.featureflagservice.dto.FeatureRequest;
import com.teja.featureflagservice.dto.FeatureResponse;
import com.teja.featureflagservice.dto.ReleaseHistoryResponse;
import com.teja.featureflagservice.dto.ToggleRequest;
import com.teja.featureflagservice.entity.FeatureFlag;
import com.teja.featureflagservice.entity.ReleaseHistory;
import com.teja.featureflagservice.exception.DuplicateFeatureException;
import com.teja.featureflagservice.exception.FeatureNotFoundException;
import com.teja.featureflagservice.repository.FeatureRepository;
import com.teja.featureflagservice.repository.ReleaseHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FeatureService {

    private static final Set<String> SUPPORTED_ENVIRONMENTS = Set.of("DEV", "QA", "PRODUCTION");

    private final FeatureRepository featureRepository;
    private final ReleaseHistoryRepository releaseHistoryRepository;

    @Transactional
    public FeatureResponse createFeature(FeatureRequest request) {
        String featureName = normalizeFeatureName(request.getFeatureName());
        String environment = normalizeEnvironment(request.getEnvironment());

        if (featureRepository.existsByFeatureNameAndEnvironment(featureName, environment)) {
            throw new DuplicateFeatureException(featureName, environment);
        }

        FeatureFlag featureFlag = FeatureFlag.builder()
                .featureName(featureName)
                .environment(environment)
                .enabled(request.getEnabled())
                .build();

        return FeatureResponse.fromEntity(featureRepository.save(featureFlag));
    }

    @Transactional(readOnly = true)
    public List<FeatureResponse> getFeaturesByEnvironment(String environment) {
        String normalizedEnvironment = normalizeEnvironment(environment);

        return featureRepository.findByEnvironment(normalizedEnvironment)
                .stream()
                .map(FeatureResponse::fromEntity)
                .toList();
    }

    @Transactional
    public FeatureResponse toggleFeature(ToggleRequest request) {
        String featureName = normalizeFeatureName(request.getFeatureName());
        String environment = normalizeEnvironment(request.getEnvironment());

        FeatureFlag featureFlag = featureRepository.findByFeatureNameAndEnvironment(featureName, environment)
                .orElseThrow(() -> new FeatureNotFoundException(featureName, environment));

        Boolean oldStatus = featureFlag.getEnabled();
        Boolean newStatus = request.getEnabled();

        featureFlag.setEnabled(newStatus);
        FeatureFlag updatedFeatureFlag = featureRepository.save(featureFlag);

        ReleaseHistory releaseHistory = ReleaseHistory.builder()
                .featureName(featureName)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .environment(environment)
                .build();
        releaseHistoryRepository.save(releaseHistory);

        return FeatureResponse.fromEntity(updatedFeatureFlag);
    }

    @Transactional(readOnly = true)
    public List<ReleaseHistoryResponse> getReleaseHistory() {
        return releaseHistoryRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(ReleaseHistoryResponse::fromEntity)
                .toList();
    }

    private String normalizeFeatureName(String featureName) {
        return featureName.trim().toLowerCase();
    }

    private String normalizeEnvironment(String environment) {
        String normalizedEnvironment = environment.trim().toUpperCase();

        if (!SUPPORTED_ENVIRONMENTS.contains(normalizedEnvironment)) {
            throw new IllegalArgumentException("environment must be DEV, QA, or PRODUCTION");
        }

        return normalizedEnvironment;
    }
}
