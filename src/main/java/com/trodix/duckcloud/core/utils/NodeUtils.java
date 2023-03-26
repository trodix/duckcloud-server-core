package com.trodix.duckcloud.core.utils;

import com.trodix.duckcloud.core.domain.models.ContentModel;
import com.trodix.duckcloud.core.persistance.entities.Node;
import com.trodix.duckcloud.core.persistance.entities.Property;
import com.trodix.duckcloud.core.persistance.entities.Tag;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NodeUtils {

    public static List<String> tagsToNameList(List<Tag> tags) {
        if (tags == null) {
            return Collections.emptyList();
        }
        return tags.stream().map(p -> p.getName()).toList();
    }

    public static List<String> propertiesToNameList(List<Property> properties) {
        return properties.stream().map(p -> p.getPropertyName()).toList();
    }

    public static Optional<Property> getProperty(List<Property> properties, String propName) {
        if (properties == null) {
            return Optional.empty();
        }
        return properties.stream().filter(p -> p.getPropertyName().equals(propName)).findFirst();
    }

    public static void addProperty(Node node, Property property) {
        if (node.getProperties() == null) {
            node.setProperties(new ArrayList<>());
        }
        node.getProperties().add(property);
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

    public static boolean isContentType(Node node) {
        if (node.getType() == null) {
            return false;
        }
        return ContentModel.TYPE_CONTENT.equals(node.getType().getName());
    }

    public static boolean isDirectoryType(Node node) {
        if (node.getType() == null) {
            return false;
        }
        return ContentModel.TYPE_DIRECTORY.equals(node.getType().getName());
    }

}
