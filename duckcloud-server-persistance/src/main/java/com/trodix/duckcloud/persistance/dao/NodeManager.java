package com.trodix.duckcloud.persistance.dao;

import com.trodix.duckcloud.persistance.dao.mappers.NodeMapper;
import com.trodix.duckcloud.persistance.dao.mappers.PropertyMapper;
import com.trodix.duckcloud.persistance.dao.mappers.TagMapper;
import com.trodix.duckcloud.persistance.dao.mappers.TypeMapper;
import com.trodix.duckcloud.persistance.entities.*;
import com.trodix.duckcloud.persistance.utils.NodeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
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

    public List<Node> findAllByNodeId(List<Long> ids) {
        return nodeMapper.findAllByNodeId(ids);
    }

    public List<TreeNode> buildTreeFromParent(Long parentId) {
        return nodeMapper.findTreeNodesByParentId(parentId);
    }

    public List<Tag> findSavedTagsForNode(Node node) {
        return tagMapper.findAllByNodeId(node.getId());
    }

    public List<Property> findSavedPropertiesForNode(Node node) {
        return propertyMapper.findAllByNodeId(node.getId());
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

}
