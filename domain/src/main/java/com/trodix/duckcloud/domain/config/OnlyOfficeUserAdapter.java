package com.trodix.duckcloud.domain.config;

import com.trodix.duckcloud.security.services.AuthenticationService;
import com.trodix.duckcloud.security.services.KeycloakService;
import com.trodix.onlyoffice.models.UserRepresentation;
import com.trodix.onlyoffice.services.OnlyOfficeUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OnlyOfficeUserAdapter implements OnlyOfficeUserService {

    private final AuthenticationService authenticationService;
    private final KeycloakService keycloakService;


    @Override
    public String getUserId() {
        return authenticationService.getUserId();
    }

    @Override
    public String getName() {
        return authenticationService.getName();
    }

    @Override
    public UserRepresentation fetchUserProfile(String userId) {
        org.keycloak.representations.idm.UserRepresentation keycloakRep = keycloakService.fetchUserProfile(userId);

        UserRepresentation representation = new UserRepresentation();
        representation.setId(keycloakRep.getId());
        representation.setEmail(keycloakRep.getEmail());
        representation.setUsername(keycloakRep.getUsername());
        representation.setLastName(keycloakRep.getLastName());

        return representation;
    }

    @Override
    public String getDefaultUser() {
        return AuthenticationService.DEFAULT_USER;
    }

    @Override
    public String getDefaultEmail() {
        return AuthenticationService.DEFAULT_EMAIL;
    }

}
