package com.bank.medicineinformationsystembackend.service;

import com.bank.medicineinformationsystembackend.entity.User;
import com.bank.medicineinformationsystembackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Знаходить у БД користувача, що відповідає поточному токену/сесії (за "sub" з Keycloak).
     * Зазвичай рядок уже існує, бо створюється при вході через GET /api/users/me.
     */
    public User require(Authentication authentication) {
        String externalId = authentication.getName();
        return userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Користувача не знайдено. Увійдіть в застосунок ще раз."));
    }
}