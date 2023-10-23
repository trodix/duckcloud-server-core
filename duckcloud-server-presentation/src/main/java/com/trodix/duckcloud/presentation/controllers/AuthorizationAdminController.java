package com.trodix.duckcloud.presentation.controllers;

import com.trodix.casbinserver.client.api.v1.EnforcerApi;
import com.trodix.duckcloud.presentation.dto.requests.AddPolicyRequest;
import com.trodix.duckcloud.presentation.dto.requests.AddRoleForUserRequest;
import com.trodix.duckcloud.presentation.dto.responses.UserResponse;
import com.trodix.duckcloud.security.services.KeycloakService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/security")
@RequiredArgsConstructor
@Slf4j
public class AuthorizationAdminController {

    private final EnforcerApi enforcer;

    private final KeycloakService keycloakService;

    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        List<UserRepresentation> users = keycloakService.fetchUsers();
        List<UserResponse> usersResponse = users.stream().map(u -> new UserResponse(u.getId(), u.getUsername())).toList();

        return usersResponse;
    }

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

    @PostMapping("/add-policy")
    public void addPolicy(@RequestBody @Valid AddPolicyRequest request) {
        enforcer.addPolicy(request.getSub(), request.getObj(), request.getAct());
    }

    @PostMapping("/add-role-for-user")
    public void assignUserToGroup(@RequestBody @Valid AddRoleForUserRequest request) {
        enforcer.addRoleForUser(request.getUser(), request.getRole());
    }

}
