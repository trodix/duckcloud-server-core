package com.trodix.duckcloud.connectors.onlyoffice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class OnlyOfficeSecurityConfig {

    @Bean
    public SecurityFilterChain onlyOfficefilter(final HttpSecurity http) throws Exception {
        // FIXME
        http.authorizeHttpRequests()
                .requestMatchers("/api/v1/integration/onlyoffice/**")
                .permitAll()
                .anyRequest()
                .authenticated();

        return http.build();
    }

}
