package com.trodix.duckcloud.presentation.controllers;

import com.trodix.casbinserver.client.api.v1.EnforcerApi;
import com.trodix.duckcloud.presentation.dto.requests.AddRoleForUserRequest;
import com.trodix.duckcloud.presentation.dto.requests.GroupingPolicyDto;
import com.trodix.duckcloud.presentation.dto.requests.PolicyDto;
import com.trodix.duckcloud.presentation.dto.responses.PermissionResponse;
import com.trodix.duckcloud.presentation.dto.responses.UserResponse;
import com.trodix.duckcloud.security.services.KeycloakService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.NotAcceptableStatusException;

import java.util.ArrayList;
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
    public void addPolicy(@RequestBody @Valid PolicyDto request) {
        enforcer.addPolicy(request.getSub(), request.getObj(), request.getAct());
    }

    @PostMapping("/add-role-for-user")
    public void assignUserToGroup(@RequestBody @Valid AddRoleForUserRequest request) {
        if (request.getUser().equals(request.getRole())) {
            throw new NotAcceptableStatusException("A subject can not be assigned to itself");
        }
        enforcer.addRoleForUser(request.getUser(), request.getRole());
    }

    @GetMapping("/permissions")
    public List<PermissionResponse> getAllPermissions() {

        List<String> subjects = enforcer.getAllSubjects();
        List<String> objects = enforcer.getAllObjects();
        List<String> actions = enforcer.getAllActions();

        List<PermissionResponse> authorizedSubjects = new ArrayList<>();

        for (String object : objects) {
            for (String subject : subjects) {
                for (String action : actions) {
                    boolean isAuthorizedByDomain = enforcer.enforce(subject, object, action);
                    if (isAuthorizedByDomain) {
                        PermissionResponse authorizedSub = new PermissionResponse(subject, object, action);
                        authorizedSubjects.add(authorizedSub);
                    }
                }
            }
        }

        return authorizedSubjects;
    }

    @PostMapping("/remove-policy")
    public void removePolicy(PolicyDto policy) {
        enforcer.removePolicy(policy.getSub(), policy.getSub(), policy.getAct());
    }

    @PostMapping("/permissions-for-user")
    public List<PolicyDto> getImplicitPermissionsForUser(@RequestBody String user) {
        return enforcer.getImplicitPermissionsForUser(user).stream()
                .map(p -> new PolicyDto(p.get(0), p.get(1), p.get(2)))
                .toList();
    }

    @GetMapping("/grouping-policy")
    public List<GroupingPolicyDto> getGroupingPolicies() {
        return enforcer.getGroupingPolicy().stream()
                .map(p -> new GroupingPolicyDto(p.get(0), p.get(1)))
                .toList();
    }

}
