package com.trodix.duckcloud.presentation.controllers;

import com.trodix.duckcloud.domain.services.NodeService;
import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.presentation.dto.mappers.NodeMapper;
import com.trodix.duckcloud.presentation.dto.mappers.TreeNodeMapper;
import com.trodix.duckcloud.presentation.dto.requests.NodeRequest;
import com.trodix.duckcloud.presentation.dto.requests.PermissionRequest;
import com.trodix.duckcloud.presentation.dto.responses.ExtendedPermissionResponse;
import com.trodix.duckcloud.presentation.dto.responses.NodeResponse;
import com.trodix.duckcloud.presentation.dto.responses.PermissionResponse;
import com.trodix.duckcloud.presentation.dto.responses.TreeNodeResponse;
import com.trodix.duckcloud.security.annotations.AuthResourceId;
import com.trodix.duckcloud.security.annotations.Authorization;
import com.trodix.duckcloud.security.models.PermissionType;
import com.trodix.duckcloud.security.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/nodes")
@RequiredArgsConstructor
@Slf4j
public class NodeController {

    private final NodeService nodeService;

    private final NodeMapper nodeMapper;

    private final TreeNodeMapper treeNodeMapper;

    private final Enforcer enforcer;

    private final AuthenticationService authenticationService;

    @GetMapping("/{id}")
    public NodeResponse getOneWithParentRecursive(@PathVariable Long id) {
        return nodeMapper.toDto2(nodeService.getOneNodeWithRecursiveParents(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Node not found for id " + id)));
    }

    @GetMapping("")
    public List<NodeResponse> getAll() {
        List<Node> result = nodeService.getAll();
        List<NodeResponse> response = nodeMapper.toDto(result);
        return response;
    }

    @GetMapping("/tree/{parentId}")
    public List<TreeNodeResponse> getTreePrimaryChildren(@PathVariable Long parentId) {
        return treeNodeMapper.toDto(nodeService.buildTreeFromParent(parentId));
    }

    @GetMapping("/{parentId}/children")
    public List<NodeResponse> getChildren(@PathVariable Long parentId) {
        return nodeMapper.toDto2(nodeService.getChildrenWithPath(parentId));
    }

    @PostMapping("")
    @Authorization(resourceType = "feature:node", permissionType = PermissionType.WRITE)
    public void create(@RequestBody @Valid NodeRequest request) {

        final Node data = nodeMapper.toEntity(request);
        nodeService.create(data);
    }

    @PutMapping("/{id}")
    @Authorization(resourceType = "feature:node", permissionType = PermissionType.WRITE)
    public void update(@PathVariable @AuthResourceId Long id, @RequestBody @Valid NodeRequest request) {

        final Node data = nodeMapper.toEntity(request);
        data.setId(id);
        nodeService.update(data);
    }

    @DeleteMapping("/{id}")
    @Authorization(resourceType = "feature:node", permissionType = PermissionType.DELETE)
    public void delete(@PathVariable @AuthResourceId Long id) {
        nodeService.delete(id);
    }

    @GetMapping("/permitted-actions")
    public Set<String> getPermitedActionsOnNodeDomainForUser() {
        String userId = authenticationService.getUserId();
        return enforcer.getPermittedActions(userId, "feature:node");
    }

    @GetMapping("/{id}/permitted-actions")
    @Authorization(resourceType = "feature:node", permissionType = PermissionType.READ)
    public Set<String> getPermitedActionsOnNodeForUser(@PathVariable @AuthResourceId Long id) {
        String userId = authenticationService.getUserId();
        return SetUtils.union(
                enforcer.getPermittedActions(userId, String.format("feature:node:%s", id)),
                enforcer.getPermittedActions(userId, "feature:node")
        );
    }

    @PostMapping("/permissions")
    public ResponseEntity<Void> addPermission(@Valid @RequestBody List<PermissionRequest> request) {

        String userId = authenticationService.getUserId();

        for (PermissionRequest permission : request) {

            if (!enforcer.enforce(userId, "feature:node", permission.getAct())) {

                log.error("User {} has been forbidden to grant action {} on object {} to subject {}",
                        userId, permission.getAct(), permission.getObj(), permission.getSub());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            enforcer.addPolicy(permission.getSub(), permission.getObj(), permission.getAct());
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/remove-permissions")
    public ResponseEntity<Void> removePermission(@Valid @RequestBody List<PermissionRequest> request) {

        String userId = authenticationService.getUserId();

        for (PermissionRequest permission : request) {

            if (!(enforcer.enforce(userId, "feature:node", permission.getAct()) || enforcer.enforce(userId, permission.getObj(), permission.getAct()))) {

                log.error("User {} has been forbidden to remove permission {} on object {} to subject {}",
                        userId, permission.getAct(), permission.getObj(), permission.getSub());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            enforcer.removePolicy(permission.getSub(), permission.getObj(), permission.getAct());
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}/permissions")
    @Authorization(resourceType = "feature:node", permissionType = PermissionType.READ)
    public List<ExtendedPermissionResponse> getPermissionsOnNode(@PathVariable @AuthResourceId Long id) {

        String userId = authenticationService.getUserId();
        List<String> subjects = enforcer.getAllSubjects();

        String resourceDomain = String.format("feature:node", id);
        String scope = String.format("%s:%s", resourceDomain, id);

        List<ExtendedPermissionResponse> authorizedSubjects = new ArrayList<>();

        for (String subject : subjects) {
            List<String> actions = enforcer.getAllActions();

            for (String action : actions) {
                boolean isAuthorizedByDomain = enforcer.enforce(subject, resourceDomain, action);
                boolean isAuthorizedByResource = enforcer.enforce(subject, scope, action);
                if (isAuthorizedByDomain || isAuthorizedByResource) {
                    String currentSub = isAuthorizedByDomain ? resourceDomain : scope;
                    boolean currentUserHasPermission = enforcer.enforce(userId, resourceDomain, action) || enforcer.enforce(userId, scope, action);

                    PermissionResponse authorizedSub = new PermissionResponse(subject, currentSub, action);

                    ExtendedPermissionResponse item = new ExtendedPermissionResponse(authorizedSub, currentUserHasPermission);

                    authorizedSubjects.add(item);
                }
            }
        }

        return authorizedSubjects;
    }

    @GetMapping("/{id}/user-permissions")
    @Authorization(resourceType = "feature:node", permissionType = PermissionType.READ)
    public List<PermissionResponse> getPermissionsOnNodeForUser(@PathVariable @AuthResourceId Long id) {

        String userId = authenticationService.getUserId();

        String resourceDomain = String.format("feature:node", id);
        String scope = String.format("%s:%s", resourceDomain, id);

        List<PermissionResponse> authorizedSubjects = new ArrayList<>();
        List<String> actions = enforcer.getAllActions();

        for (String action : actions) {
            boolean isAuthorizedByDomain = enforcer.enforce(userId, resourceDomain, action);
            boolean isAuthorizedByResource = enforcer.enforce(userId, scope, action);
            if (isAuthorizedByDomain || isAuthorizedByResource) {
                String currentSub = isAuthorizedByDomain ? resourceDomain : scope;
                PermissionResponse authorizedSub = new PermissionResponse(userId, currentSub, action);

                authorizedSubjects.add(authorizedSub);
            }
        }

        return authorizedSubjects;
    }

    @GetMapping("/{id}/not-authorized-subjects/{action}")
    @Authorization(resourceType = "feature:node", permissionType = PermissionType.READ)
    public Set<String> getUnAuthorizedSubjectsOnNodeAndAction(@PathVariable @AuthResourceId Long id, @PathVariable String action) {

        Set<String> unauthorizedSubjects = new HashSet<>();

        List<String> subjects = enforcer.getAllSubjects();

        for (String subject : subjects) {
            if (!(enforcer.enforce(subject, "feature:node", action) || enforcer.enforce(subject, String.format("feature:node:%s", id), action))) {
                unauthorizedSubjects.add(subject);
            }
        }

        return unauthorizedSubjects;
    }

}
