package com.trodix.duckcloud.persistance.utils;

import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.persistance.entities.Property;
import com.trodix.duckcloud.persistance.entities.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;
import java.util.*;

@Slf4j
public class NodeUtils {

    public static List<String> tagsToNameList(List<Tag> tags) {
        if (tags == null) {
            return new ArrayList<>();
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

    public static boolean hasProperty(List<Property> properties, String propName) {
        return properties.stream().filter(p -> p.getPropertyName().equals(propName)).findAny().isPresent();
    }

    public static boolean hasProperty(Node node, String propName) {
        return node.getProperties().stream().filter(p -> p.getPropertyName().equals(propName)).findAny().isPresent();
    }

    public static void addProperties(Node node, List<Property> properties) {
        for (Property p : properties) {
            addProperty(node, p);
        }
    }

    public static void addProperty(Node node, Property property) {
        log.trace("addProperty: {}", property);
        if (node.getProperties() == null) {
            node.setProperties(new ArrayList<>());
        }

        if (hasProperty(node, property.getPropertyName())) {
            Property currentProperty = getProperty(node.getProperties(), property.getPropertyName()).orElse(property);
            removeProperty(node.getProperties(), property.getPropertyName());
            property.setId(currentProperty.getId());
            addProperty(node, property);
        } else {
            node.getProperties().add(property);
        }
    }

    public static void removeProperty(List<Property> properties, String propName) {
        log.trace("removeProperty: {}", propName);
        properties.removeIf(p -> p.getPropertyName().equals(propName));
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
            return p1.getDateVal() == p2.getDateVal();
        }

        return false;
    }

    public static Map<String, Serializable> toMapProperties(List<Property> properties) {
        Map<String, Serializable> target = new HashMap<>();

        for (Property property : properties) {
            String key = property.getPropertyName();

            if (property.getStringVal() != null) {
                target.put(key, property.getStringVal());
            } else if (property.getLongVal() != null) {
                target.put(key, property.getLongVal());
            } else if (property.getDoubleVal() != null) {
                target.put(key, property.getDoubleVal());
            } else if (property.getDateVal() != null) {
                target.put(key, property.getDateVal());
            }

        }

        return target;
    }

}
