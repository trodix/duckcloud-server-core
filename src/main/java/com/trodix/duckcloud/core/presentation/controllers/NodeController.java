package com.trodix.duckcloud.core.presentation.controllers;

import com.trodix.duckcloud.core.domain.services.NodeService;
import com.trodix.duckcloud.core.persistance.entities.Node;
import com.trodix.duckcloud.core.persistance.entities.TreeNode;
import com.trodix.duckcloud.core.presentation.dto.mappers.NodeMapper;
import com.trodix.duckcloud.core.presentation.dto.requests.NodeRequest;
import com.trodix.duckcloud.core.presentation.dto.responses.NodeResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nodes")
@AllArgsConstructor
@Slf4j
public class NodeController {

    private final NodeService nodeService;

    private final NodeMapper nodeMapper;

    @GetMapping("/{id}")
    public NodeResponse getOne(@PathVariable Long id) {
        Node result = nodeService.getOne(id);
        NodeResponse response = nodeMapper.toDto(result);
        return response;
    }

    @GetMapping("")
    public List<NodeResponse> getAll() {
        List<Node> result = nodeService.getAll();
        List<NodeResponse> response = nodeMapper.toDto(result);
        return response;
    }

    @GetMapping("/tree/{parentId}")
    public List<TreeNode> getTree(@PathVariable Long parentId) {
        return nodeService.buildTreeFromParent(parentId);
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
