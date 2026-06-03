package com.PAWPAW.pawpaw.vet.repository;

import com.PAWPAW.pawpaw.vet.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByVetProfileId(Long vetProfileId);
    void deleteByVetProfileId(Long vetProfileId);
}