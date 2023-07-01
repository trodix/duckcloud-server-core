package com.trodix.duckcloud.security.services;

import com.trodix.duckcloud.security.KeycloakClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.keycloak.Config;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

    public void createClientScope(ClientScopeRepresentation clientScopeRepresentation) {
        ResponseEntity<Void> response = keycloakClient().postForEntity("/client-scopes", clientScopeRepresentation, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Unable to create scope. HTTP Code: " + response.getStatusCode().value());
        }
    }

    public void deleteClientScope(String scopeId) {
        try {
            keycloakClient().delete("/client-scopes/" + scopeId);
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete scope. Error: " + e.getMessage());
        }
    }

    public ClientScopeRepresentation fetchClientScopes() {
        ResponseEntity<ClientScopeRepresentation> response = keycloakClient().getForEntity("/client-scopes", ClientScopeRepresentation.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }

        throw new RuntimeException("Unable to fetch client scopes. HTTP Code: " + response.getStatusCode().value());
    }

    public List<UserRepresentation> fetchUsers() {
        throw new NotImplementedException("Unable to fetch users");
    }
}
