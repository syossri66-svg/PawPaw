package com.PAWPAW.pawpaw.vet.repository;

import com.PAWPAW.pawpaw.vet.entity.VetProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface VetProfileRepository extends JpaRepository<VetProfile, UUID> {



    @Query("SELECT v FROM VetProfile v WHERE v.user.id = :userId")
    Optional<VetProfile> findByCustomUserId(@Param("userId") Long userId);
    List<VetProfile> findByIsApprovedTrue();

    List<VetProfile> findBySpecializationContainingIgnoreCase(String specialization);
    long countByIsApprovedTrue();
    long countByIsApprovedFalse();
    List<VetProfile> findByIsApprovedFalse();

    @Query("SELECT v FROM VetProfile v WHERE v.user.fullName LIKE %:keyword% OR v.licenseNumber LIKE %:keyword%")
    List<VetProfile> searchVets(@Param("keyword") String keyword);
}