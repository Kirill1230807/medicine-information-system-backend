package com.bank.medicineinformationsystembackend.service;

import com.bank.medicineinformationsystembackend.entity.User;
import com.bank.medicineinformationsystembackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Користувача з ID " + id + " не знайдено"));
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Користувач з email " + user.getEmail() + " вже існує");
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Користувача з ID " + id + " не знайдено");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional getUserByExternalId(String externalId) {
        return userRepository.findByExternalId(externalId);
    }

    @Transactional(readOnly = true)
    public Optional getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User syncOidcUser(String externalId, String email, String role) {
        return userRepository.findByExternalId(externalId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setExternalId(externalId);
                    newUser.setEmail(email);
                    newUser.setRole(role);
                    return userRepository.save(newUser);
                });
    }
}