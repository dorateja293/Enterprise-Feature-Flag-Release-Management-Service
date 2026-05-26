package com.teja.featureflagservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateFeatureException extends RuntimeException {

    public DuplicateFeatureException(String featureName, String environment) {
        super("Feature '%s' already exists in %s environment".formatted(featureName, environment));
    }
}
