package com.PAWPAW.pawpaw.config;

import com.PAWPAW.pawpaw.appointment.entity.Appointment;
import com.PAWPAW.pawpaw.appointment.entity.AppointmentStatus;
import com.PAWPAW.pawpaw.appointment.repository.AppointmentRepository;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.entity.UserRole;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.medical.entity.MedicalRecord;
import com.PAWPAW.pawpaw.medical.repository.MedicalRecordRepository;
import com.PAWPAW.pawpaw.pet.entity.Pet;
import com.PAWPAW.pawpaw.pet.entity.PetGender;
import com.PAWPAW.pawpaw.pet.repository.PetRepository;
import com.PAWPAW.pawpaw.vet.entity.Certification;
import com.PAWPAW.pawpaw.vet.entity.VetProfile;
import com.PAWPAW.pawpaw.vet.repository.CertificationRepository;
import com.PAWPAW.pawpaw.vet.repository.VetProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VetProfileRepository vetProfileRepository;
    private final CertificationRepository certificationRepository;
    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail("wesal@pawpaw.com").isPresent()) return;

        // 1️⃣ Vet user
        User vet = User.builder()
                .fullName("wesal")
                .email("wesal@pawpaw.com")
                .password(passwordEncoder.encode("Wesal123@"))
                .role(UserRole.ROLE_VET)
                .isVerified(true)
                .build();
        userRepository.save(vet);

        // 2️⃣ Vet profile
        VetProfile vetProfile = VetProfile.builder()
                .user(vet)
                .clinicName("paw paw")
                .clinicAddress("Cairo, Egypt")
                .phoneNumber("01000000000")
                .specialization("General")
                .licenseNumber("VET-002")
                .bio("Experienced vet for over 10 years")
                .isApproved(true)
                .yearsOfExperience(10)
                .build();
        vetProfileRepository.save(vetProfile);

        // 3️⃣ Certifications
        certificationRepository.saveAll(List.of(
                Certification.builder()
                        .vetProfile(vetProfile)
                        .title("Doctor of Veterinary Medicine")
                        .status("VERIFIED")
                        .build(),
                Certification.builder()
                        .vetProfile(vetProfile)
                        .title("Board Certified Small Animal Surgeon")
                        .status("VERIFIED")
                        .build(),
                Certification.builder()
                        .vetProfile(vetProfile)
                        .title("Advanced Internal Medicine")
                        .status("VERIFIED")
                        .build()
        ));

        // 4️⃣ Pet owner
        User owner = User.builder()
                .fullName("Ahmed Owner")
                .email("ahmed@pawpaw.com")
                .password(passwordEncoder.encode("Ahmed123@"))
                .role(UserRole.ROLE_PET_OWNER)
                .isVerified(true)
                .build();
        userRepository.save(owner);

        // 5️⃣ Pet — Buddy
        Pet pet = Pet.builder()
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .dateOfBirth(LocalDate.of(2022, 1, 1))
                .gender(PetGender.MALE)
                .healthStatus("Healthy")
                .uniqueId("PWP-2021-0042")
                .weight(28.0)
                .vaccinated(true)
                .owner(owner)
                .build();
        petRepository.save(pet);

        // 6️⃣ Medical record
        MedicalRecord record = MedicalRecord.builder()
                .pet(pet)
                .vet(vet)
                .diagnosis("Annual Checkup — Healthy")
                .treatment("No treatment needed")
                .notes("Pet is in excellent health")
                .clinicName("paw paw")
                .weight(28.0)
                .visitDate(LocalDateTime.of(2025, 10, 15, 10, 0))
                .vaccinationStatus("Up To Date")
                .visitTitle("Annual Checkup")
                .hasAiReport(false)
                .build();
        medicalRecordRepository.save(record);

        // 7️⃣ Appointments
        appointmentRepository.saveAll(List.of(
                Appointment.builder()
                        .pet(pet)
                        .owner(owner)
                        .vet(vet)
                        .scheduledAt(LocalDateTime.of(2026, 6, 7, 10, 0))
                        .reason("Annual Vaccination")
                        .status(AppointmentStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build(),
                Appointment.builder()
                        .pet(pet)
                        .owner(owner)
                        .vet(vet)
                        .scheduledAt(LocalDateTime.of(2026, 6, 15, 14, 0))
                        .reason("Follow-up")
                        .status(AppointmentStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build()
        ));

        System.out.println("✅ DataSeeder: Done!");
    }
}