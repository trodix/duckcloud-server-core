package com.trodix.duckcloud.core.business.services;

import com.trodix.duckcloud.core.persistance.dao.mappers.NodeMapper;
import com.trodix.duckcloud.core.persistance.entities.Node;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class NodeService {

    private final NodeMapper nodeMapper;

    public Node getOne(Long id) {
        return nodeMapper.findOne(id);
    }

    public List<Node> getAll() {
        return nodeMapper.findAll();
    }

    public void create(Node node) {
        nodeMapper.insert(node);
    }

    public void update(Node node) {
        nodeMapper.update(node);
    }

    public void delete(Long id) {
        nodeMapper.delete(id);
    }

}
