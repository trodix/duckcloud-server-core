package com.trodix.duckcloud.core.config.security.filters;

import com.trodix.duckcloud.core.config.security.exceptions.InvalidUserException;
import com.trodix.duckcloud.core.config.security.services.AuthenticationService;
import com.trodix.duckcloud.core.config.security.utils.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.io.IOException;

/**
 * Filter that denies all request that not contains a Jwt with an email claim
 */
@Configuration
@AllArgsConstructor
@Slf4j
public class MandatoryJwtEmailClaimSecurityFilter extends HttpFilter {

    private final AuthenticationService authService;

    @Override
    protected void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        try {
            authService.getEmail();
        } catch (InvalidUserException e) {
            log.info("The JWT token did not contained the mandatory {} claim.", Claims.EMAIL.value);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (RuntimeException e) {
            // case when no security on the requested url
        }

        chain.doFilter(request, response);
    }

}
