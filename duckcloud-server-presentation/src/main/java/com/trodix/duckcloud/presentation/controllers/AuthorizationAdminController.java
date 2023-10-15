package com.trodix.duckcloud.presentation.controllers;

import com.trodix.casbinserver.client.api.v1.EnforcerApi;
import com.trodix.duckcloud.presentation.dto.requests.PermissionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/security")
@RequiredArgsConstructor
@Slf4j
public class AuthorizationAdminController {

    private final EnforcerApi enforcer;

    @GetMapping("/subjects")
    public List<String> getSubjects() {
        return enforcer.getAllSubjects();
    }

    @GetMapping("/roles")
    public List<String> getRoles() {
        return enforcer.getAllRoles();
    }

    @GetMapping("/objects")
    public List<String> getObjects() {
        return enforcer.getAllObjects();
    }

    @GetMapping("/actions")
    public List<String> getActions() {
        return enforcer.getAllActions();
    }

    @PostMapping("/permissions")
    public void addPermission(@RequestBody @Valid PermissionRequest request) {
        enforcer.addPolicy(request.getSub(), request.getObj(), request.getAct());
    }

}
