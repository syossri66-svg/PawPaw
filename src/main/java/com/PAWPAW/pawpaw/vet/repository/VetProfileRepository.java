package com.PAWPAW.pawpaw.vet.repository;

import com.PAWPAW.pawpaw.vet.entity.VetProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VetProfileRepository extends JpaRepository<VetProfile, Long> {
    Optional<VetProfile> findByUserId(Long userId);
    List<VetProfile> findByIsApprovedTrue();
    List<VetProfile> findBySpecializationContainingIgnoreCase(String specialization);
}