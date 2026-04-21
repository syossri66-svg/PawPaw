package com.PAWPAW.pawpaw.appointment.entity;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.pet.entity.Pet;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "vet_id", nullable = false)
    private User vet;

    private LocalDateTime scheduledAt;

    private String reason;

    private String notes;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        status = AppointmentStatus.PENDING;
    }
}