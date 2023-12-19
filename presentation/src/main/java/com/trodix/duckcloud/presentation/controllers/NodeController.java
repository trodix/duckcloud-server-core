package com.trodix.duckcloud.presentation.controllers;

import com.trodix.casbinserver.annotations.AuthResourceId;
import com.trodix.casbinserver.annotations.Authorization;
import com.trodix.casbinserver.client.api.v1.EnforcerApi;
import com.trodix.casbinserver.models.PermissionType;
import com.trodix.duckcloud.domain.models.ContentModel;
import com.trodix.duckcloud.domain.models.NodeWithPath;
import com.trodix.duckcloud.domain.services.NodeService;
import com.trodix.duckcloud.domain.utils.ModelUtils;
import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.persistance.pagination.Pagination;
import com.trodix.duckcloud.persistance.pagination.PaginationResult;
import com.trodix.duckcloud.presentation.dto.mappers.NodeMapper;
import com.trodix.duckcloud.presentation.dto.mappers.TreeNodeMapper;
import com.trodix.duckcloud.presentation.dto.requests.NodeMoveRequest;
import com.trodix.duckcloud.presentation.dto.requests.NodeRequest;
import com.trodix.duckcloud.presentation.dto.requests.PolicyDto;
import com.trodix.duckcloud.presentation.dto.responses.ExtendedPermissionResponse;
import com.trodix.duckcloud.presentation.dto.responses.NodeResponse;
import com.trodix.duckcloud.presentation.dto.responses.PermissionResponse;
import com.trodix.duckcloud.presentation.dto.responses.TreeNodeResponse;
import com.trodix.duckcloud.security.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
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

    private final EnforcerApi enforcer;

    private final AuthenticationService authenticationService;

    @GetMapping("/{id}")
    public NodeResponse getOneWithParentRecursive(@PathVariable Long id) {
        return nodeMapper.toDto2(nodeService.getOneNodeWithRecursiveParents(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Node not found for id " + id)));
    }

    @GetMapping("/tree/{parentId}")
    public List<TreeNodeResponse> getTreePrimaryChildren(@PathVariable Long parentId) {
        // TODO pagination
        return treeNodeMapper.toDto(nodeService.buildTreeFromParent(parentId));
    }

    @GetMapping("/{parentId}/children")
    public PaginationResult<List<NodeResponse>> getChildren(@PathVariable Long parentId, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "50") int limit) {
        PaginationResult<List<NodeWithPath>> result = nodeService.getChildrenWithPath(parentId, new Pagination(offset, limit));
        return new PaginationResult<>(
                result.getOffset(),
                result.getPageSize(),
                result.getTotal(),
                result.getEntries()
                    .stream()
                    .map(t -> nodeMapper.toDto2(t))
                    .toList()
        );
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
        final Node nodeInDB = nodeService.getOne(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Node with id " + id + " not found"));
        data.setId(id);
        if (!nodeInDB.getParentId().equals(data.getParentId())) {
            new HttpClientErrorException(HttpStatus.BAD_REQUEST, "To move the node, use the move API");
        }
        nodeService.update(data);
    }

    @PutMapping("/{id}/move")
    @Authorization(resourceType = "feature:node", permissionType = PermissionType.WRITE)
    public void move(@PathVariable @AuthResourceId Long id, @RequestBody @Valid NodeMoveRequest request) {

        final Node source = nodeService.getOne(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Source node not found with id " + id));

        final Node destination = nodeService.getOne(request.getDestinationId())
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Destination node not found with id " + request.getDestinationId()));

        if (!ModelUtils.isDirectoryType(destination)) {
            throw new IllegalArgumentException("Destination node must be of type " + ContentModel.TYPE_DIRECTORY);
        }

        source.setParentId(destination.getId());
        nodeService.update(source);
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
    public ResponseEntity<Void> addPermission(@Valid @RequestBody List<PolicyDto> request) {

        String userId = authenticationService.getUserId();

        for (PolicyDto permission : request) {

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
    public ResponseEntity<Void> removePermission(@Valid @RequestBody List<PolicyDto> request) {

        String userId = authenticationService.getUserId();

        for (PolicyDto permission : request) {

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
