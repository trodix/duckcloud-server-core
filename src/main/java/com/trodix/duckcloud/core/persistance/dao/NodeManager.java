package com.trodix.duckcloud.core.persistance.dao;

import com.trodix.duckcloud.core.persistance.dao.mappers.NodeMapper;
import com.trodix.duckcloud.core.persistance.dao.mappers.PropertyMapper;
import com.trodix.duckcloud.core.persistance.dao.mappers.TagMapper;
import com.trodix.duckcloud.core.persistance.dao.mappers.TypeMapper;
import com.trodix.duckcloud.core.persistance.entities.*;
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
public class NodeManager {

    private final NodeMapper nodeMapper;

    private final TypeMapper typeMapper;

    private final TagMapper tagMapper;

    private final PropertyMapper propertyMapper;

    public static List<String> tagsToNameList(List<Tag> tags) {
        return tags.stream().map(p -> p.getName()).toList();
    }

    public static List<String> propertiesToNameList(List<Property> properties) {
        return properties.stream().map(p -> p.getPropertyName()).toList();
    }

    public static Optional<Property> getProperty(List<Property> properties, String propName) {
        return properties.stream().filter(p -> p.getPropertyName().equals(propName)).findFirst();
    }

    public static boolean isPropertiesValueEquals(Property p1, Property p2) {
        if (!(new EqualsBuilder().append(p1.getPropertyName(), p2.getPropertyName()).isEquals())) {
            throw new IllegalArgumentException("Comparing different properties: " + p1.getPropertyName() + " with " + p2.getPropertyName());
        }

        if (p1.getStringVal() != null && p2.getStringVal() != null) {
            return p1.getStringVal().equals(p2.getStringVal());
        } else if (p1.getLongVal() != null && p2.getLongVal() != null) {
            return p1.getLongVal() == p2.getLongVal();
        } else if (p1.getDoubleVal() != null && p2.getDoubleVal() != null) {
            return p1.getDoubleVal() == p2.getDoubleVal();
        } else if (p1.getDateVal() != null && p2.getDateVal() != null) {
            return p1.getDateVal() != p2.getDateVal();
        }

        return false;
    }

    public Node findOne(Long id) {
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

        Collection<String> toInsertTagList = CollectionUtils.subtract(tagsToNameList(node.getTags()), tagsToNameList(existingTagsInDb));
        Collection<String> toDeleteAssocList = CollectionUtils.subtract(tagsToNameList(savedTagsForNode), tagsToNameList(node.getTags()));
        Collection<String> toInsertAssocList = CollectionUtils.subtract(tagsToNameList(node.getTags()), tagsToNameList(savedTagsForNode));

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

        Collection<String> toDeleteList = CollectionUtils.subtract(propertiesToNameList(existingPropertiesInDb), propertiesToNameList(node.getProperties()));
        Collection<String> toInsertList = CollectionUtils.subtract(propertiesToNameList(node.getProperties()), propertiesToNameList(existingPropertiesInDb));

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

        for (Property savedPropertyForNode : existingPropertiesInDb) {
            Property propertyOnNode = getProperty(node.getProperties(), savedPropertyForNode.getPropertyName()).get();
            if (propertiesToNameList(node.getProperties()).contains(savedPropertyForNode.getPropertyName())) {
                // property already exists
                if (!isPropertiesValueEquals(propertyOnNode, savedPropertyForNode)) {
                    // update property
                    propertyOnNode.setId(savedPropertyForNode.getId());
                    propertyOnNode.setNodeId(node.getId());
                    propertyMapper.update(propertyOnNode);
                }
            }
        }
    }

}
