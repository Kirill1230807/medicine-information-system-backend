package com.bank.userservice.controller;

import com.bank.userservice.dto.user.*;
import com.bank.userservice.entity.User;
import com.bank.userservice.mapper.UserMapper;
import com.bank.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
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

// simple comment
    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(Authentication authentication) {
        String externalId = authentication.getName();
        String email = "unknown@medical.com";
        String firstName = null;
        String lastName = null;
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            email = jwt.getClaimAsString("email");
            firstName = jwt.getClaimAsString("given_name");
            lastName = jwt.getClaimAsString("family_name");
        } else if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            email = oidcUser.getEmail();
            firstName = oidcUser.getGivenName();
            lastName = oidcUser.getFamilyName();
        }

        User user = userService.syncOidcUser(externalId, email, firstName, lastName, resolveRole(authentication));

        return userMapper.toDto(user);
    }

    private String resolveRole(Authentication authentication) {
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        for (String role : List.of("ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_PATIENT")) {
            if (authorities.contains(role)) {
                return role;
            }
        }
        return "ROLE_USER";
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List getAllUsers() {
        return userService.getAllUsers().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO assignRole(@PathVariable UUID id, @RequestBody UserRoleUpdateDTO dto) {
        return userMapper.toDto(userService.assignRole(id, dto.role()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}