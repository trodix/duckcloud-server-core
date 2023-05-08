package com.trodix.duckcloud.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KeycloakClientConfig {

    @Value("${app.client.registration.name}")
    private String clientRegistrationName;

    @Value("${spring.security.oauth2.client.registration.market.client-id}")
    private String clientId;

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public String getAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest =
                OAuth2AuthorizeRequest
                        .withClientRegistrationId(clientRegistrationName)
                        .principal(clientId)
                        .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        return accessToken.getTokenValue();
    }

}
