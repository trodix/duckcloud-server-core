package com.trodix.duckcloud.persistance.dao;

import com.trodix.duckcloud.persistance.dao.mappers.NodeMapper;
import com.trodix.duckcloud.persistance.dao.mappers.PropertyMapper;
import com.trodix.duckcloud.persistance.dao.mappers.TagMapper;
import com.trodix.duckcloud.persistance.dao.mappers.TypeMapper;
import com.trodix.duckcloud.persistance.entities.*;
import com.trodix.duckcloud.persistance.pagination.Pagination;
import com.trodix.duckcloud.persistance.pagination.PaginationResult;
import com.trodix.duckcloud.persistance.utils.NodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NodeManager {

    private final NodeMapper nodeMapper;

    private final TypeMapper typeMapper;

    private final TagMapper tagMapper;

    private final PropertyMapper propertyMapper;


    public Optional<Node> findOne(Long id) {
        return nodeMapper.findOne(id);
    }

    public List<Node> findAll() {
        return nodeMapper.findAll();
    }

    public PaginationResult<List<Node>> findAllTypeContent(Pagination pagination) {
        return new PaginationResult(
                pagination.getOffset(),
                pagination.getPageSize(),
                (int) nodeMapper.count(),
                nodeMapper.findAllTypeContentPaginated(pagination)
        );
    }

    public List<Node> findAllByNodeId(List<Long> ids) {
        List<Node> result = nodeMapper.findAllByNodeId(ids);
        
        result.sort(Comparator.comparing(parent -> ids.indexOf(parent.getId())));

        return result;
    }

    public List<Node> findAllByParentId(Long parentId) {
        return nodeMapper.findAllByParentId(parentId);
    }

    public PaginationResult<List<Node>> findAllByParentId(Long parentId, Pagination pagination) {
        return new PaginationResult(
                pagination.getOffset(),
                pagination.getPageSize(),
                (int) nodeMapper.countByParentId(parentId),
                nodeMapper.findAllByParentIdPaginated(parentId, pagination)
        );
    }

    public List<TreeNode> buildTreeFromParent(Long parentId) {
        return nodeMapper.findTreeNodesByParentId(parentId);
    }

    public List<TreeNode> buildTreeWithRecursiveParents(Long nodeId) {
        return nodeMapper.findRecursiveNodeParents(nodeId);
    }

    public List<Tag> findSavedTagsForNode(Node node) {
        return tagMapper.findAllByNodeId(node.getId());
    }

    public List<Property> findSavedPropertiesForNode(Node node) {
        return propertyMapper.findAllByNodeId(node.getId());
    }

    public long count() {
        return nodeMapper.count();
    }

    @Transactional
    public void create(Node node) {
        String typeName = node.getType().getName();
        Type type = typeMapper.findOneByName(typeName);
        if (type == null) {
            throw new IllegalArgumentException("Type " + typeName + " not found");
        }
        node.setType(type);
        nodeMapper.insert(node);
        updateAssociatedTags(node);
        updateAssociatedProperties(node);
    }

    @Transactional
    public void update(Node node) {
        String typeName = node.getType().getName();
        Type type = typeMapper.findOneByName(typeName);
        if (type == null) {
            throw new IllegalArgumentException("Type " + typeName + " not found");
        }
        node.setType(type);
        nodeMapper.update(node);
        updateAssociatedTags(node);
        updateAssociatedProperties(node);
    }

    @Transactional
    public void delete(Long id) {
        nodeMapper.deleteAllNodeTagAssociations(id);
        propertyMapper.deleteByNodeId(id);
        nodeMapper.delete(id);
    }

    @Transactional
    protected void updateAssociatedTags(Node node) {

        if (node.getId() == null) {
            throw new IllegalArgumentException("Node id should not be null");
        }

        List<Tag> savedTagsForNode = findSavedTagsForNode(node);
        List<Tag> existingTagsInDb = tagMapper.findAll();

        Collection<String> toInsertTagList = CollectionUtils.subtract(NodeUtils.tagsToNameList(node.getTags()), NodeUtils.tagsToNameList(existingTagsInDb));
        Collection<String> toDeleteAssocList = CollectionUtils.subtract(NodeUtils.tagsToNameList(savedTagsForNode), NodeUtils.tagsToNameList(node.getTags()));
        Collection<String> toInsertAssocList = CollectionUtils.subtract(NodeUtils.tagsToNameList(node.getTags()), NodeUtils.tagsToNameList(savedTagsForNode));

        for (String toDeleteAssoc : toDeleteAssocList) {
            Tag toDeleteTagAssoc = savedTagsForNode.stream().filter(t -> t.getName().equals(toDeleteAssoc)).findAny().get();
            nodeMapper.deleteNodeTagAssociation(node.getId(), toDeleteTagAssoc.getId());
        }

        for (String toInsertTag : toInsertTagList) {
            Tag tagObj = node.getTags().stream().filter(t -> t.getName().equals(toInsertTag)).findAny().get();
            tagMapper.insert(tagObj);
            existingTagsInDb.add(tagObj);
        }

        for (String toInsertAssoc : toInsertAssocList) {
            Tag toInsertTagAssoc = node.getTags().stream().filter(t -> t.getName().equals(toInsertAssoc)).findAny().get();
            Long idFromDb = existingTagsInDb.stream().filter(t -> t.getName().equals(toInsertAssoc)).findAny().get().getId();
            toInsertTagAssoc.setId(idFromDb);
            nodeMapper.insertNodeTagAssociation(node.getId(), toInsertTagAssoc.getId());
        }

    }

    @Transactional
    protected void updateAssociatedProperties(Node node) {
        List<Property> existingPropertiesInDb = findSavedPropertiesForNode(node);

        Collection<String> toDeleteList = CollectionUtils.subtract(NodeUtils.propertiesToNameList(existingPropertiesInDb), NodeUtils.propertiesToNameList(node.getProperties()));
        Collection<String> toInsertList = CollectionUtils.subtract(NodeUtils.propertiesToNameList(node.getProperties()), NodeUtils.propertiesToNameList(existingPropertiesInDb));

        for (String toDelete : toDeleteList) {
            Property toDeleteProperty = existingPropertiesInDb.stream().filter(p -> p.getPropertyName().equals(toDelete)).findAny().get();
            propertyMapper.delete(toDeleteProperty.getId());
            existingPropertiesInDb.remove(toDeleteProperty);
        }

        for (String toInsert : toInsertList) {
            Property toInsertProperty = node.getProperties().stream().filter(p -> p.getPropertyName().equals(toInsert)).findAny().get();
            toInsertProperty.setNodeId(node.getId());
            propertyMapper.insert(toInsertProperty);
            existingPropertiesInDb.add(toInsertProperty);
        }

        // update properties
        for (Property property : node.getProperties()) {
            for (Property dbProperty : existingPropertiesInDb) {
                if (property.getPropertyName().equals(dbProperty.getPropertyName()) && !NodeUtils.isPropertiesValueEquals(property, dbProperty)) {
                    property.setId(dbProperty.getId());
                    property.setNodeId(dbProperty.getNodeId());
                    propertyMapper.update(property);
                }
            }
        }

    }

    /**
     * Get a string representation of the path of a node (the requested node is not part of the path)
     * @param nodeId
     * @return a string representation of the path of a node
     */
    public String getPath(long nodeId) {
        List<List<String>> tree = buildTreeWithRecursiveParents(nodeId).stream().map(tn -> tn.getNodePath()).toList();
        if (tree.isEmpty()) {
            log.debug("tree has empty while getting path for nodeId {}", nodeId);
            throw new RuntimeException("No path found for nodeId " + nodeId);
        }
        List<String> pathIds = tree.get(0);
        List<Node> parentNodes = findAllByNodeId(pathIds.stream().map(p -> Long.valueOf(p)).toList());

        String path = parentNodes.stream()
                .filter(pathPart -> !pathPart.getId().equals(nodeId))
                .map(n -> NodeUtils.getProperty(n.getProperties(), "cm:name").get().getStringVal())
                .collect(Collectors.joining("/"));

        log.debug("Path found for nodeId {}: \n{}", nodeId, path);

        return path;
    }

}
