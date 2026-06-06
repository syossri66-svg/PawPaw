package com.PAWPAW.pawpaw.pet.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.entity.UserRole;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.medical.repository.MedicalRecordRepository;
import com.PAWPAW.pawpaw.pet.dto.PetRequest;
import com.PAWPAW.pawpaw.pet.dto.PetResponse;
import com.PAWPAW.pawpaw.pet.entity.Pet;
import com.PAWPAW.pawpaw.pet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public PetResponse addPet(PetRequest request) {
        User owner = getCurrentUser();

        if (owner.getRole() == UserRole.ROLE_VET) {
            throw new RuntimeException("Vets cannot add pets");
        }

        Pet pet = Pet.builder()
                .name(request.getName())
                .species(request.getSpecies())
                .breed(request.getBreed())
                .dateOfBirth(request.getDateOfBirth())
                .photoUrl(request.getPhotoUrl())
                .medicalNotes(request.getMedicalNotes())
                .gender(request.getGender())
                .healthStatus(request.getHealthStatus())
                .uniqueId(request.getUniqueId())
                .vaccinated(request.getVaccinated())
                .weight(request.getWeight())
                .owner(owner)
                .build();

        Pet saved = petRepository.save(pet);
        return mapToResponse(saved);
    }

    public List<PetResponse> getMyPets() {
        User owner = getCurrentUser();
        return petRepository.findByOwnerId(owner.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ بترجع كل الـ fields اللي الفرونت محتاجها (مش id و name بس)
    public List<java.util.Map<String, Object>> getMyPetsLight() {
        User owner = getCurrentUser();
        return petRepository.findByOwnerId(owner.getId())
                .stream()
                .map(pet -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", pet.getId());
                    map.put("name", pet.getName());
                    map.put("species", pet.getSpecies());
                    map.put("breed", pet.getBreed());
                    map.put("dateOfBirth", pet.getDateOfBirth());
                    map.put("photoUrl", pet.getPhotoUrl());
                    map.put("gender", pet.getGender());
                    map.put("healthStatus", pet.getHealthStatus());
                    map.put("uniqueId", pet.getUniqueId());
                    map.put("medicalNotes", pet.getMedicalNotes());

                    // weight و vaccinated: بناخدهم من آخر سجل طبي لو موجود
                    // وإلا بناخدهم من الـ entity نفسه
                    var latestRecord = medicalRecordRepository
                            .findByPetIdOrderByVisitDateDesc(pet.getId())
                            .stream()
                            .findFirst();

                    if (latestRecord.isPresent()) {
                        var record = latestRecord.get();
                        map.put("weight", record.getWeight() != null ? record.getWeight() : pet.getWeight());
                        map.put("vaccinated",
                                record.getVaccinationStatus() != null &&
                                        record.getVaccinationStatus().equalsIgnoreCase("Up To Date"));
                        map.put("vetName", record.getVet() != null ? record.getVet().getFullName() : null);
                        map.put("lastDiagnosis", record.getDiagnosis());
                        map.put("lastVisitDate", record.getVisitDate());
                    } else {
                        map.put("weight", pet.getWeight());
                        map.put("vaccinated", pet.getVaccinated());
                        map.put("vetName", null);
                        map.put("lastDiagnosis", null);
                        map.put("lastVisitDate", null);
                    }

                    return map;
                })
                .collect(Collectors.toList());
    }

    public PetResponse getPetById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found"));
        return mapToResponse(pet);
    }

    public PetResponse updatePet(Long id, PetRequest request) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setDateOfBirth(request.getDateOfBirth());
        pet.setPhotoUrl(request.getPhotoUrl());
        pet.setMedicalNotes(request.getMedicalNotes());
        pet.setGender(request.getGender());
        pet.setHealthStatus(request.getHealthStatus());
        pet.setUniqueId(request.getUniqueId());
        pet.setVaccinated(request.getVaccinated());
        pet.setWeight(request.getWeight());

        return mapToResponse(petRepository.save(pet));
    }

    public void deletePet(Long id) {
        petRepository.deleteById(id);
    }

    private PetResponse mapToResponse(Pet pet) {
        PetResponse response = new PetResponse();
        response.setId(pet.getId());
        response.setName(pet.getName());
        response.setSpecies(pet.getSpecies());
        response.setBreed(pet.getBreed());
        response.setDateOfBirth(pet.getDateOfBirth());
        response.setPhotoUrl(pet.getPhotoUrl());
        response.setMedicalNotes(pet.getMedicalNotes());
        response.setGender(pet.getGender());
        response.setOwnerId(pet.getOwner().getId());
        response.setOwnerName(pet.getOwner().getFullName());
        response.setCreatedAt(pet.getCreatedAt());
        response.setHealthStatus(pet.getHealthStatus());
        response.setUniqueId(pet.getUniqueId());

        // ✅ بياخد weight و vaccinated من آخر سجل طبي لو موجود
        medicalRecordRepository
                .findByPetIdOrderByVisitDateDesc(pet.getId())
                .stream()
                .findFirst()
                .ifPresentOrElse(
                        record -> {
                            response.setVetName(record.getVet() != null ? record.getVet().getFullName() : null);
                            response.setLastDiagnosis(record.getDiagnosis());
                            response.setLastVisitDate(record.getVisitDate());
                            response.setWeight(record.getWeight() != null ? record.getWeight() : pet.getWeight());
                            response.setVaccinated(
                                    record.getVaccinationStatus() != null &&
                                            record.getVaccinationStatus().equalsIgnoreCase("Up To Date")
                            );
                        },
                        () -> {
                            // لو مفيش سجل طبي → بياخد من الـ entity مباشرة
                            response.setWeight(pet.getWeight());
                            response.setVaccinated(pet.getVaccinated());
                        }
                );

        return response;
    }

    // ✅ تاريخ الوزن
    public List<java.util.Map<String, Object>> getWeightHistory(Long petId) {
        return medicalRecordRepository.findByPetIdOrderByVisitDateDesc(petId)
                .stream()
                .filter(record -> record.getWeight() != null && record.getVisitDate() != null)
                .map(record -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    String monthName = record.getVisitDate().getMonth().getDisplayName(
                            java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH);
                    map.put("month", monthName);
                    map.put("weight", record.getWeight());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ✅ سجل الزيارات الطبية
    public List<java.util.Map<String, Object>> getPetMedicalRecordsForFront(Long petId) {
        return medicalRecordRepository.findByPetIdOrderByVisitDateDesc(petId)
                .stream()
                .map(record -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", record.getId());
                    map.put("vet_name", record.getVet() != null ? record.getVet().getFullName() : "Unknown Vet");
                    map.put("date", record.getVisitDate());
                    map.put("diagnosis_link", "/api/medical/" + record.getId());
                    map.put("record_link", "/api/medical/" + record.getId());
                    return map;
                })
                .collect(Collectors.toList());
    }
}