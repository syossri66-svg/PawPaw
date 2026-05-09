package com.PAWPAW.pawpaw.pet.service;

import com.PAWPAW.pawpaw.auth.entity.User;
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

        Pet pet = Pet.builder()
                .name(request.getName())
                .species(request.getSpecies())
                .breed(request.getBreed())
                .dateOfBirth(request.getDateOfBirth())
                .photoUrl(request.getPhotoUrl())
                .medicalNotes(request.getMedicalNotes())
                .gender(request.getGender())
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

        medicalRecordRepository
                .findByPetIdOrderByVisitDateDesc(pet.getId())
                .stream()
                .findFirst()
                .ifPresent(record -> {
                    response.setVetName(record.getVet().getFullName());
                    response.setLastDiagnosis(record.getDiagnosis());
                    response.setLastVisitDate(record.getVisitDate());
                });
        return response;
    }
}