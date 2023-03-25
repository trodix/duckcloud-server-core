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

        // associate node properties to tree items
        List<Long> nodeIdsInTree = treeNodeList.stream().map(n -> n.getNodeId()).toList();
        List<Node> nodeList = nodeManager.findAllByNodeId(nodeIdsInTree);

        nodeList.forEach(node -> {
            treeNodeList.stream()
                    .filter(treeNode -> treeNode.getNodeId() == node.getId())
                    .findAny()
                    .ifPresent(nodeTree -> {
                        nodeTree.setName(
                                node.getProperties().stream()
                                        .filter(p -> p.getPropertyName().equals("cm:name")).findAny().get().getStringVal());

                        nodeTree.setType(node.getType().getName());
                    });
        });

        // build nested tree
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
