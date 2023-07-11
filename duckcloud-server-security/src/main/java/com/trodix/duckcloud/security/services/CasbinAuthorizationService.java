package com.trodix.duckcloud.security.services;

import lombok.RequiredArgsConstructor;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CasbinAuthorizationService {

    private final Enforcer casbinEnforcer;

    public boolean checkPermission(String subject, String object, String action) {
        return casbinEnforcer.enforce(subject, object, action);
    }

}
