package com.PAWPAW.pawpaw.medical.entity;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.pet.entity.Pet;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

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
    // ضيفي الـ fields دي جوا الـ entity
    @ElementCollection
    @CollectionTable(name = "pet_allergies", joinColumns = @JoinColumn(name = "record_id"))
    @Column(name = "allergy")
    private List<String> allergies;

    private String prescription;
    private String dosage;
    private String duration;
    private String reportUrl;
}