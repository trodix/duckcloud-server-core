package com.trodix.duckcloud.security.services;

import com.trodix.duckcloud.security.KeycloakClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    @Value("${app.client.registration.keycloak-admin-uri}")
    private String keycloakAdminUri;

    private final KeycloakClientConfig config;

    public RestTemplate keycloakClient() {
        return new RestTemplateBuilder()
                .rootUri(keycloakAdminUri)
                .defaultHeader("Authorization", "Bearer " + config.getAccessToken())
                .build();
    }

    public UserRepresentation fetchUserProfile(String userId) throws RuntimeException {
        ResponseEntity<UserRepresentation> response = keycloakClient().getForEntity("/users/" + userId, UserRepresentation.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }

        throw new RuntimeException("Unable to fetch user representation for userId " + userId +". HTTP Code: " + response.getStatusCode().value());
    }

}
