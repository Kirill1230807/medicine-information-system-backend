package com.bank.medicineinformationsystembackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Клієнт Admin REST API Keycloak. Працює від імені service account клієнта "medical-backend"
 * (client_credentials), тому не залежить від того, хто саме з адмінів викликав операцію.
 */
@Service
public class KeycloakAdminService {

    /** Ролі застосунку, якими керує адміністратор */
    private static final Set<String> APP_ROLES = Set.of("DOCTOR", "PATIENT");

    private final RestClient restClient = RestClient.create();

    private final String serverUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    public KeycloakAdminService(
            @Value("${keycloak.admin.server-url:http://localhost:8081}") String serverUrl,
            @Value("${keycloak.admin.realm:medical-realm}") String realm,
            @Value("${keycloak.admin.client-id:medical-backend}") String clientId,
            @Value("${keycloak.admin.client-secret:medical-backend-dev-secret}") String clientSecret) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Робить роль (DOCTOR або PATIENT) єдиною роллю застосунку для користувача:
     * попередні ролі DOCTOR/PATIENT знімаються, нова призначається.
     */
    public void assignRealmRole(String keycloakUserId, String roleName) {
        try {
            String token = fetchAdminToken();
            String adminBase = serverUrl + "/admin/realms/" + realm;
            String mappingUrl = adminBase + "/users/{id}/role-mappings/realm";

            Map<String, Object> newRole = restClient.get()
                    .uri(adminBase + "/roles/{name}", roleName)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            List<Map<String, Object>> currentRoles = restClient.get()
                    .uri(mappingUrl, keycloakUserId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            List<Map<String, Object>> toRemove = (currentRoles == null ? List.<Map<String, Object>>of() : currentRoles)
                    .stream()
                    .filter(r -> APP_ROLES.contains(String.valueOf(r.get("name"))))
                    .toList();

            if (!toRemove.isEmpty()) {
                restClient.method(HttpMethod.DELETE)
                        .uri(mappingUrl, keycloakUserId)
                        .headers(h -> h.setBearerAuth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(toRemove)
                        .retrieve()
                        .toBodilessEntity();
            }

            restClient.post()
                    .uri(mappingUrl, keycloakUserId)
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(newRole))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Не вдалося змінити роль у Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Видаляє користувача з Keycloak повністю (акаунт, паролі, ролі).
     * Викликається ПІСЛЯ успішного видалення пов'язаних даних у БД застосунку.
     */
    public void deleteUser(String keycloakUserId) {
        try {
            String token = fetchAdminToken();
            restClient.method(HttpMethod.DELETE)
                    .uri(serverUrl + "/admin/realms/" + realm + "/users/{id}", keycloakUserId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Не вдалося видалити користувача в Keycloak: " + e.getMessage(), e);
        }
    }

    private String fetchAdminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        Map<String, Object> response = restClient.post()
                .uri(serverUrl + "/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response == null || response.get("access_token") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Keycloak не повернув токен для service account");
        }
        return String.valueOf(response.get("access_token"));
    }
}