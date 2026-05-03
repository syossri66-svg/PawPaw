package com.PAWPAW.pawpaw.medical.entity;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.pet.entity.Pet;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "vet_id", nullable = false)
    private User vet;

    private String diagnosis;
    private String treatment;
    private String notes;
    private String clinicName;
    private Double weight;
    private LocalDateTime visitDate;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (visitDate == null) visitDate = LocalDateTime.now();
    }
}