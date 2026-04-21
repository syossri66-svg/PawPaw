package com.PAWPAW.pawpaw.appointment.repository;

import com.PAWPAW.pawpaw.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByOwnerId(Long ownerId);
    List<Appointment> findByVetId(Long vetId);
    List<Appointment> findByPetId(Long petId);
}