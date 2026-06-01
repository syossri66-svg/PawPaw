package com.PAWPAW.pawpaw.ai.repository;

import com.PAWPAW.pawpaw.ai.entity.AiScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AiScanRepository extends JpaRepository<AiScan, Long> {

    List<AiScan> findByUserId(Long userId);
}