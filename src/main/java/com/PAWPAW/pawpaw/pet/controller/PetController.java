package com.PAWPAW.pawpaw.pet.controller;

import com.PAWPAW.pawpaw.pet.dto.PetRequest;
import com.PAWPAW.pawpaw.pet.dto.PetResponse;
import com.PAWPAW.pawpaw.pet.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PetController {

    private final PetService petService;

    @PostMapping
    public ResponseEntity<PetResponse> addPet(@Valid @RequestBody PetRequest request) {
        return ResponseEntity.ok(petService.addPet(request));
    }

    @GetMapping
    public ResponseEntity<List<PetResponse>> getMyPets() {
        return ResponseEntity.ok(petService.getMyPets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.getPetById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> updatePet(@PathVariable Long id,
                                                 @Valid @RequestBody PetRequest request) {
        return ResponseEntity.ok(petService.updatePet(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return ResponseEntity.noContent().build();
    }

}
