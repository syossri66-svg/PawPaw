package com.PAWPAW.pawpaw.appointment.repository;

import com.PAWPAW.pawpaw.appointment.entity.Appointment;
import com.PAWPAW.pawpaw.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;


@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByOwnerId(Long ownerId);
    List<Appointment> findByVetId(Long vetId);
    List<Appointment> findByPetId(Long petId);
    long countByVetId(Long vetId);
    long countByVetIdAndStatus(Long vetId, AppointmentStatus status);
    List<Appointment> findByVetIdAndStatusOrderByScheduledAtAsc(Long vetId, AppointmentStatus status);

    @Query("SELECT COUNT(DISTINCT a.pet.owner.id) FROM Appointment a WHERE a.vet.id = :vetId AND a.createdAt >= :startOfMonth")
    long countNewPatientsThisMonth(@Param("vetId") Long vetId, @Param("startOfMonth") LocalDateTime startOfMonth);
}