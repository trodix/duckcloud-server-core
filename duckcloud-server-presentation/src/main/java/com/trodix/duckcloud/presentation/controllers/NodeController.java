package com.trodix.duckcloud.presentation.controllers;

import com.trodix.duckcloud.domain.models.NodePath;
import com.trodix.duckcloud.domain.models.NodeWithPath;
import com.trodix.duckcloud.domain.services.NodeService;
import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.presentation.dto.mappers.NodeMapper;
import com.trodix.duckcloud.presentation.dto.mappers.TreeNodeMapper;
import com.trodix.duckcloud.presentation.dto.requests.NodeRequest;
import com.trodix.duckcloud.presentation.dto.responses.NodeResponse;
import com.trodix.duckcloud.presentation.dto.responses.TreeNodeResponse;
import com.trodix.duckcloud.security.annotations.AuthResourceId;
import com.trodix.duckcloud.security.annotations.Authorization;
import com.trodix.duckcloud.security.models.PermissionType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nodes")
@RequiredArgsConstructor
@Slf4j
public class NodeController {

    private final NodeService nodeService;

    private final NodeMapper nodeMapper;

    private final TreeNodeMapper treeNodeMapper;

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
    @Authorization(permissionType = PermissionType.CREATE, resourceType = Node.class)
    public void create(@RequestBody @Valid NodeRequest request) {

        final Node data = nodeMapper.toEntity(request);
        nodeService.create(data);
    }

    @PutMapping("/{id}")
    @Authorization(permissionType = PermissionType.UPDATE, resourceType = Node.class)
    public void update(@PathVariable @AuthResourceId Long id, @RequestBody @Valid NodeRequest request) {

        final Node data = nodeMapper.toEntity(request);
        data.setId(id);
        nodeService.update(data);
    }

    @DeleteMapping("/{id}")
    @Authorization(permissionType = PermissionType.DELETE, resourceType = Node.class)
    public void delete(@PathVariable @AuthResourceId Long id) {
        nodeService.delete(id);
    }

}
