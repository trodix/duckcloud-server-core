package com.trodix.duckcloud.connectors.onlyoffice.config;

import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACVerifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
@Slf4j
public class OnlyOfficeJwtAuthProvider implements AuthenticationProvider {

    @Value("${app.onlyoffice.jwt.secret}")
    private String onlyOfficeJwtSecret;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String bearer = ((BearerTokenAuthenticationToken) authentication).getToken();
        String kid = null;

        // check if the bearer is a OnlyOffice JWT or a Keycloak JWT
        try {
            Verifier verifier = HMACVerifier.newVerifier(onlyOfficeJwtSecret);
            JWT jwt = JWT.getDecoder().decode(bearer, verifier);
            kid = jwt.header.getString("kid");
        } catch (RuntimeException e) {
            log.debug(e.getMessage());
        }
        // if the bearer is a Keycloak JWT (from oAuth2 server)
        if (!StringUtils.isBlank(kid)) {
            log.debug("Authentication delegated to oauth2 server");
            return null;
        }

        // if the bearer is a OnlyOffice JWT
        try {
            Verifier verifier = HMACVerifier.newVerifier(onlyOfficeJwtSecret);
            JWT jwtOnlyOfficeFormat = JWT.getDecoder().decode(bearer, verifier);

            Jwt jwtSpringFormat = Jwt
                    .withTokenValue(jwtOnlyOfficeFormat.toString())
                    .header("typ", jwtOnlyOfficeFormat.header.type)
                    .header("alg", jwtOnlyOfficeFormat.header.algorithm.getName())
                    .claims((claims) -> claims.putAll(jwtOnlyOfficeFormat.getAllClaims()))
                    .issuedAt(jwtOnlyOfficeFormat.issuedAt.toInstant())
                    .expiresAt(jwtOnlyOfficeFormat.expiration.toInstant())
                    .build();

            log.debug("Successfully authenticated from OnlyOffice JWT");

            Collection<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ONLYOFFICE_WEBSERVICE"));

            return new JwtAuthenticationToken(jwtSpringFormat, grantedAuthorities);
        } catch (JwtException e) {
            log.debug("Invalid JWT token", e);
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(BearerTokenAuthenticationToken.class);
    }

}
