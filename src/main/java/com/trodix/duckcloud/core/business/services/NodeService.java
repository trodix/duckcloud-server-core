package com.trodix.duckcloud.core.business.services;

import com.trodix.duckcloud.core.persistance.dao.mappers.NodeMapper;
import com.trodix.duckcloud.core.persistance.entities.Node;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class NodeService {

    private final NodeMapper nodeMapper;

    public void createNode(Node node) {
        nodeMapper.insertNode(node);
    }

}
