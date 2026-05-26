package com.teja.featureflagservice.repository;

import com.teja.featureflagservice.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeatureRepository extends JpaRepository<FeatureFlag, Long> {

    List<FeatureFlag> findByEnvironment(String environment);

    Optional<FeatureFlag> findByFeatureNameAndEnvironment(String featureName, String environment);

    boolean existsByFeatureNameAndEnvironment(String featureName, String environment);
}
