package com.PAWPAW.pawpaw.medical.repository;

import com.PAWPAW.pawpaw.medical.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPetIdOrderByVisitDateDesc(Long petId);
    List<MedicalRecord> findByVetId(Long vetId);
}