package com.trodix.duckcloud.security.bootstrap;

import com.trodix.duckcloud.security.services.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootstrapOidcClientScopes implements ApplicationListener<ApplicationReadyEvent> {

    private final KeycloakService keycloakService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
//        log.debug("BootstrapOidcClientScopes event triggered");
//        registerOidcClientScopes();
    }

    /**
     * Create client scope for every user found in Keycloak
     */
    private void registerOidcClientScopes() {
        log.info("Registering client scopes...");

        List<UserRepresentation> users = keycloakService.fetchUsers();
        log.info(users.size() + " users found");

        for (UserRepresentation user : users) {
            processUser(user);
        }

        log.info("All users have been processed");
    }

    private void processUser(UserRepresentation user) {
        log.info("Processing user " + user.getUsername() + "(" + user.getId() + ")");
        // TODO

    }

}
