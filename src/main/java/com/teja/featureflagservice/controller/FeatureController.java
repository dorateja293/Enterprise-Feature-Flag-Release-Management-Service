package com.teja.featureflagservice.controller;

import com.teja.featureflagservice.dto.FeatureRequest;
import com.teja.featureflagservice.dto.FeatureResponse;
import com.teja.featureflagservice.dto.ReleaseHistoryResponse;
import com.teja.featureflagservice.dto.ToggleRequest;
import com.teja.featureflagservice.service.FeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Feature Flags", description = "Feature flag and release management APIs")
public class FeatureController {

    private final FeatureService featureService;

    @PostMapping("/feature")
    @Operation(summary = "Create a feature flag")
    public ResponseEntity<FeatureResponse> createFeature(@Valid @RequestBody FeatureRequest request) {
        FeatureResponse response = featureService.createFeature(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/features/{environment}")
    @Operation(summary = "Get feature flags by environment")
    public ResponseEntity<List<FeatureResponse>> getFeaturesByEnvironment(@PathVariable String environment) {
        return ResponseEntity.ok(featureService.getFeaturesByEnvironment(environment));
    }

    @PutMapping("/toggle-feature")
    @Operation(summary = "Toggle a feature flag")
    public ResponseEntity<FeatureResponse> toggleFeature(@Valid @RequestBody ToggleRequest request) {
        return ResponseEntity.ok(featureService.toggleFeature(request));
    }

    @GetMapping("/release-history")
    @Operation(summary = "Get release history")
    public ResponseEntity<List<ReleaseHistoryResponse>> getReleaseHistory() {
        return ResponseEntity.ok(featureService.getReleaseHistory());
    }
}
