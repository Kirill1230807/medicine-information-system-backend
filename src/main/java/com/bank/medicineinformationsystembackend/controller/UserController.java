package com.bank.medicineinformationsystembackend.controller;

import com.bank.medicineinformationsystembackend.dto.user.UserResponseDTO;
import com.bank.medicineinformationsystembackend.entity.User;
import com.bank.medicineinformationsystembackend.mapper.UserMapper;
import com.bank.medicineinformationsystembackend.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }


    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(Authentication authentication) {
        String externalId = authentication.getName();
        String email = "unknown@medical.com";
        String role = "ROLE_USER";

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            email = jwt.getClaimAsString("email");
        } else if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            email = oidcUser.getEmail();
        }

        User user = userService.syncOidcUser(externalId, email, role);

        return userMapper.toDto(user);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List getAllUsers() {
        return userService.getAllUsers().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}