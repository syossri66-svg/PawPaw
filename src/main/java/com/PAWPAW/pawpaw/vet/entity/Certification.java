package com.PAWPAW.pawpaw.vet.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "certifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vet_profile_id", nullable = false)
    private VetProfile vetProfile;

    private String title;

    private String status; // e.g. "VERIFIED", "PENDING"

    private String imageUrl;
}