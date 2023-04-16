package com.trodix.duckcloud.persistance.entities;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Data
public class TreeNode {

    private Long nodeId;

    private Long parentId;

    private Integer nodeLevel;

    private String type;

    private String name;

    private List<String> nodePath;

    private List<Tag> tags;

    private List<Property> properties;

    private List<TreeNode> children = new ArrayList<>();

    public void setNodePath(String valuesSeparatedByComma) {
        if (this.nodePath == null) {
            this.nodePath = new LinkedList<>();
        }
        this.nodePath.addAll(Arrays.stream(valuesSeparatedByComma.split(",")).toList());
    }

}
