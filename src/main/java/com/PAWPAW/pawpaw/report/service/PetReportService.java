package com.PAWPAW.pawpaw.report.service;

import com.PAWPAW.pawpaw.medical.repository.MedicalRecordRepository;
import com.PAWPAW.pawpaw.pet.entity.Pet;
import com.PAWPAW.pawpaw.pet.repository.PetRepository;
import com.PAWPAW.pawpaw.report.dto.PetReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetReportService {

    private final PetRepository petRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public PetReportResponse generatePetReport(Long petId) {

        // ── 1. جيب الـ Pet من الـ DB ──────────────────────────────────────
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + petId));

        // ── 2. احسب العمر من dateOfBirth ──────────────────────────────────
        String age = "Unknown";
        if (pet.getDateOfBirth() != null) {
            Period period = Period.between(pet.getDateOfBirth(), LocalDate.now());
            if (period.getYears() > 0) {
                age = period.getYears() + (period.getYears() == 1 ? " year" : " years");
            } else if (period.getMonths() > 0) {
                age = period.getMonths() + (period.getMonths() == 1 ? " month" : " months");
            } else {
                age = period.getDays() + " days";
            }
        }

        // ── 3. جيب آخر سجل طبي للوزن والتشخيص ───────────────────────────
        var latestRecord = medicalRecordRepository
                .findByPetIdOrderByVisitDateDesc(petId)
                .stream()
                .findFirst();

        Double weight = latestRecord
                .map(r -> r.getWeight() != null ? r.getWeight() : pet.getWeight())
                .orElse(pet.getWeight());

        String lastDiagnosis = latestRecord
                .map(r -> r.getDiagnosis())
                .orElse(null);

        String vetName = latestRecord
                .map(r -> r.getVet() != null ? r.getVet().getFullName() : null)
                .orElse(null);

        // ── 4. بناء petData ───────────────────────────────────────────────
        PetReportResponse.PetDataDto petData = PetReportResponse.PetDataDto.builder()
                .name(pet.getName())
                .age(age)
                .breed(pet.getBreed() != null ? pet.getBreed() : "Unknown")
                .weight(weight != null ? weight + " kg" : "N/A")
                .build();

        // ── 5. بناء ownerData ─────────────────────────────────────────────
        var owner = pet.getOwner();
        PetReportResponse.OwnerDataDto ownerData = PetReportResponse.OwnerDataDto.builder()
                .name(owner.getFullName() != null ? owner.getFullName() : "Unknown")
                .id(owner.getId().toString())
                .contact(owner.getPhone() != null ? owner.getPhone() : owner.getEmail())
                .build();

        // ── 6. بناء الـ AI Summary ────────────────────────────────────────
        String summary = buildAiSummary(pet, weight, lastDiagnosis, vetName);

        // ── 7. بناء الـ Vitamins بناءً على حالة الحيوان ───────────────────
        List<PetReportResponse.VitaminDto> vitamins = List.of(
                PetReportResponse.VitaminDto.builder()
                        .id("b12")
                        .label("B12")
                        .dose("1 times/day")
                        .selected(true)
                        .build(),
                PetReportResponse.VitaminDto.builder()
                        .id("supp")
                        .label("Supplement")
                        .dose("4 times/day")
                        .selected(pet.getHealthStatus() != null &&
                                !pet.getHealthStatus().equalsIgnoreCase("Healthy"))
                        .build()
        );

        return PetReportResponse.builder()
                .petData(petData)
                .ownerData(ownerData)
                .aiSummary(summary)
                .vitamins(vitamins)
                .build();
    }

    // ── Helper: بيبني الـ Summary ديناميكياً من بيانات الـ Pet ────────────
    private String buildAiSummary(Pet pet, Double weight, String lastDiagnosis, String vetName) {
        StringBuilder sb = new StringBuilder();

        sb.append(pet.getName() != null ? pet.getName() : "Your pet");

        if ("Healthy".equalsIgnoreCase(pet.getHealthStatus())) {
            sb.append(" is in good health.");
        } else if (pet.getHealthStatus() != null) {
            sb.append("'s current health status is: ").append(pet.getHealthStatus()).append(".");
        } else {
            sb.append(" has been examined.");
        }

        if (weight != null) {
            sb.append(" Current weight: ").append(weight).append(" kg.");
        }

        if (lastDiagnosis != null && !lastDiagnosis.isBlank()) {
            sb.append(" Latest diagnosis: ").append(lastDiagnosis).append(".");
        }

        if (vetName != null) {
            sb.append(" Attended by Dr. ").append(vetName).append(".");
        }

        if (pet.getVaccinated() != null && pet.getVaccinated()) {
            sb.append(" Vaccinations are up to date.");
        } else {
            sb.append(" Please check vaccination schedule.");
        }

        return sb.toString();
    }
}