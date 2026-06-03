package com.PAWPAW.pawpaw.medical.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.medical.dto.*;
import com.PAWPAW.pawpaw.medical.entity.MedicalRecord;
import com.PAWPAW.pawpaw.medical.entity.Medication;
import com.PAWPAW.pawpaw.medical.repository.MedicalRecordRepository;
import com.PAWPAW.pawpaw.pet.entity.Pet;
import com.PAWPAW.pawpaw.pet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public MedicalRecordResponse addRecord(MedicalRecordRequest request) {
        User vet = getCurrentUser();
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        MedicalRecord record = MedicalRecord.builder()
                .pet(pet)
                .vet(vet)
                .diagnosis(request.getDiagnosis())
                .treatment(request.getTreatment())
                .notes(request.getNotes())
                .clinicName(request.getClinicName())
                .weight(request.getWeight())
                .visitDate(request.getVisitDate())
                .allergies(request.getAllergies())
                .vaccinationStatus(request.getVaccinationStatus())
                .nextVaccinationDate(request.getNextVaccinationDate())
                .nextVisitDate(request.getNextVisitDate())
                .rxNumber(request.getRxNumber())
                .clinicalNotes(request.getClinicalNotes())
                .hasAiReport(request.getHasAiReport())
                .visitTitle(request.getVisitTitle())
                .reportUrl(request.getReportUrl())
                .build();
        MedicalRecord saved = medicalRecordRepository.save(record);

        if (request.getMedications() != null) {
            request.getMedications().forEach(m -> {
                Medication med = Medication.builder()
                        .medicalRecord(saved)
                        .name(m.getName())
                        .strength(m.getStrength())
                        .instruction(m.getInstruction())
                        .duration(m.getDuration())
                        .build();
                saved.getMedications().add(med);
            });
            medicalRecordRepository.save(saved);
        }

        return mapToResponse(saved);
    }

    public List<MedicalRecordResponse> getPetRecords(Long petId) {
        return medicalRecordRepository.findByPetIdOrderByVisitDateDesc(petId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MedicalRecordResponse> getMyVetRecords() {
        User vet = getCurrentUser();
        return medicalRecordRepository.findByVetId(vet.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MedicalRecordResponse getRecordById(Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        return mapToResponse(record);
    }

    // ✅ GET /api/medical/pet/{petId}/full
    public PetMedicalFullResponse getPetFullMedical(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        List<MedicalRecordResponse> history = medicalRecordRepository
                .findByPetIdOrderByVisitDateDesc(petId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        PetMedicalFullResponse response = new PetMedicalFullResponse();
        response.setPetId(pet.getId());
        response.setPetName(pet.getName());
        response.setPetSpecies(pet.getSpecies());
        response.setPetBreed(pet.getBreed());
        response.setPetGender(pet.getGender() != null ? pet.getGender().toString() : null);
        response.setPetWeight(pet.getWeight());
        response.setPetPhotoUrl(pet.getPhotoUrl());
        response.setMedicalHistory(history);

        return response;
    }

    // ✅ PATCH /api/medical/record/{recordId}
    @Transactional
    public MedicalRecordResponse updateRecord(Long recordId, MedicalRecordRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        if (request.getDiagnosis() != null) record.setDiagnosis(request.getDiagnosis());
        if (request.getTreatment() != null) record.setTreatment(request.getTreatment());
        if (request.getNotes() != null) record.setNotes(request.getNotes());
        if (request.getClinicName() != null) record.setClinicName(request.getClinicName());
        if (request.getWeight() != null) record.setWeight(request.getWeight());
        if (request.getVisitDate() != null) record.setVisitDate(request.getVisitDate());
        if (request.getAllergies() != null) record.setAllergies(request.getAllergies());
        if (request.getVaccinationStatus() != null) record.setVaccinationStatus(request.getVaccinationStatus());
        if (request.getNextVaccinationDate() != null) record.setNextVaccinationDate(request.getNextVaccinationDate());
        if (request.getNextVisitDate() != null) record.setNextVisitDate(request.getNextVisitDate());
        if (request.getRxNumber() != null) record.setRxNumber(request.getRxNumber());
        if (request.getClinicalNotes() != null) record.setClinicalNotes(request.getClinicalNotes());
        if (request.getVisitTitle() != null) record.setVisitTitle(request.getVisitTitle());
        if (request.getPrescription() != null) record.setPrescription(request.getPrescription());
        if (request.getDosage() != null) record.setDosage(request.getDosage());
        if (request.getDuration() != null) record.setDuration(request.getDuration());
        if (request.getReportUrl() != null) record.setReportUrl(request.getReportUrl());

        if (request.getMedications() != null) {
            record.getMedications().clear();
            request.getMedications().forEach(m -> {
                Medication med = Medication.builder()
                        .medicalRecord(record)
                        .name(m.getName())
                        .strength(m.getStrength())
                        .instruction(m.getInstruction())
                        .duration(m.getDuration())
                        .build();
                record.getMedications().add(med);
            });
        }

        return mapToResponse(medicalRecordRepository.save(record));
    }

    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        MedicalRecordResponse response = new MedicalRecordResponse();
        response.setId(record.getId());
        response.setPetId(record.getPet().getId());
        response.setPetName(record.getPet().getName());
        response.setPetSpecies(record.getPet().getSpecies());
        response.setPetBreed(record.getPet().getBreed());
        response.setPetGender(record.getPet().getGender() != null ? record.getPet().getGender().toString() : null);
        response.setPetPhotoUrl(record.getPet().getPhotoUrl());
        response.setPetWeight(record.getWeight());
        response.setVetId(record.getVet().getId());
        response.setVetName(record.getVet().getFullName());
        response.setClinicName(record.getClinicName());
        response.setVisitDate(record.getVisitDate());
        response.setVisitTitle(record.getVisitTitle());
        response.setDiagnosis(record.getDiagnosis());
        response.setTreatment(record.getTreatment());
        response.setNotes(record.getNotes());
        response.setWeight(record.getWeight());
        response.setAllergies(record.getAllergies());
        response.setVaccinationStatus(record.getVaccinationStatus());
        response.setNextVaccinationDate(record.getNextVaccinationDate());
        response.setNextVisitDate(record.getNextVisitDate());
        response.setRxNumber(record.getRxNumber());
        response.setClinicalNotes(record.getClinicalNotes());
        response.setPrescription(record.getPrescription());
        response.setDosage(record.getDosage());
        response.setDuration(record.getDuration());
        response.setReportUrl(record.getReportUrl());
        response.setCreatedAt(record.getCreatedAt());
        response.setMedications(record.getMedications().stream()
                .map(m -> {
                    MedicationResponse med = new MedicationResponse();
                    med.setId(m.getId());
                    med.setName(m.getName());
                    med.setStrength(m.getStrength());
                    med.setInstruction(m.getInstruction());
                    med.setDuration(m.getDuration());
                    return med;
                }).collect(Collectors.toList()));
        return response;
    }
    public List<MedicalRecordTimelineResponse> getPetTimeline(Long petId) {
        return medicalRecordRepository.findByPetIdOrderByVisitDateDesc(petId)
                .stream()
                .map(record -> {
                    MedicalRecordTimelineResponse res = new MedicalRecordTimelineResponse();
                    res.setId(record.getId());
                    res.setVisitTitle(record.getVisitTitle());
                    res.setClinicName(record.getClinicName());
                    res.setVisitDate(record.getVisitDate());
                    return res;
                })
                .collect(Collectors.toList());
    }
}