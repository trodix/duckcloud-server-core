package com.trodix.duckcloud.security.services;

import com.trodix.duckcloud.security.exceptions.InvalidUserException;
import com.trodix.duckcloud.security.utils.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    public static final String DEFAULT_USER = "System";

    public Jwt getJwt() throws RuntimeException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof final Jwt jwt) {
            return jwt;
        }

        throw new RuntimeException("Principal is not an instance of Jwt");
    }

    public String getEmail() throws InvalidUserException {
        if (getJwt().hasClaim(Claims.EMAIL.value)) {
            log.trace("Claim {} found.", Claims.EMAIL.value);
            return getJwt().getClaim(Claims.EMAIL.value);
        }

        throw new InvalidUserException("Claim " + Claims.EMAIL.value + " not found from Jwt");
    }

    public String getUserId() {
        return getJwt().getSubject();
    }

    public String getName() {
        return getJwt().getClaim(Claims.NAME.value);
    }

}
