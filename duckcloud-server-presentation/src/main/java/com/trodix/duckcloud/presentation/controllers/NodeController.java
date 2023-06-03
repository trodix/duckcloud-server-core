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
    public void create(@RequestBody @Valid NodeRequest request) {

        final Node data = nodeMapper.toEntity(request);
        nodeService.create(data);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody @Valid NodeRequest request) {

        final Node data = nodeMapper.toEntity(request);
        data.setId(id);
        nodeService.update(data);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        nodeService.delete(id);
    }

}
