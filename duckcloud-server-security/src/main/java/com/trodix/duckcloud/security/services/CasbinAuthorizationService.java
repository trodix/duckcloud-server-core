package com.trodix.duckcloud.security.services;

import com.trodix.casbinserver.client.api.v1.EnforcerApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CasbinAuthorizationService {

    private final EnforcerApi enforcer;

    public boolean checkPermission(String subject, String object, String action) {
        return enforcer.enforce(subject, object, action);
    }

}
