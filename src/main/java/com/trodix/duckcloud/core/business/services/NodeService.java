package com.trodix.duckcloud.core.business.services;

import com.trodix.duckcloud.core.persistance.dao.NodeManager;
import com.trodix.duckcloud.core.persistance.dao.mappers.NodeMapper;
import com.trodix.duckcloud.core.persistance.dao.mappers.PropertyMapper;
import com.trodix.duckcloud.core.persistance.dao.mappers.TagMapper;
import com.trodix.duckcloud.core.persistance.dao.mappers.TypeMapper;
import com.trodix.duckcloud.core.persistance.entities.Node;
import com.trodix.duckcloud.core.persistance.entities.Property;
import com.trodix.duckcloud.core.persistance.entities.Tag;
import com.trodix.duckcloud.core.persistance.entities.Type;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class NodeService {

    private final NodeManager nodeManager;

    public Node getOne(Long id) {
        return nodeManager.findOne(id);
    }

    public List<Node> getAll() {
        return nodeManager.findAll();
    }

    public void create(Node node) {
        nodeManager.create(node);
    }

    public void update(Node node) {
        nodeManager.update(node);
    }

    public void delete(Long id) {
        nodeManager.delete(id);
    }

}
