package com.bank.medicineinformationsystembackend.repository;

import com.bank.medicineinformationsystembackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Пошук користувача за ID з Keycloak (знадобиться для авторизації)
    Optional<User> findByExternalId(String externalId);

    // Пошук за електронною поштою
    Optional<User> findByEmail(String email);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    Optional<User> findById(UUID id);
}