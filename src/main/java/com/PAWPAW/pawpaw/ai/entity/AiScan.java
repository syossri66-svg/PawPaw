package com.PAWPAW.pawpaw.ai.entity;

import lombok.*;
import jakarta.persistence.*; // أو javax.persistence لو شغالين سبرينج بوت قديم
import java.time.LocalDateTime;

@Entity // غيرناها من @Table لـ @Entity
@Table(name = "ai_scans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = true)
    private Long petId;
    private String imageUrl;
    private String status;
    private String breedDetected;
    private boolean hasIssue;
    private String issueName;
    private double confidence;

    @Column(columnDefinition = "TEXT")
    private String treatmentTip;

    private LocalDateTime scanDate;
}