package com.trodix.duckcloud.security;

import com.trodix.casbinserver.configuration.AuthorizedUserSubjectProvider;
import com.trodix.casbinserver.models.AuthorizedUserSubject;
import com.trodix.duckcloud.security.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CasbinConfig {

    private final AuthenticationService authenticationService;

    @Bean
    public AuthorizedUserSubjectProvider authorizedUserSubjectProvider() {
        return () -> {
            AuthorizedUserSubject subj = new AuthorizedUserSubject();
            subj.setId(authenticationService.getUserId());
            return subj;
        };
    }

}
