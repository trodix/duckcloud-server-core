package com.trodix.duckcloud.core.presentation.controllers;

import com.trodix.duckcloud.core.business.services.NodeService;
import com.trodix.duckcloud.core.persistance.entities.Node;
import com.trodix.duckcloud.core.presentation.dto.mappers.NodeMapper;
import com.trodix.duckcloud.core.presentation.dto.requests.NodeRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class NodeController {

    private final NodeService nodeService;

    private final NodeMapper nodeMapper;

    @PostMapping("/nodes")
    public void createNode(@RequestBody NodeRequest request) {

        final Node data = nodeMapper.toEntity(request);
        nodeService.createNode(data);
    }

}
