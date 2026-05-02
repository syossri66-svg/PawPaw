package com.PAWPAW.pawpaw.vet.entity;

import com.PAWPAW.pawpaw.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vet_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String clinicName;

    private String clinicAddress;

    private String phoneNumber;

    private String specialization;

    private String licenseNumber;

    private boolean isApproved = false;

    private String bio;

    private Double latitude;

    private Double longitude;

    private String rejectionReason;



    private Integer yearsOfExperience;
    private String education;
    private String documentUrl;
    private String profileImage;

}