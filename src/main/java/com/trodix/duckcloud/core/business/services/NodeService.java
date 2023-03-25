package com.trodix.duckcloud.core.business.services;

import com.trodix.duckcloud.core.persistance.dao.NodeManager;
import com.trodix.duckcloud.core.persistance.entities.Node;
import com.trodix.duckcloud.core.persistance.entities.TreeNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public List<TreeNode> buildTreeFromParent(Long parentId) {
        List<TreeNode> treeNodeList = nodeManager.buildTreeFromParent(parentId);

        final List<TreeNode> copyList = new ArrayList<>(treeNodeList);

        copyList.forEach(element -> {
            treeNodeList
                    .stream()
                    .filter(parent -> parent.getNodeId() == element.getParentId() && parent.getNodeId() != element.getNodeId())
                    .findAny()
                    .ifPresent(parent -> {
                        parent.getChildren().add(element);
                    });
        });

        treeNodeList.subList(1, treeNodeList.size()).clear();

        return treeNodeList;
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
