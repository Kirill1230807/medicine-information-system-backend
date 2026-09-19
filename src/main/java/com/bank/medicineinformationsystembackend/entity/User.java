package com.bank.medicineinformationsystembackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String role; // Наприклад: "ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_PATIENT"

    @Column(name = "external_id", unique = true)
    private String externalId;
}