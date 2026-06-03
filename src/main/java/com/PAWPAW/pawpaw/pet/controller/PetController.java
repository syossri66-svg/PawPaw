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

    // ✅ تعديل الميثود لترجع الصيغة الخفيفة المطلوبة (id و name بس)
    @GetMapping
    public ResponseEntity<List<java.util.Map<String, Object>>> getMyPets() {
        return ResponseEntity.ok(petService.getMyPetsLight());
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

    // ✅ الـ Endpoint الخاص بـ تاريخ الوزن التاريخي للحيوان
    @GetMapping("/{id}/weight-history")
    public ResponseEntity<List<java.util.Map<String, Object>>> getPetWeightHistory(@PathVariable Long id) {
        List<java.util.Map<String, Object>> history = petService.getWeightHistory(id);
        return ResponseEntity.ok(history);
    }

    // ✅ الـ Endpoint الخاص بـ إحصائيات النشاط اليومي الـ Mocked مؤقتاً
    @GetMapping("/{id}/activity")
    public ResponseEntity<java.util.Map<String, Object>> getPetActivity(@PathVariable Long id) {
        java.util.Map<String, Object> activity = new java.util.HashMap<>();
        activity.put("steps", 10000);
        activity.put("water_liters", 1.5);
        activity.put("sleep_hours_restful", 8);
        activity.put("diet_type", "High-Protein Kibble");
        return ResponseEntity.ok(activity);
    }

    // ✅ الـ Endpoint الخاص بـ جدول الزيارات الطبية مفرمت بـ Snake Case ورابط التفاصيل
    @GetMapping("/{id}/medical-records")
    public ResponseEntity<List<java.util.Map<String, Object>>> getPetMedicalRecordsForFront(@PathVariable Long id) {
        List<java.util.Map<String, Object>> records = petService.getPetMedicalRecordsForFront(id);
        return ResponseEntity.ok(records);
    }
}