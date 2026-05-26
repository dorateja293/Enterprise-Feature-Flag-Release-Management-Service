package com.teja.featureflagservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureRequest {

    @NotBlank(message = "featureName is required")
    @Size(max = 120, message = "featureName must not exceed 120 characters")
    private String featureName;

    @NotBlank(message = "environment is required")
    @Pattern(regexp = "DEV|QA|PRODUCTION", message = "environment must be DEV, QA, or PRODUCTION")
    private String environment;

    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
