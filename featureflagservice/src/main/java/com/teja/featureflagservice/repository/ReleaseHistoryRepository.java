package com.teja.featureflagservice.repository;

import com.teja.featureflagservice.entity.ReleaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseHistoryRepository extends JpaRepository<ReleaseHistory, Long> {
}
