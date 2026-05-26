package com.teja.featureflagservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FeatureNotFoundException extends RuntimeException {

    public FeatureNotFoundException(String featureName, String environment) {
        super("Feature '%s' was not found in %s environment".formatted(featureName, environment));
    }
}
