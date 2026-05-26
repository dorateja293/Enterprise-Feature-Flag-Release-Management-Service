package com.teja.featureflagservice.repository;

import com.teja.featureflagservice.entity.ReleaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReleaseHistoryRepository extends JpaRepository<ReleaseHistory, Long> {

    List<ReleaseHistory> findAllByOrderByTimestampDesc();
}
