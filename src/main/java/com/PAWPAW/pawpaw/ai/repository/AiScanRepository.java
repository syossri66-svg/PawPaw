package com.PAWPAW.pawpaw.ai.repository;

import com.PAWPAW.pawpaw.ai.entity.AiScan;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AiScanRepository extends ReactiveCrudRepository<AiScan, Long> {

    Flux<AiScan> findByUserId(Long userId);
}