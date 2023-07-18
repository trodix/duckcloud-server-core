package com.trodix.duckcloud.presentation.controllers;

import com.trodix.duckcloud.security.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/security")
@RequiredArgsConstructor
@Slf4j
public class AuthorizationController {

    private final AuthenticationService authenticationService;

    private final Enforcer enforcer;

    @GetMapping("/roles")
    public List<String> getRoles() {
        String userId = authenticationService.getUserId();
        return enforcer.getAllRoles();
    }

    @GetMapping("/roles/-me-")
    public List<String> getRolesForUser() {
        String userId = authenticationService.getUserId();
        return enforcer.getRolesForUser(userId);
    }

    @GetMapping("/permissions")
    public List<List<String>> getObjects() {
        String userId = authenticationService.getUserId();
        return enforcer.getImplicitPermissionsForUser(userId);
    }

}
