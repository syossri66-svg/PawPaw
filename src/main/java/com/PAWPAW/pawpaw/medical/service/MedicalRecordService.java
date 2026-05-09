package com.PAWPAW.pawpaw.medical.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.medical.dto.MedicalRecordRequest;
import com.PAWPAW.pawpaw.medical.dto.MedicalRecordResponse;
import com.PAWPAW.pawpaw.medical.entity.MedicalRecord;
import com.PAWPAW.pawpaw.medical.repository.MedicalRecordRepository;
import com.PAWPAW.pawpaw.pet.entity.Pet;
import com.PAWPAW.pawpaw.pet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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
                .build();

        return mapToResponse(medicalRecordRepository.save(record));
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

    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        MedicalRecordResponse response = new MedicalRecordResponse();
        response.setId(record.getId());
        response.setPetId(record.getPet().getId());
        response.setPetName(record.getPet().getName());
        response.setVetId(record.getVet().getId());
        response.setVetName(record.getVet().getFullName());
        response.setDiagnosis(record.getDiagnosis());
        response.setTreatment(record.getTreatment());
        response.setNotes(record.getNotes());
        response.setClinicName(record.getClinicName());
        response.setWeight(record.getWeight());
        response.setVisitDate(record.getVisitDate());
        response.setCreatedAt(record.getCreatedAt());
        response.setAllergies(record.getAllergies());
        response.setPrescription(record.getPrescription());
        response.setDosage(record.getDosage());
        response.setDuration(record.getDuration());
        response.setReportUrl(record.getReportUrl());
// Pet snapshot
        response.setPetPhotoUrl(record.getPet().getPhotoUrl());
        response.setPetSpecies(record.getPet().getSpecies());
        response.setPetBreed(record.getPet().getBreed());
        response.setPetWeight(record.getWeight());
        return response;
    }

    public MedicalRecordResponse getRecordById(Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        return mapToResponse(record);
    }
}